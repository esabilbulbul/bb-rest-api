/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */ 
package wapi.core;

import Methods.Users;
import Objects.ssoUserAccounts;
import bb.app.account.AccountMisc;
import bb.app.account.ssoAccStmtCore;
import bb.app.account.ssoVendorProfile;
import bb.app.account.ssoVendorSalesSummary;
import bb.app.dict.DictionaryOps;
import bb.app.obj.ssoInvBrandItemCodes;
import bb.app.obj.ssoVendorItemStats;
import bb.app.obj.ssoVendorStats;
import bb.app.payment.PaymentOps;
import bb.app.vendor.VendorOps;
import bb.reports.ssReportSearchInventory;
import entity.acc.SsAccInvItemStats;
import entity.acc.SsAccInvVendorStats;
import entity.dct.SsDctInvVendorSummary;
import entity.stmt.SsStmInvStatements;
import entity.user.SsUsrAccounts;
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
import jaxesa.persistence.ssoCacheSplitKey;
import jaxesa.util.Util;
import jaxesa.webapi.ssoAPIResponse;
import restapi.jeiRestInterface;

/**
 *
 * @author Administrator
 */
@Path("/api/spplr")
public class UISupplier implements jeiRestInterface
{
    // SESSION VARIABLES
    BigInteger gUserId  = BigInteger.ZERO;
    EntityManager gem;
    ArrayList<String> gServiceGrants = new ArrayList<String>();

    private static final Logger logger = Logger.getLogger(UIHome.class.getName());
    
    public UISupplier()
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
    @Path("/gvp/{aid},"
                + "{lng},"
                + "{cnt},"
                + "{sid},"
                + "{ip},"
                + "{vid},"
                + "{syr}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse getVendorProfile( @PathParam("aid")                          String pAccId,
                                            @PathParam("lng")                          String psLang,
                                            @PathParam("cnt")                          String psCountry,
                                            @PathParam("sid")                          String psSessionId,
                                            @PathParam("ip")                           String psIP,
                                            @PathParam("vid")                          String psVendorId,
                                            @PathParam("syr")                          String psStatementYear
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

            //int ThisYear = Integer.parseInt(Util.DateTime.GetDateTime_s().substring(0, 4));
            int iStmtYear  = Integer.parseInt(psStatementYear);
            BigInteger lVendorId = new BigInteger(psVendorId);

            //int lStartRowIndex = 0;//Integer.parseInt(pRowIndex);
            boolean bFullRows  = true;//Boolean.parseBoolean(pbAllRows);
            boolean bCleanMemoryData = false;

            ssoVendorProfile vendorPage = new ssoVendorProfile();

            vendorPage.inventory = ssReportSearchInventory.generate4SummaryByBrand( gem, 
                                                                                    false,
                                                                                    accConnected.userId, 
                                                                                    lVendorId,
                                                                                    iStmtYear);

            vendorPage.Info = AccountMisc.getVendorProfile( gem, 
                                                            false,
                                                            accConnected.userId, 
                                                            accConnected.uid, 
                                                            lVendorId,
                                                            iStmtYear);

            vendorPage.Stmt = AccountMisc.getStatement4Brand(gem,
                                                             bCleanMemoryData,
                                                             accConnected.userId,
                                                             accConnected.uid,
                                                             lVendorId,
                                                             iStmtYear,
                                                             0,//lStartRowIndex,
                                                             bFullRows);//-1 WILL BE FIXED

            vendorPage.payments = PaymentOps.getAccountPaymentHistory(  gem,
                                                                        accConnected.userId,
                                                                        accConnected.uid,
                                                                        lVendorId, 
                                                                        iStmtYear,
                                                                        bCleanMemoryData, 
                                                                        0,//BigInteger.ZERO,//last date,
                                                                        bFullRows);

            vendorPage.branches = DictionaryOps.User.getListOfAccounts4User(gem, accConnected.userId, false);

            rsp.Content = Util.JSON.Convert2JSON(vendorPage).toString();
            rsp.Response = "ok";

            //rsp.Content = Util.JSON.Convert2JSON(MainParams).toString();
            //rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }//getvendorprofile

    @GET
    @Path("/rfsh/{aid},"
                + "{lng},"
                + "{cnt},"
                + "{sid},"
                + "{ip},"
                + "{vid},"
                + "{syr},"
                + "{rst}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse refreshVendorProfile( @PathParam("aid")                          String pAccId,
                                                @PathParam("lng")                          String psLang,
                                                @PathParam("cnt")                          String psCountry,
                                                @PathParam("sid")                          String psSessionId,
                                                @PathParam("ip")                           String psIP,
                                                @PathParam("vid")                          String psVendorId,
                                                @PathParam("syr")                          String psStatementYear,
                                                @PathParam("rst")                          String pbCleanMemory
                                             ) throws Exception
    {
        try
        {
            ssoAPIResponse rsp = new ssoAPIResponse();

            ssoUserAccounts accConnected = new ssoUserAccounts();
            
            Util.Tomcat.print2TomcatLog(logger, "api/rfrsh > refreshVendorProfile > starting", pAccId);
            
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

            //int ThisYear = Integer.parseInt(Util.DateTime.GetDateTime_s().substring(0, 4));
            int iStmtYear  = Integer.parseInt(psStatementYear);
            BigInteger lVendorId = new BigInteger(psVendorId);

            //BigInteger lStartRowIndex = BigInteger.ZERO;//Integer.parseInt(pRowIndex);
            boolean bFullRows  = true;//Boolean.parseBoolean(pbAllRows);
            boolean bCleanMemoryData = false;

            if (pbCleanMemory.equals("Y")==true)
                bCleanMemoryData = true;

            ssoVendorProfile vendorPage = new ssoVendorProfile();

            // IMPORTANT
            // to do THIS RECALC LOGIC WILL BE MOVED TO CALLBACK !!!
            //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            AccountMisc.recalculateVendorBalance(gem, gUserId, accConnected.uid, lVendorId);// to do THIS RECALC LOGIC WILL BE MOVED TO CALLBACK
            
            Util.Tomcat.print2TomcatLog(logger, "api/rfrsh > refreshVendorProfile > getVendorProfile", pAccId);

            //refresh no need to fetch for info only stmt, inv and payments
            vendorPage.Info = AccountMisc.getVendorProfile( gem, 
                                                            bCleanMemoryData,
                                                            accConnected.userId, 
                                                            accConnected.uid, 
                                                            lVendorId,
                                                            iStmtYear);

            Util.Tomcat.print2TomcatLog(logger, "api/rfrsh > refreshVendorProfile > getStatement4Brand", pAccId);
            
            vendorPage.Stmt      = AccountMisc.getStatement4Brand(  gem,
                                                                    bCleanMemoryData,
                                                                    accConnected.userId, 
                                                                    accConnected.uid, 
                                                                    lVendorId, 
                                                                    iStmtYear, 
                                                                    0,//lStartRowIndex,
                                                                    bFullRows);//-1 WILL BE FIXED

            Util.Tomcat.print2TomcatLog(logger, "api/rfrsh > refreshVendorProfile > generate4SummaryByBrand", pAccId);
            
            vendorPage.inventory = ssReportSearchInventory.generate4SummaryByBrand( gem, 
                                                                                    bCleanMemoryData,
                                                                                    accConnected.userId, 
                                                                                    lVendorId,
                                                                                    iStmtYear);

            vendorPage.payments  = PaymentOps.getAccountPaymentHistory( gem, 
                                                                        accConnected.userId, 
                                                                        accConnected.uid, 
                                                                        lVendorId, 
                                                                        iStmtYear,
                                                                        bCleanMemoryData,
                                                                        0,//BigInteger.ZERO,//last date 
                                                                        bFullRows);

            rsp.Content = Util.JSON.Convert2JSON(vendorPage).toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            Util.Tomcat.print2TomcatLog(logger, "Exception @ api/rfrsh > refreshVendorProfile: " + e.getMessage(), pAccId);
            throw e;
        }
    }

    @GET
    @Path("/giss/{aid},"
                + "{lng},"
                + "{cnt},"
                + "{sid},"
                + "{ip},"
                + "{vid},"
                + "{ic},"
                + "{yr},"
                //+ "{ltd}"
                + "{pg}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse getItemSalesSummary(    @PathParam("aid")                          String pAccId,
                                                    @PathParam("lng")                          String psLang,
                                                    @PathParam("cnt")                          String psCountry,
                                                    @PathParam("sid")                          String psSessionId,
                                                    @PathParam("ip")                           String psIP,
                                                    @PathParam("vid")                          String psVendorId,
                                                    @PathParam("ic")                           String psItemCode,
                                                    @PathParam("yr")                           String psYear,
                                                    //@PathParam("ltd")                        String psLastTxnDate
                                                    @PathParam("pg")                           String psPageNumber
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

            int iYear  = Integer.parseInt(psYear);
            BigInteger lVendorId = new BigInteger(psVendorId);
            //long lLastTxnDate = Long.parseLong(psLastTxnDate);
            int iPageNumber = Integer.parseInt(psPageNumber);

            ArrayList<ssoVendorSalesSummary> summary = new ArrayList<ssoVendorSalesSummary>();

            summary = AccountMisc.getVendorSalesSummaryByDate(  gem, 
                                                                accConnected.userId, 
                                                                accConnected.uid, 
                                                                lVendorId,
                                                                psItemCode,
                                                                iYear,
                                                                iPageNumber);

            rsp.Content = Util.JSON.Convert2JSON(summary).toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    @GET
    @Path("/gvss/{aid},"
                + "{lng},"
                + "{cnt},"
                + "{sid},"
                + "{ip},"
                + "{vid},"
                + "{yr},"
                //+ "{ltd},"
                + "{pg}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse getVendorSalesSummary(    @PathParam("aid")                          String pAccId,
                                                    @PathParam("lng")                          String psLang,
                                                    @PathParam("cnt")                          String psCountry,
                                                    @PathParam("sid")                          String psSessionId,
                                                    @PathParam("ip")                           String psIP,
                                                    @PathParam("vid")                          String psVendorId,
                                                    @PathParam("yr")                           String psYear,
                                                    //@PathParam("ltd")                          String psLastTxnDate,
                                                    @PathParam("pg")                           String psPageNumber
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

            int iYear  = Integer.parseInt(psYear);
            BigInteger lVendorId = new BigInteger(psVendorId);
            //long lLastTxnDate = Long.parseLong(psLastTxnDate);
            int iPageNumber = Integer.parseInt(psPageNumber);

            ArrayList<ssoVendorSalesSummary> summary = new ArrayList<ssoVendorSalesSummary>();

            summary = AccountMisc.getVendorSalesSummaryByDate(  gem, 
                                                                accConnected.userId, 
                                                                accConnected.uid, 
                                                                lVendorId,
                                                                "",
                                                                iYear,
                                                                iPageNumber);

            rsp.Content = Util.JSON.Convert2JSON(summary).toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    @GET
    @Path("/nbrnd/{aid},"
                    + "{lng},"
                    + "{cnt},"
                    + "{sid},"
                    + "{ip},"
                    + "{brnd},"
                    + "{cnm},"
                    + "{pcc},"
                    + "{phn},"
                    + "{tid},"
                    + "{eml},"
                    + "{cty},"
                    + "{adr},"
                    + "{nts}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse saveBrand(@PathParam("aid")                          String pAccId,
                                    @PathParam("lng")                          String psLang,
                                    @PathParam("cnt")                          String psCountry,
                                    @PathParam("sid")                          String psSessionId,
                                    @PathParam("ip")                           String psIP,
                                    @PathParam("brnd")                         String psBrand,
                                    @PathParam("cnm")                          String psContactName,
                                    @PathParam("pcc")                          String psPhoneCountryCode,
                                    @PathParam("phn")                          String psPhoneNumber,
                                    @PathParam("tid")                          String psTaxOrNationalId,
                                    @PathParam("eml")                          String psEmail,
                                    @PathParam("cty")                          String psCity,
                                    @PathParam("adr")                          String psAddress,
                                    @PathParam("nts")                          String psNotes
                                 ) throws Exception
    {
        try
        {
            Util.Tomcat.print2TomcatLog(logger, "api/nwbrnd > saveBrand > starting with accId =" + pAccId, pAccId);
            
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

            //rsp.Content = Util.JSON.Convert2JSON(aItemsFound).toString();
            //int ThisYear = Integer.parseInt(Util.DateTime.GetDateTime_s().substring(0, 4));
            String sBrand = psBrand.toLowerCase();

            boolean rc = DictionaryOps.Vendor.Check(gem, accConnected.userId, lTargetAccId, psBrand);
            if (rc==false)
            {
                Util.Tomcat.print2TomcatLog(logger, "api/nwbrnd > saveBrand > brand not found", pAccId);
                
                // REGISTER NEW BRAND  
                //--------------------------------------------------------------
                AccountMisc.registerNewBrand(   gem, 
                                                accConnected.userId, 
                                                accConnected.uid, 
                                                sBrand,
                                                psContactName,
                                                psPhoneCountryCode,
                                                psPhoneNumber.replace("_", ""),
                                                psTaxOrNationalId,
                                                psEmail,
                                                psCity,
                                                psAddress,
                                                psNotes);
                //rsp.Content = Util.JSON.Convert2JSON(brandStmt).toString();
            }
            else
            {
                Util.Tomcat.print2TomcatLog(logger, "api/nwbrnd > saveBrand > updating brand ", pAccId);
                
                // UPDATE BRAND INFO 
                //--------------------------------------------------------------
                AccountMisc.updateBrandInfo(gem, 
                                            accConnected.userId, 
                                            accConnected.uid, 
                                            sBrand, 
                                            psContactName, 
                                            psPhoneCountryCode, 
                                            psPhoneNumber, 
                                            psTaxOrNationalId, 
                                            psEmail, 
                                            psCity, 
                                            psAddress, 
                                            psNotes);
            }

            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    @GET
    @Path("/rsvn/{aid},"
                        + "{lng},"
                        + "{cnt},"
                        + "{sid},"
                        + "{ip},"
                        + "{vid}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse resetVendor( @PathParam("aid")                          String pAccId,
                                       @PathParam("lng")                          String psLang,
                                       @PathParam("cnt")                          String psCountry,
                                       @PathParam("sid")                          String psSessionId,
                                       @PathParam("ip")                           String psIP,
                                       @PathParam("vid")                          String psVendorId
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

            BigInteger bdVendorId = new BigInteger(psVendorId);

            AccountMisc.resetBrand(gem, gUserId, accConnected.uid, bdVendorId);
            
            // clean cache for stmt
            //------------------------------------------------------------------
            ArrayList<ssoCacheSplitKey> keys2 = new ArrayList<ssoCacheSplitKey>();
            ssoCacheSplitKey ColN1 = new ssoCacheSplitKey();
            ColN1.column = "ACCOUNT_ID";
            ColN1.value  = accConnected.uid;
            keys2.add(ColN1);

            ssoCacheSplitKey ColN2 = new ssoCacheSplitKey();
            ColN2.column = "VENDOR_ID";
            ColN2.value  = bdVendorId;
            keys2.add(ColN2);

            gem.flush(SsStmInvStatements.class, keys2);

            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }

    }

    @GET
    @Path("/cbrnd/{aid},"
                        + "{lng},"
                        + "{cnt},"
                        + "{sid},"
                        + "{ip},"
                        + "{brnd}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse checkBrand( @PathParam("aid")                          String pAccId,
                                      @PathParam("lng")                          String psLang,
                                      @PathParam("cnt")                          String psCountry,
                                      @PathParam("sid")                          String psSessionId,
                                      @PathParam("ip")                           String psIP,
                                      @PathParam("brnd")                         String psBrand
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

            boolean rc = DictionaryOps.Vendor.Check(gem, accConnected.userId, lTargetAccId, psBrand);
            if (rc==true)
                rsp.ResponseMsg = "exist";
            else
                rsp.ResponseMsg = "not-exist";

            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }//CHECK BRAND


}
