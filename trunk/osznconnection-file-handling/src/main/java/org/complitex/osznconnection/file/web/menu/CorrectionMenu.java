package org.complitex.osznconnection.file.web.menu;

import com.google.common.collect.Lists;
import org.apache.wicket.Page;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.complitex.correction.web.*;
import org.complitex.osznconnection.file.web.pages.account.PersonAccountList;
import org.complitex.osznconnection.file.web.pages.ownership.OwnershipCorrectionList;
import org.complitex.osznconnection.file.web.pages.privilege.PrivilegeCorrectionList;
import org.complitex.template.web.template.ITemplateLink;
import org.complitex.template.web.template.ResourceTemplateMenu;

import java.util.List;
import java.util.Locale;

/**
 *
 * @author Artem
 */
public class CorrectionMenu extends ResourceTemplateMenu {

    @Override
    public String getTitle(Locale locale) {
        return getString(CorrectionMenu.class, locale, "title");
    }

    @Override
    public List<ITemplateLink> getTemplateLinks(Locale locale) {
        List<ITemplateLink> links = Lists.newArrayList();

        links.add(new ITemplateLink() {

            @Override
            public String getLabel(Locale locale) {
                return getString(CorrectionMenu.class, locale, "organization");
            }

            @Override
            public Class<? extends Page> getPage() {
                return OrganizationCorrectionList.class;
            }

            @Override
            public PageParameters getParameters() {
                return new PageParameters();
            }

            @Override
            public String getTagId() {
                return "organization_correction_item";
            }
        });

        links.add(new ITemplateLink() {

            @Override
            public String getLabel(Locale locale) {
                return getString(CorrectionMenu.class, locale, "city_correction");
            }

            @Override
            public Class<? extends Page> getPage() {
                return CityCorrectionList.class;
            }

            @Override
            public PageParameters getParameters() {
                return new PageParameters();
            }

            @Override
            public String getTagId() {
                return "city_correction_item";
            }
        });
        links.add(new ITemplateLink() {

            @Override
            public String getLabel(Locale locale) {
                return getString(CorrectionMenu.class, locale, "district_correction");
            }

            @Override
            public Class<? extends Page> getPage() {
                return DistrictCorrectionList.class;
            }

            @Override
            public PageParameters getParameters() {
                return new PageParameters();
            }

            @Override
            public String getTagId() {
                return "district_correction_item";
            }
        });
        links.add(new ITemplateLink() {

            @Override
            public String getLabel(Locale locale) {
                return getString(CorrectionMenu.class, locale, "street_correction");
            }

            @Override
            public Class<? extends Page> getPage() {
                return StreetCorrectionList.class;
            }

            @Override
            public PageParameters getParameters() {
                return new PageParameters();
            }

            @Override
            public String getTagId() {
                return "street_correction_item";
            }
        });
        links.add(new ITemplateLink() {

            @Override
            public String getLabel(Locale locale) {
                return getString(CorrectionMenu.class, locale, "street_type_correction");
            }

            @Override
            public Class<? extends Page> getPage() {
                return StreetTypeCorrectionList.class;
            }

            @Override
            public PageParameters getParameters() {
                return new PageParameters();
            }

            @Override
            public String getTagId() {
                return "street_type_correction_item";
            }
        });
        links.add(new ITemplateLink() {

            @Override
            public String getLabel(Locale locale) {
                return getString(CorrectionMenu.class, locale, "building_correction");
            }

            @Override
            public Class<? extends Page> getPage() {
                return BuildingCorrectionList.class;
            }

            @Override
            public PageParameters getParameters() {
                return new PageParameters();
            }

            @Override
            public String getTagId() {
                return "building_correction_item";
            }
        });
        links.add(new ITemplateLink() {

            @Override
            public String getLabel(Locale locale) {
                return getString(CorrectionMenu.class, locale, "ownership_correction");
            }

            @Override
            public Class<? extends Page> getPage() {
                return OwnershipCorrectionList.class;
            }

            @Override
            public PageParameters getParameters() {
                return new PageParameters();
            }

            @Override
            public String getTagId() {
                return "ownership_correction_item";
            }
        });
        links.add(new ITemplateLink() {

            @Override
            public String getLabel(Locale locale) {
                return getString(CorrectionMenu.class, locale, "person_account");
            }

            @Override
            public Class<? extends Page> getPage() {
                return PersonAccountList.class;
            }

            @Override
            public PageParameters getParameters() {
                return new PageParameters();
            }

            @Override
            public String getTagId() {
                return "person_account_item";
            }
        });
        links.add(new ITemplateLink() {

            @Override
            public String getLabel(Locale locale) {
                return getString(CorrectionMenu.class, locale, "privilege");
            }

            @Override
            public Class<? extends Page> getPage() {
                return PrivilegeCorrectionList.class;
            }

            @Override
            public PageParameters getParameters() {
                return new PageParameters();
            }

            @Override
            public String getTagId() {
                return "privilege_correction_item";
            }
        });
        return links;
    }

    @Override
    public String getTagId() {
        return "correction_menu";
    }
}
