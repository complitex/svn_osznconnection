package org.complitex.osznconnection.file.service;

import com.google.common.collect.Maps;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.AbstractBean;
import org.complitex.osznconnection.file.entity.AbstractRequest;
import org.complitex.osznconnection.file.entity.RequestFileType;
import org.complitex.osznconnection.file.service.file_description.RequestFileDescription;
import org.complitex.osznconnection.file.service.file_description.RequestFileDescriptionBean;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Класс для работы с тарифами субсидий.
 *
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 10.09.2010 18:16:17
 */
@Stateless
public class SubsidyTarifBean extends AbstractBean {
    public static final String MAPPING_NAMESPACE = SubsidyTarifBean.class.getName();
    @EJB
    private RequestFileDescriptionBean requestFileDescriptionBean;

    @Transactional
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void insert(List<AbstractRequest> abstractRequests) {
        for (AbstractRequest abstractRequest : abstractRequests) {
            sqlSession().insert(MAPPING_NAMESPACE + ".insertTarif", abstractRequest);
        }
    }

    @Transactional
    public void delete(long requestFileId) {
        sqlSession().delete(MAPPING_NAMESPACE + ".deleteTarifs", requestFileId);
    }

    /**
     * Получить значение поля T11_CODE2 из таблицы тарифов по коду тарифа в ЦН и ОСЗН.
     * @param T11_CS_UNI Код тарифа, который пришел из ЦН.
     * @param osznId ОСЗН
     * @return значение поля T11_CODE2 из таблицы тарифов по коду тарифа в ЦН и ОСЗН
     */
    @Transactional
    public String getCode2(BigDecimal T11_CS_UNI, long osznId, long userOrganizationId) {
        RequestFileDescription tarifDescription = requestFileDescriptionBean.getFileDescription(RequestFileType.SUBSIDY_TARIF);
        Map<String, Object> params = Maps.newHashMap();

        params.put("T11_CS_UNI", tarifDescription.getTypeConverter().toString(T11_CS_UNI));
        params.put("osznId", osznId);
        params.put("userOrganizationId", userOrganizationId);

        return sqlSession().selectOne(MAPPING_NAMESPACE + ".getCode2", params);
    }
}
