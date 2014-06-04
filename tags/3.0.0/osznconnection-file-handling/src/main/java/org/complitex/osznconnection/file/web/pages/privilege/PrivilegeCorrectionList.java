package org.complitex.osznconnection.file.web.pages.privilege;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.complitex.correction.web.AbstractCorrectionList;
import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.osznconnection.file.entity.PrivilegeCorrection;
import org.complitex.osznconnection.file.service.PrivilegeCorrectionBean;

import javax.ejb.EJB;
import java.util.List;

/**
 * Список коррекций привилегий.
 * @author Artem
 */
public class PrivilegeCorrectionList extends AbstractCorrectionList<PrivilegeCorrection> {
    @EJB
    private PrivilegeCorrectionBean privilegeCorrectionBean;


    public PrivilegeCorrectionList() {
        super("privilege");
    }

    @Override
    protected PrivilegeCorrection newCorrection() {
        return new PrivilegeCorrection();
    }

    @Override
    protected List<PrivilegeCorrection> getCorrections(FilterWrapper<PrivilegeCorrection> filterWrapper) {
        return privilegeCorrectionBean.getPrivilegeCorrections(filterWrapper);
    }

    @Override
    protected Integer getCorrectionsCount(FilterWrapper<PrivilegeCorrection> filterWrapper) {
        return privilegeCorrectionBean.getPrivilegeCorrectionCount(filterWrapper);
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
