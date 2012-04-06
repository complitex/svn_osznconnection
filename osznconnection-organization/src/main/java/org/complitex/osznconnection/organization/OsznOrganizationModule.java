package org.complitex.osznconnection.organization;

import java.util.Map.Entry;
import org.apache.wicket.markup.html.WebPage;

import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.apache.wicket.PageParameters;
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
    public String getEditPageParams() {
        final StringBuilder builder = new StringBuilder();
        PageParameters pageParameters = osznOrganizationStrategy.getEditPageParams(null, null, null);
        for (Entry<String, Object> entry : pageParameters.entrySet()) {
            final String key = entry.getKey();
            final Object value = entry.getValue();
            if (value instanceof String) {
                builder.append(key).append("=").append(value).append(",");
            }
        }

        //remove last symbol '&'
        if (builder.length() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.toString();
    }
}
