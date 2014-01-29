package org.complitex.osznconnection.file.web.pages.subsidy;

import org.complitex.dictionary.util.DateUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
* @author Anatoly A. Ivanov java@inheaven.ru
*         Date: 30.01.14 3:22
*/
class SubsidyExportParameter implements Serializable {
    private int step = 0;
    private Integer exportType = 0;
    private Date date = DateUtil.getFirstDayOfCurrentMonth();
    private String type;
    private Long organizationId;
    private List<Long> districts = new ArrayList<>();

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public Integer getExportType() {
        return exportType;
    }

    public void setExportType(Integer exportType) {
        this.exportType = exportType;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public List<Long> getDistricts() {
        return districts;
    }

    public void setDistricts(List<Long> districts) {
        this.districts = districts;
    }
}
