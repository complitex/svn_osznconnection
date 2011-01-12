package org.complitex.osznconnection.file.service.status.details;

import org.complitex.dictionary.service.AbstractBean;

import javax.ejb.Stateless;
import java.util.List;
import org.complitex.osznconnection.file.entity.StatusDetailInfo;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 24.11.10 16:51
 */
@Stateless(name = "StatusDetailBean")
public class StatusDetailBean extends AbstractBean {

    public static final String MAPPING_NAMESPACE = StatusDetailBean.class.getName();

    public List<StatusDetailInfo> getPaymentStatusDetails(long requestFileId) {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".getPaymentStatusDetailInfo", requestFileId);
    }

    public List<StatusDetailInfo> getBenefitStatusDetails(long requestFileId) {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".getBenefitStatusDetailInfo", requestFileId);
    }

    public List<StatusDetailInfo> getActualPaymentStatusDetails(long requestFileId) {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".getActualPaymentStatusDetailInfo", requestFileId);
    }
}
