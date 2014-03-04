package org.complitex.osznconnection.file.web.component.process;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.strategy.organization.IOrganizationStrategy;

import javax.ejb.EJB;

/**
 *
 * @author Artem
 */
public class ItemOrganizationLabel extends Label {

    @EJB(name = "OrganizationStrategy")
    private IOrganizationStrategy organizationStrategy;

    public ItemOrganizationLabel(String id, Long organizationId) {
        super(id);

        String value = "";
        if (organizationId != null && organizationId > 0) {
            DomainObject organization = organizationStrategy.findById(organizationId, true);
            if (organization != null) {
                value = organizationStrategy.displayDomainObject(organization, getLocale());
            }
        }
        setDefaultModel(new Model<>(value));
    }
}
