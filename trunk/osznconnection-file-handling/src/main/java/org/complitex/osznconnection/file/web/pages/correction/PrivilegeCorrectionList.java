/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.correction;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * Список коррекций привилегий.
 * @author Artem
 */
public class PrivilegeCorrectionList extends AbstractCorrectionList {

    public PrivilegeCorrectionList() {
        super("privilege");
    }

    @Override
    protected Class<? extends WebPage> getEditPage() {
        return PrivilegeCorrectionEdit.class;
    }

    @Override
    protected IModel<String> getTitleModel() {
        return new StringResourceModel("title", this, null);
    }

    @Override
    protected PageParameters getEditPageParams(Long objectCorrectionId) {
        PageParameters parameters = new PageParameters();
        if (objectCorrectionId != null) {
            parameters.set(PrivilegeCorrectionEdit.CORRECTION_ID, objectCorrectionId);
        }
        return parameters;
    }
}
