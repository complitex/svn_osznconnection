package org.complitex.osznconnection.file.service;

import org.complitex.dictionaryfw.mybatis.Transactional;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.osznconnection.file.entity.AbstractRequest;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.Tarif;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 10.09.2010 18:16:17
 */
@Stateless
public class TarifBean extends AbstractBean {

    public static final String MAPPING_NAMESPACE = TarifBean.class.getName();

    @Transactional
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void insert(List<AbstractRequest> abstractRequests) {
        for (AbstractRequest abstractRequest : abstractRequests) {
            sqlSession().insert(MAPPING_NAMESPACE + ".insertTarif", abstractRequest);
        }
    }

    @Transactional
    public void insert(Tarif tarif) {
        sqlSession().insert(MAPPING_NAMESPACE + ".insertTarif", tarif);
    }

    @Transactional
    public void delete(RequestFile requestFile) {
        sqlSession().delete(MAPPING_NAMESPACE + ".deleteTarifs", requestFile.getId());
    }

    @SuppressWarnings({"unchecked"})
    @Transactional
    public Integer getCODE2_1(Double T11_CS_UNI) {
        return (Integer) sqlSession().selectOne(MAPPING_NAMESPACE + ".getCODE2_1", T11_CS_UNI);
    }
}
