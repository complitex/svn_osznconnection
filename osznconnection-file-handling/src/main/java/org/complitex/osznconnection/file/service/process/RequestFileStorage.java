package org.complitex.osznconnection.file.service.process;

import org.complitex.dictionary.service.ConfigBean;
import org.complitex.dictionary.util.EjbBeanLocator;
import org.complitex.osznconnection.file.entity.FileHandlingConfig;
import org.complitex.osznconnection.file.service.exception.StorageNotFoundException;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.wicket.util.string.Strings;
import org.complitex.osznconnection.organization.strategy.IOsznOrganizationStrategy;
import org.complitex.osznconnection.organization.strategy.OsznOrganizationStrategy;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.08.2010 12:18:43
 *
 * Класс одиночка для работы с файловой системой.
 * Поиск, сохранение, удаление.
 */
public class RequestFileStorage {

    public static RequestFileStorage INSTANCE = new RequestFileStorage();

    private String getConfigString(FileHandlingConfig config, boolean flush) {
        return EjbBeanLocator.getBean(ConfigBean.class).getString(config, flush);
    }

    public List<File> getInputTarifFiles(String child, FileFilter filter) throws StorageNotFoundException {
        List<File> files = new ArrayList<File>();
        File dir = new File(getConfigString(FileHandlingConfig.LOAD_TARIF_DIR, true), child);
        if (!dir.exists()) {
            throw new StorageNotFoundException(dir.getAbsolutePath());
        }
        addFiles(files, dir, filter);
        return files;
    }

    public List<File> getInputRequestFiles(long userOrganizationId, String districtCodeDir, FileHandlingConfig defaultConfigLoadDirectory,
            FileFilter filter) throws StorageNotFoundException {
        List<File> files = new ArrayList<File>();
        File dir = new File(getRequestFilesStorageDir(userOrganizationId, defaultConfigLoadDirectory), districtCodeDir);
        if (!dir.exists()) {
            throw new StorageNotFoundException(dir.getAbsolutePath());
        }
        addFiles(files, dir, filter);
        return files;
    }

    private void addFiles(List<File> list, File dir, FileFilter filter) {
        for (File file : dir.listFiles(filter)) {
            if (file.isDirectory()) {
                addFiles(list, file, filter);
            } else {
                list.add(file);
            }
        }
    }

    private void checkOutputRequestFileStorageExists(String parentDir) throws StorageNotFoundException {
        File parent = new File(parentDir);

        //Желательно чтобы директория для исходящих файлов запроса уже была создана
        if (!parent.exists()) {
            throw new StorageNotFoundException(parent.getAbsolutePath());
        }
    }

    public File createOutputRequestFileDirectory(String parent, String name, String child) throws StorageNotFoundException {
        checkOutputRequestFileStorageExists(parent);

        //Создаем директорию с промежуточным именем если что
        File dir = new File(parent, child);
        try {
            forceMkdir(dir);
        } catch (IOException e) {
            throw new RuntimeException("Unexpected error: ", e);
        }

        return new File(dir, name);
    }

    private void forceMkdir(File directory) throws IOException {
        if (directory.exists()) {
            if (!directory.isDirectory()) {
                String message = "File " + directory + " exists and is not a directory. Unable to create directory.";
                throw new IOException(message);
            }
        } else {
            if (!directory.mkdirs()) {
                // Double-check that some other thread or process hasn't made
                // the directory in the background
                if (!directory.isDirectory()) {
                    throw new IOException("Unable to create directory " + directory);
                }
            }
        }
    }

    public File createFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        return file;
    }

    public void delete(String path) {
        new File(path).delete();
    }

    public String getRelativeParent(File file, String subPath) {
        File root = new File(subPath);
        String absolutePath = file.getParent();
        return absolutePath.substring(root.getAbsolutePath().length());
    }

    public String getRequestFilesStorageDir(long userOrganizationId, FileHandlingConfig defaultConfigDir) {
        if (defaultConfigDir == null) {
            throw new NullPointerException("Default config dir parameter is null.");
        }

        IOsznOrganizationStrategy osznOrganizationStrategy =
                EjbBeanLocator.getBean(OsznOrganizationStrategy.OSZN_ORGANIZATION_STRATEGY_NAME);
        long organizationAttributeTypeId;
        switch (defaultConfigDir) {
            case DEFAULT_LOAD_PAYMENT_BENEFIT_FILES_DIR:
                organizationAttributeTypeId = IOsznOrganizationStrategy.LOAD_PAYMENT_BENEFIT_FILES_DIR;
                break;
            case DEFAULT_SAVE_PAYMENT_BENEFIT_FILES_DIR:
                organizationAttributeTypeId = IOsznOrganizationStrategy.SAVE_PAYMENT_BENEFIT_FILES_DIR;
                break;
            case DEFAULT_LOAD_ACTUAL_PAYMENT_DIR:
                organizationAttributeTypeId = IOsznOrganizationStrategy.LOAD_ACTUAL_PAYMENT_DIR;
                break;
            case DEFAULT_SAVE_ACTUAL_PAYMENT_DIR:
                organizationAttributeTypeId = IOsznOrganizationStrategy.SAVE_ACTUAL_PAYMENT_DIR;
                break;
            case DEFAULT_LOAD_SUBSIDY_DIR:
                organizationAttributeTypeId = IOsznOrganizationStrategy.LOAD_SUBSIDY_DIR;
                break;
            case DEFAULT_SAVE_SUBSIDY_DIR:
                organizationAttributeTypeId = IOsznOrganizationStrategy.SAVE_SUBSIDY_DIR;
                break;
            default:
                throw new IllegalStateException("Wrong default config dir parameter: " + defaultConfigDir);
        }

        String requestFilesStorageDir = osznOrganizationStrategy.getRequestFilesStorageDir(userOrganizationId,
                organizationAttributeTypeId);
        if (Strings.isEmpty(requestFilesStorageDir)) {
            requestFilesStorageDir = getConfigString(defaultConfigDir, true);
        }
        return requestFilesStorageDir;
    }
}
