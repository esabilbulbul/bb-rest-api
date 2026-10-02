/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wapi.core;

import Methods.Users;
import Objects.ssoUserAccounts;
import bb.app.account.AccountMisc;
import bb.app.account.CachingOps_Account;
import bb.app.bill.InventoryBill;
import bb.app.bill.ssoBillShort;
import bb.app.obj.ssoInvBrandItemCodes;
import bb.app.stmt.InventoryStatement;
import entity.user.SsUsrAccounts;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;
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
import jaxesa.redis.ssoRedisLettuce;
import jaxesa.util.Util;
import jaxesa.webapi.ssoAPIResponse;
import redis.clients.jedis.Jedis;
import restapi.jeiRestInterface;

/**
 *
 * @author Administrator
 */
@Path("/api/bll")
public class UIBill implements jeiRestInterface
{
    BigInteger gUserId  = BigInteger.ZERO;
    EntityManager gem;
    ArrayList<String>    gServiceGrants = new ArrayList<String>();

    private static final Logger logger = Logger.getLogger(UIHome.class.getName());

    public UIBill()
    {
        
    }

    @Override
    public void init(String pUserId, String pBrowserId, String p3rdPartyAPIKey, EntityManager pem, String psUserRoleReqs)
    {
        try
        {
            String s = "";

            gUserId = new BigInteger(pUserId);
            gem = pem;
            gem.SetSessionUser(pUserId);
            
            gServiceGrants.addAll(Arrays.asList(psUserRoleReqs.split(",")));

        }
        catch(Exception e)
        {
            
        }
    }

    
    @GET
//    @Path("/nwinvtxn/{aid},"
    @Path("/nwbll/{aid},"
                        + "{lng},"
                        + "{cnt},"
                        + "{sid},"
                        + "{ip},"
                        + "{txn},"
                        + "{bnm},"
                        + "{bix},"
                        + "{bdt},"
                        + "{trt},"
                        + "{dsc},"
                        + "{dct},"
                        + "{src},"
                        + "{pyt},"
                        + "{ptt},"
                        + "{dts}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    //@Callback(type = ThreadActionType.AFTER, source = "newInventoryTransaction", targetClass="apicallbacks.cbInventoryBill_FirstEntry", targetMethod = "newInventoryTransaction_Callback")
    @Callback(type = ThreadActionType.AFTER, source = "newInventoryBill", targetClass="apicallbacks.cbInventoryBill_FirstEntry", targetMethod = "newInventoryTransaction_Callback")
//    public ssoAPIResponse newInventoryTransaction(  @PathParam("aid")                          String pAccId,
    public ssoAPIResponse newInventoryBill(         @PathParam("aid")                          String pAccId,
                                                    @PathParam("lng")                          String psLang,
                                                    @PathParam("cnt")                          String psCountry,
                                                    @PathParam("sid")                          String psSessionId,
                                                    @PathParam("ip")                           String psIP,
                                                    @PathParam("txn")                          String psTxnType,
                                                    @PathParam("bnm")                          String psBrand,
                                                    @PathParam("bix")                          String psBrandId,
                                                    @PathParam("bdt")                          String psBillingDate,
                                                    @PathParam("trt")                          String psTaxRate,
                                                    @PathParam("dsc")                          String psDesc,
                                                    @PathParam("dct")                          String psBottomDiscount,
                                                    @PathParam("src")                          String psBottomSurcharge,
                                                    @PathParam("pyt")                          String psBottomPaymentTerm,
                                                    @PathParam("ptt")                          String psBottomPaymentTermType,
                                                    @PathParam("dts")                          String pjsStmtDets
                                                 ) throws Exception
    {
        try
        {
            
            Util.Tomcat.print2TomcatLog(logger, "/nwbll/ starting", pAccId);
            
            long lTxnDateTime = Util.DateTime.GetDateTime_l();
            
            ssoAPIResponse rsp = new ssoAPIResponse();
            ssoUserAccounts accConnected = new ssoUserAccounts();

            BigInteger lSessionUserId = gUserId;//for now
            BigInteger lTargetAccId   = new BigInteger(pAccId);

            accConnected = Users.connectAccount(gem, gUserId, lTargetAccId, gServiceGrants);
            if(accConnected.bAccessGranted==false)
            {
                if(accConnected.bForce2Login==true)
                    rsp.AuthenticationFailed = true;

                rsp.Response = "err";
                rsp.ResponseMsg = "Account invalid";
                return rsp;
            }

            //String sTxnType = InventoryStatement.getTransactionType(psTxnType);
            String sTxnType = psTxnType;

            Util.Tomcat.print2TomcatLog(logger, "processStatement", pAccId);

            BigInteger InvStmtId = BigInteger.ZERO;
            InvStmtId = InventoryStatement.processStatement(gem, 
                                                            accConnected.userId, 
                                                            accConnected.uid, 
                                                            accConnected.bTaxInPrice,
                                                            accConnected.bdTaxRate,
                                                            psLang, 
                                                            psCountry, 
                                                            psSessionId, 
                                                            sTxnType,
                                                            pjsStmtDets,
                                                            lTxnDateTime);

            //if (InvStmtId==-1)
            if (InvStmtId.compareTo(BigInteger.valueOf(-1))==0)
            {
                // Transaction has been inserted in previous txn
                rsp.Response = "entry-exist";
                rsp.ResponseMsg = "Transaction entry is already exist";
            }
            else
            {
                ssoRedisLettuce lettuce = null;

                try
                {
                     lettuce = Util.Redis.getConnection("newInventoryBill");

                    // 1. Delete Account Page Cache (account - mystats)
                    // 2. Delete Supplier Page Cache
                    CachingOps_Account.deleteAccPageData(lettuce, accConnected.userId, accConnected.uid);

                    lettuce.close();
                }
                catch(Exception e)
                {
                    if(lettuce!=null)
                        lettuce.close();
                }

                rsp.Content  = InvStmtId.toString();
                rsp.Response = "ok";
                rsp.callbackId = InvStmtId.toString();
            }

            //rsp.Response = "entry-exist";//this is for test (remember to remove)

            return rsp;
        }
        catch(Exception e)
        {
            Util.Tomcat.print2TomcatLog(logger, "/nwbll/ exception: " + e.getMessage(), pAccId);
            
            throw e;
        }
    }


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
                        + "{xp},"
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
                                        @PathParam("xp")                           String psNewExpense,
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
            ssoUserAccounts accConnected = new ssoUserAccounts();

            BigInteger lSessionUserId = gUserId;//for now
            BigInteger lTargetAccId   = new BigInteger(pAccId);

            accConnected = Users.connectAccount(gem, gUserId, lTargetAccId, gServiceGrants);
            if(accConnected.bAccessGranted==false)
            {
                if(accConnected.bForce2Login==true)
                    rsp.AuthenticationFailed = true;

                rsp.Response = "err";
                rsp.ResponseMsg = "Account invalid";
                return rsp;
            }

            BigInteger lBillId  = new BigInteger(psBillId);
            BigInteger lBrandId = new BigInteger(psBrandId);

            BigDecimal bdNewTaxRate = new BigDecimal(psNewTaxRate);
            BigDecimal bdDiscRate   = new BigDecimal(psNewDiscRate);
            BigDecimal bdExpense    = new BigDecimal(psNewExpense);

            InventoryBill.updateBillNDetails(gem, 
                                             accConnected.uid,
                                             lBrandId, 
                                             lBillId,
                                             bdDiscRate,
                                             bdNewTaxRate,
                                             bdExpense,
                                             psChanges);

            ssoRedisLettuce lettuce = null;

            try
            {
                lettuce = Util.Redis.getConnection("updateBill");

                // 1. Delete Account Page Cache (account - mystats)
                // 2. Delete Supplier Page Cache
                CachingOps_Account.deleteAccPageData(lettuce, accConnected.userId, accConnected.uid);

                lettuce.close();
            }
            catch(Exception e)
            {
                if(lettuce!=null)
                    lettuce.close();
            }

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
            ssoUserAccounts accConnected = new ssoUserAccounts();

            BigInteger lSessionUserId = gUserId;//for now
            BigInteger lTargetAccId   = new BigInteger(pAccId);

            accConnected = Users.connectAccount(gem, gUserId, lTargetAccId, gServiceGrants);
            if(accConnected.bAccessGranted==false)
            {
                if(accConnected.bForce2Login==true)
                    rsp.AuthenticationFailed = true;

                rsp.Response = "err";
                rsp.ResponseMsg = "Account invalid";
                return rsp;
            }

            BigInteger lBillId = new BigInteger(psBillId);
            BigInteger lBrandId = new BigInteger(psBrandId);
            ssoBillShort Bill = new ssoBillShort();

            AccountMisc.deleteBill( gem, 
                                    accConnected.userId, 
                                    accConnected.uid, 
                                    lBrandId, 
                                    lBillId);
            //deleteBill(gem, gUserId, lAccId, lBillId);
            //Bill = AccountMisc.getBill(gem, gUserId, lBillId);
            //rsp.Content = Util.JSON.Convert2JSON(Bill).toString();
            ssoRedisLettuce lettuce = null;

            try
            {
                lettuce = Util.Redis.getConnection("deleteBillN");

                // 1. Delete Account Page Cache (account - mystats)
                // 2. Delete Supplier Page Cache
                CachingOps_Account.deleteAccPageData(lettuce, accConnected.userId, accConnected.uid);

                lettuce.close();
            }
            catch(Exception e)
            {
                if(lettuce!=null)
                    lettuce.close();
            }
            
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }

    }//DELETE BILL

    @GET
    @Path("/gbll/{aid},"
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
            ssoUserAccounts accConnected = new ssoUserAccounts();

            BigInteger lSessionUserId = gUserId;//for now
            BigInteger lTargetAccId   = new BigInteger(pAccId);

            accConnected = Users.connectAccount(gem, gUserId, lTargetAccId, gServiceGrants);
            if(accConnected.bAccessGranted==false)
            {
                if(accConnected.bForce2Login==true)
                    rsp.AuthenticationFailed = true;

                rsp.Response = "err";
                rsp.ResponseMsg = "Account invalid";
                return rsp;
            }

            BigInteger lBillId = new BigInteger(psBillId);
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
    @Path("/ao2b/{aid},"
                            + "{lng},"
                            + "{cnt},"
                            + "{sid},"
                            + "{ip},"
                            + "{vid},"
                            + "{bll},"
                            + "{opt}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse appendOptions2Bill(   @PathParam("aid")                          String pAccId,
                                                @PathParam("lng")                          String psLang,
                                                @PathParam("cnt")                          String psCountry,
                                                @PathParam("sid")                          String psSessionId,
                                                @PathParam("ip")                           String psIP,
                                                @PathParam("vid")                          String psBrandId,//vendorId
                                                @PathParam("bll")                          String psBillId,
                                                @PathParam("opt")                          String psOptions2Append // "[{"itemcode":"N001","lineid":46891430,"group":"PETROL YESIL","option":"46","quantity":1},{"itemcode":"N002","lineid":46891431,"group":"","option":"","quantity":2},{"itemcode":"N002","lineid":46891431,"group":"","option":"A","quantity":1},{"itemcode":"N003","lineid":46891432,"group":"","option":"","quantity":3},{"itemcode":"N003","lineid":46891432,"group":"","option":"B","quantity":1}]"
                                              ) throws Exception
    {
        try
        {
            //------------------------------------------------------------------
            // 3 types of updates on bill
            // 
            // 1. Updates financial structure of Bill (that leads to removal of old)
            // 2. Updates quantity structure of Bill (that also leads to removal of old)
            // 3. Append (Updates) only options structure of Bill that doesn't change financial structure of Bill
            // 
            // First 2 occurs with BillUpdate
            // 3rd is here
            //------------------------------------------------------------------

            ssoAPIResponse rsp = new ssoAPIResponse();
            ssoUserAccounts accConnected = new ssoUserAccounts();

            Util.Tomcat.print2TomcatLog(logger, "/ao2b/ starting at sid: " + psSessionId + " for billId: " + psBillId + " and param: " + psOptions2Append, pAccId);

            BigInteger lSessionUserId = gUserId;//for now
            BigInteger lTargetAccId   = new BigInteger(pAccId);
            BigInteger lVendorId      = new BigInteger(psBrandId);

            accConnected = Users.connectAccount(gem, gUserId, lTargetAccId, gServiceGrants);
            if(accConnected.bAccessGranted==false)
            {
                if(accConnected.bForce2Login==true)
                    rsp.AuthenticationFailed = true;

                rsp.Response = "err";
                rsp.ResponseMsg = "Account invalid";
                return rsp;
            }

            BigInteger lBillId = new BigInteger(psBillId);

            AccountMisc.updateBillOnJustOptions(gem, lSessionUserId, lTargetAccId, lVendorId, lBillId, psOptions2Append);

            rsp.Content = Util.JSON.Convert2JSON(lBillId).toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            Util.Tomcat.print2TomcatLog(logger, "/ao2b/ exception at sid: " + psSessionId + " detail: " + e.getMessage(), pAccId);

            throw e;
        }
    }


}
