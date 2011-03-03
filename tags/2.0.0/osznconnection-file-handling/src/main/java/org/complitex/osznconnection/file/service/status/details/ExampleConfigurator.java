/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service.status.details;

import org.complitex.osznconnection.file.entity.StatusDetail;
import org.complitex.osznconnection.file.entity.StatusDetailInfo;
import org.complitex.osznconnection.file.entity.example.AbstractRequestExample;

/**
 *
 * @author Artem
 */
public interface ExampleConfigurator<T extends AbstractRequestExample> {

    T createExample(Class<T> exampleClass, StatusDetailInfo statusDetailInfo);

    T createExample(StatusDetail statusDetail);
}
