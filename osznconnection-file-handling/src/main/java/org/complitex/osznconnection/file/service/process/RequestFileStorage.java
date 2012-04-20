package org.complitex.osznconnection.file.service.process;

import org.complitex.dictionary.service.ConfigBean;
import org.complitex.dictionary.util.EjbBeanLocator;
import org.complitex.osznconnection.file.entity.FileHandlingConfig;
import org.complitex.osznconnection.file.service.exception.StorageNotFoundException;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.entity.IConfig;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.08.2010 12:18:43
 *
 * Класс одиночка для работы с файловой системой.
 * Поиск, сохранение, удаление.
 */
public class RequestFileStorage {

    private static RequestFileStorage instance;

    public static synchronized RequestFileStorage getInstance() {
        if (instance == null) {
            instance = new RequestFileStorage();
        }

        return instance;
    }

    /**
     * @param child дочерняя директория
     * @param filter фильтр
     * @return Список файлов в дочерней директории корневой директории файлового хранилища
     * @throws org.complitex.osznconnection.file.service.exception.StorageNotFoundException Директория не найдена
     */
    public List<File> getInputRequestFiles(String child, FileFilter filter) throws StorageNotFoundException {
        List<File> files = new ArrayList<File>();

        File dir = new File(EjbBeanLocator.getBean(ConfigBean.class)
                .getString(FileHandlingConfig.LOAD_INPUT_REQUEST_FILE_STORAGE_DIR, true), child);

        if (!dir.exists()) {
            throw new StorageNotFoundException(dir.getAbsolutePath());
        }

        addFiles(files, dir, filter);

        return files;
    }

    public List<File> getInputActualPaymentFiles(String child, FileFilter filter) throws StorageNotFoundException {
        List<File> files = new ArrayList<File>();

        File dir = new File(EjbBeanLocator.getBean(ConfigBean.class)
                .getString(FileHandlingConfig.LOAD_INPUT_ACTUAL_PAYMENT_FILE_STORAGE_DIR, true), child);

        if (!dir.exists()) {
            throw new StorageNotFoundException(dir.getAbsolutePath());
        }

        addFiles(files, dir, filter);

        return files;
    }
    
    public List<File> getInputSubsidyFiles(String child, FileFilter filter) throws StorageNotFoundException {
        List<File> files = new ArrayList<File>();

        File dir = new File(EjbBeanLocator.getBean(ConfigBean.class)
                .getString(FileHandlingConfig.LOAD_INPUT_SUBSIDY_FILE_STORAGE_DIR, true), child);

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

    private void checkOutputRequestFileStorageExists(IConfig configDir) throws StorageNotFoundException {
        File parent = new File(EjbBeanLocator.getBean(ConfigBean.class).getString(configDir, true));

        //Желательно чтобы директория для исходящих файлов запроса уже была создана
        if (!parent.exists()) {
            throw new StorageNotFoundException(parent.getAbsolutePath());
        }
    }

    public File createOutputRequestFileDirectory(IConfig configDir, String name, String child) throws StorageNotFoundException {
        checkOutputRequestFileStorageExists(configDir);

        //Создаем директорию с именем кода района если что
        File dir = new File(EjbBeanLocator.getBean(ConfigBean.class).getString(configDir, false), child);
        if (!dir.exists()) {
            dir.mkdir();
        }

        return new File(dir, name);
    }
    
    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    public File createFile(String path, boolean replace) {
        File file = new File(path);
        if (replace && file.exists()) {
            file.delete();
        }

        return file;
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    public void delete(String path) {
        new File(path).delete();
    }

    public String getRelativeParent(File file, FileHandlingConfig dir) {
        File root = new File(EjbBeanLocator.getBean(ConfigBean.class).getString(dir, false));
        String absolutePath = file.getParent();

        return absolutePath.substring(root.getAbsolutePath().length());
    }
}
