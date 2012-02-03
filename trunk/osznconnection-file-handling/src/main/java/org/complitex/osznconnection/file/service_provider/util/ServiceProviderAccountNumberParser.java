/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service_provider.util;

import org.complitex.osznconnection.file.service_provider.ServiceProviderAccountNumberInfo;
import org.complitex.osznconnection.file.service_provider.exception.ServiceProviderAccountNumberParseException;
import static org.apache.wicket.util.string.Strings.*;
import static org.complitex.dictionary.util.StringUtil.*;

/**
 *
 * @author Artem
 */
public final class ServiceProviderAccountNumberParser {

    private ServiceProviderAccountNumberParser() {
    }

    public static ServiceProviderAccountNumberInfo parse(String serviceProviderAccountNumberInfo)
            throws ServiceProviderAccountNumberParseException {
        if (serviceProviderAccountNumberInfo == null) {
            throw new ServiceProviderAccountNumberParseException();
        }
        serviceProviderAccountNumberInfo = serviceProviderAccountNumberInfo.trim();
        String[] parts = serviceProviderAccountNumberInfo.split("\\.");
        if (parts == null || parts.length != 2) {
            throw new ServiceProviderAccountNumberParseException();
        }
        String serviceProviderId = parts[0];
        if (serviceProviderId != null) {
            serviceProviderId = serviceProviderId.trim();
        }
        String serviceProviderAccountNumber = parts[1];
        if (serviceProviderAccountNumber != null) {
            serviceProviderAccountNumber = serviceProviderAccountNumber.trim();
        }
        if (isEmpty(serviceProviderId) || isEmpty(serviceProviderAccountNumber)
                || !isNumeric(serviceProviderId) || !isNumeric(serviceProviderAccountNumber)) {
            throw new ServiceProviderAccountNumberParseException();
        }
        return new ServiceProviderAccountNumberInfo(serviceProviderId, serviceProviderAccountNumber);
    }

    public static boolean matches(String realServiceProviderAccountNumber, String remoteServiceProviderAccountNumber) {
        if (isEmpty(realServiceProviderAccountNumber)) {
            throw new IllegalArgumentException("Real service provider account number is null or empty.");
        }
        if (isEmpty(remoteServiceProviderAccountNumber)) {
            throw new IllegalArgumentException("Remote service provider account number is null or empty.");
        }
        if (!isNumeric(remoteServiceProviderAccountNumber)) {
            throw new IllegalArgumentException("Remote service provider account number is not numeric.");
        }
        return realServiceProviderAccountNumber.matches("0*" + remoteServiceProviderAccountNumber);
    }

    public static boolean matches(String realServiceProviderAccountNumber, String remoteServiceProviderId,
            String remoteServiceProviderAccountNumber) {
        if (isEmpty(realServiceProviderAccountNumber)) {
            throw new IllegalArgumentException("Real service provider account number is null or empty.");
        }
        if (isEmpty(remoteServiceProviderId)) {
            throw new IllegalArgumentException("Remote service provider id is null or empty.");
        }
        if (isEmpty(remoteServiceProviderAccountNumber)) {
            throw new IllegalArgumentException("Remote service provider account number is null or empty.");
        }
        if (!isNumeric(remoteServiceProviderId)) {
            throw new IllegalArgumentException("Remote service provider id is not numeric.");
        }
        if (!isNumeric(remoteServiceProviderAccountNumber)) {
            throw new IllegalArgumentException("Remote service provider account number is not numeric.");
        }
        return realServiceProviderAccountNumber.matches("0*" + remoteServiceProviderId + "0*" + remoteServiceProviderAccountNumber)
                || realServiceProviderAccountNumber.equals(remoteServiceProviderId + "." + remoteServiceProviderAccountNumber)
                || realServiceProviderAccountNumber.equals(remoteServiceProviderId + "/" + remoteServiceProviderAccountNumber)
                || realServiceProviderAccountNumber.equals(remoteServiceProviderId + "\\" + remoteServiceProviderAccountNumber);
    }

    public static boolean matches(String realServiceProviderAccountNumber, String realLastName,
            String remoteServiceProviderAccountNumber, String remoteName) {
        if (isEmpty(realServiceProviderAccountNumber)) {
            throw new IllegalArgumentException("Real service provider account number is null or empty.");
        }
        if (isEmpty(realLastName)) {
            throw new IllegalArgumentException("Real last name is null or empty.");
        }
        if (isEmpty(remoteServiceProviderAccountNumber)) {
            throw new IllegalArgumentException("Remote service provider account number is null or empty.");
        }
        if (isEmpty(remoteName)) {
            return false;
        }
        return remoteName.trim().toUpperCase().startsWith(realLastName.trim().toUpperCase())
                && realServiceProviderAccountNumber.endsWith(remoteServiceProviderAccountNumber);
    }
}
