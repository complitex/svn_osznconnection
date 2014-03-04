/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.component.process;

import com.google.common.collect.ImmutableList;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import javax.ejb.EJB;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.complitex.dictionary.entity.Log;
import org.complitex.dictionary.service.LogBean;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.service.RequestFileBean;

/**
 *
 * @author Artem
 */
public abstract class RequestFileDeleteButton extends DeleteButton {

    @EJB
    private RequestFileBean requestFileBean;
    @EJB
    private LogBean logBean;
    private final SelectManager selectManager;
    private final Collection<Component> updateComponents;

    public RequestFileDeleteButton(String id, SelectManager selectManager) {
        this(id, selectManager, new Component[0]);
    }

    public RequestFileDeleteButton(String id, SelectManager selectManager, Component... updateComponents) {
        super(id);
        this.selectManager = selectManager;
        this.updateComponents = new ArrayList<>(ImmutableList.copyOf(updateComponents));
    }

    public RequestFileDeleteButton addUpdateComponent(Component component) {
        this.updateComponents.add(component);
        return this;
    }

    @Override
    public void onClick(AjaxRequestTarget target) {
        for (long requestFileId : selectManager.getSelectedFileIds()) {
            RequestFile requestFile = requestFileBean.findById(requestFileId);
            try {
                requestFileBean.delete(requestFile);

                selectManager.remove(requestFileId);

                logSuccess(requestFile);
                info(MessageFormat.format(getString("info.deleted"), requestFile.getFullName()));
                logBean.info(Module.NAME, getLoggerControllerClass(), RequestFile.class,
                        null, requestFile.getId(), Log.EVENT.REMOVE, requestFile.getLogChangeList(),
                        getString("info.requestFileDeleted"), requestFile.getLogObjectName());
            } catch (Exception e) {
                logError(requestFile, e);
                error(MessageFormat.format("error.delete", requestFile.getFullName()));
                logBean.error(Module.NAME, getLoggerControllerClass(), RequestFile.class,
                        null, requestFile.getId(), Log.EVENT.REMOVE, requestFile.getLogChangeList(),
                        getString("error.requestFileDeleted"), requestFile.getLogObjectName());
                break;
            }
        }

        if (!updateComponents.isEmpty()) {
            for (Component c : updateComponents) {
                target.add(c);
            }
        }
    }

    protected abstract Class<?> getLoggerControllerClass();

    protected void logSuccess(RequestFile requestFile) {
    }

    protected void logError(RequestFile requestFile, Exception e) {
    }
}
