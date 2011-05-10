/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.calculation.adapter;

import org.complitex.osznconnection.file.calculation.adapter.entity.PuAccountNumberInfo;
import org.complitex.osznconnection.file.calculation.adapter.exception.PuAccountNumberInfoParseException;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Artem
 */
public class PuAccountNumberInfoParserTest {

    @Test
    public void parserTest() throws PuAccountNumberInfoParseException {
        PuAccountNumberInfo info = PuAccountNumberInfoParser.parse("123.234");
        assertEquals(info.getPuId(), "123");
        assertEquals(info.getPuAccountNumber(), "234");

        info = PuAccountNumberInfoParser.parse("1s23.2f34");
        assertEquals(info.getPuId(), "1s23");
        assertEquals(info.getPuAccountNumber(), "2f34");

        info = PuAccountNumberInfoParser.parse("  123.234     ");
        assertEquals(info.getPuId(), "123");
        assertEquals(info.getPuAccountNumber(), "234");

        info = PuAccountNumberInfoParser.parse(".234");
        assertEquals(info.getPuId(), "");
        assertEquals(info.getPuAccountNumber(), "234");

        info = PuAccountNumberInfoParser.parse("123.  ");
        assertEquals(info.getPuId(), "123");
        assertEquals(info.getPuAccountNumber(), "");

        info = PuAccountNumberInfoParser.parse(".");
        assertEquals(info.getPuId(), "");
        assertEquals(info.getPuAccountNumber(), "");
    }

    @Test(expectedExceptions = {PuAccountNumberInfoParseException.class})
    public void parserExceptionsTest1() throws PuAccountNumberInfoParseException {
        PuAccountNumberInfoParser.parse("234");
    }

    @Test(expectedExceptions = {PuAccountNumberInfoParseException.class})
    public void parserExceptionsTest2() throws PuAccountNumberInfoParseException {
        PuAccountNumberInfoParser.parse("12.23.qw");
    }

    @Test(expectedExceptions = {PuAccountNumberInfoParseException.class})
    public void parserExceptionsTest3() throws PuAccountNumberInfoParseException {
        PuAccountNumberInfoParser.parse("   ");
    }

    @Test
    public void matchTest() {
        assertEquals(PuAccountNumberInfoParser.match("00123", "123"), true);
        assertEquals(PuAccountNumberInfoParser.match("00124", "123"), false);
        assertEquals(PuAccountNumberInfoParser.match("123", "123"), true);
        assertEquals(PuAccountNumberInfoParser.match("qw124", "124"), false);
        assertEquals(PuAccountNumberInfoParser.match("00000125", "00125"), true);
        assertEquals(PuAccountNumberInfoParser.match("0000", ""), false);
    }
}
