package org.complitex.osznconnection.organization.strategy.web.edit;

import java.text.MessageFormat;
import java.util.Locale;

import java.util.Set;
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
        boolean valid = super.validate(organization, editComponent);

        final OsznOrganizationEditComponent editComp = (OsznOrganizationEditComponent) editComponent;
        if (editComp.isUserOrganization()) {
            if (editComp.isServiceAssociationListEmpty()) {
                valid = false;
                editComponent.error(editComponent.getString("service_associations_empty"));
            } else if (editComp.isServiceAssociationListHasNulls()) {
                valid = false;
                editComponent.error(editComponent.getString("service_associations_has_nulls"));
            }

            if (valid) {
                Set<String> duplicateServiceProviderTypes = editComp.getDuplicateServiceProviderTypes();
                if (duplicateServiceProviderTypes != null && !duplicateServiceProviderTypes.isEmpty()) {
                    valid = false;

                    //form error message
                    StringBuilder sb = new StringBuilder();
                    for (String spt : duplicateServiceProviderTypes) {
                        sb.append(spt).append(", ");
                    }
                    sb.delete(sb.length() - 2, sb.length());

                    final String messageKey = duplicateServiceProviderTypes.size() == 1 ? "service_associations_has_one_duplicate"
                            : "service_associations_has_duplicates";

                    editComponent.error(MessageFormat.format(editComponent.getString(messageKey), sb.toString()));
                }

            }
        }
        return valid;
    }
}
