/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.menu;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Locale;
import org.apache.wicket.Page;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.complitex.osznconnection.file.web.DwellingCharacteristicsFileList;
import org.complitex.osznconnection.file.web.FacilityForm2FileList;
import org.complitex.osznconnection.file.web.FacilityServiceTypeFileList;
import org.complitex.osznconnection.file.web.pages.facility.ReferenceBookList;
import org.complitex.template.web.template.ITemplateLink;
import org.complitex.template.web.template.ResourceTemplateMenu;

/**
 *
 * @author Artem
 */
public class FacilityRequestMenu extends ResourceTemplateMenu {

    @Override
    public String getTitle(Locale locale) {
        return getString(FacilityRequestMenu.class, locale, "title");
    }

    @Override
    public List<ITemplateLink> getTemplateLinks(Locale locale) {
        return ImmutableList.<ITemplateLink>of(
                new ITemplateLink() {

                    @Override
                    public String getLabel(Locale locale) {
                        return getString(FacilityRequestMenu.class, locale, "dwelling_characteristics_list");
                    }

                    @Override
                    public Class<? extends Page> getPage() {
                        return DwellingCharacteristicsFileList.class;
                    }

                    @Override
                    public PageParameters getParameters() {
                        return new PageParameters();
                    }

                    @Override
                    public String getTagId() {
                        return "DwellingCharacteristicsFileList";
                    }
                },
                new ITemplateLink() {

                    @Override
                    public String getLabel(Locale locale) {
                        return getString(FacilityRequestMenu.class, locale, "facility_service_type_list");
                    }

                    @Override
                    public Class<? extends Page> getPage() {
                        return FacilityServiceTypeFileList.class;
                    }

                    @Override
                    public PageParameters getParameters() {
                        return new PageParameters();
                    }

                    @Override
                    public String getTagId() {
                        return "FacilityServiceTypeFileList";
                    }
                },
                new ITemplateLink() {

                    @Override
                    public String getLabel(Locale locale) {
                        return getString(FacilityRequestMenu.class, locale, "facility_form2_list");
                    }

                    @Override
                    public Class<? extends Page> getPage() {
                        return FacilityForm2FileList.class;
                    }

                    @Override
                    public PageParameters getParameters() {
                        return new PageParameters();
                    }

                    @Override
                    public String getTagId() {
                        return "FacilityForm2FileList";
                    }
                },
                new ITemplateLink() {

                    @Override
                    public String getLabel(Locale locale) {
                        return getString(FacilityRequestMenu.class, locale, "reference_book_list");
                    }

                    @Override
                    public Class<? extends Page> getPage() {
                        return ReferenceBookList.class;
                    }

                    @Override
                    public PageParameters getParameters() {
                        return new PageParameters();
                    }

                    @Override
                    public String getTagId() {
                        return "ReferenceBookFileList";
                    }
                });
    }

    @Override
    public String getTagId() {
        return "facility_request_menu";
    }
}
