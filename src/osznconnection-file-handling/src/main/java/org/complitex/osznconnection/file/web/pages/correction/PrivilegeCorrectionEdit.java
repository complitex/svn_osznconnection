/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.correction;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.Locale;
import java.util.Map;
import javax.ejb.EJB;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.entity.example.DomainObjectExample;
import org.complitex.dictionaryfw.web.component.search.ISearchCallback;
import org.complitex.dictionaryfw.web.component.search.SearchComponent;
import org.complitex.dictionaryfw.web.component.search.SearchComponentState;
import org.complitex.osznconnection.commons.web.template.FormTemplatePage;
import org.complitex.osznconnection.file.entity.ObjectCorrection;
import org.complitex.osznconnection.file.web.component.correction.edit.AbstractCorrectionEditPanel;
import org.complitex.osznconnection.privilege.strategy.PrivilegeStrategy;

/**
 *
 * @author Artem
 */
public final class PrivilegeCorrectionEdit extends FormTemplatePage {

    public static final String CORRECTION_ID = "correction_id";

    @EJB(name = "PrivilegeStrategy")
    private PrivilegeStrategy privilegeStrategy;

    private class PrivilegeCallback implements ISearchCallback, Serializable {

        private ObjectCorrection correction;

        public PrivilegeCallback(ObjectCorrection correction) {
            this.correction = correction;
        }

        @Override
        public void found(SearchComponent component, Map<String, Long> ids, AjaxRequestTarget target) {
            Long id = ids.get(privilegeStrategy.getEntityTable());
            if (id != null && id > 0) {
                correction.setInternalObjectId(id);
            }
        }
    }

    public PrivilegeCorrectionEdit(PageParameters params) {
        Long correctionId = params.getAsLong(CORRECTION_ID);

        add(new AbstractCorrectionEditPanel("correctionEditPanel", privilegeStrategy.getEntityTable(), correctionId) {

            @Override
            protected IModel<String> internalObjectLabel(Locale locale) {
                return new ResourceModel("privilege");
            }

            @Override
            protected Panel internalObjectPanel(String id) {
                SearchComponentState componentState = new SearchComponentState();
                ObjectCorrection correction = getModel();
                if (!isNew()) {
                    componentState.put(privilegeStrategy.getEntityTable(), findPrivilege(correction.getInternalObjectId()));
                }

                return new SearchComponent(id, componentState, ImmutableList.of(privilegeStrategy.getEntityTable()),
                        new PrivilegeCallback(correction), true);
            }

            @Override
            protected boolean validate() {
                boolean valid = getModel().getInternalObjectId() != null;
                if (!valid) {
                    error(getString("privilege_required"));
                }
                return valid;
            }

            private DomainObject findPrivilege(long privilegeId) {
                DomainObjectExample example = new DomainObjectExample();
                example.setId(privilegeId);
                DomainObject object = privilegeStrategy.find(example).get(0);
                return object;
            }

            @Override
            protected void back() {
                PageParameters parameters = new PageParameters();
                parameters.put(PrivilegeCorrectionList.CORRECTED_ENTITY, getEntity());
                setResponsePage(PrivilegeCorrectionList.class, parameters);
            }
        });
    }
}

