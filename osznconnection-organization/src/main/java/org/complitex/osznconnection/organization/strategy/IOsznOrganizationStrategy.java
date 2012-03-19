package org.complitex.osznconnection.organization.strategy;

import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.strategy.organization.IOrganizationStrategy;

import java.util.List;
import java.util.Locale;
import org.complitex.osznconnection.organization.strategy.entity.RemoteDataSource;
import org.complitex.osznconnection.organization.strategy.entity.ServiceAssociationList;

/**
 *
 * @author Artem
 */
public interface IOsznOrganizationStrategy extends IOrganizationStrategy {

    /**
     * Attribute type ids
     */
    /**
     * Reference to jdbc data source. It is calculation center only attribute.
     */
    long DATA_SOURCE = 913;
    /**
     * References to associations between service provider types and calculation centres. It is user organization only attribute.
     */
    long SERVICE_ASSOCIATIONS = 914;
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
     * Figures out list of service associations. Each service association is link between service provider type and 
     * caluclation center.
     * 
     * @param userOrganization User organization.
     * @return 
     */
    ServiceAssociationList getServiceAssociations(DomainObject userOrganization);

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
