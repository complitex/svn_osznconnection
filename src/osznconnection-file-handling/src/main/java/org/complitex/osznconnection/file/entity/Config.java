package org.complitex.osznconnection.file.entity;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 04.10.2010 11:12:46
 */
public enum Config {
    LOAD_INPUT_FILE_STORAGE_DIR("C:\\storage\\in"),
    SAVE_OUTPUT_FILE_STORAGE_DIR("C:\\storage\\out"),

    LOAD_THREAD_SIZE("4"),
    BIND_THREAD_SIZE("4"),
    FILL_THREAD_SIZE("4"),
    SAVE_THREAD_SIZE("4"),

    LOAD_MAX_ERROR_COUNT("10"),
    BIND_MAX_ERROR_COUNT("10"),
    FILL_MAX_ERROR_COUNT("10"),
    SAVE_MAX_ERROR_COUNT("10"),

    LOAD_RECORD_BATCH_SIZE("64");

    private String defaultValue;

    Config(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}

