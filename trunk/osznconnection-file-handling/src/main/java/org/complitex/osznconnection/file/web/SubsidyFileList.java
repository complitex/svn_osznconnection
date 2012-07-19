/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web;

import java.util.List;
import java.util.Map;
import org.apache.wicket.markup.html.WebPage;
import org.complitex.osznconnection.file.entity.RequestFile.TYPE;
import org.complitex.osznconnection.file.service.process.ProcessManagerBean;
import org.complitex.osznconnection.file.service.process.ProcessType;
import org.complitex.template.web.security.SecurityRole;

import javax.ejb.EJB;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.web.pages.subsidy.SubsidyList;
import org.complitex.template.web.component.toolbar.ToolbarButton;
import org.complitex.template.web.pages.ScrollListPage;

/**
 *
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public final class SubsidyFileList extends ScrollListPage {

    @EJB
    private ProcessManagerBean processManagerBean;
    private final AbstractFileListPanel fileListPanel;

    public SubsidyFileList(PageParameters parameters) {
        super(parameters);

        add(new Label("title", new ResourceModel("title")));
        add(fileListPanel = new AbstractFileListPanel("fileListPanel") {

            @Override
            protected String getPreferencePage() {
                return SubsidyFileList.class.getName();
            }

            @Override
            protected TYPE getRequestFileType() {
                return RequestFile.TYPE.SUBSIDY;
            }

            @Override
            protected Class<? extends WebPage> getItemListPageClass() {
                return SubsidyList.class;
            }

            @Override
            protected ProcessType getLoadProcessType() {
                return ProcessType.LOAD_SUBSIDY;
            }

            @Override
            protected ProcessType getBindProcessType() {
                return ProcessType.BIND_SUBSIDY;
            }

            @Override
            protected ProcessType getFillProcessType() {
                return ProcessType.FILL_SUBSIDY;
            }

            @Override
            protected ProcessType getSaveProcessType() {
                return ProcessType.SAVE_SUBSIDY;
            }

            @Override
            protected void bind(List<Long> selectedFileIds, Map<Enum<?>, Object> commandParameters) {
                processManagerBean.bindSubsidy(selectedFileIds, commandParameters);
            }

            @Override
            protected void fill(List<Long> selectedFileIds, Map<Enum<?>, Object> commandParameters) {
            }

            @Override
            protected void save(List<Long> selectedFileIds, Map<Enum<?>, Object> commandParameters) {
                processManagerBean.saveSubsidy(selectedFileIds, commandParameters);
            }

            @Override
            protected void load(long userOrganizationId, long osznId, int monthFrom, int monthTo, int year) {
                processManagerBean.loadSubsidy(userOrganizationId, osznId, monthFrom, monthTo, year);
            }
        });
    }

    @Override
    protected List<? extends ToolbarButton> getToolbarButtons(String id) {
        return fileListPanel.getToolbarButtons(id);
    }
}
