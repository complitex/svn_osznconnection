package org.complitex.osznconnection.file.service.process;

import org.complitex.dictionary.service.executor.ExecutorStatus;
import org.complitex.osznconnection.file.entity.RequestFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 24.01.11 16:07
 */
public class ProcessStatus {
    public static enum PROCESS {
        LOAD_GROUP, BIND_GROUP, FILL_GROUP, SAVE_GROUP,
        LOAD_ACTUAL_PAYMENT, BIND_ACTUAL_PAYMENT, FILL_ACTUAL_PAYMENT, SAVE_ACTUAL_PAYMENT,
        LOAD_TARIF
    }

    private List<RequestFile> linkError = new CopyOnWriteArrayList<RequestFile>();

    Map<Object, Integer> processedIndex = new ConcurrentHashMap<Object, Integer>();

    private PROCESS process;

    private boolean preprocess = false;
    private int preprocessErrorCount = 0;
    private boolean preprocessError = false;

    private ExecutorStatus executorStatus = new ExecutorStatus();

    public PROCESS getProcess() {
        return process;
    }

    public void setProcess(PROCESS process) {
        this.process = process;
    }

    public void startPreprocess() {
        preprocess = true;
    }

    public void donePreprocess() {
        preprocess = false;
    }

    public void preprocessError(){
        preprocessError = true;
        donePreprocess();
    }

    public void addLinkError(List<RequestFile> list){
        linkError.addAll(list);
        preprocessErrorCount = linkError.size();
    }

    public ExecutorStatus getExecutorStatus() {
        return executorStatus;
    }

    public List<RequestFile> getLinkError(boolean flush) {
        List<RequestFile> list = new ArrayList<RequestFile>();
        list.addAll(linkError);

        if (flush){
            linkError.clear();
        }

        return Collections.unmodifiableList(list);
    }

     @SuppressWarnings({"unchecked"})
     public <T> List<T> getProcessed(Object queryKey){
        List<T> list = new ArrayList<T>();

        Integer index = processedIndex.get(queryKey);

        int size = executorStatus.getProcessed().size();

        List processed = executorStatus.getProcessed().subList(index != null ? index : 0, size);

        for (Object obj : processed){
            list.add((T) obj);
        }

        processedIndex.put(queryKey, size);

        return Collections.unmodifiableList(list);
    }

    public int getSuccessCount() {
        return executorStatus.getSuccessCount();
    }

    public int getSkippedCount(){
        return executorStatus.getSkippedCount();
    }

    public int getErrorCount() {
        return executorStatus.getErrorCount() + preprocessErrorCount;
    }

    public boolean isProcessing(){
        return executorStatus.isRunning() || preprocess;
    }

    public boolean isCriticalError(){
        return executorStatus.isCriticalError() || preprocessError;
    }

    public boolean isCompleted(){
        return executorStatus.isCompleted();
    }

    public boolean isCanceled(){
        return executorStatus.isCanceled();
    }

    public boolean isStop(){
        return executorStatus.isStop();
    }

    public void cancel(){
        executorStatus.cancel();
    }

    public void init(){
        preprocessError = false;
        processedIndex.clear();
        linkError.clear();
        preprocessErrorCount = 0;
    }
}
