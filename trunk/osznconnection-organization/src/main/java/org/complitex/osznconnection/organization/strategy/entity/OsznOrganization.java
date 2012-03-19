/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.organization.strategy.entity;

import org.complitex.dictionary.entity.DomainObject;

/**
 *
 * @author Artem
 */
public class OsznOrganization extends DomainObject {

    private ServiceAssociationList serviceAssociationList;

    public OsznOrganization(DomainObject copy, ServiceAssociationList serviceAssociationList) {
        super(copy);
        this.serviceAssociationList = serviceAssociationList;
    }

    public OsznOrganization(ServiceAssociationList serviceAssociationList) {
        this.serviceAssociationList = serviceAssociationList;
    }

    public ServiceAssociationList getServiceAssociationList() {
        return serviceAssociationList;
    }
}
