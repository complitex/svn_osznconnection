/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.organization.strategy.web.edit;

import java.text.MessageFormat;
import java.util.Locale;
import org.apache.wicket.Component;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.strategy.web.DomainObjectEditPanel;
import org.complitex.dictionary.strategy.web.validate.IValidator;
import org.complitex.dictionary.util.EjbBeanLocator;
import org.complitex.osznconnection.organization.strategy.IOsznOrganizationStrategy;

/**
 *
 * @author Artem
 */
public class OrganizationValidator implements IValidator {

    private Locale systemLocale;

    public OrganizationValidator(Locale systemLocale) {
        this.systemLocale = systemLocale;
    }
    private OrganizationEditComponent organizationEditComponent;

    @Override
    public boolean validate(DomainObject object, DomainObjectEditPanel editPanel) {
        boolean valid = checkDistrict(object, getEditComponent(editPanel));
        if (valid) {
            valid = checkUniqueness(object, getEditComponent(editPanel));
        }
        return valid;
    }

    private OrganizationEditComponent getEditComponent(DomainObjectEditPanel editPanel) {
        if (organizationEditComponent == null) {
            editPanel.visitChildren(OrganizationEditComponent.class, new Component.IVisitor<OrganizationEditComponent>() {

                @Override
                public Object component(OrganizationEditComponent component) {
                    organizationEditComponent = component;
                    return STOP_TRAVERSAL;
                }
            });
        }
        return organizationEditComponent;
    }

    private boolean checkDistrict(DomainObject object, OrganizationEditComponent editComponent) {
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

    private boolean checkUniqueness(DomainObject object, OrganizationEditComponent editComponent) {
        IOsznOrganizationStrategy organizationStrategy = EjbBeanLocator.getBean("OrganizationStrategy");
        boolean valid = true;
        Long byName = organizationStrategy.validateName(object.getId(), organizationStrategy.getName(object, systemLocale), object.getParentId(),
                object.getParentEntityId(), systemLocale);
        if (byName != null) {
            valid = false;
            editComponent.error(MessageFormat.format(editComponent.getString("unique_name"), byName));
        }

        Long byCode = organizationStrategy.validateCode(object.getId(), organizationStrategy.getCode(object), object.getParentId(),
                object.getParentEntityId());
        if (byCode != null) {
            valid = false;
            editComponent.error(MessageFormat.format(editComponent.getString("unique_code"), byCode));
        }
        return valid;
    }
}
