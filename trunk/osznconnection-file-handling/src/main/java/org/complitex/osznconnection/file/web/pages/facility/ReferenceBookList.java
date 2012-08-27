/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.facility;

import java.util.Arrays;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.EnumChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.FormTemplatePage;

/**
 *
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public final class ReferenceBookList extends FormTemplatePage {

    public ReferenceBookList() {
        add(new Label("title", new ResourceModel("title")));

        final FeedbackPanel messages = new FeedbackPanel("messages");
        messages.setOutputMarkupId(true);
        add(messages);

        Form<Void> form = new Form<Void>("form");
        add(form);

        final IModel<FacilityReferenceBookType> model = new Model<>();
        RadioChoice<FacilityReferenceBookType> referenceBookPicker = new RadioChoice<>("referenceBookPicker", model,
                Arrays.asList(FacilityReferenceBookType.values()),
                new EnumChoiceRenderer<FacilityReferenceBookType>(this));
        referenceBookPicker.setRequired(true);
        form.add(referenceBookPicker);

        form.add(new IndicatingAjaxButton("submit", form) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                FacilityReferenceBookType referenceBookType = model.getObject();
                switch (referenceBookType) {
                    case STREET_TYPE:
                        setResponsePage(FacilityStreetTypeFileList.class);
                        return;
                    case STREET:
                        setResponsePage(FacilityStreetFileList.class);
                        return;
                    case TARIF:
                        setResponsePage(FacilityTarifFileList.class);
                        return;
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.add(messages);
            }
        });
    }
}
