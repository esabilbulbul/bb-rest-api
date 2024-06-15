/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webapi;

import Methods.Users;
import bb.app.account.AccountMisc;
import bb.app.account.ssoAccStmtCore;
import bb.app.bill.ssoBillLine;
import bb.app.account.ssoUIBalanceItem;
import bb.app.account.ssoUIPaymentItem;
import bb.app.account.ssoVendorPayment;
import bb.app.account.ssoVendorPaymentSummary;
import bb.app.bill.InventoryBill;
import bb.app.bill.ssoBillShort;
import bb.app.obj.ssoInvBrandItemCodes;
import bb.app.revolving.RevolvingOperations;
import bb.app.vendor.VendorOps;
import bb.reports.ssReportSearchBalances;
import bb.reports.ssReportSearchPayments;
import entity.user.SsUsrAccounts;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import jaxesa.annotations.Callback;
import jaxesa.annotations.Consumes;
import jaxesa.annotations.GET;
import jaxesa.annotations.MediaType;
import jaxesa.annotations.Path;
import jaxesa.annotations.PathParam;
import jaxesa.annotations.Produces;
import jaxesa.annotations.ThreadActionType;
import jaxesa.annotations.Token;
import jaxesa.annotations.UserGrants;
import jaxesa.annotations.UserRole;
import jaxesa.annotations.VerificationType;
import jaxesa.persistence.EntityManager;
import jaxesa.util.Util;
import jaxesa.webapi.ssoAPIResponse;
import restapi.jeiRestInterface;

/**
 *
 * @author Administrator
 */
@Path("/api/biz/bb/blnc")
public class UIBalances implements jeiRestInterface
{
    long gUserId  = -1;
    EntityManager gem;
    ArrayList<String>    gServiceGrants = new ArrayList<String>();

    public UIBalances()
    {
        
    }

    @Override
    public void init(String pUserId, EntityManager pem, String psUserRoleReqs)
    {
        try
        {
            String s = "";

            gUserId = Long.parseLong(pUserId);
            gem = pem;
            gem.SetSessionUser(pUserId);
            
            gServiceGrants.addAll(Arrays.asList(psUserRoleReqs.split(",")));

        }
        catch(Exception e)
        {
            
        }
    }

    @GET
    @Path("/gtbrndblncsbykywrd/{aid},"
                            + "{lng},"
                            + "{cnt},"
                            + "{sid},"
                            + "{ip},"
                            + "{ky},"
                            + "{rc}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse getBrandBalances( @PathParam("aid")                          String pAccId,
                                            @PathParam("lng")                          String psLang,
                                            @PathParam("cnt")                          String psCountry,
                                            @PathParam("sid")                          String psSessionId,
                                            @PathParam("ip")                           String psIP,
                                            @PathParam("ky")                           String psKeyword,
                                            @PathParam("rc")                           String pResetCache
                                         ) throws Exception
    {
        try
        {
            ArrayList<ssoInvBrandItemCodes> aItemsFound = new ArrayList<ssoInvBrandItemCodes>();

            ssoAPIResponse rsp = new ssoAPIResponse();
            SsUsrAccounts accConnected = new SsUsrAccounts();

            long lSessionUserId = gUserId;//for now
            long lTargetAccId = Long.parseLong(pAccId);

            accConnected = Users.connectAccount(gem, gUserId, lTargetAccId, gServiceGrants);
            if (accConnected==null)
            {
                rsp.Response = "err";
                rsp.ResponseMsg = "Account invalid";
                return rsp;
            }

            boolean bCleanMemoryData = false;
            if (pResetCache.equals("true")==true)
                bCleanMemoryData = true;

            //rsp.Content = Util.JSON.Convert2JSON(aItemsFound).toString();
            ArrayList<ssoUIBalanceItem> balances = new ArrayList<ssoUIBalanceItem>();

            // NOTICE:  Following Method Full on Cache
            //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            balances = ssReportSearchBalances.generate(gem, accConnected.userId, psKeyword);
            rsp.Content = Util.JSON.Convert2JSON(balances).toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    @GET
    @Path("/gtbrndstmt/{aid},"
                            + "{lng},"
                            + "{cnt},"
                            + "{sid},"
                            + "{ip},"
                            + "{brnd},"
                            + "{rwi},"
                            + "{alr}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse getBrandStatement(@PathParam("aid")                          String pAccId,
                                            @PathParam("lng")                          String psLang,
                                            @PathParam("cnt")                          String psCountry,
                                            @PathParam("sid")                          String psSessionId,
                                            @PathParam("ip")                           String psIP,
                                            @PathParam("brnd")                         String psBrandId,
                                            @PathParam("rwi")                          String pRowIndex,
                                            @PathParam("alr")                          String pbAllRows
                                         ) throws Exception
    {
        try
        {
            ArrayList<ssoInvBrandItemCodes> aItemsFound = new ArrayList<ssoInvBrandItemCodes>();

            ssoAPIResponse rsp = new ssoAPIResponse();
            SsUsrAccounts accConnected = new SsUsrAccounts();

            long lSessionUserId = gUserId;//for now
            long lTargetAccId   = Long.parseLong(pAccId);

            accConnected = Users.connectAccount(gem, gUserId, lTargetAccId, gServiceGrants);
            if (accConnected==null)
            {
                rsp.Response = "err";
                rsp.ResponseMsg = "Account invalid";
                return rsp;
            }

            //rsp.Content = Util.JSON.Convert2JSON(aItemsFound).toString();
            int ThisYear = Integer.parseInt(Util.DateTime.GetDateTime_s().substring(0, 4));

            long lVendorId = Long.parseLong(psBrandId);
            
            int lStartRowIndex = Integer.parseInt(pRowIndex);
            boolean bFullRows  = Boolean.parseBoolean(pbAllRows);

            ArrayList<ssoAccStmtCore> brandStmt = new ArrayList<ssoAccStmtCore>();
            brandStmt = AccountMisc.getStatement4Brand( gem, 
                                                        accConnected.userId, 
                                                        accConnected.uid, 
                                                        lVendorId, 
                                                        ThisYear, 
                                                        lStartRowIndex, 
                                                        bFullRows);//-1 WILL BE FIXED
            rsp.Content = Util.JSON.Convert2JSON(brandStmt).toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    @GET
    @Path("/gtstmtbll/{aid},"
                            + "{lng},"
                            + "{cnt},"
                            + "{sid},"
                            + "{ip},"
                            + "{bid}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse getBillDetails( @PathParam("aid")                          String pAccId,
                                            @PathParam("lng")                          String psLang,
                                            @PathParam("cnt")                          String psCountry,
                                            @PathParam("sid")                          String psSessionId,
                                            @PathParam("ip")                           String psIP,
                                            @PathParam("bll")                          String psBillId
                                         ) throws Exception
    {
        try
        {
            ArrayList<ssoInvBrandItemCodes> aItemsFound = new ArrayList<ssoInvBrandItemCodes>();

            ssoAPIResponse rsp = new ssoAPIResponse();
            SsUsrAccounts accConnected = new SsUsrAccounts();

            long lSessionUserId = gUserId;//for now
            long lTargetAccId   = Long.parseLong(pAccId);

            accConnected = Users.connectAccount(gem, gUserId, lTargetAccId, gServiceGrants);
            if (accConnected==null)
            {
                rsp.Response = "err";
                rsp.ResponseMsg = "Account invalid";
                return rsp;
            }

            long lBillId = Long.parseLong(psBillId);
            ssoBillShort Bill = new ssoBillShort();

            Bill = AccountMisc.getBill(gem, accConnected.userId, lBillId);

            rsp.Content = Util.JSON.Convert2JSON(Bill).toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    @GET
    @Path("/delbll/{aid},"
                            + "{lng},"
                            + "{cnt},"
                            + "{sid},"
                            + "{ip},"
                            + "{bid},"
                            + "{bll}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse deleteBillN(  @PathParam("aid")                          String pAccId,
                                        @PathParam("lng")                          String psLang,
                                        @PathParam("cnt")                          String psCountry,
                                        @PathParam("sid")                          String psSessionId,
                                        @PathParam("ip")                           String psIP,
                                        @PathParam("bid")                          String psBrandId,
                                        @PathParam("bll")                          String psBillId
                                     ) throws Exception
    {
        try
        {
            ArrayList<ssoInvBrandItemCodes> aItemsFound = new ArrayList<ssoInvBrandItemCodes>();

            ssoAPIResponse rsp = new ssoAPIResponse();
            SsUsrAccounts accConnected = new SsUsrAccounts();

            long lSessionUserId = gUserId;//for now
            long lTargetAccId   = Long.parseLong(pAccId);

            accConnected = Users.connectAccount(gem, gUserId, lTargetAccId, gServiceGrants);
            if (accConnected==null)
            {
                rsp.Response = "err";
                rsp.ResponseMsg = "Account invalid";
                return rsp;
            }

            long lBillId = Long.parseLong(psBillId);
            long lBrandId = Long.parseLong(psBrandId);
            ssoBillShort Bill = new ssoBillShort();

            AccountMisc.deleteBill( gem, 
                                    accConnected.userId, 
                                    accConnected.uid, 
                                    lBrandId, 
                                    lBillId);
            //deleteBill(gem, gUserId, lAccId, lBillId);
            //Bill = AccountMisc.getBill(gem, gUserId, lBillId);
            //rsp.Content = Util.JSON.Convert2JSON(Bill).toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
        
    }//DELETE BILL

    @GET
    @Path("/upbll/{aid},"
                        + "{lng},"
                        + "{cnt},"
                        + "{sid},"
                        + "{ip},"
                        + "{bid},"
                        + "{bll},"
                        + "{dsc},"
                        + "{tx},"
                        + "{chg}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @Callback(type = ThreadActionType.AFTER, source = "updateBill", targetClass="apicallbacks.cbInventoryBill_Update", targetMethod = "updateBill_Transaction_Callback")
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse updateBill(   @PathParam("aid")                          String pAccId,
                                        @PathParam("lng")                          String psLang,
                                        @PathParam("cnt")                          String psCountry,
                                        @PathParam("sid")                          String psSessionId,
                                        @PathParam("ip")                           String psIP,
                                        @PathParam("bid")                          String psBrandId,
                                        @PathParam("bll")                          String psBillId,
                                        @PathParam("dsc")                          String psNewDiscRate,
                                        @PathParam("tx")                           String psNewTaxRate,
                                        @PathParam("chg")                          String psChanges
                                     ) throws Exception
    {
        try
        {
            /*
                CHANGES Example;
                = {"46390262":{"x":"true"},"46390263":{"p":{"o":"155.00","n":"165.00"}},"46390264":{"q":{"o":"2","n":"3"}}}
                - 46390263 = bill line Id (dets.UID)
                - p = price
                - q = quantity
                - c = item code
                - s = surcharge
                - d = discount
                - x = deleted 
                - old = the value before changed
                - new = the new value 
                = 
            */

            ArrayList<ssoInvBrandItemCodes> aItemsFound = new ArrayList<ssoInvBrandItemCodes>();

            ssoAPIResponse rsp = new ssoAPIResponse();
            SsUsrAccounts accConnected = new SsUsrAccounts();

            long lSessionUserId = gUserId;//for now
            long lTargetAccId   = Long.parseLong(pAccId);

            accConnected = Users.connectAccount(gem, gUserId, lTargetAccId, gServiceGrants);
            if (accConnected==null)
            {
                rsp.Response = "err";
                rsp.ResponseMsg = "Account invalid";
                return rsp;
            }

            long lBillId  = Long.parseLong(psBillId);
            long lBrandId = Long.parseLong(psBrandId);

            BigDecimal bdNewTaxRate = new BigDecimal(psNewTaxRate);
            InventoryBill.updateBillNDetails(gem, 
                                             accConnected.uid, 
                                             lBrandId, 
                                             lBillId, 
                                             bdNewTaxRate.toString(), 
                                             psChanges);

            rsp.callbackId = psBillId;
            rsp.Response = "ok";
            
            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }
 
    @GET
    @Path("/uprv/{aid},"
                    + "{lng},"
                    + "{cnt},"
                    + "{sid},"
                    + "{ip},"
                    + "{bid},"
                    + "{ryr},"
                    + "{rvl}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    //@Callback(type = ThreadActionType.AFTER, source = "updateBill", targetClass="apicallbacks.cbInventoryBill_Update", targetMethod = "updateBill_Transaction_Callback")
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse updateRevolving(  @PathParam("aid")                          String pAccId,
                                            @PathParam("lng")                          String psLang,
                                            @PathParam("cnt")                          String psCountry,
                                            @PathParam("sid")                          String psSessionId,
                                            @PathParam("ip")                           String psIP,
                                            @PathParam("bid")                          String psBrandId,
                                            @PathParam("ryr")                          String psRevolvingYear,
                                            @PathParam("rvl")                          String psRevolvingValue
                                         ) throws Exception
    {
        try
        {

            ArrayList<ssoInvBrandItemCodes> aItemsFound = new ArrayList<ssoInvBrandItemCodes>();

            ssoAPIResponse rsp = new ssoAPIResponse();
            SsUsrAccounts accConnected = new SsUsrAccounts();

            long lSessionUserId = gUserId;//for now
            long lTargetAccId   = Long.parseLong(pAccId);

            accConnected = Users.connectAccount(gem, gUserId, lTargetAccId, gServiceGrants);
            if (accConnected==null)
            {
                rsp.Response = "err";
                rsp.ResponseMsg = "Account invalid";
                return rsp;
            }

            long lVendorId = Long.parseLong(psBrandId);

            RevolvingOperations.updateRevolvingBalance( gem, 
                                                        accConnected.userId, 
                                                        lTargetAccId, 
                                                        lVendorId, 
                                                        psRevolvingYear, 
                                                        psRevolvingValue);
            rsp.Response = "ok";
            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

}
        
