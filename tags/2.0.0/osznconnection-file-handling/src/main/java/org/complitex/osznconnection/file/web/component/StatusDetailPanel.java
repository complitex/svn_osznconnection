package org.complitex.osznconnection.file.web.component;

import java.lang.reflect.ParameterizedType;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.complitex.osznconnection.file.entity.StatusDetail;

import javax.ejb.EJB;
import java.util.List;
import org.complitex.osznconnection.file.entity.StatusDetailInfo;
import org.complitex.osznconnection.file.entity.example.AbstractRequestExample;
import org.complitex.osznconnection.file.service.StatusRenderService;
import org.complitex.osznconnection.file.service.status.details.ExampleConfigurator;
import org.complitex.osznconnection.file.service.status.details.IStatusDetailRenderer;
import org.complitex.osznconnection.file.service.status.details.StatusDetailRenderService;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 24.11.10 15:49
 */
public abstract class StatusDetailPanel<T extends AbstractRequestExample> extends Panel {

    @EJB(name = "StatusRenderService")
    private StatusRenderService statusRenderService;
    @EJB(name = "StatusDetailRenderService")
    private StatusDetailRenderService statusDetailRenderService;

    public StatusDetailPanel(String id, final IModel<T> exampleModel, final ExampleConfigurator<T> exampleConfigurator,
            final IStatusDetailRenderer statusDetailRenderer, final Component... update) {
        super(id);
        setOutputMarkupId(true);

        final Class<T> exampleClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];

        WebMarkupContainer container = new WebMarkupContainer("container");
        add(container);

        IModel<List<StatusDetailInfo>> model = new LoadableDetachableModel<List<StatusDetailInfo>>() {

            @Override
            protected List<StatusDetailInfo> load() {
                return loadStatusDetails();
            }
        };

        ListView<StatusDetailInfo> statusDetailsInfo = new ListView<StatusDetailInfo>("statusDetailsInfo", model) {

            @Override
            protected void populateItem(ListItem<StatusDetailInfo> item) {
                final StatusDetailInfo statusDetailInfo = item.getModelObject();

                //Контейнер для ajax обновления вложенного списка
                final WebMarkupContainer statusDetailsContainer = new WebMarkupContainer("statusDetailsContainer");
                statusDetailsContainer.setOutputMarkupPlaceholderTag(true);
                item.add(statusDetailsContainer);

                AjaxLink expand = new AjaxLink("expand") {

                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        for (Component component : update) {
                            target.addComponent(component);
                        }

                        filterByStatusDetailInfo(statusDetailInfo, exampleClass, exampleModel, exampleConfigurator);

                        if (statusDetailInfo.getStatusDetails() != null && !statusDetailInfo.getStatusDetails().isEmpty()) {
                            statusDetailsContainer.setVisible(!statusDetailsContainer.isVisible());
                            target.addComponent(statusDetailsContainer);
                        }
                    }
                };
                item.add(expand);

                String info = statusRenderService.displayStatus(statusDetailInfo.getStatus(), getLocale())
                        + statusDetailRenderService.displayCount(statusDetailInfo.getCount());
                expand.add(new Label("info", info));

                ListView<StatusDetail> statusDetails = new ListView<StatusDetail>("statusDetails",
                        statusDetailInfo.getStatusDetails()) {

                    @Override
                    protected void populateItem(ListItem<StatusDetail> item) {
                        final StatusDetail statusDetail = item.getModelObject();

                        AjaxLink filter = new AjaxLink("filter") {

                            @Override
                            public void onClick(AjaxRequestTarget target) {
                                filterByStatusDetail(statusDetail, exampleModel, exampleConfigurator);

                                for (Component component : update) {
                                    target.addComponent(component);
                                }
                            }
                        };
                        item.add(filter);

                        filter.add(new Label("name", statusDetailRenderService.displayStatusDetail(statusDetailInfo.getStatus(), statusDetail,
                                statusDetailRenderer)));
                    }
                };
                statusDetailsContainer.setVisible(false);
                statusDetailsContainer.add(statusDetails);
            }
        };

        container.add(statusDetailsInfo);
    }

    protected void filterByStatusDetailInfo(StatusDetailInfo statusDetailInfo, Class<T> exampleClass, IModel<T> exampleModel,
            ExampleConfigurator<T> exampleConfigurator) {
        T example = exampleConfigurator.createExample(exampleClass, statusDetailInfo);
        example.setRequestFileId(exampleModel.getObject().getRequestFileId());
        exampleModel.setObject(example);
    }

    protected void filterByStatusDetail(StatusDetail statusDetail, IModel<T> exampleModel,
            ExampleConfigurator<T> exampleConfigurator) {
        T example = exampleConfigurator.createExample(statusDetail);
        example.setRequestFileId(exampleModel.getObject().getRequestFileId());
        exampleModel.setObject(example);
    }

    public abstract List<StatusDetailInfo> loadStatusDetails();
}
