package org.complitex.osznconnection.organization;

import org.apache.wicket.markup.html.WebPage;

import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.complitex.organization.DefaultOrganizationModule;
import org.complitex.organization.IOrganizationModule;
import org.complitex.osznconnection.organization.strategy.IOsznOrganizationStrategy;

@Singleton(name = DefaultOrganizationModule.CUSTOM_ORGANIZATION_MODULE_BEAN_NAME)
@Startup
public class OsznOrganizationModule implements IOrganizationModule {

    @EJB(name = "OsznOrganizationStrategy")
    private IOsznOrganizationStrategy osznOrganizationStrategy;
    public static final String NAME = "org.complitex.osznconnection.organization";

    @Override
    public Class<? extends WebPage> getEditPage() {
        return osznOrganizationStrategy.getEditPage();
    }

    @Override
    public PageParameters getEditPageParams() {
        return osznOrganizationStrategy.getEditPageParams(null, null, null);
    }
}
