/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service.status.details;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import org.complitex.osznconnection.file.entity.RequestStatus;
import org.complitex.osznconnection.file.entity.StatusDetail;

/**
 *
 * @author Artem
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class StatusDetailRenderer {

    public String displayStatusDetail(StatusDetail statusDetail, RequestStatus status) {
        String countString = displayCount(statusDetail.getCount());
        switch (status) {
            // payment and benefit related statuses.
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
        return countString;
    }

    public String displayCount(Long count) {
        return count > 1 ? " (" + count + ")" : "";
    }
}
