/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wapi.core;

import Methods.Users;
import Objects.ssoUserAccounts;
import bb.app.bill.InventoryUpdate;
import bb.app.obj.ssoInvBrandItemCodes;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mysql.cj.MysqlType;
import entity.user.SsUsrAccounts;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
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
public class UIInventoryUpdate implements jeiRestInterface
{
    // SESSION VARIABLES
    BigInteger gUserId  = BigInteger.ZERO;
    EntityManager gem;
    ArrayList<String>    gServiceGrants = new ArrayList<String>();

    private static final Logger logger = Logger.getLogger(UIHome.class.getName());
    
    public UIInventoryUpdate()
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
    @Path("/upblnwln/{aid},"
                        + "{lng},"
                        + "{cnt},"
                        + "{sid},"
                        + "{ip},"
                        + "{txn},"
                        + "{bnm},"
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
    //@Callback(type = ThreadActionType.AFTER, source = "updateBill_AddNewLine", targetClass="apicallbacks.cbInventoryBill_AddNewLine", targetMethod = "addNewLine_Inventory_Transaction_Callback")
    public ssoAPIResponse updateBill_AddNewLine(   @PathParam("aid")                          String pAccId,
                                                   @PathParam("lng")                          String psLang,
                                                   @PathParam("cnt")                          String psCountry,
                                                   @PathParam("sid")                          String psSessionId,
                                                   @PathParam("ip")                           String psIP,
                                                   @PathParam("txn")                          String psBillId,
                                                   @PathParam("chg")                          String psUpdates
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

            // NOT SUPPORTED YET
            //---------------------------------------------
            // 1. Insert new row into ss_txn_inv_bill
            // 2. Pass it to the callback 

            //String sQuantityEntered  = Util.JSON.getValue(jsOptionsEntered,  sKey);

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }
    
    @GET
    @Path("/uiqi/{aid},"
                        + "{lng},"
                        + "{cnt},"
                        + "{sid},"
                        + "{ip},"
                        + "{vl}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse updateInventory_ItemCode( @PathParam("aid")                          String pAccId,
                                                    @PathParam("lng")                          String psLang,
                                                    @PathParam("cnt")                          String psCountry,
                                                    @PathParam("sid")                          String psSessionId,
                                                    @PathParam("ip")                           String psIP,
                                                    @PathParam("vl")                           String psQuantityChanges
                                                 ) throws Exception
    {
        //----------------------------------------------------------------------
        // IMPORTANT: This endpoint only updates options. The ItemCode quantities
        //            will not be affected.
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

            boolean rc = Util.HTTP.isParamSafe(psQuantityChanges);
            if (rc==false)
                throw new Exception("Parameter unsafe");

            if (psQuantityChanges.trim().length()!=0)
            {
                InventoryUpdate.updateInventory4Account_ItemCodes(gem, accConnected.uid, psQuantityChanges);

                rsp.Response = "ok";
            }
            else
                rsp.Response = "nok";//empty record

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    @GET
    @Path("/uiqo/{aid},"
                        + "{lng},"
                        + "{cnt},"
                        + "{sid},"
                        + "{ip},"
                        + "{vl}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse updateInventory_Options(  @PathParam("aid")                          String pAccId,
                                                    @PathParam("lng")                          String psLang,
                                                    @PathParam("cnt")                          String psCountry,
                                                    @PathParam("sid")                          String psSessionId,
                                                    @PathParam("ip")                           String psIP,
                                                    @PathParam("vl")                           String psQuantityChanges
                                                 ) throws Exception
    {
        //----------------------------------------------------------------------
        // IMPORTANT: This endpoint only updates options. The ItemCode quantities
        //            will not be affected.
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

            boolean rc = Util.HTTP.isParamSafe(psQuantityChanges);
            if (rc==false)
                throw new Exception("Parameter unsafe");

            if (psQuantityChanges.trim().length()!=0)
            {
                // IMPORTANT:
                // If option quantity changed, update option stats only
                // If itemcode quantity changed, update item stats and vendor stats
                InventoryUpdate.updateInventory4Account_Options(gem, accConnected.uid, psQuantityChanges);

                rsp.Response = "ok";
            }
            else
                rsp.Response = "nok";//empty record

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    // "[{"group":"BLACK","option":"B","quantity":-1}]" 
    @GET
    @Path("/ano/{aid},"
                    + "{lng},"
                    + "{cnt},"
                    + "{sid},"
                    + "{ip},"
                    + "{vid},"
                    + "{ic},"
                    + "{opt}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse addNewOption( @PathParam("aid")                          String pAccId,
                                        @PathParam("lng")                          String psLang,
                                        @PathParam("cnt")                          String psCountry,
                                        @PathParam("sid")                          String psSessionId,
                                        @PathParam("ip")                           String psIP,
                                        @PathParam("vid")                          String psBrandId,//vendorId
                                        @PathParam("ic")                           String psItemCode,
                                        @PathParam("opt")                          String psOptions2Add //  "[{"group":"BLACK","option":"B","quantity":-1}]" 
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

            Util.Tomcat.print2TomcatLog(logger, "/ao2b/ starting at sid: " + psSessionId + " for brandId: " + psBrandId + " and param: " + psOptions2Add, pAccId);

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

            InventoryUpdate.addNewOptions4ItemCode(gem, accConnected.uid , lVendorId, psItemCode, psOptions2Add);
            
            rsp.Content = Util.JSON.Convert2JSON(lVendorId).toString();
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

