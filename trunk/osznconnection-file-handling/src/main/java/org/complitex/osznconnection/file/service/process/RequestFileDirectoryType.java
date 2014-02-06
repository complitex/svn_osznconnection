package org.complitex.osznconnection.file.service.process;


import org.complitex.osznconnection.organization.strategy.OsznOrganizationStrategy;

/**
 *
 * @author Artem
 */
public enum RequestFileDirectoryType {

    LOAD_PAYMENT_BENEFIT_FILES_DIR(OsznOrganizationStrategy.LOAD_PAYMENT_BENEFIT_FILES_DIR),
    SAVE_PAYMENT_BENEFIT_FILES_DIR(OsznOrganizationStrategy.SAVE_PAYMENT_BENEFIT_FILES_DIR),
    LOAD_ACTUAL_PAYMENT_DIR(OsznOrganizationStrategy.LOAD_ACTUAL_PAYMENT_DIR),
    SAVE_ACTUAL_PAYMENT_DIR(OsznOrganizationStrategy.SAVE_ACTUAL_PAYMENT_DIR),
    LOAD_SUBSIDY_DIR(OsznOrganizationStrategy.LOAD_SUBSIDY_DIR),
    SAVE_SUBSIDY_DIR(OsznOrganizationStrategy.SAVE_SUBSIDY_DIR),
    LOAD_DWELLING_CHARACTERISTICS_DIR(OsznOrganizationStrategy.LOAD_DWELLING_CHARACTERISTICS_DIR),
    SAVE_DWELLING_CHARACTERISTICS_DIR(OsznOrganizationStrategy.SAVE_DWELLING_CHARACTERISTICS_DIR),
    LOAD_FACILITY_SERVICE_TYPE_DIR(OsznOrganizationStrategy.LOAD_FACILITY_SERVICE_TYPE_DIR),
    SAVE_FACILITY_SERVICE_TYPE_DIR(OsznOrganizationStrategy.SAVE_FACILITY_SERVICE_TYPE_DIR),
    SAVE_FACILITY_FORM2_DIR(OsznOrganizationStrategy.SAVE_FACILITY_FORM2_DIR),
    EXPORT_SUBSIDY_DIR(OsznOrganizationStrategy.EXPORT_SUBSIDY_DIR),
    REFERENCES_DIR(OsznOrganizationStrategy.REFERENCES_DIR);
    
    private long attributeTypeId;

    private RequestFileDirectoryType(long attributeTypeId) {
        this.attributeTypeId = attributeTypeId;
    }

    public long getAttributeTypeId() {
        return attributeTypeId;
    }
}
