package org.complitex.osznconnection.file.entity;

import org.complitex.dictionary.entity.IConfig;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 04.10.2010 11:12:46
 */
public enum FileHandlingConfig implements IConfig{
    LOAD_INPUT_FILE_STORAGE_DIR("c:\\storage\\in"),
    SAVE_OUTPUT_FILE_STORAGE_DIR("c:\\storage\\out"),

    LOAD_THREAD_SIZE("2"),
    BIND_THREAD_SIZE("4"),
    FILL_THREAD_SIZE("4"),
    SAVE_THREAD_SIZE("4"),

    LOAD_BATCH_SIZE("16"),
    BIND_BATCH_SIZE("64"),
    FILL_BATCH_SIZE("64"),

    LOAD_MAX_ERROR_COUNT("10"),
    BIND_MAX_ERROR_COUNT("10"),
    FILL_MAX_ERROR_COUNT("10"),
    SAVE_MAX_ERROR_COUNT("10");

    private String defaultValue;

    FileHandlingConfig(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }
}

