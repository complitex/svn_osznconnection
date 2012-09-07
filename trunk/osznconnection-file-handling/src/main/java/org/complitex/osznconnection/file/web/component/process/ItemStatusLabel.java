/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.component.process;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.LoadableDetachableModel;
import org.complitex.dictionary.entity.IExecutorObject;
import org.complitex.dictionary.util.StringUtil;
import org.complitex.osznconnection.file.entity.RequestFileStatus;

/**
 *
 * @author Artem
 */
public abstract class ItemStatusLabel<M extends IExecutorObject> extends Label {

    private final ProcessingManager<M> processingManager;
    private final TimerManager timerManager;

    public ItemStatusLabel(String id, ProcessingManager<M> processingManager, TimerManager timerManager) {
        super(id);
        this.processingManager = processingManager;
        this.timerManager = timerManager;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        final Item<M> item = findParent(Item.class);

        setDefaultModel(new LoadableDetachableModel<String>() {

            @Override
            protected String load() {
                String dots = "";
                if (processingManager.isProcessing(item.getModelObject()) && processingManager.isGlobalProcessing()) {
                    dots += StringUtil.getDots(timerManager.getTimerIndex() % 5);
                }

                final RequestFileStatus status = getStatus(item.getModelObject());
                return (status != null ? RequestFileStatusRenderer.render(status, getLocale()) : "") + dots;
            }
        });
    }

    protected abstract RequestFileStatus getStatus(M object);
}
