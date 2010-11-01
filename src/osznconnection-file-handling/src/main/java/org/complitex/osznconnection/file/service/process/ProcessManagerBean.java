package org.complitex.osznconnection.file.service.process;

import org.complitex.osznconnection.file.entity.Config;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestFileGroup;
import org.complitex.osznconnection.file.service.ConfigBean;
import org.complitex.osznconnection.file.service.RequestFileBean;
import org.complitex.osznconnection.file.service.executor.ExecutorBean;
import org.complitex.osznconnection.file.storage.StorageNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 11.10.2010 15:53:41
 */
@Singleton(name = "ProcessManagerBean")
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
    public class ProcessManagerBean {
    private static final Logger log = LoggerFactory.getLogger(BindTaskBean.class);

    public static enum PROCESS {
        NONE, LOAD, BIND, FILL, SAVE
    }

    @EJB(beanName = "LoadTaskBean")
    private LoadTaskBean loadTaskBean;

    @EJB(beanName = "BindTaskBean")
    private BindTaskBean bindTaskBean;

    @EJB(beanName = "FillTaskBean")
    private FillTaskBean fillTaskBean;

    @EJB(beanName = "SaveTaskBean")
    private SaveTaskBean saveTaskBean;

    @EJB(beanName = "ExecutorBean")
    private ExecutorBean executorBean;

    @EJB(beanName = "ConfigBean")
    private ConfigBean configBean;

    @EJB(beanName = "RequestFileBean")
    private RequestFileBean requestFileBean;

    private List<RequestFile> linkError = new CopyOnWriteArrayList<RequestFile>();

    Map<Object, Integer> processedIndex = new ConcurrentHashMap<Object, Integer>();

    private PROCESS process = PROCESS.NONE;

    private boolean pretreatment = false;

    @PostConstruct
    public void init(){
        requestFileBean.cancelLoading();
        requestFileBean.cancelSaving();
    }

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

    public List<RequestFileGroup> getProcessed(Object queryKey){
        List<RequestFileGroup> list = new ArrayList<RequestFileGroup>();

        Integer index = processedIndex.get(queryKey);

        int size = executorBean.getProcessed().size();

        list.addAll(executorBean.getProcessed().subList(index != null ? index : 0, size));

        processedIndex.put(queryKey, size);

        return Collections.unmodifiableList(list);
    }

    public int getCount(RequestFile.STATUS status){
        int count = 0;

//        for (RequestFileGroup group : executorBean.getProcessed()){
//            if (group.getStatus().equals(status)){
//                count++;
//            }
//        }

        return count;
    }

//    public int getProcessedCount() {
//        return executorBean.getProcessedCount();
//    }

    public int getErrorCount() {
        return executorBean.getErrorCount();
    }

    public boolean isProcessing(){
        return executorBean.getStatus().equals(ExecutorBean.STATUS.RUNNING) || pretreatment;
    }

    public boolean isCriticalError(){
        return executorBean.getStatus().equals(ExecutorBean.STATUS.CRITICAL_ERROR);
    }

    public boolean isCompleted(){
        return executorBean.getStatus().equals(ExecutorBean.STATUS.COMPLETED);
    }

    @Asynchronous
    public void load(Long organizationId, String districtCode, int monthFrom, int monthTo, int year){
        process = PROCESS.LOAD;
        processedIndex.clear();

        try {
            pretreatment = true; // предобработка

            LoadUtil.LoadParameter loadParameter = LoadUtil.getLoadParameter(organizationId, districtCode, monthFrom, monthTo, year);

            linkError.addAll(loadParameter.getLinkError());

            pretreatment = false;

//            executorBean.execute(loadParameter.getRequestFileGroups(),
//                    loadTaskBean,
//                    configBean.getInteger(Config.LOAD_THREADS_SIZE, true),
//                    configBean.getInteger(Config.LOAD_MAX_ERROR_COUNT, true));
        } catch (StorageNotFoundException e) {
            log.error("Директория файлов для загрузки не найдена", e);
        }
    }

    @Asynchronous
    public void bind(List<RequestFileGroup> groups){
        process = PROCESS.BIND;
        processedIndex.clear();

//        executorBean.execute(groups,
//                bindTaskBean,
//                configBean.getInteger(Config.BIND_THREADS_SIZE, true),
//                configBean.getInteger(Config.BIND_MAX_ERROR_COUNT, true));
    }

    @Asynchronous
    public void fill(List<RequestFileGroup> groups){
        process = PROCESS.FILL;
        processedIndex.clear();

//        executorBean.execute(groups,
//                fillTaskBean,
//                configBean.getInteger(Config.FILL_THREADS_SIZE, true),
//                configBean.getInteger(Config.FILL_MAX_ERROR_COUNT, true));
    }

    @Asynchronous
    public void save(List<RequestFileGroup> groups){
        process = PROCESS.SAVE;
        processedIndex.clear();

//        executorBean.execute(groups,
//                saveTaskBean,
//                configBean.getInteger(Config.SAVE_THREADS_SIZE, true),
//                configBean.getInteger(Config.SAVE_MAX_ERROR_COUNT, true));
    }
}