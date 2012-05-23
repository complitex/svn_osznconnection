package org.complitex.osznconnection.file.entity;

import org.complitex.dictionary.entity.IConfig;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 04.10.2010 11:12:46
 */
public enum FileHandlingConfig implements IConfig{
    LOAD_TARIF_DIR("c:\\storage\\tarif_in", "storage"),
    
    DEFAULT_LOAD_PAYMENT_BENEFIT_FILES_DIR("c:\\storage\\subsidy_in", "storage"),
    DEFAULT_SAVE_PAYMENT_BENEFIT_FILES_DIR("c:\\storage\\subsidy_out", "storage"),
    DEFAULT_LOAD_ACTUAL_PAYMENT_DIR("c:\\storage\\actual_in", "storage"),
    DEFAULT_SAVE_ACTUAL_PAYMENT_DIR("c:\\storage\\actual_out", "storage"),
    DEFAULT_LOAD_SUBSIDY_DIR("c:\\storage\\subsidy_in", "storage"),
    DEFAULT_SAVE_SUBSIDY_DIR("c:\\storage\\subsidy_out", "storage"),

    PAYMENT_FILENAME_PREFIX("A_", "mask"),
    BENEFIT_FILENAME_PREFIX("AF", "mask"),
    PAYMENT_BENEFIT_FILENAME_SUFFIX("\\d{4}{MM}\\.DBF", "mask"),
    
    ACTUAL_PAYMENT_FILENAME_MASK(".*{MM}{YY}\\.DBF", "mask"),
    SUBSIDY_FILENAME_MASK("J.*{MM}\\.DBF", "mask"),
    
    TARIF_PAYMENT_FILENAME_MASK("TARIF12\\.DBF", "mask"),

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
    SAVE_MAX_ERROR_COUNT("10", "error");

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

