package org.complitex.osznconnection.file.service;

import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.AbstractBean;
import org.complitex.dictionary.service.SessionBean;
import org.complitex.osznconnection.file.entity.RequestFileGroup;
import org.complitex.osznconnection.file.entity.RequestFileGroupFilter;
import org.complitex.osznconnection.file.entity.RequestFileStatus;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.HashMap;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.09.2010 14:32:05
 */
@Stateless(name = "RequestFileGroupBean")
public class RequestFileGroupBean extends AbstractBean {

    public static final String MAPPING_NAMESPACE = RequestFileGroupBean.class.getName();
    @EJB
    private RequestFileBean requestFileBean;
    @EJB
    private SessionBean sessionBean;

    @SuppressWarnings({"unchecked"})
    public List<RequestFileGroup> getRequestFileGroups(RequestFileGroupFilter filter) {
        sessionBean.prepareFilterForPermissionCheck(filter);
        return sqlSession().selectList(MAPPING_NAMESPACE + ".selectRequestFilesGroups", filter);
    }

    public int getRequestFileGroupsCount(RequestFileGroupFilter filter) {
        sessionBean.prepareFilterForPermissionCheck(filter);
        return (Integer) sqlSession().selectOne(MAPPING_NAMESPACE + ".selectRequestFilesGroupsCount", filter);
    }

    public RequestFileGroup getRequestFileGroup(Long id) {
        return (RequestFileGroup) sqlSession().selectOne(MAPPING_NAMESPACE + ".selectRequestFilesGroup", id);
    }

    public void delete(RequestFileGroup requestFileGroup) {
        if (requestFileGroup.getBenefitFile() != null) {
            requestFileBean.delete(requestFileGroup.getBenefitFile());
        }

        if (requestFileGroup.getPaymentFile() != null) {
            requestFileBean.delete(requestFileGroup.getPaymentFile());
        }

        sqlSession().delete(MAPPING_NAMESPACE + ".deleteRequestFileGroup", requestFileGroup.getId());
    }

    public void clear(RequestFileGroup requestFileGroup) {
        sqlSession().delete(MAPPING_NAMESPACE + ".deleteRequestFileGroup", requestFileGroup.getId());
    }

    public void save(RequestFileGroup group) {
        if (group.getId() == null) {
            sqlSession().insert(MAPPING_NAMESPACE + ".insertRequestFileGroup", group);
        } else {
            sqlSession().update(MAPPING_NAMESPACE + ".updateRequestFileGroup", group);
        }
    }

    public void clearEmptyGroup() {
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

    public RequestFileStatus getRequestFileStatus(RequestFileGroup group) {
        return (RequestFileStatus) sqlSession().selectOne(MAPPING_NAMESPACE + ".selectGroupStatus", group.getId());
    }

    public void fixProcessingOnInit() {
        sqlSession().update(MAPPING_NAMESPACE + ".fixLoadingOnInit");
        sqlSession().update(MAPPING_NAMESPACE + ".fixBingingOnInit");
        sqlSession().update(MAPPING_NAMESPACE + ".fixFillingOnInit");
        sqlSession().update(MAPPING_NAMESPACE + ".fixSavingOnInit");
    }
}
