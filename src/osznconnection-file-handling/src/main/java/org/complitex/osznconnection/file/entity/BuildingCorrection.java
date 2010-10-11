/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.entity;

/**
 *
 * @author Artem
 */
public class BuildingCorrection extends ObjectCorrection {

    private String correctionCorp;

    private Long internalStreetId;

    public BuildingCorrection() {
    }

    public BuildingCorrection(Long organizationId, Long internalObjectId) {
        super(organizationId, internalObjectId, "building");
    }

    public BuildingCorrection(String buildingNumber, String buildingCorp, Long organizationId, Long internalParentId) {
        super("building", buildingNumber, organizationId, internalParentId);
        this.correctionCorp = buildingCorp;
    }

    public String getCorrectionCorp() {
        return correctionCorp;
    }

    public void setCorrectionCorp(String correctionCorp) {
        this.correctionCorp = correctionCorp;
    }

    public Long getInternalStreetId() {
        return internalStreetId;
    }

    public void setInternalStreetId(Long internalStreetId) {
        this.internalStreetId = internalStreetId;
    }
}
