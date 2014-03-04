/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.entity.example;

import java.io.Serializable;
import org.complitex.osznconnection.file.entity.RequestStatus;

/**
 *
 * @author Artem
 */
public class AbstractRequestExample implements Serializable {

    private RequestStatus status;
    private Long requestFileId;

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public Long getRequestFileId() {
        return requestFileId;
    }

    public void setRequestFileId(Long requestFileId) {
        this.requestFileId = requestFileId;
    }
}
