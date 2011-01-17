package org.complitex.osznconnection.file.service.process;

import org.complitex.dictionary.entity.Log;
import org.complitex.dictionary.service.LogBean;
import org.complitex.dictionary.service.executor.ExecutorBean;
import org.complitex.dictionary.service.executor.IExecutorListener;
import org.complitex.dictionary.service.executor.ITaskBean;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.service.ConfigBean;
import org.complitex.osznconnection.file.service.exception.StorageNotFoundException;
import org.complitex.osznconnection.file.service.warning.IWarningRenderer;
import org.complitex.osznconnection.file.service.warning.WebWarningRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.complitex.osznconnection.file.service.warning.ReportWarningRenderer;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 01.11.10 12:55
 */
@Singleton(name = "ProcessManagerBean")
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class ProcessManagerBean {
    private static final Logger log = LoggerFactory.getLogger(ProcessManagerBean.class);

    public static enum PROCESS {
        LOAD, BIND, FILL, SAVE
    }

    @EJB(beanName = "LoadGroupTaskBean")
    private ITaskBean<RequestFileGroup> loadGroupTaskBean;

    @EJB(beanName = "LoadTarifTaskBean")
    private ITaskBean<RequestFile> loadTarifTaskBean;

    @EJB(beanName = "LoadActualPaymentTaskBean")
    private ITaskBean<RequestFile> loadActualPaymentTaskBean;

    @EJB(beanName = "BindTaskBean")
    private ITaskBean<RequestFileGroup> bindTaskBean;

    @EJB(beanName = "FillTaskBean")
    private ITaskBean<RequestFileGroup> fillTaskBean;

    @EJB(beanName = "SaveTaskBean")
    private ITaskBean<RequestFileGroup> saveTaskBean;

    @EJB(beanName = "ExecutorBean")
    private ExecutorBean executorBean;

    @EJB(beanName = "ConfigBean")
    private ConfigBean configBean;

    @EJB(beanName = "LogBean")
    private LogBean logBean;

    @EJB
    private ReportWarningRenderer reportWarningRenderer;

    private List<RequestFile> linkError = new CopyOnWriteArrayList<RequestFile>();

    Map<Object, Integer> processedIndex = new ConcurrentHashMap<Object, Integer>();

    private PROCESS process;

    private boolean preprocess = false;
    private int preprocessErrorCount = 0;
    private boolean preprocessError = false;

    public PROCESS getProcess() {
        return process;
    }

    public List<RequestFile> getLinkError(boolean flush) {
        List<RequestFile> list = new ArrayList<RequestFile>();
        list.addAll(linkError);

        if (flush){
            linkError.clear();
        }

        return Collections.unmodifiableList(list);
    }

    public List<RequestFileGroup> getProcessedGroups(Object queryKey){
        List<RequestFileGroup> list = new ArrayList<RequestFileGroup>();

        Integer index = processedIndex.get(queryKey);

        int size = executorBean.getProcessed().size();

        List processed = executorBean.getProcessed().subList(index != null ? index : 0, size);

        for (Object obj : processed){
            if (obj instanceof RequestFileGroup){
                list.add((RequestFileGroup) obj);
            }
        }

        processedIndex.put(queryKey, size);

        return Collections.unmodifiableList(list);
    }

    public List<RequestFile> getProcessedTarifFiles(Object queryKey){
        List<RequestFile> list = new ArrayList<RequestFile>();

        Integer index = processedIndex.get(queryKey);

        int size = executorBean.getProcessed().size();

        List processed = executorBean.getProcessed().subList(index != null ? index : 0, size);

        for (Object obj : processed){
            if (obj instanceof RequestFile){
                RequestFile requestFile = (RequestFile) obj;
                if (RequestFile.TYPE.TARIF.equals(requestFile.getType())) {
                    list.add(requestFile);
                }
            }
        }

        processedIndex.put(queryKey, size);

        return Collections.unmodifiableList(list);
    }

    public int getSuccessCount() {
        return executorBean.getSuccessCount();
    }

    public int getSkippedCount(){
        return executorBean.getSkippedCount();
    }

    public int getErrorCount() {
        return executorBean.getErrorCount() + preprocessErrorCount;
    }

    public boolean isProcessing(){
        return executorBean.getStatus().equals(ExecutorBean.STATUS.RUNNING) || preprocess;
    }

    public boolean isCriticalError(){
        return executorBean.getStatus().equals(ExecutorBean.STATUS.CRITICAL_ERROR) || preprocessError;
    }

    public boolean isCompleted(){
        return executorBean.getStatus().equals(ExecutorBean.STATUS.COMPLETED);
    }

    public boolean isCanceled(){
        return executorBean.getStatus().equals(ExecutorBean.STATUS.CANCELED);
    }

    public boolean isStop(){
        return executorBean.isStop();
    }

    public void cancel(){
        executorBean.cancel();
    }

    @Asynchronous
    public void loadGroup(Long organizationId, String districtCode, int monthFrom, int monthTo, int year){
        try {
            process = PROCESS.LOAD;

            preprocess = true; // предобработка

            init();

            LoadUtil.LoadGroupParameter loadParameter = LoadUtil.getLoadParameter(organizationId, districtCode, monthFrom, monthTo, year);

            linkError.addAll(loadParameter.getLinkError());

            preprocessErrorCount = linkError.size();

            for (RequestFile rf : linkError){
                logBean.error(Module.NAME, ProcessManagerBean.class, RequestFileGroup.class, null, rf.getId(),
                        Log.EVENT.CREATE, rf.getLogChangeList(), "Связанный файл не найден для объекта {0}",
                        rf.getLogObjectName());
            }

            preprocess = false;

            executorBean.execute(loadParameter.getRequestFileGroups(),
                    loadGroupTaskBean,
                    configBean.getInteger(Config.LOAD_THREAD_SIZE, true),
                    configBean.getInteger(Config.LOAD_MAX_ERROR_COUNT, true));
        } catch (StorageNotFoundException e) {
            preprocess = false;
            preprocessError = true;

            log.error("Ошибка процесса загрузки файлов.", e);
            logBean.error(Module.NAME, ProcessManagerBean.class, RequestFileGroup.class, null,
                    Log.EVENT.CREATE, "Ошибка процесса загрузки файлов. Причина: {0}", e.getMessage());
        }
    }

    @Asynchronous
    public void loadTarif(Long organizationId, String districtCode, int monthFrom, int monthTo, int year){
        try {
            process = PROCESS.LOAD;
            init();

            executorBean.execute(LoadUtil.getTarifs(organizationId, districtCode, monthFrom, monthTo, year),
                    loadTarifTaskBean,
                    configBean.getInteger(Config.LOAD_THREAD_SIZE, true),
                    configBean.getInteger(Config.LOAD_MAX_ERROR_COUNT, true));
        } catch (StorageNotFoundException e) {
            preprocess = false;
            preprocessError = true;

            log.error("Ошибка процесса загрузки файлов.", e);
            logBean.error(Module.NAME, ProcessManagerBean.class, Tarif.class, null,
                    Log.EVENT.CREATE, "Ошибка процесса загрузки файлов. Причина: {0}", e.getMessage());
        }
    }

    @Asynchronous
    public void loadActualPayment(Long organizationId, String districtCode, int monthFrom, int monthTo, int year){
        try {
            process = PROCESS.LOAD;
            init();

            executorBean.execute(LoadUtil.getActualPayments(organizationId, districtCode, monthFrom, monthTo, year),
                    loadActualPaymentTaskBean,
                    configBean.getInteger(Config.LOAD_THREAD_SIZE, true),
                    configBean.getInteger(Config.LOAD_MAX_ERROR_COUNT, true));
        } catch (StorageNotFoundException e) {
            preprocess = false;
            preprocessError = true;

            log.error("Ошибка процесса загрузки файлов.", e);
            logBean.error(Module.NAME, ProcessManagerBean.class, ActualPayment.class, null,
                    Log.EVENT.CREATE, "Ошибка процесса загрузки файлов. Причина: {0}", e.getMessage());
        }
    }

    @Asynchronous
    public void bind(List<RequestFileGroup> groups){
        process = PROCESS.BIND;
        init();

        executorBean.execute(groups,
                bindTaskBean,
                configBean.getInteger(Config.BIND_THREAD_SIZE, true),
                configBean.getInteger(Config.BIND_MAX_ERROR_COUNT, true));
    }

    @Asynchronous
    public void fill(List<RequestFileGroup> groups){
        process = PROCESS.FILL;
        init();

        executorBean.execute(groups,
                fillTaskBean,
                configBean.getInteger(Config.FILL_THREAD_SIZE, true),
                configBean.getInteger(Config.FILL_MAX_ERROR_COUNT, true));
    }

    @Asynchronous
    public void save(List<RequestFileGroup> groups){
        process = PROCESS.SAVE;
        init();

        executorBean.execute(groups,
                saveTaskBean,
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

    private void init(){
        preprocessError = false;
        processedIndex.clear();
        linkError.clear();
        preprocessErrorCount = 0;
    }
}