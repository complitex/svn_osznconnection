/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web;

import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestFile.TYPE;
import org.complitex.osznconnection.file.service.process.ProcessManagerBean;
import org.complitex.osznconnection.file.service.process.ProcessType;
import org.complitex.osznconnection.file.web.component.load.DateParameter;
import org.complitex.osznconnection.file.web.component.load.RequestFileLoadPanel.MonthParameterViewMode;
import org.complitex.osznconnection.file.web.pages.facility_service_type.FacilityServiceTypeList;
import org.complitex.template.web.component.toolbar.ToolbarButton;
import org.complitex.template.web.pages.ScrollListPage;
import org.complitex.template.web.security.SecurityRole;

/**
 *
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public final class FacilityServiceTypeFileList extends ScrollListPage {

    @EJB
    private ProcessManagerBean processManagerBean;
    private final AbstractFileListPanel fileListPanel;

    public FacilityServiceTypeFileList(PageParameters parameters) {
        super(parameters);

        add(new Label("title", new ResourceModel("title")));
        add(fileListPanel = new AbstractFileListPanel("fileListPanel") {

            @Override
            protected String getPreferencePage() {
                return FacilityServiceTypeFileList.class.getName();
            }

            @Override
            protected TYPE getRequestFileType() {
                return RequestFile.TYPE.FACILITY_SERVICE_TYPE;
            }

            @Override
            protected Class<? extends WebPage> getItemListPageClass() {
                return FacilityServiceTypeList.class;
            }

            @Override
            protected ProcessType getLoadProcessType() {
                return ProcessType.LOAD_FACILITY_SERVICE_TYPE;
            }

            @Override
            protected ProcessType getBindProcessType() {
                return ProcessType.BIND_FACILITY_SERVICE_TYPE;
            }

            @Override
            protected ProcessType getFillProcessType() {
                return ProcessType.FILL_FACILITY_SERVICE_TYPE;
            }

            @Override
            protected ProcessType getSaveProcessType() {
                return ProcessType.SAVE_FACILITY_SERVICE_TYPE;
            }

            @Override
            protected void bind(List<Long> selectedFileIds, Map<Enum<?>, Object> commandParameters) {
                processManagerBean.bindFacilityServiceType(selectedFileIds, commandParameters);
            }

            @Override
            protected void fill(List<Long> selectedFileIds, Map<Enum<?>, Object> commandParameters) {
            }

            @Override
            protected void save(List<Long> selectedFileIds, Map<Enum<?>, Object> commandParameters) {
                processManagerBean.saveFacilityServiceType(selectedFileIds, commandParameters);
            }

            @Override
            protected void load(long userOrganizationId, long osznId, DateParameter dateParameter) {
                processManagerBean.loadFacilityServiceType(userOrganizationId, osznId,
                        dateParameter.getMonth(), dateParameter.getYear());
            }

            @Override
            protected MonthParameterViewMode getLoadMonthParameterViewMode() {
                return MonthParameterViewMode.EXACT;
            }
        });
    }

    @Override
    protected List<? extends ToolbarButton> getToolbarButtons(String id) {
        return fileListPanel.getToolbarButtons(id);
    }
}
