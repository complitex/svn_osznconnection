/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service.util;

import org.apache.wicket.util.string.Strings;

/**
 *
 * @author Artem
 */
public final class SubsidyNameParser {

    public static class SubsidyName {

        private final String firstName;
        private final String middleName;
        private final String lastName;

        public SubsidyName(String firstName, String middleName, String lastName) {
            this.firstName = firstName;
            this.middleName = middleName;
            this.lastName = lastName;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getMiddleName() {
            return middleName;
        }
    }

    private SubsidyNameParser() {
    }

    /**
     *   FIO может быть в двух видах: 
     *   1. "<LastName> <F>.<M>." or 
     *   2. "<LastName> <FirstName> <MiddleName>".
     */
    public static SubsidyName parse(String rash, String fio) {
        if (Strings.isEmpty(fio)) {
            throw new IllegalArgumentException("Поле `FIO` пустое, `RASH` записи: " + rash);
        }

        fio = fio.trim();

        final String errorMessage = "Поле `FIO` имеет некорректный формат: '" + fio + "', `RASH` записи: " + rash;

        String lastName = null;
        String firstName = null;
        String middleName = null;

        // если заканчивается на "." то выбрасываем точку.
        if (fio.endsWith(".")) {
            fio = fio.substring(0, fio.length() - 1);
        }
        // читаем с конца fio до первого пробела или точки - это и будет отчество.
        int lastDotIndex = fio.lastIndexOf('.');
        int lastWhiteSpaceIndex = fio.lastIndexOf(' ');
        int middleNameStartIndex;
        if (lastDotIndex > lastWhiteSpaceIndex && lastDotIndex > -1) {
            middleNameStartIndex = lastDotIndex;
        } else if (lastWhiteSpaceIndex > lastDotIndex && lastWhiteSpaceIndex > -1) {
            middleNameStartIndex = lastWhiteSpaceIndex;
        } else {
            throw new RuntimeException(errorMessage);
        }
        middleName = fio.substring(middleNameStartIndex + 1);
        fio = fio.substring(0, middleNameStartIndex).trim();
        if (Strings.isEmpty(fio)) {
            throw new RuntimeException(errorMessage);
        }

        // дальше читаем до ближайшего пробела - это имя.
        int firstNameStartIndex = fio.lastIndexOf(' ');
        firstName = fio.substring(firstNameStartIndex + 1);
        fio = fio.substring(0, firstNameStartIndex).trim();
        if (Strings.isEmpty(fio)) {
            throw new RuntimeException(errorMessage);
        }

        // наконец все что осталось - фамилия
        lastName = fio;

        return new SubsidyName(firstName.trim(), middleName.trim(), lastName.trim());
    }
}
