/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webapi;

import Methods.Users;
import bb.app.account.AccountMisc;
import bb.app.account.ssoAccStmtCore;
import bb.app.account.ssoVendorProfile;
import bb.app.dict.DictionaryOps;
import bb.app.obj.ssoInvBrandItemCodes;
import bb.app.obj.ssoVendorItemStats;
import bb.app.obj.ssoVendorStats;
import bb.app.payment.PaymentOps;
import bb.app.vendor.VendorOps;
import bb.reports.ssReportSearchInventory;
import entity.acc.SsAccInvItemStats;
import entity.acc.SsAccInvVendorStats;
import entity.user.SsUsrAccounts;
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
@Path("/api/biz/bb/spplr")
public class UISupplier implements jeiRestInterface
{
    // SESSION VARIABLES
    long gUserId  = -1;
    EntityManager gem;
    ArrayList<String> gServiceGrants = new ArrayList<String>();

    public UISupplier()
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

            SsUsrAccounts accConnected = new SsUsrAccounts();

            long lSessionUserId = gUserId;//for now
            long lTargetAccId   = Long.parseLong(pAccId);

            accConnected = Users.connectAccount(gem, lSessionUserId, lTargetAccId, gServiceGrants);
            if(accConnected==null)
            {
                rsp.Response = "err";
                rsp.ResponseMsg = "Account invalid";
                return rsp;
            }

            //int ThisYear = Integer.parseInt(Util.DateTime.GetDateTime_s().substring(0, 4));
            int iStmtYear  = Integer.parseInt(psStatementYear);
            long lVendorId = Long.parseLong(psVendorId);

            int lStartRowIndex = 0;//Integer.parseInt(pRowIndex);
            boolean bFullRows  = true;//Boolean.parseBoolean(pbAllRows);
            boolean bCleanMemoryData = false;

            ssoVendorProfile vendorPage = new ssoVendorProfile();

            vendorPage.inventory = ssReportSearchInventory.generate4SummaryByBrand( gem, 
                                                                                    accConnected.userId, 
                                                                                    lVendorId);

            vendorPage.Info = AccountMisc.getVendorProfile( gem, 
                                                            accConnected.userId, 
                                                            accConnected.uid, 
                                                            lVendorId);

            vendorPage.Stmt = AccountMisc.getStatement4Brand(gem,
                                                             accConnected.userId,
                                                             accConnected.uid,
                                                             lVendorId,
                                                             iStmtYear,
                                                             lStartRowIndex,
                                                             bFullRows);//-1 WILL BE FIXED

            vendorPage.payments = PaymentOps.getAccountPaymentHistory(  gem,
                                                                        accConnected.userId,
                                                                        accConnected.uid,
                                                                        lVendorId, 
                                                                        iStmtYear,
                                                                        bCleanMemoryData, 
                                                                        lStartRowIndex,
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

            SsUsrAccounts accConnected = new SsUsrAccounts();

            long lSessionUserId = gUserId;//for now
            long lTargetAccId   = Long.parseLong(pAccId);

            accConnected = Users.connectAccount(gem, lSessionUserId, lTargetAccId, gServiceGrants);
            if(accConnected==null)
            {
                rsp.Response = "err";
                rsp.ResponseMsg = "Account invalid";
                return rsp;
            }

            //int ThisYear = Integer.parseInt(Util.DateTime.GetDateTime_s().substring(0, 4));
            int iStmtYear  = Integer.parseInt(psStatementYear);
            long lVendorId = Long.parseLong(psVendorId);

            int lStartRowIndex = 0;//Integer.parseInt(pRowIndex);
            boolean bFullRows  = true;//Boolean.parseBoolean(pbAllRows);
            boolean bCleanMemoryData = false;

            if (pbCleanMemory.equals("Y")==true)
                bCleanMemoryData = true;

            ssoVendorProfile vendorPage = new ssoVendorProfile();
            //refresh no need to fetch for info only stmt, inv and payments
            vendorPage.Stmt      = AccountMisc.getStatement4Brand(  gem, 
                                                                    accConnected.userId, 
                                                                    accConnected.uid, 
                                                                    lVendorId, 
                                                                    iStmtYear, 
                                                                    lStartRowIndex, 
                                                                    bFullRows);//-1 WILL BE FIXED

            vendorPage.inventory = ssReportSearchInventory.generate4SummaryByBrand( gem, 
                                                                                    accConnected.userId, 
                                                                                    lVendorId);

            vendorPage.payments  = PaymentOps.getAccountPaymentHistory( gem, 
                                                                        accConnected.userId, 
                                                                        accConnected.uid, 
                                                                        lVendorId, 
                                                                        iStmtYear,
                                                                        bCleanMemoryData, 
                                                                        lStartRowIndex,
                                                                        bFullRows);

            rsp.Content = Util.JSON.Convert2JSON(vendorPage).toString();
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
            ArrayList<ssoInvBrandItemCodes> aItemsFound = new ArrayList<ssoInvBrandItemCodes>();

            ssoAPIResponse rsp = new ssoAPIResponse();
            SsUsrAccounts accConnected = new SsUsrAccounts();

            long lSessionUserId = gUserId;//for now
            long lTargetAccId   = Long.parseLong(pAccId);

            accConnected = Users.connectAccount(gem, lSessionUserId, lTargetAccId, gServiceGrants);
            if(accConnected==null)
            {
                rsp.Response = "err";
                rsp.ResponseMsg = "Account invalid";
                return rsp;
            }

            //rsp.Content = Util.JSON.Convert2JSON(aItemsFound).toString();
            //int ThisYear = Integer.parseInt(Util.DateTime.GetDateTime_s().substring(0, 4));
            String sBrand = psBrand.toLowerCase();

            boolean rc = DictionaryOps.Vendor.Check(gem, accConnected.userId, psBrand);
            if (rc==false)
            {
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
            SsUsrAccounts accConnected = new SsUsrAccounts();

            long lSessionUserId = gUserId;//for now
            long lTargetAccId   = Long.parseLong(pAccId);

            accConnected = Users.connectAccount(gem, lSessionUserId, lTargetAccId, gServiceGrants);
            if(accConnected==null)
            {
                rsp.Response = "err";
                rsp.ResponseMsg = "Account invalid";
                return rsp;
            }

            boolean rc = DictionaryOps.Vendor.Check(gem, accConnected.userId, psBrand);
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
