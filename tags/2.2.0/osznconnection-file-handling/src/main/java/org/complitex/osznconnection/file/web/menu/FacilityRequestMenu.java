/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.menu;

import org.complitex.osznconnection.file.web.DwellingCharacteristicsFileList;
import org.complitex.osznconnection.file.web.FacilityForm2FileList;
import org.complitex.osznconnection.file.web.FacilityServiceTypeFileList;
import org.complitex.osznconnection.file.web.pages.facility.FacilityStreetFileList;
import org.complitex.osznconnection.file.web.pages.facility.FacilityStreetTypeFileList;
import org.complitex.osznconnection.file.web.pages.facility.FacilityTarifFileList;
import org.complitex.template.web.template.ResourceTemplateMenu;

import java.util.Locale;

/**
 *
 * @author Artem
 */
public class FacilityRequestMenu extends ResourceTemplateMenu {
    public FacilityRequestMenu() {
        add("dwelling_characteristics_list", DwellingCharacteristicsFileList.class);
        add("facility_service_type_list", FacilityServiceTypeFileList.class);
        add("facility_form2_list", FacilityForm2FileList.class);
        add("facility_street_type_file_list", FacilityStreetTypeFileList.class);
        add("facility_street_file_list", FacilityStreetFileList.class);
        add("facility_tarif_file_list", FacilityTarifFileList.class);

    }

    @Override
    public String getTitle(Locale locale) {
        return getString(FacilityRequestMenu.class, locale, "title");
    }

    @Override
    public String getTagId() {
        return "facility_request_menu";
    }
}
