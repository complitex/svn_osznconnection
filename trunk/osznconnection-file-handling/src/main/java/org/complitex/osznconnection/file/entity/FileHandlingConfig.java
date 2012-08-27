package org.complitex.osznconnection.file.entity;

import org.complitex.dictionary.entity.IConfig;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 04.10.2010 11:12:46
 */
public enum FileHandlingConfig implements IConfig{
    PAYMENT_FILENAME_PREFIX("A_", "mask"),
    BENEFIT_FILENAME_PREFIX("AF", "mask"),
    PAYMENT_BENEFIT_FILENAME_SUFFIX("\\d{4}{MM}\\.DBF", "mask"),
    
    ACTUAL_PAYMENT_FILENAME_MASK(".*{MM}{YY}\\.DBF", "mask"),
    SUBSIDY_FILENAME_MASK("J.*{MM}\\.DBF", "mask"),
    
    DWELLING_CHARACTERISTICS_INPUT_FILENAME_MASK("\\d{8}\\.a\\d{2}", "mask"),
    DWELLING_CHARACTERISTICS_OUTPUT_FILE_EXTENSION_PREFIX("c", "mask"),
    
    FACILITY_SERVICE_TYPE_INPUT_FILENAME_MASK("\\d{8}\\.b\\d{2}", "mask"),
    FACILITY_SERVICE_TYPE_OUTPUT_FILE_EXTENSION_PREFIX("d", "mask"),
    
    TARIF_PAYMENT_FILENAME_MASK("TARIF12\\.DBF", "mask"),
    
    FACILITY_STREET_TYPE_REFERENCE_FILENAME_MASK("KLKATUL\\.DBF", "mask"),
    FACILITY_STREET_REFERENCE_FILENAME_MASK("KLUL\\.DBF", "mask"),
    FACILITY_TARIF_REFERENCE_FILENAME_MASK("TARIF\\.DBF", "mask"),

    LOAD_THREAD_SIZE("2", "thread"),
    BIND_THREAD_SIZE("4", "thread"),
    FILL_THREAD_SIZE("4", "thread"),
    SAVE_THREAD_SIZE("4", "thread"),

    LOAD_BATCH_SIZE("16", "batch"),
    BIND_BATCH_SIZE("64", "batch"),
    FILL_BATCH_SIZE("64", "batch"),

    LOAD_MAX_ERROR_COUNT("10", "error"),
    BIND_MAX_ERROR_COUNT("10", "error"),
    FILL_MAX_ERROR_COUNT("10", "error"),
    SAVE_MAX_ERROR_COUNT("10", "error"),
    
    DEFAULT_REQUEST_FILE_CITY("", "request_file_params");

    private String defaultValue;
    private String groupKey;

    FileHandlingConfig(String defaultValue, String groupKey) {
        this.defaultValue = defaultValue;
        this.groupKey = groupKey;
    }

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String getGroupKey() {
        return groupKey;
    }
}