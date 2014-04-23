package org.complitex.osznconnection.file.web.pages.subsidy;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.complitex.dictionary.web.component.form.TextFieldPanel;
import org.complitex.osznconnection.file.entity.RequestFileType;
import org.complitex.osznconnection.file.service.file_description.RequestFileDescription;
import org.complitex.osznconnection.file.service.file_description.RequestFileDescriptionBean;
import org.complitex.osznconnection.file.service.file_description.RequestFileFieldDescription;
import org.odlabs.wiquery.ui.dialog.Dialog;

import javax.ejb.EJB;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 16.04.2014 16:29
 */
public class SubsidyFilterDialog extends Panel{
    @EJB
    private RequestFileDescriptionBean requestFileDescriptionBean;

    private final static List<String> FIELDS = Arrays.asList(
            "P1","P2","P3","P4","P5","P6","P7","P8",
            "SM1","SM2","SM3","SM4","SM5","SM6","SM7","SM8",
            "SB1","SB2","SB3","SB4","SB5","SB6","SB7","SB8",
            "OB1","OB2","OB3","OB4","OB5","OB6","OB7","OB8",
            "NM_PAY","SUMMA","SUBS","KVT");

    private Dialog dialog;
    private Form form;

    public SubsidyFilterDialog(String id, IModel<Map<String, Object>> model, final Component updateOnSubmit) {
        super(id);

        dialog = new Dialog("dialog");
        dialog.setModal(true);
        dialog.setWidth(750);
        add(dialog);

        form = new Form<>("form", CompoundPropertyModel.of(model));
        form.setOutputMarkupId(true);
        dialog.add(form);

        final FeedbackPanel messages = new FeedbackPanel("messages");
        messages.setOutputMarkupId(true);
        form.add(messages);

        RequestFileDescription description = requestFileDescriptionBean.getFileDescription(RequestFileType.SUBSIDY);

        for (RequestFileFieldDescription d : description.getFields()){
            if (FIELDS.contains(d.getName())) {
                TextFieldPanel<Object> textField = new TextFieldPanel<>(d.getName(), new PropertyModel<>(model, d.getName()),
                        d.getFieldType(), d.getLength());
                textField.setOutputMarkupId(true);

                form.add(textField);
            }
        }

        form.add(new AjaxButton("filter") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                target.add(updateOnSubmit);
                dialog.close(target);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.add(messages);
            }
        });

        form.add(new AjaxLink("close") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                dialog.close(target);
            }
        });
    }

    public void open(AjaxRequestTarget target){
        target.add(form);
        dialog.open(target);
    }
}
