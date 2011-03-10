package org.complitex.osznconnection.file.web;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.complitex.admin.web.UserList;
import org.complitex.logging.web.LogList;
import org.complitex.template.web.pages.ConfigEdit;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.ITemplateLink;
import org.complitex.template.web.template.ResourceTemplateMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.07.2010 14:01:04
 *
 *   Меню администрирование
 */
@AuthorizeInstantiation(SecurityRole.ADMIN_MODULE_EDIT)
public class AdminTemplateMenu extends org.complitex.admin.web.AdminTemplateMenu {

    @Override
    public List<ITemplateLink> getTemplateLinks(Locale locale) {
        List<ITemplateLink> links = super.getTemplateLinks(locale);

        links.add(new ITemplateLink(){
            @Override
            public String getLabel(Locale locale) {
                return getString(ImportPage.class, locale, "title");
            }

            @SuppressWarnings({"unchecked"})
            @Override
            public Class<? extends Page> getPage() {
                return ImportPage.class;
            }

            @Override
            public PageParameters getParameters() {
                return PageParameters.NULL;
            }

            @Override
            public String getTagId() {
                return "ImportPage";
            }
        });

        return links;
    }
}