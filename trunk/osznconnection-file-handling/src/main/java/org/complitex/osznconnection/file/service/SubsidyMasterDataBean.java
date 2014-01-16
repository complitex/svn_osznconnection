package org.complitex.osznconnection.file.service;

import org.complitex.dictionary.entity.FilterWrapper;
import org.complitex.dictionary.service.AbstractBean;
import org.complitex.osznconnection.file.entity.SubsidyMasterData;

import javax.ejb.Stateless;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 14.01.14 20:32
 */
@Stateless
public class SubsidyMasterDataBean extends AbstractBean {
    public final static String NS = SubsidyMasterDataBean.class.getName();

    public void save(SubsidyMasterData subsidyMasterData){
        if (subsidyMasterData.getId() == null){
            sqlSession().insert(NS + ".insertSubsidyMasterData", subsidyMasterData);
            sqlSession().insert(NS + ".insertSubsidyMasterDataPart", subsidyMasterData);
        }else {
            sqlSession().update(NS + ".updateSubsidyMasterData", subsidyMasterData);
        }
    }

    public SubsidyMasterData getSubsidyMasterData(Long id){
        return sqlSession().selectOne(NS + ".selectSubsidyMasterData", id);
    }

    public List<SubsidyMasterData> getSubsidyMasterDataList(Long subsidyId){
        return sqlSession().selectList(NS + ".selectSubsidyMasterDataList", subsidyId);
    }

    public Integer getSubsidyMasterDataListCount(FilterWrapper<SubsidyMasterData> filterWrapper){
        return sqlSession().selectOne(NS + ".selectSubsidyMasterDataListCount", filterWrapper);
    }

    public void clearSubsidyMasterDataList(Long subsidyId){
        sqlSession().delete(NS + ".deleteSubsidyMasterDataList", subsidyId);
    }
}
