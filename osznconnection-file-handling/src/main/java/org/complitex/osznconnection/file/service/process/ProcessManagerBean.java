package org.complitex.osznconnection.file.service.process;

import org.complitex.dictionary.entity.IExecutorObject;
import org.complitex.dictionary.entity.Log;
import org.complitex.dictionary.service.ConfigBean;
import org.complitex.dictionary.service.LogBean;
import org.complitex.dictionary.service.SessionBean;
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
import static org.complitex.osznconnection.file.service.process.ProcessType.FILL_ACTUAL_PAYMENT;
import static org.complitex.osznconnection.file.service.process.ProcessType.SAVE_ACTUAL_PAYMENT;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 01.11.10 12:55
 */
@Singleton(name = "ProcessManagerBean")
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class ProcessManagerBean {
    private static final Logger log = LoggerFactory.getLogger(ProcessManagerBean.class);

    @Resource
    private SessionContext sessionContext;

    @EJB(beanName = "ExecutorBean")
    private ExecutorBean executorBean;

    @EJB(beanName = "ConfigBean")
    private ConfigBean configBean;

    @EJB(beanName = "LogBean")
    private LogBean logBean;

    @EJB
    private ReportWarningRenderer reportWarningRenderer;

    @EJB
    private SessionBean sessionBean;

    @EJB
    private RequestFileGroupBean requestFileGroupBean;

    @EJB
    private RequestFileBean requestFileBean;

    private Map<String, Map<ProcessType, Process>> processStatusMap = new ConcurrentHashMap<String, Map<ProcessType, Process>>();

    private Process getProcess(ProcessType processType){
        //Principal Name
        String principalName = sessionContext.getCallerPrincipal().getName();

        Map<ProcessType, Process> map = processStatusMap.get(principalName);

        Process process = null;

        if (map != null){
            process = map.get(processType);
        }else{
            map = new HashMap<ProcessType, Process>();
            processStatusMap.put(principalName, map);
        }

        if (process == null){
            process = new Process();
            map.put(processType, process);
        }

        return process;
    }

    private List<Process> getAllUsersProcess(ProcessType processType){
        List<Process> processes = new ArrayList<Process>();

        for ( Map<ProcessType, Process> map : processStatusMap.values()){
            Process p = map.get(processType);

            if (p != null){
                processes.add(p);
            }
        }

        return processes;
    }

    public List<RequestFile> getLinkError(ProcessType processType, boolean flush) {
        Process process = getProcess(processType);

        if (process != null){
            return process.getLinkError(flush);
        }

        return Collections.emptyList();
    }

    public <T> List<T> getProcessed(ProcessType processType, Object queryKey){
        return getProcess(processType).getProcessed(queryKey);
    }

    public int getSuccessCount(ProcessType processType) {
        return getProcess(processType).getSuccessCount();
    }

    public int getSkippedCount(ProcessType processType){
        return getProcess(processType).getSkippedCount();
    }

    public int getErrorCount(ProcessType processType) {
        return getProcess(processType).getErrorCount();
    }

    public boolean isProcessing(ProcessType processType){
        return getProcess(processType).isProcessing();
    }

    public boolean isCriticalError(ProcessType processType){
        return getProcess(processType).isCriticalError();
    }

    public boolean isCompleted(ProcessType processType){
        return getProcess(processType).isCompleted();
    }

    public boolean isCanceled(ProcessType processType){
        return getProcess(processType).isCanceled();
    }

    public void cancel(ProcessType processType){
        getProcess(processType).cancel();
    }

    public boolean isWaiting(ProcessType processType, IExecutorObject executorObject){
        for (Process process : getAllUsersProcess(processType)){
            if (process.isRunning() && process.isWaiting(executorObject)){
                return true;
            }
        }

        return false;
    }

    private void execute(ProcessType processType, Class<? extends ITaskBean> taskClass,
                         List<? extends IExecutorObject> list, IExecutorListener listener,
                         FileHandlingConfig threadCount, FileHandlingConfig maxErrorCount){
        Process process = getProcess(processType);

        process.getQueue().addAll(list);

        if (!process.isRunning()){
            process.init();

            process.setMaxThread(configBean.getInteger(threadCount, true));
            process.setMaxErrors(configBean.getInteger(maxErrorCount, true));
            process.setTask(EjbBeanLocator.getBean(taskClass));
            process.setListener(listener);

            process.getQueue().addAll(list);

            executorBean.execute(process);
        }
    }

    @Asynchronous
    public void loadGroup(Long organizationId, String districtCode, int monthFrom, int monthTo, int year){
        Process process = getProcess(LOAD_GROUP);

        try {
            //поиск групп файлов запросов
            if (!process.isRunning()) {
                process.setPreprocess(true);
                process.init();
            }

            LoadUtil.LoadGroupParameter loadParameter = LoadUtil.getLoadParameter(organizationId, districtCode, monthFrom, monthTo, year);

            List<RequestFile> linkError = loadParameter.getLinkError();

            process.addLinkError(linkError);

            for (RequestFile rf : linkError){
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
                process.setTask(EjbBeanLocator.getBean(LoadGroupTaskBean.class));

                executorBean.execute(process);
            }
        } catch (StorageNotFoundException e) {
            process.preprocessError();

            log.error("Ошибка процесса загрузки файлов.", e);
            logBean.error(Module.NAME, ProcessManagerBean.class, RequestFileGroup.class, null,
                    Log.EVENT.CREATE, "Ошибка процесса загрузки файлов. Причина: {0}", e.getMessage());
        }
    }

    private List<RequestFileGroup> getGroups(List<Long> ids){
        List<RequestFileGroup> groups = new ArrayList<RequestFileGroup>();

        for (Long id : ids){
            RequestFileGroup group = requestFileGroupBean.getRequestFileGroup(id);

            if (group != null && !group.isProcessing() && !isWaiting(BIND_GROUP, group) && !isWaiting(FILL_GROUP, group)
                    && !isWaiting(SAVE_GROUP, group)){
                groups.add(group);
            }
        }

        return groups;
    }

    private List<RequestFile> getActualPaymentFiles(List<Long> ids){
        List<RequestFile> requestFiles = new ArrayList<RequestFile>();

        for (Long id : ids){
            RequestFile requestFile = requestFileBean.findById(id);

            if (requestFile != null && !requestFile.isProcessing() && !isWaiting(BIND_ACTUAL_PAYMENT, requestFile)
                    && !isWaiting(FILL_ACTUAL_PAYMENT, requestFile) && !isWaiting(SAVE_ACTUAL_PAYMENT, requestFile)){
                requestFiles.add(requestFile);
            }
        }

        return requestFiles;
    }

    @Asynchronous
    public void bindGroup(List<Long> ids){
        execute(BIND_GROUP, BindTaskBean.class, getGroups(ids), null, BIND_THREAD_SIZE, BIND_MAX_ERROR_COUNT);
    }

    @Asynchronous
    public void fillGroup(List<Long> ids){
        execute(FILL_GROUP, FillTaskBean.class, getGroups(ids), null, FILL_THREAD_SIZE, FILL_MAX_ERROR_COUNT);
    }

    @Asynchronous
    public void saveGroup(List<Long> ids){
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

        execute(SAVE_GROUP, SaveTaskBean.class, getGroups(ids), listener, SAVE_THREAD_SIZE, SAVE_MAX_ERROR_COUNT);
    }

    @Asynchronous
    public void loadActualPayment(Long organizationId, String districtCode, int monthFrom, int monthTo, int year){
        try {
            List<RequestFile> list = LoadUtil.getActualPayments(organizationId, districtCode, monthFrom, monthTo, year);

            execute(LOAD_ACTUAL_PAYMENT, ActualPaymentLoadTaskBean.class, list, null, LOAD_THREAD_SIZE, LOAD_MAX_ERROR_COUNT);
        } catch (StorageNotFoundException e) {
            log.error("Ошибка процесса загрузки файлов.", e);
            logBean.error(Module.NAME, ProcessManagerBean.class, RequestFile.class, null,
                    Log.EVENT.CREATE, "Ошибка процесса загрузки файлов. Причина: {0}", e.getMessage());
        }
    }

    @Asynchronous
    public void bindActualPayment(List<Long> ids){
        execute(BIND_ACTUAL_PAYMENT, ActualPaymentBindTaskBean.class, getActualPaymentFiles(ids), null, BIND_THREAD_SIZE,
                BIND_MAX_ERROR_COUNT);
    }

    @Asynchronous
    public void fillActualPayment(List<Long> ids){
        execute(FILL_ACTUAL_PAYMENT, ActualPaymentFillTaskBean.class, getActualPaymentFiles(ids), null, FILL_THREAD_SIZE,
                FILL_MAX_ERROR_COUNT);
    }

    @Asynchronous
    public void saveActualPayment(List<Long> ids){
        execute(SAVE_ACTUAL_PAYMENT, ActualPaymentSaveTaskBean.class, getActualPaymentFiles(ids), null, SAVE_THREAD_SIZE,
                SAVE_MAX_ERROR_COUNT);
    }

    @Asynchronous
    public void loadTarif(Long organizationId, String districtCode, int monthFrom, int monthTo, int year){
        try {
            List<RequestFile> list = LoadUtil.getTarifs(organizationId, districtCode, monthFrom, monthTo, year);

            execute(LOAD_TARIF, LoadTarifTaskBean.class, list, null, LOAD_THREAD_SIZE, LOAD_MAX_ERROR_COUNT);
        } catch (StorageNotFoundException e) {
            log.error("Ошибка процесса загрузки файлов.", e);
            logBean.error(Module.NAME, ProcessManagerBean.class, RequestFile.class, null,
                    Log.EVENT.CREATE, "Ошибка процесса загрузки файлов. Причина: {0}", e.getMessage());
        }
    }
}