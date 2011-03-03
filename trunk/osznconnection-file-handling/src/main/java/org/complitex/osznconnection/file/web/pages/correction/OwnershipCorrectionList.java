/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.correction;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

/**
 * Список коррекций форм власти.
 * @author Artem
 */
public class OwnershipCorrectionList extends AbstractCorrectionList {

    public OwnershipCorrectionList() {
        super("ownership");
    }

    @Override
    protected Class<? extends WebPage> getEditPage() {
        return OwnershipCorrectionEdit.class;
    }

    @Override
    protected IModel<String> getTitleModel() {
        return new StringResourceModel("title", this, null);
    }

    @Override
    protected PageParameters getEditPageParams(Long objectCorrectionId) {
        PageParameters parameters = new PageParameters();
        if (objectCorrectionId != null) {
            parameters.put(OwnershipCorrectionEdit.CORRECTION_ID, objectCorrectionId);
        }
        return parameters;
    }
}
