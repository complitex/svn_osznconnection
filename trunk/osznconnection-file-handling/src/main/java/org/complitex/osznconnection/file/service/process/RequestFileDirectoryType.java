/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service.process;

import org.complitex.osznconnection.organization.strategy.IOsznOrganizationStrategy;

/**
 *
 * @author Artem
 */
public enum RequestFileDirectoryType {

    LOAD_PAYMENT_BENEFIT_FILES_DIR(IOsznOrganizationStrategy.LOAD_PAYMENT_BENEFIT_FILES_DIR),
    SAVE_PAYMENT_BENEFIT_FILES_DIR(IOsznOrganizationStrategy.SAVE_PAYMENT_BENEFIT_FILES_DIR),
    LOAD_ACTUAL_PAYMENT_DIR(IOsznOrganizationStrategy.LOAD_ACTUAL_PAYMENT_DIR),
    SAVE_ACTUAL_PAYMENT_DIR(IOsznOrganizationStrategy.SAVE_ACTUAL_PAYMENT_DIR),
    LOAD_SUBSIDY_DIR(IOsznOrganizationStrategy.LOAD_SUBSIDY_DIR),
    SAVE_SUBSIDY_DIR(IOsznOrganizationStrategy.SAVE_SUBSIDY_DIR),
    LOAD_DWELLING_CHARACTERISTICS_DIR(IOsznOrganizationStrategy.LOAD_DWELLING_CHARACTERISTICS_DIR),
    SAVE_DWELLING_CHARACTERISTICS_DIR(IOsznOrganizationStrategy.SAVE_DWELLING_CHARACTERISTICS_DIR),
    LOAD_FACILITY_SERVICE_TYPE_DIR(IOsznOrganizationStrategy.LOAD_FACILITY_SERVICE_TYPE_DIR),
    SAVE_FACILITY_SERVICE_TYPE_DIR(IOsznOrganizationStrategy.SAVE_FACILITY_SERVICE_TYPE_DIR),
    SAVE_FACILITY_FORM2_DIR(IOsznOrganizationStrategy.SAVE_FACILITY_FORM2_DIR),
    REFERENCES_DIR(IOsznOrganizationStrategy.REFERENCES_DIR);
    
    private long attributeTypeId;

    private RequestFileDirectoryType(long attributeTypeId) {
        this.attributeTypeId = attributeTypeId;
    }

    public long getAttributeTypeId() {
        return attributeTypeId;
    }
}
