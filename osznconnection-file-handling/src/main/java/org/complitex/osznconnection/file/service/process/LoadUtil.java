package org.complitex.osznconnection.file.service.process;

import org.complitex.dictionary.service.ConfigBean;
import org.complitex.dictionary.util.EjbBeanLocator;
import org.complitex.osznconnection.file.entity.FileHandlingConfig;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestFileGroup;
import org.complitex.osznconnection.file.service.exception.StorageNotFoundException;
import org.complitex.osznconnection.file.service.process.RequestFileStorage.RequestFiles;

import java.io.File;
import java.io.FileFilter;
import java.util.*;
import java.util.regex.Pattern;

import static org.complitex.dictionary.util.DateUtil.newDate;
import static org.complitex.osznconnection.file.entity.FileHandlingConfig.*;
import static org.complitex.osznconnection.file.service.process.RequestFileDirectoryType.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 01.11.10 12:57
 */
public class LoadUtil {

    private LoadUtil() {
    }

    public static class LoadGroupParameter {

        List<RequestFileGroup> requestFileGroups;
        List<RequestFile> linkError;

        public LoadGroupParameter(List<RequestFileGroup> requestFileGroups, List<RequestFile> linkError) {
            this.requestFileGroups = requestFileGroups;
            this.linkError = linkError;
        }

        public List<RequestFileGroup> getRequestFileGroups() {
            return requestFileGroups;
        }

        public List<RequestFile> getLinkError() {
            return linkError;
        }
    }

    private static String getConfigString(FileHandlingConfig config) {
        return EjbBeanLocator.getBean(ConfigBean.class).getString(config, true);
    }

    private static String getPattern(String mask, int month, int year) {
        return mask.replace("{MM}", (month <= 9 ? "0" + month : "" + month)).replace("{YY}",
                String.valueOf(year).substring(2, 4)).replace("{YYYY}", "" + year);
    }

    private static boolean isMatches(FileHandlingConfig config, String name) {
        return Pattern.compile(getConfigString(config), Pattern.CASE_INSENSITIVE).matcher(name).matches();
    }

    private static boolean isMatches(FileHandlingConfig config, String name, int month, int year) {
        return Pattern.compile(getPattern(getConfigString(config), month, year),
                Pattern.CASE_INSENSITIVE).matcher(name).matches();
    }

    private static boolean isMatches(FileHandlingConfig prefix, FileHandlingConfig suffix, String name, int month, int year) {
        return Pattern.compile(getConfigString(prefix) + getPattern(getConfigString(suffix), month, year),
                Pattern.CASE_INSENSITIVE).matcher(name).matches();
    }

    private static RequestFiles getInputSubsidyTarifFiles(long userOrganizationId, long osznId, 
            final FileHandlingConfig mask) throws StorageNotFoundException {
        return RequestFileStorage.INSTANCE.getInputRequestFiles(userOrganizationId, osznId, REFERENCES_DIR,
                new FileFilter() {

                    @Override
                    public boolean accept(File file) {
                        return isMatches(mask, file.getName());
                    }
                });
    }

    private static RequestFiles getInputFacilityReferenceFiles(long userOrganizationId, long osznId,
            final FileHandlingConfig mask) throws StorageNotFoundException {
        return RequestFileStorage.INSTANCE.getInputRequestFiles(userOrganizationId, osznId, REFERENCES_DIR,
                new FileFilter() {

                    @Override
                    public boolean accept(File file) {
                        return isMatches(mask, file.getName());
                    }
                });
    }

    private static RequestFiles getInputPaymentBenefitFiles(long userOrganizationId, long osznId, final FileHandlingConfig prefix,
            final FileHandlingConfig suffix, final int month, final int year)
            throws StorageNotFoundException {
        return RequestFileStorage.INSTANCE.getInputRequestFiles(userOrganizationId, osznId, LOAD_PAYMENT_BENEFIT_FILES_DIR,
                new FileFilter() {

                    @Override
                    public boolean accept(File file) {
                        return isMatches(prefix, suffix, file.getName(), month, year);
                    }
                });
    }

    private static RequestFiles getInputActualPaymentFiles(long userOrganizationId, long osznId, final FileHandlingConfig mask,
            final int month, final int year) throws StorageNotFoundException {
        return RequestFileStorage.INSTANCE.getInputRequestFiles(userOrganizationId, osznId, LOAD_ACTUAL_PAYMENT_DIR,
                new FileFilter() {

                    @Override
                    public boolean accept(File file) {
                        return isMatches(mask, file.getName(), month, year);
                    }
                });
    }

    private static RequestFiles getInputSubsidyFiles(long userOrganizationId, long osznId, final FileHandlingConfig mask,
            final int month, final int year) throws StorageNotFoundException {
        return RequestFileStorage.INSTANCE.getInputRequestFiles(userOrganizationId, osznId, LOAD_SUBSIDY_DIR,
                new FileFilter() {

                    @Override
                    public boolean accept(File file) {
                        return isMatches(mask, file.getName(), month, year);
                    }
                });
    }

    private static RequestFiles getInputDwellingCharacteristicsFiles(long userOrganizationId, long osznId,
            final FileHandlingConfig mask) throws StorageNotFoundException {
        return RequestFileStorage.INSTANCE.getInputRequestFiles(userOrganizationId, osznId, LOAD_DWELLING_CHARACTERISTICS_DIR,
                new FileFilter() {

                    @Override
                    public boolean accept(File file) {
                        return isMatches(mask, file.getName());
                    }
                });
    }

    private static RequestFiles getInputFacilityServiceTypeFiles(long userOrganizationId, long osznId,
            final FileHandlingConfig mask) throws StorageNotFoundException {
        return RequestFileStorage.INSTANCE.getInputRequestFiles(userOrganizationId, osznId, LOAD_FACILITY_SERVICE_TYPE_DIR,
                new FileFilter() {

                    @Override
                    public boolean accept(File file) {
                        return isMatches(mask, file.getName());
                    }
                });
    }

    private static String getSuffix(String name, FileHandlingConfig prefix) {
        return name.substring(getConfigString(prefix).length()).toLowerCase();
    }

    private static RequestFile newPaymentBenefitRequestFile(File file, RequestFile.TYPE type, String relativeDirectory,
            long osznId, Date beginDate, Date endDate) {
        RequestFile requestFile = new RequestFile();

        requestFile.setName(file.getName());
        requestFile.setType(type);
        requestFile.setDirectory(relativeDirectory);
        requestFile.setLength(file.length());
        requestFile.setAbsolutePath(file.getAbsolutePath());
        requestFile.setOrganizationId(osznId);
        requestFile.setBeginDate(beginDate);
        requestFile.setEndDate(endDate);
        return requestFile;
    }

    public static LoadGroupParameter getLoadGroupParameter(long userOrganizationId, long osznId,
            int monthFrom, int monthTo, int year) throws StorageNotFoundException {

        Map<String, Map<String, RequestFileGroup>> requestFileGroupsMap = new HashMap<String, Map<String, RequestFileGroup>>();

        //payment
        for (int month = monthFrom; month <= monthTo; ++month) {
            RequestFiles requestFiles = getInputPaymentBenefitFiles(userOrganizationId, osznId, PAYMENT_FILENAME_PREFIX,
                    PAYMENT_BENEFIT_FILENAME_SUFFIX, month, year);

            List<File> payments = requestFiles.getFiles();
            for (int i = 0; i < payments.size(); ++i) {
                File file = payments.get(i);

                RequestFileGroup group = new RequestFileGroup();

                group.setPaymentFile(newPaymentBenefitRequestFile(file, RequestFile.TYPE.PAYMENT,
                        RequestFileStorage.INSTANCE.getRelativeParent(file, requestFiles.getPath()),
                        osznId, newDate(year, month), null));

                payments.remove(i);
                i--;

                Map<String, RequestFileGroup> map = requestFileGroupsMap.get(file.getParent());

                if (map == null) {
                    map = new HashMap<>();
                    requestFileGroupsMap.put(file.getParent(), map);
                }

                map.put(getSuffix(file.getName(), PAYMENT_FILENAME_PREFIX), group);
            }
        }

        List<RequestFile> linkError = new ArrayList<RequestFile>();

        //benefit
        for (int month = monthFrom; month <= monthTo; ++month) {
            RequestFiles requestFiles = getInputPaymentBenefitFiles(userOrganizationId, osznId, BENEFIT_FILENAME_PREFIX,
                    PAYMENT_BENEFIT_FILENAME_SUFFIX, month, year);

            List<File> benefits = requestFiles.getFiles();
            for (File file : benefits) {
                RequestFile requestFile = newPaymentBenefitRequestFile(file, RequestFile.TYPE.BENEFIT,
                        RequestFileStorage.INSTANCE.getRelativeParent(file, requestFiles.getPath()),
                        osznId, newDate(year, month), null);

                Map<String, RequestFileGroup> map = requestFileGroupsMap.get(file.getParent());

                if (map != null) {
                    RequestFileGroup group = map.get(getSuffix(file.getName(), BENEFIT_FILENAME_PREFIX));

                    if (group != null) {
                        group.setBenefitFile(requestFile);
                        continue;
                    }
                }

                linkError.add(requestFile);
            }
        }

        List<RequestFileGroup> requestFileGroups = new ArrayList<RequestFileGroup>();

        for (Map<String, RequestFileGroup> map : requestFileGroupsMap.values()) {
            for (RequestFileGroup group : map.values()) {
                if (group.getBenefitFile() != null) {
                    requestFileGroups.add(group);
                } else {
                    RequestFile payment = group.getPaymentFile();

                    linkError.add(payment);
                }
            }
        }

        return new LoadGroupParameter(requestFileGroups, linkError);
    }

    public static List<RequestFile> getSubsidyTarifs(long userOrganizationId, long osznId, int month, int year)
            throws StorageNotFoundException {
        List<RequestFile> subsidyTarifs = new ArrayList<>();

        RequestFiles requestFiles = getInputSubsidyTarifFiles(userOrganizationId, osznId, SUBSIDY_TARIF_FILENAME_MASK);

        List<File> files = requestFiles.getFiles();
        for (File file : files) {
            //fill fields
            RequestFile requestFile = new RequestFile();

            requestFile.setName(file.getName());
            requestFile.setLength(file.length());
            requestFile.setAbsolutePath(file.getAbsolutePath());
            requestFile.setDirectory(RequestFileStorage.INSTANCE.getRelativeParent(file, requestFiles.getPath()));
            requestFile.setOrganizationId(osznId);
            requestFile.setBeginDate(newDate(year, month));
            requestFile.setType(RequestFile.TYPE.SUBSIDY_TARIF);

            subsidyTarifs.add(requestFile);
        }
        return subsidyTarifs;
    }

    public static List<RequestFile> getActualPayments(long userOrganizationId, long osznId, int monthFrom,
            int monthTo, int year) throws StorageNotFoundException {
        List<RequestFile> actualPayments = new ArrayList<>();

        for (int month = monthFrom; month <= monthTo; ++month) {
            RequestFiles requestFiles = getInputActualPaymentFiles(userOrganizationId, osznId, ACTUAL_PAYMENT_FILENAME_MASK, month, year);

            List<File> files = requestFiles.getFiles();
            for (File file : files) {
                //fill fields
                RequestFile requestFile = new RequestFile();

                requestFile.setName(file.getName());
                requestFile.setLength(file.length());
                requestFile.setAbsolutePath(file.getAbsolutePath());
                requestFile.setDirectory(RequestFileStorage.INSTANCE.getRelativeParent(file, requestFiles.getPath()));
                requestFile.setOrganizationId(osznId);
                requestFile.setBeginDate(newDate(year, month));
                requestFile.setType(RequestFile.TYPE.ACTUAL_PAYMENT);

                actualPayments.add(requestFile);
            }
        }
        return actualPayments;
    }

    public static List<RequestFile> getSubsidies(long userOrganizationId, long osznId, int monthFrom,
            int monthTo, int year) throws StorageNotFoundException {
        List<RequestFile> subsidies = new ArrayList<>();

        for (int month = monthFrom; month <= monthTo; ++month) {
            RequestFiles requestFiles = getInputSubsidyFiles(userOrganizationId, osznId, SUBSIDY_FILENAME_MASK, month, year);

            List<File> files = requestFiles.getFiles();
            for (File file : files) {
                //fill fields
                RequestFile requestFile = new RequestFile();

                requestFile.setName(file.getName());
                requestFile.setLength(file.length());
                requestFile.setAbsolutePath(file.getAbsolutePath());
                requestFile.setDirectory(RequestFileStorage.INSTANCE.getRelativeParent(file, requestFiles.getPath()));
                requestFile.setOrganizationId(osznId);
                requestFile.setBeginDate(newDate(year, month));
                requestFile.setType(RequestFile.TYPE.SUBSIDY);

                subsidies.add(requestFile);
            }
        }
        return subsidies;
    }

    public static List<RequestFile> getDwellingCharacteristics(long userOrganizationId, long osznId, int month, int year)
            throws StorageNotFoundException {
        List<RequestFile> dwellingCharacteristicsFiles = new ArrayList<RequestFile>();

        RequestFiles requestFiles = getInputDwellingCharacteristicsFiles(userOrganizationId, osznId,
                DWELLING_CHARACTERISTICS_INPUT_FILENAME_MASK);

        List<File> files = requestFiles.getFiles();
        for (File file : files) {
            //fill fields
            RequestFile requestFile = new RequestFile();

            requestFile.setName(file.getName());
            requestFile.setLength(file.length());
            requestFile.setAbsolutePath(file.getAbsolutePath());
            requestFile.setDirectory(RequestFileStorage.INSTANCE.getRelativeParent(file, requestFiles.getPath()));
            requestFile.setOrganizationId(osznId);
            requestFile.setBeginDate(newDate(year, month));
            requestFile.setType(RequestFile.TYPE.DWELLING_CHARACTERISTICS);

            dwellingCharacteristicsFiles.add(requestFile);
        }
        return dwellingCharacteristicsFiles;
    }

    public static List<RequestFile> getFacilityServiceTypes(long userOrganizationId, long osznId, int month, int year)
            throws StorageNotFoundException {
        List<RequestFile> facilityServiceTypeFiles = new ArrayList<>();

        RequestFiles requestFiles = getInputFacilityServiceTypeFiles(userOrganizationId, osznId,
                FACILITY_SERVICE_TYPE_INPUT_FILENAME_MASK);

        List<File> files = requestFiles.getFiles();
        for (File file : files) {
            //fill fields
            RequestFile requestFile = new RequestFile();

            requestFile.setName(file.getName());
            requestFile.setLength(file.length());
            requestFile.setAbsolutePath(file.getAbsolutePath());
            requestFile.setDirectory(RequestFileStorage.INSTANCE.getRelativeParent(file, requestFiles.getPath()));
            requestFile.setOrganizationId(osznId);
            requestFile.setBeginDate(newDate(year, month));
            requestFile.setType(RequestFile.TYPE.FACILITY_SERVICE_TYPE);

            facilityServiceTypeFiles.add(requestFile);
        }
        return facilityServiceTypeFiles;
    }

    public static List<RequestFile> getFacilityStreetTypeReferences(long userOrganizationId, long osznId, int month, int year)
            throws StorageNotFoundException {
        List<RequestFile> streetTypeFiles = new ArrayList<>();

        RequestFiles requestFiles = getInputFacilityReferenceFiles(userOrganizationId, osznId,
                FACILITY_STREET_TYPE_REFERENCE_FILENAME_MASK);

        List<File> files = requestFiles.getFiles();
        for (File file : files) {
            //fill fields
            RequestFile requestFile = new RequestFile();

            requestFile.setName(file.getName());
            requestFile.setLength(file.length());
            requestFile.setAbsolutePath(file.getAbsolutePath());
            requestFile.setDirectory(RequestFileStorage.INSTANCE.getRelativeParent(file, requestFiles.getPath()));
            requestFile.setOrganizationId(osznId);
            requestFile.setBeginDate(newDate(year, month));
            requestFile.setType(RequestFile.TYPE.FACILITY_STREET_TYPE);
            streetTypeFiles.add(requestFile);
        }
        return streetTypeFiles;
    }

    public static List<RequestFile> getFacilityStreetReferences(long userOrganizationId, long osznId, int month, int year)
            throws StorageNotFoundException {
        List<RequestFile> streetFiles = new ArrayList<>();

        RequestFiles requestFiles = getInputFacilityReferenceFiles(userOrganizationId, osznId,
                FACILITY_STREET_REFERENCE_FILENAME_MASK);

        List<File> files = requestFiles.getFiles();
        for (File file : files) {
            //fill fields
            RequestFile requestFile = new RequestFile();

            requestFile.setName(file.getName());
            requestFile.setLength(file.length());
            requestFile.setAbsolutePath(file.getAbsolutePath());
            requestFile.setDirectory(RequestFileStorage.INSTANCE.getRelativeParent(file, requestFiles.getPath()));
            requestFile.setOrganizationId(osznId);
            requestFile.setBeginDate(newDate(year, month));
            requestFile.setType(RequestFile.TYPE.FACILITY_STREET);
            streetFiles.add(requestFile);
        }
        return streetFiles;
    }

    public static List<RequestFile> getFacilityTarifReferences(long userOrganizationId, long osznId, int month, int year)
            throws StorageNotFoundException {
        List<RequestFile> facilityTarifFiles = new ArrayList<>();

        RequestFiles requestFiles = getInputFacilityReferenceFiles(userOrganizationId, osznId,
                FACILITY_TARIF_REFERENCE_FILENAME_MASK);

        List<File> files = requestFiles.getFiles();
        for (File file : files) {
            //fill fields
            RequestFile requestFile = new RequestFile();

            requestFile.setName(file.getName());
            requestFile.setLength(file.length());
            requestFile.setAbsolutePath(file.getAbsolutePath());
            requestFile.setDirectory(RequestFileStorage.INSTANCE.getRelativeParent(file, requestFiles.getPath()));
            requestFile.setOrganizationId(osznId);
            requestFile.setBeginDate(newDate(year, month));
            requestFile.setType(RequestFile.TYPE.FACILITY_TARIF);
            facilityTarifFiles.add(requestFile);
        }
        return facilityTarifFiles;
    }
}
