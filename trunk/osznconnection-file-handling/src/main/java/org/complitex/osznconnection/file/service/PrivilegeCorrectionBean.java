package org.complitex.osznconnection.file.service;

import com.google.common.collect.ImmutableMap;
import org.complitex.dictionary.entity.Correction;
import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.AbstractBean;
import org.complitex.osznconnection.file.entity.PrivilegeCorrection;

import javax.ejb.Stateless;
import java.util.List;
import java.util.Map;

/**
 * Класс для работы с коррекциями привилегий.
 * @author Artem
 */
@Stateless(name = "PrivilegeCorrectionBean")
public class PrivilegeCorrectionBean extends AbstractBean {
    private static final String NS = PrivilegeCorrectionBean.class.getName();
    private static final String NS_CORRECTION = Correction.class.getName();

    /**
     * Найти id внутреннего объекта системы(привилегии) в таблице коррекций привилегий по коду коррекции(organizationCode) и организации(organizationId)
     * @param organizationCode
     * @param calculationCenterId
     * @return
     */
    @Transactional
    public Long findInternalPrivilege(String organizationCode, long calculationCenterId) {
        Map<String, Object> params = ImmutableMap.<String, Object>of("code", organizationCode, "organizationId", calculationCenterId);
        List<Long> ids = sqlSession().selectList(NS + ".findInternalPrivilege", params);
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
        List<String> codes = sqlSession().selectList(NS + ".findPrivilegeCode", params);
        if (codes != null && !codes.isEmpty()) {
            return codes.get(0);
        }
        return null;
    }

    @Transactional
    public void save(PrivilegeCorrection privilegeCorrection) {
        if (privilegeCorrection.getId() == null) {
            sqlSession().insert(NS_CORRECTION + ".insertCorrection", privilegeCorrection);
        }else {
            sqlSession().update(NS_CORRECTION + ".updateCorrection", privilegeCorrection);
        }
    }

    public void delete(PrivilegeCorrection privilegeCorrection){
        sqlSession().delete(NS_CORRECTION + ".deleteCorrection", privilegeCorrection);
    }

    public PrivilegeCorrection getPrivilegeCorrection(Long id){
        return sqlSession().selectOne(NS + ".selectPrivilegeCorrection", id);
    }

    public List<PrivilegeCorrection> getPrivilegeCorrections(FilterWrapper<PrivilegeCorrection> filterWrapper){
        return sqlSession().selectList(NS + ".selectPrivilegeCorrections", filterWrapper);
    }

    public Integer getPrivilegeCorrectionCount(FilterWrapper<PrivilegeCorrection> filterWrapper){
        return sqlSession().selectOne(NS + ".selectPrivilegeCorrectionsCount", filterWrapper);
    }
}
