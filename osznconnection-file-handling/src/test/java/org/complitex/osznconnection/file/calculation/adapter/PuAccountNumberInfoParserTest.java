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

        info = PuAccountNumberInfoParser.parse("   123.234  ");
        assertEquals(info.getPuId(), "123");
        assertEquals(info.getPuAccountNumber(), "234");
    }

    @Test(expectedExceptions = {PuAccountNumberInfoParseException.class})
    public void parserExceptionsTest1() throws PuAccountNumberInfoParseException {
        PuAccountNumberInfoParser.parse(".234");
    }

    @Test(expectedExceptions = {PuAccountNumberInfoParseException.class})
    public void parserExceptionsTest2() throws PuAccountNumberInfoParseException {
        PuAccountNumberInfoParser.parse("234");
    }

    @Test(expectedExceptions = {PuAccountNumberInfoParseException.class})
    public void parserExceptionsTest3() throws PuAccountNumberInfoParseException {
        PuAccountNumberInfoParser.parse("12.23.qw");
    }

    @Test(expectedExceptions = {PuAccountNumberInfoParseException.class})
    public void parserExceptionsTest4() throws PuAccountNumberInfoParseException {
        PuAccountNumberInfoParser.parse("   ");
    }

    @Test(expectedExceptions = {PuAccountNumberInfoParseException.class})
    public void parserExceptionsTest5() throws PuAccountNumberInfoParseException {
        PuAccountNumberInfoParser.parse("123.");
    }

    @Test(expectedExceptions = {PuAccountNumberInfoParseException.class})
    public void parserExceptionsTest6() throws PuAccountNumberInfoParseException {
        PuAccountNumberInfoParser.parse(".");
    }

    @Test(expectedExceptions = {PuAccountNumberInfoParseException.class})
    public void parserExceptionsTest7() throws PuAccountNumberInfoParseException {
        PuAccountNumberInfoParser.parse("12f.23");
    }

    @Test(expectedExceptions = {PuAccountNumberInfoParseException.class})
    public void parserExceptionsTest8() throws PuAccountNumberInfoParseException {
        PuAccountNumberInfoParser.parse("123.234g");
    }

    @Test
    public void matchTest() {
        assertEquals(PuAccountNumberInfoParser.matches("00123", "123"), true);
        assertEquals(PuAccountNumberInfoParser.matches("00124", "123"), false);
        assertEquals(PuAccountNumberInfoParser.matches("123", "123"), true);
        assertEquals(PuAccountNumberInfoParser.matches("qw124", "124"), false);
        assertEquals(PuAccountNumberInfoParser.matches("00000125", "125"), true);
        assertEquals(PuAccountNumberInfoParser.matches("00000125000", "125"), false);
        assertEquals(PuAccountNumberInfoParser.matches("0000012500", "12500"), true);
        assertEquals(PuAccountNumberInfoParser.matches("212147", "21", "2147"), true);
        assertEquals(PuAccountNumberInfoParser.matches("00210002147", "21", "2147"), true);
        assertEquals(PuAccountNumberInfoParser.matches("21002147", "21", "2147"), true);
        assertEquals(PuAccountNumberInfoParser.matches("210021470", "21", "2147"), false);
        assertEquals(PuAccountNumberInfoParser.matches("021000021470", "21", "21470"), true);
        assertEquals(PuAccountNumberInfoParser.matches("02102147", "210", "2147"), true);

    }
}
