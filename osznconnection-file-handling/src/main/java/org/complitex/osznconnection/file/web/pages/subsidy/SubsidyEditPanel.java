package org.complitex.osznconnection.file.web.pages.subsidy;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.complitex.dictionary.web.component.LabelTextField;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.entity.example.SubsidyExample;
import org.complitex.osznconnection.file.service.RequestFileBean;
import org.complitex.osznconnection.file.service.SubsidyBean;
import org.complitex.osznconnection.file.service.SubsidyService;
import org.complitex.osznconnection.file.service.file_description.RequestFileDescription;
import org.complitex.osznconnection.file.service.file_description.RequestFileDescriptionBean;
import org.complitex.osznconnection.file.service.file_description.RequestFileFieldDescription;
import org.odlabs.wiquery.ui.dialog.Dialog;

import javax.ejb.EJB;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 05.12.13 18:06
 */
public class SubsidyEditPanel extends Panel {
    @EJB
    private RequestFileDescriptionBean requestFileDescriptionBean;

    @EJB
    private SubsidyBean subsidyBean;

    @EJB
    private SubsidyService subsidyService;

    @EJB
    private RequestFileBean requestFileBean;

    private Dialog dialog;
    private IModel<Subsidy> subsidyModel = Model.of(new Subsidy());
    private Form form;

    private Map<String, LabelTextField> textFieldMap = new HashMap<>();

    private AjaxSubmitLink link;

    public SubsidyEditPanel(String id, final Component toUpdate) {
        super(id);

        dialog = new Dialog("dialog");
        dialog.setModal(true);
        dialog.setWidth(750);
        dialog.setCloseOnEscape(false);
        add(dialog);

        form = new Form("form"){
            @Override
            protected void onBeforeRender() {
                super.onBeforeRender();

                if (subsidyModel.getObject().getUserOrganizationId() != null) {
                    SubsidyEditPanel.this.validate(null);
                }
            }
        };
        form.setOutputMarkupId(true);
        dialog.add(form);

        final FeedbackPanel messages = new FeedbackPanel("messages");
        messages.setOutputMarkupId(true);
        form.add(messages);

        final RequestFileDescription description = requestFileDescriptionBean.getFileDescription (RequestFileType.SUBSIDY);

        int index = 0;
        for (RequestFileFieldDescription d : description.getFields()){
            LabelTextField textField = new LabelTextField<>(d.getName(), d.getLength(),
                    new PropertyModel<>(subsidyModel, "convertedFields[" + d.getName() + "]"));
            textField.setType(d.getFieldType());
            textField.setOutputMarkupId(true);

            if (++index > 11){
                textField.add(new AjaxFormComponentUpdatingBehavior("onchange") {
                    @Override
                    protected void onUpdate(AjaxRequestTarget target) {
                        validate(target);
                    }
                });
            }else {
                textField.setEnabled(false);
            }

            textFieldMap.put(d.getName(), textField);

            form.add(textField);
        }

        form.add(link = new AjaxSubmitLink("save") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                Subsidy subsidy = subsidyModel.getObject();

                RequestFile requestFile = requestFileBean.findById(subsidy.getRequestFileId());

                if (RequestStatus.SUBSIDY_NM_PAY_ERROR.equals(subsidy.getStatus())) {
                    subsidy.setStatus(RequestStatus.LOADED);

                    //save
                    Map<String, String> updateFields = new HashMap<>();
                    int index = 0;
                    for (RequestFileFieldDescription d : description.getFields()){
                        if (++index > 11){
                            updateFields.put(d.getName(), subsidy.getDbfFields().get(d.getName()));
                        }
                    }
                    subsidy.setUpdateFieldMap(updateFields);

                    subsidyBean.update(subsidy);

                    //update request file status
                    if (RequestFileStatus.LOAD_ERROR.equals(requestFile.getStatus())) {
                        SubsidyExample loaded = new SubsidyExample();
                        loaded.setRequestFileId(subsidy.getRequestFileId());
                        loaded.setStatus(RequestStatus.LOADED);

                        SubsidyExample all = new SubsidyExample();
                        all.setRequestFileId(subsidy.getRequestFileId());

                        if (subsidyBean.count(loaded) == subsidyBean.count(all)) {
                            requestFile.setStatus(RequestFileStatus.LOADED);

                            requestFileBean.save(requestFile);
                        }
                    }
                }

                dialog.close(target);

                getSession().info(subsidy.getField(SubsidyDBF.RASH) + " " + subsidy.getField(SubsidyDBF.FIO) + ": "
                        + getString("info_updated"));
                target.add(toUpdate);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.add(messages);
            }
        });
        link.setOutputMarkupId(true);

        form.add(new AjaxLink("cancel") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                dialog.close(target);
                target.add(toUpdate);
            }
        });

        form.add(new AjaxLink("recalculate") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                Subsidy subsidy = subsidyModel.getObject();

                SubsidySum subsidySum = subsidyService.getSubsidySum(subsidy);

                subsidy.setField(SubsidyDBF.NM_PAY, subsidySum.getNSum());
                subsidy.setField(SubsidyDBF.SUMMA, subsidySum.getSmSum());
                subsidy.setField(SubsidyDBF.SUBS, subsidySum.getSbSum());

                validate(target);
            }
        });
    }

    public void open(AjaxRequestTarget target, Subsidy subsidy){
        subsidyModel.setObject(subsidy);

        target.add(form);
        dialog.open(target);
    }

    private void validate(final AjaxRequestTarget target){
        Subsidy subsidy = subsidyModel.getObject();

        SubsidySum subsidySum = subsidyService.getSubsidySum(subsidy);

        Long numm = subsidy.getField("NUMM");
        BigDecimal summa =  subsidy.getField("SUMMA");
        BigDecimal subs = subsidy.getField("SUBS");
        BigDecimal nmPay = subsidy.getField("NM_PAY");

        LabelTextField nmPayTextField = textFieldMap.get("NM_PAY");
        LabelTextField summaTextField = textFieldMap.get("SUMMA");
        LabelTextField subsTextField = textFieldMap.get("SUBS");
        LabelTextField nummTextField = textFieldMap.get("NUMM");

        boolean nummCheck =  numm <= 0 || summa.compareTo(subs.multiply(new BigDecimal(numm))) == 0;

        nmPayTextField.add(AttributeModifier.replace("style", nmPay.compareTo(subsidySum.getNSum()) != 0
                ? "background-color: lightpink;" : ""));
        summaTextField.add(AttributeModifier.replace("style", !nummCheck || summa.compareTo(subsidySum.getSmSum()) != 0
                ? "background-color: lightpink;" : ""));
        subsTextField.add(AttributeModifier.replace("style", !nummCheck || subs.compareTo(subsidySum.getSbSum()) != 0
                ? "background-color: lightpink;" : ""));
        nummTextField.add(AttributeModifier.replace("style", !nummCheck
                ? "background-color: lightpink;" : ""));


        //save
        boolean enabled = subsidyModel.getObject().getUserOrganizationId() == null
                || subsidyService.validate(subsidyModel.getObject());
        link.add(AttributeModifier.replace("style", "opacity:" + (enabled ? "1" : "0.5")));
        link.setEnabled(enabled);


        if (target != null) {
            target.add(nmPayTextField);
            target.add(summaTextField);
            target.add(subsTextField);
            target.add(nummTextField);
            target.add(link);
        }
    }
}
