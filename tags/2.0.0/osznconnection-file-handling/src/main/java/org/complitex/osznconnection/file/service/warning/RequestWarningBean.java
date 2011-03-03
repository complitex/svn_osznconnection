/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service.warning;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import javax.ejb.Stateless;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.AbstractBean;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestWarning;
import org.complitex.osznconnection.file.entity.RequestWarningParameter;
import org.complitex.osznconnection.file.entity.RequestWarningStatus;

/**
 *
 * @author Artem
 */
@Stateless
public class RequestWarningBean extends AbstractBean {

    private static final String MAPPING_NAMESPACE = RequestWarningBean.class.getName();

    @Transactional
    public void save(RequestWarning requestWarning) {
        sqlSession().insert(MAPPING_NAMESPACE + ".insertWarning", requestWarning);
        for (RequestWarningParameter parameter : requestWarning.getParameters()) {
            parameter.setRequestWarningId(requestWarning.getId());
            sqlSession().insert(MAPPING_NAMESPACE + ".insertParameter", parameter);
        }
    }

    @Transactional
    public List<RequestWarning> getWarnings(long requestId, RequestFile.TYPE requestFileType) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("requestId", requestId);
        params.put("requestFileType", requestFileType);
        List<RequestWarning> warnings = sqlSession().selectList(MAPPING_NAMESPACE + ".getWarnings", params);
        return warnings;
    }

    public void delete(long requestFileId, RequestFile.TYPE requestFileType) {
        List<Long> warningsIds = getWarningIdsByFile(requestFileType, requestFileId);
        for (Long warningId : warningsIds) {
            sqlSession().delete(MAPPING_NAMESPACE + ".deleteParameter", warningId);
            sqlSession().delete(MAPPING_NAMESPACE + ".deleteWarning", warningId);
        }
    }

    /**
     * Helper methods
     */
    @Transactional
    public void save(RequestFile.TYPE requestFileType, long requestId, RequestWarningStatus warningStatus, RequestWarningParameter... parameters) {
        RequestWarning warning = new RequestWarning(requestId, requestFileType, warningStatus);
        if (parameters != null) {
            warning.setParameters(Lists.newArrayList(parameters));
        }
        save(warning);
    }

    @Transactional
    protected List<Long> getWarningIdsByFile(RequestFile.TYPE requestFileType, long requestFileId) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("requestFileId", requestFileId);
        params.put("requestFileType", requestFileType);
        params.put("requestTableName", requestFileType.name().toLowerCase());
        return sqlSession().selectList(MAPPING_NAMESPACE + ".getWarningIdsByFile", params);
    }
}
