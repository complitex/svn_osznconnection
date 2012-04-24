package org.complitex.osznconnection.file.service;

import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.AbstractBean;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestFileFilter;
import org.complitex.osznconnection.file.entity.RequestFileStatus;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.08.2010 12:15:53
 * 
 * Работа с базой данных для файла запроса.
 * Поиск, сохранение, обновление, удаление, проверка на наличие в базе.
 * Изменение статуса при отмене процесса загрузки и сохранения.
 *
 * @see org.complitex.osznconnection.file.entity.RequestFile
 */
@Stateless(name = "RequestFileBean")
public class RequestFileBean extends AbstractBean {

    public static final String MAPPING_NAMESPACE = RequestFileBean.class.getName();
    @EJB
    private OsznSessionBean osznSessionBean;
    @EJB
    private TarifBean tarifBean;
    @EJB
    private PaymentBean paymentBean;
    @EJB
    private BenefitBean benefitBean;
    @EJB
    private ActualPaymentBean actualPaymentBean;
    @EJB
    private SubsidyBean subsidyBean;

    public RequestFile findById(long fileId) {
        return (RequestFile) sqlSession().selectOne(MAPPING_NAMESPACE + ".findById", fileId);
    }

    @SuppressWarnings({"unchecked"})
    public List<RequestFile> getRequestFiles(RequestFileFilter filter) {
        osznSessionBean.prepareFilterForPermissionCheck(filter);

        switch (filter.getType()) {
            case ACTUAL_PAYMENT:
                return getActualPaymentFiles(filter);
            case SUBSIDY:
                return getSubsidyFiles(filter);
            case TARIF:
                return getTarifFiles(filter);
        }
        throw new IllegalStateException("Unexpected request file type detected: '" + filter.getType() + "'.");
    }

    @SuppressWarnings("unchecked")
    private List<RequestFile> getActualPaymentFiles(RequestFileFilter filter) {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".selectActualPaymentFiles", filter);
    }

    @SuppressWarnings("unchecked")
    private List<RequestFile> getSubsidyFiles(RequestFileFilter filter) {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".selectSubsidyFiles", filter);
    }

    @SuppressWarnings("unchecked")
    private List<RequestFile> getTarifFiles(RequestFileFilter filter) {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".selectTarifFiles", filter);
    }

    public int size(RequestFileFilter filter) {
        osznSessionBean.prepareFilterForPermissionCheck(filter);
        return (Integer) sqlSession().selectOne(MAPPING_NAMESPACE + ".selectRequestFilesCount", filter);
    }

    public void save(RequestFile requestFile) {
        if (requestFile.getId() == null) {
            sqlSession().insert(MAPPING_NAMESPACE + ".insertRequestFile", requestFile);
        } else {
            sqlSession().update(MAPPING_NAMESPACE + ".updateRequestFile", requestFile);
        }
    }

    @Transactional
    public void delete(RequestFile requestFile) {
        if (requestFile.getType() != null && requestFile.getId() != null) {
            switch (requestFile.getType()) {
                case BENEFIT:
                    benefitBean.delete(requestFile.getId());
                    break;
                case PAYMENT:
                    paymentBean.delete(requestFile.getId());
                    break;
                case TARIF:
                    tarifBean.delete(requestFile.getId());
                    break;
                case ACTUAL_PAYMENT:
                    actualPaymentBean.delete(requestFile.getId());
                    break;
                case SUBSIDY:
                    subsidyBean.delete(requestFile.getId());
                    break;
            }
        }
        sqlSession().delete(MAPPING_NAMESPACE + ".deleteRequestFile", requestFile.getId());
    }

    public boolean checkLoaded(RequestFile requestFile) {
        Long id = (Long) sqlSession().selectOne(MAPPING_NAMESPACE + ".selectLoadedId", requestFile);

        if (id != null) {
            requestFile.setId(id);
            return true;
        }

        return false;
    }

    @Transactional
    @SuppressWarnings({"unchecked"})
    public void deleteTarif(Long organizationId) {
        List<RequestFile> tarifs = sqlSession().selectList(MAPPING_NAMESPACE + ".findTarifFiles", organizationId);
        for (RequestFile tarif : tarifs) {
            delete(tarif);
        }
    }

    public RequestFileStatus getRequestFileStatus(RequestFile requestFile) {
        return (RequestFileStatus) sqlSession().selectOne(MAPPING_NAMESPACE + ".selectRequestFileStatus", requestFile);
    }

    public void fixProcessingOnInit() {
        sqlSession().update(MAPPING_NAMESPACE + ".fixLoadingOnInit");
        sqlSession().update(MAPPING_NAMESPACE + ".fixBingingOnInit");
        sqlSession().update(MAPPING_NAMESPACE + ".fixFillingOnInit");
        sqlSession().update(MAPPING_NAMESPACE + ".fixSavingOnInit");
    }
}
