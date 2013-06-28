/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import com.google.common.collect.ImmutableMap;
import org.complitex.correction.entity.Correction;
import org.complitex.correction.service.CorrectionBean;
import org.complitex.dictionary.mybatis.Transactional;

import javax.ejb.Stateless;
import java.util.List;
import java.util.Map;

/**
 * Класс для работы с коррекциями форм власти
 * @author Artem
 */
@Stateless
public class OwnershipCorrectionBean extends CorrectionBean {

    private static final String MAPPING_NAMESPACE = OwnershipCorrectionBean.class.getName();

    /**
     * Найти id внутреннего объекта системы(форму власти) в таблице коррекций форм власти по коррекции(correction) и организации(organizationId)
     * @param correction
     * @param calculationCenterId
     * @return
     */
    @Transactional
    public Long findInternalOwnership(String correction, long calculationCenterId) {
        Map<String, Object> params = ImmutableMap.<String, Object>of("correction", correction, "organizationId", calculationCenterId);
        List<Long> ids = sqlSession().selectList(MAPPING_NAMESPACE + ".findInternalOwnership", params);
        if (ids != null && !ids.isEmpty()) {
            return ids.get(0);
        }
        return null;
    }

    /**
     * Найти код коррекции в таблице коррекций форм власти по id внутреннего объекта системы(формы власти) и организации.
     * @param objectId
     * @param osznId
     * @return
     */
    @Transactional
    public String findOwnershipCode(long objectId, long osznId, long userOrganizationId) {
        Map<String, Long> params = ImmutableMap.of("objectId", objectId, "organizationId", osznId,
                "userOrganizationId", userOrganizationId);
        List<String> codes = sqlSession().selectList(MAPPING_NAMESPACE + ".findOwnershipCode", params);
        if (codes != null && !codes.isEmpty()) {
            return codes.get(0);
        }
        return null;
    }

    private Correction createOwnershipCorrection(String code, String ownership, long ownershipObjectId,
            long organizationId, long internalOrganizationId, long userOrganizationId) {
        Correction correction = new Correction("ownership");
        correction.setParentId(null);
        correction.setExternalId(code);
        correction.setCorrection(ownership);
        correction.setOrganizationId(organizationId);
        correction.setModuleId(internalOrganizationId);
        correction.setObjectId(ownershipObjectId);
        correction.setUserOrganizationId(userOrganizationId);
        return correction;
    }

    public void insertOwnershipCorrection(String code, String ownership, long ownershipObjectId, long organizationId,
            long internalOrganizationId, Long userOrganizationId) {
        insert(createOwnershipCorrection(code, ownership, ownershipObjectId, organizationId, internalOrganizationId,
                userOrganizationId));
    }
}
