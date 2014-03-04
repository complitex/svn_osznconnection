/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service.status.details;

import java.util.Locale;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import org.complitex.osznconnection.file.entity.RequestStatus;
import org.complitex.osznconnection.file.entity.StatusDetail;

/**
 *
 * @author Artem
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class StatusDetailRenderService {

    public String displayStatusDetail(RequestStatus status, StatusDetail statusDetail,
            IStatusDetailRenderer statusDetailRenderer, Locale locale) {
        String countString = displayCount(statusDetail.getCount());
        return statusDetailRenderer.displayStatusDetail(status, statusDetail, locale) + countString;
    }

    public String displayCount(Long count) {
        return count > 1 ? " (" + count + ")" : "";
    }
}
