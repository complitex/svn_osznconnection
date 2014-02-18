package org.complitex.osznconnection.file.entity;

import org.complitex.dictionary.entity.LogChangeList;

import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 10.02.14 1:05
 */
public class SubsidyMasterDataFile extends AbstractExecutorObject {
    private Long id;

    private List<SubsidyMasterData> masterDataList;

    private String objectName;

    private RequestFileStatus status;

    private RequestFileType type;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<SubsidyMasterData> getMasterDataList() {
        return masterDataList;
    }

    public void setMasterDataList(List<SubsidyMasterData> masterDataList) {
        this.masterDataList = masterDataList;
    }

    @Override
    public String getLogObjectName() {
        return null;
    }

    @Override
    public LogChangeList getLogChangeList() {
        return null;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    @Override
    public boolean isProcessing() {
        return RequestFileStatus.EXPORTING.equals(status);
    }

    public RequestFileStatus getStatus() {
        return status;
    }

    public void setStatus(RequestFileStatus status) {
        this.status = status;
    }

    public RequestFileType getType() {
        return type;
    }

    public void setType(RequestFileType type) {
        this.type = type;
    }
}
