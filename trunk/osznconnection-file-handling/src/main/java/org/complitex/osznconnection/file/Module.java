package org.complitex.osznconnection.file;

import org.complitex.dictionary.service.LogManager;
import org.complitex.osznconnection.file.entity.RequestFileGroup;
import org.complitex.osznconnection.file.web.GroupList;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 02.09.2010 17:16:01
 */
@Singleton(name = "FileHandlingModule")
@Startup
public class Module {
    public static final String NAME = "org.complitex.osznconnection.file";

    @PostConstruct
    public void init() {
        //todo tarif and request file
        //LogManager.get().registerLink(RequestFile.class.getName(), TarifFileList.class, null, "request_file_id");
        LogManager.get().registerLink(RequestFileGroup.class.getName(), GroupList.class, null, "group_id");
    }
}
