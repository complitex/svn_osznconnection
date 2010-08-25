/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.payment;

import javax.ejb.EJB;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.ResourceModel;
import org.complitex.osznconnection.file.entity.RequestPayment;
import org.complitex.osznconnection.file.service.AddressResolver;
import org.complitex.osznconnection.file.service.RequestPaymentBean;
import org.complitex.osznconnection.file.web.component.correction.CorrectionPanel;

/**
 *
 * @author Artem
 */
public final class PaymentCorrection extends WebPage {

    public static final String REQUEST_PAYMENT_ID = "request_payment_id";

    @EJB(name = "RequestPaymentBean")
    private RequestPaymentBean requestPaymentBean;

    @EJB(name = "AddressResolver")
    private AddressResolver addressResolver;

    private RequestPayment requestPayment;

    public PaymentCorrection(PageParameters params) {
        long requestPaymentId = params.getAsLong(REQUEST_PAYMENT_ID);
        init(requestPaymentId);
    }

    private void init(long id) {
        requestPayment = requestPaymentBean.findById(id);

        add(new Label("title", new ResourceModel("label")));
        add(new Label("label", new ResourceModel("label")));

        String name = requestPayment.getSurNam() + " " + requestPayment.getfNam() + " " + requestPayment.getmNam();
        String address = requestPayment.getnName() + ", " + requestPayment.getVulName() + ", " + requestPayment.getBldNum() + ", " + requestPayment.getFlat();

        add(new CorrectionPanel("correntionPanel", name, address, requestPayment.getCityId(), requestPayment.getStreetId(),
                requestPayment.getBuildingId(), requestPayment.getApartmentId()) {

            @Override
            protected void correctAddress(long cityId, long streetId, long buildingId, long apartmentId) {
                RequestPayment correctedRequestPayment = addressResolver.correctAddress(requestPayment, cityId, streetId, buildingId, apartmentId);
                requestPaymentBean.update(correctedRequestPayment);
            }

            @Override
            public void back() {
                setResponsePage(RequestPaymentList.class);
            }
        });
    }
}
