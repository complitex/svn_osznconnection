package org.complitex.osznconnection.file.entity;

import java.io.Serializable;
import java.util.Map;

/**
 * 
 * @author Artem
 */
public class StatusDetail implements Serializable {

    private Long count;
    private Map<String, String> details;

    public Map<String, String> getDetails() {
        return details;
    }

    public void setDetails(Map<String, String> details) {
        this.details = details;
    }

    public String getDetail(String detailName) {
        return details.get(detailName);
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
