package org.complitex.osznconnection.file.service.process;

import org.complitex.dictionary.service.ConfigBean;
import org.complitex.dictionary.util.EjbBeanLocator;
import org.complitex.osznconnection.file.entity.FileHandlingConfig;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.RequestFileGroup;
import org.complitex.osznconnection.file.service.exception.StorageNotFoundException;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.complitex.osznconnection.file.entity.FileHandlingConfig.*;

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

    private static boolean isMatches(FileHandlingConfig config, String name, int month, int year) {
        return Pattern.compile(getPattern(getConfigString(config), month, year),
                Pattern.CASE_INSENSITIVE).matcher(name).matches();

    }

    private static boolean isMatches(FileHandlingConfig prefix, FileHandlingConfig suffix, String name, int month, int year) {
        return Pattern.compile(getConfigString(prefix) + getPattern(getConfigString(suffix), month, year),
                Pattern.CASE_INSENSITIVE).matcher(name).matches();

    }

    private static List<File> getInputTarifFiles(final String districtDir, final int month,
            final int year) throws StorageNotFoundException {
        return RequestFileStorage.INSTANCE.getInputTarifFiles(districtDir, new FileFilter() {

            @Override
            public boolean accept(File file) {
                return file.isDirectory() || isMatches(TARIF_PAYMENT_FILENAME_MASK, file.getName(), month, year);
            }
        });
    }

    private static List<File> getInputPaymentBenefitFiles(long userOrganizationId, final FileHandlingConfig prefix,
            final FileHandlingConfig suffix, final String districtCodeDir, final int month, final int year)
            throws StorageNotFoundException {
        return RequestFileStorage.INSTANCE.getInputRequestFiles(userOrganizationId, districtCodeDir,
                DEFAULT_LOAD_PAYMENT_BENEFIT_FILES_DIR, new FileFilter() {

            @Override
            public boolean accept(File file) {
                return file.isDirectory() || isMatches(prefix, suffix, file.getName(), month, year);
            }
        });
    }

    private static List<File> getInputActualPaymentFiles(long userOrganizationId, final FileHandlingConfig mask,
            String districtCodeDir, final int month, final int year) throws StorageNotFoundException {
        return RequestFileStorage.INSTANCE.getInputRequestFiles(userOrganizationId, districtCodeDir, DEFAULT_LOAD_ACTUAL_PAYMENT_DIR,
                new FileFilter() {

                    @Override
                    public boolean accept(File file) {
                        return file.isDirectory() || isMatches(mask, file.getName(), month, year);
                    }
                });
    }

    private static List<File> getInputSubsidyFiles(long userOrganizationId, final FileHandlingConfig mask, final String districtCodeDir,
            final int month, final int year) throws StorageNotFoundException {
        return RequestFileStorage.INSTANCE.getInputRequestFiles(userOrganizationId, districtCodeDir, DEFAULT_LOAD_SUBSIDY_DIR,
                new FileFilter() {

                    @Override
                    public boolean accept(File file) {
                        return file.isDirectory() || isMatches(mask, file.getName(), month, year);
                    }
                });
    }

    private static String getSuffix(String name, FileHandlingConfig prefix) {
        return name.substring(getConfigString(prefix).length()).toLowerCase();
    }

    private static RequestFile newPaymentBenefitRequestFile(File file, RequestFile.TYPE type,
            Long osznId, int month, int year) {
        RequestFile requestFile = new RequestFile();

        requestFile.setName(file.getName());
        requestFile.setType(type);
        requestFile.setDirectory(RequestFileStorage.INSTANCE.getRelativeParent(file,
                RequestFileStorage.INSTANCE.getRequestFilesStorageDir(osznId, DEFAULT_LOAD_PAYMENT_BENEFIT_FILES_DIR)));
        requestFile.setLength(file.length());
        requestFile.setAbsolutePath(file.getAbsolutePath());
        requestFile.setOrganizationId(osznId);
        requestFile.setMonth(month);
        requestFile.setYear(year);
        return requestFile;
    }

    public static LoadGroupParameter getLoadGroupParameter(long userOrganizationId, long osznId, String districtCode,
            int monthFrom, int monthTo, int year) throws StorageNotFoundException {

        Map<String, Map<String, RequestFileGroup>> requestFileGroupsMap = new HashMap<String, Map<String, RequestFileGroup>>();

        //payment
        for (int month = monthFrom; month <= monthTo; ++month) {
            List<File> payments = getInputPaymentBenefitFiles(userOrganizationId, PAYMENT_FILENAME_PREFIX,
                    PAYMENT_BENEFIT_FILENAME_SUFFIX, districtCode,
                    month, year);

            for (int i = 0; i < payments.size(); ++i) {
                File file = payments.get(i);

                RequestFileGroup group = new RequestFileGroup();

                group.setPaymentFile(newPaymentBenefitRequestFile(file, RequestFile.TYPE.PAYMENT, osznId, month, year));

                payments.remove(i);
                i--;

                Map<String, RequestFileGroup> map = requestFileGroupsMap.get(file.getParent());

                if (map == null) {
                    map = new HashMap<String, RequestFileGroup>();
                    requestFileGroupsMap.put(file.getParent(), map);
                }

                map.put(getSuffix(file.getName(), PAYMENT_FILENAME_PREFIX), group);
            }
        }

        List<RequestFile> linkError = new ArrayList<RequestFile>();

        //benefit
        for (int month = monthFrom; month <= monthTo; ++month) {
            List<File> benefits = getInputPaymentBenefitFiles(userOrganizationId, BENEFIT_FILENAME_PREFIX,
                    PAYMENT_BENEFIT_FILENAME_SUFFIX, districtCode,
                    month, year);

            for (File file : benefits) {
                RequestFile requestFile = newPaymentBenefitRequestFile(file, RequestFile.TYPE.BENEFIT, osznId, month, year);

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

    public static List<RequestFile> getTarifs(long osznId, String districtCode, int monthFrom, int monthTo, int year)
            throws StorageNotFoundException {
        final String inputTarifDir = getConfigString(LOAD_TARIF_DIR);

        List<RequestFile> tarifs = new ArrayList<RequestFile>();

        for (int month = monthFrom; month <= monthTo; ++month) {
            List<File> files = getInputTarifFiles(districtCode, month, year);

            for (File file : files) {
                //fill fields
                RequestFile requestFile = new RequestFile();

                requestFile.setName(file.getName());
                requestFile.setLength(file.length());
                requestFile.setAbsolutePath(file.getAbsolutePath());
                requestFile.setDirectory(RequestFileStorage.INSTANCE.getRelativeParent(file, inputTarifDir));
                requestFile.setOrganizationId(osznId);
                requestFile.setYear(year);
                requestFile.setType(RequestFile.TYPE.TARIF);

                tarifs.add(requestFile);
            }
        }
        return tarifs;
    }

    public static List<RequestFile> getActualPayments(long userOrganizationId, long osznId, String districtCode, int monthFrom,
            int monthTo, int year) throws StorageNotFoundException {
        List<RequestFile> actualPayments = new ArrayList<RequestFile>();

        for (int month = monthFrom; month <= monthTo; ++month) {
            List<File> files = getInputActualPaymentFiles(userOrganizationId, ACTUAL_PAYMENT_FILENAME_MASK, districtCode, month, year);

            for (File file : files) {
                //fill fields
                RequestFile requestFile = new RequestFile();

                requestFile.setName(file.getName());
                requestFile.setLength(file.length());
                requestFile.setAbsolutePath(file.getAbsolutePath());
                requestFile.setDirectory(RequestFileStorage.INSTANCE.getRelativeParent(file,
                        RequestFileStorage.INSTANCE.getRequestFilesStorageDir(userOrganizationId, DEFAULT_LOAD_ACTUAL_PAYMENT_DIR)));
                requestFile.setOrganizationId(osznId);
                requestFile.setMonth(month);
                requestFile.setYear(year);
                requestFile.setType(RequestFile.TYPE.ACTUAL_PAYMENT);

                actualPayments.add(requestFile);
            }
        }
        return actualPayments;
    }

    public static List<RequestFile> getSubsidies(long userOrganizationId, long osznId, String districtCode, int monthFrom,
            int monthTo, int year) throws StorageNotFoundException {
        List<RequestFile> subsidies = new ArrayList<RequestFile>();

        for (int month = monthFrom; month <= monthTo; ++month) {
            List<File> files = getInputSubsidyFiles(userOrganizationId, SUBSIDY_FILENAME_MASK, districtCode, month, year);

            for (File file : files) {
                //fill fields
                RequestFile requestFile = new RequestFile();

                requestFile.setName(file.getName());
                requestFile.setLength(file.length());
                requestFile.setAbsolutePath(file.getAbsolutePath());
                requestFile.setDirectory(RequestFileStorage.INSTANCE.getRelativeParent(file,
                        RequestFileStorage.INSTANCE.getRequestFilesStorageDir(userOrganizationId, DEFAULT_LOAD_SUBSIDY_DIR)));
                requestFile.setOrganizationId(osznId);
                requestFile.setMonth(month);
                requestFile.setYear(year);
                requestFile.setType(RequestFile.TYPE.SUBSIDY);

                subsidies.add(requestFile);
            }
        }
        return subsidies;
    }
}
