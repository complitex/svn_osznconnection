package org.complitex.osznconnection.file.entity;

import org.complitex.dictionary.entity.IImportFile;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 02.03.11 13:25
 */
public enum OwnershipImportFile implements IImportFile{
    OWNERSHIP("ownership.csv");

    private String fileName;

    OwnershipImportFile(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String getFileName() {
        return fileName;
    }
}
