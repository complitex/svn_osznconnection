package org.complitex.osznconnection.file.service.process;

import org.complitex.dictionary.entity.IConfig;
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

    public static String getConfigString(IConfig config) {
        return EjbBeanLocator.getBean(ConfigBean.class).getString(config, true);
    }

    private static String getPattern(String mask, int month, int year) {
        return mask.replace("{MM}", (month <= 9 ? "0" + month : "" + month)).replace("{YY}",
                String.valueOf(year).substring(2, 4)).replace("{YYYY}", "" + year);
    }

    private static boolean isMatches(IConfig config, String name, int month, int year) {
        return Pattern.compile(getPattern(getConfigString(config), month, year),
                Pattern.CASE_INSENSITIVE).matcher(name).matches();

    }

    private static boolean isMatches(IConfig prefix, IConfig suffix, String name, int month, int year) {
        return Pattern.compile(getConfigString(prefix) + getPattern(getConfigString(suffix), month, year),
                Pattern.CASE_INSENSITIVE).matcher(name).matches();

    }

    private static List<File> getInputRequestFiles(final String districtDir, final int monthFrom, final int monthTo,
            final int year, final IConfig... filenameMasks) throws StorageNotFoundException {
        return RequestFileStorage.getInstance().getInputRequestFiles(districtDir, new FileFilter() {

            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true;
                }

                for (int m = monthFrom; m <= monthTo; ++m) {
                    for (IConfig c : filenameMasks) {
                        if (isMatches(c, file.getName(), m, year)) {
                            return true;
                        }
                    }
                }

                return false;
            }
        });
    }

    private static List<File> getInputRequestFiles(final IConfig prefix, final IConfig suffix, final String districtDir,
            final int month, final int year) throws StorageNotFoundException {
        return RequestFileStorage.getInstance().getInputRequestFiles(districtDir, new FileFilter() {

            @Override
            public boolean accept(File file) {
                return file.isDirectory() || isMatches(prefix, suffix, file.getName(), month, year);
            }
        });
    }

    private static List<File> getInputActualPaymentFiles(final IConfig mask, final String districtDir,
            final int month, final int year) throws StorageNotFoundException {
        return RequestFileStorage.getInstance().getInputActualPaymentFiles(districtDir, new FileFilter() {

            @Override
            public boolean accept(File file) {
                return file.isDirectory() || isMatches(mask, file.getName(), month, year);
            }
        });
    }

    private static List<File> getInputSubsidyFiles(final IConfig mask, final String districtDir,
            final int month, final int year) throws StorageNotFoundException {
        return RequestFileStorage.getInstance().getInputSubsidyFiles(districtDir, new FileFilter() {

            @Override
            public boolean accept(File file) {
                return file.isDirectory() || isMatches(mask, file.getName(), month, year);
            }
        });
    }

    private static String getSuffix(String name, IConfig prefix) {
        return name.substring(getConfigString(prefix).length()).toLowerCase();
    }

    private static RequestFile newRequestFile(File file, RequestFile.TYPE type, Long organizationId, int month, int year) {
        RequestFile requestFile = new RequestFile();

        requestFile.setName(file.getName());
        requestFile.setType(type);
        requestFile.setDirectory(RequestFileStorage.getInstance().getRelativeParent(file,
                FileHandlingConfig.LOAD_INPUT_REQUEST_FILE_STORAGE_DIR));
        requestFile.setLength(file.length());
        requestFile.setAbsolutePath(file.getAbsolutePath());
        requestFile.setOrganizationId(organizationId);
        requestFile.setMonth(month);
        requestFile.setYear(year);
        return requestFile;
    }

    public static LoadGroupParameter getLoadParameter(Long osznId, String districtCode, int monthFrom, int monthTo, int year)
            throws StorageNotFoundException {

        Map<String, Map<String, RequestFileGroup>> requestFileGroupsMap = new HashMap<String, Map<String, RequestFileGroup>>();

        //payment
        for (int month = monthFrom; month <= monthTo; ++month) {
            List<File> payments = getInputRequestFiles(PAYMENT_FILENAME_PREFIX, PAYMENT_BENEFIT_FILENAME_SUFFIX, districtCode,
                    month, year);

            for (int i = 0; i < payments.size(); ++i) {
                File file = payments.get(i);

                RequestFileGroup group = new RequestFileGroup();

                group.setPaymentFile(newRequestFile(file, RequestFile.TYPE.PAYMENT, osznId, month, year));

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
            List<File> benefits = getInputRequestFiles(BENEFIT_FILENAME_PREFIX, PAYMENT_BENEFIT_FILENAME_SUFFIX, districtCode,
                    month, year);

            for (File file : benefits) {
                RequestFile requestFile = newRequestFile(file, RequestFile.TYPE.BENEFIT, osznId, month, year);

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

    public static List<RequestFile> getTarifs(Long osznId, String districtCode, int monthFrom, int monthTo, int year)
            throws StorageNotFoundException {
        List<File> files = getInputRequestFiles(districtCode, monthFrom, monthTo, year, TARIF_PAYMENT_FILENAME_MASK);

        List<RequestFile> tarifs = new ArrayList<RequestFile>();

        for (File file : files) {
            //fill fields
            RequestFile requestFile = new RequestFile();

            requestFile.setName(file.getName());
            requestFile.setLength(file.length());
            requestFile.setAbsolutePath(file.getAbsolutePath());
            requestFile.setDirectory(RequestFileStorage.getInstance().getRelativeParent(file,
                    FileHandlingConfig.LOAD_INPUT_REQUEST_FILE_STORAGE_DIR));
            requestFile.setOrganizationId(osznId);
            requestFile.setYear(year);
            requestFile.setType(RequestFile.TYPE.TARIF);

            tarifs.add(requestFile);
        }

        return tarifs;
    }

    public static List<RequestFile> getActualPayments(Long osznId, String districtCode, int monthFrom,
            int monthTo, int year) throws StorageNotFoundException {
        List<RequestFile> actualPayments = new ArrayList<RequestFile>();

        for (int month = monthFrom; month <= monthTo; ++month) {
            List<File> files = getInputActualPaymentFiles(ACTUAL_PAYMENT_FILENAME_MASK, districtCode, month, year);

            for (File file : files) {
                //fill fields
                RequestFile requestFile = new RequestFile();

                requestFile.setName(file.getName());
                requestFile.setLength(file.length());
                requestFile.setAbsolutePath(file.getAbsolutePath());
                requestFile.setDirectory(RequestFileStorage.getInstance().getRelativeParent(file,
                        FileHandlingConfig.LOAD_INPUT_ACTUAL_PAYMENT_FILE_STORAGE_DIR));
                requestFile.setOrganizationId(osznId);
                requestFile.setMonth(month);
                requestFile.setYear(year);
                requestFile.setType(RequestFile.TYPE.ACTUAL_PAYMENT);

                actualPayments.add(requestFile);
            }
        }
        return actualPayments;
    }

    public static List<RequestFile> getSubsidies(Long osznId, String districtCode, int monthFrom,
            int monthTo, int year) throws StorageNotFoundException {
        List<RequestFile> subsidies = new ArrayList<RequestFile>();

        for (int month = monthFrom; month <= monthTo; ++month) {
            List<File> files = getInputSubsidyFiles(SUBSIDY_FILENAME_MASK, districtCode, month, year);

            for (File file : files) {
                //fill fields
                RequestFile requestFile = new RequestFile();

                requestFile.setName(file.getName());
                requestFile.setLength(file.length());
                requestFile.setAbsolutePath(file.getAbsolutePath());
                requestFile.setDirectory(RequestFileStorage.getInstance().getRelativeParent(file,
                        FileHandlingConfig.LOAD_INPUT_SUBSIDY_FILE_STORAGE_DIR));
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
