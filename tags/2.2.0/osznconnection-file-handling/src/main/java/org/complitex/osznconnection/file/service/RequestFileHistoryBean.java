package org.complitex.osznconnection.file.service;

import org.complitex.dictionary.service.AbstractBean;
import org.complitex.osznconnection.file.entity.RequestFileHistory;

import javax.ejb.Stateless;
import java.util.List;

/**
 * @author Anatoly Ivanov java@inheaven.ru
 *         Date: 10.12.13 23:41
 */
@Stateless
public class RequestFileHistoryBean extends AbstractBean {
    public static final String NS = RequestFileHistoryBean.class.getName();

    public void save(RequestFileHistory requestFileHistory){
        sqlSession().insert(NS + ".insertRequestFileHistory", requestFileHistory);
    }

    public List<RequestFileHistory> getRequestFileHistories(Long requestFileId){
        return sqlSession().selectList(NS + ".selectRequestFileHistories", requestFileId);
    }

    public RequestFileHistory getLastRequestFileHistory(Long requestFileId){
        return sqlSession().selectOne(NS + ".selectLastRequestFileHistory", requestFileId);
    }


}