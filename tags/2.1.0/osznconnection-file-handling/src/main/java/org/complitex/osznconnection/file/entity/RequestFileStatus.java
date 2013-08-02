package org.complitex.osznconnection.file.entity;

/**
* User: Anatoly A. Ivanov java@inhell.ru
* Date: 19.01.11 23:18
*/
public enum RequestFileStatus implements IEnumCode {
    SKIPPED(100),
    LOADING(112),   LOAD_ERROR(111),   LOADED(110),
    BINDING(122),   BIND_ERROR(121),   BOUND(120),
    FILLING(132),   FILL_ERROR(131),   FILLED(130),
    SAVING(142),    SAVE_ERROR(141),   SAVED(140);

    private int code;

    RequestFileStatus(int code) {
        this.code = code;
    }

    @Override
    public int getCode() {
        return code;
    }
}
