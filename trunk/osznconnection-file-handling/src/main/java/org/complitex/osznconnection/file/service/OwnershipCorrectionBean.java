/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.AbstractBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import java.util.List;
import org.complitex.osznconnection.file.entity.example.CorrectionExample;

/**
 * Класс для работы с коррекциями форм власти
 * @author Artem
 */
@Stateless
public class OwnershipCorrectionBean extends AbstractBean {

    private static final Logger log = LoggerFactory.getLogger(OwnershipCorrectionBean.class);
    
    private static final String MAPPING_NAMESPACE = OwnershipCorrectionBean.class.getName();

    /**
     * Найти id внутреннего объекта системы(форму власти) в таблице коррекций форм власти по коррекции(correction) и организации(organizationId)
     * @param correction
     * @param organizationId
     * @return
     */
    @Transactional
    public Long findInternalOwnership(String correction, long organizationId) {
        CorrectionExample example = new CorrectionExample();
        example.setCorrection(correction);
        example.setOrganizationId(organizationId);
        List<Long> ids = sqlSession().selectList(MAPPING_NAMESPACE + ".findInternalOwnership", example);
        if (ids != null && !ids.isEmpty()) {
            return ids.get(0);
        }
        return null;
    }

    /**
     * Найти код коррекции в таблице коррекций форм власти по id внутреннего объекта системы(формы власти) и организации.
     * @param objectId
     * @param organizationId
     * @return
     */
    @Transactional
    public String findOwnershipCode(long objectId, long organizationId) {
        CorrectionExample example = new CorrectionExample();
        example.setObjectId(objectId);
        example.setOrganizationId(organizationId);
        List<String> codes = sqlSession().selectList(MAPPING_NAMESPACE + ".findOwnershipCode", example);
        if (codes != null && !codes.isEmpty()) {
            return codes.get(0);
        }
        return null;
    }
}