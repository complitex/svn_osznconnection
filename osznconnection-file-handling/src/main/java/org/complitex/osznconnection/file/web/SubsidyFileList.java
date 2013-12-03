package org.complitex.osznconnection.file.web;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.complitex.osznconnection.file.web.component.SubsidyFileListPanel;
import org.complitex.template.web.component.toolbar.ToolbarButton;
import org.complitex.template.web.pages.ScrollListPage;
import org.complitex.template.web.security.SecurityRole;

import java.util.List;

/**
 *
 * @author Artem
 */
@AuthorizeInstantiation(SecurityRole.AUTHORIZED)
public final class SubsidyFileList extends ScrollListPage {

    private final AbstractFileListPanel fileListPanel;

    public SubsidyFileList(PageParameters parameters) {
        super(parameters);

        add(new Label("title", new ResourceModel("title")));
        add(fileListPanel = new SubsidyFileListPanel("fileListPanel"));
    }

    @Override
    protected List<? extends ToolbarButton> getToolbarButtons(String id) {
        return fileListPanel.getToolbarButtons(id);
    }
}
