/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.calculation.adapter;

import static org.apache.wicket.util.string.Strings.*;
import static org.complitex.dictionary.util.StringUtil.*;
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
        if (isEmpty(puId) || isEmpty(puAccountNumber) || !isNumeric(puId) || !isNumeric(puAccountNumber)) {
            throw new PuAccountNumberInfoParseException();
        }
        return new PuAccountNumberInfo(puId, puAccountNumber);
    }

    public static boolean matches(String realPuAccountNumber, String remotePuAccountNumber) {
        if (isEmpty(realPuAccountNumber)) {
            throw new IllegalArgumentException("Real pu account number is null or empty.");
        }
        if (isEmpty(remotePuAccountNumber)) {
            throw new IllegalArgumentException("Remote pu account number is null or empty.");
        }
        if (!isNumeric(remotePuAccountNumber)) {
            throw new IllegalArgumentException("Remote pu account number is not numeric.");
        }
        return realPuAccountNumber.matches("0*" + remotePuAccountNumber);
    }

    public static boolean matches(String realPuAccountNumber, String remotePuId, String remotePuAccountNumber) {
        if (isEmpty(realPuAccountNumber)) {
            throw new IllegalArgumentException("Real pu account number is null or empty.");
        }
        if (isEmpty(remotePuId)) {
            throw new IllegalArgumentException("Remote pu id is null or empty.");
        }
        if (isEmpty(remotePuAccountNumber)) {
            throw new IllegalArgumentException("Remote pu account number is null or empty.");
        }
        if (!isNumeric(remotePuId)) {
            throw new IllegalArgumentException("Remote pu id is not numeric.");
        }
        if (!isNumeric(remotePuAccountNumber)) {
            throw new IllegalArgumentException("Remote pu account number is not numeric.");
        }
        return realPuAccountNumber.matches("0*" + remotePuId + "0*" + remotePuAccountNumber);
    }

    public static boolean matches(String realPuAccountNumber, String realLastName, String remotePuAccountNumber,
            String remoteName) {
        if (isEmpty(realPuAccountNumber)) {
            throw new IllegalArgumentException("Real pu account number is null or empty.");
        }
        if (isEmpty(realLastName)) {
            throw new IllegalArgumentException("Real last name is null or empty.");
        }
        if (isEmpty(remotePuAccountNumber)) {
            throw new IllegalArgumentException("Remote pu account number is null or empty.");
        }
        if (isEmpty(remoteName)) {
            return false;
        }
        return remoteName.toUpperCase().startsWith(realLastName.toUpperCase())
                && realPuAccountNumber.matches("\\w*0+" + remotePuAccountNumber);
    }
}
