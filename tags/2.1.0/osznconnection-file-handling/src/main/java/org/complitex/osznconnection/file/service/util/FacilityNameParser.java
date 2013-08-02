/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service.util;

import org.apache.wicket.util.string.Strings;
import org.complitex.osznconnection.file.entity.PersonName;

/**
 *
 * @author Artem
 */
public final class FacilityNameParser {

    private FacilityNameParser() {
    }

    public static PersonName parse(String fio) {
        String firstName = "";
        String middleName = "";
        String lastName = "";
        if (!Strings.isEmpty(fio)) {
            String[] parts = fio.split(" ", 3);
            if (parts.length > 0) {
                lastName = parts[0];
                if (parts.length > 1) {
                    firstName = parts[1];
                    if (parts.length > 2) {
                        middleName = parts[2];
                    }
                }
            }
        }
        return new PersonName(firstName, middleName, lastName);
    }
}
