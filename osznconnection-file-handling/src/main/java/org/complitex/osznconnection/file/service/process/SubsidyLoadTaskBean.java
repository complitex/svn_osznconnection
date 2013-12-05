package org.complitex.osznconnection.file.service.process;

import org.complitex.dictionary.entity.IExecutorObject;
import org.complitex.dictionary.entity.Log;
import org.complitex.dictionary.service.executor.ExecuteException;
import org.complitex.dictionary.service.executor.ITaskBean;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.service.RequestFileBean;
import org.complitex.osznconnection.file.service.SubsidyBean;
import org.complitex.osznconnection.file.service.util.SubsidyNameParser;
import org.complitex.osznconnection.organization.strategy.OsznOrganizationStrategy;
import org.complitex.osznconnection.organization.strategy.entity.OsznOrganization;
import org.complitex.osznconnection.organization.strategy.entity.ServiceAssociation;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class SubsidyLoadTaskBean implements ITaskBean {

    @EJB
    private RequestFileBean requestFileBean;

    @EJB
    private LoadRequestFileBean loadRequestFileBean;

    @EJB
    private SubsidyBean subsidyBean;

    @EJB
    private OsznOrganizationStrategy organizationStrategy;

    @Override
    public boolean execute(IExecutorObject executorObject, Map commandParameters) throws ExecuteException {
        final RequestFile requestFile = (RequestFile) executorObject;

        requestFile.setStatus(RequestFileStatus.LOADING);

        boolean noSkip = loadRequestFileBean.load(requestFile, new LoadRequestFileBean.AbstractLoadRequestFile() {

            @Override
            public Enum[] getFieldNames() {
                return SubsidyDBF.values();
            }

            @Override
            public AbstractRequest newObject() {
                return new Subsidy();
            }

            @Override
            public void save(List<AbstractRequest> batch) {
                //check sum
                for (AbstractRequest request : batch) {
                    OsznOrganization organization = organizationStrategy.findById(request.getUserOrganizationId(), true);

                    BigDecimal nSum = new BigDecimal(0);
                    BigDecimal sbSum = new BigDecimal(0);
                    BigDecimal smSum = new BigDecimal(0);

                    /*
                       Если поле NUMM != 0, то нужно выполнять проверку SUMMA == SUBS * NUMM и SUMMA должен быть
                       вточности равен сумме полей SBN, где N - коды услуг, разрешенных для организации пользователей.
                       Если поле NUMM == 0, то нужно выполнять проверку SUMMA должен быть вточности равен сумме полей SMN,
                       где N - коды услуг, разрешенных для организации пользователей.
                     */

                    for (ServiceAssociation sa : organization.getServiceAssociationList()) {
                        nSum = nSum.add((BigDecimal) request.getField("P" + sa.getServiceProviderTypeId()));
                        sbSum = sbSum.add((BigDecimal) request.getField("SB" + sa.getServiceProviderTypeId()));
                        smSum = smSum.add((BigDecimal) request.getField("SM" + sa.getServiceProviderTypeId()));
                    }

                    Long numm = (Long)request.getField("NUMM");
                    BigDecimal summa = (BigDecimal) request.getField("SUMMA");
                    BigDecimal subs = (BigDecimal) request.getField("SUBS");

                    if (!request.getField("NM_PAY").equals(nSum.setScale(2))
                            || (numm != 0 && !summa.equals(subs.multiply(new BigDecimal(numm)))
                                && !summa.equals(sbSum.setScale(2)))
                            || (numm == 0 && !summa.equals(smSum.setScale(2)))) {
                        request.setStatus(RequestStatus.SUBSIDY_NM_PAY_ERROR);
                        requestFile.setStatus(RequestFileStatus.LOAD_ERROR);
                    }
                }

                subsidyBean.insert(batch);
            }

            @Override
            public void postProcess(int rowNumber, AbstractRequest request) {
                final Subsidy subsidy = (Subsidy) request;
                parseFio(subsidy);
            }
        });

        if (!noSkip) {
            requestFile.setStatus(RequestFileStatus.SKIPPED);

            return false; //skip - file already loaded
        }

        if (!requestFile.getStatus().equals(RequestFileStatus.LOAD_ERROR)) {
            requestFile.setStatus(RequestFileStatus.LOADED);
        }

        requestFileBean.save(requestFile);

        return true;
    }

    private void parseFio(Subsidy subsidy) {
        final String rash = subsidy.getStringField(SubsidyDBF.RASH);
        final String fio = subsidy.getStringField(SubsidyDBF.FIO);
        PersonName personName = SubsidyNameParser.parse(rash, fio);
        subsidy.setFirstName(personName.getFirstName());
        subsidy.setMiddleName(personName.getMiddleName());
        subsidy.setLastName(personName.getLastName());
    }

    @Override
    public void onError(IExecutorObject executorObject) {
        RequestFile requestFile = (RequestFile) executorObject;
        requestFileBean.delete(requestFile);
    }

    @Override
    public String getModuleName() {
        return Module.NAME;
    }

    @Override
    public Class<?> getControllerClass() {
        return SubsidyLoadTaskBean.class;
    }

    @Override
    public Log.EVENT getEvent() {
        return Log.EVENT.CREATE;
    }
}
