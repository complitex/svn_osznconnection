/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service.status.details;

import java.io.Serializable;
import java.util.Locale;
import org.complitex.osznconnection.file.entity.RequestStatus;
import org.complitex.osznconnection.file.entity.StatusDetail;

/**
 *
 * @author Artem
 */
public class PaymentBenefitStatusDetailRenderer implements IStatusDetailRenderer, Serializable {

    @Override
    public String displayStatusDetail(RequestStatus status, StatusDetail statusDetail, Locale locale) {
        switch (status) {
            case ACCOUNT_NUMBER_NOT_FOUND:
            case MORE_ONE_ACCOUNTS: {
                return statusDetail.getDetail("account");
            }
            case CITY_UNRESOLVED_LOCALLY:
            case CITY_UNRESOLVED:
            case CITY_NOT_FOUND: {
                return statusDetail.getDetail("city");
            }
            case STREET_UNRESOLVED_LOCALLY:
            case STREET_UNRESOLVED:
            case STREET_TYPE_UNRESOLVED:
            case STREET_NOT_FOUND:
            case STREET_TYPE_NOT_FOUND: {
                String city = statusDetail.getDetail("city");
                String street = statusDetail.getDetail("street");
                return city + ", " + street;
            }
            case BUILDING_UNRESOLVED_LOCALLY:
            case BUILDING_UNRESOLVED:
            case BUILDING_NOT_FOUND: {
                String city = statusDetail.getDetail("city");
                String street = statusDetail.getDetail("street");
                String building = statusDetail.getDetail("building");
                return city + ", " + street + ", " + building;
            }
            case BUILDING_CORP_NOT_FOUND: {
                String city = statusDetail.getDetail("city");
                String street = statusDetail.getDetail("street");
                String building = statusDetail.getDetail("building");
                String buildingCorp = statusDetail.getDetail("buildingCorp");
                return city + ", " + street + ", " + building + ", " + buildingCorp;
            }
            case APARTMENT_NOT_FOUND: {
                String city = statusDetail.getDetail("city");
                String street = statusDetail.getDetail("street");
                String building = statusDetail.getDetail("building");
                String buildingCorp = statusDetail.getDetail("buildingCorp");
                String apartment = statusDetail.getDetail("apartment");
                return city + ", " + street + ", " + building + ", " + buildingCorp + ", " + apartment;
            }
        }
        return "";
    }
}
