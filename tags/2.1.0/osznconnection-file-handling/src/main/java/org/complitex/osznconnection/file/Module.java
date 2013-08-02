package org.complitex.osznconnection.file;

import org.complitex.osznconnection.file.service.RequestFileBean;
import org.complitex.osznconnection.file.service.RequestFileGroupBean;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
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

    @EJB
    private RequestFileBean requestFileBean;
    @EJB
    private RequestFileGroupBean requestFileGroupBean;

    @PostConstruct
    public void init() {
        requestFileBean.fixProcessingOnInit();
        requestFileGroupBean.fixProcessingOnInit();
    }
}
