/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import com.google.common.collect.ImmutableMap;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.osznconnection.file.entity.Correction;

import javax.ejb.Stateless;
import java.util.List;
import java.util.Map;

/**
 * Класс для работы с коррекциями привилегий.
 * @author Artem
 */
@Stateless(name = "PrivilegeCorrectionBean")
public class PrivilegeCorrectionBean extends CorrectionBean {

    private static final String MAPPING_NAMESPACE = PrivilegeCorrectionBean.class.getName();

    /**
     * Найти id внутреннего объекта системы(привилегии) в таблице коррекций привилегий по коду коррекции(organizationCode) и организации(organizationId)
     * @param organizationCode
     * @param calculationCenterId
     * @return
     */
    @Transactional
    public Long findInternalPrivilege(String organizationCode, long calculationCenterId) {
        Map<String, Object> params = ImmutableMap.<String, Object>of("code", organizationCode, "organizationId", calculationCenterId);
        List<Long> ids = sqlSession().selectList(MAPPING_NAMESPACE + ".findInternalPrivilege", params);
        if (ids != null && !ids.isEmpty()) {
            return ids.get(0);
        }
        return null;
    }

    /**
     * Найти код коррекции в таблице коррекций привилегий по id внутреннего объекта системы(привилегии) и организации.
     * @param objectId
     * @param osznId
     * @return
     */
    @Transactional
    public String findPrivilegeCode(long objectId, long osznId, long userOrganizationId) {
        Map<String, Long> params = ImmutableMap.of("objectId", objectId, "organizationId", osznId,
                "userOrganizationId", userOrganizationId);
        List<String> codes = sqlSession().selectList(MAPPING_NAMESPACE + ".findPrivilegeCode", params);
        if (codes != null && !codes.isEmpty()) {
            return codes.get(0);
        }
        return null;
    }

    private Correction createPrivilegeCorrection(String code, String privilege, long ownershipObjectId,
            long organizationId, long internalOrganizationId, long userOrganizationId) {
        Correction correction = new Correction("privilege");
        correction.setParentId(null);
        correction.setCode(code);
        correction.setCorrection(privilege);
        correction.setOrganizationId(organizationId);
        correction.setInternalOrganizationId(internalOrganizationId);
        correction.setObjectId(ownershipObjectId);
        correction.setUserOrganizationId(userOrganizationId);
        return correction;
    }

    @Transactional
    public void insertPrivilegeCorrection(String code, String privilege, long ownershipObjectId, long organizationId,
            long internalOrganizationId, Long userOrganizationId) {
        insert(createPrivilegeCorrection(code, privilege, ownershipObjectId, organizationId, internalOrganizationId,
                userOrganizationId));
    }
}
