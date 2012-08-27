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
public final class SubsidyNameParser {

    private SubsidyNameParser() {
    }

    /**
     *   FIO может быть в следующих видах: 
     *   1. "<LastName> <F>.<M>." or 
     *   2. "<LastName> <FirstName> <MiddleName>".
     *   3. "<LastName> <F>."
     *   4. "<LastName> <FirstName>"
     *   5. "<LastName>"
     */
    public static PersonName parse(String rash, String fio) {
        if (Strings.isEmpty(fio)) {
            throw new IllegalArgumentException("Поле `FIO` пустое, `RASH` записи: " + rash);
        }

        // нормализация: заменить несколько подряд идущих точек одной, то же самое для пробелов, 
        // обрезать пробелы с начала и с конца.
        fio = fio.replaceAll("(\\.)+", ".").replaceAll("( )+", " ").trim();

        String lastName = "";
        String firstName = "";
        String middleName = "";

        // если заканчивается на "." то выбрасываем её.
        if (fio.endsWith(".")) {
            fio = fio.substring(0, fio.length() - 1);
        }

        // переворачиваем строку и разделяем её на части, где разделитель либо пробел, либо точка.
        // причём делим на максимум 3 части.
        String reverseFio = new StringBuilder(fio).reverse().toString();
        String[] parts = reverseFio.split("(\\.| )", 3);

        // восстанавливаем фамилию, имя и отчество в зависимости от кол-ва частей.
        switch (parts.length) {
            case 3:
                middleName = new StringBuilder(parts[0]).reverse().toString();
            case 2:
                firstName = new StringBuilder(parts[parts.length - 2]).reverse().toString();
            case 1:
                lastName = new StringBuilder(parts[parts.length - 1]).reverse().toString();
        }

        if (Strings.isEmpty(lastName)) {
            throw new RuntimeException("Поле `FIO` не содержит фамилию. `FIO`: '" + fio + "', `RASH` записи: " + rash);
        }

        return new PersonName(firstName, middleName, lastName);
    }
}
