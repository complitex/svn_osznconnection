/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service.file_description;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.ejb.Stateless;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.util.ValidationEventCollector;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.AbstractBean;
import static org.complitex.dictionary.util.ResourceUtil.*;
import org.complitex.osznconnection.file.entity.ActualPaymentDBF;
import org.complitex.osznconnection.file.entity.BenefitDBF;
import org.complitex.osznconnection.file.entity.PaymentDBF;
import org.complitex.osznconnection.file.entity.RequestFile;
import org.complitex.osznconnection.file.entity.SubsidyDBF;
import org.complitex.osznconnection.file.entity.TarifDBF;
import org.complitex.osznconnection.file.service.file_description.RequestFileDescriptionValidateException.ValidationError;
import org.complitex.osznconnection.file.service.file_description.jaxb.Field;
import org.complitex.osznconnection.file.service.file_description.jaxb.Fields;
import org.complitex.osznconnection.file.service.file_description.jaxb.FileDescription;
import org.complitex.osznconnection.file.service.file_description.jaxb.FileDescriptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Artem
 */
@Stateless
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class RequestFileDescriptionBean extends AbstractBean {

    private static final Logger log = LoggerFactory.getLogger(RequestFileDescriptionBean.class);
    private static final String MAPPING_NAMESPACE = RequestFileDescriptionBean.class.getName();
    private static final String RESOURCE_BUNDLE = RequestFileDescriptionBean.class.getName();
    private final static Map<RequestFile.TYPE, RequestFileDescription> cache = Collections.synchronizedMap(
            new EnumMap<RequestFile.TYPE, RequestFileDescription>(RequestFile.TYPE.class));
    private final static Map<RequestFile.TYPE, Class<? extends Enum<?>>> REQUEST_FILE_TYPE_MAP =
            ImmutableMap.of(
            RequestFile.TYPE.ACTUAL_PAYMENT, ActualPaymentDBF.class,
            RequestFile.TYPE.PAYMENT, PaymentDBF.class,
            RequestFile.TYPE.BENEFIT, BenefitDBF.class,
            RequestFile.TYPE.SUBSIDY, SubsidyDBF.class,
            RequestFile.TYPE.TARIF, TarifDBF.class);

    @Transactional
    private void insert(RequestFileDescription fileDescription) {
        sqlSession().insert(MAPPING_NAMESPACE + ".insertFileDescription", fileDescription);
        if (!fileDescription.getFields().isEmpty()) {
            for (RequestFileFieldDescription field : fileDescription.getFields()) {
                field.setRequestFileDescriptionId(fileDescription.getId());
                sqlSession().insert(MAPPING_NAMESPACE + ".insertFileFieldDescription", field);
            }
        }
    }

    @Transactional
    private void delete() {
        sqlSession().delete(MAPPING_NAMESPACE + ".deleteFileFieldDescription");
        sqlSession().delete(MAPPING_NAMESPACE + ".deleteFileDescription");
    }

    @Transactional
    public void update(List<RequestFileDescription> requestFileDescriptions) {
        synchronized (cache) {
            cache.clear();
            delete();

            for (RequestFileDescription fileDescription : requestFileDescriptions) {
                insert(fileDescription);
                //prefill cache
                getFileDescription(fileDescription.getRequestFileType());
            }
        }
    }

    public RequestFileDescription getFileDescription(RequestFile.TYPE requestFileType) {
        synchronized (cache) {
            RequestFileDescription fileDescription = cache.get(requestFileType);
            if (fileDescription == null) {
                fileDescription = (RequestFileDescription) sqlSession().selectOne(MAPPING_NAMESPACE + ".find",
                        requestFileType.name());
                cache.put(requestFileType, fileDescription);
            }
            return fileDescription;
        }
    }

    public List<RequestFileDescription> getDescription(InputStream inputStream, Locale locale)
            throws RequestFileDescriptionValidateException {
        final List<ValidationError> errors = Lists.newArrayList();

        // parsing xml step.
        FileDescriptions fileDescriptions = null;
        try {
            Schema schema = null;
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            try {
                final String schemaPath = RequestFileDescriptionBean.class.getPackage().getName().replace('.', '/')
                        + "/request_file_description.xsd";
                final URL schemaUrl = Thread.currentThread().getContextClassLoader().getResource(schemaPath);
                schema = schemaFactory.newSchema(schemaUrl);
            } catch (Exception e) {
                throw new RuntimeException("Couldn't load request_file_description.xsd scheam file.", e);
            }

            JAXBContext context = JAXBContext.newInstance(FileDescriptions.class.getPackage().getName());
            Unmarshaller unmarshaller = context.createUnmarshaller();
            unmarshaller.setSchema(schema);
            ValidationEventCollector validationEventCollector = new ValidationEventCollector();
            unmarshaller.setEventHandler(validationEventCollector);

            try {
                fileDescriptions = (FileDescriptions) unmarshaller.unmarshal(inputStream);
            } catch (UnmarshalException e) {
                log.error("", e);
            } finally {
                if (validationEventCollector != null && validationEventCollector.hasEvents()) {
                    // pick up first error and ignore the rest.
                    ValidationEvent event = validationEventCollector.getEvents()[0];
                    errors.add(new ValidationError(getFormatString(RESOURCE_BUNDLE, "file_validation_error", locale,
                            event.getLocator().getLineNumber(), event.getMessage())));
                    throw new RequestFileDescriptionValidateException(errors);
                }
            }
        } catch (JAXBException e) {
            log.error("", e);
            errors.add(new ValidationError(getString(RESOURCE_BUNDLE, "unexpected_jaxb_error", locale)));
            throw new RequestFileDescriptionValidateException(errors);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                log.error("Couldn't close file inout stream.", e);
            }
        }

        // transformation and validation step.
        final List<RequestFileDescription> requestFileDescriptions = Lists.newArrayList();

        for (RequestFile.TYPE requestFileType : REQUEST_FILE_TYPE_MAP.keySet()) {
            // there are must be one and only one description for each file type.
            FileDescription fileDescription = null;
            boolean tooManyDescriptionsFound = false;
            for (FileDescription fd : fileDescriptions.getFileDescriptionList()) {
                if (requestFileType.name().equals(fd.getType())) {
                    if (fileDescription == null) {
                        fileDescription = fd;
                    } else {
                        tooManyDescriptionsFound = true;
                        break;
                    }
                }
            }

            if (fileDescription == null) {
                errors.add(new ValidationError(getFormatString(RESOURCE_BUNDLE, "file_description_not_found", locale, requestFileType)));
            } else if (tooManyDescriptionsFound) {
                errors.add(new ValidationError(getFormatString(RESOURCE_BUNDLE, "file_description_too_many", locale, requestFileType)));
            } else {
                final RequestFileDescription requestFileDescription = new RequestFileDescription(requestFileType.name(),
                        fileDescription.getFormatters().getDatePattern().getPattern());
                requestFileDescriptions.add(requestFileDescription);

                // there are must be one and only one field description in each file type.
                final Fields fields = fileDescription.getFields();
                final Class<? extends Enum<?>> dbfClass = REQUEST_FILE_TYPE_MAP.get(requestFileType);
                for (Enum<?> dbfField : dbfClass.getEnumConstants()) {
                    final String fieldName = dbfField.name();
                    Field fieldDescription = null;
                    boolean tooManyFieldDescriptionsFound = false;
                    for (Field field : fields.getFieldList()) {
                        if (fieldName.equals(field.getName())) {
                            if (fieldDescription == null) {
                                fieldDescription = field;
                            } else {
                                tooManyFieldDescriptionsFound = true;
                                break;
                            }
                        }
                    }

                    if (fieldDescription == null) {
                        errors.add(new ValidationError(getFormatString(RESOURCE_BUNDLE, "field_description_not_found", locale,
                                requestFileType, fieldName)));
                    } else if (tooManyFieldDescriptionsFound) {
                        errors.add(new ValidationError(getFormatString(RESOURCE_BUNDLE, "field_description_too_many", locale,
                                requestFileType, fieldName)));
                    } else {
                        final String type = fieldDescription.getType().value();
                        final int length = fieldDescription.getLength();
                        final Integer scale = fieldDescription.getScale();

                        //if all field description validations pass then create field and add it to file description.
                        requestFileDescription.addField(new RequestFileFieldDescription(fieldName, type, length, scale));
                    }
                }
            }
        }

        //there are must be only expected request file types.
        for (FileDescription fd : fileDescriptions.getFileDescriptionList()) {
            final String fileType = fd.getType();

            RequestFile.TYPE expectedRequestFileType = null;
            for (RequestFile.TYPE requestFileType : REQUEST_FILE_TYPE_MAP.keySet()) {
                if (requestFileType.name().equals(fileType)) {
                    expectedRequestFileType = requestFileType;
                    break;
                }
            }

            if (expectedRequestFileType == null) {
                errors.add(new ValidationError(getFormatString(RESOURCE_BUNDLE, "unexpected_file_description", locale,
                        fileType)));
            } else {
                //there are must be only expected request file's field descriptions.
                for (Field f : fd.getFields().getFieldList()) {
                    final String fieldName = f.getName();

                    boolean expectedField = false;
                    final Class<? extends Enum<?>> dbfClass = REQUEST_FILE_TYPE_MAP.get(expectedRequestFileType);
                    for (Enum<?> dbfField : dbfClass.getEnumConstants()) {
                        if (dbfField.name().equals(fieldName)) {
                            expectedField = true;
                            break;
                        }
                    }

                    if (!expectedField) {
                        errors.add(new ValidationError(getFormatString(RESOURCE_BUNDLE, "unexpected_field_description", locale,
                                fileType, fieldName)));
                    }
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new RequestFileDescriptionValidateException(errors);
        } else {
            return requestFileDescriptions;
        }
    }
}
