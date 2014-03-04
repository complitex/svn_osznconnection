package org.complitex.osznconnection.file.entity;

import org.complitex.dictionary.entity.Correction;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.07.13 14:37
 */
public class OwnershipCorrection extends Correction {
    public OwnershipCorrection() {
    }

    public OwnershipCorrection(String externalId, Long objectId, String ownership,
                               Long organizationId, Long userOrganizationId, Long moduleId) {
        setExternalId(externalId);
        setObjectId(objectId);
        setCorrection(ownership);
        setOrganizationId(organizationId);
        setUserOrganizationId(userOrganizationId);
        setModuleId(moduleId);
    }

    @Override
    public String getEntity() {
        return "ownership_correction";
    }
}
