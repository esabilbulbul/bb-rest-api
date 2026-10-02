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
import restapi.jeiRestInterface;

/**
 *
 * @author Administrator
 */
@Path("/api/blnc")
public class UIBalances implements jeiRestInterface
{
    BigInteger gUserId  = BigInteger.valueOf(-1);
    EntityManager gem;
    ArrayList<String>    gServiceGrants = new ArrayList<String>();

    public UIBalances()
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

            boolean bCleanMemoryData = false;
            if (pResetCache.equals("true")==true)
                bCleanMemoryData = true;

            //rsp.Content = Util.JSON.Convert2JSON(aItemsFound).toString();
            ArrayList<ssoUIBalanceItem> balances = new ArrayList<ssoUIBalanceItem>(); 

            // NOTICE:  Following Method Full on Cache
            //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            balances = ssReportSearchBalances.generate(gem, accConnected.userId, psKeyword, bCleanMemoryData);
            rsp.Content = Util.JSON.Convert2JSON(balances).toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            String sErr = e.getMessage();
            if(sErr.equals(Users.ERR_USER_TRYING_TO_CONNECT_TO_DIFFERENT_ACCOUNT)==true)
            {
                
            }
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

            //rsp.Content = Util.JSON.Convert2JSON(aItemsFound).toString();
            int ThisYear = Integer.parseInt(Util.DateTime.GetDateTime_s().substring(0, 4));

            BigInteger lVendorId = new BigInteger(psBrandId);

            int lStartRowIndex = Integer.parseInt(pRowIndex);
            boolean bFullRows  = Boolean.parseBoolean(pbAllRows);

            ArrayList<ssoAccStmtCore> brandStmt = new ArrayList<ssoAccStmtCore>();
            brandStmt = AccountMisc.getStatement4Brand( gem, 
                                                        false,
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

            BigInteger lVendorId = new BigInteger(psBrandId);

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
        
