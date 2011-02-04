package org.complitex.osznconnection.file.service.process;

import org.complitex.dictionary.entity.Log;
import org.complitex.dictionary.service.*;
import org.complitex.dictionary.service.SessionBean;
import org.complitex.dictionary.service.executor.ExecutorBean;
import org.complitex.dictionary.service.executor.ExecutorStatus;
import org.complitex.dictionary.service.executor.IExecutorListener;
import org.complitex.dictionary.service.executor.ITaskBean;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.service.ConfigBean;
import org.complitex.osznconnection.file.service.exception.StorageNotFoundException;
import org.complitex.osznconnection.file.service.warning.ReportWarningRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.complitex.osznconnection.file.service.process.ProcessManagerBean.TYPE.*;
import static org.complitex.osznconnection.file.service.process.ProcessStatus.PROCESS.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 01.11.10 12:55
 */
@Singleton(name = "ProcessManagerBean")
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class ProcessManagerBean {
    private static final Logger log = LoggerFactory.getLogger(ProcessManagerBean.class);

    public static enum TYPE {
        GROUP, ACTUAL_PAYMENT, TARIF
    }

    @Resource
    private SessionContext sessionContext;

    @EJB(beanName = "LoadGroupTaskBean")
    private ITaskBean<RequestFileGroup> loadGroupTaskBean;

    @EJB(beanName = "LoadTarifTaskBean")
    private ITaskBean<RequestFile> loadTarifTaskBean;

    @EJB(beanName = "BindTaskBean")
    private ITaskBean<RequestFileGroup> bindTaskBean;

    @EJB(beanName = "FillTaskBean")
    private ITaskBean<RequestFileGroup> fillTaskBean;

    @EJB(beanName = "SaveTaskBean")
    private ITaskBean<RequestFileGroup> saveTaskBean;

    @EJB(beanName = "ActualPaymentBindTaskBean")
    private ITaskBean<RequestFile> actualPaymentBindTaskBean;

    @EJB(beanName = "ActualPaymentFillTaskBean")
    private ITaskBean<RequestFile> actualPaymentFillTaskBean;

    @EJB(beanName = "ActualPaymentLoadTaskBean")
    private ITaskBean<RequestFile> actualPaymentLoadTaskBean;

    @EJB(beanName = "ActualPaymentSaveTaskBean")
    private ITaskBean<RequestFile> actualPaymentSaveTaskBean;

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

    private Map<String, Map<TYPE, ProcessStatus>> processStatusMap = new ConcurrentHashMap<String, Map<TYPE, ProcessStatus>>();

    private ProcessStatus getProcessStatus(TYPE type){
        //Principal Name
        String principalName = sessionContext.getCallerPrincipal().getName();

        Map<TYPE, ProcessStatus> map = processStatusMap.get(principalName);

        ProcessStatus processStatus = null;

        if (map != null){
            processStatus = map.get(type);
        }else{
            map = new HashMap<TYPE, ProcessStatus>();
            processStatusMap.put(principalName, map);
        }

        if (processStatus == null){
            processStatus = new ProcessStatus();
            map.put(type, processStatus);
        }

        return processStatus;
    }

    private ProcessStatus initProcessStatus(TYPE type, ProcessStatus.PROCESS process){
        ProcessStatus processStatus = getProcessStatus(type);

        processStatus.setProcess(process);
        processStatus.init();

        return processStatus;
    }

    public List<RequestFile> getLinkError(TYPE type, boolean flush) {
        ProcessStatus processStatus = getProcessStatus(type);

        if (processStatus != null){
            return processStatus.getLinkError(flush);
        }

        return Collections.emptyList();
    }

    public <T> List<T> getProcessed(TYPE type, Object queryKey){
        return getProcessStatus(type).getProcessed(queryKey);
    }

    public int getSuccessCount(TYPE type) {
        return getProcessStatus(type).getSuccessCount();
    }

    public int getSkippedCount(TYPE type){
        return getProcessStatus(type).getSkippedCount();
    }

    public int getErrorCount(TYPE type) {
        return getProcessStatus(type).getErrorCount();
    }

    public boolean isProcessing(TYPE type){
        return getProcessStatus(type).isProcessing();
    }

    public boolean isCriticalError(TYPE type){
        return getProcessStatus(type).isCriticalError();
    }

    public boolean isCompleted(TYPE type){
        return getProcessStatus(type).isCompleted();
    }

    public boolean isCanceled(TYPE type){
        return getProcessStatus(type).isCanceled();
    }

    public boolean isStop(TYPE type){
        return getProcessStatus(type).isStop();
    }

    public void cancel(TYPE type){
        getProcessStatus(type).cancel();
    }

    public ProcessStatus.PROCESS getProcess(TYPE type){
        return getProcessStatus(type).getProcess();
    }

    @Asynchronous
    public void loadGroup(Long organizationId, String districtCode, int monthFrom, int monthTo, int year){
        ProcessStatus processStatus = getProcessStatus(GROUP);

        try {
            processStatus.setProcess(LOAD_GROUP);

            processStatus.startPreprocess(); // предобработка
            processStatus.init();

            //создание ключа для текущего пользователя
            if (sessionBean.createPermissionId(RequestFileGroup.TABLE) == null){
                processStatus.preprocessError();

                log.error("Ошибка процесса загрузки файлов. Причина: ошибка получения ключа безопасности");
                logBean.error(Module.NAME, ProcessManagerBean.class, RequestFileGroup.class, null, Log.EVENT.CREATE,
                        "Ошибка процесса загрузки файлов. Ошибка получения ключа безопасности. " +
                                "Возможно пользователю не добавлено ни одной организации.");

                return;
            }

            LoadUtil.LoadGroupParameter loadParameter = LoadUtil.getLoadParameter(organizationId, districtCode, monthFrom, monthTo, year);

            List<RequestFile> linkError = loadParameter.getLinkError();

            processStatus.addLinkError(linkError);

            for (RequestFile rf : linkError){
                logBean.error(Module.NAME, ProcessManagerBean.class, RequestFileGroup.class, null, rf.getId(),
                        Log.EVENT.CREATE, rf.getLogChangeList(), "Связанный файл не найден для объекта {0}",
                        rf.getLogObjectName());
            }

            processStatus.donePreprocess();

            executorBean.execute(loadParameter.getRequestFileGroups(),
                    loadGroupTaskBean,
                    processStatus.getExecutorStatus(),
                    configBean.getInteger(Config.LOAD_THREAD_SIZE, true),
                    configBean.getInteger(Config.LOAD_MAX_ERROR_COUNT, true));
        } catch (StorageNotFoundException e) {
            processStatus.preprocessError();

            log.error("Ошибка процесса загрузки файлов.", e);
            logBean.error(Module.NAME, ProcessManagerBean.class, RequestFileGroup.class, null,
                    Log.EVENT.CREATE, "Ошибка процесса загрузки файлов. Причина: {0}", e.getMessage());
        }
    }

    @Asynchronous
    public void bindGroup(List<RequestFileGroup> groups){
        executorBean.execute(groups,
                bindTaskBean,
                initProcessStatus(GROUP, BIND_GROUP).getExecutorStatus(),
                configBean.getInteger(Config.BIND_THREAD_SIZE, true),
                configBean.getInteger(Config.BIND_MAX_ERROR_COUNT, true));
    }

    @Asynchronous
    public void fillGroup(List<RequestFileGroup> groups){
        executorBean.execute(groups,
                fillTaskBean,
                initProcessStatus(GROUP, FILL_GROUP).getExecutorStatus(),
                configBean.getInteger(Config.FILL_THREAD_SIZE, true),
                configBean.getInteger(Config.FILL_MAX_ERROR_COUNT, true));
    }

    @Asynchronous
    public void saveGroup(List<RequestFileGroup> groups){
        executorBean.execute(groups,
                saveTaskBean,
                initProcessStatus(GROUP, SAVE_GROUP).getExecutorStatus(),
                new IExecutorListener<RequestFileGroup>() {
                    @Override
                    public void onComplete(List<RequestFileGroup> processed) {
                        try {
                            SaveUtil.createResult(processed, reportWarningRenderer);
                        } catch (StorageNotFoundException e) {
                            log.error("Ошибка создания файла Result.txt.", e);
                            logBean.error(Module.NAME, ProcessManagerBean.class, RequestFileGroup.class, null,
                                    Log.EVENT.CREATE, "Ошибка создания файла Result.txt. Причина: {0}", e.getMessage());
                        }
                    }
                },
                configBean.getInteger(Config.SAVE_THREAD_SIZE, true),
                configBean.getInteger(Config.SAVE_MAX_ERROR_COUNT, true));
    }

    @Asynchronous
    public void loadActualPayment(Long organizationId, String districtCode, int monthFrom, int monthTo, int year){
        try {
            ProcessStatus processStatus = initProcessStatus(ACTUAL_PAYMENT, LOAD_ACTUAL_PAYMENT);

            //создание ключа для текущего пользователя
            if (sessionBean.createPermissionId(RequestFile.TABLE) == null){
                processStatus.preprocessError();

                log.error("Ошибка процесса загрузки файлов. Причина: ошибка получения ключа безопасности");
                logBean.error(Module.NAME, ProcessManagerBean.class, ActualPayment.class, null, Log.EVENT.CREATE,
                       "Ошибка процесса загрузки файлов. Ошибка получения ключа безопасности. " +
                                "Возможно пользователю не добавлено ни одной организации.");

                return;
            }

            executorBean.execute(LoadUtil.getActualPayments(organizationId, districtCode, monthFrom, monthTo, year),
                    actualPaymentLoadTaskBean,
                    processStatus.getExecutorStatus(),
                    configBean.getInteger(Config.LOAD_THREAD_SIZE, true),
                    configBean.getInteger(Config.LOAD_MAX_ERROR_COUNT, true));
        } catch (StorageNotFoundException e) {
            log.error("Ошибка процесса загрузки файлов.", e);
            logBean.error(Module.NAME, ProcessManagerBean.class, ActualPayment.class, null,
                    Log.EVENT.CREATE, "Ошибка процесса загрузки файлов. Причина: {0}", e.getMessage());
        }
    }

    @Asynchronous
    public void bindActualPayment(List<RequestFile> actualPayments){
        executorBean.execute(actualPayments,
                actualPaymentBindTaskBean,
                initProcessStatus(ACTUAL_PAYMENT, BIND_ACTUAL_PAYMENT).getExecutorStatus(),
                configBean.getInteger(Config.BIND_THREAD_SIZE, true),
                configBean.getInteger(Config.BIND_MAX_ERROR_COUNT, true));
    }

    @Asynchronous
    public void fillActualPayment(List<RequestFile> actualPayments){
        executorBean.execute(actualPayments,
                actualPaymentFillTaskBean,
                initProcessStatus(ACTUAL_PAYMENT, FILL_ACTUAL_PAYMENT).getExecutorStatus(),
                configBean.getInteger(Config.FILL_THREAD_SIZE, true),
                configBean.getInteger(Config.FILL_MAX_ERROR_COUNT, true));
    }

    @Asynchronous
    public void saveActualPayment(List<RequestFile> actualPayments){
        executorBean.execute(actualPayments,
                actualPaymentSaveTaskBean,
                initProcessStatus(ACTUAL_PAYMENT, SAVE_ACTUAL_PAYMENT).getExecutorStatus(),
                configBean.getInteger(Config.SAVE_THREAD_SIZE, true),
                configBean.getInteger(Config.SAVE_MAX_ERROR_COUNT, true));
    }

    @Asynchronous
    public void loadTarif(Long organizationId, String districtCode, int monthFrom, int monthTo, int year){
        try {
            ProcessStatus processStatus = initProcessStatus(TARIF, LOAD_TARIF);

            //создание ключа для текущего пользователя
            if (sessionBean.createPermissionId(RequestFile.TABLE) == null){
                processStatus.preprocessError();

                log.error("Ошибка процесса загрузки файлов. Причина: ошибка получения ключа безопасности");
                logBean.error(Module.NAME, ProcessManagerBean.class, Tarif.class, null, Log.EVENT.CREATE,
                       "Ошибка процесса загрузки файлов. Ошибка получения ключа безопасности. " +
                                "Возможно пользователю не добавлено ни одной организации.");

                return;
            }

            executorBean.execute(LoadUtil.getTarifs(organizationId, districtCode, monthFrom, monthTo, year),
                    loadTarifTaskBean,
                    processStatus.getExecutorStatus(),
                    configBean.getInteger(Config.LOAD_THREAD_SIZE, true),
                    configBean.getInteger(Config.LOAD_MAX_ERROR_COUNT, true));
        } catch (StorageNotFoundException e) {
            log.error("Ошибка процесса загрузки файлов.", e);
            logBean.error(Module.NAME, ProcessManagerBean.class, Tarif.class, null,
                    Log.EVENT.CREATE, "Ошибка процесса загрузки файлов. Причина: {0}", e.getMessage());
        }
    }
}