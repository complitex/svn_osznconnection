package org.complitex.osznconnection.file.entity;

import java.io.Serializable;
import java.util.Set;

/**
 *
 * @author Artem
 */
public class CalculationContext implements Serializable {

    private final long calculationCenterId;
    private final String dataSource;
    private final Set<Long> serviceProviderTypeIds;
    private final long userOrganizationId;

    public CalculationContext(long calculationCenterId, String dataSource, Set<Long> serviceProviderTypeIds,
            long userOrganizationId) {
        this.calculationCenterId = calculationCenterId;
        this.dataSource = dataSource;
        this.serviceProviderTypeIds = serviceProviderTypeIds;
        this.userOrganizationId = userOrganizationId;
    }

    public String getDataSource() {
        return dataSource;
    }

    public long getCalculationCenterId() {
        return calculationCenterId;
    }

    public Set<Long> getServiceProviderTypeIds() {
        return serviceProviderTypeIds;
    }

    public long getUserOrganizationId() {
        return userOrganizationId;
    }

    @Override
    public String toString() {
        return "{calculation center id: " + getCalculationCenterId() + ", data source: " + getDataSource() + " }";
    }
}
