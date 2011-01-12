/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service.status.details;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import org.complitex.osznconnection.file.entity.ActualPayment;
import org.complitex.osznconnection.file.entity.Benefit;
import org.complitex.osznconnection.file.entity.Payment;
import org.complitex.osznconnection.file.entity.RequestStatus;
import org.complitex.osznconnection.file.entity.StatusDetail;

/**
 *
 * @author Artem
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class StatusDetailRenderer {

    public String displayStatusDetail(Class<?> requestClass, StatusDetail statusDetail, RequestStatus status) {
        String countString = displayCount(statusDetail.getCount());
        if (Payment.class.equals(requestClass) || Benefit.class.equals(requestClass)) {// payment and benefit related statuses.
            switch (status) {
                case ACCOUNT_NUMBER_NOT_FOUND:
                case MORE_ONE_ACCOUNTS: {
                    String account = statusDetail.getDetail("account");
                    return account + countString;
                }
                case CITY_UNRESOLVED_LOCALLY:
                case CITY_UNRESOLVED:
                case CITY_NOT_FOUND: {
                    String city = statusDetail.getDetail("city");
                    return city + countString;
                }
                case STREET_UNRESOLVED_LOCALLY:
                case STREET_UNRESOLVED:
                case STREET_TYPE_UNRESOLVED:
                case STREET_NOT_FOUND:
                case STREET_TYPE_NOT_FOUND: {
                    String city = statusDetail.getDetail("city");
                    String street = statusDetail.getDetail("street");
                    return city + ", " + street + countString;
                }
                case BUILDING_UNRESOLVED_LOCALLY:
                case BUILDING_UNRESOLVED:
                case BUILDING_NOT_FOUND: {
                    String city = statusDetail.getDetail("city");
                    String street = statusDetail.getDetail("street");
                    String building = statusDetail.getDetail("building");
                    return city + ", " + street + ", " + building + countString;
                }
                case BUILDING_CORP_NOT_FOUND: {
                    String city = statusDetail.getDetail("city");
                    String street = statusDetail.getDetail("street");
                    String building = statusDetail.getDetail("building");
                    String buildingCorp = statusDetail.getDetail("buildingCorp");
                    return city + ", " + street + ", " + building + ", " + buildingCorp + countString;
                }
                case APARTMENT_NOT_FOUND: {
                    String city = statusDetail.getDetail("city");
                    String street = statusDetail.getDetail("street");
                    String building = statusDetail.getDetail("building");
                    String buildingCorp = statusDetail.getDetail("buildingCorp");
                    String apartment = statusDetail.getDetail("apartment");
                    return city + ", " + street + ", " + building + ", " + buildingCorp + ", " + apartment + countString;
                }
            }
        } else if (ActualPayment.class.equals(requestClass)) {// actualPayment related statuses.
            switch (status) {
                case ACCOUNT_NUMBER_NOT_FOUND:
                case MORE_ONE_ACCOUNTS: {
                    String lastName = statusDetail.getDetail("lastName");
                    String firstName = statusDetail.getDetail("firstName");
                    String middleName = statusDetail.getDetail("middleName");
                    return lastName + " " + firstName + " " + middleName + countString;
                }
                case CITY_UNRESOLVED_LOCALLY:
                case CITY_UNRESOLVED:
                case CITY_NOT_FOUND: {
                    String city = statusDetail.getDetail("city");
                    return city + countString;
                }
                case STREET_UNRESOLVED_LOCALLY:
                case STREET_UNRESOLVED:
                case STREET_TYPE_UNRESOLVED_LOCALLY:
                case STREET_TYPE_UNRESOLVED:
                case STREET_NOT_FOUND:
                case STREET_TYPE_NOT_FOUND: {
                    String city = statusDetail.getDetail("city");
                    String street = statusDetail.getDetail("street");
                    String streetType = statusDetail.getDetail("streetType");
                    return city + ", " + street + ", " + streetType + countString;
                }
                case BUILDING_UNRESOLVED_LOCALLY:
                case BUILDING_UNRESOLVED:
                case BUILDING_NOT_FOUND: {
                    String city = statusDetail.getDetail("city");
                    String street = statusDetail.getDetail("street");
                    String streetType = statusDetail.getDetail("streetType");
                    String building = statusDetail.getDetail("building");
                    return city + ", " + street + ", " + streetType + ", " + building + countString;
                }
                case BUILDING_CORP_NOT_FOUND: {
                    String city = statusDetail.getDetail("city");
                    String street = statusDetail.getDetail("street");
                    String streetType = statusDetail.getDetail("streetType");
                    String building = statusDetail.getDetail("building");
                    String buildingCorp = statusDetail.getDetail("buildingCorp");
                    return city + ", " + street + ", " + streetType + ", " + building + ", " + buildingCorp + countString;
                }
                case APARTMENT_NOT_FOUND: {
                    String city = statusDetail.getDetail("city");
                    String street = statusDetail.getDetail("street");
                    String streetType = statusDetail.getDetail("streetType");
                    String building = statusDetail.getDetail("building");
                    String buildingCorp = statusDetail.getDetail("buildingCorp");
                    String apartment = statusDetail.getDetail("apartment");
                    return city + ", " + street + ", " + streetType + ", " + building + ", " + buildingCorp + ", " + apartment + countString;
                }
            }
        }
        return countString;
    }

    public String displayCount(Long count) {
        return count > 1 ? " (" + count + ")" : "";
    }
}
