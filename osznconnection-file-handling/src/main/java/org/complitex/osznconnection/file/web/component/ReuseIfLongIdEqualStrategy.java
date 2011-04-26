package org.complitex.osznconnection.file.web.component;

import org.apache.wicket.markup.repeater.IItemFactory;
import org.apache.wicket.markup.repeater.IItemReuseStrategy;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.complitex.dictionary.entity.ILongId;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 21.04.11 18:11
 */
public class ReuseIfLongIdEqualStrategy implements IItemReuseStrategy {
    @Override
    public <T> Iterator<Item<T>> getItems(final IItemFactory<T> factory, final Iterator<IModel<T>> newModels,
                                          Iterator<Item<T>> existingItems) {
        final Map<Long, Item<T>> idToItem = new HashMap<Long, Item<T>>();

        while (existingItems.hasNext()) {
            Item<T> item = existingItems.next();
            idToItem.put(((ILongId) item.getModel().getObject()).getId(), item);
        }

        return new Iterator<Item<T>>() {
            private int index = 0;

            public boolean hasNext() {
                return newModels.hasNext();
            }

            public Item<T> next() {
                IModel<T> model = newModels.next();
                Item<T> oldItem = idToItem.get(((ILongId) model.getObject()).getId());

                Item<T> item;

                if (oldItem == null) {
                    item = factory.newItem(index, model);
                } else {
                    oldItem.setModel(model);
                    oldItem.setIndex(index);
                    item = oldItem;
                }

                index++;

                return item;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
