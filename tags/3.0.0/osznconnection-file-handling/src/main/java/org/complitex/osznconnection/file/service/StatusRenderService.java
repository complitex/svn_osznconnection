package org.complitex.osznconnection.file.service;

import org.complitex.dictionary.util.ResourceUtil;
import org.complitex.osznconnection.file.entity.RequestStatus;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import java.util.Locale;

@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class StatusRenderService {

    private static final String RESOURCE_BUNDLE = StatusRenderService.class.getName();

    public String displayStatus(RequestStatus status, Locale locale) {
        return ResourceUtil.getString(RESOURCE_BUNDLE, status.name(), locale);
    }
}
