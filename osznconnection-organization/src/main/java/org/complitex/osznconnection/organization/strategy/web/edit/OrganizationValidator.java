/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.organization.strategy.web.edit;

import java.util.Locale;

import org.complitex.dictionary.entity.DomainObject;
import org.complitex.osznconnection.organization.strategy.IOsznOrganizationStrategy;

/**
 *
 * @author Artem
 */
public class OrganizationValidator extends org.complitex.organization.strategy.web.edit.OrganizationValidator{

    public OrganizationValidator(Locale systemLocale) {
        super(systemLocale);
    }

    protected boolean checkDistrict(DomainObject object, OsznOrganizationEditComponent editComponentOszn) {
        Long entityTypeId = object.getEntityTypeId();
        if (entityTypeId != null && entityTypeId.equals(IOsznOrganizationStrategy.OSZN)) {
            boolean validated = editComponentOszn.isDistrictEntered();
            if (!validated) {
                editComponentOszn.error(editComponentOszn.getString("must_have_district"));
            }
            return validated;
        } else {
            return true;
        }
    }
}
