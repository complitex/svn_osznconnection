/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.osznconnection.file.entity.Correction;
import org.complitex.osznconnection.file.entity.example.CorrectionExample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import java.util.List;

/**
 * Класс для работы с коррекциями привилегий.
 * @author Artem
 */
@Stateless(name = "PrivilegeCorrectionBean")
public class PrivilegeCorrectionBean extends CorrectionBean {
    private static final Logger log = LoggerFactory.getLogger(PrivilegeCorrectionBean.class);

    private static final String MAPPING_NAMESPACE = PrivilegeCorrectionBean.class.getName();

    /**
     * Найти id внутреннего объекта системы(привилегии) в таблице коррекций привилегий по коду коррекции(organizationCode) и организации(organizationId)
     * @param organizationCode
     * @param organizationId
     * @return
     */
    @SuppressWarnings({"unchecked"})
    @Transactional
    public Long findInternalPrivilege(String organizationCode, long organizationId) {
        CorrectionExample example = new CorrectionExample();
        example.setCode(organizationCode);
        example.setOrganizationId(organizationId);
        List<Long> ids = sqlSession().selectList(MAPPING_NAMESPACE + ".findInternalPrivilege", example);
        if (ids != null && !ids.isEmpty()) {
            return ids.get(0);
        }
        return null;
    }

    /**
     * Найти код коррекции в таблице коррекций привилегий по id внутреннего объекта системы(привилегии) и организации.
     * @param objectId
     * @param organizationId
     * @return
     */
    @SuppressWarnings({"unchecked"})
    @Transactional
    public String findPrivilegeCode(long objectId, long organizationId) {
        CorrectionExample example = new CorrectionExample();
        example.setObjectId(objectId);
        example.setOrganizationId(organizationId);
        List<String> codes = sqlSession().selectList(MAPPING_NAMESPACE + ".findPrivilegeCode", example);
        if (codes != null && !codes.isEmpty()) {
            return codes.get(0);
        }
        return null;
    }

    public Correction createOwnershipCorrection(String code, String privilege, long ownershipObjectId, long organizationId,
                                                long internalOrganizationId) {
        Correction correction = new Correction("privilege");
        correction.setParentId(null);
        correction.setCode(code);
        correction.setCorrection(privilege);
        correction.setOrganizationId(organizationId);
        correction.setInternalOrganizationId(internalOrganizationId);
        correction.setObjectId(ownershipObjectId);
        return correction;
    }

    public void insertOwnershipCorrection(String code, String privilege, long ownershipObjectId, long organizationId,
                                          long internalOrganizationId) {
        insert(createOwnershipCorrection(code, privilege, ownershipObjectId, organizationId, internalOrganizationId));
    }
}
