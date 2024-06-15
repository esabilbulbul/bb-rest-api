/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webapi;

import Methods.Users;
import bb.app.account.ssoVendorPayment;
import bb.app.inv.InventoryOps;
import bb.app.inv.InventoryParams;
import bb.app.obj.ssoBrandDets;
import bb.app.obj.ssoInvBrandItemCodes;
import bb.app.obj.ssoInvCategory;
import bb.app.obj.ssoInvParams;
import bb.app.obj.ssoMainParams;
import bb.app.obj.ssoMerchant;
import bb.app.account.UserOps;
import bb.app.dict.DictionaryOps;
import static bb.app.dict.DictionaryOps.getAllPaymentGroups;
import bb.app.obj.ssoCityCode;
import bb.app.obj.ssoCountryCodes;
import bb.app.obj.ssoCountyCode;
import bb.app.obj.ssoMCC;
import bb.app.obj.ssoMerchantPreferences;
import bb.app.obj.ssoPageParams;
import bb.app.obj.ssoPaymentGroup;
import bb.app.obj.ssoPaymentParams;
import bb.app.obj.ssoPostCode;
import bb.app.payment.PaymentOps;
import bb.app.vendor.VendorOps;
import entity.prm.SsPrmCountryCodes;
import entity.prm.SsPrmCountryPostcodes;
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
import jaxesa.persistence.DBPool;
import jaxesa.persistence.EntityManager;
import jaxesa.util.Util;
import jaxesa.webapi.ssoAPIResponse;
import misc.DekontMisc;
import restapi.jeiRestInterface;

/**
 *
 * @author Administrator
 * 
 * LIST OF PARAMS
 * 
 * PARAMS ARE SPREAD OUT TO PARTIAL DOWNLOADS
 * 
 * 1. Main Params      = Branches, Vendor Details (at the time of page load)
 * 2. Inventory Params = Item Codes, Categories (at the time of inventory screen load)
 * 3. Supplier Detail  = Supplier/Vendor Details (at the time of supplier page)
 * 4. General Params (getparams) = Postcodes, MCCs (at the time of page load)
 * 
 */
@Path("/api/biz/bb/params")
public class UI_Param implements jeiRestInterface
{
    // SESSION VARIABLE
    long gUserId  = -1;
    EntityManager gem;
    ArrayList<String> gServiceGrants = new ArrayList<String>();

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
    @Path("/gmp/{aid},"
                + "{lng},"
                + "{cnt},"
                + "{sid}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    //public ssoAPIResponse getMerchantListOfUsers(   @PathParam("aid")                           String psUser_SessionInfo,//user account id
    public ssoAPIResponse getAccountParams( @PathParam("aid")                       String pAccountId,//user account id
                                            @PathParam("lng")                       String psLang,
                                            @PathParam("cnt")                       String psCountry,
                                            @PathParam("sid")                       String psSessionId
                                          ) throws Exception
    {
        int iRec = 0;
        try
        {
            ssoAPIResponse rsp = new ssoAPIResponse();

            //EntityManager em = DBPool.getSessionConnection(psUser_SessionInfo, Util.Methods.hash());
            EntityManager em = gem;
            SsUsrAccounts accConnected = new SsUsrAccounts();

            long lSessionUserId = gUserId;//for now
            long lTargetAccId   = Long.parseLong(pAccountId);

            accConnected = Users.connectAccount(gem, lSessionUserId, lTargetAccId, gServiceGrants);
            if(accConnected==null)
            {
                rsp.Response = "err";
                rsp.ResponseMsg = "Account invalid";
                return rsp;
            }
            //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            // 
            // Page Params for "mystats"
            // 1. List of users/retails/branches linked to the user
            // 2. List of vendors/suppliers
            // 2. Market Data 
            //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            ssoMainParams MainParams = new ssoMainParams();
            ArrayList<ssoMerchant> mrcList = new ArrayList<ssoMerchant>();
            ArrayList<ssoBrandDets> brandDets = new ArrayList<ssoBrandDets>();

            // This gets linked accounts/retailers to the users 
            // mrcList = DekontMisc.getListOfMerchants4User(gem, gUserId);
            ssoMerchantPreferences mrcPrefs = new ssoMerchantPreferences();

            mrcPrefs = DekontMisc.getShortMerchantPreferences(gem, accConnected.userId, accConnected.uid);
            
            mrcList = DictionaryOps.User.getListOfAccounts4User(gem, lSessionUserId, false);//This must be sessionUserId

            brandDets = VendorOps.getAllVendorDetails(gem, accConnected.userId);

            MainParams.branches = mrcList;
            MainParams.vendors  = brandDets;
            MainParams.prefs    = mrcPrefs;

            rsp.Content = Util.JSON.Convert2JSON(MainParams).toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    @GET
    @Path("/gprmpym/{lng},"
                    + "{cnt},"
                    + "{bid},"
                    + "{sid},"
                    + "{ip},"
                    + "{aid}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse getPaymentParams( @PathParam("lng")         String psLang,
                                            @PathParam("cnt")         String psCountry,
                                            @PathParam("bid")         String psBrowserId,
                                            @PathParam("sid")         String psUISessionId,//don't change the paramter name (sid) 
                                            @PathParam("ip")          String psIp,
                                            @PathParam("aid")         String pAccId
                                         ) throws Exception
    {
        // This API returns brand list and category list for the account requesting

        int iRec = 0;
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

            // PARAMS = PAYMENT GROUPS 
            ssoPaymentParams paymentParams = new ssoPaymentParams();

            paymentParams.groups    = getAllPaymentGroups(gem, accConnected.userId);
            paymentParams.brandDets = VendorOps.getAllVendorDetails(gem, accConnected.userId);

            rsp.Content = Util.JSON.Convert2JSON(paymentParams).toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    @GET
    @Path("/dlpygr/{lng},"
                    + "{cnt},"
                    + "{bid},"
                    + "{sid},"
                    + "{ip},"
                    + "{aid},"
                    + "{pgi}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse deletePaymentGroup(   @PathParam("lng")         String psLang,
                                                @PathParam("cnt")         String psCountry,
                                                @PathParam("bid")         String psBrowserId,
                                                @PathParam("sid")         String psUISessionId,//don't change the paramter name (sid) 
                                                @PathParam("ip")          String psIp,
                                                @PathParam("aid")         String pAccId,
                                                @PathParam("pgi")         String pPaymentGroupId
                                             ) throws Exception
    {
        // This API returns brand list and category list for the account requesting

        int iRec = 0;
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

            long lPaymentGroupId = Long.parseLong(pPaymentGroupId);

            PaymentOps.removePaymentGroup(gem, accConnected.userId, lPaymentGroupId);

            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    @GET
    @Path("/gprminvtxn/{lng},"
                        + "{cnt},"
                        + "{bid},"
                        + "{sid},"
                        + "{ip},"
                        + "{aid}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse getNewInventoryParams(@PathParam("lng")         String psLang,
                                                @PathParam("cnt")         String psCountry,
                                                @PathParam("bid")         String psBrowserId,
                                                @PathParam("sid")         String psUISessionId,//don't change the paramter name (sid) 
                                                @PathParam("ip")          String psIp,
                                                @PathParam("aid")         String pAccId
                                             ) throws Exception
    {
        // This API returns brand list and category list for the account requesting

        int iRec = 0;
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

            ssoInvParams params = new ssoInvParams();

            //Get Brand / Firm List (Vendors & Item Codes)
            params.brandItemCodes = InventoryParams.getVendorItemCodeSummary(gem, accConnected.userId);

            //Get Category List
            params.categories = InventoryParams.getCategoryList(gem, accConnected.uid);

            rsp.Content  = Util.JSON.Convert2JSON(params).toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    @GET
    @Path("/delinvctg/{lng},"
                        + "{cnt},"
                        + "{bid},"
                        + "{sid},"
                        + "{ip},"
                        + "{aid},"
                        + "{ctg}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse removeParam_Category( @PathParam("lng")         String psLang,
                                                @PathParam("cnt")         String psCountry,
                                                @PathParam("bid")         String psBrowserId,
                                                @PathParam("sid")         String psUISessionId,//don't change the paramter name (sid) 
                                                @PathParam("ip")          String psIp,
                                                @PathParam("aid")         String pAccId,
                                                @PathParam("ctg")         String psCategory
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

            DictionaryOps.Account.delete_Category(gem, lSessionUserId, psCategory);

            rsp.Content = "";
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }

    }

    @GET
    @Path("/dib/{lng},"
                        + "{cnt},"
                        + "{bid},"
                        + "{sid},"
                        + "{ip},"
                        + "{aid},"
                        + "{vid}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse removeParam_Brand(@PathParam("lng")         String psLang,
                                            @PathParam("cnt")         String psCountry,
                                            @PathParam("bid")         String psBrowserId,
                                            @PathParam("sid")         String psUISessionId,//don't change the paramter name (sid) 
                                            @PathParam("ip")          String psIp,
                                            @PathParam("aid")         String pAccId,
                                            @PathParam("vid")         String psBrandId
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

            //String sBrand = psBrand.toLowerCase();
            DictionaryOps.Vendor.delete_Brand(gem, accConnected.userId, accConnected.uid, psBrandId);

            rsp.Content = "";
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }

    }

    @GET
    @Path("/delinvitmcd/{lng},"
                                + "{cnt},"
                                + "{bid},"
                                + "{sid},"
                                + "{ip},"
                                + "{aid},"
                                + "{vid},"
                                + "{icd}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse removeParam_ItemCode( @PathParam("lng")         String psLang,
                                                @PathParam("cnt")         String psCountry,
                                                @PathParam("bid")         String psBrowserId,
                                                @PathParam("sid")         String psUISessionId,//don't change the paramter name (sid) 
                                                @PathParam("ip")          String psIp,
                                                @PathParam("aid")         String pAccId,
                                                @PathParam("vid")         String pBrandId,
                                                @PathParam("icd")         String pItemCode
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
            
            long lBrandId = Long.parseLong(pBrandId);
            DictionaryOps.Vendor.delete_ItemCode(gem, accConnected.userId, accConnected.uid, lBrandId, pItemCode);

            rsp.Content = "";
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }

    }

    @GET
    @Path("/getmysupdets/{lng},"
                            + "{cnt},"
                            + "{bid},"
                            + "{sid},"
                            + "{ip},"
                            + "{aid}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse getSupplierDetails(   @PathParam("lng")         String psLang,
                                                @PathParam("cnt")         String psCountry,
                                                @PathParam("bid")         String psBrowserId,
                                                @PathParam("sid")         String psUISessionId,//don't change the paramter name (sid) 
                                                @PathParam("ip")          String psIp,
                                                @PathParam("aid")         String pAccId
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

            ArrayList<ssoBrandDets> brandDets = new ArrayList<ssoBrandDets>();

            brandDets = VendorOps.getAllVendorDetails(gem, accConnected.userId);

            rsp.Content = Util.JSON.Convert2JSON(brandDets).toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    /*
        THIS FUNCTION SENDS THE PARAMETERS TO THE PAGE AUTO SELECTION OF COUNTRY
    */
    @GET
    @Path("/gprms/{userid},"
                            + "{lang},"
                            + "{country},"//browser
                            + "{sessionid}"
                            + "{pgid}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    public ssoAPIResponse getGeneralParams( @PathParam("userid")                        String psUser_SessionInfo,
                                            @PathParam("lang")                         String psLang,
                                            @PathParam("country")                      String psCountry,
                                            @PathParam("sessionid")                    String psSessionId,
                                            @PathParam("pgid")                         String psPageId
                                          ) throws Exception
    {
        int iRec = 0;
        try
        {
            ssoAPIResponse Rsp = new ssoAPIResponse();

            //EntityManager em = DBPool.getSessionConnection(psUser_SessionInfo, Util.Methods.hash());
            EntityManager em = gem;

            if (psCountry.trim().length()==0)
                psCountry = "tr";//default tr
            
            ssoPageParams pageParams = new ssoPageParams();
            pageParams.CountryCodeDefault = psCountry;

            // Country Codes
            //------------------------------------------------------------------
            ArrayList<SsPrmCountryCodes> countryCodes = new ArrayList<SsPrmCountryCodes>();

            countryCodes = InventoryParams.getCountryCodes(em);

            for (SsPrmCountryCodes CCN:countryCodes)
            {
                ssoCountryCodes newCode = new ssoCountryCodes();
                newCode.code = CCN.countryCode;
                newCode.lang = "en";
                newCode.name = CCN.countryName;
                
                pageParams.CountryCodes.add(newCode);
            }

            // City Codes (if TR then city codes, else then State codes)
            // For instance; US -> IL, NY, ...
            // Example 2: TR -> Istanbul, Izmir ...
            //------------------------------------------------------------------
            ArrayList<ssoCityCode> cityCodes = new ArrayList<ssoCityCode>();

            //returns city if TR otherwise States 
            cityCodes = InventoryParams.getCityCodes(em, psCountry);
            for (ssoCityCode CN:cityCodes)
            {
                ssoCityCode newCode = new ssoCityCode();
                newCode.code = CN.code;
                newCode.lang = "en";
                newCode.name = CN.name;

                pageParams.Cities.add(newCode);
            }
            
            // POST CODES - THIS WILL BE REMOVED
            //------------------------------------------------------------------            
            //InventoryOps.getPostCodes(em, "US", "IL");
            
            
            //THIS 3 LINES WILL BE REMOVED (NOT NEEDED ONLY FOR TESTING)

            //ArrayList<ssoCountyCode> countyCodes = new ArrayList<ssoCountyCode>();
            //countyCodes = InventoryOps.getCountyCodes(em, "US", "IL");
            //InventoryOps.readTest(em);


            // MCC 
            //------------------------------------------------------------------

            ArrayList<ssoMCC> MCCs = new ArrayList<ssoMCC>();
            MCCs = InventoryParams.getMCCs(em);
            for (ssoMCC CN:MCCs)
            {
                ssoMCC newCode = new ssoMCC();
                newCode.code = CN.code;
                newCode.lang = "en";
                newCode.name = CN.name;

                pageParams.MCCs.add(newCode);
            }


            Rsp.Content = Util.JSON.Convert2JSON(pageParams).toString();
            Rsp.Response = "ok";
            return Rsp;

        }
        catch(Exception e)
        {
            throw e;
        }
    }

    @GET
    @Path("/gpstcds/{userid},"
                            + "{lang},"
                            + "{country},"//browser
                            + "{sessionid},"
                            + "{pgid},"
                            + "{countrycode},"//selected
                            + "{citycode}"//selected
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    public ssoAPIResponse getPostCodes(    @PathParam("userid")                       String psUser_SessionInfo,
                                            @PathParam("lang")                         String psLang,
                                            @PathParam("country")                      String psCountry,
                                            @PathParam("sessionid")                    String psSessionId,
                                            @PathParam("pgid")                         String psPageId,
                                            @PathParam("countrycode")                  String psCountryCode,//selected
                                            @PathParam("citycode")                     String psCityCode//selected
                                          ) throws Exception
    {
        try
        {
            ssoAPIResponse Rsp = new ssoAPIResponse();

            EntityManager em = DBPool.getSessionConnection(psUser_SessionInfo, Util.Methods.hash());
            String sCountryCode = psCountryCode;
            String sCityCode = "";

            if (sCountryCode.trim().length()==0)
                sCountryCode = "tr";//default tr

            sCityCode = psCityCode;

            ArrayList<SsPrmCountryPostcodes> postCodes = new ArrayList<SsPrmCountryPostcodes>();
            ArrayList<ssoPostCode> UICodes = new ArrayList<ssoPostCode>();//to the client

            postCodes = InventoryParams.getPostCodes(em, sCountryCode, sCityCode);

            for (SsPrmCountryPostcodes SuburbN:postCodes)
            {
                ssoPostCode newCode = new ssoPostCode();
                newCode.code = SuburbN.postCode;
                newCode.lang = "en";
                newCode.name = SuburbN.placeName;
                
                UICodes.add(newCode);
            }
            
            Rsp.Content = Util.JSON.Convert2JSON(UICodes).toString();
            Rsp.Response = "ok";
            return Rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }
    
    @GET
    @Path("/gcnts/{uid},"
                    + "{lng},"
                    + "{cnt},"//country / browser
                    + "{sid},"
                    + "{pgid},"
                    + "{ccd},"//country code
                    + "{cic}"//selected
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    public ssoAPIResponse getCountyCodes(  @PathParam("uid")                       String psUser_SessionInfo,
                                            @PathParam("lng")                         String psLang,
                                            @PathParam("cnt")                      String psCountry,
                                            @PathParam("sid")                    String psSessionId,
                                            @PathParam("pgid")                         String psPageId,
                                            @PathParam("ccd")                  String psCountryCode,//selected
                                            @PathParam("cic")                     String psCityCode//selected
                                          ) throws Exception
    {
        try
        {
            ssoAPIResponse Rsp = new ssoAPIResponse();

            EntityManager em = DBPool.getSessionConnection(psUser_SessionInfo, Util.Methods.hash());
            String sCountryCode = psCountryCode;
            String sCityCode = "";

            if (sCountryCode.trim().length()==0)
                sCountryCode = "tr";//default tr

            sCityCode = psCityCode;

            ArrayList<ssoCountyCode> countyCodes = new ArrayList<ssoCountyCode>();
            //ArrayList<ssoPostCode> UICodes = new ArrayList<ssoPostCode>();//to the client

            countyCodes = InventoryParams.getCountyCodes(em, sCountryCode, sCityCode);

            /*
            for (SsPrmCountryPostcodes SuburbN:postCodes)
            {
                ssoPostCode newCode = new ssoPostCode();
                newCode.code = SuburbN.postCode;
                newCode.lang = "en";
                newCode.name = SuburbN.placeName;
                
                UICodes.add(newCode);
            }
            */
            
            Rsp.Content = Util.JSON.Convert2JSON(countyCodes).toString();
            Rsp.Response = "ok";
            return Rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

}


