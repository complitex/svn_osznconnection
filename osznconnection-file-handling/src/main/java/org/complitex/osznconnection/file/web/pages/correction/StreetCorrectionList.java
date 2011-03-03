/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.correction;

import java.util.List;
import javax.ejb.EJB;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.string.Strings;
import org.complitex.osznconnection.file.entity.Correction;
import org.complitex.osznconnection.file.entity.StreetCorrection;
import org.complitex.osznconnection.file.entity.example.CorrectionExample;
import org.complitex.osznconnection.file.service.AddressCorrectionBean;
import org.complitex.osznconnection.file.web.pages.util.AddressRenderer;

/**
 *
 * @author Artem
 */
public class StreetCorrectionList extends AddressCorrectionList {

    @EJB
    private AddressCorrectionBean addressCorrectionBean;

    public StreetCorrectionList() {
        super("street");
    }

    @Override
    protected IModel<String> getTitleModel() {
        return new StringResourceModel("title", this, null);
    }

    @Override
    protected List<StreetCorrection> find(CorrectionExample example) {
        return addressCorrectionBean.findStreets(example);
    }

    @Override
    protected String displayCorrection(Correction correction) {
        StreetCorrection streetCorrection = (StreetCorrection) correction;
        String city = null;
        if (streetCorrection.getParent() != null) {
            city = streetCorrection.getParent().getCorrection();
        }
        String streetType = null;
        if (streetCorrection.getStreetTypeCorrection() != null) {
            streetType = streetCorrection.getStreetTypeCorrection().getCorrection();
        }
        if (Strings.isEmpty(streetType)) {
            streetType = null;
        }

        return AddressRenderer.displayAddress(null, city, streetType, streetCorrection.getCorrection(), null, null, null, getLocale());
    }
}
