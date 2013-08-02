package org.complitex.osznconnection.file.web.pages.facility;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.osznconnection.file.entity.FacilityStreet;
import org.complitex.osznconnection.file.entity.FacilityStreetDBF;
import org.complitex.osznconnection.file.service.FacilityReferenceBookBean;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.ListTemplatePage;

import javax.ejb.EJB;
import java.util.List;

import static org.complitex.dictionary.util.StringUtil.asList;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 10.04.13 16:29
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class FacilityStreetList extends ListTemplatePage<FacilityStreet>{
    @EJB
    private FacilityReferenceBookBean facilityReferenceBookBean;

    public FacilityStreetList(PageParameters pageParameters) {
        super(pageParameters, "dbfFields.", FacilityStreetFileList.class);
    }

    @Override
    protected FacilityStreet newFilterObject(PageParameters pageParameters) {
        return new FacilityStreet(pageParameters.get("request_file_id").toLongObject());
    }

    @Override
    protected List<FacilityStreet> getList(FilterWrapper<FacilityStreet> filterWrapper) {
        return facilityReferenceBookBean.getFacilityStreets(filterWrapper);
    }

    @Override
    protected int getCount(FilterWrapper<FacilityStreet> filterWrapper) {
        return facilityReferenceBookBean.getFacilityStreetsCount(filterWrapper);
    }

    @Override
    protected List<String> getProperties() {
        return asList(FacilityStreetDBF.class);
    }
}
