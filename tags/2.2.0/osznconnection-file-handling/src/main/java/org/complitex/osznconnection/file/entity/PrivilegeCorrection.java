package org.complitex.osznconnection.file.entity;

import org.complitex.dictionary.entity.Correction;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.07.13 14:39
 */
public class PrivilegeCorrection extends Correction {
    public PrivilegeCorrection() {
    }

    public PrivilegeCorrection(String externalId, Long objectId, String privilege,
                               Long organizationId, Long userOrganizationId, Long moduleId) {
        setExternalId(externalId);
        setObjectId(objectId);
        setCorrection(privilege);
        setOrganizationId(organizationId);
        setModuleId(moduleId);
        setUserOrganizationId(userOrganizationId);
    }

    @Override
    public String getEntity() {
        return "privilege_correction";
    }
}
