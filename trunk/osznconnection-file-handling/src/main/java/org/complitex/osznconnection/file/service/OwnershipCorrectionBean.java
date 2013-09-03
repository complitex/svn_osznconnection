package org.complitex.osznconnection.file.service;

import com.google.common.collect.ImmutableMap;
import org.complitex.dictionary.entity.Correction;
import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.AbstractBean;
import org.complitex.osznconnection.file.entity.OwnershipCorrection;

import javax.ejb.Stateless;
import java.util.List;
import java.util.Map;

/**
 * Класс для работы с коррекциями форм власти
 * @author Artem
 */
@Stateless
public class OwnershipCorrectionBean extends AbstractBean {
    private static final String NS = OwnershipCorrectionBean.class.getName();
    private static final String NS_CORRECTION = Correction.class.getName();

    /**
     * Найти id внутреннего объекта системы(форму власти) в таблице коррекций форм власти по коррекции(correction) и организации(organizationId)
     * @param correction
     * @param calculationCenterId
     * @return
     */
    @Transactional
    public Long findInternalOwnership(String correction, long calculationCenterId) {
        Map<String, Object> params = ImmutableMap.<String, Object>of("correction", correction, "organizationId", calculationCenterId);
        List<Long> ids = sqlSession().selectList(NS + ".findInternalOwnership", params);
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
        List<String> codes = sqlSession().selectList(NS + ".findOwnershipCode", params);
        if (codes != null && !codes.isEmpty()) {
            return codes.get(0);
        }
        return null;
    }

    public OwnershipCorrection getOwnershipCorrection(Long id){
        return sqlSession().selectOne(NS + ".selectOwnershipCorrection", id);
    }

    public void save(OwnershipCorrection ownershipCorrection) {
        if (ownershipCorrection.getId() == null) {
            sqlSession().insert(NS_CORRECTION + ".insertCorrection", ownershipCorrection);
        }else {
            sqlSession().update(NS_CORRECTION + ".updateCorrection", ownershipCorrection);
        }
    }

    public void delete(Long id){
        sqlSession().delete(NS_CORRECTION + ".deleteCorrection", id);
    }

    public List<OwnershipCorrection> getOwnershipCorrections(FilterWrapper<OwnershipCorrection> filterWrapper){
        return sqlSession().selectList(NS + ".selectOwnershipCorrections", filterWrapper);
    }

    public Integer getOwnershipCorrectionsCount(FilterWrapper<OwnershipCorrection> filterWrapper){
        return sqlSession().selectOne(NS + ".selectOwnershipCorrectionsCount", filterWrapper);
    }
}
