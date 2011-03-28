/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.calculation.service;

import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.AbstractBean;
import org.complitex.osznconnection.file.calculation.adapter.ICalculationCenterAdapter;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import org.complitex.osznconnection.file.calculation.adapter.DefaultCalculationCenterAdapter;
import org.complitex.osznconnection.organization.strategy.IOsznOrganizationStrategy;

/**
 * 
 * @author Artem
 */
@Stateless
public class CalculationCenterBean extends AbstractBean {

    @EJB(name = "OsznOrganizationStrategy")
    private IOsznOrganizationStrategy organizationStrategy;
    @Resource
    private SessionContext sessionContext;

    @Transactional
    public long getCurrentCalculationCenterId() {
        return organizationStrategy.getCurrentCalculationCenterId();
    }

    public ICalculationCenterAdapter getDefaultCalculationCenterAdapter() {
        return (ICalculationCenterAdapter) sessionContext.lookup("java:module/" + DefaultCalculationCenterAdapter.class.getName());
    }
}
