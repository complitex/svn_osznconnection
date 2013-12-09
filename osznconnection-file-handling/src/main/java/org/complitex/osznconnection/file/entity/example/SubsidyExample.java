package org.complitex.osznconnection.file.entity.example;

import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author Artem
 */
public class SubsidyExample extends AbstractRequestExample {

    private String rash;
    private String firstName;
    private String middleName;
    private String lastName;
    private String city;
    private String street;
    private String building;
    private String corp;
    private String apartment;
    private int start;
    private int size;
    private String orderByClause;
    private boolean asc;

    private Date DAT1, DAT2;
    private Integer NUMM;
    private BigDecimal NM_PAY, SUMMA, SUBS;

    public String getRash() {
        return rash;
    }

    public void setRash(String rash) {
        this.rash = rash;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getCorp() {
        return corp;
    }

    public void setCorp(String corp) {
        this.corp = corp;
    }

    public String getApartment() {
        return apartment;
    }

    public void setApartment(String apartment) {
        this.apartment = apartment;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public boolean isAsc() {
        return asc;
    }

    public void setAsc(boolean asc) {
        this.asc = asc;
    }

    public Date getDAT1() {
        return DAT1;
    }

    public void setDAT1(Date DAT1) {
        this.DAT1 = DAT1;
    }

    public Date getDAT2() {
        return DAT2;
    }

    public void setDAT2(Date DAT2) {
        this.DAT2 = DAT2;
    }

    public Integer getNUMM() {
        return NUMM;
    }

    public void setNUMM(Integer NUMM) {
        this.NUMM = NUMM;
    }

    public BigDecimal getNM_PAY() {
        return NM_PAY;
    }

    public void setNM_PAY(BigDecimal NM_PAY) {
        this.NM_PAY = NM_PAY;
    }

    public BigDecimal getSUMMA() {
        return SUMMA;
    }

    public void setSUMMA(BigDecimal SUMMA) {
        this.SUMMA = SUMMA;
    }

    public BigDecimal getSUBS() {
        return SUBS;
    }

    public void setSUBS(BigDecimal SUBS) {
        this.SUBS = SUBS;
    }
}
