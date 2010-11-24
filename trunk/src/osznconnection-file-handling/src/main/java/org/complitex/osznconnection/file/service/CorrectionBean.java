/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import com.google.common.collect.Maps;
import org.complitex.dictionaryfw.entity.DomainObject;
import org.complitex.dictionaryfw.mybatis.Transactional;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.dictionaryfw.strategy.Strategy;
import org.complitex.dictionaryfw.strategy.StrategyFactory;
import org.complitex.osznconnection.file.entity.Correction;
import org.complitex.osznconnection.file.entity.example.CorrectionExample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Обобщенный класс для работы с коррекциями.
 * @author Artem
 */
@Stateless
public class CorrectionBean extends AbstractBean {

    private static final Logger log = LoggerFactory.getLogger(CorrectionBean.class);

    protected static final String CORRECTION_BEAN_MAPPING_NAMESPACE = CorrectionBean.class.getName();

    @EJB(beanName = "StrategyFactory")
    protected StrategyFactory strategyFactory;

    public static enum OrderBy {

        CORRECTION("correction"), CODE("organization_code"), ORGANIZATION("organization"), INTERNAL_ORGANIZATION("internalOrganization");

        private String orderBy;

        private OrderBy(String orderBy) {
            this.orderBy = orderBy;
        }

        public String getOrderBy() {
            return orderBy;
        }
    }

    @Transactional
    public List<Correction> find(CorrectionExample example) {
        return (List<Correction>) find(example, CORRECTION_BEAN_MAPPING_NAMESPACE + ".find");
    }

    protected List<? extends Correction> find(CorrectionExample example, String queryId) {
        Strategy strategy = strategyFactory.getStrategy(example.getEntity());
        List<Correction> results = sqlSession().selectList(queryId, example);
        for (Correction correction : results) {
            DomainObject object = strategy.findById(correction.getObjectId());
            correction.setDisplayObject(strategy.displayDomainObject(object, new Locale(example.getLocale())));
        }
        return results;
    }

    @Transactional
    public int count(CorrectionExample example) {
        return (Integer) sqlSession().selectOne(CORRECTION_BEAN_MAPPING_NAMESPACE + ".count", example);
    }

    @Transactional
    public Correction findById(String entity, Long correctionId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("entity", entity);
        params.put("id", correctionId);

        Correction correction = (Correction) sqlSession().selectOne(CORRECTION_BEAN_MAPPING_NAMESPACE + ".findById", params);

        if (correction != null) {
            correction.setEntity(entity);
        }

        return correction;
    }

    @Transactional
    public void update(Correction correction) {
        sqlSession().update(CORRECTION_BEAN_MAPPING_NAMESPACE + ".update", correction);
    }

    @Transactional
    public void insert(Correction correction) {
        sqlSession().insert(CORRECTION_BEAN_MAPPING_NAMESPACE + ".insert", correction);
    }

    @Transactional
    public void delete(Correction correction) {
        sqlSession().delete(CORRECTION_BEAN_MAPPING_NAMESPACE + ".delete", correction);
    }
}
