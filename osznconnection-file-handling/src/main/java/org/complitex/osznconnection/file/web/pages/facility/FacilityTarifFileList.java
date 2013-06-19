package org.complitex.osznconnection.file.web.pages.facility;

import org.apache.wicket.Page;
import org.complitex.osznconnection.file.entity.RequestFileType;
import org.complitex.osznconnection.file.service.process.ProcessManagerBean;
import org.complitex.osznconnection.file.service.process.ProcessType;
import org.complitex.osznconnection.file.web.component.load.DateParameter;

import javax.ejb.EJB;

/**
 *
 * @author Artem
 */
public final class FacilityTarifFileList extends AbstractReferenceBookFileList {

    @EJB
    private ProcessManagerBean processManagerBean;

    public FacilityTarifFileList() {
    }

    @Override
    protected RequestFileType getRequestFileType() {
        return RequestFileType.FACILITY_TARIF;
    }

    @Override
    protected void load(long userOrganizationId, long osznId, DateParameter dateParameter) {
        processManagerBean.loadFacilityTarifReferences(userOrganizationId, osznId, dateParameter.getMonth(),
                dateParameter.getYear(), getLocale());
    }

    @Override
    protected ProcessType getLoadProcessType() {
        return ProcessType.LOAD_FACILITY_TARIF_REFERENCE;
    }

    @Override
    protected Class<? extends Page> getItemsPage() {
        return FacilityTarifList.class;
    }
}
