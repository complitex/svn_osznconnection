package org.complitex.osznconnection.organization.web;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.util.EjbBeanLocator;
import org.complitex.organization.web.OrganizationMenu;
import org.complitex.osznconnection.organization.strategy.OsznOrganizationStrategy;
import org.complitex.template.web.security.SecurityRole;

/**
 *
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class OsznOrganizationTemplateMenu extends OrganizationMenu {

    @Override
    protected IStrategy getStrategy() {
        return EjbBeanLocator.getBean(OsznOrganizationStrategy.class);
    }
}
