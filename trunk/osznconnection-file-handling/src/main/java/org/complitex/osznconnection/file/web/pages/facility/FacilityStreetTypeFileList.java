package org.complitex.osznconnection.file.web.pages.facility;

import org.apache.wicket.Page;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.complitex.osznconnection.file.entity.RequestFileType;
import org.complitex.osznconnection.file.service.process.ProcessManagerBean;
import org.complitex.osznconnection.file.service.process.ProcessType;
import org.complitex.osznconnection.file.web.component.load.DateParameter;
import org.complitex.template.web.security.SecurityRole;

import javax.ejb.EJB;

/**
 *
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public final class FacilityStreetTypeFileList extends AbstractReferenceBookFileList {

    @EJB
    private ProcessManagerBean processManagerBean;

    public FacilityStreetTypeFileList() {
    }

    @Override
    protected RequestFileType getRequestFileType() {
        return RequestFileType.FACILITY_STREET_TYPE;
    }

    @Override
    protected void load(long userOrganizationId, long osznId, DateParameter dateParameter) {
        processManagerBean.loadFacilityStreetTypeReferences(userOrganizationId, osznId, dateParameter.getMonth(),
                dateParameter.getYear());
    }

    @Override
    protected ProcessType getLoadProcessType() {
        return ProcessType.LOAD_FACILITY_STREET_TYPE_REFERENCE;
    }

    @Override
    protected Class<? extends Page> getItemsPage() {
        return FacilityStreetTypeList.class;
    }
}
