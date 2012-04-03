/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service.util;

import org.complitex.osznconnection.file.service.util.SubsidyNameParser.SubsidyName;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 *
 * @author Artem
 */
public class SubsidyNameParserTest {

    @Test
    public void parseTest1() {
        SubsidyName n;
        n = SubsidyNameParser.parse("123", "Петров В.В.");
        assertEquals(n.getLastName(), "Петров");
        assertEquals(n.getFirstName(), "В");
        assertEquals(n.getMiddleName(), "В");

        n = SubsidyNameParser.parse("123", " Петров  Петр      Петрович ");
        assertEquals(n.getLastName(), "Петров");
        assertEquals(n.getFirstName(), "Петр");
        assertEquals(n.getMiddleName(), "Петрович");

        n = SubsidyNameParser.parse("123", " Петров - Сидоров П.П.");
        assertEquals(n.getLastName(), "Петров - Сидоров");
        assertEquals(n.getFirstName(), "П");
        assertEquals(n.getMiddleName(), "П");

        n = SubsidyNameParser.parse("123", " Петров - Сидоров  Петр    П.");
        assertEquals(n.getLastName(), "Петров - Сидоров");
        assertEquals(n.getFirstName(), "Петр");
        assertEquals(n.getMiddleName(), "П");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void parseTest2() {
        SubsidyNameParser.parse("123", "  ");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void parseTest3() {
        SubsidyNameParser.parse("123", "Петров П");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void parseTest4() {
        SubsidyNameParser.parse("123", "  П.П.");
    }
}
