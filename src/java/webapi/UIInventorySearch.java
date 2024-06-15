/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webapi;

import Methods.Users;
import bb.app.account.ssoUIBalanceItem;
import bb.app.inv.InventoryOps;
import bb.app.obj.ssoInvBrandItemCodes;
import bb.app.obj.ssoInventoryParams;
import bb.app.obj.ssoPrintByBillData;
import bb.reports.ssReportSearchInventory;
import entity.user.SsUsrAccounts;
import java.util.ArrayList;
import java.util.Arrays;
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
@Path("/api/biz/bb/inv")
public class UIInventorySearch implements jeiRestInterface
{
    // SESSION VARIABLES
    long gUserId  = -1;
    EntityManager gem;
    ArrayList<String> gServiceGrants = new ArrayList<String>();

    public UIInventorySearch()
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
    @Path("/srchinv4smry/{aid},"
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
    public ssoAPIResponse searchInventory4Summary(  @PathParam("aid")                          String pAccId,
                                                    @PathParam("lng")                          String psLang,
                                                    @PathParam("cnt")                          String psCountry,
                                                    @PathParam("sid")                          String psSessionId,
                                                    @PathParam("ip")                           String psIP,
                                                    @PathParam("ky")                           String psKeyword
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
            ArrayList<ssoUIBalanceItem> balances = new ArrayList<ssoUIBalanceItem>();

            balances = ssReportSearchInventory.generate4SummaryByKeyword(gem, accConnected.userId, psKeyword);
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
    @Path("/srchinv4itm/{aid},"
                            + "{lng},"
                            + "{cnt},"
                            + "{sid},"
                            + "{ip},"
                            
                            + "{taid},"//acc Id / user Id
                            + "{tatp},"//acc type
                            + "{bid},"
                            + "{icd}"
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
                                                    @PathParam("icd")                          String psItemCode
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
            ArrayList<ssoUIBalanceItem> balances = new ArrayList<ssoUIBalanceItem>();

            long lTargetAccId2Search = Long.parseLong(psTargetId);
            long lBrandId            = Long.parseLong(psBrandId);

            balances = ssReportSearchInventory.generate4Items(  gem, 
                                                                accConnected.uid, 
                                                                lTargetAccId2Search, 
                                                                psTargetType, 
                                                                lBrandId, 
                                                                psItemCode);

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



}
