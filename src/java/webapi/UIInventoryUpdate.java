/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webapi;

import Methods.Users;
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
public class UIInventoryUpdate implements jeiRestInterface
{
    // SESSION VARIABLES
    long gUserId  = -1;
    EntityManager gem;
    ArrayList<String>    gServiceGrants = new ArrayList<String>();

    public UIInventoryUpdate()
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
    @Path("/updinvqnty/{aid},"
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
    public ssoAPIResponse updateInventory(  @PathParam("aid")                          String pAccId,
                                            @PathParam("lng")                          String psLang,
                                            @PathParam("cnt")                          String psCountry,
                                            @PathParam("sid")                          String psSessionId,
                                            @PathParam("ip")                           String psIP,
                                            @PathParam("vl")                           String psQuantityChanges
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

            boolean rc = Util.HTTP.isParamSafe(psQuantityChanges);
            if (rc==false)
                throw new Exception("Parameter unsafe");

            if (psQuantityChanges.trim().length()!=0)
            {
                InventoryUpdate.updateInventory4Account(gem, accConnected.uid, psQuantityChanges);

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

}

