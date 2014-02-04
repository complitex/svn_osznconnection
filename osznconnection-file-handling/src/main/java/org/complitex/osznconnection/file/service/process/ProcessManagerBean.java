package org.complitex.osznconnection.file.service.process;

import com.google.common.collect.ImmutableMap;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.IExecutorObject;
import org.complitex.dictionary.entity.Log;
import org.complitex.dictionary.service.ConfigBean;
import org.complitex.dictionary.service.LogBean;
import org.complitex.dictionary.service.executor.ExecutorBean;
import org.complitex.dictionary.service.executor.IExecutorListener;
import org.complitex.dictionary.service.executor.ITaskBean;
import org.complitex.dictionary.util.EjbBeanLocator;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.FileHandlingConfig;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestFileGroup;
import org.complitex.osznconnection.file.service.RequestFileBean;
import org.complitex.osznconnection.file.service.RequestFileGroupBean;
import org.complitex.osznconnection.file.service.exception.StorageNotFoundException;
import org.complitex.osznconnection.file.service.warning.ReportWarningRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.complitex.osznconnection.file.entity.FileHandlingConfig.*;
import static org.complitex.osznconnection.file.service.process.ProcessType.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 01.11.10 12:55
 */
@Singleton(name = "ProcessManagerBean")
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class ProcessManagerBean {

    private final Logger log = LoggerFactory.getLogger(ProcessManagerBean.class);
    @Resource
    private SessionContext sessionContext;
    @EJB
    private ExecutorBean executorBean;
    @EJB
    private ConfigBean configBean;
    @EJB
    private LogBean logBean;
    @EJB
    private ReportWarningRenderer reportWarningRenderer;
    @EJB
    private RequestFileGroupBean requestFileGroupBean;
    @EJB
    private RequestFileBean requestFileBean;
    private Map<String, Map<ProcessType, Process>> processStatusMap = new ConcurrentHashMap<String, Map<ProcessType, Process>>();

    private Process getProcess(ProcessType processType) {
        //Principal Name
        String principalName = sessionContext.getCallerPrincipal().getName();

        Map<ProcessType, Process> map = processStatusMap.get(principalName);

        Process process = null;

        if (map != null) {
            process = map.get(processType);
        } else {
            map = new EnumMap<>(ProcessType.class);
            processStatusMap.put(principalName, map);
        }

        if (process == null) {
            process = new Process();
            map.put(processType, process);
        }

        return process;
    }

    private List<Process> getAllUsersProcess(ProcessType processType) {
        List<Process> processes = new ArrayList<Process>();

        for (Map<ProcessType, Process> map : processStatusMap.values()) {
            Process p = map.get(processType);

            if (p != null) {
                processes.add(p);
            }
        }

        return processes;
    }

    public List<RequestFile> getLinkError(ProcessType processType, boolean flush) {
        Process process = getProcess(processType);

        if (process != null) {
            return process.getLinkError(flush);
        }

        return Collections.emptyList();
    }

    public <T> List<T> getProcessed(ProcessType processType, Object queryKey) {
        return getProcess(processType).getProcessed(queryKey);
    }

    public int getSuccessCount(ProcessType processType) {
        return getProcess(processType).getSuccessCount();
    }

    public int getSkippedCount(ProcessType processType) {
        return getProcess(processType).getSkippedCount();
    }

    public int getErrorCount(ProcessType processType) {
        return getProcess(processType).getErrorCount();
    }

    public boolean isProcessing(ProcessType processType) {
        return getProcess(processType).isProcessing();
    }

    public boolean isCriticalError(ProcessType processType) {
        return getProcess(processType).isCriticalError();
    }

    public boolean isCompleted(ProcessType processType) {
        return getProcess(processType).isCompleted();
    }

    public boolean isCanceled(ProcessType processType) {
        return getProcess(processType).isCanceled();
    }

    public void cancel(ProcessType processType) {
        getProcess(processType).cancel();
    }

    public boolean isGlobalWaiting(ProcessType processType, IExecutorObject executorObject) {
        for (Process process : getAllUsersProcess(processType)) {
            if (process.isRunning() && process.isWaiting(executorObject)) {
                return true;
            }
        }

        return false;
    }

    public boolean isGlobalProcessing(ProcessType processType) {
        for (Process process : getAllUsersProcess(processType)) {
            if (process.isProcessing()) {
                return true;
            }
        }

        return false;
    }

    private void execute(ProcessType processType, Class<? extends ITaskBean> taskClass,
            List<? extends IExecutorObject> list, IExecutorListener listener,
            FileHandlingConfig threadCount, FileHandlingConfig maxErrorCount, Map processParameters) {
        Process process = getProcess(processType);

        process.getQueue().addAll(list);

        if (!process.isRunning()) {
            process.init();

            process.setMaxThread(configBean.getInteger(threadCount, true));
            process.setMaxErrors(configBean.getInteger(maxErrorCount, true));
            process.setTask(EjbBeanLocator.getBean(taskClass));
            process.setListener(listener);
            process.setCommandParameters(processParameters);

            process.getQueue().addAll(list);

            executorBean.execute(process);
        } else {
            int freeThreadCount = process.getMaxThread() - process.getRunningThreadCount();

            for (int i = 0; i < freeThreadCount; ++i) {
                executorBean.executeNext(process);
            }
        }
    }

    @Asynchronous
    public void loadGroup(long userOrganizationId, long osznId, int monthFrom, int monthTo, int year) {
        Process process = getProcess(LOAD_GROUP);

        try {
            //поиск групп файлов запросов
            if (!process.isRunning()) {
                process.setPreprocess(true);
                process.init();
            }

            LoadUtil.LoadGroupParameter loadParameter = LoadUtil.getLoadGroupParameter(userOrganizationId, osznId,
                    monthFrom, monthTo, year);

            for (RequestFileGroup fileGroup : loadParameter.getRequestFileGroups()) {
                fileGroup.getPaymentFile().setUserOrganizationId(userOrganizationId);
                fileGroup.getBenefitFile().setUserOrganizationId(userOrganizationId);
            }

            List<RequestFile> linkError = loadParameter.getLinkError();

            process.addLinkError(linkError);

            for (RequestFile rf : linkError) {
                logBean.error(Module.NAME, ProcessManagerBean.class, RequestFileGroup.class, null, rf.getId(),
                        Log.EVENT.CREATE, rf.getLogChangeList(), "Связанный файл не найден для объекта {0}",
                        rf.getLogObjectName());
            }

            process.getQueue().addAll(loadParameter.getRequestFileGroups());

            //загрузка данных
            if (!process.isRunning()) {
                process.setPreprocess(false);
                process.setMaxErrors(configBean.getInteger(LOAD_MAX_ERROR_COUNT, true));
                process.setMaxThread(configBean.getInteger(LOAD_THREAD_SIZE, true));
                process.setTask(EjbBeanLocator.getBean(GroupLoadTaskBean.class));

                executorBean.execute(process);
            } else {
                int freeThreadCount = process.getMaxThread() - process.getRunningThreadCount();

                for (int i = 0; i < freeThreadCount; ++i) {
                    executorBean.executeNext(process);
                }
            }
        } catch (StorageNotFoundException e) {
            process.preprocessError();

            log.error("Ошибка процесса загрузки файлов.", e);
            logBean.error(Module.NAME, ProcessManagerBean.class, RequestFileGroup.class, null,
                    Log.EVENT.CREATE, "Ошибка процесса загрузки файлов. Причина: {0}", e.getMessage());
        }
    }

    private List<RequestFileGroup> getGroups(List<Long> ids) {
        List<RequestFileGroup> groups = new ArrayList<RequestFileGroup>();

        for (Long id : ids) {
            RequestFileGroup group = requestFileGroupBean.getRequestFileGroup(id);

            if (group != null && !group.isProcessing() && !isGlobalWaiting(BIND_GROUP, group) && !isGlobalWaiting(FILL_GROUP, group)
                    && !isGlobalWaiting(SAVE_GROUP, group)) {
                groups.add(group);
            }
        }

        return groups;
    }

    private List<RequestFile> getActualPaymentFiles(List<Long> ids) {
        List<RequestFile> requestFiles = new ArrayList<RequestFile>();

        for (Long id : ids) {
            RequestFile requestFile = requestFileBean.findById(id);

            if (requestFile != null && !requestFile.isProcessing() && !isGlobalWaiting(BIND_ACTUAL_PAYMENT, requestFile)
                    && !isGlobalWaiting(FILL_ACTUAL_PAYMENT, requestFile) && !isGlobalWaiting(SAVE_ACTUAL_PAYMENT, requestFile)) {
                requestFiles.add(requestFile);
            }
        }
        return requestFiles;
    }

    private List<RequestFile> getSubsidyFiles(List<Long> ids) {
        List<RequestFile> requestFiles = new ArrayList<RequestFile>();

        for (Long id : ids) {
            RequestFile requestFile = requestFileBean.findById(id);

            if (requestFile != null && !requestFile.isProcessing() && !isGlobalWaiting(BIND_SUBSIDY, requestFile)
                    && !isGlobalWaiting(FILL_SUBSIDY, requestFile) && !isGlobalWaiting(SAVE_SUBSIDY, requestFile)) {
                requestFiles.add(requestFile);
            }
        }
        return requestFiles;
    }

    private List<RequestFile> getSubsidyFiles(List<DomainObject> balanceHolders, List<DomainObject> districts,
                                                       List<DomainObject> organizations){
        ArrayList<RequestFile> requestFiles = new ArrayList<>();




        return requestFiles;
    }

    private List<RequestFile> getDwellingCharacteristicsFiles(List<Long> ids) {
        List<RequestFile> requestFiles = new ArrayList<RequestFile>();

        for (Long id : ids) {
            RequestFile requestFile = requestFileBean.findById(id);

            if (requestFile != null && !requestFile.isProcessing() && !isGlobalWaiting(BIND_DWELLING_CHARACTERISTICS, requestFile)
                    && !isGlobalWaiting(FILL_DWELLING_CHARACTERISTICS, requestFile)
                    && !isGlobalWaiting(SAVE_DWELLING_CHARACTERISTICS, requestFile)) {
                requestFiles.add(requestFile);
            }
        }
        return requestFiles;
    }

    private List<RequestFile> getFacilityServiceTypeFiles(List<Long> ids) {
        List<RequestFile> requestFiles = new ArrayList<RequestFile>();

        for (Long id : ids) {
            RequestFile requestFile = requestFileBean.findById(id);

            if (requestFile != null && !requestFile.isProcessing() && !isGlobalWaiting(BIND_FACILITY_SERVICE_TYPE, requestFile)
                    && !isGlobalWaiting(FILL_FACILITY_SERVICE_TYPE, requestFile)
                    && !isGlobalWaiting(SAVE_FACILITY_SERVICE_TYPE, requestFile)) {
                requestFiles.add(requestFile);
            }
        }
        return requestFiles;
    }

    private List<RequestFile> getFacilityForm2Files(List<Long> ids) {
        List<RequestFile> requestFiles = new ArrayList<RequestFile>();

        for (Long id : ids) {
            RequestFile requestFile = requestFileBean.findById(id);

            if (requestFile != null && !requestFile.isProcessing()
                    && !isGlobalWaiting(FILL_FACILITY_FORM2, requestFile)
                    && !isGlobalWaiting(SAVE_FACILITY_FORM2, requestFile)) {
                requestFiles.add(requestFile);
            }
        }
        return requestFiles;
    }

    @Asynchronous
    public void bindGroup(List<Long> ids, Map processParameters) {
        execute(BIND_GROUP, GroupBindTaskBean.class, getGroups(ids), null, BIND_THREAD_SIZE, BIND_MAX_ERROR_COUNT, processParameters);
    }

    @Asynchronous
    public void fillGroup(List<Long> ids, Map processParameters) {
        execute(FILL_GROUP, GroupFillTaskBean.class, getGroups(ids), null, FILL_THREAD_SIZE, FILL_MAX_ERROR_COUNT, processParameters);
    }

    @Asynchronous
    public void saveGroup(List<Long> ids, Map processParameters) {
        IExecutorListener listener = new IExecutorListener() {

            @Override
            public void onComplete(List<IExecutorObject> processed) {
                try {
                    SaveUtil.createResult(processed, reportWarningRenderer);
                } catch (StorageNotFoundException e) {
                    log.error("Ошибка создания файла Result.txt.", e);
                    logBean.error(Module.NAME, ProcessManagerBean.class, RequestFileGroup.class, null,
                            Log.EVENT.CREATE, "Ошибка создания файла Result.txt. Причина: {0}", e.getMessage());
                }
            }
        };

        execute(SAVE_GROUP, GroupSaveTaskBean.class, getGroups(ids), listener, SAVE_THREAD_SIZE, SAVE_MAX_ERROR_COUNT, processParameters);
    }

    @Asynchronous
    public void loadActualPayment(long userOrganizationId, long osznId, int monthFrom, int monthTo, int year) {
        try {
            List<RequestFile> list = LoadUtil.getActualPayments(userOrganizationId, osznId, monthFrom, monthTo, year);

            for (RequestFile file : list) {
                file.setUserOrganizationId(userOrganizationId);
            }

            execute(LOAD_ACTUAL_PAYMENT, ActualPaymentLoadTaskBean.class, list, null, LOAD_THREAD_SIZE, LOAD_MAX_ERROR_COUNT, null);
        } catch (StorageNotFoundException e) {
            log.error("Ошибка процесса загрузки файлов.", e);
            logBean.error(Module.NAME, ProcessManagerBean.class, RequestFile.class, null,
                    Log.EVENT.CREATE, "Ошибка процесса загрузки файлов. Причина: {0}", e.getMessage());
        }
    }

    @Asynchronous
    public void bindActualPayment(List<Long> ids, Map processParameters) {
        execute(BIND_ACTUAL_PAYMENT, ActualPaymentBindTaskBean.class, getActualPaymentFiles(ids), null, BIND_THREAD_SIZE,
                BIND_MAX_ERROR_COUNT, processParameters);
    }

    @Asynchronous
    public void fillActualPayment(List<Long> ids, Map processParameters) {
        execute(FILL_ACTUAL_PAYMENT, ActualPaymentFillTaskBean.class, getActualPaymentFiles(ids), null, FILL_THREAD_SIZE,
                FILL_MAX_ERROR_COUNT, processParameters);
    }

    @Asynchronous
    public void saveActualPayment(List<Long> ids, Map processParameters) {
        execute(SAVE_ACTUAL_PAYMENT, ActualPaymentSaveTaskBean.class, getActualPaymentFiles(ids), null, SAVE_THREAD_SIZE,
                SAVE_MAX_ERROR_COUNT, processParameters);
    }

    @Asynchronous
    public void loadSubsidy(long userOrganizationId, long osznId, int monthFrom, int monthTo, int year) {
        try {
            List<RequestFile> list = LoadUtil.getSubsidies(userOrganizationId, osznId, monthFrom, monthTo, year);

            for (RequestFile file : list) {
                file.setUserOrganizationId(userOrganizationId);
            }

            execute(LOAD_SUBSIDY, SubsidyLoadTaskBean.class, list, null, LOAD_THREAD_SIZE, LOAD_MAX_ERROR_COUNT, null);
        } catch (StorageNotFoundException e) {
            log.error("Ошибка процесса загрузки файлов.", e);
            logBean.error(Module.NAME, ProcessManagerBean.class, RequestFile.class, null,
                    Log.EVENT.CREATE, "Ошибка процесса загрузки файлов. Причина: {0}", e.getMessage());
        }
    }

    @Asynchronous
    public void bindSubsidy(List<Long> ids, Map processParameters) {
        execute(BIND_SUBSIDY, SubsidyBindTaskBean.class, getSubsidyFiles(ids), null, BIND_THREAD_SIZE,
                BIND_MAX_ERROR_COUNT, processParameters);
    }

    @Asynchronous
    public void fillSubsidy(List<Long> ids, Map processParameters) {
        execute(FILL_SUBSIDY, SubsidyFillTaskBean.class, getSubsidyFiles(ids), null, FILL_THREAD_SIZE,
                FILL_MAX_ERROR_COUNT, processParameters);
    }

    @Asynchronous
    public void saveSubsidy(List<Long> ids, Map processParameters) {
        execute(SAVE_SUBSIDY, SubsidySaveTaskBean.class, getSubsidyFiles(ids), null, SAVE_THREAD_SIZE,
                SAVE_MAX_ERROR_COUNT, processParameters);
    }

    @Asynchronous
    public void exportSubsidy(List<DomainObject> balanceHolders, List<DomainObject> districts, List<DomainObject> organizations) {
        execute(EXPORT_SUBSIDY, SubsidyExportTaskBean.class, getSubsidyFiles(balanceHolders, districts, organizations),
                null, SAVE_THREAD_SIZE, SAVE_MAX_ERROR_COUNT, null);
    }

    @Asynchronous
    public void loadSubsidyTarif(long userOrganizationId, long osznId, int month, int year) {
        try {
            List<RequestFile> list = LoadUtil.getSubsidyTarifs(userOrganizationId, osznId, month, year);

            for (RequestFile file : list) {
                file.setUserOrganizationId(userOrganizationId);
            }

            execute(LOAD_SUBSIDY_TARIF, SubsidyTarifLoadTaskBean.class, list, null, LOAD_THREAD_SIZE, LOAD_MAX_ERROR_COUNT, null);
        } catch (StorageNotFoundException e) {
            log.error("Ошибка процесса загрузки файлов.", e);
            logBean.error(Module.NAME, ProcessManagerBean.class, RequestFile.class, null,
                    Log.EVENT.CREATE, "Ошибка процесса загрузки файлов. Причина: {0}", e.getMessage());
        }
    }

    @Asynchronous
    public void loadDwellingCharacteristics(long userOrganizationId, long osznId, int month, int year) {
        try {
            List<RequestFile> list = LoadUtil.getDwellingCharacteristics(userOrganizationId, osznId, month, year);

            for (RequestFile file : list) {
                file.setUserOrganizationId(userOrganizationId);
            }

            execute(LOAD_DWELLING_CHARACTERISTICS, DwellingCharacteristicsLoadTaskBean.class, list, null,
                    LOAD_THREAD_SIZE, LOAD_MAX_ERROR_COUNT, null);
        } catch (StorageNotFoundException e) {
            log.error("Ошибка процесса загрузки файлов.", e);
            logBean.error(Module.NAME, ProcessManagerBean.class, RequestFile.class, null,
                    Log.EVENT.CREATE, "Ошибка процесса загрузки файлов. Причина: {0}", e.getMessage());
        }
    }

    @Asynchronous
    public void bindDwellingCharacteristics(List<Long> ids, Map processParameters) {
        execute(BIND_DWELLING_CHARACTERISTICS, DwellingCharacteristicsBindTaskBean.class, getDwellingCharacteristicsFiles(ids),
                null, BIND_THREAD_SIZE, BIND_MAX_ERROR_COUNT, processParameters);
    }

    @Asynchronous
    public void saveDwellingCharacteristics(List<Long> ids, Map processParameters) {
        execute(SAVE_DWELLING_CHARACTERISTICS, DwellingCharacteristicsSaveTaskBean.class,
                getDwellingCharacteristicsFiles(ids), null, SAVE_THREAD_SIZE, SAVE_MAX_ERROR_COUNT, processParameters);
    }

    @Asynchronous
    public void loadFacilityServiceType(long userOrganizationId, long osznId, int month, int year) {
        try {
            List<RequestFile> list = LoadUtil.getFacilityServiceTypes(userOrganizationId, osznId, month, year);

            for (RequestFile file : list) {
                file.setUserOrganizationId(userOrganizationId);
            }

            execute(LOAD_FACILITY_SERVICE_TYPE, FacilityServiceTypeLoadTaskBean.class, list, null,
                    LOAD_THREAD_SIZE, LOAD_MAX_ERROR_COUNT, null);
        } catch (StorageNotFoundException e) {
            log.error("Ошибка процесса загрузки файлов.", e);
            logBean.error(Module.NAME, ProcessManagerBean.class, RequestFile.class, null,
                    Log.EVENT.CREATE, "Ошибка процесса загрузки файлов. Причина: {0}", e.getMessage());
        }
    }

    @Asynchronous
    public void bindFacilityServiceType(List<Long> ids, Map processParameters) {
        execute(BIND_FACILITY_SERVICE_TYPE, FacilityServiceTypeBindTaskBean.class, getFacilityServiceTypeFiles(ids),
                null, BIND_THREAD_SIZE, BIND_MAX_ERROR_COUNT, processParameters);
    }

    @Asynchronous
    public void saveFacilityServiceType(List<Long> ids, Map processParameters) {
        execute(SAVE_FACILITY_SERVICE_TYPE, FacilityServiceTypeSaveTaskBean.class,
                getFacilityServiceTypeFiles(ids), null, SAVE_THREAD_SIZE, SAVE_MAX_ERROR_COUNT, processParameters);
    }

    @Asynchronous
    public void saveFacilityForm2(List<Long> ids, Map processParameters) {
        execute(SAVE_FACILITY_FORM2, FacilityForm2SaveTaskBean.class,
                getFacilityForm2Files(ids), null, SAVE_THREAD_SIZE, SAVE_MAX_ERROR_COUNT, processParameters);
    }

    @Asynchronous
    public void loadFacilityStreetTypeReferences(long userOrganizationId, long osznId, int month, int year) {
        try {
            List<RequestFile> list = LoadUtil.getFacilityStreetTypeReferences(userOrganizationId, osznId, month, year);

            for (RequestFile file : list) {
                file.setUserOrganizationId(userOrganizationId);
            }

            execute(LOAD_FACILITY_STREET_TYPE_REFERENCE, FacilityStreetTypeLoadTaskBean.class, list, null,
                    LOAD_THREAD_SIZE, LOAD_MAX_ERROR_COUNT, null);
        } catch (StorageNotFoundException e) {
            log.error("Ошибка процесса загрузки файлов.", e);
            logBean.error(Module.NAME, ProcessManagerBean.class, RequestFile.class, null,
                    Log.EVENT.CREATE, "Ошибка процесса загрузки файлов. Причина: {0}", e.getMessage());
        }
    }

    @Asynchronous
    public void loadFacilityStreetReferences(long userOrganizationId, long osznId, int month, int year,
            Locale locale) {
        try {
            List<RequestFile> list = LoadUtil.getFacilityStreetReferences(userOrganizationId, osznId, month, year);

            for (RequestFile file : list) {
                file.setUserOrganizationId(userOrganizationId);
            }

            execute(LOAD_FACILITY_STREET_REFERENCE, FacilityStreetLoadTaskBean.class, list, null,
                    LOAD_THREAD_SIZE, LOAD_MAX_ERROR_COUNT,
                    ImmutableMap.of(FacilityStreetLoadTaskBean.LOCALE_TASK_PARAMETER_KEY, locale));
        } catch (StorageNotFoundException e) {
            log.error("Ошибка процесса загрузки файлов.", e);
            logBean.error(Module.NAME, ProcessManagerBean.class, RequestFile.class, null,
                    Log.EVENT.CREATE, "Ошибка процесса загрузки файлов. Причина: {0}", e.getMessage());
        }
    }

    @Asynchronous
    public void loadFacilityTarifReferences(long userOrganizationId, long osznId, int month, int year,
            Locale locale) {
        try {
            List<RequestFile> list = LoadUtil.getFacilityTarifReferences(userOrganizationId, osznId, month, year);

            for (RequestFile file : list) {
                file.setUserOrganizationId(userOrganizationId);
            }

            execute(LOAD_FACILITY_TARIF_REFERENCE, FacilityTarifLoadTaskBean.class, list, null,
                    LOAD_THREAD_SIZE, LOAD_MAX_ERROR_COUNT, null);
        } catch (StorageNotFoundException e) {
            log.error("Ошибка процесса загрузки файлов.", e);
            logBean.error(Module.NAME, ProcessManagerBean.class, RequestFile.class, null,
                    Log.EVENT.CREATE, "Ошибка процесса загрузки файлов. Причина: {0}", e.getMessage());
        }
    }
}