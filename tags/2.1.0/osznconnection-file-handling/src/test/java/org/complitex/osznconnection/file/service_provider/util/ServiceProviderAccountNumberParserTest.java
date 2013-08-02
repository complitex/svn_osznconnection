/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service_provider.util;

import org.complitex.osznconnection.file.service_provider.ServiceProviderAccountNumberInfo;
import org.complitex.osznconnection.file.service_provider.exception.ServiceProviderAccountNumberParseException;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Artem
 */
public class ServiceProviderAccountNumberParserTest {

    @Test
    public void parserTest() throws ServiceProviderAccountNumberParseException {
        ServiceProviderAccountNumberInfo info = ServiceProviderAccountNumberParser.parse("123.234");
        assertEquals(info.getServiceProviderId(), "123");
        assertEquals(info.getServiceProviderAccountNumber(), "234");

        info = ServiceProviderAccountNumberParser.parse("   123.234  ");
        assertEquals(info.getServiceProviderId(), "123");
        assertEquals(info.getServiceProviderAccountNumber(), "234");
    }

    @Test(expectedExceptions = {ServiceProviderAccountNumberParseException.class})
    public void parserExceptionsTest1() throws ServiceProviderAccountNumberParseException {
        ServiceProviderAccountNumberParser.parse(".234");
    }

    @Test(expectedExceptions = {ServiceProviderAccountNumberParseException.class})
    public void parserExceptionsTest2() throws ServiceProviderAccountNumberParseException {
        ServiceProviderAccountNumberParser.parse("234");
    }

    @Test(expectedExceptions = {ServiceProviderAccountNumberParseException.class})
    public void parserExceptionsTest3() throws ServiceProviderAccountNumberParseException {
        ServiceProviderAccountNumberParser.parse("12.23.qw");
    }

    @Test(expectedExceptions = {ServiceProviderAccountNumberParseException.class})
    public void parserExceptionsTest4() throws ServiceProviderAccountNumberParseException {
        ServiceProviderAccountNumberParser.parse("   ");
    }

    @Test(expectedExceptions = {ServiceProviderAccountNumberParseException.class})
    public void parserExceptionsTest5() throws ServiceProviderAccountNumberParseException {
        ServiceProviderAccountNumberParser.parse("123.");
    }

    @Test(expectedExceptions = {ServiceProviderAccountNumberParseException.class})
    public void parserExceptionsTest6() throws ServiceProviderAccountNumberParseException {
        ServiceProviderAccountNumberParser.parse(".");
    }

    @Test(expectedExceptions = {ServiceProviderAccountNumberParseException.class})
    public void parserExceptionsTest7() throws ServiceProviderAccountNumberParseException {
        ServiceProviderAccountNumberParser.parse("12f.23");
    }

    @Test(expectedExceptions = {ServiceProviderAccountNumberParseException.class})
    public void parserExceptionsTest8() throws ServiceProviderAccountNumberParseException {
        ServiceProviderAccountNumberParser.parse("123.234g");
    }

    @Test
    public void matchTest() {
        assertEquals(ServiceProviderAccountNumberParser.matches("00123", "123"), true);
        assertEquals(ServiceProviderAccountNumberParser.matches("00124", "123"), false);
        assertEquals(ServiceProviderAccountNumberParser.matches("123", "123"), true);
        assertEquals(ServiceProviderAccountNumberParser.matches("qw124", "124"), false);
        assertEquals(ServiceProviderAccountNumberParser.matches("00000125", "125"), true);
        assertEquals(ServiceProviderAccountNumberParser.matches("00000125000", "125"), false);
        assertEquals(ServiceProviderAccountNumberParser.matches("0000012500", "12500"), true);
        assertEquals(ServiceProviderAccountNumberParser.matches("212147", "21", "2147"), true);
        assertEquals(ServiceProviderAccountNumberParser.matches("00210002147", "21", "2147"), true);
        assertEquals(ServiceProviderAccountNumberParser.matches("21002147", "21", "2147"), true);
        assertEquals(ServiceProviderAccountNumberParser.matches("210021470", "21", "2147"), false);
        assertEquals(ServiceProviderAccountNumberParser.matches("021000021470", "21", "21470"), true);
        assertEquals(ServiceProviderAccountNumberParser.matches("02102147", "210", "2147"), true);
        assertEquals(ServiceProviderAccountNumberParser.matches("122.1226", "122", "1226"), true);
        assertEquals(ServiceProviderAccountNumberParser.matches("122\\1226", "122", "1226"), true);
        assertEquals(ServiceProviderAccountNumberParser.matches("122/1226", "122", "1226"), true);
        assertEquals(ServiceProviderAccountNumberParser.matches("122\1226", "122", "1226"), false);
        assertEquals(ServiceProviderAccountNumberParser.matches("122;1226", "122", "1226"), false);
        assertEquals(ServiceProviderAccountNumberParser.matches("www1234", "матвеев", "1234", "  МАТвеев А.В."), true);
        assertEquals(ServiceProviderAccountNumberParser.matches("www01234", "lastname", "234", "lastname aaa bb"), true);
        assertEquals(ServiceProviderAccountNumberParser.matches("www01234", "lastname", "1234", " "), false);
        assertEquals(ServiceProviderAccountNumberParser.matches("16-1234", "  Матвеев", "1234", "МАТВЕЕВ"), true);
    }
}
