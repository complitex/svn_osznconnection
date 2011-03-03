/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.correction;

import org.complitex.osznconnection.file.entity.BuildingCorrection;
import org.complitex.osznconnection.file.entity.Correction;
import org.complitex.osznconnection.file.entity.example.BuildingCorrectionExample;
import org.complitex.osznconnection.file.entity.example.CorrectionExample;
import org.complitex.osznconnection.file.service.AddressCorrectionBean;
import org.complitex.osznconnection.file.web.pages.util.AddressRenderer;

import javax.ejb.EJB;
import java.util.List;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

/**
 * Список коррекций домов.
 * @author Artem
 */
public class BuildingCorrectionList extends AddressCorrectionList {

    @EJB
    private AddressCorrectionBean addressCorrectionBean;

    public BuildingCorrectionList() {
        super("building");
    }

    @Override
    protected IModel<String> getTitleModel() {
        return new StringResourceModel("title", this, null);
    }

    @Override
    protected CorrectionExample newExample() {
        BuildingCorrectionExample correctionExample = new BuildingCorrectionExample();
        correctionExample.setEntity(this.getEntity());
        return correctionExample;
    }

    @Override
    protected List<BuildingCorrection> find(CorrectionExample example) {
        return addressCorrectionBean.findBuildings(example);
    }

    @Override
    protected int count(CorrectionExample example) {
        return addressCorrectionBean.countBuildings(example);
    }

    @Override
    protected String displayCorrection(Correction correction) {
        String city = null;
        String street = null;
        if (correction.getParent() != null && correction.getParent().getParent() != null) {
            city = correction.getParent().getParent().getCorrection();
            street = correction.getParent().getCorrection();
        }

        BuildingCorrection bc = (BuildingCorrection) correction;
        return AddressRenderer.displayAddress(null, city, null, street, bc.getCorrection(), bc.getCorrectionCorp(), null, getLocale());
    }
}
