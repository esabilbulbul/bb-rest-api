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
import bb.app.account.ssoUIPaymentItem;
import bb.app.account.ssoVendorPayment;
import bb.app.obj.ssoInvBrandItemCodes;
import bb.app.payment.PaymentOps;
import bb.app.vendor.VendorOps;
import bb.reports.ssReportSearchPayments;
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
import jaxesa.redis.ssoRedisLettuce;
import jaxesa.util.Util;
import jaxesa.webapi.ssoAPIResponse;
import redis.clients.jedis.Jedis;
import restapi.jeiRestInterface;

/**
 *
 * @author Administrator
 */
@Path("/api/pymnts")
public class UIPayments implements jeiRestInterface
{
    // session variables
    BigInteger gUserId  = BigInteger.ZERO;
    EntityManager gem;
    ArrayList<String> gServiceGrants = new ArrayList<String>();

    public UIPayments()
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
    @Path("/anwpymnt/{aid},"
                        + "{lng},"
                        + "{cnt},"
                        + "{sid},"
                        + "{ip},"
                        + "{ptp},"
                        + "{bid},"
                        + "{bnm},"
                        + "{amtp},"
                        + "{amti},"
                        + "{ddt},"
                        + "{wdt},"
                        + "{inu},"
                        + "{dsc},"
                        + "{gid},"
                        + "{grp},"
                        + "{psn}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    @Callback(type = ThreadActionType.AFTER, source = "addNewPayment", targetClass="apicallbacks.cbInventoryPayment_New", targetMethod = "newPayment_Transaction_Callback")
    public ssoAPIResponse addNewPayment(@PathParam("aid")                          String pAccId,
                                        @PathParam("lng")                          String psLang,
                                        @PathParam("cnt")                          String psCountry,
                                        @PathParam("sid")                          String psSessionId,
                                        @PathParam("ip")                           String psIP,

                                        @PathParam("ptp")                          String psPaymentType,
                                        @PathParam("bid")                          String psBrandId,
                                        @PathParam("bnm")                          String psBrandName,
                                        @PathParam("amtp")                         String psAmountPrincipal,
                                        @PathParam("amti")                         String psAmountInterest,
                                        @PathParam("ddt")                          String psDueDate,
                                        @PathParam("wdt")                          String psWritingDate,
                                        @PathParam("inu")                          String psInstallmentNumber,
                                        @PathParam("dsc")                          String psDesc,
                                        @PathParam("gid")                          String psPaymentGroupId,
                                        @PathParam("gnm")                          String psPaymentGroupName,
                                        @PathParam("psn")                          String psIsPaymentSendDirection
                                     ) throws Exception
    {
        try
        {
            ssoAPIResponse rsp = new ssoAPIResponse();

            ssoUserAccounts accConnected = new ssoUserAccounts();

            BigInteger lSessionUserId = gUserId;//for now
            BigInteger lTargetAccId   = new BigInteger(pAccId);
            
            long lTxnDateTime = Util.DateTime.GetDateTime_l();
            
            accConnected = Users.connectAccount(gem, lSessionUserId, lTargetAccId, gServiceGrants);
            if(accConnected.bAccessGranted==false)
            {
                if(accConnected.bForce2Login==true)
                    rsp.AuthenticationFailed = true;

                rsp.Response = "err";
                rsp.ResponseMsg = "Account invalid";
                return rsp;
            }

            boolean bPaymentSent = false;
            if(psIsPaymentSendDirection.equals("Y")==true)
                bPaymentSent = true;

            BigInteger lUID = VendorOps.addPayment4Vendor(  gem,
                                                            accConnected.userId,
                                                            accConnected.uid,
                                                            psPaymentType, 
                                                            "",//for now
                                                            psBrandId,
                                                            psBrandName,
                                                            psAmountPrincipal,
                                                            psAmountInterest,
                                                            psWritingDate.replace("-", "").replace(".",""), 
                                                            psDueDate.replace("-", "").replace(".",""), 
                                                            psInstallmentNumber,
                                                            psDesc,
                                                            psPaymentGroupId,
                                                            psPaymentGroupName,
                                                            bPaymentSent,
                                                            lTxnDateTime);

            ssoRedisLettuce lettuce = null;

            try
            {
                lettuce = Util.Redis.getConnection("addNewPayment");

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
            
            rsp.Content = Util.JSON.Convert2JSON(lUID).toString();
            rsp.callbackId = lUID.toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }

    }

    @GET
    @Path("/updpymnt/{aid},"
                        + "{lng},"
                        + "{cnt},"
                        + "{sid},"
                        + "{ip},"

                        + "{pid},"
                        + "{pai},"
                        + "{ptp},"
                        + "{bid},"
                        + "{bnm},"
                        + "{amtp},"
                        + "{amti},"
                        + "{ddt},"
                        + "{wdt},"
                        + "{inu},"
                        + "{dsc},"
                        + "{psn},"
                        + "{gid},"
                        + "{gnm}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    @Callback(type = ThreadActionType.AFTER, source = "updatePayment", targetClass="apicallbacks.cbInventoryPayment_Update", targetMethod = "updatePayment_Transaction_Callback")
    public ssoAPIResponse updatePayment(@PathParam("aid")                          String pAccId,
                                        @PathParam("lng")                          String psLang,
                                        @PathParam("cnt")                          String psCountry,
                                        @PathParam("sid")                          String psSessionId,
                                        @PathParam("ip")                           String psIP,

                                        @PathParam("pid")                          String psPaymentId,
                                        @PathParam("pai")                          String psPaymentAccId,
                                        @PathParam("ptp")                          String psPaymentType,
                                        @PathParam("bid")                          String psBrandId,
                                        @PathParam("bnm")                          String psBrandName,
                                        @PathParam("amtp")                         String psAmountPrincipal,
                                        @PathParam("amti")                         String psAmountInterest,
                                        @PathParam("ddt")                          String psDueDate,
                                        @PathParam("wdt")                          String psWritingDate,
                                        @PathParam("inu")                          String psInstallmentNumber,
                                        @PathParam("dsc")                          String psDesc,
                                        @PathParam("psn")                          String psIsPaymentSendDirection,
                                        @PathParam("gid")                          String psPaymentGroupId,
                                        @PathParam("gnm")                          String psPaymentGroupName
                                     ) throws Exception
    {
        try
        {
            ssoAPIResponse rsp = new ssoAPIResponse();

            ssoUserAccounts accConnected = new ssoUserAccounts();

            BigInteger lSessionUserId = gUserId;//for now
            BigInteger lTargetAccId   = new BigInteger(pAccId);

            long lTxnDateTime = Util.DateTime.GetDateTime_l();

            accConnected = Users.connectAccount(gem, lSessionUserId, lTargetAccId, gServiceGrants);
            if(accConnected.bAccessGranted==false)
            {
                if(accConnected.bForce2Login==true)
                    rsp.AuthenticationFailed = true;

                rsp.Response = "err";
                rsp.ResponseMsg = "Account invalid";
                return rsp;
            }

            BigInteger lPaymentId = new BigInteger(psPaymentId);
            String sDueDate     = psDueDate.replace("-", "").replace(".", "");
            String sWritingDate = psWritingDate.replace("-", "").replace(".", "");
            BigInteger lPaymentAccId = new BigInteger(psPaymentAccId);

            boolean bPaymentSent = false;
            if(psIsPaymentSendDirection.equals("Y")==true)
                bPaymentSent = true;

            BigInteger lUID = VendorOps.updatePayment(    gem,
                                                    accConnected.userId,
                                                    accConnected.uid,
                                                    lPaymentId,
                                                    lPaymentAccId,
                                                    psPaymentType, 
                                                    psBrandId, 
                                                    psBrandName, 
                                                    psAmountPrincipal, 
                                                    psAmountInterest, 
                                                    sWritingDate, 
                                                    sDueDate, 
                                                    psInstallmentNumber, 
                                                    psDesc,
                                                    bPaymentSent,
                                                    psPaymentGroupId,
                                                    psPaymentGroupName,
                                                    lTxnDateTime);

            ssoRedisLettuce lettuce = null;
            try
            {
                lettuce = Util.Redis.getConnection("updatePayment");

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

            rsp.Content = Util.JSON.Convert2JSON(lUID).toString();
            rsp.callbackId = lUID.toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    @GET
    @Path("/delpym/{aid},"
                    + "{lng},"
                    + "{cnt},"
                    + "{sid},"
                    + "{ip},"
                    + "{bid},"
                    + "{pid},"
                    + "{p_aid},"
                    + "{p_brnd}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse deletePaymentN(   @PathParam("aid")                          String pAccId,
                                            @PathParam("lng")                          String psLang,
                                            @PathParam("cnt")                          String psCountry,
                                            @PathParam("sid")                          String psSessionId,
                                            @PathParam("ip")                           String psIP,
                                            @PathParam("bid")                          String psBrowserId,
                                            @PathParam("pid")                          String psPaymentId,
                                            @PathParam("p_aid")                        String psPaymentAccId,
                                            @PathParam("p_brnd")                       String psPaymentBrandId
                                         ) throws Exception
    {
        try
        {
            ArrayList<ssoInvBrandItemCodes> aItemsFound = new ArrayList<ssoInvBrandItemCodes>();

            ssoAPIResponse rsp = new ssoAPIResponse();
            ssoUserAccounts accConnected = new ssoUserAccounts();

            BigInteger lSessionUserId = gUserId;//for now
            BigInteger lTargetAccId   = new BigInteger(pAccId);

            accConnected = Users.connectAccount(gem, lSessionUserId, lTargetAccId, gServiceGrants);
            if(accConnected.bAccessGranted==false)
            {
                if(accConnected.bForce2Login==true)
                    rsp.AuthenticationFailed = true;

                rsp.Response = "err";
                rsp.ResponseMsg = "Account invalid";
                return rsp;
            }

            BigInteger lPaymentId       = new BigInteger(psPaymentId);//UID
            BigInteger lPym_AccountId   = new BigInteger(psPaymentAccId);
            BigInteger lPym_BrandId     = new BigInteger(psPaymentBrandId);
            //ssoBillShort Bill = new ssoBillShort();
            String sAmountPrincipal = "0";//delete always = 0
            String sAmountInterest  = "0";//delete always = 0

            PaymentOps.deletePayment(gem, 
                                     accConnected.userId, 
                                     accConnected.uid, 
                                     lPaymentId, 
                                     lPym_AccountId, 
                                     lPym_BrandId,
                                     sAmountPrincipal,
                                     sAmountInterest);

            //deleteBill(gem, gUserId, lAccId, lBillId);
            //Bill = AccountMisc.getBill(gem, gUserId, lBillId);
            //rsp.Content = Util.JSON.Convert2JSON(Bill).toString();
            ssoRedisLettuce lettuce = null;

            try
            {
                lettuce = Util.Redis.getConnection("deletePaymentN");

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

    }//DELETE PAYMENT

    @GET
    @Path("/gtpymdts/{lng},"
                            + "{cnt},"
                            + "{bid},"
                            + "{sid},"
                            + "{ip},"
                            + "{aid},"
                            + "{pid},"
                            + "{rc}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse getPaymentDetails(    @PathParam("lng")         String psLang,
                                                @PathParam("cnt")         String psCountry,
                                                @PathParam("bid")         String psBrowserId,
                                                @PathParam("sid")         String psUISessionId,//don't change the paramter name (sid) 
                                                @PathParam("ip")          String psIp,
                                                @PathParam("aid")         String pAccId,
                                                @PathParam("pid")         String pbPaymentId,
                                                @PathParam("rc")          String pResetCache
                                           ) throws Exception
    {
        try
        {
            ssoAPIResponse rsp = new ssoAPIResponse();
            ssoUserAccounts accConnected = new ssoUserAccounts();

            BigInteger lSessionUserId = gUserId;//for now
            BigInteger lTargetAccId   = new BigInteger(pAccId);

            accConnected = Users.connectAccount(gem, lSessionUserId, lTargetAccId, gServiceGrants);
            if(accConnected.bAccessGranted==false)
            {
                if(accConnected.bForce2Login==true)
                    rsp.AuthenticationFailed = true;

                rsp.Response = "err";
                rsp.ResponseMsg = "Account invalid";
                return rsp;
            }

            ssoVendorPayment paymentDetail = new ssoVendorPayment();

            BigInteger lPaymentId = new BigInteger(pbPaymentId);
            paymentDetail   = PaymentOps.getPaymentDetails( gem, 
                                                            accConnected.userId, 
                                                            accConnected.uid, 
                                                            lPaymentId);

            if(paymentDetail!=null)
            {
                rsp.Content = Util.JSON.Convert2JSON(paymentDetail).toString();
                rsp.Response = "ok";
            }
            else
            {
                rsp.Response = "nf";
            }    

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    @GET
    @Path("/gtpymsmry/{lng},"
                            + "{cnt},"
                            + "{bid},"
                            + "{sid},"
                            + "{ip},"
                            + "{aid},"
                            + "{ky},"
                            + "{rc},"
                            + "{yy},"
                            + "{vid},"
                            + "{pgp},"
                            + "{alr},"
                            + "{pg}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse getAccountPaymentsSummary(    @PathParam("lng")         String psLang,
                                                        @PathParam("cnt")         String psCountry,
                                                        @PathParam("bid")         String psBrowserId,
                                                        @PathParam("sid")         String psUISessionId,//don't change the paramter name (sid) 
                                                        @PathParam("ip")          String psIp,
                                                        @PathParam("aid")         String pAccId,
                                                        @PathParam("ky")          String pKeyword,//Default: N. this set Y to skip memory read. In other words, memory will be wiped and the data will be refetched.
                                                        @PathParam("rc")          String pResetCache,//,
                                                        @PathParam("yy")          String pYear,//,
                                                        @PathParam("vid")         String pVendorId,
                                                        @PathParam("pgp")         String pPaymentGroupId,
                                                        @PathParam("alr")         String pbAllRows,
                                                        @PathParam("pg")          String psPageNumber
                                                   ) throws Exception
    {

        try
        {
            ssoAPIResponse rsp = new ssoAPIResponse();

            ssoUserAccounts accConnected = new ssoUserAccounts();

            BigInteger lSessionUserId = gUserId;//for now
            BigInteger lTargetAccId   = new BigInteger(pAccId);

            accConnected = Users.connectAccount(gem, lSessionUserId, lTargetAccId, gServiceGrants);
            if(accConnected.bAccessGranted==false)
            {
                if(accConnected.bForce2Login==true)
                    rsp.AuthenticationFailed = true;

                rsp.Response = "err";
                rsp.ResponseMsg = "Account invalid";
                return rsp;
            }

            ArrayList<ssoUIPaymentItem> summaryReport = new ArrayList<ssoUIPaymentItem>();
            boolean bCleanMemoryData = false;

            if (pResetCache.equals("true")==true)
                bCleanMemoryData = true;

            //payments = AccountMisc.getVendorPaymentSummary(gem, gUserId, lAccId, pKeyword);
            //int ThisYear = Integer.parseInt(Util.DateTime.GetDateTime_s().substring(0, 4));
            long lYear = Long.parseLong(pYear);
            
            BigInteger biVendorId = BigInteger.ZERO;
            if(pVendorId.trim().length()>0)
                biVendorId = new BigInteger(pVendorId);
            
            BigInteger biPaymentGroupId = BigInteger.ZERO;
            if(pPaymentGroupId.trim().length()>0)
                biPaymentGroupId = new BigInteger(pPaymentGroupId);

            boolean bFullRows  = Boolean.parseBoolean(pbAllRows);

            int iPageNumber = Integer.parseInt(psPageNumber);

            summaryReport = ssReportSearchPayments.generate(gem, 
                                                            gUserId, 
                                                            pKeyword, 
                                                            lYear, 
                                                            bCleanMemoryData,
                                                            biVendorId,
                                                            biPaymentGroupId,
                                                            iPageNumber,
                                                            bFullRows);

            rsp.Content = Util.JSON.Convert2JSON(summaryReport).toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }

    }

    @GET
    @Path("/gtpymhst/{lng},"
                            + "{cnt},"
                            + "{bid},"
                            + "{sid},"
                            + "{ip},"
                            + "{aid},"
                            + "{vid},"
                            + "{yr},"
                            + "{rst},"
                            + "{rwd},"
                            + "{alr}"

         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse getPaymentHistoryOfVendor(        @PathParam("lng")         String psLang,
                                                            @PathParam("cnt")         String psCountry,
                                                            @PathParam("bid")         String psBrowserId,
                                                            @PathParam("sid")         String psUISessionId,//don't change the paramter name (sid) 
                                                            @PathParam("ip")          String psIp,
                                                            @PathParam("aid")         String pAccId,
                                                            @PathParam("vid")         String pVendorId,
                                                            @PathParam("yr")          String pYear,
                                                            @PathParam("rst")         String pForceCleanFetch,//Default: N. this set Y to skip memory read. In other words, memory will be wiped and the data will be refetched.
                                                            @PathParam("rwd")         String pLastRowTxnDate,
                                                            @PathParam("alr")         String pbAllRows
                                                       ) throws Exception
    {

        try
        {
            ssoAPIResponse rsp = new ssoAPIResponse();

            ssoUserAccounts accConnected = new ssoUserAccounts();

            BigInteger lSessionUserId = gUserId;//for now
            BigInteger lTargetAccId   = new BigInteger(pAccId);

            BigInteger lAccId    = new BigInteger(pAccId);
            BigInteger lVendorId = new BigInteger(pVendorId);
            long lYear     = Long.parseLong(pYear);
            
            accConnected = Users.connectAccount(gem, lSessionUserId, lTargetAccId, gServiceGrants);
            if(accConnected.bAccessGranted==false)
            {
                if(accConnected.bForce2Login==true)
                    rsp.AuthenticationFailed = true;

                rsp.Response = "err";
                rsp.ResponseMsg = "Account invalid";
                return rsp;
            }

            ArrayList<ssoVendorPayment> payments = new ArrayList<ssoVendorPayment>();
            boolean bCleanMemoryData = false;

            if (pForceCleanFetch.equals("true")==true)
                bCleanMemoryData = true;

            int biLastRowTxnDate = Integer.parseInt(pLastRowTxnDate);
            boolean bFullRows  = Boolean.parseBoolean(pbAllRows);

            payments = PaymentOps.getAccountPaymentHistory(gem, 
                                                            gUserId, 
                                                            lAccId, 
                                                            lVendorId, 
                                                            lYear,
                                                            bCleanMemoryData, 
                                                            biLastRowTxnDate,
                                                            bFullRows);

            rsp.Content = Util.JSON.Convert2JSON(payments).toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }

    }


}
