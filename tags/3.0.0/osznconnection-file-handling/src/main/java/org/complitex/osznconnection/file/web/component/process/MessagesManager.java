package org.complitex.osznconnection.file.web.component.process;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.complitex.dictionary.entity.IExecutorObject;
import org.complitex.dictionary.util.EjbBeanLocator;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestFileStatus;
import org.complitex.osznconnection.file.service.process.ProcessManagerBean;
import org.complitex.osznconnection.file.service.process.ProcessType;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public abstract class MessagesManager implements Serializable {

    private final Map<ProcessType, Boolean> completedDisplayed = new EnumMap<ProcessType, Boolean>(ProcessType.class);
    private final Component component;

    private ProcessManagerBean processManagerBean() {
        return EjbBeanLocator.getBean(ProcessManagerBean.class);
    }

    public MessagesManager(Component component) {
        this.component = component;
    }

    protected void addMessages(String keyPrefix, AjaxRequestTarget target, ProcessType processType,
            RequestFileStatus processedStatus, RequestFileStatus errorStatus) {
        ProcessManagerBean processManagerBean = processManagerBean();
        List<IExecutorObject> list = processManagerBean.getProcessed(processType, getClass());

        for (IExecutorObject object : list) {
            boolean highlight = object instanceof RequestFile && object.getId() != null;


            if (RequestFileStatus.SKIPPED.equals(object.getStatus())) {
                if (highlight) {
                    HighlightManager.highlightProcessed(target, object.getId());
                }
                component.info(getString(keyPrefix + ".skipped", object.getObjectName()));
            } else if (processedStatus.equals(object.getStatus())) {
                if (highlight) {
                    HighlightManager.highlightProcessed(target, object.getId());
                }

                component.info(getString(keyPrefix + ".processed", object.getObjectName()));
            } else if (errorStatus.equals(object.getStatus())) {
                if (highlight) {
                    HighlightManager.highlightError(target, object.getId());
                }


                String message = object.getErrorMessage() != null ? ": " + object.getErrorMessage() : "";
                component.error(getString(keyPrefix + ".error", object.getObjectName()) + message);
            }
        }
    }

    private String getString(String key, Object... parameters) {
        return MessageFormat.format(component.getString(key), parameters);
    }

    public abstract void showMessages(AjaxRequestTarget target);

    public void showMessages() {
        showMessages(null);
    }

    public void resetCompletedStatus(ProcessType processType) {
        completedDisplayed.put(processType, false);
    }

    protected void addCompetedMessages(String keyPrefix, ProcessType processType) {
        if (completedDisplayed.get(processType) == null || !completedDisplayed.get(processType)) {
            ProcessManagerBean processManagerBean = processManagerBean();

            //Process completed
            if (processManagerBean.isCompleted(processType)) {
                component.info(getString(keyPrefix + ".completed",
                        processManagerBean.getSuccessCount(processType),
                        processManagerBean.getSkippedCount(processType),
                        processManagerBean.getErrorCount(processType)));
                completedDisplayed.put(processType, true);
            }

            //Process canceled
            if (processManagerBean.isCanceled(processType)) {
                component.info(getString(keyPrefix + ".canceled",
                        processManagerBean.getSuccessCount(processType),
                        processManagerBean.getSkippedCount(processType),
                        processManagerBean.getErrorCount(processType)));
                completedDisplayed.put(processType, true);
            }

            //Process error
            if (processManagerBean.isCriticalError(processType)) {
                component.error(getString(keyPrefix + ".critical_error",
                        processManagerBean.getSuccessCount(processType),
                        processManagerBean.getSkippedCount(processType),
                        processManagerBean.getErrorCount(processType)));
                completedDisplayed.put(processType, true);
            }
        }
    }
}
