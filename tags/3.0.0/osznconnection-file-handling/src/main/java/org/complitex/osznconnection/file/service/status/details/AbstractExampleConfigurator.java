/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service.status.details;

import java.io.Serializable;
import org.complitex.osznconnection.file.entity.StatusDetailInfo;
import org.complitex.osznconnection.file.entity.example.AbstractRequestExample;

/**
 *
 * @author Artem
 */
public abstract class AbstractExampleConfigurator<T extends AbstractRequestExample> implements ExampleConfigurator<T>, Serializable {

    @Override
    public T createExample(Class<T> exampleClass, StatusDetailInfo statusDetailInfo) {
        try {
            T example = exampleClass.newInstance();
            example.setStatus(statusDetailInfo.getStatus());
            return example;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
