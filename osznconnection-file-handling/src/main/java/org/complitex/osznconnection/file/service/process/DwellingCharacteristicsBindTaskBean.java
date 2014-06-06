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
import org.complitex.osznconnection.file.service.DwellingCharacteristicsBean;
import org.complitex.osznconnection.file.service.PersonAccountService;
import org.complitex.osznconnection.file.service.RequestFileBean;
import org.complitex.osznconnection.file.service.exception.AlreadyProcessingException;
import org.complitex.osznconnection.file.service.exception.BindException;
import org.complitex.osznconnection.file.service.exception.CanceledByUserException;
import org.complitex.osznconnection.file.service.exception.MoreOneAccountException;
import org.complitex.osznconnection.file.service_provider.CalculationCenterBean;
import org.complitex.osznconnection.file.service_provider.ServiceProviderAdapter;
import org.complitex.osznconnection.file.service_provider.exception.DBException;
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

import static org.complitex.osznconnection.file.entity.DwellingCharacteristicsDBF.IDCODE;
import static org.complitex.osznconnection.file.entity.RequestStatus.ACCOUNT_NUMBER_RESOLVED;
import static org.complitex.osznconnection.file.entity.RequestStatus.MORE_ONE_ACCOUNTS_LOCALLY;

/**
 *
 * @author Artem
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class DwellingCharacteristicsBindTaskBean implements ITaskBean {
    private final Logger log = LoggerFactory.getLogger(DwellingCharacteristicsBindTaskBean.class);

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
    private DwellingCharacteristicsBean dwellingCharacteristicsBean;

    @EJB
    private RequestFileBean requestFileBean;

    @EJB
    private ServiceProviderAdapter serviceProviderAdapter;


    private boolean resolveAddress(DwellingCharacteristics dwellingCharacteristics, CalculationContext calculationContext) {
        addressService.resolveAddress(dwellingCharacteristics, calculationContext);

        return dwellingCharacteristics.getStatus().isAddressResolved();
    }

    private void resolveLocalAccount(DwellingCharacteristics dwellingCharacteristics, CalculationContext calculationContext) {
        try {
            String accountNumber = personAccountService.getAccountNumber(dwellingCharacteristics,
                    dwellingCharacteristics.getStringField(IDCODE),
                    calculationContext.getCalculationCenterId());

            if (!Strings.isEmpty(accountNumber)) {
                dwellingCharacteristics.setAccountNumber(accountNumber);
                dwellingCharacteristics.setStatus(RequestStatus.ACCOUNT_NUMBER_RESOLVED);
            }
        } catch (MoreOneAccountException e) {
            dwellingCharacteristics.setStatus(RequestStatus.MORE_ONE_ACCOUNTS_LOCALLY);
        }
    }

    private boolean resolveRemoteAccountNumber(DwellingCharacteristics dwellingCharacteristics,
            CalculationContext calculationContext) throws DBException {
        serviceProviderAdapter.acquireFacilityPersonAccount(calculationContext, dwellingCharacteristics,
                dwellingCharacteristics.getOutgoingDistrict(), dwellingCharacteristics.getOutgoingStreetType(),
                dwellingCharacteristics.getOutgoingStreet(),
                dwellingCharacteristics.getOutgoingBuildingNumber(), dwellingCharacteristics.getOutgoingBuildingCorp(),
                dwellingCharacteristics.getOutgoingApartment(), dwellingCharacteristics.getDate(),
                dwellingCharacteristics.getStringField(DwellingCharacteristicsDBF.IDPIL),
                dwellingCharacteristics.getStringField(DwellingCharacteristicsDBF.PASPPIL));

        if (dwellingCharacteristics.getStatus() == RequestStatus.ACCOUNT_NUMBER_RESOLVED) {
            try {
                personAccountService.save(dwellingCharacteristics, dwellingCharacteristics.getStringField(IDCODE),
                        calculationContext.getCalculationCenterId());
            } catch (MoreOneAccountException e) {
                throw new DBException(e);
            }
        }

        return dwellingCharacteristics.getStatus() == ACCOUNT_NUMBER_RESOLVED;
    }

    private void bind(DwellingCharacteristics dwellingCharacteristics, CalculationContext calculationContext)
            throws DBException {
        //resolve address
        resolveAddress(dwellingCharacteristics, calculationContext);

        if (dwellingCharacteristics.getStatus().isAddressResolved()){
            //resolve local account.
            resolveLocalAccount(dwellingCharacteristics, calculationContext);

            if (dwellingCharacteristics.getStatus().isNotIn(ACCOUNT_NUMBER_RESOLVED, MORE_ONE_ACCOUNTS_LOCALLY)) {
                resolveRemoteAccountNumber(dwellingCharacteristics, calculationContext);
            }
        }

        // обновляем dwelling characteristics запись
        dwellingCharacteristicsBean.update(dwellingCharacteristics);
    }

    private void bindDwellingCharacteristicsFile(RequestFile dwellingCharacteristicsFile, CalculationContext calculationContext)
            throws BindException, DBException, CanceledByUserException {
        //извлечь из базы все id подлежащие связыванию для файла dwelling characteristics и доставать записи порциями по BATCH_SIZE штук.
        List<Long> notResolvedDwellingCharacteristicsIds = dwellingCharacteristicsBean.findIdsForBinding(dwellingCharacteristicsFile.getId());
        List<Long> batch = Lists.newArrayList();

        int batchSize = configBean.getInteger(FileHandlingConfig.BIND_BATCH_SIZE, true);

        while (notResolvedDwellingCharacteristicsIds.size() > 0) {
            batch.clear();
            int toRemoveCount = Math.min(batchSize, notResolvedDwellingCharacteristicsIds.size());
            for (int i = 0; i < toRemoveCount; i++) {
                batch.add(notResolvedDwellingCharacteristicsIds.remove(0));
            }

            //достать из базы очередную порцию записей
            List<DwellingCharacteristics> dwellingCharacteristics =
                    dwellingCharacteristicsBean.findForOperation(dwellingCharacteristicsFile.getId(), batch);
            for (DwellingCharacteristics dwellingCharacteristic : dwellingCharacteristics) {
                if (dwellingCharacteristicsFile.isCanceled()) {
                    throw new CanceledByUserException();
                }

                //связать dwelling characteristics запись
                try {
                    userTransaction.begin();

                    bind(dwellingCharacteristic, calculationContext);

                    userTransaction.commit();
                } catch (Exception e) {
                    log.error("The dwelling characteristics item ( id = " + dwellingCharacteristic.getId() + ") was bound with error: ", e);

                    try {
                        userTransaction.rollback();
                    } catch (SystemException e1) {
                        log.error("Couldn't rollback transaction for binding dwelling characteristics item.", e1);
                    }
                }
            }
        }
    }

    @Override
    public boolean execute(IExecutorObject executorObject, Map commandParameters) throws ExecuteException {
        // ищем в параметрах комманды опцию "Переписывать номер л/с ПУ номером л/с МН"
//        final Boolean updatePuAccount = commandParameters.containsKey(GlobalOptions.UPDATE_PU_ACCOUNT)
//                ? (Boolean) commandParameters.get(GlobalOptions.UPDATE_PU_ACCOUNT) : false;

        RequestFile requestFile = (RequestFile) executorObject;

        requestFile.setStatus(requestFileBean.getRequestFileStatus(requestFile)); //обновляем статус из базы данных

        if (requestFile.isProcessing()) { //проверяем что не обрабатывается в данный момент
            throw new BindException(new AlreadyProcessingException(requestFile), true, requestFile);
        }

        requestFile.setStatus(RequestFileStatus.BINDING);
        requestFileBean.save(requestFile);

        //получаем информацию о текущем контексте вычислений 
        final CalculationContext calculationContext = calculationCenterBean.getContextWithAnyCalculationCenter(requestFile.getUserOrganizationId());

        dwellingCharacteristicsBean.clearBeforeBinding(requestFile.getId(), calculationContext.getServiceProviderTypeIds());

        //связывание файла dwelling characteristics
        try {
            bindDwellingCharacteristicsFile(requestFile, calculationContext);
        } catch (DBException e) {
            throw new RuntimeException(e);
        } catch (CanceledByUserException e) {
            throw new BindException(e, true, requestFile);
        }

        //проверить все ли записи в dwelling characteristics файле связались
        if (!dwellingCharacteristicsBean.isDwellingCharacteristicsFileBound(requestFile.getId())) {
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
        return DwellingCharacteristicsBindTaskBean.class;
    }

    @Override
    public EVENT getEvent() {
        return Log.EVENT.EDIT;
    }
}
