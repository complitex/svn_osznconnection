package org.complitex.osznconnection.file.service;

import com.google.common.collect.ImmutableMap;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.AbstractBean;
import org.complitex.dictionary.service.SessionBean;
import org.complitex.dictionary.service.executor.ExecuteException;
import org.complitex.dictionary.util.DateUtil;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestFileFilter;
import org.complitex.osznconnection.file.entity.RequestFileStatus;
import org.complitex.osznconnection.file.entity.RequestFileType;

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
@Stateless
public class RequestFileBean extends AbstractBean {
    public static final String NS = RequestFileBean.class.getName();

    @EJB
    private SessionBean sessionBean;

    @EJB
    private SubsidyTarifBean subsidyTarifBean;

    @EJB
    private PaymentBean paymentBean;

    @EJB
    private BenefitBean benefitBean;

    @EJB
    private ActualPaymentBean actualPaymentBean;

    @EJB
    private SubsidyBean subsidyBean;

    @EJB
    private DwellingCharacteristicsBean dwellingCharacteristicsBean;

    @EJB
    private FacilityServiceTypeBean facilityServiceTypeBean;

    @EJB
    private FacilityForm2Bean facilityForm2Bean;

    @EJB
    private FacilityReferenceBookBean facilityReferenceBookBean;

    public RequestFile findById(long fileId) {
        return sqlSession().selectOne(NS + ".findById", fileId);
    }

    public List<RequestFile> getRequestFiles(RequestFileFilter filter) {
        sessionBean.prepareFilterForPermissionCheck(filter);

        switch (filter.getType()) {
            case ACTUAL_PAYMENT:
                return getActualPaymentFiles(filter);
            case SUBSIDY:
                return getSubsidyFiles(filter);
            case SUBSIDY_TARIF:
                return getSubsidyTarifFiles(filter);
            case DWELLING_CHARACTERISTICS:
                return getDwellingCharacteristicsFiles(filter);
            case FACILITY_SERVICE_TYPE:
                return getFacilityServiceTypeFiles(filter);
            case FACILITY_FORM2:
                return getFacilityForm2Files(filter);
            case FACILITY_STREET_TYPE:
                return getFacilityStreetTypeFiles(filter);
            case FACILITY_STREET:
                return getFacilityStreetFiles(filter);
            case FACILITY_TARIF:
                return getFacilityTarifFiles(filter);
        }
        throw new IllegalStateException("Unexpected request file type detected: '" + filter.getType() + "'.");
    }

    private List<RequestFile> getActualPaymentFiles(RequestFileFilter filter) {
        return sqlSession().selectList(NS + ".selectActualPaymentFiles", filter);
    }

    private List<RequestFile> getSubsidyFiles(RequestFileFilter filter) {
        return sqlSession().selectList(NS + ".selectSubsidyFiles", filter);
    }

    private List<RequestFile> getDwellingCharacteristicsFiles(RequestFileFilter filter) {
        return sqlSession().selectList(NS + ".selectDwellingCharacteristicsFiles", filter);
    }

    private List<RequestFile> getFacilityServiceTypeFiles(RequestFileFilter filter) {
        return sqlSession().selectList(NS + ".selectFacilityServiceTypeFiles", filter);
    }

    private List<RequestFile> getFacilityForm2Files(RequestFileFilter filter) {
        return sqlSession().selectList(NS + ".selectFacilityForm2Files", filter);
    }

    private List<RequestFile> getFacilityStreetTypeFiles(RequestFileFilter filter) {
        return sqlSession().selectList(NS + ".selectFacilityStreetTypeFiles", filter);
    }

    private List<RequestFile> getFacilityStreetFiles(RequestFileFilter filter) {
        return sqlSession().selectList(NS + ".selectFacilityStreetFiles", filter);
    }

    private List<RequestFile> getFacilityTarifFiles(RequestFileFilter filter) {
        return sqlSession().selectList(NS + ".selectFacilityTarifFiles", filter);
    }

    private List<RequestFile> getSubsidyTarifFiles(RequestFileFilter filter) {
        return sqlSession().selectList(NS + ".selectSubsidyTarifFiles", filter);
    }

    public int size(RequestFileFilter filter) {
        sessionBean.prepareFilterForPermissionCheck(filter);
        return sqlSession().selectOne(NS + ".selectRequestFilesCount", filter);
    }

    @Transactional
    public void save(RequestFile requestFile) {
        if (requestFile.getId() == null) {
            sqlSession().insert(NS + ".insertRequestFile", requestFile);
        } else {
            sqlSession().update(NS + ".updateRequestFile", requestFile);
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
                case SUBSIDY_TARIF:
                    subsidyTarifBean.delete(requestFile.getId());
                    break;
                case ACTUAL_PAYMENT:
                    actualPaymentBean.delete(requestFile.getId());
                    break;
                case SUBSIDY:
                    subsidyBean.delete(requestFile.getId());
                    break;
                case DWELLING_CHARACTERISTICS:
                    dwellingCharacteristicsBean.delete(requestFile.getId());
                    break;
                case FACILITY_SERVICE_TYPE:
                    facilityServiceTypeBean.delete(requestFile.getId());
                    break;
                case FACILITY_FORM2:
                    facilityForm2Bean.delete(requestFile.getId());
                    break;
                case FACILITY_STREET_TYPE:
                case FACILITY_STREET:
                case FACILITY_TARIF:
                    facilityReferenceBookBean.delete(requestFile.getId(), requestFile.getType());
                    break;
            }
        }
        sqlSession().delete(NS + ".deleteRequestFile", requestFile.getId());
    }

    public Long getLoadedId(RequestFile requestFile) {
        return sqlSession().selectOne(NS + ".selectLoadedId", requestFile);
    }

    public boolean checkLoaded(RequestFile requestFile) {
        return sqlSession().selectOne(NS + ".selectIsLoaded", requestFile);
    }

    @Transactional
    public void deleteSubsidyTarifFiles(Long organizationId) {
        List<RequestFile> subsidyTarifs = sqlSession().selectList(NS + ".findSubsidyTarifFiles", organizationId);
        for (RequestFile subsidyTarif : subsidyTarifs) {
            delete(subsidyTarif);
        }
    }

    @Transactional
    public void deleteFacilityReferenceFiles(long osznId, long userOrganizationId, RequestFileType requestFileType) {
        List<RequestFile> facilityReferenceFiles = sqlSession().selectList(NS + ".getFacilityReferenceFiles",
                ImmutableMap.of("osznId", osznId, "userOrganizationId", userOrganizationId,
                        "requestFileType", requestFileType.name()));
        for (RequestFile facilityReferenceFile : facilityReferenceFiles) {
            delete(facilityReferenceFile);
        }
    }

    public RequestFileStatus getRequestFileStatus(RequestFile requestFile) {
        return sqlSession().selectOne(NS + ".selectRequestFileStatus", requestFile);
    }

    public void fixProcessingOnInit() {
        sqlSession().update(NS + ".fixLoadingOnInit");
        sqlSession().update(NS + ".fixBingingOnInit");
        sqlSession().update(NS + ".fixFillingOnInit");
        sqlSession().update(NS + ".fixSavingOnInit");
    }

    public RequestFile getLastRequestFile(RequestFile requestFile){
        return sqlSession().selectOne(NS + ".selectLastRequestFile", requestFile);
    }

    public RequestFile getFirstRequestFile(RequestFile requestFile){
        return sqlSession().selectOne(NS + ".selectFirstRequestFile", requestFile);
    }

    public void updateDateRange(RequestFile requestFile) throws ExecuteException {
        RequestFile last = getLastRequestFile(requestFile);

        if (last != null){
            last.setEndDate(requestFile.getBeginDate());

            save(last);
        }else {
            RequestFile first = getFirstRequestFile(requestFile);

            if (first != null){
                requestFile.setEndDate(first.getBeginDate());
            } else if (checkLoaded(requestFile)){
                throw new ExecuteException("Файл {0} за месяц {1} уже загружен", requestFile.getFullName(), DateUtil.format(requestFile.getBeginDate()));
            }
        }
    }
}
