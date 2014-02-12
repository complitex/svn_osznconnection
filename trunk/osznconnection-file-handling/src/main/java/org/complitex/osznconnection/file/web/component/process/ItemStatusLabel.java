package org.complitex.osznconnection.file.web.component.process;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.LoadableDetachableModel;
import org.complitex.dictionary.entity.IExecutorObject;
import org.complitex.dictionary.util.StringUtil;
import org.complitex.osznconnection.file.entity.RequestFileStatus;

public class ItemStatusLabel extends Label {
    private final ProcessingManager processingManager;
    private final TimerManager timerManager;

    public ItemStatusLabel(String id, ProcessingManager processingManager, TimerManager timerManager) {
        super(id);
        this.processingManager = processingManager;
        this.timerManager = timerManager;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        final Item item = findParent(Item.class);

        setDefaultModel(new LoadableDetachableModel<String>() {

            @Override
            protected String load() {
                if (item.getModelObject() instanceof IExecutorObject) {
                    IExecutorObject object = (IExecutorObject) item.getModelObject();

                    String dots = "";
                    if (object.isProcessing() && processingManager.isGlobalProcessing()) {
                        dots += StringUtil.getDots(timerManager.getTimerIndex() % 5);
                    }

                    if (object.getStatus() instanceof RequestFileStatus) {
                        final RequestFileStatus status = (RequestFileStatus) object.getStatus();

                        return (status != null ? RequestFileStatusRenderer.render(status, getLocale()) : "") + dots;
                    }else {
                        return "";
                    }
                }

                return "";
            }
        });
    }
}
