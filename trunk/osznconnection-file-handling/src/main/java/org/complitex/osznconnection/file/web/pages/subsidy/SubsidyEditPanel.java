package org.complitex.osznconnection.file.web.pages.subsidy;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
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
import java.util.Arrays;
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
    private Subsidy subsidy = new Subsidy();
    private Form form;
    private ListView listView;

    private Map<String, LabelTextField> textFieldMap = new HashMap<>();

    public SubsidyEditPanel(String id, final Component toUpdate) {
        super(id);

        dialog = new Dialog("dialog");
        dialog.setModal(true);
        dialog.setWidth(600);
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

                LabelTextField textField = new LabelTextField<>("field", fileFieldDescription.getLength(),
                        new PropertyModel<>(subsidy, "convertedFields[" + fileFieldDescription.getName() + "]"));
                textField.setType(fileFieldDescription.getFieldType());
                textField.setOutputMarkupId(true);

                if (item.getIndex() > 11){
                    textField.add(new AjaxFormComponentUpdatingBehavior("onchange") {
                        @Override
                        protected void onUpdate(AjaxRequestTarget target) {
                            validate(target);
                        }
                    });
                }else {
                    textField.setEnabled(false);
                }

                textFieldMap.put(fileFieldDescription.getName(), textField);

                item.add(textField);


                String name = fileFieldDescription.getName();

                if (Arrays.asList("DAT1", "P1", "SM1", "SB1", "OB1", "SUMMA").contains(name)) {
                    item.add(AttributeModifier.replace("style", "float:left; clear:left"));
                }
            }

            @Override
            protected void onBeforeRender() {
                super.onBeforeRender();

                if (subsidy.getUserOrganizationId() != null) {
                    validate(null);
                }
            }
        }.setReuseItems(true));

        form.add(new AjaxSubmitLink("save") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                RequestFile requestFile = requestFileBean.findById(subsidy.getRequestFileId());

                if (!subsidyService.validate(subsidy)){
                    subsidy.setStatus(RequestStatus.SUBSIDY_NM_PAY_ERROR);

                    //save
                    subsidy.setUpdateFieldMap(subsidy.getDbfFields());
                    subsidyBean.update(subsidy);

                    if (!RequestFileStatus.LOAD_ERROR.equals(requestFile.getStatus())){
                        requestFile.setStatus(RequestFileStatus.LOAD_ERROR);

                        requestFileBean.save(requestFile);
                    }
                }else if (RequestStatus.SUBSIDY_NM_PAY_ERROR.equals(subsidy.getStatus())){
                    subsidy.setStatus(RequestStatus.LOADED);

                    //save
                    subsidy.setUpdateFieldMap(subsidy.getDbfFields());
                    subsidyBean.update(subsidy);

                    //update request file status
                    if (RequestFileStatus.LOAD_ERROR.equals(requestFile.getStatus())){
                        SubsidyExample loaded = new SubsidyExample();
                        loaded.setRequestFileId(subsidy.getRequestFileId());
                        loaded.setStatus(RequestStatus.LOADED);

                        SubsidyExample all = new SubsidyExample();
                        all.setRequestFileId(subsidy.getRequestFileId());

                        if (subsidyBean.count(loaded) == subsidyBean.count(all)){
                            requestFile.setStatus(RequestFileStatus.LOADED);

                            requestFileBean.save(requestFile);
                        }
                    }
                }

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
                target.add(toUpdate);
            }
        });

        form.add(new AjaxLink("recalculate") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                SubsidySum subsidySum = subsidyService.getSubsidySum(subsidy);

                subsidy.setField(SubsidyDBF.NM_PAY, subsidySum.getNSum());

                Long numm = subsidy.getField("NUMM");

                if (numm != 0){
                    subsidy.setField(SubsidyDBF.SUMMA, subsidySum.getSbSum());
                    subsidy.setField(SubsidyDBF.SUBS, ((BigDecimal)subsidy.getField(SubsidyDBF.SUMMA))
                            .divide(new BigDecimal(numm)));
                }else {
                    subsidy.setField(SubsidyDBF.SUMMA, subsidySum.getSmSum());
                }

                validate(target);
            }
        });
    }

    public void open(AjaxRequestTarget target, Subsidy subsidy){
        this.subsidy = subsidy;

        listView.removeAll();

        target.add(form);
        dialog.open(target);
    }

    private void validate(final AjaxRequestTarget target){
        SubsidySum subsidySum = subsidyService.getSubsidySum(subsidy);

        Long numm = subsidy.getField("NUMM");
        BigDecimal summa =  subsidy.getField("SUMMA");
        BigDecimal subs = subsidy.getField("SUBS");
        BigDecimal nmPay = subsidy.getField("NM_PAY");

        LabelTextField nmPayTextField = textFieldMap.get("NM_PAY");
        LabelTextField summaTextField = textFieldMap.get("SUMMA");
        LabelTextField subsTextField = textFieldMap.get("SUBS");

        nmPayTextField.add(new AttributeModifier("style", nmPay.compareTo(subsidySum.getNSum()) != 0
                ? "background-color: lightpink;" : ""));

        if (numm != 0){
            boolean check = summa.compareTo(subs.multiply(new BigDecimal(numm))) == 0
                    && summa.compareTo(subsidySum.getSbSum()) == 0;

            summaTextField.add(new AttributeModifier("style", !check ? "background-color: lightpink;" : ""));
            subsTextField.add(new AttributeModifier("style", !check ? "background-color: lightpink;" : ""));
        }else {
            summaTextField.add(new AttributeModifier("style", (summa.compareTo(subsidySum.getSmSum()) != 0)
                    ? "background-color: lightpink;" : ""));
            subsTextField.add(new AttributeModifier("style", ""));
        }

        if (target != null) {
            target.add(nmPayTextField);
            target.add(summaTextField);
            target.add(subsTextField);
        }
    }
}
