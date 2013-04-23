package org.complitex.osznconnection.file.service;

import com.google.common.collect.ImmutableMap;
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
@Stateless
public class RequestFileBean extends AbstractBean {

    public static final String MAPPING_NAMESPACE = RequestFileBean.class.getName();
    @EJB
    private OsznSessionBean osznSessionBean;
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
        return sqlSession().selectOne(MAPPING_NAMESPACE + ".findById", fileId);
    }

    public List<RequestFile> getRequestFiles(RequestFileFilter filter) {
        osznSessionBean.prepareFilterForPermissionCheck(filter);

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
        return sqlSession().selectList(MAPPING_NAMESPACE + ".selectActualPaymentFiles", filter);
    }

    private List<RequestFile> getSubsidyFiles(RequestFileFilter filter) {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".selectSubsidyFiles", filter);
    }

    private List<RequestFile> getDwellingCharacteristicsFiles(RequestFileFilter filter) {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".selectDwellingCharacteristicsFiles", filter);
    }

    private List<RequestFile> getFacilityServiceTypeFiles(RequestFileFilter filter) {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".selectFacilityServiceTypeFiles", filter);
    }

    private List<RequestFile> getFacilityForm2Files(RequestFileFilter filter) {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".selectFacilityForm2Files", filter);
    }

    private List<RequestFile> getFacilityStreetTypeFiles(RequestFileFilter filter) {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".selectFacilityStreetTypeFiles", filter);
    }

    private List<RequestFile> getFacilityStreetFiles(RequestFileFilter filter) {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".selectFacilityStreetFiles", filter);
    }

    private List<RequestFile> getFacilityTarifFiles(RequestFileFilter filter) {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".selectFacilityTarifFiles", filter);
    }

    private List<RequestFile> getSubsidyTarifFiles(RequestFileFilter filter) {
        return sqlSession().selectList(MAPPING_NAMESPACE + ".selectSubsidyTarifFiles", filter);
    }

    public int size(RequestFileFilter filter) {
        osznSessionBean.prepareFilterForPermissionCheck(filter);
        return sqlSession().selectOne(MAPPING_NAMESPACE + ".selectRequestFilesCount", filter);
    }

    @Transactional
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
        sqlSession().delete(MAPPING_NAMESPACE + ".deleteRequestFile", requestFile.getId());
    }

    public boolean checkLoaded(RequestFile requestFile) {
        Long id = sqlSession().selectOne(MAPPING_NAMESPACE + ".selectLoadedId", requestFile);

        if (id != null) {
            requestFile.setId(id);
            return true;
        }

        return false;
    }

    @Transactional
    public void deleteSubsidyTarifFiles(Long organizationId) {
        List<RequestFile> subsidyTarifs = sqlSession().selectList(MAPPING_NAMESPACE + ".findSubsidyTarifFiles", organizationId);
        for (RequestFile subsidyTarif : subsidyTarifs) {
            delete(subsidyTarif);
        }
    }

    @Transactional
    public void deleteFacilityReferenceFiles(long osznId, long userOrganizationId, RequestFile.TYPE requestFileType) {
        List<RequestFile> facilityReferenceFiles = sqlSession().selectList(MAPPING_NAMESPACE + ".getFacilityReferenceFiles",
                ImmutableMap.of("osznId", osznId, "userOrganizationId", userOrganizationId,
                "requestFileType", requestFileType.name()));
        for (RequestFile facilityReferenceFile : facilityReferenceFiles) {
            delete(facilityReferenceFile);
        }
    }

    public RequestFileStatus getRequestFileStatus(RequestFile requestFile) {
        return sqlSession().selectOne(MAPPING_NAMESPACE + ".selectRequestFileStatus", requestFile);
    }

    public void fixProcessingOnInit() {
        sqlSession().update(MAPPING_NAMESPACE + ".fixLoadingOnInit");
        sqlSession().update(MAPPING_NAMESPACE + ".fixBingingOnInit");
        sqlSession().update(MAPPING_NAMESPACE + ".fixFillingOnInit");
        sqlSession().update(MAPPING_NAMESPACE + ".fixSavingOnInit");
    }
}
