package org.complitex.osznconnection.file.service;

import org.complitex.dictionary.service.AbstractBean;
import org.complitex.osznconnection.file.entity.RequestFileType;
import org.complitex.osznconnection.file.service.warning.RequestWarningBean;

import javax.ejb.EJB;

/**
 *
 * @author Artem
 */
public abstract class AbstractRequestBean extends AbstractBean {

    @EJB
    private RequestWarningBean requestWarningBean;

    protected void clearWarnings(long requestFileId, RequestFileType requestFileType) {
        requestWarningBean.delete(requestFileId, requestFileType);
    }
}
