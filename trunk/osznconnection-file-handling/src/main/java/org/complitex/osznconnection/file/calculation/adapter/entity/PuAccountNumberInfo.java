/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.calculation.adapter.entity;

import org.complitex.dictionary.util.StringUtil;

/**
 *
 * @author Artem
 */
public class PuAccountNumberInfo {

    private String puId;
    private String puAccountNumber;

    public PuAccountNumberInfo(String puId, String puAccountNumber) {
        this.puId = puId;
        this.puAccountNumber = puAccountNumber;
    }

    public String getPuId() {
        return puId;
    }

    public String getPuAccountNumber() {
        return puAccountNumber;
    }

    public String getFullInfo() {
        if (puId == null && puAccountNumber == null) {
            return null;
        }
        return StringUtil.valueOf(puId) + StringUtil.valueOf(puAccountNumber);
    }
}
