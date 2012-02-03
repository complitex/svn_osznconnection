package org.complitex.osznconnection.organization.strategy.web.edit;

import java.util.Locale;

import org.complitex.dictionary.entity.DomainObject;
import org.complitex.organization.strategy.web.edit.OrganizationEditComponent;
import org.complitex.organization.strategy.web.edit.OrganizationValidator;

/**
 *
 * @author Artem
 */
public class OsznOrganizationValidator extends OrganizationValidator {

    public OsznOrganizationValidator(Locale systemLocale) {
        super(systemLocale);
    }

    @Override
    protected boolean checkDistrict(DomainObject object, OrganizationEditComponent editComponent) {
        OsznOrganizationEditComponent editComp = (OsznOrganizationEditComponent) editComponent;
        if (editComp.isOszn()) {
            boolean validated = editComponent.isDistrictEntered();
            if (!validated) {
                editComponent.error(editComponent.getString("must_have_district"));
            }
            return validated;
        } else {
            return true;
        }
    }

    @Override
    protected boolean validate(DomainObject organization, OrganizationEditComponent editComponent) {
        boolean superValid = super.validate(organization, editComponent);

        final OsznOrganizationEditComponent editComp = (OsznOrganizationEditComponent) editComponent;
        if (editComp.isCalculationCenter() && !editComp.isServiceProviderTypeEntered()) {
            superValid = false;
            editComponent.error(editComponent.getString("must_have_service_provider"));
        }
        return superValid;
    }
}
