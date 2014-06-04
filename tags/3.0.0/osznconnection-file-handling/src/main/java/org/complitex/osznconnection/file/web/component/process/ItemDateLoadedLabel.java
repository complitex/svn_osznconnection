/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.component.process;

import java.util.Date;
import org.apache.wicket.datetime.PatternDateConverter;
import org.apache.wicket.datetime.markup.html.basic.DateLabel;
import org.apache.wicket.model.Model;
import org.complitex.dictionary.util.DateUtil;

/**
 *
 * @author Artem
 */
public final class ItemDateLoadedLabel extends DateLabel {

    public ItemDateLoadedLabel(String id, Date loaded) {
        super(id, new Model<Date>(loaded),
                new PatternDateConverter(DateUtil.isCurrentDay(loaded) ? "HH:mm:ss" : "dd.MM.yy HH:mm:ss", true));
    }
}
