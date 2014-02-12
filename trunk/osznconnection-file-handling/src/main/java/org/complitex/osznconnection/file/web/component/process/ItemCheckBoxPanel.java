/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.component.process;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.request.resource.SharedResourceReference;
import org.complitex.dictionary.entity.IExecutorObject;
import org.complitex.dictionary.web.component.css.CssAttributeBehavior;
import org.complitex.dictionary.web.component.image.StaticImage;

/**
 *
 * @author Artem
 */
public final class ItemCheckBoxPanel<M extends IExecutorObject> extends Panel {

    private final static String IMAGE_AJAX_LOADER = "images/ajax-loader2.gif";
    private final static String IMAGE_AJAX_WAITING = "images/ajax-waiting.gif";
    private final ProcessingManager processingManager;
    private final SelectManager selectManager;

    public ItemCheckBoxPanel(String id, ProcessingManager processingManager, SelectManager selectManager) {
        super(id);
        this.processingManager = processingManager;
        this.selectManager = selectManager;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        final Item<IExecutorObject> item = findParent(Item.class);

        //Выбор файлов
        CheckBox checkBox = new CheckBox("selected", selectManager.newSelectCheckboxModel(item.getModelObject().getId())) {

            @Override
            public boolean isVisible() {
                return !item.getModelObject().isProcessing()
                        && !processingManager.isGlobalWaiting(item.getModelObject());
            }

            @Override
            public boolean isEnabled() {
                return !processingManager.isGlobalWaiting(item.getModelObject());
            }
        };

        checkBox.add(new AjaxFormComponentUpdatingBehavior("onchange") {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
            }
        });
        checkBox.add(new CssAttributeBehavior("processable-list-panel-select"));
        add(checkBox);

        //Анимация в обработке
        add(new StaticImage("processing", new SharedResourceReference(IMAGE_AJAX_LOADER)) {

            @Override
            public boolean isVisible() {
                return item.getModelObject().isProcessing();
            }
        });

        //Анимация ожидание
        add(new StaticImage("waiting", new SharedResourceReference(IMAGE_AJAX_WAITING)) {

            @Override
            public boolean isVisible() {
                return processingManager.isGlobalWaiting(item.getModelObject())
                        && !item.getModelObject().isProcessing();
            }
        });
    }
}
