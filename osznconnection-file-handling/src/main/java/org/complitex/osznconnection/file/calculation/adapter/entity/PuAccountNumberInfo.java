/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.calculation.adapter.entity;

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
}
