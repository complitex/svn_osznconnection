/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service.status.details;

import org.complitex.osznconnection.file.entity.StatusDetail;
import org.complitex.osznconnection.file.entity.example.FacilityServiceTypeExample;

/**
 *
 * @author Artem
 */
public class FacilityServiceTypeExampleConfigurator extends AbstractExampleConfigurator<FacilityServiceTypeExample> {

    @Override
    public FacilityServiceTypeExample createExample(StatusDetail statusDetail) {
        FacilityServiceTypeExample example = new FacilityServiceTypeExample();
        example.setLastName(statusDetail.getDetail("lastName"));
        example.setFirstName(statusDetail.getDetail("firstName"));
        example.setMiddleName(statusDetail.getDetail("middleName"));
        example.setStreetCode(statusDetail.getDetail("streetCode"));
        example.setBuilding(statusDetail.getDetail("building"));
        example.setCorp(statusDetail.getDetail("buildingCorp"));
        example.setApartment(statusDetail.getDetail("apartment"));
        return example;
    }
}
