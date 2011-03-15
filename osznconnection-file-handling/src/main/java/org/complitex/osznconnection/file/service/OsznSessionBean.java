package org.complitex.osznconnection.file.service;

import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.service.SessionBean;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.osznconnection.organization.strategy.IOsznOrganizationStrategy;
import org.complitex.osznconnection.organization.strategy.OsznOrganizationStrategy;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 09.02.11 14:43
 */
@Stateless
public class OsznSessionBean {
    @EJB
    private SessionBean sessionBean;

    @EJB
    protected StrategyFactory strategyFactory;

    @EJB
    protected IOsznOrganizationStrategy osznOrganizationStrategy;

    public boolean isAdmin(){
        return sessionBean.isAdmin();
    }

    public String getAllOuterOrganizationString() {
        String s = "";
        String d = "";

        for (DomainObject o : getOsznOrganizationStrategy().getAllOuterOrganizations(null)){
            s += d + o.getId();
            d = ",";
        }

        return "(" + s + ")";
    }

    public List<Long> getAllOuterOrganizationObjectIds(){
        List<Long> objectIds = new ArrayList<Long>();

        for (DomainObject o : getOsznOrganizationStrategy().getAllOuterOrganizations(null)){
            objectIds.add(o.getId());
        }

        return objectIds;
    }

    private IOsznOrganizationStrategy getOsznOrganizationStrategy(){
        return osznOrganizationStrategy;
    }

    public boolean hasOuterOrganization(Long objectId){
        return getAllOuterOrganizationObjectIds().contains(objectId);
    }

    public boolean isAuthorized(Long organizationObjectId){
        return isAdmin() || hasOuterOrganization(organizationObjectId);
    }
}
