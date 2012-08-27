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
import org.complitex.osznconnection.file.web.pages.actualpayment.ActualPaymentList;
import org.complitex.template.web.component.toolbar.ToolbarButton;
import org.complitex.template.web.pages.ScrollListPage;
import org.complitex.template.web.security.SecurityRole;

@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class ActualPaymentFileList extends ScrollListPage {

    @EJB
    private ProcessManagerBean processManagerBean;
    private final AbstractFileListPanel fileListPanel;

    public ActualPaymentFileList(PageParameters parameters) {
        super(parameters);

        add(new Label("title", new ResourceModel("title")));
        add(fileListPanel = new AbstractFileListPanel("fileListPanel") {

            @Override
            protected String getPreferencePage() {
                return ActualPaymentFileList.class.getName();
            }

            @Override
            protected TYPE getRequestFileType() {
                return RequestFile.TYPE.ACTUAL_PAYMENT;
            }

            @Override
            protected Class<? extends WebPage> getItemListPageClass() {
                return ActualPaymentList.class;
            }

            @Override
            protected ProcessType getLoadProcessType() {
                return ProcessType.LOAD_ACTUAL_PAYMENT;
            }

            @Override
            protected ProcessType getBindProcessType() {
                return ProcessType.BIND_ACTUAL_PAYMENT;
            }

            @Override
            protected ProcessType getFillProcessType() {
                return ProcessType.FILL_ACTUAL_PAYMENT;
            }

            @Override
            protected ProcessType getSaveProcessType() {
                return ProcessType.SAVE_ACTUAL_PAYMENT;
            }

            @Override
            protected void bind(List<Long> selectedFileIds, Map<Enum<?>, Object> commandParameters) {
                processManagerBean.bindActualPayment(selectedFileIds, commandParameters);
            }

            @Override
            protected void fill(List<Long> selectedFileIds, Map<Enum<?>, Object> commandParameters) {
                processManagerBean.fillActualPayment(selectedFileIds, commandParameters);
            }

            @Override
            protected void save(List<Long> selectedFileIds, Map<Enum<?>, Object> commandParameters) {
                processManagerBean.saveActualPayment(selectedFileIds, commandParameters);
            }

            @Override
            protected void load(long userOrganizationId, long osznId, DateParameter dateParameter) {
                processManagerBean.loadActualPayment(userOrganizationId, osznId,
                        dateParameter.getMonthFrom(), dateParameter.getMonthTo(), dateParameter.getYear());
            }
        });
    }

    @Override
    protected List<? extends ToolbarButton> getToolbarButtons(String id) {
        return fileListPanel.getToolbarButtons(id);
    }
}
