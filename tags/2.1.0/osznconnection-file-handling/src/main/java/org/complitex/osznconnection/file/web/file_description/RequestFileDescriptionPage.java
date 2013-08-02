/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.file_description;

import java.util.List;
import javax.ejb.EJB;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.string.Strings;
import org.complitex.osznconnection.file.service.file_description.RequestFileDescription;
import org.complitex.osznconnection.file.service.file_description.RequestFileDescriptionBean;
import org.complitex.osznconnection.file.service.file_description.RequestFileDescriptionValidateException;
import org.complitex.osznconnection.file.service.file_description.RequestFileDescriptionValidateException.ValidationError;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.TemplatePage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.ADMIN_MODULE_EDIT)
public final class RequestFileDescriptionPage extends TemplatePage {

    private static final Logger log = LoggerFactory.getLogger(RequestFileDescriptionPage.class);
    @EJB
    private RequestFileDescriptionBean fileDescriptionBean;

    public RequestFileDescriptionPage() {
        add(new Label("title", new ResourceModel("title")));

        final WebMarkupContainer container = new WebMarkupContainer("container");
        container.setOutputMarkupId(true);
        add(container);

        final FeedbackPanel messages = new FeedbackPanel("messages");
        container.add(messages);

        final FileUploadField chooseFile = new FileUploadField("chooseFile");
        chooseFile.setRequired(true);

        Form<Void> form = new Form<Void>("form");
        container.add(form);

        form.add(chooseFile);
        form.add(new IndicatingAjaxButton("loadFile", form) {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                final FileUpload fileUpload = chooseFile.getFileUpload();

                final String fileName = fileUpload.getClientFileName();
                if (Strings.isEmpty(fileName) || !fileName.endsWith(".xml")) {
                    error(getStringFormat("file_not_xml", fileName));
                    return;
                }

                List<RequestFileDescription> fileDescriptions = null;
                try {
                    fileDescriptions = fileDescriptionBean.getDescription(fileUpload.getInputStream(), getLocale());
                } catch (RequestFileDescriptionValidateException e) {
                    for (ValidationError error : e.getErrors()) {
                        error(error.getErrorMessage());
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                if (fileDescriptions != null) {
                    try {
                        fileDescriptionBean.update(fileDescriptions);
                        info(getString("descriptions_saved"));
                    } catch (Exception e) {
                        log.error("", e);
                        error(getString("db_error"));
                    }
                }
                target.add(container);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.add(container);
            }
        });
    }
}
