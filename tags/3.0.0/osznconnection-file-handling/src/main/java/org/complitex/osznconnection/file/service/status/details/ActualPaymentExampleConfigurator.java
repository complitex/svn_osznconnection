/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service.status.details;

import org.complitex.osznconnection.file.entity.StatusDetail;
import org.complitex.osznconnection.file.entity.example.ActualPaymentExample;

/**
 *
 * @author Artem
 */
public class ActualPaymentExampleConfigurator extends AbstractExampleConfigurator<ActualPaymentExample> {

    @Override
    public ActualPaymentExample createExample(StatusDetail statusDetail) {
        ActualPaymentExample example = new ActualPaymentExample();
        example.setLastName(statusDetail.getDetail("lastName"));
        example.setFirstName(statusDetail.getDetail("firstName"));
        example.setMiddleName(statusDetail.getDetail("middleName"));
        example.setCity(statusDetail.getDetail("city"));
        example.setStreet(statusDetail.getDetail("street"));
        example.setBuilding(statusDetail.getDetail("building"));
        example.setCorp(statusDetail.getDetail("buildingCorp"));
        example.setApartment(statusDetail.getDetail("apartment"));
        return example;
    }
}
