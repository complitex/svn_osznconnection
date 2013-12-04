package org.complitex.osznconnection.organization.strategy.entity;

import org.complitex.dictionary.entity.DomainObject;

//todo is it oszn organization or user organization with service association?
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
