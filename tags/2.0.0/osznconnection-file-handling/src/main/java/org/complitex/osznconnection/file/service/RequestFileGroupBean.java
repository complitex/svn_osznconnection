package org.complitex.osznconnection.file.service;

import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.service.AbstractBean;
import org.complitex.dictionary.service.SessionBean;
import org.complitex.dictionary.strategy.StrategyFactory;
import org.complitex.osznconnection.file.entity.RequestFileGroup;
import org.complitex.osznconnection.file.entity.RequestFileGroupFilter;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.osznconnection.file.entity.RequestFileStatus;
import org.complitex.osznconnection.organization.strategy.IOsznOrganizationStrategy;
import org.complitex.osznconnection.organization.strategy.OrganizationStrategy;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.09.2010 14:32:05
 */
@Stateless(name = "RequestFileGroupBean")
public class RequestFileGroupBean extends AbstractBean{
    public static final String MAPPING_NAMESPACE = RequestFileGroupBean.class.getName();

    @EJB(beanName = "RequestFileBean")
    private RequestFileBean requestFileBean;

    @EJB
    private OsznSessionBean osznSessionBean;

    @SuppressWarnings({"unchecked"})
    public List<RequestFileGroup> getRequestFileGroups(RequestFileGroupFilter filter){
        filter.setAdmin(osznSessionBean.isAdmin());
        filter.setOrganizations(osznSessionBean.getAllOuterOrganizationString());

        return sqlSession().selectList(MAPPING_NAMESPACE + ".selectRequestFilesGroups", filter);
    }

    public int getRequestFileGroupsCount(RequestFileGroupFilter filter){
        filter.setAdmin(osznSessionBean.isAdmin());
        filter.setOrganizations(osznSessionBean.getAllOuterOrganizationString());

        return (Integer)sqlSession().selectOne(MAPPING_NAMESPACE + ".selectRequestFilesGroupsCount", filter);
    }

    public void delete(RequestFileGroup requestFileGroup) {
        if (requestFileGroup.getBenefitFile() != null){
            requestFileBean.delete(requestFileGroup.getBenefitFile());
        }

        if (requestFileGroup.getPaymentFile() != null){
            requestFileBean.delete(requestFileGroup.getPaymentFile());
        }

        sqlSession().delete(MAPPING_NAMESPACE + ".deleteRequestFileGroup", requestFileGroup.getId());
    }

    public void clear(RequestFileGroup requestFileGroup){
        sqlSession().delete(MAPPING_NAMESPACE + ".deleteRequestFileGroup", requestFileGroup.getId());
    }

    public void save(RequestFileGroup group){
        if (group.getId() == null){
            sqlSession().insert(MAPPING_NAMESPACE + ".insertRequestFileGroup", group);
        }else {
            sqlSession().update(MAPPING_NAMESPACE + ".updateRequestFileGroup", group);
        }
    }

    public void clearEmptyGroup(){
        sqlSession().delete(MAPPING_NAMESPACE + ".clearEmptyGroup");
    }

    @Transactional
    public void updateStatus(final long requestFileId, final RequestFileStatus status) {
        sqlSession().update(MAPPING_NAMESPACE + ".updateStatus", new HashMap<String, Object>() {

            {
                put("fileId", requestFileId);
                put("status", status);
            }
        });
    }

    @Transactional
    public long getPaymentFileId(long benefitFileId) {
        return (Long) sqlSession().selectOne(MAPPING_NAMESPACE + ".getPaymentFileId", benefitFileId);
    }

    @Transactional
    public long getBenefitFileId(long paymentFileId) {
        return (Long) sqlSession().selectOne(MAPPING_NAMESPACE + ".getBenefitFileId", paymentFileId);
    }

    public RequestFileStatus getRequestFileStatus(RequestFileGroup group){
        return (RequestFileStatus) sqlSession().selectOne(MAPPING_NAMESPACE + ".selectGroupStatus", group.getId());
    }
}