/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package wapi.estore;

import Methods.Users;
import Objects.ssoUserAccounts;
import bb.app.obj.ssoInvBrandItemCodes;
import bb.app.report.invsearch.ssoUIBalanceReport;
import bb.estore.ssEStoreOps;
import bb.estore.ssoEItemRow;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
import jaxesa.util.Util;
import jaxesa.webapi.ssoAPIResponse;
import restapi.jeiRestInterface;
import wapi.core.UIHome;

/**
 *
 * @author Administrator
 */
@Path("/api/est")
public class UIEStore implements jeiRestInterface
{
    // SESSION VARIABLES
    BigInteger gUserId  = BigInteger.ZERO;
    EntityManager gem;
    ArrayList<String> gServiceGrants = new ArrayList<String>();

    private static final Logger logger = Logger.getLogger(UIHome.class.getName());

    public UIEStore()
    {
        
    }

    @Override
    public void init(String pUserId, String pBrowserId, String p3rdPartyAPIKey, EntityManager pem, String psUserRoleReqs)
    {
        try
        {
            String s = "";

            gUserId = new BigInteger(pUserId);
            gem     = pem;
            gem.SetSessionUser(pUserId);

            gServiceGrants.addAll(Arrays.asList(psUserRoleReqs.split(",")));
        }
        catch(Exception e)
        {
            
        }
    }

    @GET
    @Path("/sitm/{aid},"
                + "{lng},"
                + "{cnt},"
                + "{sid},"
                + "{ip},"
                + "{ky},"
                + "{pg},"
                + "{rst}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse searchItems( @PathParam("aid")                          String pAccId,
                                       @PathParam("lng")                          String psLang,
                                       @PathParam("cnt")                          String psCountry,
                                       @PathParam("sid")                          String psSessionId,
                                       @PathParam("ip")                           String psIP,
                                       
                                       @PathParam("ky")                           String psKeyword,
                                       @PathParam("pg")                           String psPageNumber,
                                       @PathParam("rst")                          String psReset
                                     ) throws Exception
    {
        try
        {
            ArrayList<ssoInvBrandItemCodes> aItemsFound = new ArrayList<ssoInvBrandItemCodes>();

            Util.Tomcat.print2TomcatLog(logger, "estore.searchItems", pAccId);
            
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
            int iPageNumber = Integer.parseInt(psPageNumber);

            ArrayList<ssoEItemRow> aRows       = new ArrayList<ssoEItemRow>();
            
            boolean bReset = false;
            if(psReset.toLowerCase().trim().equals("y")==true)
                bReset = true;

            aRows = ssEStoreOps.searchByKeyword(gem, gUserId, accConnected.uid, psKeyword, iPageNumber, bReset);

            rsp.Content = Util.JSON.Convert2JSON(aRows).toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            Util.Tomcat.print2TomcatLog(logger, "estore.searchItems exception: " + e.getMessage(), pAccId);
            throw e;
        }
    }

    @GET
    @Path("/uis/{aid},"
                + "{lng},"
                + "{cnt},"
                + "{sid},"
                + "{ip},"
                + "{cc}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    @Callback(type = ThreadActionType.AFTER, source = "updateItemSettings", targetClass="apicallbacks.cbEStoreItemsUpdate", targetMethod = "updateEStoreItems_Transaction_Callback")
    public ssoAPIResponse updateItemSettings(   @PathParam("aid")                          String pAccId,
                                                @PathParam("lng")                          String psLang,
                                                @PathParam("cnt")                          String psCountry,
                                                @PathParam("sid")                          String psSessionId,
                                                @PathParam("ip")                           String psIP,
                                                @PathParam("cc")                           String psItemChanges//[{Id:, code:, vndId:, active:, name:, ctgId: htId:, ftId:, imgs:[], defImg:, vids:[]}]
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

            //rsp.Content = Util.JSON.Convert2JSON(aItemsFound).toString();
            //ArrayList<ssoUIBalanceReportGeneric> balances = new ArrayList<ssoUIBalanceReportGeneric>();
            //int iPageNumber = Integer.parseInt(psPageNumber);

            ArrayList<bb.estore.ssoItem> aChanges = new ArrayList<bb.estore.ssoItem>();
            JsonArray jsaCategoryChanges = (JsonArray)Util.JSON.toArray(psItemChanges);
            for (int j=0; j<jsaCategoryChanges.size();j++)
            {
                JsonObject jsChangeN = (JsonObject)jsaCategoryChanges.get(j);

                bb.estore.ssoItem itemChangeN = new bb.estore.ssoItem();

                itemChangeN.Id          = jsChangeN.get("Id").getAsString();
                itemChangeN.itemCode    = jsChangeN.get("code").getAsString();
                itemChangeN.name        = jsChangeN.get("name").getAsString();
                itemChangeN.active      = jsChangeN.get("active").getAsString();
                itemChangeN.vendorId    = jsChangeN.get("vndId").getAsString();
                itemChangeN.categoryId  = jsChangeN.get("ctgId").getAsString();
                itemChangeN.hashTagId   = jsChangeN.get("htId").getAsString();
                itemChangeN.featureId   = jsChangeN.get("ftId").getAsString();
                itemChangeN.images      = jsChangeN.get("imgs").toString();// .getAsString();
                itemChangeN.videos      = jsChangeN.get("vids").toString();// .getAsString();
                itemChangeN.defaultImage= jsChangeN.get("defImg").getAsString();
                
                if(itemChangeN.images.trim().length()==0)
                    itemChangeN.images = "[]";

                if(itemChangeN.videos.trim().length()==0)
                    itemChangeN.videos = "[]";
                
                aChanges.add(itemChangeN);

            }

            ssEStoreOps.updateItems(gem, gUserId, accConnected.uid, aChanges);

            rsp.Content = Util.JSON.Convert2JSON(aChanges).toString();
            rsp.callbackId = Util.DateTime.GetDateTime_s();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }
    
    @GET
    @Path("/gin/{aid},"
                + "{lng},"
                + "{cnt},"
                + "{sid},"
                + "{ip},"
                + "{iid}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse generateItemName( @PathParam("aid")                          String pAccId,
                                            @PathParam("lng")                          String psLang,
                                            @PathParam("cnt")                          String psCountry,
                                            @PathParam("sid")                          String psSessionId,
                                            @PathParam("ip")                           String psIP,
                                            @PathParam("iid")                          String psItemId
                                          ) throws Exception
    {
        try
        {
            ArrayList<ssoInvBrandItemCodes> aItemsFound = new ArrayList<ssoInvBrandItemCodes>();

            ssoAPIResponse rsp = new ssoAPIResponse();
            ssoUserAccounts accConnected = new ssoUserAccounts();

            BigInteger lSessionUserId = gUserId;//for now
            BigInteger lTargetAccId   = new BigInteger(pAccId);
            BigInteger biItemUID      = new BigInteger(psItemId);

            accConnected = Users.connectAccount(gem, lSessionUserId, lTargetAccId, gServiceGrants);
            if(accConnected.bAccessGranted==false)
            {
                if(accConnected.bForce2Login==true)
                    rsp.AuthenticationFailed = true;

                rsp.Response = "err";
                rsp.ResponseMsg = "Account invalid";
                return rsp;
            }
            
            String sItemName = ssEStoreOps.autoGenerateItemName(gem, gUserId, accConnected.uid, biItemUID);
         
            rsp.Content = Util.JSON.Convert2JSON(sItemName).toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }
    

}
