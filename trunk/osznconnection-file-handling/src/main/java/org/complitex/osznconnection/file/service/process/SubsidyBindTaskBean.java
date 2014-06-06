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
import org.complitex.osznconnection.file.service.*;
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

import static org.complitex.osznconnection.file.entity.RequestStatus.ACCOUNT_NUMBER_RESOLVED;
import static org.complitex.osznconnection.file.entity.RequestStatus.MORE_ONE_ACCOUNTS_LOCALLY;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class SubsidyBindTaskBean implements ITaskBean {

    private final Logger log = LoggerFactory.getLogger(SubsidyBindTaskBean.class);
    @Resource
    private UserTransaction userTransaction;

    @EJB
    protected ConfigBean configBean;

    @EJB(name = "OsznAddressService")
    private AddressService addressService;

    @EJB
    private PersonAccountService personAccountService;

    @EJB
    private CalculationCenterBean calculationCenterBean;

    @EJB
    private SubsidyBean subsidyBean;

    @EJB
    private RequestFileBean requestFileBean;

    @EJB
    private SubsidyService subsidyService;

    @EJB
    private ServiceProviderAdapter serviceProviderAdapter;

    private void bind(Subsidy subsidy, CalculationContext calculationContext, boolean updatePuAccount) throws DBException {
        //resolve address
        addressService.resolveAddress(subsidy, calculationContext);

        //resolve account number
        if (subsidy.getStatus().isAddressResolved()){
            personAccountService.resolveAccountNumber(subsidy, subsidy.getStringField(SubsidyDBF.RASH),
                    subsidyService.getServicingOrganizationCode(subsidy.getRequestFileId()),
                    calculationContext, updatePuAccount);
        }

        // обновляем subsidy запись
        subsidyBean.update(subsidy);
    }

    private void bindSubsidyFile(RequestFile subsidyFile, CalculationContext calculationContext,
            Boolean updatePuAccount) throws BindException, DBException, CanceledByUserException {
        //извлечь из базы все id подлежащие связыванию для файла subsidy и доставать записи порциями по BATCH_SIZE штук.
        List<Long> notResolvedSubsidyIds = subsidyBean.findIdsForBinding(subsidyFile.getId());

        List<Long> batch = Lists.newArrayList();

        int batchSize = configBean.getInteger(FileHandlingConfig.BIND_BATCH_SIZE, true);

        while (notResolvedSubsidyIds.size() > 0) {
            batch.clear();
            int toRemoveCount = Math.min(batchSize, notResolvedSubsidyIds.size());
            for (int i = 0; i < toRemoveCount; i++) {
                batch.add(notResolvedSubsidyIds.remove(0));
            }

            //достать из базы очередную порцию записей
            List<Subsidy> subsidies = subsidyBean.findForOperation(subsidyFile.getId(), batch);
            for (Subsidy subsidy : subsidies) {
                if (subsidyFile.isCanceled()) {
                    throw new CanceledByUserException();
                }

                //связать subsidy запись
                try {
                    userTransaction.begin();
                    bind(subsidy, calculationContext, updatePuAccount);
                    userTransaction.commit();
                } catch (Exception e) {
                    log.error("The subsidy item ( id = " + subsidy.getId() + ") was bound with error: ", e);

                    try {
                        userTransaction.rollback();
                    } catch (SystemException e1) {
                        log.error("Couldn't rollback transaction for binding subsidy item.", e1);
                    }
                }
            }
        }
    }

    @Override
    public boolean execute(IExecutorObject executorObject, Map commandParameters) throws ExecuteException {
        // ищем в параметрах комманды опцию "Переписывать номер л/с ПУ номером л/с МН"
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

        subsidyBean.clearBeforeBinding(requestFile.getId(), calculationContext.getServiceProviderTypeIds());

        //связывание файла subsidy
        try {
            bindSubsidyFile(requestFile, calculationContext, updatePuAccount);
        } catch (DBException e) {
            throw new RuntimeException(e);
        } catch (CanceledByUserException e) {
            throw new BindException(e, true, requestFile);
        }

        //проверить все ли записи в subsidy файле связались
        if (!subsidyBean.isSubsidyFileBound(requestFile.getId())) {
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
        return SubsidyBindTaskBean.class;
    }

    @Override
    public EVENT getEvent() {
        return Log.EVENT.EDIT;
    }
}
