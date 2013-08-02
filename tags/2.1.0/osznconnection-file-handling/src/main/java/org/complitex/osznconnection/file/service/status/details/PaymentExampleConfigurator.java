/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service.status.details;

import org.complitex.osznconnection.file.entity.StatusDetail;
import org.complitex.osznconnection.file.entity.example.PaymentExample;

/**
 *
 * @author Artem
 */
public class PaymentExampleConfigurator extends AbstractExampleConfigurator<PaymentExample> {

    @Override
    public PaymentExample createExample(StatusDetail statusDetail) {
        PaymentExample example = new PaymentExample();
        example.setAccount(statusDetail.getDetail("account"));
        example.setCity(statusDetail.getDetail("city"));
        example.setStreet(statusDetail.getDetail("street"));
        example.setBuilding(statusDetail.getDetail("building"));
        example.setCorp(statusDetail.getDetail("buildingCorp"));
        example.setApartment(statusDetail.getDetail("apartment"));
        return example;
    }
}
