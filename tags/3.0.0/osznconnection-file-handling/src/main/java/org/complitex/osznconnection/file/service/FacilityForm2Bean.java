/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.osznconnection.file.entity.AbstractAccountRequest;
import org.complitex.osznconnection.file.entity.AbstractRequest;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.util.List;

/**
 *
 * @author Artem
 */
@Stateless
public class FacilityForm2Bean extends AbstractRequestBean {

    public static final String MAPPING_NAMESPACE = FacilityForm2Bean.class.getName();

    @Transactional
    public void delete(long requestFileId) {
        sqlSession().delete(MAPPING_NAMESPACE + ".deleteFacilityForm2", requestFileId);
    }

    @Transactional
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void insert(List<AbstractRequest> abstractRequests) {
        if (abstractRequests.isEmpty()) {
            return;
        }
        sqlSession().insert(MAPPING_NAMESPACE + ".insertFacilityForm2List", abstractRequests);
    }

    public List<AbstractAccountRequest> getFacilityForm2(long requestFileId) {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".selectFacilityForm2", requestFileId);
    }
}
