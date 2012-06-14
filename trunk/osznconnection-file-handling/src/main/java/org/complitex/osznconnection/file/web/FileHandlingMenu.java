package org.complitex.osznconnection.file.web;

import org.apache.wicket.Page;
import org.complitex.template.web.template.ITemplateLink;
import org.complitex.template.web.template.ResourceTemplateMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 27.08.2010 17:31:55
 */
public class FileHandlingMenu extends ResourceTemplateMenu {

    @Override
    public String getTitle(Locale locale) {
        return getString(FileHandlingMenu.class, locale, "title");
    }

    @Override
    public List<ITemplateLink> getTemplateLinks(Locale locale) {
        List<ITemplateLink> links = new ArrayList<ITemplateLink>();

        links.add(new ITemplateLink() {

            @Override
            public String getLabel(Locale locale) {
                return getString(FileHandlingMenu.class, locale, "request_file_group_list");
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
                return getString(FileHandlingMenu.class, locale, "actual_payment");
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
                return getString(FileHandlingMenu.class, locale, "subsidy");
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
                return getString(FileHandlingMenu.class, locale, "tarif_list");
            }

            @Override
            public Class<? extends Page> getPage() {
                return TarifFileList.class;
            }

            @Override
            public PageParameters getParameters() {
                return new PageParameters();
            }

            @Override
            public String getTagId() {
                return "TarifList";
            }
        });

        return links;
    }

    @Override
    public String getTagId() {
        return "file_handling_menu";
    }
}
