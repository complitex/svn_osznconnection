package org.complitex.osznconnection.file.web.menu;

import org.apache.wicket.Page;
import org.complitex.template.web.template.ITemplateLink;
import org.complitex.template.web.template.ResourceTemplateMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.complitex.osznconnection.file.web.ActualPaymentFileList;
import org.complitex.osznconnection.file.web.GroupList;
import org.complitex.osznconnection.file.web.SubsidyFileList;
import org.complitex.osznconnection.file.web.SubsidyTarifFileList;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 27.08.2010 17:31:55
 */
public class SubsidyRequestMenu extends ResourceTemplateMenu {

    @Override
    public String getTitle(Locale locale) {
        return getString(SubsidyRequestMenu.class, locale, "title");
    }

    @Override
    public List<ITemplateLink> getTemplateLinks(Locale locale) {
        List<ITemplateLink> links = new ArrayList<ITemplateLink>();

        links.add(new ITemplateLink() {

            @Override
            public String getLabel(Locale locale) {
                return getString(SubsidyRequestMenu.class, locale, "request_file_group_list");
            }

            @Override
            public Class<? extends Page> getPage() {
                return GroupList.class;
            }

            @Override
            public PageParameters getParameters() {
                return new PageParameters();
            }

            @Override
            public String getTagId() {
                return "RequestFileGroupList";
            }
        });

        links.add(new ITemplateLink() {

            @Override
            public String getLabel(Locale locale) {
                return getString(SubsidyRequestMenu.class, locale, "actual_payment");
            }

            @Override
            public Class<? extends Page> getPage() {
                return ActualPaymentFileList.class;
            }

            @Override
            public PageParameters getParameters() {
                return new PageParameters();
            }

            @Override
            public String getTagId() {
                return "ActualPaymentFileList";
            }
        });

        links.add(new ITemplateLink() {

            @Override
            public String getLabel(Locale locale) {
                return getString(SubsidyRequestMenu.class, locale, "subsidy");
            }

            @Override
            public Class<? extends Page> getPage() {
                return SubsidyFileList.class;
            }

            @Override
            public PageParameters getParameters() {
                return new PageParameters();
            }

            @Override
            public String getTagId() {
                return "SubsidyFileList";
            }
        });

        links.add(new ITemplateLink() {

            @Override
            public String getLabel(Locale locale) {
                return getString(SubsidyRequestMenu.class, locale, "subsidy_tarif_list");
            }

            @Override
            public Class<? extends Page> getPage() {
                return SubsidyTarifFileList.class;
            }

            @Override
            public PageParameters getParameters() {
                return new PageParameters();
            }

            @Override
            public String getTagId() {
                return "SubsidyTarifList";
            }
        });

        return links;
    }

    @Override
    public String getTagId() {
        return "subsidy_request_menu";
    }
}
