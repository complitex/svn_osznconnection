package org.complitex.osznconnection.organization.web;

import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.complitex.dictionary.strategy.IStrategy;
import org.complitex.dictionary.util.EjbBeanLocator;
import org.complitex.organization.web.OrganizationTemplateMenu;
import org.complitex.osznconnection.organization.strategy.OsznOrganizationStrategy;
import org.complitex.template.web.security.SecurityRole;

/**
 *
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class OsznOrganizationTemplateMenu extends OrganizationTemplateMenu {

//    protected IStrategy getStrategy() {
//        return EjbBeanLocator.getBean(OsznOrganizationStrategy.class);
//    }
}
