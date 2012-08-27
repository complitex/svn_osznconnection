/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service.status.details;

import java.io.Serializable;
import java.util.Locale;
import org.complitex.dictionary.util.ResourceUtil;
import org.complitex.osznconnection.file.entity.RequestStatus;
import org.complitex.osznconnection.file.entity.StatusDetail;

/**
 *
 * @author Artem
 */
public class FacilityServiceTypeStatusDetailRenderer implements IStatusDetailRenderer, Serializable {

    private static final String RESOURCE_BUNDLE = FacilityServiceTypeStatusDetailRenderer.class.getName();

    @Override
    public String displayStatusDetail(RequestStatus status, StatusDetail statusDetail, Locale locale) {

        switch (status) {
            case ACCOUNT_NUMBER_NOT_FOUND:
            case MORE_ONE_ACCOUNTS: {
                return statusDetail.getDetail("fio");
            }
            case STREET_UNRESOLVED_LOCALLY:
            case STREET_UNRESOLVED:
            case STREET_TYPE_UNRESOLVED_LOCALLY:
            case STREET_TYPE_UNRESOLVED:
            case STREET_NOT_FOUND:
            case STREET_TYPE_NOT_FOUND: {
                return ResourceUtil.getFormatString(RESOURCE_BUNDLE, "streetCode", locale,
                        statusDetail.getDetail("streetCode"));
            }
            case BUILDING_UNRESOLVED_LOCALLY:
            case BUILDING_UNRESOLVED:
            case BUILDING_NOT_FOUND: {
                return ResourceUtil.getFormatString(RESOURCE_BUNDLE, "building", locale,
                        ResourceUtil.getFormatString(RESOURCE_BUNDLE, "streetCode", locale,
                        statusDetail.getDetail("streetCode")),
                        statusDetail.getDetail("building"));
            }
            case BUILDING_CORP_NOT_FOUND: {
                return ResourceUtil.getFormatString(RESOURCE_BUNDLE, "buildingWithCorp", locale,
                        ResourceUtil.getFormatString(RESOURCE_BUNDLE, "streetCode", locale,
                        statusDetail.getDetail("streetCode")),
                        statusDetail.getDetail("building"),
                        statusDetail.getDetail("buildingCorp"));
            }
            case APARTMENT_NOT_FOUND: {
                return ResourceUtil.getFormatString(RESOURCE_BUNDLE, "apartment", locale,
                        ResourceUtil.getFormatString(RESOURCE_BUNDLE, "buildingWithCorp", locale,
                        ResourceUtil.getFormatString(RESOURCE_BUNDLE, "streetCode", locale,
                        statusDetail.getDetail("streetCode")),
                        statusDetail.getDetail("building"),
                        statusDetail.getDetail("buildingCorp")),
                        statusDetail.getDetail("apartment"));
            }
        }
        return "";
    }
}
