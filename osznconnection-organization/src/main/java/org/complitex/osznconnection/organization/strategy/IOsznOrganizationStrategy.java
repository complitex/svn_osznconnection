package org.complitex.osznconnection.organization.strategy;

import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.strategy.organization.IOrganizationStrategy;

import java.util.List;
import java.util.Locale;
import org.complitex.osznconnection.organization.strategy.entity.RemoteDataSource;
import org.complitex.osznconnection.organization.strategy.entity.ServiceAssociationList;

/**
 *
 * @author Artem
 */
public interface IOsznOrganizationStrategy extends IOrganizationStrategy {

    /*
     * Attribute type ids
     */
    /**
     * Reference to jdbc data source. It is calculation center only attribute.
     */
    long DATA_SOURCE = 913;
    /**
     * References to associations between service provider types and calculation centres. It is user organization only attribute.
     */
    long SERVICE_ASSOCIATIONS = 914;
    /**
     * Load payments/benefits directory. It is OSZN only attribute.
     */
    long LOAD_PAYMENT_BENEFIT_FILES_DIR = 915;
    /**
     * Save payments/benefits directory. It is OSZN only attribute.
     */
    long SAVE_PAYMENT_BENEFIT_FILES_DIR = 916;
    /**
     * Load actual payments directory. It is OSZN only attribute.
     */
    long LOAD_ACTUAL_PAYMENT_DIR = 917;
    /**
     * Save actual payments directory. It is OSZN only attribute.
     */
    long SAVE_ACTUAL_PAYMENT_DIR = 918;
    /**
     * Load subsidies directory. It is OSZN only attribute.
     */
    long LOAD_SUBSIDY_DIR = 919;
    /**
     * Save subsidies directory. It is OSZN only attribute.
     */
    long SAVE_SUBSIDY_DIR = 920;
    /**
     * Load dwelling characteristics directory. It is OSZN only attribute.
     */
    long LOAD_DWELLING_CHARACTERISTICS_DIR = 921;
    /**
     * Save dwelling characteristics directory. It is OSZN only attribute.
     */
    long SAVE_DWELLING_CHARACTERISTICS_DIR = 922;
    /**
     * Load facility service type directory. It is OSZN only attribute.
     */
    long LOAD_FACILITY_SERVICE_TYPE_DIR = 923;
    /**
     * Save facility service type directory. It is OSZN only attribute.
     */
    long SAVE_FACILITY_SERVICE_TYPE_DIR = 924;
    /**
     * References directory. It is OSZN only attribute.
     */
    long REFERENCES_DIR = 925;
    /**
     * EDRPOU(ЕДРПОУ). It is user organization only attribute.
     */
    long EDRPOU = 926;
    /**
     * Root directory for loading and saving request files. It is user organization only attribute.
     */
    long ROOT_REQUEST_FILE_DIRECTORY = 927;
    /**
     * Save facility form2 directory. It is OSZN only attribute.
     */
    long SAVE_FACILITY_FORM2_DIR = 928;
    /**
     * Itself organization instance id.
     */
    long ITSELF_ORGANIZATION_OBJECT_ID = 0;

    /**
     * Figures out all outer (OSZNs and calculation centers) organizations visible to current user 
     * and returns them sorted by organization's name in given {@code locale}.
     * 
     * @param locale Locale. It is used in sorting of organizations by name.
     * @return All outer organizations visible to user.
     */
    @Transactional
    List<DomainObject> getAllOuterOrganizations(Locale locale);

    /**
     * Figures out all OSZN organizations visible to current user 
     * and returns them sorted by organization's name in given {@code locale}.
     * 
     * @param locale Locale. It is used in sorting of organizations by name.
     * @return All OSZN organizations.
     */
    @Transactional
    List<DomainObject> getAllOSZNs(Locale locale);

    /**
     * Figures out all calculation center organizations visible to current user 
     * and returns them sorted by organization's name in given {@code locale}.
     * 
     * @param locale Locale. It is used in sorting of organizations by name.
     * @return All calculation center organizations.
     */
    @Transactional
    List<DomainObject> getAllCalculationCentres(Locale locale);

    /**
     * Returns organization that represents osznconnection program module, i.e. "itself".
     * 
     * @return "Itself" organization.
     */
    @Transactional
    DomainObject getItselfOrganization();

    /**
     * Figures out list of service associations. Each service association is link between service provider type and 
     * caluclation center.
     * 
     * @param userOrganization User organization.
     * @return Service associations list.
     */
    ServiceAssociationList getServiceAssociations(DomainObject userOrganization);

    /**
     * Finds remote jdbc data sources.
     * @param currentDataSource Current data source.
     * @return Remote jdbc data sources.
     */
    List<RemoteDataSource> findRemoteDataSources(String currentDataSource);

    /**
     * Figures out data source of calculation center.
     * 
     * @param calculationCenterId Calculation center's id
     * @return Calculation center's data source
     */
    String getDataSource(long calculationCenterId);

    /**
     * Returns relative path to request files storage.
     * @param osznId Oszn's id.
     * @param fileTypeAttributeTypeId Attribute type id corresponding desired file type.
     * @return Relative path to request files storage.
     */
    String getRelativeRequestFilesPath(long osznId, long fileTypeAttributeTypeId);

    /**
     * Returns root directory to request files storage.
     * @param userOrganizationId User organization's id.
     * @return Root directory to request files storage.
     */
    String getRootRequestFilesStoragePath(long userOrganizationId);
}
