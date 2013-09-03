package org.complitex.osznconnection.file.entity;

import java.io.Serializable;
import java.util.Set;

/**
 *
 * @author Artem
 */
public class CalculationContext implements Serializable {
    private Long osznId;
    private Long userOrganizationId;
    private Long calculationCenterId;
    private String dataSource;
    private Set<Long> serviceProviderTypeIds;


    public CalculationContext(Long osznId, Long userOrganizationId, Long calculationCenterId, String dataSource,
                              Set<Long> serviceProviderTypeIds) {
        this.osznId = osznId;
        this.userOrganizationId = userOrganizationId;
        this.calculationCenterId = calculationCenterId;
        this.dataSource = dataSource;
        this.serviceProviderTypeIds = serviceProviderTypeIds;
    }

    public Long getOsznId() {
        return osznId;
    }

    public void setOsznId(Long osznId) {
        this.osznId = osznId;
    }

    public Long getUserOrganizationId() {
        return userOrganizationId;
    }

    public void setUserOrganizationId(Long userOrganizationId) {
        this.userOrganizationId = userOrganizationId;
    }

    public Long getCalculationCenterId() {
        return calculationCenterId;
    }

    public void setCalculationCenterId(Long calculationCenterId) {
        this.calculationCenterId = calculationCenterId;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public Set<Long> getServiceProviderTypeIds() {
        return serviceProviderTypeIds;
    }

    public void setServiceProviderTypeIds(Set<Long> serviceProviderTypeIds) {
        this.serviceProviderTypeIds = serviceProviderTypeIds;
    }

    @Override
    public String toString() {
        return "{calculation center id: " + getCalculationCenterId() + ", data source: " + getDataSource() + " }";
    }
}
