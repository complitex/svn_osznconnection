package org.complitex.osznconnection.file.web.pages.subsidy;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.*;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.complitex.osznconnection.file.entity.SubsidyMasterData;
import org.complitex.osznconnection.file.entity.SubsidyMasterDataDBF;
import org.complitex.osznconnection.file.service.SubsidyMasterDataBean;
import org.complitex.template.web.template.TemplatePage;

import javax.ejb.EJB;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 21.01.14 18:37
 */
public class SubsidyMasterDataList extends TemplatePage {
    @EJB
    private SubsidyMasterDataBean subsidyMasterDataBean;

    public SubsidyMasterDataList(PageParameters pageParameters) {
        Long subsidyId = pageParameters.get("subsidy_id").toLongObject();
        final Long requestFileId = pageParameters.get("request_file_id").toLongObject();

        add(new Label("title", new ResourceModel("title")));
        add(new Label("label", new ResourceModel("title")));

        List<IColumn<SubsidyMasterData>> columns = new ArrayList<>();

        columns.add(new PropertyColumn<SubsidyMasterData>(Model.of("ID"), "id"));
        columns.add(new PropertyColumn<SubsidyMasterData>(Model.of("SERVICING"), "servicingOrganizationId"));

        for (final SubsidyMasterDataDBF key : SubsidyMasterDataDBF.values()){
            columns.add(new AbstractColumn<SubsidyMasterData>(Model.of(key.name())) {
                @Override
                public void populateItem(Item<ICellPopulator<SubsidyMasterData>> cellItem, String componentId,
                                         IModel<SubsidyMasterData> rowModel) {
                    cellItem.add(new Label(componentId, new PropertyModel<>(rowModel, "dbfFields." + key.name())));
                }
            });
        }

        DataTable dataTable = new DataTable<>("data_table", columns,
                new ListDataProvider<>(subsidyMasterDataBean.getSubsidyMasterDataList(subsidyId)), 10);
        dataTable.addTopToolbar(new HeadersToolbar(dataTable, null));
        add(dataTable);

        add(new AjaxLink("back") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                setResponsePage(SubsidyList.class, new PageParameters().add("request_file_id", requestFileId));
            }
        });
    }
}
