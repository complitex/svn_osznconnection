package org.complitex.osznconnection.organization.strategy.web.edit;

import java.util.Locale;

import org.complitex.dictionary.entity.DomainObject;
import org.complitex.organization.strategy.web.edit.OrganizationEditComponent;
import org.complitex.organization.strategy.web.edit.OrganizationValidator;
import org.complitex.osznconnection.organization.strategy.IOsznOrganizationStrategy;

/**
 *
 * @author Artem
 */
public class OsznOrganizationValidator extends OrganizationValidator {

    public OsznOrganizationValidator(Locale systemLocale) {
        super(systemLocale);
    }

    protected boolean checkDistrict(DomainObject object, OrganizationEditComponent editComponent) {
        Long entityTypeId = object.getEntityTypeId();
        if (entityTypeId != null && entityTypeId.equals(IOsznOrganizationStrategy.OSZN)) {
            boolean validated = editComponent.isDistrictEntered();
            if (!validated) {
                editComponent.error(editComponent.getString("must_have_district"));
            }
            return validated;
        } else {
            return true;
        }
    }
}
