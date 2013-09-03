package org.complitex.osznconnection.file.web.pages.privilege;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.complitex.correction.web.component.AbstractCorrectionEditPanel;
import org.complitex.dictionary.entity.Correction;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.web.component.ShowMode;
import org.complitex.dictionary.web.component.search.ISearchCallback;
import org.complitex.dictionary.web.component.search.SearchComponentState;
import org.complitex.dictionary.web.component.search.WiQuerySearchComponent;
import org.complitex.osznconnection.file.entity.PrivilegeCorrection;
import org.complitex.osznconnection.file.service.PrivilegeCorrectionBean;
import org.complitex.osznconnection.file.strategy.PrivilegeStrategy;
import org.complitex.template.web.component.toolbar.DeleteItemButton;
import org.complitex.template.web.component.toolbar.ToolbarButton;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.FormTemplatePage;

import javax.ejb.EJB;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Страница для редактирования коррекций привилегий.
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public final class PrivilegeCorrectionEdit extends FormTemplatePage {
    public static final String CORRECTION_ID = "correction_id";

    @EJB
    private PrivilegeStrategy privilegeStrategy;

    @EJB
    private PrivilegeCorrectionBean privilegeCorrectionBean;

    private class PrivilegeCallback implements ISearchCallback, Serializable {

        private Correction correction;

        PrivilegeCallback(Correction correction) {
            this.correction = correction;
        }

        @Override
        public void found(Component component, Map<String, Long> ids, AjaxRequestTarget target) {
            Long id = ids.get(privilegeStrategy.getEntityTable());
            if (id != null && id > 0) {
                correction.setObjectId(id);
            }
        }
    }
    private AbstractCorrectionEditPanel correctionEditPanel;

    public PrivilegeCorrectionEdit(PageParameters params) {
        Long correctionId = params.get(CORRECTION_ID).toOptionalLong();

        add(correctionEditPanel = new AbstractCorrectionEditPanel<PrivilegeCorrection>("correctionEditPanel", correctionId) {

            @Override
            protected PrivilegeCorrection getCorrection(Long correctionId) {
                return privilegeCorrectionBean.getPrivilegeCorrection(correctionId);
            }

            @Override
            protected PrivilegeCorrection newCorrection() {
                return new PrivilegeCorrection();
            }

            @Override
            protected IModel<String> internalObjectLabel(Locale locale) {
                return new ResourceModel("privilege");
            }

            @Override
            protected Panel internalObjectPanel(String id) {
                SearchComponentState componentState = new SearchComponentState();
                Correction correction = getCorrection();
                if (!isNew()) {
                    componentState.put(privilegeStrategy.getEntityTable(), findPrivilege(correction.getObjectId()));
                }

                return new WiQuerySearchComponent(id, componentState, ImmutableList.of(privilegeStrategy.getEntityTable()),
                        new PrivilegeCallback(correction), ShowMode.ACTIVE, true);
            }

            @Override
            protected String getNullObjectErrorMessage() {
                return getString("privilege_required");
            }

            @Override
            protected boolean validateExistence() {
                return false;  //To change body of implemented methods use File | Settings | File Templates.
            }

            private DomainObject findPrivilege(long privilegeId) {
                return privilegeStrategy.findById(privilegeId, true);
            }

            @Override
            protected boolean isOrganizationCodeRequired() {
                return true;
            }

            @Override
            protected Class<? extends Page> getBackPageClass() {
                return PrivilegeCorrectionList.class;
            }

            @Override
            protected PageParameters getBackPageParameters() {
                return new PageParameters();
            }

            @Override
            protected void save() {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            protected void delete() {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            protected IModel<String> getTitleModel() {
                return new StringResourceModel("title", this, null);
            }
        });
    }

    @Override
    protected List<? extends ToolbarButton> getToolbarButtons(String id) {
        List<ToolbarButton> toolbar = Lists.newArrayList();
        toolbar.add(new DeleteItemButton(id) {

            @Override
            protected void onClick() {
                correctionEditPanel.executeDeletion();
            }

            @Override
            public boolean isVisible() {
                return !correctionEditPanel.isNew();
            }
        });
        return toolbar;
    }
}
