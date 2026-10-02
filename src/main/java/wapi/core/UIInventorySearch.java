/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wapi.core;

import Methods.Users;
import Objects.ssoUserAccounts;
import bb.app.account.ssoUIBalanceItem;
import bb.app.inv.InventoryOps;
import bb.app.obj.ssoInvBrandItemCodes;
import bb.app.obj.ssoInventoryParams;
import bb.app.obj.ssoPrintByBillData;
import bb.app.report.invsearch.ssoUIBalanceReport;
import bb.app.report.invsearch.ssoUIBalanceReportAccount;
import bb.app.report.invsearch.ssoUIBalanceReportGeneric;
import bb.reports.ssReportSearchInventory;
import entity.user.SsUsrAccounts;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;
import jaxesa.annotations.Consumes;
import jaxesa.annotations.GET;
import jaxesa.annotations.MediaType;
import jaxesa.annotations.Path;
import jaxesa.annotations.PathParam;
import jaxesa.annotations.Produces;
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
@Path("/api/inv")
public class UIInventorySearch implements jeiRestInterface
{
    // SESSION VARIABLES
    BigInteger gUserId  = BigInteger.ZERO;
    EntityManager gem;
    ArrayList<String> gServiceGrants = new ArrayList<String>();

    private static final Logger logger = Logger.getLogger(UIHome.class.getName());
    
    public UIInventorySearch()
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
    @Path("/srchinv4smry/{aid},"
                            + "{lng},"
                            + "{cnt},"
                            + "{sid},"
                            + "{ip},"
                            + "{ky},"
                            + "{yy},"
                            + "{pg}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse searchInventory4Summary(  @PathParam("aid")                          String pAccId,
                                                    @PathParam("lng")                          String psLang,
                                                    @PathParam("cnt")                          String psCountry,
                                                    @PathParam("sid")                          String psSessionId,
                                                    @PathParam("ip")                           String psIP,
                                                    @PathParam("ky")                           String psKeyword,
                                                    @PathParam("yy")                           String psFinancialYear,
                                                    @PathParam("pg")                           String psPageNumber
                                                 ) throws Exception
    {
        //----------------------------------------------------------------------
        // ATTENTION: 
        // For this Endpoint, the paging will always be on offline. In other 
        // words, the full data found to be sent to the client, then client 
        // will page thru
        //----------------------------------------------------------------------
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

            //rsp.Content = Util.JSON.Convert2JSON(aItemsFound).toString();
            //ArrayList<ssoUIBalanceReportGeneric> balances = new ArrayList<ssoUIBalanceReportGeneric>();
            ssoUIBalanceReport report = new ssoUIBalanceReport();
            boolean bCleanMemory = false;

            int iPageNumber = Integer.parseInt(psPageNumber);

            report = ssReportSearchInventory.generate4SummaryByKeyword(gem, bCleanMemory, accConnected.userId, psKeyword, psFinancialYear, iPageNumber);
            rsp.Content = Util.JSON.Convert2JSON(report).toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    @GET
    @Path("/srchinv4itm/{aid},"
                            + "{lng},"
                            + "{cnt},"
                            + "{sid},"
                            + "{ip},"
                            
                            + "{taid},"//acc Id / user Id
                            + "{tatp},"//acc type
                            + "{bid},"
                            + "{icd},"
                            + "{rst},"
                            + "{pg}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse searchInventory4Items(    @PathParam("aid")                          String pAccId,
                                                    @PathParam("lng")                          String psLang,
                                                    @PathParam("cnt")                          String psCountry,
                                                    @PathParam("sid")                          String psSessionId,
                                                    @PathParam("ip")                           String psIP,

                                                    @PathParam("taid")                         String psTargetId,
                                                    @PathParam("tatp")                         String psTargetType,// User or Account(branch)
                                                    @PathParam("bid")                          String psBrandId,
                                                    @PathParam("icd")                          String psItemCode,
                                                    @PathParam("rst")                          String psResetMemory,
                                                    @PathParam("pg")                           String psPageNumber
                                                 ) throws Exception
    {
        //----------------------------------------------------------------------
        // ATTENTION: 
        // For this Endpoint, the paging will always be on offline. In other 
        // words, the full data found to be sent to the client, then client 
        // will page thru
        //----------------------------------------------------------------------
        try
        {
            Util.Tomcat.print2TomcatLog(logger, "searchInventory4Items", pAccId);

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
            ArrayList<ssoUIBalanceItem> balances = new ArrayList<ssoUIBalanceItem>();

            BigInteger lTargetAccId2Search = new BigInteger(psTargetId);
            BigInteger lBrandId            = new BigInteger(psBrandId);

            //if(psTargetType.equals("U")==true)
            //    lTargetAccId2Search = gUserId;

            boolean bResetCache = false;
            if(psResetMemory.toUpperCase().equals("Y")==true)
                bResetCache = true;

            int iPageNumber = Integer.parseInt(psPageNumber);
            
            Util.Tomcat.print2TomcatLog(logger, "searchInventory4Items > generate4Items", pAccId);
            
            balances = ssReportSearchInventory.generate4Items(  gem, 
                                                                bResetCache,
                                                                gUserId,
                                                                lTargetAccId2Search, 
                                                                psTargetType, 
                                                                lBrandId, 
                                                                psItemCode,
                                                                iPageNumber);

            rsp.Content = Util.JSON.Convert2JSON(balances).toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            
            Util.Tomcat.print2TomcatLog(logger, "exception @ searchInventory4Items: " + e.getMessage(), pAccId);
            
            throw e;
        }
    }

    @GET
    @Path("/gtblls4brcd/{aid},"
                            + "{lng},"
                            + "{cnt},"
                            + "{sid},"
                            + "{ip},"
                            + "{ky},"
                            + "{mtc},"
                            + "{rst}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse getBills4Barcode(     @PathParam("aid")                          String pAccId,
                                                @PathParam("lng")                          String psLang,
                                                @PathParam("cnt")                          String psCountry,
                                                @PathParam("sid")                          String psSessionId,
                                                @PathParam("ip")                           String psIP,
                                                @PathParam("ky")                           String psKeyword,//for vendor name
                                                @PathParam("mtc")                          String psExactMatch,//for vendor name
                                                @PathParam("rst")                          String psReset
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

            boolean bReset = false;
            if(psReset.trim().equals("Y")==true)
                bReset = true;

            boolean bExactMatch = false;
            if(psExactMatch.trim().equals("Y")==true)
                bExactMatch = true;

            ArrayList<ssoPrintByBillData> printData = new ArrayList<ssoPrintByBillData>();
            printData = InventoryOps.getPrintDataByBill(gem, 
                                                        accConnected.userId, 
                                                        accConnected.uid, 
                                                        psKeyword,
                                                        bExactMatch,
                                                        bReset);

            rsp.Content = Util.JSON.Convert2JSON(printData).toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }
    
    
    @GET
    @Path("/gia/{aid},"//get item activity
                        + "{lng},"
                        + "{cnt},"
                        + "{sid},"
                        + "{ip},"
                        + "{bnm},"
                        + "{icd}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse getItemLatestEntryInfo(   @PathParam("aid")                          String pAccId,
                                                    @PathParam("lng")                          String psLang,
                                                    @PathParam("cnt")                          String psCountry,
                                                    @PathParam("sid")                          String psSessionId,
                                                    @PathParam("ip")                           String psIP,
                                                    @PathParam("bnm")                          String psBrand,
                                                    @PathParam("icd")                          String psItemCode
                                                 ) throws Exception
    {
        try
        {
            ssoAPIResponse rsp = new ssoAPIResponse();
            
            ssoInventoryParams params = new ssoInventoryParams();
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

            params = InventoryOps.getItemLastEntryInfo(gem, accConnected.uid, psItemCode);

            rsp.Content = Util.JSON.Convert2JSON(params).toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    @GET
    @Path("/gi4b/{aid},"
                    + "{lng},"
                    + "{cnt},"
                    + "{sid},"
                    + "{ip},"
                    + "{ky}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse getInventoryItems(    @PathParam("aid")                          String pAccId,
                                                @PathParam("lng")                          String psLang,
                                                @PathParam("cnt")                          String psCountry,
                                                @PathParam("sid")                          String psSessionId,
                                                @PathParam("ip")                           String psIP,
                                                @PathParam("ky")                           String psKeyword
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

            aItemsFound = InventoryOps.searchInventoryItems(gem, accConnected.uid, psKeyword);

            rsp.Content = Util.JSON.Convert2JSON(aItemsFound).toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    @GET
    @Path("/sios/{aid},"
                    + "{lng},"
                    + "{cnt},"
                    + "{sid},"
                    + "{ip},"
                    + "{vid},"
                    + "{ic}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse searchInOtherStores(  @PathParam("aid")                          String pAccId,
                                                @PathParam("lng")                          String psLang,
                                                @PathParam("cnt")                          String psCountry,
                                                @PathParam("sid")                          String psSessionId,
                                                @PathParam("ip")                           String psIP,
                                                @PathParam("vid")                          String psBrandId,
                                                @PathParam("ic")                           String psItemCode
                                             ) throws Exception
    {
        //----------------------------------------------------------------------
        // ATTENTION: 
        // For this Endpoint, the paging will always be on offline. In other 
        // words, the full data found to be sent to the client, then client 
        // will page thru
        //----------------------------------------------------------------------
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

            //rsp.Content = Util.JSON.Convert2JSON(aItemsFound).toString();
            //ArrayList<ssoUIBalanceReportGeneric> balances = new ArrayList<ssoUIBalanceReportGeneric>();
            ssoUIBalanceReport report = new ssoUIBalanceReport();
            boolean bCleanMemory = false;

            BigInteger biVendorId = new BigInteger(psBrandId);

            ArrayList<ssoUIBalanceReportAccount> invs = new ArrayList<ssoUIBalanceReportAccount>();
            invs = InventoryOps.searchInOtherLocations(gem, gUserId, biVendorId, psItemCode);

            rsp.Content = Util.JSON.Convert2JSON(invs).toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

}
