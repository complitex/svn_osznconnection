/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.component.correction.edit;

import java.util.Collections;
import java.util.List;
import javax.ejb.EJB;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AbstractAutoCompleteTextRenderer;
import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteSettings;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.service.EntityBean;
import org.complitex.dictionary.service.StringCultureBean;
import org.complitex.dictionary.web.component.AbstractAutoCompleteTextField;
import org.complitex.osznconnection.file.entity.Correction;
import org.complitex.osznconnection.file.entity.StreetCorrection;
import org.complitex.osznconnection.file.entity.example.CorrectionExample;
import org.complitex.osznconnection.file.service.AddressCorrectionBean;
import org.complitex.osznconnection.file.web.pages.util.AddressRenderer;

/**
 *
 * @author Artem
 */
public class AddressCorrectionInputPanel extends Panel {

    @EJB(name = "AddressCorrectionBean")
    private AddressCorrectionBean addressCorrectionBean;
    @EJB(name = "StringCultureBean")
    private StringCultureBean stringBean;
    @EJB(name = "EntityBean")
    private EntityBean entityBean;

    private static class CorrectionRenderer<T extends Correction> extends AbstractAutoCompleteTextRenderer<T> {

        @Override
        protected String getTextValue(T correction) {
            return correction.getCorrection();
        }
    }

    private static abstract class AutoCompleteCorrectionTextField<T extends Correction> extends AbstractAutoCompleteTextField<T> {

        private CorrectionRenderer<T> renderer;

        public AutoCompleteCorrectionTextField(String id, CorrectionModel model, CorrectionRenderer<T> renderer, AutoCompleteSettings settings) {
            super(id, null, String.class, renderer, settings);
            model.setAutoComplete(this);
            this.setModel(model);
            this.renderer = renderer;
        }

        @Override
        protected String getChoiceValue(T choice) throws Throwable {
            return renderer.getTextValue(choice);
        }

        public CorrectionRenderer<T> getRenderer() {
            return renderer;
        }
    }

    private static class CorrectionModel<T extends Correction> extends Model<String> {

        private AutoCompleteCorrectionTextField<T> autoComplete;
        private IModel<T> model;

        public CorrectionModel(IModel<T> model) {
            this.model = model;
        }

        @Override
        public String getObject() {
            T object = model.getObject();
            if (object != null) {
                return autoComplete.getRenderer().getTextValue(object);
            }
            return null;
        }

        @Override
        public void setObject(String object) {
            model.setObject(autoComplete.findChoice());
        }

        private void setAutoComplete(AutoCompleteCorrectionTextField autoComplete) {
            this.autoComplete = autoComplete;
        }
    }
    private static final int AUTO_COMPLETE_SIZE = 10;
    private static final AutoCompleteSettings settings = new AutoCompleteSettings();

    public AddressCorrectionInputPanel(String id, final Correction correction) {
        super(id);

        final boolean isDistrict = "district".equals(correction.getEntity());
        final boolean isStreet = "street".equals(correction.getEntity());
        final boolean isBuilding = "building".equals(correction.getEntity());

        add(new Label("cityLabel", new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                return stringBean.displayValue(entityBean.getEntity("city").getEntityNames(), getLocale());
            }
        }));
        WebMarkupContainer districtLabelContainer = new WebMarkupContainer("districtLabelContainer");
        districtLabelContainer.setVisible(isDistrict);
        add(districtLabelContainer);
        districtLabelContainer.add(new Label("districtLabel", new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                return stringBean.displayValue(entityBean.getEntity("district").getEntityNames(), getLocale());
            }
        }));
        WebMarkupContainer streetLabelContainer = new WebMarkupContainer("streetLabelContainer");
        streetLabelContainer.setVisible(isStreet || isBuilding);
        add(streetLabelContainer);
        streetLabelContainer.add(new Label("streetLabel", new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                return stringBean.displayValue(entityBean.getEntity("street").getEntityNames(), getLocale());
            }
        }));

        WebMarkupContainer buildingLabelContainer = new WebMarkupContainer("buildingLabelContainer");
        buildingLabelContainer.setVisible(isBuilding);
        add(buildingLabelContainer);
        buildingLabelContainer.add(new Label("buildingLabel", new AbstractReadOnlyModel<String>() {

            @Override
            public String getObject() {
                return stringBean.displayValue(entityBean.getEntity("building").getEntityNames(), getLocale());
            }
        }));

        final IModel<Correction> cityModel = new Model<Correction>();
        AutoCompleteCorrectionTextField<Correction> city = new AutoCompleteCorrectionTextField<Correction>("city",
                new CorrectionModel<Correction>(cityModel), new CorrectionRenderer<Correction>(), settings) {

            @Override
            protected List<Correction> getChoiceList(String cityInput) {
                if (correction.getOrganizationId() != null) {
                    CorrectionExample example = createExample(cityInput, correction.getOrganizationId(), null);
                    List<Correction> cityCorrections = addressCorrectionBean.findCityCorrections(example);
                    return cityCorrections;
                }
                return Collections.emptyList();
            }
        };
        city.add(new AjaxFormComponentUpdatingBehavior("onblur") {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
            }
        });
        add(city);

        WebMarkupContainer districtContainer = new WebMarkupContainer("districtContainer");
        districtContainer.setVisible(isDistrict);
        add(districtContainer);
        districtContainer.add(new TextField("district", new PropertyModel<String>(correction, "correction") {

            @Override
            public void setObject(String object) {
                super.setObject(object);
                correction.setParentId(cityModel.getObject().getId());
            }
        }));

        WebMarkupContainer streetContainer = new WebMarkupContainer("streetContainer");
        streetContainer.setVisible(isStreet || isBuilding);
        add(streetContainer);

        IModel<Correction> streetTypeModel = new PropertyModel<Correction>(correction, "streetTypeCorrection");
        IModel<List<Correction>> allStreetTypeCorrectionsModel = new AbstractReadOnlyModel<List<Correction>>() {

            @Override
            public List<Correction> getObject() {
                if (correction.getOrganizationId() != null) {
                    return addressCorrectionBean.findStreetTypeCorrections(correction.getOrganizationId());
                } else {
                    return Collections.emptyList();
                }
            }
        };
        final DropDownChoice<Correction> streetType = new DropDownChoice<Correction>("streetType", streetTypeModel, allStreetTypeCorrectionsModel,
                new ChoiceRenderer<Correction>("correction", "id"));
        streetType.setOutputMarkupId(true);
        streetType.setVisible(isStreet);

        final IModel<StreetCorrection> streetModel = new Model<StreetCorrection>();
        TextField street = null;
        if (isBuilding) {
            CorrectionRenderer<StreetCorrection> streetCorrectionRenderer = new CorrectionRenderer<StreetCorrection>() {

                @Override
                protected String getTextValue(StreetCorrection streetCorrection) {
                    String streetType = null;
                    if (streetCorrection.getStreetTypeCorrection() != null) {
                        streetType = streetCorrection.getStreetTypeCorrection().getCorrection();
                    }
                    if (Strings.isEmpty(streetType)) {
                        streetType = null;
                    }

                    return AddressRenderer.displayStreet(streetType, streetCorrection.getCorrection(), getLocale());
                }
            };
            street = new AutoCompleteCorrectionTextField<StreetCorrection>("street", new CorrectionModel<StreetCorrection>(streetModel),
                    streetCorrectionRenderer, settings) {

                @Override
                protected List<StreetCorrection> getChoiceList(String streetInput) {
                    Correction cityCorrection = cityModel.getObject();
                    if (cityCorrection != null && correction.getOrganizationId() != null) {
                        CorrectionExample example = createExample(streetInput, correction.getOrganizationId(), cityCorrection.getId());
                        List<StreetCorrection> streetCorrections = addressCorrectionBean.findStreetCorrections(example);
                        return streetCorrections;
                    }
                    return Collections.emptyList();
                }
            };
            street.add(new AjaxFormComponentUpdatingBehavior("onblur") {

                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                }
            });
        } else {
            street = new TextField<String>("street", new PropertyModel<String>(correction, "correction") {

                @Override
                public void setObject(String object) {
                    super.setObject(object);
                    correction.setParentId(cityModel.getObject().getId());
                }
            });
        }
        streetContainer.add(streetType);
        streetContainer.add(street);

        WebMarkupContainer buildingContainer = new WebMarkupContainer("buildingContainer");
        buildingContainer.setVisible(isBuilding);
        add(buildingContainer);
        TextField<String> building = new TextField<String>("building", new PropertyModel<String>(correction, "correction") {

            @Override
            public void setObject(String object) {
                super.setObject(object);
                correction.setParentId(streetModel.getObject().getId());
            }
        });
        buildingContainer.add(building);
        buildingContainer.add(new TextField<String>("buildingCorp", new PropertyModel<String>(correction, "correctionCorp")));
    }

    private CorrectionExample createExample(String correction, long organizationId, Long parentId) {
        CorrectionExample example = new CorrectionExample();
        example.setCorrection(correction);
        example.setOrganizationId(organizationId);
        example.setParentId(parentId);
        example.setSize(AUTO_COMPLETE_SIZE);
        return example;
    }
}
