/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service;

import com.google.common.collect.Lists;
import org.apache.ibatis.session.SqlSession;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.mybatis.Transactional;
import org.complitex.dictionary.mysql.MySqlErrors;
import org.complitex.dictionary.service.AbstractBean;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.entity.example.PersonAccountExample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.sql.SQLException;
import java.util.List;

/**
 * Класс для работы с локальной таблицей номеров л/c person_account.
 * @author Artem
 */
@Stateless(name = "PersonAccountLocalBean")
public class PersonAccountLocalBean extends AbstractBean {

    private static final Logger log = LoggerFactory.getLogger(PersonAccountLocalBean.class);
    private static final String MAPPING_NAMESPACE = PersonAccountLocalBean.class.getName();

    public static class MoreOneAccountException extends Exception {
    }

    public static enum OrderBy {

        FIRST_NAME("first_name"), MIDDLE_NAME("middle_name"), LAST_NAME("last_name"), CITY("city"), STREET("street"),
        BUILDING_NUMBER("building_num"), BUILDING_CORP("building_corp"), APARTMENT("apartment"),
        ACCOUNT_NUMBER("account_number"), OSZN("oszn"), CALCULATION_CENTER("calculation_center"),
        USER_ORGANIZATION("user_organization"), PU_ACCOUNT_NUMBER("pu_account_number");
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

    private PersonAccountExample newExample(String firstName, String middleName, String lastName, String city,
            String street, String buildingNumber, String buildingCorp, String apartment, long osznId, long calculationCenterId,
            String puAccountNumber, Long userOrganizationId) {
        PersonAccountExample example = new PersonAccountExample();
        example.setFirstName(firstName);
        example.setMiddleName(middleName);
        example.setLastName(lastName);
        example.setCity(city);
        example.setStreet(street);
        example.setBuildingNumber(buildingNumber);
        example.setBuildingCorp(buildingCorp);
        example.setApartment(apartment);
        example.setOsznId(osznId);
        example.setCalculationCenterId(calculationCenterId);
        example.setPuAccountNumber(puAccountNumber);
        example.setUserOrganizationId(userOrganizationId);
        return example;
    }

    private List<PersonAccount> findAccounts(String firstName, String middleName, String lastName, String city,
            String street, String buildingNumber, String buildingCorp, String apartment, long osznId, long calculationCenterId,
            String puAccountNumber, Long userOrganizationId, boolean blocking, SqlSession session) {
        PersonAccountExample example = newExample(firstName, middleName, lastName, city, street, buildingNumber,
                buildingCorp, apartment, osznId, calculationCenterId, puAccountNumber, userOrganizationId);
        if (blocking) {
            return session.selectList(MAPPING_NAMESPACE + ".findAccountsBlocking", example);
        } else {
            return session.selectList(MAPPING_NAMESPACE + ".findAccounts", example);
        }
    }

    private List<PersonAccount> findAccountsLikeName(String firstName, String middleName, String lastName, String city,
            String street, String buildingNumber, String buildingCorp, String apartment, long osznId, long calculationCenterId,
            String puAccountNumber, Long userOrganizationId, boolean blocking, SqlSession session) {
        PersonAccountExample example = newExample(firstName, middleName, lastName, city, street, buildingNumber,
                buildingCorp, apartment, osznId, calculationCenterId, puAccountNumber, userOrganizationId);

        if (blocking) {
            return session.selectList(MAPPING_NAMESPACE + ".findAccountsLikeNameBlocking", example);
        } else {
            return session.selectList(MAPPING_NAMESPACE + ".findAccountsLikeName", example);
        }
    }

    private String haveTheSameAccountNumber(List<PersonAccount> accounts) {
        if (accounts == null || accounts.isEmpty()) {
            throw new IllegalArgumentException("Accounts can't be null or empty.");
        }

        String firstAccountNumber = accounts.get(0).getAccountNumber();
        for (PersonAccount account : accounts) {
            if (!firstAccountNumber.equals(account.getAccountNumber())) {
                return null;
            }
        }
        return firstAccountNumber;
    }

    @Transactional
    public String findLocalAccountNumber(Payment payment, long calculationCenterId, long userOrganizationId)
            throws MoreOneAccountException {
        List<PersonAccount> accounts = findAccounts(payment.getStringField(PaymentDBF.F_NAM),
                payment.getStringField(PaymentDBF.M_NAM), payment.getStringField(PaymentDBF.SUR_NAM),
                payment.getStringField(PaymentDBF.N_NAME), payment.getStringField(PaymentDBF.VUL_NAME),
                payment.getStringField(PaymentDBF.BLD_NUM), payment.getStringField(PaymentDBF.CORP_NUM),
                payment.getStringField(PaymentDBF.FLAT), payment.getOrganizationId(), calculationCenterId,
                payment.getStringField(PaymentDBF.OWN_NUM_SR), userOrganizationId, false, sqlSession());
        if (accounts.isEmpty()) {
            return null;
        } else if (accounts.size() == 1) {
            PersonAccount account = accounts.get(0);
            return account.getAccountNumber();
        } else {
            String accountNumber = haveTheSameAccountNumber(accounts);
            if (accountNumber != null) {
                return accountNumber;
            } else {
                throw new MoreOneAccountException();
            }
        }
    }

    @Transactional
    public String findLocalAccountNumber(ActualPayment actualPayment, long calculationCenterId, long userOrganizationId)
            throws MoreOneAccountException {
        List<PersonAccount> accounts = findAccounts(actualPayment.getStringField(ActualPaymentDBF.F_NAM),
                actualPayment.getStringField(ActualPaymentDBF.M_NAM), actualPayment.getStringField(ActualPaymentDBF.SUR_NAM),
                actualPayment.getStringField(ActualPaymentDBF.N_NAME), actualPayment.getStringField(ActualPaymentDBF.VUL_NAME),
                actualPayment.getStringField(ActualPaymentDBF.BLD_NUM), actualPayment.getStringField(ActualPaymentDBF.CORP_NUM),
                actualPayment.getStringField(ActualPaymentDBF.FLAT),
                actualPayment.getOrganizationId(), calculationCenterId, actualPayment.getStringField(ActualPaymentDBF.OWN_NUM),
                userOrganizationId, false, sqlSession());
        String currentStreetType = actualPayment.getStringField(ActualPaymentDBF.VUL_CAT);
        if (currentStreetType != null) {
            currentStreetType = currentStreetType.toUpperCase();
        }

        if (accounts.isEmpty()) {
            return null;
        } else if (accounts.size() == 1) {
            PersonAccount account = accounts.get(0);
            if (!Strings.isEmpty(account.getStreetType())) {
                return account.getStreetType().equals(currentStreetType) ? account.getAccountNumber() : null;
            } else {
                account.setStreetType(currentStreetType);
                updateAccountNumberAndStreetType(account, sqlSession());
                return account.getAccountNumber();
            }
        } else {
            // find with current street type
            List<PersonAccount> withStreetType = Lists.newArrayList();
            for (PersonAccount account : accounts) {
                if (Strings.isEqual(account.getStreetType(), currentStreetType)) {
                    withStreetType.add(account);
                }
            }

            if (withStreetType.isEmpty()) {
                String accountNumber = haveTheSameAccountNumber(accounts);
                if (accountNumber != null) {
                    return accountNumber;
                } else {
                    throw new MoreOneAccountException();
                }
            } else if (withStreetType.size() == 1) {
                return withStreetType.get(0).getAccountNumber();
            } else {
                String accountNumber = haveTheSameAccountNumber(withStreetType);
                if (accountNumber != null) {
                    return accountNumber;
                } else {
                    throw new MoreOneAccountException();
                }
            }
        }
    }

    @Transactional
    public String findLocalAccountNumber(Subsidy subsidy, long calculationCenterId, long userOrganizationId)
            throws MoreOneAccountException {
        List<PersonAccount> accounts = findAccountsLikeName(subsidy.getFirstName(),
                subsidy.getMiddleName(), subsidy.getLastName(),
                subsidy.getStringField(SubsidyDBF.NP_NAME), subsidy.getStringField(SubsidyDBF.NAME_V),
                subsidy.getStringField(SubsidyDBF.BLD), subsidy.getStringField(SubsidyDBF.CORP),
                subsidy.getStringField(SubsidyDBF.FLAT), subsidy.getOrganizationId(), calculationCenterId,
                subsidy.getStringField(SubsidyDBF.RASH), userOrganizationId, false, sqlSession());
        String currentStreetType = subsidy.getStringField(SubsidyDBF.CAT_V);
        if (currentStreetType != null) {
            currentStreetType = currentStreetType.toUpperCase();
        }

        if (accounts.isEmpty()) {
            return null;
        } else if (accounts.size() == 1) {
            PersonAccount account = accounts.get(0);
            if (!Strings.isEmpty(account.getStreetType())) {
                return account.getStreetType().equals(currentStreetType) ? account.getAccountNumber() : null;
            } else {
                account.setStreetType(currentStreetType);
                updateAccountNumberAndStreetType(account, sqlSession());
                return account.getAccountNumber();
            }
        } else {
            // find with current street type
            List<PersonAccount> withStreetType = Lists.newArrayList();
            for (PersonAccount account : accounts) {
                if (Strings.isEqual(account.getStreetType(), currentStreetType)) {
                    withStreetType.add(account);
                }
            }

            if (withStreetType.isEmpty()) {
                String accountNumber = haveTheSameAccountNumber(accounts);
                if (accountNumber != null) {
                    return accountNumber;
                } else {
                    throw new MoreOneAccountException();
                }
            } else if (withStreetType.size() == 1) {
                return withStreetType.get(0).getAccountNumber();
            } else {
                String accountNumber = haveTheSameAccountNumber(withStreetType);
                if (accountNumber != null) {
                    return accountNumber;
                } else {
                    throw new MoreOneAccountException();
                }
            }
        }
    }

    @Transactional
    public String findLocalAccountNumber(DwellingCharacteristics dwellingCharacteristics, long calculationCenterId,
                                         long userOrganizationId) throws MoreOneAccountException {
        List<PersonAccount> accounts = findAccountsLikeName(dwellingCharacteristics.getFirstName(),
                dwellingCharacteristics.getMiddleName(),
                dwellingCharacteristics.getLastName(),
                dwellingCharacteristics.getCity(),
                dwellingCharacteristics.getStreet(),
                dwellingCharacteristics.getStringField(DwellingCharacteristicsDBF.HOUSE),
                dwellingCharacteristics.getStringField(DwellingCharacteristicsDBF.BUILD),
                dwellingCharacteristics.getStringField(DwellingCharacteristicsDBF.APT),
                dwellingCharacteristics.getOrganizationId(),
                calculationCenterId,
                dwellingCharacteristics.getStringField(DwellingCharacteristicsDBF.IDCODE),
                userOrganizationId,
                false,
                sqlSession());

        String currentStreetType = dwellingCharacteristics.getStreetType();

        if (currentStreetType != null) {
            currentStreetType = currentStreetType.toUpperCase();
        }

        if (accounts.isEmpty()) {
            return null;
        } else if (accounts.size() == 1) {
            PersonAccount account = accounts.get(0);
            if (!Strings.isEmpty(account.getStreetType())) {
                return account.getStreetType().equals(currentStreetType) ? account.getAccountNumber() : null;
            } else {
                account.setStreetType(currentStreetType);
                updateAccountNumberAndStreetType(account, sqlSession());
                return account.getAccountNumber();
            }
        } else {
            // find with current street type
            List<PersonAccount> withStreetType = Lists.newArrayList();
            for (PersonAccount account : accounts) {
                if (Strings.isEqual(account.getStreetType(), currentStreetType)) {
                    withStreetType.add(account);
                }
            }

            if (withStreetType.isEmpty()) {
                String accountNumber = haveTheSameAccountNumber(accounts);
                if (accountNumber != null) {
                    return accountNumber;
                } else {
                    throw new MoreOneAccountException();
                }
            } else if (withStreetType.size() == 1) {
                return withStreetType.get(0).getAccountNumber();
            } else {
                String accountNumber = haveTheSameAccountNumber(withStreetType);
                if (accountNumber != null) {
                    return accountNumber;
                } else {
                    throw new MoreOneAccountException();
                }
            }
        }
    }

    @Transactional
    public String findLocalAccountNumber(FacilityServiceType facilityServiceType, long calculationCenterId, long userOrganizationId)
            throws MoreOneAccountException {
        List<PersonAccount> accounts = findAccountsLikeName(facilityServiceType.getFirstName(),
                facilityServiceType.getMiddleName(), facilityServiceType.getLastName(),
                facilityServiceType.getCity(), facilityServiceType.getStreet(),
                facilityServiceType.getStringField(FacilityServiceTypeDBF.HOUSE),
                facilityServiceType.getStringField(FacilityServiceTypeDBF.BUILD),
                facilityServiceType.getStringField(FacilityServiceTypeDBF.APT),
                facilityServiceType.getOrganizationId(), calculationCenterId,
                facilityServiceType.getStringField(FacilityServiceTypeDBF.IDCODE), userOrganizationId, false, sqlSession());
        String currentStreetType = facilityServiceType.getStreetType();
        if (currentStreetType != null) {
            currentStreetType = currentStreetType.toUpperCase();
        }

        if (accounts.isEmpty()) {
            return null;
        } else if (accounts.size() == 1) {
            PersonAccount account = accounts.get(0);
            if (!Strings.isEmpty(account.getStreetType())) {
                return account.getStreetType().equals(currentStreetType) ? account.getAccountNumber() : null;
            } else {
                account.setStreetType(currentStreetType);
                updateAccountNumberAndStreetType(account, sqlSession());
                return account.getAccountNumber();
            }
        } else {
            // find with current street type
            List<PersonAccount> withStreetType = Lists.newArrayList();
            for (PersonAccount account : accounts) {
                if (Strings.isEqual(account.getStreetType(), currentStreetType)) {
                    withStreetType.add(account);
                }
            }

            if (withStreetType.isEmpty()) {
                String accountNumber = haveTheSameAccountNumber(accounts);
                if (accountNumber != null) {
                    return accountNumber;
                } else {
                    throw new MoreOneAccountException();
                }
            } else if (withStreetType.size() == 1) {
                return withStreetType.get(0).getAccountNumber();
            } else {
                String accountNumber = haveTheSameAccountNumber(withStreetType);
                if (accountNumber != null) {
                    return accountNumber;
                } else {
                    throw new MoreOneAccountException();
                }
            }
        }
    }

    private static interface TransactionCallback {

        void doInTransaction(SqlSession session);
    }

    private void handleTransaction(TransactionCallback transactionTemplate) {
        SqlSession session = null;
        try {
            session = getSqlSessionManager().openSession();
            transactionTemplate.doInTransaction(session);
            session.commit();
        } catch (Exception e) {
            //try to rollback
            try {
                if (session != null) {
                    session.rollback();
                }
            } catch (Exception rollExc) {
                log.error("Couldn't rollback insert transaction.", rollExc);
            }

            SQLException sqlException = null;
            Throwable t = e;
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

            if (sqlException != null && MySqlErrors.isDuplicateError(sqlException)) {
                //the same person account entry has already been inserted in parallel.
                //doing nothing. Maybe some action should be taken.
            } else {
                throw new RuntimeException(e);
            }
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (Exception e) {
                log.error("Couldn't close insert sql session.", e);
            }
        }
    }

    private PersonAccount newPersonAccount(Payment payment, String accountNumber, long calculationCenterId,
            long userOrganizationId) {
        PersonAccount personAccount = new PersonAccount();
        personAccount.setFirstName(payment.getStringField(PaymentDBF.F_NAM));
        personAccount.setMiddleName(payment.getStringField(PaymentDBF.M_NAM));
        personAccount.setLastName(payment.getStringField(PaymentDBF.SUR_NAM));
        personAccount.setCity(payment.getStringField(PaymentDBF.N_NAME));
        personAccount.setStreet(payment.getStringField(PaymentDBF.VUL_NAME));
        personAccount.setBuildingNumber(payment.getStringField(PaymentDBF.BLD_NUM));
        personAccount.setBuildingCorp(payment.getStringField(PaymentDBF.CORP_NUM));
        personAccount.setApartment(payment.getStringField(PaymentDBF.FLAT));
        personAccount.setOsznId(payment.getOrganizationId());
        personAccount.setCalculationCenterId(calculationCenterId);
        personAccount.setPuAccountNumber(payment.getStringField(PaymentDBF.OWN_NUM_SR));
        personAccount.setAccountNumber(accountNumber);
        personAccount.setUserOrganizationId(userOrganizationId);
        return personAccount;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void saveOrUpdate(final Payment payment, final long calculationCenterId, final long userOrganizationId) {
        handleTransaction(new TransactionCallback() {

            @Override
            public void doInTransaction(SqlSession session) {
                String newAccountNumber = payment.getAccountNumber();
                PersonAccount newPersonAccount = newPersonAccount(payment, newAccountNumber, calculationCenterId, userOrganizationId);
                List<PersonAccount> accounts = findAccounts(payment.getStringField(PaymentDBF.F_NAM),
                        payment.getStringField(PaymentDBF.M_NAM), payment.getStringField(PaymentDBF.SUR_NAM),
                        payment.getStringField(PaymentDBF.N_NAME), payment.getStringField(PaymentDBF.VUL_NAME),
                        payment.getStringField(PaymentDBF.BLD_NUM), payment.getStringField(PaymentDBF.CORP_NUM),
                        payment.getStringField(PaymentDBF.FLAT), payment.getOrganizationId(), calculationCenterId,
                        payment.getStringField(PaymentDBF.OWN_NUM_SR), userOrganizationId, false, session);
                if (accounts.isEmpty()) {
                    insert(newPersonAccount, session);
                } else if (accounts.size() == 1) {
                    PersonAccount account = accounts.get(0);
                    account.setAccountNumber(newAccountNumber);
                    updateAccountNumber(account, session);
                } else {
                    String accountNumber = haveTheSameAccountNumber(accounts);
                    if (accountNumber != null) {
                        for (PersonAccount account : accounts) {
                            account.setAccountNumber(newAccountNumber);
                            updateAccountNumber(account, session);
                        }
                    } else {
                        // Do nothing.
                    }
                }
            }
        });
    }

    private PersonAccount newPersonAccount(ActualPayment actualPayment, String accountNumber, long calculationCenterId,
            long userOrganizationId) {
        PersonAccount personAccount = new PersonAccount();
        personAccount.setFirstName(actualPayment.getStringField(ActualPaymentDBF.F_NAM));
        personAccount.setMiddleName(actualPayment.getStringField(ActualPaymentDBF.M_NAM));
        personAccount.setLastName(actualPayment.getStringField(ActualPaymentDBF.SUR_NAM));
        personAccount.setCity(actualPayment.getStringField(ActualPaymentDBF.N_NAME));
        personAccount.setStreet(actualPayment.getStringField(ActualPaymentDBF.VUL_NAME));
        personAccount.setBuildingNumber(actualPayment.getStringField(ActualPaymentDBF.BLD_NUM));
        personAccount.setBuildingCorp(actualPayment.getStringField(ActualPaymentDBF.CORP_NUM));
        personAccount.setApartment(actualPayment.getStringField(ActualPaymentDBF.FLAT));
        personAccount.setStreetType(actualPayment.getStringField(ActualPaymentDBF.VUL_CAT));
        personAccount.setOsznId(actualPayment.getOrganizationId());
        personAccount.setCalculationCenterId(calculationCenterId);
        personAccount.setPuAccountNumber(actualPayment.getStringField(ActualPaymentDBF.OWN_NUM));
        personAccount.setAccountNumber(accountNumber);
        personAccount.setUserOrganizationId(userOrganizationId);
        return personAccount;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void saveOrUpdate(final ActualPayment actualPayment, final long calculationCenterId, final long userOrganizationId) {
        handleTransaction(new TransactionCallback() {

            @Override
            public void doInTransaction(SqlSession session) {
                String newAccountNumber = actualPayment.getAccountNumber();
                PersonAccount newPersonAccount = newPersonAccount(actualPayment, newAccountNumber, calculationCenterId, userOrganizationId);
                List<PersonAccount> accounts = findAccounts(actualPayment.getStringField(ActualPaymentDBF.F_NAM),
                        actualPayment.getStringField(ActualPaymentDBF.M_NAM), actualPayment.getStringField(ActualPaymentDBF.SUR_NAM),
                        actualPayment.getStringField(ActualPaymentDBF.N_NAME), actualPayment.getStringField(ActualPaymentDBF.VUL_NAME),
                        actualPayment.getStringField(ActualPaymentDBF.BLD_NUM), actualPayment.getStringField(ActualPaymentDBF.CORP_NUM),
                        actualPayment.getStringField(ActualPaymentDBF.FLAT), actualPayment.getOrganizationId(), calculationCenterId,
                        actualPayment.getStringField(ActualPaymentDBF.OWN_NUM), userOrganizationId, false, session);

                String currentStreetType = actualPayment.getStringField(ActualPaymentDBF.VUL_CAT);
                if (currentStreetType != null) {
                    currentStreetType = currentStreetType.toUpperCase();
                }

                if (accounts.isEmpty()) {
                    insert(newPersonAccount, session);
                } else if (accounts.size() == 1) {
                    PersonAccount account = accounts.get(0);
                    if (!Strings.isEmpty(account.getStreetType())) {
                        if (account.getStreetType().equals(currentStreetType)) {
                            account.setAccountNumber(newAccountNumber);
                            updateAccountNumber(account, session);
                        } else {
                            insert(newPersonAccount, session);
                        }
                    } else {
                        account.setAccountNumber(newAccountNumber);
                        account.setStreetType(currentStreetType);
                        updateAccountNumberAndStreetType(account, session);
                    }
                } else {
                    // find with current street type
                    List<PersonAccount> withStreetType = Lists.newArrayList();
                    for (PersonAccount account : accounts) {
                        if (Strings.isEqual(account.getStreetType(), currentStreetType)) {
                            withStreetType.add(account);
                        }
                    }

                    if (withStreetType.isEmpty()) {
                        String accountNumber = haveTheSameAccountNumber(accounts);
                        if (accountNumber != null) {
                            for (PersonAccount account : accounts) {
                                account.setAccountNumber(newAccountNumber);
                                updateAccountNumber(account, session);
                            }
                        } else {
                            // Do nothing.
                        }
                    } else if (withStreetType.size() == 1) {
                        PersonAccount account = withStreetType.get(0);
                        account.setAccountNumber(newAccountNumber);
                        updateAccountNumber(account, session);
                    } else {
                        String accountNumber = haveTheSameAccountNumber(withStreetType);
                        if (accountNumber != null) {
                            for (PersonAccount account : accounts) {
                                account.setAccountNumber(newAccountNumber);
                                updateAccountNumber(account, session);
                            }
                        } else {
                            // Do nothing.
                        }
                    }
                }
            }
        });
    }

    private PersonAccount newPersonAccount(Subsidy subsidy, String accountNumber, long calculationCenterId,
            long userOrganizationId) {
        PersonAccount personAccount = new PersonAccount();
        personAccount.setFirstName(subsidy.getFirstName());
        personAccount.setMiddleName(subsidy.getMiddleName());
        personAccount.setLastName(subsidy.getLastName());
        personAccount.setCity(subsidy.getStringField(SubsidyDBF.NP_NAME));
        personAccount.setStreet(subsidy.getStringField(SubsidyDBF.NAME_V));
        personAccount.setBuildingNumber(subsidy.getStringField(SubsidyDBF.BLD));
        personAccount.setBuildingCorp(subsidy.getStringField(SubsidyDBF.CORP));
        personAccount.setApartment(subsidy.getStringField(SubsidyDBF.FLAT));
        personAccount.setStreetType(subsidy.getStringField(SubsidyDBF.CAT_V));
        personAccount.setOsznId(subsidy.getOrganizationId());
        personAccount.setCalculationCenterId(calculationCenterId);
        personAccount.setPuAccountNumber(subsidy.getStringField(SubsidyDBF.RASH));
        personAccount.setAccountNumber(accountNumber);
        personAccount.setUserOrganizationId(userOrganizationId);
        return personAccount;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void saveOrUpdate(final Subsidy subsidy, final long calculationCenterId, final long userOrganizationId) {
        handleTransaction(new TransactionCallback() {

            @Override
            public void doInTransaction(SqlSession session) {
                String newAccountNumber = subsidy.getAccountNumber();
                PersonAccount newPersonAccount = newPersonAccount(subsidy, newAccountNumber, calculationCenterId, userOrganizationId);
                List<PersonAccount> accounts = findAccountsLikeName(subsidy.getFirstName(),
                        subsidy.getMiddleName(), subsidy.getLastName(),
                        subsidy.getStringField(SubsidyDBF.NP_NAME), subsidy.getStringField(SubsidyDBF.NAME_V),
                        subsidy.getStringField(SubsidyDBF.BLD), subsidy.getStringField(SubsidyDBF.CORP),
                        subsidy.getStringField(SubsidyDBF.FLAT), subsidy.getOrganizationId(), calculationCenterId,
                        subsidy.getStringField(SubsidyDBF.RASH), userOrganizationId, false, session);
                String currentStreetType = subsidy.getStringField(SubsidyDBF.CAT_V);
                if (currentStreetType != null) {
                    currentStreetType = currentStreetType.toUpperCase();
                }

                if (accounts.isEmpty()) {
                    insert(newPersonAccount, session);
                } else if (accounts.size() == 1) {
                    PersonAccount account = accounts.get(0);
                    if (!Strings.isEmpty(account.getStreetType())) {
                        if (account.getStreetType().equals(currentStreetType)) {
                            account.setAccountNumber(newAccountNumber);
                            updateAccountNumber(account, session);
                        } else {
                            insert(newPersonAccount, session);
                        }
                    } else {
                        account.setAccountNumber(newAccountNumber);
                        account.setStreetType(currentStreetType);
                        updateAccountNumberAndStreetType(account, session);
                    }
                } else {
                    // find with current street type
                    List<PersonAccount> withStreetType = Lists.newArrayList();
                    for (PersonAccount account : accounts) {
                        if (Strings.isEqual(account.getStreetType(), currentStreetType)) {
                            withStreetType.add(account);
                        }
                    }

                    if (withStreetType.isEmpty()) {
                        String accountNumber = haveTheSameAccountNumber(accounts);
                        if (accountNumber != null) {
                            for (PersonAccount account : accounts) {
                                account.setAccountNumber(newAccountNumber);
                                updateAccountNumber(account, session);
                            }
                        } else {
                            // Do nothing.
                        }
                    } else if (withStreetType.size() == 1) {
                        PersonAccount account = withStreetType.get(0);
                        account.setAccountNumber(newAccountNumber);
                        updateAccountNumber(account, session);
                    } else {
                        String accountNumber = haveTheSameAccountNumber(withStreetType);
                        if (accountNumber != null) {
                            for (PersonAccount account : accounts) {
                                account.setAccountNumber(newAccountNumber);
                                updateAccountNumber(account, session);
                            }
                        } else {
                            // Do nothing.
                        }
                    }
                }
            }
        });
    }

    private PersonAccount newPersonAccount(DwellingCharacteristics dwellingCharacteristics, String accountNumber, long calculationCenterId,
            long userOrganizationId) {
        PersonAccount personAccount = new PersonAccount();
        personAccount.setFirstName(dwellingCharacteristics.getFirstName());
        personAccount.setMiddleName(dwellingCharacteristics.getMiddleName());
        personAccount.setLastName(dwellingCharacteristics.getLastName());
        personAccount.setCity(dwellingCharacteristics.getCity());
        personAccount.setStreet(dwellingCharacteristics.getStreet());
        personAccount.setBuildingNumber(dwellingCharacteristics.getStringField(DwellingCharacteristicsDBF.HOUSE));
        personAccount.setBuildingCorp(dwellingCharacteristics.getStringField(DwellingCharacteristicsDBF.BUILD));
        personAccount.setApartment(dwellingCharacteristics.getStringField(DwellingCharacteristicsDBF.APT));
        personAccount.setStreetType(dwellingCharacteristics.getStreetType());
        personAccount.setOsznId(dwellingCharacteristics.getOrganizationId());
        personAccount.setCalculationCenterId(calculationCenterId);
        personAccount.setPuAccountNumber(dwellingCharacteristics.getStringField(DwellingCharacteristicsDBF.IDCODE));
        personAccount.setAccountNumber(accountNumber);
        personAccount.setUserOrganizationId(userOrganizationId);
        return personAccount;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void saveOrUpdate(final DwellingCharacteristics dwellingCharacteristics, final long calculationCenterId, final long userOrganizationId) {
        handleTransaction(new TransactionCallback() {

            @Override
            public void doInTransaction(SqlSession session) {
                String newAccountNumber = dwellingCharacteristics.getAccountNumber();
                PersonAccount newPersonAccount = newPersonAccount(dwellingCharacteristics, newAccountNumber, calculationCenterId, userOrganizationId);
                List<PersonAccount> accounts = findAccountsLikeName(dwellingCharacteristics.getFirstName(),
                        dwellingCharacteristics.getMiddleName(), dwellingCharacteristics.getLastName(),
                        dwellingCharacteristics.getCity(), dwellingCharacteristics.getStreet(),
                        dwellingCharacteristics.getStringField(DwellingCharacteristicsDBF.HOUSE),
                        dwellingCharacteristics.getStringField(DwellingCharacteristicsDBF.BUILD),
                        dwellingCharacteristics.getStringField(DwellingCharacteristicsDBF.APT),
                        dwellingCharacteristics.getOrganizationId(), calculationCenterId,
                        dwellingCharacteristics.getStringField(DwellingCharacteristicsDBF.IDCODE), userOrganizationId, false, session);
                String currentStreetType = dwellingCharacteristics.getStreetType();
                if (currentStreetType != null) {
                    currentStreetType = currentStreetType.toUpperCase();
                }

                if (accounts.isEmpty()) {
                    insert(newPersonAccount, session);
                } else if (accounts.size() == 1) {
                    PersonAccount account = accounts.get(0);
                    if (!Strings.isEmpty(account.getStreetType())) {
                        if (account.getStreetType().equals(currentStreetType)) {
                            account.setAccountNumber(newAccountNumber);
                            updateAccountNumber(account, session);
                        } else {
                            insert(newPersonAccount, session);
                        }
                    } else {
                        account.setAccountNumber(newAccountNumber);
                        account.setStreetType(currentStreetType);
                        updateAccountNumberAndStreetType(account, session);
                    }
                } else {
                    // find with current street type
                    List<PersonAccount> withStreetType = Lists.newArrayList();
                    for (PersonAccount account : accounts) {
                        if (Strings.isEqual(account.getStreetType(), currentStreetType)) {
                            withStreetType.add(account);
                        }
                    }

                    if (withStreetType.isEmpty()) {
                        String accountNumber = haveTheSameAccountNumber(accounts);
                        if (accountNumber != null) {
                            for (PersonAccount account : accounts) {
                                account.setAccountNumber(newAccountNumber);
                                updateAccountNumber(account, session);
                            }
                        } else {
                            // Do nothing.
                        }
                    } else if (withStreetType.size() == 1) {
                        PersonAccount account = withStreetType.get(0);
                        account.setAccountNumber(newAccountNumber);
                        updateAccountNumber(account, session);
                    } else {
                        String accountNumber = haveTheSameAccountNumber(withStreetType);
                        if (accountNumber != null) {
                            for (PersonAccount account : accounts) {
                                account.setAccountNumber(newAccountNumber);
                                updateAccountNumber(account, session);
                            }
                        } else {
                            // Do nothing.
                        }
                    }
                }
            }
        });
    }

    private PersonAccount newPersonAccount(FacilityServiceType facilityServiceType, String accountNumber, long calculationCenterId,
            long userOrganizationId) {
        PersonAccount personAccount = new PersonAccount();
        personAccount.setFirstName(facilityServiceType.getFirstName());
        personAccount.setMiddleName(facilityServiceType.getMiddleName());
        personAccount.setLastName(facilityServiceType.getLastName());
        personAccount.setCity(facilityServiceType.getCity());
        personAccount.setStreet(facilityServiceType.getStreet());
        personAccount.setBuildingNumber(facilityServiceType.getStringField(FacilityServiceTypeDBF.HOUSE));
        personAccount.setBuildingCorp(facilityServiceType.getStringField(FacilityServiceTypeDBF.BUILD));
        personAccount.setApartment(facilityServiceType.getStringField(FacilityServiceTypeDBF.APT));
        personAccount.setStreetType(facilityServiceType.getStreetType());
        personAccount.setOsznId(facilityServiceType.getOrganizationId());
        personAccount.setCalculationCenterId(calculationCenterId);
        personAccount.setPuAccountNumber(facilityServiceType.getStringField(FacilityServiceTypeDBF.IDCODE));
        personAccount.setAccountNumber(accountNumber);
        personAccount.setUserOrganizationId(userOrganizationId);
        return personAccount;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void saveOrUpdate(final FacilityServiceType facilityServiceType, final long calculationCenterId, final long userOrganizationId) {
        handleTransaction(new TransactionCallback() {

            @Override
            public void doInTransaction(SqlSession session) {
                String newAccountNumber = facilityServiceType.getAccountNumber();
                PersonAccount newPersonAccount = newPersonAccount(facilityServiceType, newAccountNumber, calculationCenterId, userOrganizationId);
                List<PersonAccount> accounts = findAccountsLikeName(facilityServiceType.getFirstName(),
                        facilityServiceType.getMiddleName(), facilityServiceType.getLastName(),
                        facilityServiceType.getCity(), facilityServiceType.getStreet(),
                        facilityServiceType.getStringField(FacilityServiceTypeDBF.HOUSE),
                        facilityServiceType.getStringField(FacilityServiceTypeDBF.BUILD),
                        facilityServiceType.getStringField(FacilityServiceTypeDBF.APT),
                        facilityServiceType.getOrganizationId(), calculationCenterId,
                        facilityServiceType.getStringField(FacilityServiceTypeDBF.IDCODE), userOrganizationId, false, session);
                String currentStreetType = facilityServiceType.getStreetType();
                if (currentStreetType != null) {
                    currentStreetType = currentStreetType.toUpperCase();
                }

                if (accounts.isEmpty()) {
                    insert(newPersonAccount, session);
                } else if (accounts.size() == 1) {
                    PersonAccount account = accounts.get(0);
                    if (!Strings.isEmpty(account.getStreetType())) {
                        if (account.getStreetType().equals(currentStreetType)) {
                            account.setAccountNumber(newAccountNumber);
                            updateAccountNumber(account, session);
                        } else {
                            insert(newPersonAccount, session);
                        }
                    } else {
                        account.setAccountNumber(newAccountNumber);
                        account.setStreetType(currentStreetType);
                        updateAccountNumberAndStreetType(account, session);
                    }
                } else {
                    // find with current street type
                    List<PersonAccount> withStreetType = Lists.newArrayList();
                    for (PersonAccount account : accounts) {
                        if (Strings.isEqual(account.getStreetType(), currentStreetType)) {
                            withStreetType.add(account);
                        }
                    }

                    if (withStreetType.isEmpty()) {
                        String accountNumber = haveTheSameAccountNumber(accounts);
                        if (accountNumber != null) {
                            for (PersonAccount account : accounts) {
                                account.setAccountNumber(newAccountNumber);
                                updateAccountNumber(account, session);
                            }
                        } else {
                            // Do nothing.
                        }
                    } else if (withStreetType.size() == 1) {
                        PersonAccount account = withStreetType.get(0);
                        account.setAccountNumber(newAccountNumber);
                        updateAccountNumber(account, session);
                    } else {
                        String accountNumber = haveTheSameAccountNumber(withStreetType);
                        if (accountNumber != null) {
                            for (PersonAccount account : accounts) {
                                account.setAccountNumber(newAccountNumber);
                                updateAccountNumber(account, session);
                            }
                        } else {
                            // Do nothing.
                        }
                    }
                }
            }
        });
    }

    @Transactional
    public int count(PersonAccountExample example) {
        osznSessionBean.prepareExampleForPermissionCheck(example);
        return (Integer) sqlSession().selectOne(MAPPING_NAMESPACE + ".count", example);
    }

    @Transactional
    public List<PersonAccount> find(PersonAccountExample example) {
        osznSessionBean.prepareExampleForPermissionCheck(example);
        return sqlSession().selectList(MAPPING_NAMESPACE + ".find", example);
    }

    private void insert(PersonAccount personAccount, SqlSession session) {
        checkBuildingCorp(personAccount);
        checkStreetType(personAccount);
        session.insert(MAPPING_NAMESPACE + ".insert", personAccount);
    }

    private void checkStreetType(PersonAccount account) {
        if (account.getStreetType() == null) {
            account.setStreetType("");
        }
    }

    private void checkBuildingCorp(PersonAccount account) {
        if (account.getBuildingCorp() == null) {
            account.setBuildingCorp("");
        }
    }

    /**
     * Web interface update operation.
     * @param personAccount
     */
    @Transactional
    public void update(PersonAccount personAccount) {
        updateAccountNumber(personAccount, sqlSession());
    }

    private void updateAccountNumber(PersonAccount account, SqlSession session) {
        session.update(MAPPING_NAMESPACE + ".updateAccountNumber", account);
    }

    private void updateAccountNumberAndStreetType(PersonAccount account, SqlSession session) {
        checkStreetType(account);
        session.update(MAPPING_NAMESPACE + ".updateAccountNumberAndStreetType", account);
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
