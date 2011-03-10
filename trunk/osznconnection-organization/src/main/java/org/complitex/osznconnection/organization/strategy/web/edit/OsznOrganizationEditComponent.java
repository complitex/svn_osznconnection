package org.complitex.osznconnection.organization.strategy.web.edit;

import org.complitex.organization.strategy.web.edit.OrganizationEditComponent;
import org.complitex.osznconnection.organization.strategy.IOsznOrganizationStrategy;

/**
 * 
 * @author Artem
 */
public class OsznOrganizationEditComponent extends OrganizationEditComponent {
    public OsznOrganizationEditComponent(String id, boolean disabled) {
        super(id, disabled);
    }

    @Override
    protected boolean isDistrictVisible(Long entityTypeId) {
        return super.isDistrictVisible(entityTypeId) || entityTypeId.equals(IOsznOrganizationStrategy.OSZN);
    }

    @Override
    protected boolean isDistrictNotRequired(Long entityTypeId) {
        return super.isDistrictNotRequired(entityTypeId);
    }
}
