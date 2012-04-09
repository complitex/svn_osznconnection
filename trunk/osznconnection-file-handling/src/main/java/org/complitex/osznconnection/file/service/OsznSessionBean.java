package org.complitex.osznconnection.file.service;

import com.google.common.collect.Sets;
import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.service.SessionBean;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.osznconnection.organization.strategy.IOsznOrganizationStrategy;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.ejb.SessionContext;
import org.complitex.osznconnection.file.entity.RequestFileFilter;
import org.complitex.osznconnection.file.entity.RequestFileGroupFilter;
import org.complitex.osznconnection.file.entity.example.CorrectionExample;
import org.complitex.osznconnection.file.entity.example.PersonAccountExample;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 09.02.11 14:43
 */
@Stateless
@DeclareRoles(SessionBean.CHILD_ORGANIZATION_VIEW_ROLE)
public class OsznSessionBean {

    @Resource
    private SessionContext sessionContext;
    @EJB
    private SessionBean sessionBean;
    @EJB
    protected StrategyFactory strategyFactory;
    @EJB
    protected IOsznOrganizationStrategy osznOrganizationStrategy;

    public boolean isAdmin() {
        return sessionBean.isAdmin();
    }

    public String getAllOuterOrganizationsString() {
        String s = "";
        String d = "";

        for (long id : getAllOuterOrganizationObjectIds()) {
            s += d + id;
            d = ",";
        }

        return "(" + s + ")";
    }

    private List<Long> getAllOuterOrganizationObjectIds() {
        List<Long> objectIds = new ArrayList<Long>();

        for (DomainObject o : getOsznOrganizationStrategy().getAllOuterOrganizations(null)) {
            objectIds.add(o.getId());
        }

        return objectIds;
    }

    private IOsznOrganizationStrategy getOsznOrganizationStrategy() {
        return osznOrganizationStrategy;
    }

    private boolean hasOuterOrganization(Long objectId) {
        return getAllOuterOrganizationObjectIds().contains(objectId);
    }

    public boolean isAuthorized(Long outerOrganizationObjectId, Long userOrganizationId) {
        return isAdmin()
                || (hasOuterOrganization(outerOrganizationObjectId) && isUserOrganizationVisibleToCurrentUser(userOrganizationId));
    }

    public Long getCurrentUserOrganizationId() {
        final DomainObject mainUserOrganization = sessionBean.getMainUserOrganization();
        return mainUserOrganization != null && mainUserOrganization.getId() != null
                && mainUserOrganization.getId() > 0 ? mainUserOrganization.getId() : null;
    }

    public void prepareFilterForPermissionCheck(RequestFileFilter filter) {
        boolean isAdmin = sessionBean.isAdmin();
        filter.setAdmin(isAdmin);
        if (!isAdmin) {
            filter.setOuterOrganizationsString(getAllOuterOrganizationsString());
            filter.setUserOrganizationsString(getCurrentUserOrganizationsString());
        }
    }

    public void prepareExampleForPermissionCheck(CorrectionExample example) {
        boolean isAdmin = sessionBean.isAdmin();
        example.setAdmin(isAdmin);
        if (!isAdmin) {
            example.setOuterOrganizationsString(getAllOuterOrganizationsString());
            example.setUserOrganizationsString(getCurrentUserOrganizationsString());
        }
    }

    public void prepareExampleForPermissionCheck(PersonAccountExample example) {
        boolean isAdmin = sessionBean.isAdmin();
        example.setAdmin(isAdmin);
        if (!isAdmin) {
            example.setOuterOrganizationsString(getAllOuterOrganizationsString());
            example.setUserOrganizationsString(getCurrentUserOrganizationsString());
        }
    }

    public void prepareFilterForPermissionCheck(RequestFileGroupFilter filter) {
        boolean isAdmin = sessionBean.isAdmin();
        filter.setAdmin(isAdmin);
        if (!isAdmin) {
            filter.setOuterOrganizationsString(getAllOuterOrganizationsString());
            filter.setUserOrganizationsString(getCurrentUserOrganizationsString());
        }
    }
    
    public String getMainUserOrganizationForSearchCorrections(){
        if(sessionBean.isAdmin()){
            return null;
        }
        return getMainUserOrganizationString();
    }

    private String getCurrentUserOrganizationsString() {
        return getUserOrganizationsString(getUserOrganizationIdsVisibleToCurrentUser());
    }

    private String getUserOrganizationsString(Set<Long> userOrganizationIds) {
        String s = "";
        String d = "";

        for (long p : userOrganizationIds) {
            s += d + p;
            d = ", ";
        }

        return "(" + s + ")";
    }

    private Set<Long> getUserOrganizationIdsVisibleToCurrentUser() {
        return sessionContext.isCallerInRole(SessionBean.CHILD_ORGANIZATION_VIEW_ROLE)
                ? Sets.newHashSet(sessionBean.getUserOrganizationTreeObjectIds())
                : Sets.newHashSet(sessionBean.getUserOrganizationObjectIds());
    }

    public String getMainUserOrganizationString() {
        return getUserOrganizationsString(Sets.newHashSet(getCurrentUserOrganizationId()));
    }

    private boolean isUserOrganizationVisibleToCurrentUser(Long userOrganizationId) {
        return userOrganizationId == null || getUserOrganizationIdsVisibleToCurrentUser().contains(userOrganizationId);
    }

    public boolean isSuperUser() {
        return sessionContext.isCallerInRole(SessionBean.CHILD_ORGANIZATION_VIEW_ROLE);
    }
}
