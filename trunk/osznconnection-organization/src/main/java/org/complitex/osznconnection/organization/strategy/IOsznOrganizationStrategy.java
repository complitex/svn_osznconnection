package org.complitex.osznconnection.organization.strategy;

import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.strategy.organization.IOrganizationStrategy;

import java.util.List;
import java.util.Locale;

/**
 *
 * @author Artem
 */
public interface IOsznOrganizationStrategy extends IOrganizationStrategy {

    /**
     * Attribute type ids
     */
    public long CURRENT_CALCULATION_CENTER = 904;
    /**
     * Itself organization instance.
     */
    public long ITSELF_ORGANIZATION_OBJECT_ID = 0;

    @Transactional
    List<DomainObject> getAllOuterOrganizations(Locale locale);

    @Transactional
    List<DomainObject> getAllOSZNs(Locale locale);

    @Transactional
    List<DomainObject> getAllCalculationCentres(Locale locale);

    @Transactional
    long getCurrentCalculationCenterId();

    @Transactional
    DomainObject getItselfOrganization();
}
