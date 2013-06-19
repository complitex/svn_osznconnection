package org.complitex.osznconnection.file.web;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.complitex.osznconnection.file.entity.RequestFileType;
import org.complitex.osznconnection.file.service.process.ProcessManagerBean;
import org.complitex.osznconnection.file.service.process.ProcessType;
import org.complitex.osznconnection.file.web.component.load.DateParameter;
import org.complitex.osznconnection.file.web.component.load.RequestFileLoadPanel.MonthParameterViewMode;
import org.complitex.osznconnection.file.web.pages.dwelling_charact.DwellingCharacteristicsList;
import org.complitex.template.web.component.toolbar.ToolbarButton;
import org.complitex.template.web.pages.ScrollListPage;
import org.complitex.template.web.security.SecurityRole;

import javax.ejb.EJB;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public final class DwellingCharacteristicsFileList extends ScrollListPage {

    @EJB
    private ProcessManagerBean processManagerBean;
    private final AbstractFileListPanel fileListPanel;

    public DwellingCharacteristicsFileList(PageParameters parameters) {
        super(parameters);

        add(new Label("title", new ResourceModel("title")));
        add(fileListPanel = new AbstractFileListPanel("fileListPanel") {

            @Override
            protected String getPreferencePage() {
                return DwellingCharacteristicsFileList.class.getName();
            }

            @Override
            protected RequestFileType getRequestFileType() {
                return RequestFileType.DWELLING_CHARACTERISTICS;
            }

            @Override
            protected Class<? extends WebPage> getItemListPageClass() {
                return DwellingCharacteristicsList.class;
            }

            @Override
            protected ProcessType getLoadProcessType() {
                return ProcessType.LOAD_DWELLING_CHARACTERISTICS;
            }

            @Override
            protected ProcessType getBindProcessType() {
                return ProcessType.BIND_DWELLING_CHARACTERISTICS;
            }

            @Override
            protected ProcessType getFillProcessType() {
                return ProcessType.FILL_DWELLING_CHARACTERISTICS;
            }

            @Override
            protected ProcessType getSaveProcessType() {
                return ProcessType.SAVE_DWELLING_CHARACTERISTICS;
            }

            @Override
            protected void bind(List<Long> selectedFileIds, Map<Enum<?>, Object> commandParameters) {
                processManagerBean.bindDwellingCharacteristics(selectedFileIds, commandParameters);
            }

            @Override
            protected void fill(List<Long> selectedFileIds, Map<Enum<?>, Object> commandParameters) {
            }

            @Override
            protected void save(List<Long> selectedFileIds, Map<Enum<?>, Object> commandParameters) {
                processManagerBean.saveDwellingCharacteristics(selectedFileIds, commandParameters);
            }

            @Override
            protected void load(long userOrganizationId, long osznId, DateParameter dateParameter) {
                processManagerBean.loadDwellingCharacteristics(userOrganizationId, osznId,
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
