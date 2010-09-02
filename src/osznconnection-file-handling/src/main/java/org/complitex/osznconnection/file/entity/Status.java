/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.entity;

/**
 *
 * @author Artem
 */
public enum Status {

    CITY_UNRESOLVED_LOCALLY(true, false), STREET_UNRESOLVED_LOCALLY(true, false), BUILDING_UNRESOLVED_LOCALLY(true, false),
    APARTMENT_UNRESOLVED_LOCALLY(true, false),
    ADDRESS_CORRECTED(false, false),
    CITY_UNRESOLVED(false, true), STREET_UNRESOLVED(false, true), BUILDING_UNRESOLVED(false, true), APARTMENT_UNRESOLVED(false, true),
    ACCOUNT_NUMBER_UNRESOLVED_LOCALLY(false, false), RESOLVED(false, false);

    private boolean localAddressCorrected;

    private boolean outgoingAddressCorrected;

    private Status(boolean localAddressCorrection, boolean outgoingAddressCorrection) {
        this.localAddressCorrected = localAddressCorrection;
        this.outgoingAddressCorrected = outgoingAddressCorrection;
    }

    public boolean isLocalAddressCorrected() {
        return localAddressCorrected;
    }

    public boolean isOutgoingAddressCorrected() {
        return outgoingAddressCorrected;
    }
}
