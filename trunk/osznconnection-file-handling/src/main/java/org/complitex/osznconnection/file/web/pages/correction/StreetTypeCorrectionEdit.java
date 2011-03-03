/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.correction;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Locale;
import javax.ejb.EJB;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.entity.example.DomainObjectExample;
import org.complitex.dictionary.web.component.DisableAwareDropDownChoice;
import org.complitex.dictionary.web.component.DomainObjectDisableAwareRenderer;
import org.complitex.template.web.component.toolbar.DeleteItemButton;
import org.complitex.template.web.component.toolbar.ToolbarButton;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.FormTemplatePage;
import org.complitex.osznconnection.file.entity.Correction;
import org.complitex.osznconnection.file.web.component.correction.edit.AbstractCorrectionEditPanel;
import org.complitex.address.strategy.street_type.StreetTypeStrategy;

/**
 *
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class StreetTypeCorrectionEdit extends FormTemplatePage {

    public static final String CORRECTION_ID = "correction_id";

    private class StreetTypeChoicePanel extends Panel {

        @EJB(name = "Street_typeStrategy")
        private StreetTypeStrategy streetTypeStrategy;

        public StreetTypeChoicePanel(String id, final Correction streetTypeCorrection) {
            super(id);

            final List<? extends DomainObject> allStreetTypes = getEntityTypes();
            IModel<DomainObject> entityTypeModel = new Model<DomainObject>() {

                @Override
                public DomainObject getObject() {
                    final Long streetTypeId = streetTypeCorrection.getObjectId();
                    if (streetTypeId != null) {
                        return Iterables.find(allStreetTypes, new Predicate<DomainObject>() {

                            @Override
                            public boolean apply(DomainObject streetTypeObject) {
                                return streetTypeObject.getId().equals(streetTypeId);
                            }
                        });
                    }
                    return null;
                }

                @Override
                public void setObject(DomainObject object) {
                    streetTypeCorrection.setObjectId(object.getId());
                }
            };
            DomainObjectDisableAwareRenderer renderer = new DomainObjectDisableAwareRenderer() {

                @Override
                public Object getDisplayValue(DomainObject object) {
                    return streetTypeStrategy.displayDomainObject(object, getLocale());
                }
            };
            DisableAwareDropDownChoice<DomainObject> streetType = new DisableAwareDropDownChoice<DomainObject>("streetType",
                    entityTypeModel, allStreetTypes, renderer);
            streetType.setRequired(true);
            add(streetType);
        }

        private List<? extends DomainObject> getEntityTypes() {
            DomainObjectExample example = new DomainObjectExample();
            streetTypeStrategy.configureExample(example, ImmutableMap.<String, Long>of(), null);
            return streetTypeStrategy.find(example);
        }
    }
    private AbstractCorrectionEditPanel correctionEditPanel;

    public StreetTypeCorrectionEdit(PageParameters params) {
        Long correctionId = params.getAsLong(CORRECTION_ID);
        add(correctionEditPanel = new AbstractCorrectionEditPanel("correctionEditPanel", "street_type", correctionId) {

            @Override
            protected IModel<String> internalObjectLabel(Locale locale) {
                return new ResourceModel("street_type");
            }

            @Override
            protected Panel internalObjectPanel(String id) {
                return new StreetTypeChoicePanel(id, getModel());
            }

            @Override
            protected String getNullObjectErrorMessage() {
                return getString("street_type_required");
            }

            @Override
            protected Class<? extends Page> getBackPageClass() {
                return StreetTypeCorrectionList.class;
            }

            @Override
            protected PageParameters getBackPageParameters() {
                return PageParameters.NULL;
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
