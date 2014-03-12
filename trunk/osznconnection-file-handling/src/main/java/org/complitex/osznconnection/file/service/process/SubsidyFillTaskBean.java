package org.complitex.osznconnection.file.service.process;

import org.complitex.dictionary.entity.IExecutorObject;
import org.complitex.dictionary.entity.Log;
import org.complitex.dictionary.service.executor.ExecuteException;
import org.complitex.dictionary.service.executor.ITaskBean;
import org.complitex.dictionary.util.DateUtil;
import org.complitex.osznconnection.file.Module;
import org.complitex.osznconnection.file.entity.*;
import org.complitex.osznconnection.file.service.*;
import org.complitex.osznconnection.file.service.exception.AlreadyProcessingException;
import org.complitex.osznconnection.file.service.exception.CanceledByUserException;
import org.complitex.osznconnection.file.service.exception.FillException;
import org.complitex.osznconnection.file.service_provider.CalculationCenterBean;
import org.complitex.osznconnection.file.service_provider.ServiceProviderAdapter;
import org.complitex.osznconnection.file.service_provider.exception.DBException;
import org.complitex.osznconnection.file.service_provider.exception.UnknownAccountNumberTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 15.01.14 18:36
 */
@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class SubsidyFillTaskBean implements ITaskBean{
    private final Logger log = LoggerFactory.getLogger(SubsidyFillTaskBean.class);

    @Resource
    private UserTransaction userTransaction;

    @EJB
    private RequestFileBean requestFileBean;

    @EJB
    private SubsidyBean subsidyBean;

    @EJB
    private SubsidyMasterDataBean subsidyMasterDataBean;

    @EJB(name = "OsznAddressService")
    private AddressService addressService;

    @EJB
    private ServiceProviderAdapter serviceProviderAdapter;

    @EJB
    private CalculationCenterBean calculationCenterBean;

    @EJB
    private SubsidyService subsidyService;

    @Override
    public boolean execute(IExecutorObject object, Map commandParameters) throws ExecuteException {
        RequestFile requestFile = (RequestFile) object;

        requestFile.setStatus(requestFileBean.getRequestFileStatus(requestFile)); //обновляем статус из базы данных

        if (requestFile.isProcessing()) { //проверяем что не обрабатывается в данный момент
            throw new FillException(new AlreadyProcessingException(requestFile), true, requestFile);
        }

        requestFile.setStatus(RequestFileStatus.FILLING);
        requestFileBean.save(requestFile);

        //Обработка
        try {
            List<Subsidy> subsidies = subsidyBean.getSubsidies(requestFile.getId());

            for (Subsidy subsidy : subsidies){
                if (requestFile.isCanceled()){
                    throw new FillException(new CanceledByUserException(), true, requestFile);
                }

                userTransaction.begin();

                fill(subsidy);

                userTransaction.commit();
            }
        } catch (Exception e) {
            log.error("Ошибка обработки файла субсидии", e);

            try {
                userTransaction.rollback();
            } catch (SystemException e1) {
                log.error("", e1);
            }

            throw new RuntimeException(e);
        }

        //проверить все ли записи в файле субсидии обработались
        if (!subsidyBean.isSubsidyFileFilled(requestFile.getId())) {
            throw new FillException(true, requestFile);
        }

        requestFile.setStatus(RequestFileStatus.FILLED);
        requestFileBean.save(requestFile);

        return true;
    }

    /**
     При обработке файла субсидий данные каждой записи (поля NUMB, FIO, FLAT, SBn и OBn) копируются
     во все порожденные записи мастер-данных без изменения. В поле RASH записывается л/с ЖЭКа, получаемый
     при обработке (а не л/с из исходной записи). В поля DOM и REG записываются код дома, код района,
     получаемые при обработке. Если в поле NUMM записи субсидии число больше 1, то для каждого месяца порождается
     своя запись мастер-данных, в поля BEGIN0 и END0 которой записывается первое и последнее число месяца
     к которому относится данная запись мастер-данных.
     */
    private void fill(Subsidy subsidy) throws DBException, UnknownAccountNumberTypeException {
        //получаем информацию о текущем контексте вычислений
        CalculationContext calculationContext = calculationCenterBean.getContextWithAnyCalculationCenter(subsidy.getUserOrganizationId());

        String districtName = addressService.resolveOutgoingDistrict(subsidy.getOrganizationId(), subsidy.getUserOrganizationId());

//  todo TEST
//        List<AccountDetail> accountDetails = serviceProviderAdapter.acquireAccountDetailsByAccount(calculationContext,
//                subsidy, districtName, subsidy.getAccountNumber() + "");

        List<AccountDetail> accountDetails = new ArrayList<>();
        accountDetails.add(new AccountDetail());

        if (!accountDetails.isEmpty()){
            //clear
            subsidyMasterDataBean.clearSubsidyMasterDataList(subsidy.getId());

            AccountDetail accountDetail = accountDetails.get(0);

            Integer numm = (Integer) subsidy.getField(SubsidyDBF.NUMM);

            for (int i=0; i <= numm; ++i){
                Date date = DateUtil.addMonth(subsidy.getDate(), -i);

                addSubsidyMasterData(subsidy, accountDetail, date);
            }

            subsidy.setStatus(RequestStatus.PROCESSED);
        }

        subsidyBean.update(subsidy);
    }

    private void addSubsidyMasterData(Subsidy subsidy, AccountDetail accountDetail, Date date){
        SubsidyMasterData subsidyMasterData = new SubsidyMasterData();

        //fill from remote call
        subsidyMasterData.putField(SubsidyMasterDataDBF.LS, accountDetail.getZheuCode());
        subsidyMasterData.putField(SubsidyMasterDataDBF.DOM, accountDetail.getHouseCode());
        subsidyMasterData.putField(SubsidyMasterDataDBF.REG, accountDetail.getDistrictCode());

        //copy from subsidy
        subsidyMasterData.setSubsidyId(subsidy.getId());

        subsidyMasterData.putField(SubsidyMasterDataDBF.DELO, subsidy.getField(SubsidyDBF.NUMB));
        subsidyMasterData.putField(SubsidyMasterDataDBF.TOT, subsidy.getField(SubsidyDBF.SUBS));
        subsidyMasterData.putField(SubsidyMasterDataDBF.PERIOD, DateUtil.getFirstDayOfMonth(date));
        subsidyMasterData.putField(SubsidyMasterDataDBF.FIO, subsidy.getFio());
        subsidyMasterData.putField(SubsidyMasterDataDBF.ADRES, subsidy.getAddress());
        subsidyMasterData.putField(SubsidyMasterDataDBF.NKW, subsidy.getField(SubsidyDBF.FLAT));
        subsidyMasterData.putField(SubsidyMasterDataDBF.KWART, subsidy.getField(SubsidyDBF.SB1));
        subsidyMasterData.putField(SubsidyMasterDataDBF.OTOPL, subsidy.getField(SubsidyDBF.SB2));
        subsidyMasterData.putField(SubsidyMasterDataDBF.PODOGR, subsidy.getField(SubsidyDBF.SB3));
        subsidyMasterData.putField(SubsidyMasterDataDBF.WODA, subsidy.getField(SubsidyDBF.SB4));
        subsidyMasterData.putField(SubsidyMasterDataDBF.GAZ, subsidy.getField(SubsidyDBF.SB5));
        subsidyMasterData.putField(SubsidyMasterDataDBF.ELEKTR, subsidy.getField(SubsidyDBF.SB6));
        subsidyMasterData.putField(SubsidyMasterDataDBF.STOKI, subsidy.getField(SubsidyDBF.SB8));
        subsidyMasterData.putField(SubsidyMasterDataDBF.KWART_O, subsidy.getField(SubsidyDBF.OB1));
        subsidyMasterData.putField(SubsidyMasterDataDBF.OTOPL_O, subsidy.getField(SubsidyDBF.OB2));
        subsidyMasterData.putField(SubsidyMasterDataDBF.GORWODA_O, subsidy.getField(SubsidyDBF.OB3));
        subsidyMasterData.putField(SubsidyMasterDataDBF.WODA_O, subsidy.getField(SubsidyDBF.OB4));
        subsidyMasterData.putField(SubsidyMasterDataDBF.GAZ_O, subsidy.getField(SubsidyDBF.OB5));
        subsidyMasterData.putField(SubsidyMasterDataDBF.ELEKTR_O, subsidy.getField(SubsidyDBF.OB6));
        subsidyMasterData.putField(SubsidyMasterDataDBF.STOKI_O, subsidy.getField(SubsidyDBF.OB8));

        //date
        subsidyMasterData.putField(SubsidyMasterDataDBF.PR_KV, DateUtil.isSameMonth(subsidy.getDate(), date));
        subsidyMasterData.putField(SubsidyMasterDataDBF.BEGIN0, DateUtil.getFirstDayOfMonth(date));
        subsidyMasterData.putField(SubsidyMasterDataDBF.END0, DateUtil.getLastDayOfMonth(date));

        //servicing organization | todo handle empty servicing organization id exception
        subsidyMasterData.setServicingOrganizationId(subsidyService.getServicingOrganizationId(subsidy.getRequestFileId()));

        subsidyMasterDataBean.save(subsidyMasterData);
    }

    @Override
    public void onError(IExecutorObject object) {
        RequestFile requestFile = (RequestFile) object;

        requestFile.setStatus(RequestFileStatus.FILL_ERROR);
        requestFileBean.save(requestFile);
    }

    @Override
    public String getModuleName() {
        return Module.NAME;
    }

    @Override
    public Class getControllerClass() {
        return SubsidyFillTaskBean.class;
    }

    @Override
    public Log.EVENT getEvent() {
        return Log.EVENT.EDIT;
    }
}
