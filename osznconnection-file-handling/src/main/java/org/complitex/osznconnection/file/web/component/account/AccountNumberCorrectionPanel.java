/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.component.account;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.Strings;
import org.complitex.osznconnection.file.entity.AccountDetail;
import org.odlabs.wiquery.core.javascript.JsStatement;
import org.odlabs.wiquery.ui.core.JsScopeUiEvent;
import org.odlabs.wiquery.ui.dialog.Dialog;

import javax.ejb.EJB;
import java.util.List;
import org.apache.wicket.Component;
import org.complitex.osznconnection.file.calculation.adapter.exception.DBException;
import org.complitex.osznconnection.file.entity.AbstractRequest;
import org.complitex.osznconnection.file.service.StatusRenderService;

/**
 * Панель для корректировки номера л/c вручную, когда больше одного человека в ЦН, имеющие разные номера л/c, привязаны к одному адресу.
 * @author Artem
 */
public abstract class AccountNumberCorrectionPanel<T extends AbstractRequest> extends Panel {

    @EJB
    private StatusRenderService statusRenderService;
    private T request;
    private Dialog dialog;
    private final IModel<AccountDetail> accountDetailModel;
    private final AccountNumberPickerPanel accountNumberPickerPanel;
    private final IModel<List<? extends AccountDetail>> accountDetailsModel;
    private final WebMarkupContainer infoContainer;

    public AccountNumberCorrectionPanel(String id, final Component... toUpdate) {
        super(id);
        accountDetailModel = new Model<AccountDetail>();
        accountDetailsModel = Model.ofList(null);

        dialog = new Dialog("dialog");
        dialog.setModal(true);
        dialog.setWidth(650);
        dialog.setOpenEvent(JsScopeUiEvent.quickScope(new JsStatement().self().chain("parents", "'.ui-dialog:first'").
                chain("find", "'.ui-dialog-titlebar-close'").
                chain("hide").render()));
        dialog.setCloseOnEscape(false);
        add(dialog);

        infoContainer = new WebMarkupContainer("infoContainer");
        infoContainer.setOutputMarkupId(true);

        dialog.add(infoContainer);

        final FeedbackPanel messages = new FeedbackPanel("messages");
        messages.setOutputMarkupId(true);
        infoContainer.add(messages);

        accountNumberPickerPanel = new AccountNumberPickerPanel("accountNumberPickerPanel", accountDetailsModel,
                accountDetailModel) {

            @Override
            public boolean isVisible() {
                return accountDetailsModel.getObject() != null && !accountDetailsModel.getObject().isEmpty();
            }
        };

        infoContainer.add(accountNumberPickerPanel);

        AjaxLink save = new AjaxLink("save") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                if (validate()) {
                    correctAccountNumber(request, accountDetailModel.getObject().getAccountNumber());

                    if (toUpdate != null) {
                        for (Component component : toUpdate) {
                            target.addComponent(component);
                        }
                    }
                    dialog.close(target);
                } else {
                    target.addComponent(messages);
                }
            }

            @Override
            public boolean isVisible() {
                return accountDetailsModel.getObject() != null && !accountDetailsModel.getObject().isEmpty();
            }
        };
        infoContainer.add(save);

        AjaxLink cancel = new AjaxLink("cancel") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                dialog.close(target);
            }
        };
        infoContainer.add(cancel);
    }

    private boolean validate() {
        boolean validated = accountDetailModel.getObject() != null
                && !Strings.isEmpty(accountDetailModel.getObject().getAccountNumber());
        if (!validated) {
            error(getString("account_number_required"));
        }
        return validated;
    }

    protected abstract void correctAccountNumber(T request, String accountNumber);

    public void open(AjaxRequestTarget target, T request) {

        this.request = request;
        List<? extends AccountDetail> accountDetails = null;
        try {
            accountDetails = acquireAccountDetailsByAddress(request);
            if (accountDetails == null || accountDetails.isEmpty()) {
                error(statusRenderService.displayStatus(request.getStatus(), getLocale()));
            }
        } catch (DBException e) {
            error(getString("db_error"));
        }

        accountDetailsModel.setObject(accountDetails);
        accountDetailModel.setObject(null);
        target.addComponent(infoContainer);
        dialog.open(target);
    }

    protected abstract List<AccountDetail> acquireAccountDetailsByAddress(T request) throws DBException;
}

