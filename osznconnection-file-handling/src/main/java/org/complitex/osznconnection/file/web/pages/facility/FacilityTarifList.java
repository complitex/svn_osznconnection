package org.complitex.osznconnection.file.web.pages.facility;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.osznconnection.file.entity.FacilityTarif;
import org.complitex.osznconnection.file.entity.FacilityTarifDBF;
import org.complitex.osznconnection.file.service.FacilityReferenceBookBean;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.ListTemplatePage;

import javax.ejb.EJB;
import java.util.List;

import static org.complitex.dictionary.util.StringUtil.asList;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 10.04.13 17:34
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class FacilityTarifList extends ListTemplatePage<FacilityTarif> {
    @EJB
    private FacilityReferenceBookBean facilityReferenceBookBean;

    public FacilityTarifList(PageParameters pageParameters) {
        super(pageParameters, "dbfFields.", FacilityTarifFileList.class);
    }

    @Override
    protected FacilityTarif newFilterObject(PageParameters pageParameters) {
        return new FacilityTarif(pageParameters.get("request_file_id").toLongObject());
    }

    @Override
    protected List<FacilityTarif> getList(FilterWrapper<FacilityTarif> filterWrapper) {
        return facilityReferenceBookBean.getFacilityTarifs(filterWrapper);
    }

    @Override
    protected int getCount(FilterWrapper<FacilityTarif> filterWrapper) {
        return facilityReferenceBookBean.getFacilityTarifsCount(filterWrapper);
    }

    @Override
    protected List<String> getProperties() {
        return asList(FacilityTarifDBF.class);
    }
}
