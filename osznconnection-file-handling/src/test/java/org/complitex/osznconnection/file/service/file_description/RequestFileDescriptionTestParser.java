/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service.file_description;

import java.io.FileInputStream;
import java.net.URL;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.util.ValidationEventCollector;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.complitex.osznconnection.file.service.file_description.jaxb.FileDescriptions;

/**
 *
 * @author Artem
 */
public class RequestFileDescriptionTestParser {

    public static void main(String[] args) throws Exception {
        FileInputStream fis = new FileInputStream("D://Artem//temp//jaxb//file_descriptions.xml");
        
        try {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            String schemaPath = RequestFileDescriptionBean.class.getPackage().getName().replace('.', '/') + "/request_file_description.xsd";
            URL schemaUrl = Thread.currentThread().getContextClassLoader().getResource(schemaPath);
            Schema schema = schemaFactory.newSchema(schemaUrl);
            
//            Validator v = schema.newValidator();
//            v.validate(new StreamSource(fis));

            JAXBContext c = JAXBContext.newInstance(FileDescriptions.class.getPackage().getName());
            Unmarshaller u = c.createUnmarshaller();
            u.setSchema(schema);
            ValidationEventCollector col = new ValidationEventCollector();
            u.setEventHandler(col);
            try {
                FileDescriptions fds = (FileDescriptions) u.unmarshal(fis);
                System.out.println(fds.getFileDescriptionList().size());
            } finally {
                if (col != null && col.hasEvents()) {
                    for (ValidationEvent ev : col.getEvents()) {
                        System.out.println("Validation event: " + ev.getMessage()
                                + ", place: line: " + ev.getLocator().getLineNumber() + ", column: " + ev.getLocator().getColumnNumber()
                                + ", url: " + ev.getLocator().getURL());
                    }
                }
            }
        } finally {
            fis.close();
        }
    }
}
