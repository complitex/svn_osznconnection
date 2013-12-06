package org.complitex.osznconnection.file.web.pages.subsidy;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.complitex.osznconnection.file.entity.RequestFileType;
import org.complitex.osznconnection.file.entity.Subsidy;
import org.complitex.osznconnection.file.entity.SubsidyDBF;
import org.complitex.osznconnection.file.service.SubsidyBean;
import org.complitex.osznconnection.file.service.file_description.RequestFileDescription;
import org.complitex.osznconnection.file.service.file_description.RequestFileDescriptionBean;
import org.complitex.osznconnection.file.service.file_description.RequestFileFieldDescription;
import org.odlabs.wiquery.ui.dialog.Dialog;

import javax.ejb.EJB;
import java.util.Arrays;


/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 05.12.13 18:06
 */
public class SubsidyEditPanel extends Panel {
    @EJB
    private RequestFileDescriptionBean requestFileDescriptionBean;

    @EJB
    private SubsidyBean subsidyBean;

    private Dialog dialog;
    private Subsidy subsidy = new Subsidy();
    private Form form;
    private ListView listView;

    public SubsidyEditPanel(String id, final Component toUpdate) {
        super(id);

        dialog = new Dialog("dialog");
        dialog.setModal(true);
        dialog.setWidth(540);
        dialog.setCloseOnEscape(false);
        add(dialog);

        form = new Form("form");
        form.setOutputMarkupId(true);
        dialog.add(form);

        final FeedbackPanel messages = new FeedbackPanel("messages");
        messages.setOutputMarkupId(true);
        form.add(messages);

        RequestFileDescription description = requestFileDescriptionBean.getFileDescription (RequestFileType.SUBSIDY);

        form.add(listView = new ListView<RequestFileFieldDescription>("fields", description.getFields()) {
            @Override
            protected void populateItem(final ListItem<RequestFileFieldDescription> item) {
                final RequestFileFieldDescription fileFieldDescription = item.getModelObject();
                item.add(new Label("name", fileFieldDescription.getName()));
                item.add(new TextField<Object>("field", new PropertyModel<>(subsidy, "convertedFields[" + fileFieldDescription.getName() + "]")) {
                    @Override
                    protected void onComponentTag(ComponentTag tag) {
                        super.onComponentTag(tag);

                        tag.put("size", fileFieldDescription.getLength() + "");
                    }
                }.setType(fileFieldDescription.getFieldType()));

                String name = fileFieldDescription.getName();

                if (Arrays.asList("P1", "SM1", "SB1", "OB1", "SUMMA").contains(name)) {
                    item.add(AttributeModifier.replace("style", "float:left; clear:left"));
                }
            }
        }.setReuseItems(true));

        form.add(new AjaxSubmitLink("save") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                subsidy.setUpdateFieldMap(subsidy.getDbfFields());
                subsidyBean.update(subsidy);
                dialog.close(target);

                getSession().info(subsidy.getField(SubsidyDBF.RASH) +" " + subsidy.getField(SubsidyDBF.FIO) + ": "
                        + getString("info_updated"));
                target.add(toUpdate);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.add(messages);
            }
        });

        form.add(new AjaxLink("cancel") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                dialog.close(target);
            }
        });
    }

    public void open(AjaxRequestTarget target, Subsidy subsidy){
        this.subsidy = subsidy;

        listView.removeAll();

        target.add(form);
        dialog.open(target);
    }
}
