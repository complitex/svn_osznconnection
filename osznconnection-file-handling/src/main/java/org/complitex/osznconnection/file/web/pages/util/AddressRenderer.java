/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.util;

import java.util.Locale;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.util.ResourceUtil;

/**
 *
 * @author Artem
 */
public final class AddressRenderer {

    private static final String RESOURCE_BUNDLE = AddressRenderer.class.getName();

    private AddressRenderer() {
    }

    public static String displayBuilding(String buildingNumber, String buildingCorp, Locale locale) {
        String result = "";
        if (!Strings.isEmpty(buildingNumber)) {
            result = buildingNumber;
            if (!Strings.isEmpty(buildingCorp)) {
                result += " " + ResourceUtil.getString(RESOURCE_BUNDLE, "building_corp", locale) + " " + buildingCorp;
            }
        }
        return result;
    }

    public static String displayApartment(String apartment, Locale locale) {
        if (!Strings.isEmpty(apartment)) {
            return ResourceUtil.getString(RESOURCE_BUNDLE, "apartment", locale) + " " + apartment;
        }
        return "";
    }

    public static String displayStreet(String streetType, String street, Locale locale) {
        if (Strings.isEmpty(streetType)) {
            return street;
        } else {
            return streetType + " " + street;
        }
    }

    public static String displayAddress(String streetType, String street, String buildingNumber, String buildingCorp, String apartment, Locale locale) {
        String displayStreet = displayStreet(streetType, street, locale);
        String displayBuilding = displayBuilding(buildingNumber, buildingCorp, locale);
        String displayApartment = displayApartment(apartment, locale);
        return displayStrings(displayStreet, displayBuilding, displayApartment);
    }

    private static String displayStrings(String... strings) {
        String result = "";
        for (String string : strings) {
            if (!Strings.isEmpty(string)) {
                if (!Strings.isEmpty(result)) {
                    result += ", " + string;
                } else {
                    result = string;
                }
            }
        }
        return result;
    }
}
