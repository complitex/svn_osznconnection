/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.facility;

import javax.ejb.EJB;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.service.process.ProcessManagerBean;
import org.complitex.osznconnection.file.service.process.ProcessType;
import org.complitex.osznconnection.file.web.component.load.DateParameter;

/**
 *
 * @author Artem
 */
public final class FacilityStreetFileList extends AbstractReferenceBookFileList {

    @EJB
    private ProcessManagerBean processManagerBean;

    public FacilityStreetFileList() {
    }

    @Override
    protected RequestFile.TYPE getRequestFileType() {
        return RequestFile.TYPE.FACILITY_STREET;
    }

    @Override
    protected void load(long userOrganizationId, long osznId, DateParameter dateParameter) {
        processManagerBean.loadFacilityStreetReferences(userOrganizationId, osznId, dateParameter.getMonth(),
                dateParameter.getYear(), getLocale());
    }

    @Override
    protected ProcessType getLoadProcessType() {
        return ProcessType.LOAD_FACILITY_STREET_REFERENCE;
    }
}