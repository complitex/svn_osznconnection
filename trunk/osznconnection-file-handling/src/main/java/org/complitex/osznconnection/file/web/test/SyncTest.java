package org.complitex.osznconnection.file.web.test;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.complitex.dictionary.util.StringUtil;
import org.complitex.osznconnection.file.entity.CalculationContext;
import org.complitex.osznconnection.file.service_provider.ServiceProviderSync;
import org.complitex.osznconnection.file.service_provider.entity.BuildingSync;
import org.complitex.osznconnection.file.service_provider.entity.DistrictSync;
import org.complitex.osznconnection.file.service_provider.entity.StreetSync;
import org.complitex.osznconnection.file.service_provider.entity.StreetTypeSync;

import javax.ejb.EJB;
import java.util.Date;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 08.10.13 16:17
 */
public class SyncTest extends WebPage {
    @EJB
    private ServiceProviderSync serviceProviderSync;

    public SyncTest() {
        final MultiLineLabel districts = new MultiLineLabel("districts");
        add(districts);

        final MultiLineLabel streetTypes = new MultiLineLabel("streetTypes");
        add(streetTypes);

        final MultiLineLabel streets = new MultiLineLabel("streets");
        add(streets);

        final MultiLineLabel buildings = new MultiLineLabel("buildings");
        add(buildings);

        Form form = new Form("form");
        add(form);

        form.add(new Button("test"){
            @Override
            public void onSubmit() {
                CalculationContext calculationContext = new CalculationContext(null, null, null,
                        "jdbc/osznconnection_remote_resource", null);

                //districts
                List<DistrictSync> districtSyncs = serviceProviderSync.getDistrictSyncs(
                        calculationContext,
                        "Тверь", "г", new Date());
                if (districtSyncs != null) {
                    String t = "";

                    for (DistrictSync d : districtSyncs){
                        t += d.getExternalId() + " " + d.getName();
                    }

                    districts.setDefaultModel(new Model<>(t));
                }

                //street types
                List<StreetTypeSync> streetTypeSyncs = serviceProviderSync.getStreetTypeSyncs(calculationContext);
                if (streetTypeSyncs != null) {
                    String t = "";

                    for (StreetTypeSync s : streetTypeSyncs){
                        t += s.getExternalId() + " " +s.getShortName() + " " + s.getName() + "\n";
                    }

                    streetTypes.setDefaultModel(new Model<>(t));
                }

                //streets
                List<StreetSync> streetSyncs = serviceProviderSync.getStreetSyncs(calculationContext,
                        "Тверь", "г", new Date());
                if (streetSyncs != null){
                    String t = "";

                    for (StreetSync s : streetSyncs){
                        t += s.getExternalId() + " " + s.getStreetTypeShortName() + " " + s.getName() + "\n";
                    }

                    streets.setDefaultModel(new Model<>(t));
                }

                //buildings
                List<BuildingSync> buildingSyncs = serviceProviderSync.getBuildingSyncs(calculationContext,
                        "Центральный", "ул", "ФРАНТИШЕКА КРАЛА", new Date());
                if (streetSyncs != null){
                    String t = "";

                    for (BuildingSync s : buildingSyncs){
                        t +=  s.getStreetExternalId() + " " + s.getExternalId() + " " + s.getName() + " "
                                + StringUtil.emptyOnNull(s.getPart())+"\n";
                    }

                    buildings.setDefaultModel(new Model<>(t));
                }

            }
        });
    }
}
