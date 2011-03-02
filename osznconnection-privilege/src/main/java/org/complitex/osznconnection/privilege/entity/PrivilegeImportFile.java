package org.complitex.osznconnection.privilege.entity;

import org.complitex.dictionary.entity.IImportFile;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 02.03.11 13:48
 */
public enum PrivilegeImportFile implements IImportFile{
    PRIVILEGE("privilege.csv");

    private String fileName;

    PrivilegeImportFile(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String getFileName() {
        return fileName;
    }
}
