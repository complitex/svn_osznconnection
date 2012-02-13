package org.complitex.osznconnection.organization.strategy;

import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.strategy.organization.IOrganizationStrategy;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.complitex.osznconnection.organization.strategy.entity.RemoteDataSource;

/**
 *
 * @author Artem
 */
public interface IOsznOrganizationStrategy extends IOrganizationStrategy {

    /**
     * Attribute type ids
     */
    /** 
     * Reference to calculation center. It is user organization only attribute.
     */
    long CALCULATION_CENTER = 911;
    /**
     * Reference to the set of service provider types. It is calculation center only attribute.
     */
    long SERVICE_PROVIDER_TYPE = 912;
    /**
     * Reference to jdbc data source. It is calculation center only attribute.
     */
    long DATA_SOURCE = 913;
    /**
     * Itself organization instance id.
     */
    long ITSELF_ORGANIZATION_OBJECT_ID = 0;

    @Transactional
    List<DomainObject> getAllOuterOrganizations(Locale locale);

    @Transactional
    List<DomainObject> getAllOSZNs(Locale locale);

    @Transactional
    List<DomainObject> getAllCalculationCentres(Locale locale);

    @Transactional
    DomainObject getItselfOrganization();

    /**
     * Figures out service provider type object's ids that associated with given calculation center organization.
     * 
     * @param calculationCenterOrganizationId Calculation center organization's id
     * @return Associated service provider type object's ids.
     */
    Set<Long> getServiceProviderTypeIds(long calculationCenterOrganizationId);

    /**
     * Figures out calculation center organization's id that associated with given user organization.
     * 
     * @param userOrganization User organization
     * @return Associated calculation center organization's id.
     */
    long getCalculationCenterId(DomainObject userOrganization);

    /**
     * Finds remote jdbc data sources.
     * @param currentDataSource Current data source.
     * @return 
     */
    List<RemoteDataSource> findRemoteDataSources(String currentDataSource);

    /**
     * Figures out data source of calculation center.
     * 
     * @param calculationCenterId Calculation center's id
     * @return Calculation center's data source
     */
    String getDataSource(long calculationCenterId);
}
