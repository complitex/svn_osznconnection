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

    public SubsidyMasterData getSubsidyMasterData(Long id){
        return sqlSession().selectOne(NS + ".selectSubsidyMasterData", id);
    }

    public List<SubsidyMasterData> getSubsidyMasterDataList(FilterWrapper<SubsidyMasterData> filterWrapper){
        return sqlSession().selectList(NS + ".selectSubsidyMasterDataList", filterWrapper);
    }

    public Integer getSubsidyMasterDataListCount(FilterWrapper<SubsidyMasterData> filterWrapper){
        return sqlSession().selectOne(NS + ".selectSubsidyMasterDataListCount", filterWrapper);
    }


}
