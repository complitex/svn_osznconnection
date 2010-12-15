package org.complitex.osznconnection.file.web;

import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.complitex.template.web.security.SecurityRole;
import org.complitex.template.web.template.FormTemplatePage;
import org.complitex.osznconnection.file.entity.Config;
import org.complitex.osznconnection.file.service.ConfigBean;

import javax.ejb.EJB;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 04.10.2010 13:21:37
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public class ConfigEdit extends FormTemplatePage{
    @EJB(name = "ConfigEdit")
    private ConfigBean configBean;

    public ConfigEdit() {
        super();

        add(new Label("title", getString("title")));
        add(new FeedbackPanel("messages"));

        Form form = new Form("form");
        add(form);

        final Map<Config, IModel<String>> model = new HashMap<Config, IModel<String>>();
        for (Config config : Config.values()){
            model.put(config, new Model<String>(configBean.getString(config, true)));
        }

        ListView<Config> listView = new ListView<Config>("listView", Arrays.asList(Config.values())){

            @Override
            protected void populateItem(ListItem<Config> item) {
                Config config = item.getModelObject();

                item.add(new Label("label", getStringOrKey(config)));
                item.add(new TextField<String>("config", model.get(config)));
            }
        };
        listView.setReuseItems(true);
        form.add(listView);

        Button save = new Button("save"){
            @Override
            public void onSubmit() {
                for (Config configName : Config.values()){
                    String value = model.get(configName).getObject();

                    if (!configBean.getString(configName, true).equals(value)){
                        configBean.update(configName, value);
                    }
                }
                info(getString("saved"));
            }
        };
        form.add(save);
    }
}
