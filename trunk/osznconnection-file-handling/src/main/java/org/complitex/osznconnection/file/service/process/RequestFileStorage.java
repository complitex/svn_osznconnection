package org.complitex.osznconnection.file.service.process;

import org.complitex.dictionary.util.EjbBeanLocator;
import org.complitex.osznconnection.file.service.exception.StorageNotFoundException;
import org.complitex.osznconnection.organization.strategy.OsznOrganizationStrategy;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.08.2010 12:18:43
 *
 * Класс одиночка для работы с файловой системой.
 * Поиск, сохранение, удаление.
 */
public class RequestFileStorage {

    public static class RequestFiles {

        private final String path;
        private final List<File> files;

        public RequestFiles(String path, List<File> files) {
            this.path = path;
            this.files = files;
        }

        public List<File> getFiles() {
            return files;
        }

        public String getPath() {
            return path;
        }
    }
    public static RequestFileStorage INSTANCE = new RequestFileStorage();

    public RequestFiles getInputRequestFiles(long userOrganizationId, long osznId, RequestFileDirectoryType fileDirectoryType,
            FileFilter filter) throws StorageNotFoundException {
        List<File> files = new ArrayList<File>();
        File dir = new File(getRequestFilesStorageDirectory(userOrganizationId, osznId, fileDirectoryType));
        if (!dir.exists()) {
            throw new StorageNotFoundException(dir.getAbsolutePath());
        }
        addFiles(files, dir, filter);
        return new RequestFiles(dir.getAbsolutePath(), files);
    }

    private void addFiles(List<File> list, File dir, FileFilter filter) {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                addFiles(list, file, filter);
            } else if (filter.accept(file)) {
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

    public File deleteAndCreateFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        return file;
    }

    public String getRelativeParent(File file, String subPath) {
        File root = new File(subPath);
        String absolutePath = file.getParent();
        return absolutePath.substring(root.getAbsolutePath().length());
    }

    public String getRequestFilesStorageDirectory(Long userOrganizationId, Long osznId, RequestFileDirectoryType fileDirectoryType)
            throws StorageNotFoundException {
        OsznOrganizationStrategy osznOrganizationStrategy =
                EjbBeanLocator.getBean(OsznOrganizationStrategy.OSZN_ORGANIZATION_STRATEGY_NAME);

        //root request files path:
        String rootRequestFilesPath = osznOrganizationStrategy.getRootRequestFilesStoragePath(userOrganizationId);
        if (rootRequestFilesPath == null) {
            throw new StorageNotFoundException(new NullPointerException("Корневой каталог к файлам запросов не задан."), "''");
        } else if (rootRequestFilesPath.endsWith(File.separator)) {
            rootRequestFilesPath = rootRequestFilesPath.substring(0, rootRequestFilesPath.length());
        }

        String relativeRequestFilesPath = "";

        //relative request files path:
        if (osznId != null) {
            relativeRequestFilesPath = osznOrganizationStrategy.getRelativeRequestFilesPath(osznId,
                    fileDirectoryType.getAttributeTypeId());
            if (relativeRequestFilesPath == null) {
                throw new StorageNotFoundException(new NullPointerException("Относительный путь к файлам запросов не задан."), "''");
            } else if (relativeRequestFilesPath.startsWith(File.separator)) {
                relativeRequestFilesPath = relativeRequestFilesPath.substring(1);
            }
        }

        return rootRequestFilesPath + File.separator + relativeRequestFilesPath;
    }
}
