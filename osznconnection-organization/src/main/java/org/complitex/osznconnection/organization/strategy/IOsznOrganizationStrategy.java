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
     * Entity type ids
     */
    public long OSZN = 900;
    public long CALCULATION_CENTER = 901;

    @Transactional
    List<DomainObject> getAllOuterOrganizations(Locale locale);

    @Transactional
    List<DomainObject> getAllOSZNs(Locale locale);

    @Transactional
    List<DomainObject> getAllCalculationCentres(Locale locale);
}
