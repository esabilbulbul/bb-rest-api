/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webapi;

import Methods.Users;
import bb.app.inv.InventoryOps;
import bb.app.obj.ssoInventoryParams;
import bb.app.stmt.InventoryStatement;
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
@Path("/api/biz/bb/inv")
public class UIInventoryEntry implements jeiRestInterface   
{
    // SESSION VARIABLES
    long gUserId  = -1;
    EntityManager gem;
    ArrayList<String> gServiceGrants = new ArrayList<String>();

    public UIInventoryEntry()
    {
        
    }

    @Override
    public void init(String pUserId, EntityManager pem,String psUserRoleReqs)
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
    @Path("/nwinvtxn/{aid},"
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
    @Callback(type = ThreadActionType.AFTER, source = "newInventoryTransaction", targetClass="apicallbacks.cbInventoryBill_FirstEntry", targetMethod = "newInventoryTransaction_Callback")
    public ssoAPIResponse newInventoryTransaction(  @PathParam("aid")                          String pAccId,
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

            ssoAPIResponse rsp = new ssoAPIResponse();
            SsUsrAccounts accConnected = new SsUsrAccounts();

            long lSessionUserId = gUserId;//for now
            long lTargetAccId   = Long.parseLong(pAccId);

            accConnected = Users.connectAccount(gem, gUserId, lTargetAccId, gServiceGrants);
            if(accConnected==null)
            {
                rsp.Response = "err";
                rsp.ResponseMsg = "Account invalid";
                return rsp;
            }

            //String sTxnType = InventoryStatement.getTransactionType(psTxnType);
            String sTxnType = psTxnType;

            long InvStmtId = 0;
            InvStmtId = InventoryStatement.processStatement(gem, 
                                                            accConnected.userId, 
                                                            accConnected.uid, 
                                                            psLang, 
                                                            psCountry, 
                                                            psSessionId, 
                                                            sTxnType,
                                                            pjsStmtDets);
            /*
            InvStmtId = InventoryOps.saveInventoryStatementDets( gem, 
                                                                gUserId, 
                                                                lAccId, 
                                                                psLang, 
                                                                psCountry, 
                                                                psSessionId,
                                                                psTxnType,
                                                                psBrand.toUpperCase().trim(),
                                                                psItemCode.toUpperCase().trim(),
                                                                psCategory.toUpperCase().trim(),
                                                                psQuantity,
                                                                psQuantityType,
                                                                psEntryPrice,
                                                                psDiscount,
                                                                psTax,
                                                                psSalesPrice,
                                                                psOptions,
                                                                psBillingDate,
                                                                psDesc);
            */

            if (InvStmtId==-1)
            {
                // Transaction has been inserted in previous txn
                rsp.Response = "entry-exist";
                rsp.ResponseMsg = "Transaction entry is already exist";
            }
            else
            {
                rsp.Response = "ok";
                rsp.callbackId = Long.toString(InvStmtId);
            }

            //rsp.Response = "entry-exist";//this is for test (remember to remove)

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }



}
