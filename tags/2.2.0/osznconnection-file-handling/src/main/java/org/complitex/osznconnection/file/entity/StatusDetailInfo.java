/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.entity;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Artem
 */
public class StatusDetailInfo implements Serializable {

    private Long count;
    private List<StatusDetail> statusDetails;
    private RequestStatus status;

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public List<StatusDetail> getStatusDetails() {
        return statusDetails;
    }

    public void setStatusDetails(List<StatusDetail> statusDetails) {
        this.statusDetails = statusDetails;
    }
}
