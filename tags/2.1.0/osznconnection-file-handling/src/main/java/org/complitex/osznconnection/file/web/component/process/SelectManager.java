/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.component.process;

import com.google.common.base.Function;
import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Lists.*;
import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.complitex.dictionary.entity.IExecutorObject;

/**
 *
 * @author Artem
 */
public class SelectManager implements Serializable {

    private static class SelectModelValueWithId implements Serializable {

        SelectModelValue selectModelValue;
        long objectId;

        SelectModelValueWithId(long objectId, SelectModelValue selectModelValue) {
            this.selectModelValue = selectModelValue;
            this.objectId = objectId;
        }
    }

    private static class SelectModelValue implements Serializable {

        boolean selected;
        int sortId;

        void toggle() {
            selected = !selected;
        }

        void clearSelect() {
            selected = false;
        }
    }

    private static class SelectValueComparator implements Comparator<SelectModelValueWithId> {

        @Override
        public int compare(SelectModelValueWithId o1, SelectModelValueWithId o2) {
            return Integer.compare(o1.selectModelValue.sortId, o2.selectModelValue.sortId);
        }
    }
    private final Map<Long, SelectModelValue> selectModels;

    public SelectManager() {
        this.selectModels = new HashMap<>();
    }

    public List<Long> getSelectedFileIds() {
        SortedSet<SelectModelValueWithId> selected = new TreeSet<>(new SelectValueComparator());
        for (long objectId : selectModels.keySet()) {
            SelectModelValue selectModelValue = selectModels.get(objectId);
            if (selectModelValue.selected) {
                selected.add(new SelectModelValueWithId(objectId, selectModelValue));
            }
        }

        return newArrayList(transform(selected, new Function<SelectModelValueWithId, Long>() {

            @Override
            public Long apply(SelectModelValueWithId s) {
                return s.objectId;
            }
        }));
    }

    public void clearSelection() {
        for (SelectModelValue selectModelValue : selectModels.values()) {
            selectModelValue.clearSelect();
        }
    }

    public void initializeSelectModels(List<? extends IExecutorObject> objects) {
        for (int i = 0; i < objects.size(); i++) {
            final IExecutorObject object = objects.get(i);
            SelectModelValue selectModelValue = selectModels.get(object.getId());
            if (selectModelValue == null) {
                selectModelValue = new SelectModelValue();
                selectModels.put(object.getId(), selectModelValue);
            }
            selectModelValue.sortId = i;
        }
    }

    IModel<Boolean> newSelectCheckboxModel(final long objectId) {
        return new Model<Boolean>() {

            @Override
            public Boolean getObject() {
                return selectModels.get(objectId).selected;
            }

            @Override
            public void setObject(Boolean object) {
                if (object != null) {
                    selectModels.get(objectId).toggle();
                }
            }
        };
    }

    public void remove(long objectId) {
        selectModels.remove(objectId);
    }
}
