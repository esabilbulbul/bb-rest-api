/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wapi.cashregister;

import Methods.Users;
import Objects.ssoUserAccounts;
import bb.app.obj.ssoCashRegisterParams;
import bb.app.obj.ssoCashierPOSMenuItem;
import bb.app.obj.ssoInvBrandItemCodes;
import bb.app.obj.ssoMerchantPreferences;
import bb.cashier.CashRegister;
import bb.cashier.ssoBatchCheckResult;
import bb.cashier.ssoCashRegTxnSummary;
import bb.cashier.ssoEODTxnRow;
import bb.reports.ssReportSearchBalances;
import entity.user.SsUsrAccounts;
import java.math.BigInteger;
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
import misc.DekontMisc;
import restapi.jeiRestInterface;
/**
 * 
 * @author Administrator
 */
@Path("/api/cshreg")
public class UICashier implements jeiRestInterface
{
    // Variables dedicated to the session
    BigInteger gUserId  = BigInteger.ZERO;
    String gBrowserId = "";
    EntityManager gem;
    ArrayList<String>    gServiceGrants = new ArrayList<String>();

    public UICashier()
    {
        
    }

    @Override
    public void init(String pUserId, String pBrowserId, String p3rdPartyAPIKey, EntityManager pem, String psUserRoleReqs)
    {
        try
        {
            String s = "";

            gUserId = new BigInteger(pUserId);
            gBrowserId = pBrowserId;
            gem = pem;
            gem.SetSessionUser(pUserId);
            
            gServiceGrants.addAll(Arrays.asList(psUserRoleReqs.split(",")));

        }
        catch(Exception e)
        {
            
        }
    }
    
    @GET
    @Path("/gtcshmn/{aid},"
                    + "{lng},"
                    + "{cnt},"
                    + "{sid},"
                    + "{ip},"
                    + "{rst}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse getCashierPOSMenu( @PathParam("aid")                          String pAccId,
                                             @PathParam("lng")                          String psLang,
                                             @PathParam("cnt")                          String psCountry,
                                             @PathParam("sid")                          String psSessionId,
                                             @PathParam("ip")                           String psIP,
                                             @PathParam("rst")                          String psReset
                                         ) throws Exception
    {
        try
        {
            ArrayList<ssoInvBrandItemCodes> aItemsFound = new ArrayList<ssoInvBrandItemCodes>();

            ssoAPIResponse rsp = new ssoAPIResponse();
            ssoUserAccounts accConnected = new ssoUserAccounts();

            BigInteger lSessionUserId = gUserId;//for now
            BigInteger lTargetAccId = new BigInteger(pAccId);

            accConnected = Users.connectAccount(gem, gUserId, lTargetAccId, gServiceGrants);
            if(accConnected.bAccessGranted==false)
            {
                if(accConnected.bForce2Login==true)
                    rsp.AuthenticationFailed = true;

                rsp.Response = "err";
                rsp.ResponseMsg = "Account invalid";
                return rsp;
            }

            boolean bReset = false;
            if (psReset.trim().equals("Y")==true)
                bReset = true;

            ssoCashRegisterParams CRParams = new ssoCashRegisterParams();
            CRParams = CashRegister.getParams(  gem, 
                                                accConnected.userId, 
                                                accConnected.uid,
                                                bReset);

            if (psReset.trim().equals("Y")==true)
                gem.flush();

            CRParams.accSet = DekontMisc.getShortMerchantPreferences(gem, accConnected.userId, accConnected.uid);

            rsp.Content = Util.JSON.Convert2JSON(CRParams).toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    @GET
    @Path("/nwcshsltxn/{aid},"
                    + "{lng},"
                    + "{cnt},"
                    + "{sid},"
                    + "{ip},"
            
                    + "{crt},"
                    + "{tot},"
                    + "{pay},"
                    + "{cst},"
                    + "{ttr},"
                    + "{bn},"
                    + "{stn}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @Callback(type = ThreadActionType.AFTER, source = "newCashierTxn", targetClass="apicallbacks.cbCashierTxnSale", targetMethod = "newTxnSale")
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse newCashierTxn(    @PathParam("aid")                           String pAccId,
                                            @PathParam("lng")                           String psLang,
                                            @PathParam("cnt")                           String psCountry,
                                            @PathParam("sid")                           String psSessionId,
                                            @PathParam("ip")                            String psIP,

                                            @PathParam("crt")                           String psCartOrders,
                                            @PathParam("tot")                           String psTotals,
                                            @PathParam("pay")                           String psPayments,
                                            @PathParam("cst")                           String psCustomer,
                                            @PathParam("ttr")                           String psTxnRefund,

                                            @PathParam("bn")                            String psBatchNo,
                                            @PathParam("stn")                           String psStan
                                          ) throws Exception
    {
        try
        {
            long lTxnDateTime = Util.DateTime.GetDateTime_l();

            ArrayList<ssoInvBrandItemCodes> aItemsFound = new ArrayList<ssoInvBrandItemCodes>();

            ssoAPIResponse rsp = new ssoAPIResponse();
            ssoUserAccounts accConnected = new ssoUserAccounts();

            BigInteger lSessionUserId = gUserId;//for now
            BigInteger lTargetAccId = new BigInteger(pAccId);

            accConnected = Users.connectAccount(gem, gUserId, lTargetAccId, gServiceGrants);
            if(accConnected.bAccessGranted==false)
            {
                if(accConnected.bForce2Login==true)
                    rsp.AuthenticationFailed = true;

                rsp.Response = "err";
                rsp.ResponseMsg = "Account invalid";
                return rsp;
            }

            boolean bRefund = false;
            if(psTxnRefund.equals("Y")==true)
                bRefund = true;

            int iBatchNo = Integer.parseInt(psBatchNo);
            int iStan    = Integer.parseInt(psStan);

            // Check if Txn with this Batch + Stan received before    
            ssoBatchCheckResult txnResult = new ssoBatchCheckResult();
            txnResult = CashRegister.isTransactionRepeat(gem, accConnected.userId, accConnected.uid, gBrowserId, iBatchNo, iStan);

            if(txnResult.stanFound==false)
            {
                //JsonObject jsoInvStmt = Util.JSON.toJsonObject(pjsStmt);
                CashRegister.saveSaleTransaction(gem,
                                                 accConnected.userId,
                                                 accConnected.uid,//account Id
                                                 gBrowserId,
                                                 psCartOrders,
                                                 psTotals,
                                                 psPayments,
                                                 psCustomer,
                                                 bRefund,
                                                 lTxnDateTime,
                                                 iBatchNo,
                                                 iStan);

            }

            if(txnResult.newBatch==true)
            {
                // CREATE NEW BATCH + ADD STAN
                CashRegister.addNewBatch(gem, 
                                         accConnected.userId,
                                         accConnected.uid, 
                                         gBrowserId, 
                                         iBatchNo, 
                                         iStan);
            }
            else if(txnResult.stanFound==false)
            {
                // UPDATE BATCH w STAN 
                CashRegister.addNewStan(gem, 
                                        accConnected.userId,
                                        accConnected.uid, 
                                        gBrowserId, 
                                        iBatchNo, 
                                        iStan);
            }
            //rsp.Content = Util.JSON.Convert2JSON(CRParams).toString();
            rsp.Response = "ok";// save or dont save but always return ok

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    @GET
    @Path("/gtxnhst/{aid},"
                    + "{lng},"
                    + "{cnt},"
                    + "{sid},"
                    + "{ip},"
                    + "{ky},"
                    + "{pg}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse getTxnSalesHistory( @PathParam("aid")                          String pAccId,
                                              @PathParam("lng")                          String psLang,
                                              @PathParam("cnt")                          String psCountry,
                                              @PathParam("sid")                          String psSessionId,
                                              @PathParam("ip")                           String psIP,
                                              @PathParam("ky")                           String psKeyword,
                                              @PathParam("pg")                           String psPageNumber
                                            ) throws Exception
    {
        try
        {
            ArrayList<ssoInvBrandItemCodes> aItemsFound = new ArrayList<ssoInvBrandItemCodes>();

            ssoAPIResponse rsp = new ssoAPIResponse();
            ssoUserAccounts accConnected = new  ssoUserAccounts();

            BigInteger lSessionUserId = gUserId;//for now
            BigInteger lTargetAccId = new BigInteger(pAccId);

            accConnected = Users.connectAccount(gem, gUserId, lTargetAccId, gServiceGrants);
            if(accConnected.bAccessGranted==false)
            {
                if(accConnected.bForce2Login==true)
                    rsp.AuthenticationFailed = true;

                rsp.Response = "err";
                rsp.ResponseMsg = "Account invalid";
                return rsp;
            }

            ArrayList<ssoCashRegTxnSummary> txnHistory = new ArrayList<ssoCashRegTxnSummary>();

            txnHistory   = CashRegister.getTxnHistory(gem, 
                                                      accConnected.userId, 
                                                      accConnected.uid, 
                                                      psKeyword,
                                                      psPageNumber);
            rsp.Content  = Util.JSON.Convert2JSON(txnHistory).toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    @GET
    @Path("/gtbd/{aid},"
                    + "{lng},"
                    + "{cnt},"
                    + "{sid},"
                    + "{ip},"
                    + "{dt},"
                    + "{ky},"
                    + "{rst},"
                    + "{pg}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse getTxnsByDate(@PathParam("aid")                          String pAccId,
                                        @PathParam("lng")                          String psLang,
                                        @PathParam("cnt")                          String psCountry,
                                        @PathParam("sid")                          String psSessionId,
                                        @PathParam("ip")                           String psIP,
                                        @PathParam("dt")                           String psDateYYYYMMDD,
                                        @PathParam("ky")                           String psKeyword,
                                        @PathParam("rst")                          String psReset,
                                        @PathParam("pg")                           String psPageNumber
                                      ) throws Exception
    {
        try
        {
            ArrayList<ssoInvBrandItemCodes> aItemsFound = new ArrayList<ssoInvBrandItemCodes>();

            ssoAPIResponse rsp = new ssoAPIResponse();
            ssoUserAccounts accConnected = new ssoUserAccounts();

            BigInteger lSessionUserId = gUserId;//for now
            BigInteger lTargetAccId = new BigInteger(pAccId);

            accConnected = Users.connectAccount(gem, gUserId, lTargetAccId, gServiceGrants);
            if(accConnected.bAccessGranted==false)
            {
                if(accConnected.bForce2Login==true)
                    rsp.AuthenticationFailed = true;

                rsp.Response = "err";
                rsp.ResponseMsg = "Account invalid";
                return rsp;
            }

            long lDateYYYYMMDD_Today    = Util.DateTime.GetDateTime("YYYYMMDD");
            long lDateYYYYMMDD_Request  = Long.parseLong(psDateYYYYMMDD);

            ArrayList<ssoEODTxnRow> aEODTxnRows = new ArrayList<ssoEODTxnRow>();
            aEODTxnRows = CashRegister.getTxnHistoryByDate( gem, 
                                                            gUserId, 
                                                            lTargetAccId, 
                                                            psDateYYYYMMDD,
                                                            psKeyword,
                                                            psPageNumber);
            rsp.Content  = Util.JSON.Convert2JSON(aEODTxnRows).toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

}

