/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.pages.component;

import java.util.Date;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.string.Strings;
import org.complitex.osznconnection.file.entity.AccountDetail;
import org.complitex.osznconnection.file.service.PersonAccountService;
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

    @EJB(name = "PersonAccountService")
    private PersonAccountService personAccountService;
    @EJB(name = "StatusRenderService")
    private StatusRenderService statusRenderService;
    private T request;
    private Dialog dialog;
    private IModel<String> accountNumberModel;
    private AccountNumberPickerPanel accountNumberCorrectionPanel;
    private List<AccountDetail> accountCorrectionDetails;
    private WebMarkupContainer infoContainer;

    public AccountNumberCorrectionPanel(String id, final Component... toUpdate) {
        super(id);
        accountNumberModel = new Model<String>();

        dialog = new Dialog("dialog");
        dialog.setModal(true);
        dialog.setWidth(450);
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

        accountNumberCorrectionPanel = new AccountNumberPickerPanel("accountNumberCorrectionPanel",
                Model.ofList(accountCorrectionDetails)) {

            @Override
            protected void updateAccountNumber(AccountDetail accountDetail, AjaxRequestTarget target) {
                accountNumberModel.setObject(accountDetail.getAccountNumber());
            }

            @Override
            public boolean isVisible() {
                return getAccountCorrectionDetails() != null && !getAccountCorrectionDetails().isEmpty();
            }
        };

        infoContainer.add(accountNumberCorrectionPanel);

        AjaxLink save = new AjaxLink("save") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                if (validate()) {
                    correctAccountNumber(request, accountNumberModel.getObject());

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
                return getAccountCorrectionDetails() != null && !getAccountCorrectionDetails().isEmpty();
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

    private List<AccountDetail> getAccountCorrectionDetails() {
        return accountCorrectionDetails;
    }

    private boolean validate() {
        boolean validated = !Strings.isEmpty(accountNumberModel.getObject());
        if (!validated) {
            error(getString("account_number_required"));
        }
        return validated;
    }

    protected abstract void correctAccountNumber(T request, String accountNumber);

    public void open(AjaxRequestTarget target, T request, String district, String streetType, String street, String buildingNumber,
            String buildingCorp, String apartment, Date date) {

        this.request = request;
        try {
            accountCorrectionDetails = personAccountService.acquireAccountCorrectionDetails(district, streetType, street, buildingNumber,
                    buildingCorp, apartment, request, date);
            if (accountCorrectionDetails == null || accountCorrectionDetails.isEmpty()) {
                error(statusRenderService.displayStatus(request.getStatus(), getLocale()));
            }
        } catch (DBException e) {
            error(getString("db_error"));
        }

        accountNumberCorrectionPanel.getAccountDetailsModel().setObject(accountCorrectionDetails);
        accountNumberCorrectionPanel.clear();
        accountNumberModel.setObject(null);
        target.addComponent(infoContainer);
        dialog.open(target);
    }
}

