/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.calculation.adapter;

import java.util.Arrays;
import org.complitex.osznconnection.file.calculation.adapter.entity.PuAccountNumberInfo;
import org.complitex.osznconnection.file.calculation.adapter.exception.PuAccountNumberInfoParseException;

/**
 *
 * @author Artem
 */
public class PuAccountNumberInfoParser {

    private PuAccountNumberInfoParser() {
    }

    public static PuAccountNumberInfo parse(String puAccountNumberInfo) throws PuAccountNumberInfoParseException {
        if (puAccountNumberInfo == null) {
            throw new PuAccountNumberInfoParseException();
        }
        puAccountNumberInfo = puAccountNumberInfo.trim();
        String[] parts = puAccountNumberInfo.split("\\.");
        if (parts == null || parts.length != 2) {
            if (puAccountNumberInfo.endsWith(".")) {
                if (parts.length == 0) {
                    return new PuAccountNumberInfo("", "");
                } else if (parts.length == 1) {
                    return new PuAccountNumberInfo(parts[0], "");
                }
            }
            throw new PuAccountNumberInfoParseException();
        }
        return new PuAccountNumberInfo(parts[0], parts[1]);
    }

    public static boolean match(String realPuAccountNumber, String remotePuAccountNumber) {
        if (realPuAccountNumber == null) {
            throw new IllegalArgumentException("Real PU account number is null.");
        }
        realPuAccountNumber = realPuAccountNumber.trim();
        
        if (remotePuAccountNumber == null || remotePuAccountNumber.isEmpty()) {
            return false;
        }
        int i = realPuAccountNumber.indexOf(remotePuAccountNumber);
        if (i >= 0) {
            String head = realPuAccountNumber.substring(0, i);
            if (head.isEmpty()) {
                return true;
            }
            return head.equals(zeroString(i));
        }
        return false;
    }
    static final char ZERO = '0';

    static String zeroString(int length) {
        char[] chars = new char[length];
        Arrays.fill(chars, ZERO);
        return new String(chars);
    }
}
