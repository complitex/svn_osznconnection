package org.complitex.osznconnection.file.service;

import org.complitex.dictionaryfw.mybatis.Transactional;
import org.complitex.dictionaryfw.service.AbstractBean;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.08.2010 12:15:53
 */
@Stateless(name = "RequestFileBean")
public class RequestFileBean extends AbstractBean {
    private static final Logger log = LoggerFactory.getLogger(RequestFileBean.class);
    public static final String MAPPING_NAMESPACE = RequestFileBean.class.getName();

    @Transactional
    public RequestFile findById(long fileId) {
        return (RequestFile) sqlSession().selectOne(MAPPING_NAMESPACE + ".findById", fileId);
    }

    @SuppressWarnings({"unchecked"})
    @Transactional
    public List<RequestFile> getRequestFiles(RequestFileFilter filter){
        return sqlSession().selectList(MAPPING_NAMESPACE + ".selectRequestFiles", filter);
    }

    @Transactional
    public int size(RequestFileFilter filter){
        return (Integer) sqlSession().selectOne(MAPPING_NAMESPACE + ".selectRequestFilesCount", filter);
    }

    @Transactional
    public void save(RequestFile requestFile){
        if (requestFile.getId() == null){
            sqlSession().insert(MAPPING_NAMESPACE + ".insertRequestFile", requestFile);
        }else{
            sqlSession().update(MAPPING_NAMESPACE + ".updateRequestFile", requestFile);
        }
    }

    @Transactional
    public void delete(RequestFile requestFile){
        switch (requestFile.getType()){
            case BENEFIT:
                sqlSession().delete(BenefitBean.MAPPING_NAMESPACE + ".deleteBenefits", requestFile.getId());
                break;
            case PAYMENT:
                sqlSession().delete(PaymentBean.MAPPING_NAMESPACE + ".deletePayments", requestFile.getId());
                break;
            case TARIF:
                sqlSession().delete(TarifBean.MAPPING_NAMESPACE + ".deleteTarifs", requestFile.getId());
                break;
        }


        sqlSession().delete(MAPPING_NAMESPACE + ".deleteRequestFile", requestFile);        
    }

    public boolean isLoaded(RequestFile requestFile){
        return (Boolean) sqlSession().selectOne(MAPPING_NAMESPACE + ".selectIsLoaded", requestFile);
    }

    public void cancelLoading(){
        sqlSession().update(MAPPING_NAMESPACE + ".cancelLoading");
    }
    
     public void cancelSaving() {
        sqlSession().update(MAPPING_NAMESPACE + ".cancelSaving");
    }
}
