package org.complitex.osznconnection.file.entity;

import org.complitex.dictionary.entity.ILongId;

import java.util.Date;

/**
 * @author Anatoly Ivanov java@inheaven.ru
 *         Date: 10.12.13 23:38
 */
public class RequestFileHistory implements ILongId {
    private Long id;
    private Long requestFileId;
    private RequestFileStatus status;
    private Date date;

    public RequestFileHistory() {
    }

    public RequestFileHistory(Long requestFileId, RequestFileStatus status, Date date) {
        this.requestFileId = requestFileId;
        this.status = status;
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRequestFileId() {
        return requestFileId;
    }

    public void setRequestFileId(Long requestFileId) {
        this.requestFileId = requestFileId;
    }

    public RequestFileStatus getStatus() {
        return status;
    }

    public void setStatus(RequestFileStatus status) {
        this.status = status;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
