package org.complitex.osznconnection.file.entity;

import org.complitex.address.entity.AddressImportFile;

import java.io.Serializable;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 01.03.11 13:30
 */
public class AddressImportMessage implements Serializable{
    private AddressImportFile addressImportFile;
    private int count;
    private int index;

    public AddressImportMessage() {
    }

    public AddressImportMessage(AddressImportFile addressImportFile, int count, int index) {
        this.addressImportFile = addressImportFile;
        this.count = count;
        this.index = index;
    }

    public AddressImportFile getAddressImportFile() {
        return addressImportFile;
    }

    public void setAddressImportFile(AddressImportFile addressImportFile) {
        this.addressImportFile = addressImportFile;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
