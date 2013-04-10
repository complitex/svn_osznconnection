package org.complitex.osznconnection.file.web.pages.facility;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.dictionary.util.StringUtil;
import org.complitex.osznconnection.file.entity.FacilityStreetType;
import org.complitex.osznconnection.file.entity.FacilityStreetTypeDBF;
import org.complitex.osznconnection.file.service.FacilityReferenceBookBean;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.ListTemplatePage;

import javax.ejb.EJB;
import java.util.List;

/**
 * @author Anatoly Ivanov java@inheaven.ru
 * Date: 03.04.13 11:04
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class FacilityStreetTypeList extends ListTemplatePage<FacilityStreetType>{
    @EJB
    private FacilityReferenceBookBean facilityReferenceBookBean;

    public FacilityStreetTypeList(PageParameters pageParameters) {
        super(pageParameters, "dbfFields.", FacilityStreetTypeFileList.class);
    }

    @Override
    protected FacilityStreetType newFilterObject(PageParameters pageParameters) {
        return new FacilityStreetType(pageParameters.get("request_file_id").toLongObject());
    }

    @Override
    protected List<FacilityStreetType> getList(FilterWrapper<FacilityStreetType> filterWrapper) {
        return facilityReferenceBookBean.getFacilityStreetTypes(filterWrapper);
    }

    @Override
    protected int getCount(FilterWrapper<FacilityStreetType> filterWrapper) {
        return facilityReferenceBookBean.getFacilityStreetTypesCount(filterWrapper);
    }

    @Override
    protected List<String> getProperties() {
        return StringUtil.asList(FacilityStreetTypeDBF.class);
    }
}
