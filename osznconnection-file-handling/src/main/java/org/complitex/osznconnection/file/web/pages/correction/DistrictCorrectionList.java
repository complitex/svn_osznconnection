/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.correction;

import java.util.List;
import javax.ejb.EJB;
import org.apache.wicket.PageParameters;
import org.complitex.osznconnection.file.entity.Correction;
import org.complitex.osznconnection.file.entity.example.CorrectionExample;
import org.complitex.osznconnection.file.service.AddressCorrectionBean;
import org.complitex.osznconnection.file.web.pages.util.AddressRenderer;

/**
 *
 * @author Artem
 */
public class DistrictCorrectionList extends AddressCorrectionList {

    @EJB(name = "AddressCorrectionBean")
    private AddressCorrectionBean addressCorrectionBean;

    public DistrictCorrectionList(PageParameters params) {
        super(params);
    }

    @Override
    protected List<? extends Correction> find(CorrectionExample example) {
        return addressCorrectionBean.findDistricts(example);
    }

    @Override
    protected String displayCorrection(Correction correction) {
        String city = null;
        if (correction.getParent() != null) {
            city = correction.getParent().getCorrection();
        }
        return AddressRenderer.displayAddress(null, city, correction.getCorrection(), getLocale());
    }
}
