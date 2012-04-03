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
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.mysql.MySqlErrors;
import org.complitex.osznconnection.file.entity.ActualPayment;
import org.complitex.osznconnection.file.entity.ActualPaymentDBF;
import org.complitex.osznconnection.file.entity.Payment;
import org.complitex.osznconnection.file.entity.PaymentDBF;
import org.complitex.osznconnection.file.entity.Subsidy;
import org.complitex.osznconnection.file.entity.SubsidyDBF;
import org.complitex.osznconnection.file.entity.example.PersonAccountExample;
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
    private static final String INCONSISTENT_STATE_ERROR_MESSAGE =
            "More one person have the same full name and address but different account numbers.";
    private static final String INCONSISTENT_STATE_ERROR_MESSAGE2 = "More one person have the same last name, "
            + ", address and the same beginning of first and middle names but different account numbers.";

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

    @SuppressWarnings("unchecked")
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

    @SuppressWarnings("unchecked")
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
    public String findLocalAccountNumber(Payment payment, long calculationCenterId, long userOrganizationId) {
        List<PersonAccount> accounts = findAccounts((String) payment.getField(PaymentDBF.F_NAM),
                (String) payment.getField(PaymentDBF.M_NAM), (String) payment.getField(PaymentDBF.SUR_NAM),
                (String) payment.getField(PaymentDBF.N_NAME), (String) payment.getField(PaymentDBF.VUL_NAME),
                (String) payment.getField(PaymentDBF.BLD_NUM), (String) payment.getField(PaymentDBF.CORP_NUM),
                (String) payment.getField(PaymentDBF.FLAT), payment.getOrganizationId(), calculationCenterId,
                (String) payment.getField(PaymentDBF.OWN_NUM_SR), userOrganizationId, false, sqlSession());
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
                throw new IllegalStateException(INCONSISTENT_STATE_ERROR_MESSAGE);
            }
        }
    }

    @Transactional
    public String findLocalAccountNumber(ActualPayment actualPayment, long calculationCenterId, long userOrganizationId) {
        List<PersonAccount> accounts = findAccounts((String) actualPayment.getField(ActualPaymentDBF.F_NAM),
                (String) actualPayment.getField(ActualPaymentDBF.M_NAM), (String) actualPayment.getField(ActualPaymentDBF.SUR_NAM),
                (String) actualPayment.getField(ActualPaymentDBF.N_NAME), (String) actualPayment.getField(ActualPaymentDBF.VUL_NAME),
                (String) actualPayment.getField(ActualPaymentDBF.BLD_NUM), (String) actualPayment.getField(ActualPaymentDBF.CORP_NUM),
                (String) actualPayment.getField(ActualPaymentDBF.FLAT), actualPayment.getOrganizationId(), calculationCenterId,
                (String) actualPayment.getField(ActualPaymentDBF.OWN_NUM), userOrganizationId, false, sqlSession());
        final String currentStreetType = (String) actualPayment.getField(ActualPaymentDBF.VUL_CAT);
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
            String accountNumber = haveTheSameAccountNumber(accounts);
            if (accountNumber != null) {
                return accountNumber;
            } else {
                throw new IllegalStateException(INCONSISTENT_STATE_ERROR_MESSAGE);
            }
        }
    }

    @Transactional
    public String findLocalAccountNumber(Subsidy subsidy, long calculationCenterId, long userOrganizationId) {
        List<PersonAccount> accounts = findAccountsLikeName(subsidy.getFirstName(),
                subsidy.getMiddleName(), subsidy.getLastName(),
                (String) subsidy.getField(SubsidyDBF.NP_NAME), (String) subsidy.getField(SubsidyDBF.NAME_V),
                (String) subsidy.getField(SubsidyDBF.BLD), (String) subsidy.getField(SubsidyDBF.CORP),
                (String) subsidy.getField(SubsidyDBF.FLAT), subsidy.getOrganizationId(), calculationCenterId,
                (String) subsidy.getField(SubsidyDBF.RASH), userOrganizationId, false, sqlSession());
        final String currentStreetType = (String) subsidy.getField(SubsidyDBF.CAT_V);
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
            String accountNumber = haveTheSameAccountNumber(accounts);
            if (accountNumber != null) {
                return accountNumber;
            } else {
                throw new IllegalStateException(INCONSISTENT_STATE_ERROR_MESSAGE2);
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

            if (sqlException != null && MySqlErrors.isDublicateError(sqlException)) {
                //the same person account entry has already been inserted in parallel.
                //TODO: doing nothing. Maybe some action should be taken.
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
        personAccount.setFirstName((String) payment.getField(PaymentDBF.F_NAM));
        personAccount.setMiddleName((String) payment.getField(PaymentDBF.M_NAM));
        personAccount.setLastName((String) payment.getField(PaymentDBF.SUR_NAM));
        personAccount.setCity((String) payment.getField(PaymentDBF.N_NAME));
        personAccount.setStreet((String) payment.getField(PaymentDBF.VUL_NAME));
        personAccount.setBuildingNumber((String) payment.getField(PaymentDBF.BLD_NUM));
        personAccount.setBuildingCorp((String) payment.getField(PaymentDBF.CORP_NUM));
        personAccount.setApartment((String) payment.getField(PaymentDBF.FLAT));
        personAccount.setOsznId(payment.getOrganizationId());
        personAccount.setCalculationCenterId(calculationCenterId);
        personAccount.setPuAccountNumber((String) payment.getField(PaymentDBF.OWN_NUM_SR));
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
                List<PersonAccount> accounts = findAccounts((String) payment.getField(PaymentDBF.F_NAM),
                        (String) payment.getField(PaymentDBF.M_NAM), (String) payment.getField(PaymentDBF.SUR_NAM),
                        (String) payment.getField(PaymentDBF.N_NAME), (String) payment.getField(PaymentDBF.VUL_NAME),
                        (String) payment.getField(PaymentDBF.BLD_NUM), (String) payment.getField(PaymentDBF.CORP_NUM),
                        (String) payment.getField(PaymentDBF.FLAT), payment.getOrganizationId(), calculationCenterId,
                        (String) payment.getField(PaymentDBF.OWN_NUM_SR), userOrganizationId, false, sqlSession());
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
                        throw new IllegalStateException(INCONSISTENT_STATE_ERROR_MESSAGE);
                    }
                }
            }
        });
    }

    private PersonAccount newPersonAccount(ActualPayment actualPayment, String accountNumber, long calculationCenterId,
            long userOrganizationId) {
        PersonAccount personAccount = new PersonAccount();
        personAccount.setFirstName((String) actualPayment.getField(ActualPaymentDBF.F_NAM));
        personAccount.setMiddleName((String) actualPayment.getField(ActualPaymentDBF.M_NAM));
        personAccount.setLastName((String) actualPayment.getField(ActualPaymentDBF.SUR_NAM));
        personAccount.setCity((String) actualPayment.getField(ActualPaymentDBF.N_NAME));
        personAccount.setStreet((String) actualPayment.getField(ActualPaymentDBF.VUL_NAME));
        personAccount.setBuildingNumber((String) actualPayment.getField(ActualPaymentDBF.BLD_NUM));
        personAccount.setBuildingCorp((String) actualPayment.getField(ActualPaymentDBF.CORP_NUM));
        personAccount.setApartment((String) actualPayment.getField(ActualPaymentDBF.FLAT));
        personAccount.setStreetType((String) actualPayment.getField(ActualPaymentDBF.VUL_CAT));
        personAccount.setOsznId(actualPayment.getOrganizationId());
        personAccount.setCalculationCenterId(calculationCenterId);
        personAccount.setPuAccountNumber((String) actualPayment.getField(ActualPaymentDBF.OWN_NUM));
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
                List<PersonAccount> accounts = findAccounts((String) actualPayment.getField(ActualPaymentDBF.F_NAM),
                        (String) actualPayment.getField(ActualPaymentDBF.M_NAM), (String) actualPayment.getField(ActualPaymentDBF.SUR_NAM),
                        (String) actualPayment.getField(ActualPaymentDBF.N_NAME), (String) actualPayment.getField(ActualPaymentDBF.VUL_NAME),
                        (String) actualPayment.getField(ActualPaymentDBF.BLD_NUM), (String) actualPayment.getField(ActualPaymentDBF.CORP_NUM),
                        (String) actualPayment.getField(ActualPaymentDBF.FLAT), actualPayment.getOrganizationId(), calculationCenterId,
                        (String) actualPayment.getField(ActualPaymentDBF.OWN_NUM), userOrganizationId, false, sqlSession());
                final String currentStreetType = (String) actualPayment.getField(ActualPaymentDBF.VUL_CAT);
                if (accounts.isEmpty()) {
                    insert(newPersonAccount, session);
                } else if (accounts.size() == 1) {
                    PersonAccount account = accounts.get(0);
                    if (!Strings.isEmpty(account.getStreetType())) {
                        if (currentStreetType.equals(account.getStreetType())) {
                            account.setAccountNumber(newAccountNumber);
                            updateAccountNumber(account, session);
                        } else {
                            if (account.getAccountNumber().equals(newAccountNumber)) {
                                insert(newPersonAccount, session);
                            } else {
                                account.setAccountNumber(newAccountNumber);
                                account.setStreetType(currentStreetType);
                                updateAccountNumberAndStreetType(account, session);
                            }
                        }
                    } else {
                        account.setAccountNumber(newAccountNumber);
                        account.setStreetType(currentStreetType);
                        updateAccountNumberAndStreetType(account, session);
                    }
                } else {
                    String accountNumber = haveTheSameAccountNumber(accounts);
                    if (accountNumber != null) {
                        for (PersonAccount account : accounts) {
                            account.setAccountNumber(newAccountNumber);
                            updateAccountNumber(account, session);
                        }
                    } else {
                        throw new IllegalStateException(INCONSISTENT_STATE_ERROR_MESSAGE);
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
        personAccount.setCity((String) subsidy.getField(SubsidyDBF.NP_NAME));
        personAccount.setStreet((String) subsidy.getField(SubsidyDBF.NAME_V));
        personAccount.setBuildingNumber((String) subsidy.getField(SubsidyDBF.BLD));
        personAccount.setBuildingCorp((String) subsidy.getField(SubsidyDBF.CORP));
        personAccount.setApartment((String) subsidy.getField(SubsidyDBF.FLAT));
        personAccount.setStreetType((String) subsidy.getField(SubsidyDBF.CAT_V));
        personAccount.setOsznId(subsidy.getOrganizationId());
        personAccount.setCalculationCenterId(calculationCenterId);
        personAccount.setPuAccountNumber((String) subsidy.getField(SubsidyDBF.RASH));
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
                        (String) subsidy.getField(SubsidyDBF.NP_NAME), (String) subsidy.getField(SubsidyDBF.NAME_V),
                        (String) subsidy.getField(SubsidyDBF.BLD), (String) subsidy.getField(SubsidyDBF.CORP),
                        (String) subsidy.getField(SubsidyDBF.FLAT), subsidy.getOrganizationId(), calculationCenterId,
                        (String) subsidy.getField(SubsidyDBF.RASH), userOrganizationId, false, sqlSession());
                final String currentStreetType = (String) subsidy.getField(SubsidyDBF.CAT_V);
                if (accounts.isEmpty()) {
                    insert(newPersonAccount, session);
                } else if (accounts.size() == 1) {
                    PersonAccount account = accounts.get(0);
                    if (!Strings.isEmpty(account.getStreetType())) {
                        if (currentStreetType.equals(account.getStreetType())) {
                            account.setAccountNumber(newAccountNumber);
                            updateAccountNumber(account, session);
                        } else {
                            if (account.getAccountNumber().equals(newAccountNumber)) {
                                insert(newPersonAccount, session);
                            } else {
                                account.setAccountNumber(newAccountNumber);
                                account.setStreetType(currentStreetType);
                                updateAccountNumberAndStreetType(account, session);
                            }
                        }
                    } else {
                        account.setAccountNumber(newAccountNumber);
                        account.setStreetType(currentStreetType);
                        updateAccountNumberAndStreetType(account, session);
                    }
                } else {
                    String accountNumber = haveTheSameAccountNumber(accounts);
                    if (accountNumber != null) {
                        for (PersonAccount account : accounts) {
                            account.setAccountNumber(newAccountNumber);
                            updateAccountNumber(account, session);
                        }
                    } else {
                        throw new IllegalStateException(INCONSISTENT_STATE_ERROR_MESSAGE2);
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

    @SuppressWarnings({"unchecked"})
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
     * Web interface validation.
     * @param personAccount
     */
    @Transactional
    public boolean validate(PersonAccount personAccount) {
        List<PersonAccount> accounts = findAccounts(personAccount.getFirstName(), personAccount.getMiddleName(),
                personAccount.getLastName(), personAccount.getCity(), personAccount.getStreet(), personAccount.getBuildingNumber(),
                personAccount.getBuildingCorp(), personAccount.getApartment(), personAccount.getOsznId(),
                personAccount.getCalculationCenterId(), personAccount.getPuAccountNumber(), personAccount.getUserOrganizationId(),
                false, sqlSession());

        if (accounts.size() > 1) {
            String accountNumber = haveTheSameAccountNumber(accounts);
            if (accountNumber != null) {
                return accountNumber.equals(personAccount.getAccountNumber()) ? true : false;
            } else {
                throw new IllegalStateException(INCONSISTENT_STATE_ERROR_MESSAGE);
            }
        } else {
            return true;
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
