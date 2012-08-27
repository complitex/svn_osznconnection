/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.web.component.load;

import java.io.Serializable;
import org.apache.wicket.ajax.AjaxRequestTarget;

/**
 *
 * @author Artem
 */
public interface IRequestFileLoader extends Serializable {

    void load(long userOrganizationId, long osznId, DateParameter dateParameter, AjaxRequestTarget target);
}
