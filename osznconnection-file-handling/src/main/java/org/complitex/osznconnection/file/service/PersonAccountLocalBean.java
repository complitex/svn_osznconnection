/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import java.sql.SQLException;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.service.AbstractBean;
import org.complitex.osznconnection.file.entity.PersonAccount;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import org.apache.ibatis.session.SqlSession;
import org.complitex.dictionary.mysql.MySqlErrors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Класс для работы с локальной таблицей номеров л/c person_account.
 * @author Artem
 */
@Stateless(name = "PersonAccountLocalBean")
public class PersonAccountLocalBean extends AbstractBean {

    private static final Logger log = LoggerFactory.getLogger(PersonAccountLocalBean.class);
    private static final String MAPPING_NAMESPACE = PersonAccountLocalBean.class.getName();
    private static final String UPDATE_OPERATION = "update";

    public static enum OrderBy {

        FIRST_NAME("first_name"), MIDDLE_NAME("middle_name"), LAST_NAME("last_name"), CITY("city"), STREET("street"), STREET_CODE("street_code"),
        BUILDING_NUMBER("building_num"), BUILDING_CORP("building_corp"), APARTMENT("apartment"),
        ACCOUNT_NUMBER("account_number"), OWN_NUM_SR("own_num_sr"),
        OSZN("oszn"), CALCULATION_CENTER("calculation_center");
        private String orderBy;

        private OrderBy(String orderBy) {
            this.orderBy = orderBy;
        }

        public String getOrderBy() {
            return orderBy;
        }
    }
    @EJB
    private OsznSessionBean osznSessionBean;

    /**
     * Найти номер л/c в локальной таблице. Поиск идет по ФИО и адресу ОСЗН, текущему ЦН и ОСЗН,
     * причем для элементов адреса при поиске применяется SQL функция TRIM().
     * Если найдено более одной записи удовлетворяющей условиям поиска, то выбрасывается исключение.
     * @param calculationCenterId
     * @return
     */
    @Transactional
    public String findLocalAccountNumber(String firstName, String middleName, String lastName, String city, String streetType,
            String street, String streetCode, String buildingNumber, String buildingCorp, String apartment, String ownNumSr, long organizationId,
            long calculationCenterId) {

        PersonAccount example = new PersonAccount();
        example.setFirstName(firstName);
        example.setMiddleName(middleName);
        example.setLastName(lastName);
        example.setCity(city);
        example.setStreetType(streetType);
        example.setStreet(street);
        example.setStreetCode(streetCode);
        example.setBuildingNumber(buildingNumber);
        example.setBuildingCorp(buildingCorp);
        example.setApartment(apartment);
        example.setOwnNumSr(ownNumSr);
        example.setOsznId(organizationId);
        example.setCalculationCenterId(calculationCenterId);

        PersonAccount account = findAccount(example);
        return account != null ? account.getAccountNumber() : null;
    }

    @Transactional
    private PersonAccount findAccount(PersonAccount example) {
        List<PersonAccount> results = sqlSession().selectList(MAPPING_NAMESPACE + ".findAccountNumber", example);
        if (results.isEmpty()) {
            return null;
        } else if (results.size() == 1) {
            return results.get(0);
        } else {
            throw new RuntimeException("More one entry in person_account table with the same data. Table person_account is in inconsistent state!");
        }
    }

    /**
     * Сохранить номер л/c локально. Данные о ФИО и адресе сохраняются как есть, т.е. без применения функций TRIM или TO_CYRILLIC.
     * Перед вставкой проверяется - есть ли уже такая запись методом findLocalAccountNumber, и если есть, то обновляется, если нет - вставляется.
     * Если при проверке найдено более одной записи удовлетворяющей условиям поиска, то выбрасывается исключение.
     * @param calculationCenterId
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void insert(String accountNumber, String firstName, String middleName, String lastName, String city, String streetType,
            String street, String streetCode, String buildingNumber, String buildingCorp, String apartment, String ownNumSr, long organizationId,
            long calculationCenterId) {

        PersonAccount personAccount = new PersonAccount();
        personAccount.setFirstName(firstName);
        personAccount.setMiddleName(middleName);
        personAccount.setLastName(lastName);
        personAccount.setCity(city);
        personAccount.setStreetType(streetType);
        personAccount.setStreet(street);
        personAccount.setStreetCode(streetCode);
        personAccount.setBuildingNumber(buildingNumber);
        personAccount.setBuildingCorp(buildingCorp);
        personAccount.setApartment(apartment);
        personAccount.setOwnNumSr(ownNumSr);
        personAccount.setOsznId(organizationId);
        personAccount.setCalculationCenterId(calculationCenterId);
        personAccount.setAccountNumber(accountNumber);

        SqlSession insertSession = null;
        try {
            insertSession = getSqlSessionManager().openSession();
            insert(personAccount, insertSession);
            insertSession.commit();
        } catch (Exception insertExc) {
            //try to rollback
            try {
                if (insertSession != null) {
                    insertSession.rollback();
                }
            } catch (Exception rollExc) {
                log.error("Couldn't rollback insert transaction.", rollExc);
            }

            SQLException sqlException = null;
            Throwable t = insertExc;
            while (true) {
                if (t == null) {
                    break;
                }
                if (t instanceof SQLException) {
                    sqlException = (SQLException) t;
                    break;
                }
                t = t.getCause();
            }

            if (sqlException != null && MySqlErrors.isDublicateError(sqlException)) {
                //the same person account entry has already been inserted in parallel.
                //update person account entry.
                updateInSeparateTransaction(personAccount);
            } else {
                throw new RuntimeException(insertExc);
            }
        } finally {
            try {
                if (insertSession != null) {
                    insertSession.close();
                }
            } catch (Exception e) {
                log.error("Couldn't close insert sql session.", e);
            }
        }
    }

    @Transactional
    public int count(PersonAccount example) {
        example.setAdmin(osznSessionBean.isAdmin());
        example.setOrganizations(osznSessionBean.getAllOuterOrganizationString());
        return (Integer) sqlSession().selectOne(MAPPING_NAMESPACE + ".count", example);
    }

    @SuppressWarnings({"unchecked"})
    @Transactional
    public List<PersonAccount> find(PersonAccount example) {
        example.setAdmin(osznSessionBean.isAdmin());
        example.setOrganizations(osznSessionBean.getAllOuterOrganizationString());
        return sqlSession().selectList(MAPPING_NAMESPACE + ".find", example);
    }

    /**
     * Вставить новую запись PersonAccount.
     * Если значение корпуса null, то сохраняется пустая строка.
     * @param personAccount
     */
    private void insert(PersonAccount personAccount, SqlSession session) {
        if (personAccount.getBuildingCorp() == null) {
            personAccount.setBuildingCorp("");
        }
        session.insert(MAPPING_NAMESPACE + ".insert", personAccount);
    }

    @Transactional
    public void update(PersonAccount personAccount) {
        sqlSession().update(MAPPING_NAMESPACE + "." + UPDATE_OPERATION, personAccount);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    private void updateInSeparateTransaction(PersonAccount personAccount) {
        PersonAccount dbAccount = findAccount(personAccount);
        if (dbAccount != null) {
            personAccount.setId(dbAccount.getId());
            SqlSession updateSession = null;
            try {
                updateSession = getSqlSessionManager().openSession();
                updateSession.update(MAPPING_NAMESPACE + "." + UPDATE_OPERATION, personAccount);
                updateSession.commit();
            } catch (Exception updateExc) {
                //try to rollback
                try {
                    if (updateSession != null) {
                        updateSession.rollback();
                    }
                } catch (Exception rollExc) {
                    log.error("Couldn't rollback update transaction.", rollExc);
                }
                throw new RuntimeException(updateExc);
            } finally {
                try {
                    if (updateSession != null) {
                        updateSession.close();
                    }
                } catch (Exception exc) {
                    log.error("Couldn't close update sql session.", exc);
                }
            }
        }
    }

    @Transactional
    public PersonAccount findById(long id) {
        return (PersonAccount) sqlSession().selectOne(MAPPING_NAMESPACE + ".findById", id);
    }

    @Transactional
    public void delete(PersonAccount personAccount) {
        sqlSession().delete(MAPPING_NAMESPACE + ".delete", personAccount);
    }
}
