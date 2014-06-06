/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.file.service.process;

import com.google.common.collect.Lists;
import org.apache.wicket.util.string.Strings;
import org.complitex.dictionary.entity.IExecutorObject;
import org.complitex.dictionary.entity.Log;
import org.complitex.dictionary.entity.Log.EVENT;
import org.complitex.dictionary.service.ConfigBean;
import org.complitex.dictionary.service.executor.ExecuteException;
import org.complitex.dictionary.service.executor.ITaskBean;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.service.AddressService;
import org.complitex.osznconnection.file.service.FacilityServiceTypeBean;
import org.complitex.osznconnection.file.service.PersonAccountService;
import org.complitex.osznconnection.file.service.RequestFileBean;
import org.complitex.osznconnection.file.service.exception.AlreadyProcessingException;
import org.complitex.osznconnection.file.service.exception.BindException;
import org.complitex.osznconnection.file.service.exception.CanceledByUserException;
import org.complitex.osznconnection.file.service.exception.MoreOneAccountException;
import org.complitex.osznconnection.file.service_provider.CalculationCenterBean;
import org.complitex.osznconnection.file.service_provider.ServiceProviderAdapter;
import org.complitex.osznconnection.file.service_provider.exception.DBException;
import org.complitex.osznconnection.file.web.pages.util.GlobalOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.util.List;
import java.util.Map;

import static org.complitex.osznconnection.file.entity.FacilityServiceTypeDBF.IDCODE;
import static org.complitex.osznconnection.file.entity.RequestStatus.ACCOUNT_NUMBER_RESOLVED;
import static org.complitex.osznconnection.file.entity.RequestStatus.MORE_ONE_ACCOUNTS_LOCALLY;

/**
 *
 * @author Artem
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class FacilityServiceTypeBindTaskBean implements ITaskBean {
    private final Logger log = LoggerFactory.getLogger(FacilityServiceTypeBindTaskBean.class);

    @Resource
    private UserTransaction userTransaction;

    @EJB
    protected ConfigBean configBean;

    @EJB
    private AddressService addressService;

    @EJB
    private PersonAccountService personAccountService;

    @EJB
    private CalculationCenterBean calculationCenterBean;

    @EJB
    private FacilityServiceTypeBean facilityServiceTypeBean;

    @EJB
    private RequestFileBean requestFileBean;

    @EJB
    private ServiceProviderAdapter serviceProviderAdapter;

    private boolean resolveAddress(FacilityServiceType facilityServiceType, CalculationContext calculationContext) {
        addressService.resolveAddress(facilityServiceType, calculationContext);

        return facilityServiceType.getStatus().isAddressResolved();
    }

    private void resolveLocalAccount(FacilityServiceType facilityServiceType, CalculationContext calculationContext) {
        try {
            String accountNumber = personAccountService.getAccountNumber(facilityServiceType,
                    facilityServiceType.getStringField(IDCODE),
                    calculationContext.getCalculationCenterId());

            if (!Strings.isEmpty(accountNumber)) {
                facilityServiceType.setAccountNumber(accountNumber);
                facilityServiceType.setStatus(ACCOUNT_NUMBER_RESOLVED);
            }
        } catch (MoreOneAccountException e) {
            facilityServiceType.setStatus(MORE_ONE_ACCOUNTS_LOCALLY);
        }
    }

    private boolean resolveRemoteAccountNumber(FacilityServiceType facilityServiceType,
            CalculationContext calculationContext, Boolean updatePuAccount) throws DBException {
        serviceProviderAdapter.acquireFacilityPersonAccount(calculationContext, facilityServiceType,
                facilityServiceType.getOutgoingDistrict(), facilityServiceType.getOutgoingStreetType(),
                facilityServiceType.getOutgoingStreet(),
                facilityServiceType.getOutgoingBuildingNumber(), facilityServiceType.getOutgoingBuildingCorp(),
                facilityServiceType.getOutgoingApartment(), facilityServiceType.getDate(),
                facilityServiceType.getStringField(FacilityServiceTypeDBF.IDPIL),
                facilityServiceType.getStringField(FacilityServiceTypeDBF.PASPPIL));


        if (facilityServiceType.getStatus() == ACCOUNT_NUMBER_RESOLVED) {
            try {
                personAccountService.save(facilityServiceType, facilityServiceType.getStringField(IDCODE),
                        calculationContext.getCalculationCenterId());
            } catch (MoreOneAccountException e) {
                throw new DBException(e);
            }
        }

        return facilityServiceType.getStatus() == ACCOUNT_NUMBER_RESOLVED;
    }

    private void bind(FacilityServiceType facilityServiceType, CalculationContext calculationContext, Boolean updatePuAccount)
            throws DBException {
        //resolve address
        resolveAddress(facilityServiceType, calculationContext);

        if (facilityServiceType.getStatus().isAddressResolved()){
            //resolve local account.
            resolveLocalAccount(facilityServiceType, calculationContext);

            if (facilityServiceType.getStatus().isNotIn(ACCOUNT_NUMBER_RESOLVED, MORE_ONE_ACCOUNTS_LOCALLY)) {
                resolveRemoteAccountNumber(facilityServiceType, calculationContext, updatePuAccount);
            }
        }

        // обновляем facility service type запись
        facilityServiceTypeBean.update(facilityServiceType);
    }

    private void bindFacilityServiceTypeFile(RequestFile facilityServiceTypeFile, CalculationContext calculationContext,
            Boolean updatePuAccount) throws BindException, DBException, CanceledByUserException {
        //извлечь из базы все id подлежащие связыванию для файла facility service type и доставать записи порциями по BATCH_SIZE штук.
        List<Long> notResolvedFacilityServiceTypeIds = facilityServiceTypeBean.findIdsForBinding(facilityServiceTypeFile.getId());
        List<Long> batch = Lists.newArrayList();

        int batchSize = configBean.getInteger(FileHandlingConfig.BIND_BATCH_SIZE, true);

        while (notResolvedFacilityServiceTypeIds.size() > 0) {
            batch.clear();
            int toRemoveCount = Math.min(batchSize, notResolvedFacilityServiceTypeIds.size());
            for (int i = 0; i < toRemoveCount; i++) {
                batch.add(notResolvedFacilityServiceTypeIds.remove(0));
            }

            //достать из базы очередную порцию записей
            List<FacilityServiceType> facilityServiceTypes =
                    facilityServiceTypeBean.findForOperation(facilityServiceTypeFile.getId(), batch);
            for (FacilityServiceType facilityServiceType : facilityServiceTypes) {
                if (facilityServiceTypeFile.isCanceled()) {
                    throw new CanceledByUserException();
                }

                //связать dwelling characteristics запись
                try {
                    userTransaction.begin();
                    bind(facilityServiceType, calculationContext, updatePuAccount);
                    userTransaction.commit();
                } catch (Exception e) {
                    log.error("The facility service type item ( id = " + facilityServiceType.getId() + ") was bound with error: ", e);

                    try {
                        userTransaction.rollback();
                    } catch (SystemException e1) {
                        log.error("Couldn't rollback transaction for binding facility service type item.", e1);
                    }
                }
            }
        }
    }

    @Override
    public boolean execute(IExecutorObject executorObject, Map commandParameters) throws ExecuteException {
        // ищем в параметрах команды опцию "Переписывать номер л/с ПУ номером л/с МН"
        final Boolean updatePuAccount = commandParameters.containsKey(GlobalOptions.UPDATE_PU_ACCOUNT)
                ? (Boolean) commandParameters.get(GlobalOptions.UPDATE_PU_ACCOUNT) : false;

        RequestFile requestFile = (RequestFile) executorObject;

        requestFile.setStatus(requestFileBean.getRequestFileStatus(requestFile)); //обновляем статус из базы данных

        if (requestFile.isProcessing()) { //проверяем что не обрабатывается в данный момент
            throw new BindException(new AlreadyProcessingException(requestFile), true, requestFile);
        }

        requestFile.setStatus(RequestFileStatus.BINDING);
        requestFileBean.save(requestFile);

        //получаем информацию о текущем контексте вычислений 
        final CalculationContext calculationContext = calculationCenterBean.getContextWithAnyCalculationCenter(requestFile.getUserOrganizationId());

        facilityServiceTypeBean.clearBeforeBinding(requestFile.getId(), calculationContext.getServiceProviderTypeIds());

        //связывание файла facility service type
        try {
            bindFacilityServiceTypeFile(requestFile, calculationContext, updatePuAccount);
        } catch (DBException e) {
            throw new RuntimeException(e);
        } catch (CanceledByUserException e) {
            throw new BindException(e, true, requestFile);
        }

        //проверить все ли записи в facility service type файле связались
        if (!facilityServiceTypeBean.isFacilityServiceTypeFileBound(requestFile.getId())) {
            throw new BindException(true, requestFile);
        }

        requestFile.setStatus(RequestFileStatus.BOUND);
        requestFileBean.save(requestFile);

        return true;
    }

    @Override
    public void onError(IExecutorObject executorObject) {
        RequestFile requestFile = (RequestFile) executorObject;
        requestFile.setStatus(RequestFileStatus.BIND_ERROR);

        requestFileBean.save(requestFile);
    }

    @Override
    public String getModuleName() {
        return Module.NAME;
    }

    @Override
    public Class<?> getControllerClass() {
        return FacilityServiceTypeBindTaskBean.class;
    }

    @Override
    public EVENT getEvent() {
        return Log.EVENT.EDIT;
    }
}
