package org.complitex.osznconnection.file.entity;

import org.complitex.dictionary.entity.DomainObject;
import org.complitex.dictionary.service.AbstractFilter;

import java.util.Date;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 03.11.2010 13:26:00
 *
 */
public class RequestFileFilter extends AbstractFilter {

    private Long id;
    private Long groupId;
    private Date loaded;
    private String name;
    private Long organizationId;
    private Integer registry;
    private Integer year;
    private Integer month;
    private String paymentName;
    private String benefitName;
    private Integer dbfRecordCount;
    private Long length;
    private String checkSum;
    private DomainObject organization;
    private DomainObject userOrganization;
    private RequestFile.TYPE type;
    private RequestFileStatus status;
    private String userOrganizationsString;
    private String outerOrganizationsString;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Date getLoaded() {
        return loaded;
    }

    public void setLoaded(Date loaded) {
        this.loaded = loaded;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Integer getRegistry() {
        return registry;
    }

    public void setRegistry(Integer registry) {
        this.registry = registry;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public String getPaymentName() {
        return paymentName;
    }

    public void setPaymentName(String paymentName) {
        this.paymentName = paymentName;
    }

    public String getBenefitName() {
        return benefitName;
    }

    public void setBenefitName(String benefitName) {
        this.benefitName = benefitName;
    }

    public Integer getDbfRecordCount() {
        return dbfRecordCount;
    }

    public void setDbfRecordCount(Integer dbfRecordCount) {
        this.dbfRecordCount = dbfRecordCount;
    }

    public Long getLength() {
        return length;
    }

    public void setLength(Long length) {
        this.length = length;
    }

    public String getCheckSum() {
        return checkSum;
    }

    public void setCheckSum(String checkSum) {
        this.checkSum = checkSum;
    }

    public DomainObject getOrganization() {
        return organization;
    }

    public void setOrganization(DomainObject organization) {
        this.organization = organization;
        this.organizationId = organization != null ? organization.getId() : null;
    }

    public RequestFile.TYPE getType() {
        return type;
    }

    public void setType(RequestFile.TYPE type) {
        this.type = type;
    }

    public RequestFileStatus getStatus() {
        return status;
    }

    public void setStatus(RequestFileStatus status) {
        this.status = status;
    }

    public String getUserOrganizationsString() {
        return userOrganizationsString;
    }

    public void setUserOrganizationsString(String userOrganizationsString) {
        this.userOrganizationsString = userOrganizationsString;
    }

    public String getOuterOrganizationsString() {
        return outerOrganizationsString;
    }

    public void setOuterOrganizationsString(String outerOrganizationsString) {
        this.outerOrganizationsString = outerOrganizationsString;
    }

    public DomainObject getUserOrganization() {
        return userOrganization;
    }

    public void setUserOrganization(DomainObject userOrganization) {
        this.userOrganization = userOrganization;
    }
}
