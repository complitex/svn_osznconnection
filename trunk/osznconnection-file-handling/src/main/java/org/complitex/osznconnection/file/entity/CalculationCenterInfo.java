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
    private String dataSource;
    private Set<Long> serviceProviderTypeIds;

    public CalculationCenterInfo(long organizationId, String dataSource, Set<Long> serviceProviderTypeIds) {
        this.organizationId = organizationId;
        this.dataSource = dataSource;
        this.serviceProviderTypeIds = serviceProviderTypeIds;
    }

    public String getDataSource() {
        return dataSource;
    }

    public long getOrganizationId() {
        return organizationId;
    }

    public Set<Long> getServiceProviderTypeIds() {
        return serviceProviderTypeIds;
    }

    @Override
    public String toString() {
        return "{id: " + getOrganizationId() + ", data source: " + getDataSource() + " }";
    }
}
