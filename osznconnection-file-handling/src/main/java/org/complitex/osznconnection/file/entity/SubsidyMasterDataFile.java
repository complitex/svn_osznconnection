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
}
