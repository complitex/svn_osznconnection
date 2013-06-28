package org.complitex.osznconnection.file.web.pages.ownership;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.complitex.correction.web.AbstractCorrectionList;

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
            parameters.set(OwnershipCorrectionEdit.CORRECTION_ID, objectCorrectionId);
        }
        return parameters;
    }
}
