/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.entity;

import java.io.Serializable;
import java.util.Set;

/**
 *
 * @author Artem
 */
public class CalculationCenterInfo implements Serializable {

    private long organizationId;
    private Set<Long> serviceProviderTypeIds;

    public long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(long organizationId) {
        this.organizationId = organizationId;
    }

    public Set<Long> getServiceProviderTypeIds() {
        return serviceProviderTypeIds;
    }

    public void setServiceProviderTypeIds(Set<Long> serviceProviderTypeIds) {
        this.serviceProviderTypeIds = serviceProviderTypeIds;
    }
}
