package org.complitex.osznconnection.file.web.component;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.complitex.dictionary.strategy.organization.IOrganizationStrategy;
import org.complitex.dictionary.web.component.datatable.ArrowOrderByBorder;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestFileType;
import org.complitex.osznconnection.file.service.process.ProcessManagerBean;
import org.complitex.osznconnection.file.service.process.ProcessType;
import org.complitex.osznconnection.file.web.AbstractFileListPanel;
import org.complitex.osznconnection.file.web.SubsidyFileList;
import org.complitex.osznconnection.file.web.component.load.DateParameter;
import org.complitex.osznconnection.file.web.pages.subsidy.SubsidyList;
import org.complitex.osznconnection.organization.strategy.OsznOrganizationStrategy;

import javax.ejb.EJB;
import java.util.List;
import java.util.Map;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 03.12.13 18:51
 */
public class SubsidyFileListPanel extends AbstractFileListPanel {

    @EJB
    private ProcessManagerBean processManagerBean;

    @EJB(name = IOrganizationStrategy.BEAN_NAME, beanInterface = IOrganizationStrategy.class)
    private OsznOrganizationStrategy organizationStrategy;

    public SubsidyFileListPanel(String id) {
        super(id);

        addColumn(new Column() {
            @Override
            public Component head(ISortStateLocator stateLocator, DataView<?> dataView, Component refresh) {
                return new ArrowOrderByBorder("header.servicing_organization", "servicing_organization", stateLocator, dataView, refresh);
            }

            @Override
            public Component filter() {
                return new TextField<>("servicing_organization", new Model<>(""));
            }

            @Override
            public Component field(Item<RequestFile> item) {
                String fileName = item.getModelObject().getName();
                String code = fileName.substring(0, fileName.length()-4);
                Long organizationId = organizationStrategy.getObjectIdByCode(code);

                String name = "";

                if (organizationId != null){
                    name = organizationStrategy.displayShortNameAndCode(organizationStrategy.findById(organizationId, true),
                            getLocale());
                }

                return new Label("servicing_organization", name);
            }
        });
    }

    @Override
    protected String getPreferencePage() {
        return SubsidyFileList.class.getName();
    }

    @Override
    protected RequestFileType getRequestFileType() {
        return RequestFileType.SUBSIDY;
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
    protected void load(long userOrganizationId, long osznId, DateParameter dateParameter) {
        processManagerBean.loadSubsidy(userOrganizationId, osznId,
                dateParameter.getMonthFrom(), dateParameter.getMonthTo(), dateParameter.getYear());
    }
}
