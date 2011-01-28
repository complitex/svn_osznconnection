/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.organization.strategy;

import java.util.List;
import java.util.Locale;
import org.complitex.dictionary.entity.Attribute;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.strategy.organization.IOrganizationStrategy;

/**
 *
 * @author Artem
 */
public interface IOsznOrganizationStrategy extends IOrganizationStrategy {

    long ITSELF_ORGANIZATION_OBJECT_ID = 0;
    /**
     * Entity type ids
     */
    long OSZN = 900;
    long CALCULATION_CENTER = 901;
    long USER_ORGANIZATION = 902;
    /**
     * Attribute type ids
     */
    long NAME = 900;
    long CODE = 901;
    long DISTRICT = 902;
    long USER_ORGANIZATION_PARENT = 903;

    List<DomainObject> getAllOuterOrganizations(Locale locale);

    List<DomainObject> getAllOSZNs(Locale locale);

    List<DomainObject> getAllCalculationCentres(Locale locale);

    Attribute getDistrictAttribute(DomainObject organization);

    Attribute getParentAttribute(DomainObject organization);

    String getDistrictCode(DomainObject organization);

    DomainObject getItselfOrganization();

    String getCode(DomainObject organization);

    String getName(DomainObject organization, Locale locale);

    Long validateCode(Long id, String code, Long parentId, Long parentEntityId);

    Long validateName(Long id, String name, Long parentId, Long parentEntityId, Locale locale);
}
