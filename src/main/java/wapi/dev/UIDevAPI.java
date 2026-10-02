/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package wapi.dev;

import Methods.Users;
import Objects.ssoUserAccounts;
import bb.app.apis.APIOps;
import static bb.app.apis.APIOps.reloadEStorePageParams;
import bb.app.apis.ssoCategory;
import bb.app.apis.ssoOption;
import bb.app.apis.ssoPageParamsHome;
import bb.app.apis.ssoProduct;
import bb.app.apis.ssoSection;
import bb.app.apis.ssoEStoreAccPageParams;
import bb.app.apis.ssoEStoreHomeSummary;
import bb.app.apis.ssoWebItem;
import java.math.BigInteger;
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
//@Path("/api/access")//3rd Party API 
@Path("/api/access")//3rd Party API 
public class UIDevAPI implements jeiRestInterface
{
    BigInteger gUserId  = BigInteger.ZERO;//From API key to User Id + Account Id
    BigInteger gAccountId  = BigInteger.ZERO;//From APIKey to UserId + Account Id

    EntityManager gem;
    ArrayList<String> gServiceGrants = new ArrayList<String>();

    String APIVersion = "1.0";

    public UIDevAPI()
    {
        
    }

    @Override
    public void init(String pUserId, String pBrowserId, String p3rdPartyAPIKey, EntityManager pem, String psUserRoleReqs)
    {
        try
        {
            String s = "";

            //gUserId = new BigInteger(pUserId);
            gem = pem;
            gem.SetSessionUser(pUserId);

            gServiceGrants.addAll(Arrays.asList(psUserRoleReqs.split(",")));

            // Validate API KEY
            //------------------------------------------------------------------
            
        }
        catch(Exception e)
        {
            
        }
    }

    @GET
    @Path("/test")
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse test() throws Exception
    {
        try
        {
            ssoAPIResponse rsp = new ssoAPIResponse();

            /*
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
            */
            ssoPageParamsHome EStoreUIParams = new ssoPageParamsHome();
            
            EStoreUIParams = APIOps.getTestEStorePageParams();

            //String sResponse = "{\"version\": \"" + APIVersion + "\"}";
            rsp.Content = Util.JSON.Convert2JSON(EStoreUIParams).toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    // THIS RESETS MEMORY DATA (USED IF NEW DATA ADDED TO THE SITE)
    @GET
    @Path("/reset/{uid},{aid},{at}")
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse reset(@PathParam("uid")                         String pUserId,
                                @PathParam("aid")                         String pAccountId,
                                @PathParam("at")                          String psAccountType// U: user A: Account ) throws Exception
                               ) throws Exception 
    {
        try
        {
            ssoAPIResponse rsp = new ssoAPIResponse();

            /*
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
            */
            BigInteger biUserId = new BigInteger(pUserId);
            BigInteger biAccId = new BigInteger(pAccountId);

            ssoEStoreHomeSummary params = new ssoEStoreHomeSummary();
            boolean bUser = false;
            if(psAccountType.toLowerCase().trim().equals("u")==true)
                bUser = true;

            params = APIOps.reloadEStorePageParams(gem, biUserId, biAccId, bUser);

            //String sResponse = "{\"version\": \"" + APIVersion + "\"}";
            rsp.Content = Util.JSON.Convert2JSON("ok").toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    // THIS RESETS MEMORY DATA (USED IF NEW DATA ADDED TO THE SITE)
    @GET
    @Path("/tpp")
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse testEStorePageParams() throws Exception
    {
        try
        {
            ssoAPIResponse rsp = new ssoAPIResponse();

            /*
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
            */
            
            ssoPageParamsHome EStoreUIParams = new ssoPageParamsHome();
            
            BigInteger biUserId = new BigInteger("38482644");
            BigInteger biAccId = new BigInteger("38482645");
            
            EStoreUIParams = APIOps.getTestEStorePageParams();
            
            //String sResponse = "{\"version\": \"" + APIVersion + "\"}";
            rsp.Content = Util.JSON.Convert2JSON(EStoreUIParams).toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    // THIS RESETS MEMORY DATA (USED IF NEW DATA ADDED TO THE SITE) 
    @GET
    @Path("/ghp/{uid},{aid},{at},{cur},{lng}")
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse getEStoreHomeParams(  @PathParam("uid")                         String pUserId,
                                                @PathParam("aid")                         String pAccountId,
                                                @PathParam("at")                          String psAccountType, // U: user A: Account 
                                                @PathParam("cur")                         String psCurrency,
                                                @PathParam("lng")                         String psLang
                                                ) throws Exception
    {
        try
        {
            ssoAPIResponse rsp = new ssoAPIResponse();

            /*
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
            */
            BigInteger biUserId = new BigInteger(pUserId);
            BigInteger biAccId = new BigInteger(pAccountId);

            boolean bUser = false;
            if(psAccountType.toLowerCase().trim().equals("u")==true)
                bUser = true;
            
            String sParams = APIOps.getEStorePageParams(gem, biUserId, biAccId, bUser);

            //String sResponse = "{\"version\": \"" + APIVersion + "\"}";
            rsp.Content = sParams;
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    @GET
    @Path("/srch/{uid},"
                + "{aid},"
                + "{cur},"
                + "{lng},"
                + "{at},"
                + "{sec},"
                + "{ctg},"
                + "{gnd},"
                + "{opt},"
                + "{prc},"
                + "{oth},"
                + "{pg}"
    )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse search(@PathParam("uid")                          String pUserId,
                                 @PathParam("aid")                          String pAccountId,
                                 @PathParam("cur")                          String psCurrency,
                                 @PathParam("lng")                          String psLang,
                                 @PathParam("at")                           String psAccountType,
                                 @PathParam("sec")                          String psSections,
                                 @PathParam("ctg")                          String psCategories,
                                 @PathParam("gnd")                          String psGenders,
                                 @PathParam("opt")                          String psOptions,
                                 @PathParam("prc")                          String psPriceRange,
                                 @PathParam("oth")                          String psOther,
                                 @PathParam("pg")                           String psPageNumber//
                                ) throws Exception
    {
        try
        {
            ssoAPIResponse rsp = new ssoAPIResponse();

            /*
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
            */
            
            int nPageNumber = Integer.parseInt(psPageNumber);
            
            String[] saFilterSections    = psSections.split(",");
            String[] saFilterCategories  = psCategories.split(",");
            String[] saFilterGenders     = psGenders.split(",");
            String[] saFilterOptions     = psOptions.split(",");
            String sFilterPriceRange    = psPriceRange;
            String[] sFilterOther = new String[0];

            BigInteger biUserId = new BigInteger(pUserId);
            BigInteger biAccId = new BigInteger(pAccountId);

            boolean bUser = false;
            if(psAccountType.toLowerCase().trim().equals("u")==true)
                bUser = true;
            
            ArrayList<ssoWebItem> items = new ArrayList<ssoWebItem>();

            boolean rc = APIOps.isEStorePageParamsLoaded(biUserId, biAccId, bUser);
            if(rc==false)
            {
                ssoEStoreHomeSummary oPageParams = new ssoEStoreHomeSummary();
                oPageParams = reloadEStorePageParams(gem, biUserId, biAccId, bUser);
            }
            
            items = APIOps.searchItems(gem, biUserId, biAccId, bUser, nPageNumber, saFilterSections, saFilterCategories, saFilterGenders, saFilterOptions, sFilterPriceRange, sFilterOther);

            //String sResponse = "{\"version\": \"" + APIVersion + "\"}";
            rsp.Content = Util.JSON.Convert2JSON(items).toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

}


