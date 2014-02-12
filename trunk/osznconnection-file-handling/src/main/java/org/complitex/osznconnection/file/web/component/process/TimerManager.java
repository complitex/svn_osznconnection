/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.component.process;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.util.time.Duration;
import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Lists.*;

/**
 *
 * @author Artem
 */
public final class TimerManager implements Serializable {

    private final int timer;
    private final MessagesManager messagesManager;
    private final ProcessingManager processingManager;
    private final AtomicInteger timerIndex;
    private final Form<?> form;
    private final WebMarkupContainer dataViewContainer;
    private final Collection<Component> updateComponents;

    public TimerManager(int timer, MessagesManager messagesManager, ProcessingManager processingManager,
            Form<?> form, WebMarkupContainer dataViewContainer) {
        this.timer = timer;
        this.messagesManager = messagesManager;
        this.processingManager = processingManager;
        this.form = form;
        this.dataViewContainer = dataViewContainer;
        this.updateComponents = new ArrayList<>();
        this.timerIndex = new AtomicInteger();
    }

    public TimerManager addUpdateComponent(Component updateComponent) {
        updateComponents.add(updateComponent);
        return this;
    }

    public int getTimerIndex() {
        return timerIndex.get();
    }

    public void startTimer() {
        if (processingManager.isGlobalProcessing()) {
            dataViewContainer.add(newTimer());
        }
    }

    private AjaxSelfUpdatingTimerBehavior newTimer() {
        final AtomicInteger waitForStopTimer = new AtomicInteger();
        return new AjaxSelfUpdatingTimerBehavior(Duration.seconds(timer)) {

            @Override
            protected void onPostProcessTarget(AjaxRequestTarget target) {
                messagesManager.showMessages(target);

                if (!processingManager.isGlobalProcessing() && waitForStopTimer.incrementAndGet() > 2) {
                    this.stop();
                    target.add(form);
                } else {
                    if (!updateComponents.isEmpty()) {
                        for (Component c : updateComponents) {
                            target.add(c);
                        }
                    }
                }

                timerIndex.incrementAndGet();
            }
        };
    }

    public void addTimer() {
        boolean needCreateNewTimer = true;

        List<AjaxSelfUpdatingTimerBehavior> timers = newArrayList(filter(dataViewContainer.getBehaviors(),
                AjaxSelfUpdatingTimerBehavior.class));
        if (timers != null && !timers.isEmpty()) {
            for (AjaxSelfUpdatingTimerBehavior t : timers) {
                if (!t.isStopped()) {
                    needCreateNewTimer = false;
                    break;
                }
            }
        }
        if (needCreateNewTimer) {
            dataViewContainer.add(newTimer());
        }
    }
}
