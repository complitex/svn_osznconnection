/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.calculation.adapter;

import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.util.StringUtil;
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
            throw new PuAccountNumberInfoParseException();
        }
        String puId = parts[0];
        if (puId != null) {
            puId = puId.trim();
        }
        String puAccountNumber = parts[1];
        if (puAccountNumber != null) {
            puAccountNumber = puAccountNumber.trim();
        }
        if (Strings.isEmpty(puId) || Strings.isEmpty(puAccountNumber) || !StringUtil.isNumeric(puId) ||
                !StringUtil.isNumeric(puAccountNumber)) {
            throw new PuAccountNumberInfoParseException();
        }
        return new PuAccountNumberInfo(puId, puAccountNumber);
    }

    public static boolean matches(String realPuAccountNumber, String remotePuAccountNumber) {
        if (Strings.isEmpty(realPuAccountNumber)) {
            throw new IllegalArgumentException("Real pu account number is null or empty.");
        }
        if (Strings.isEmpty(remotePuAccountNumber)) {
            throw new IllegalArgumentException("Remote pu account number is null or empty.");
        }
        if (!StringUtil.isNumeric(remotePuAccountNumber)) {
            throw new IllegalArgumentException("Remote pu account number is not numeric.");
        }
        return realPuAccountNumber.matches("0*" + remotePuAccountNumber);
    }

    public static boolean matches(String realPuAccountNumber, String remotePuId, String remotePuAccountNumber) {
        if (Strings.isEmpty(realPuAccountNumber)) {
            throw new IllegalArgumentException("Real pu account number is null or empty.");
        }
        if (Strings.isEmpty(remotePuId)) {
            throw new IllegalArgumentException("Remote pu id is null or empty.");
        }
        if (Strings.isEmpty(remotePuAccountNumber)) {
            throw new IllegalArgumentException("Remote pu account number is null or empty.");
        }
        if (!StringUtil.isNumeric(remotePuId)) {
            throw new IllegalArgumentException("Remote pu id is not numeric.");
        }
        if (!StringUtil.isNumeric(remotePuAccountNumber)) {
            throw new IllegalArgumentException("Remote pu account number is not numeric.");
        }
        return realPuAccountNumber.matches("0*" + remotePuId + "0*" + remotePuAccountNumber);
    }
}
