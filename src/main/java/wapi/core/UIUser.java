/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wapi.core;

import Methods.Users;
import api.methods.Signup;
import bb.app.account.AccountMisc;
import bb.app.dict.DictionaryOps;
import bb.app.obj.ssoBrandDets;
import bb.app.obj.ssoMainParams;
import Objects.ssoMerchant;
import Objects.ssoUserAccounts;
import bb.app.obj.ssoMerchantPreferences;
import entity.user.SsUsrAccounts;
import java.math.BigDecimal;
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
import jaxesa.persistence.Query;
import jaxesa.persistence.EntityManager;
import jaxesa.persistence.StoredProcedureQuery;
import jaxesa.persistence.annotations.ParameterMode;
import jaxesa.persistence.ssoCacheSplitKey;
import jaxesa.util.Util;
import jaxesa.webapi.ssoAPIResponse;
import misc.DekontMisc;
import restapi.jeiRestInterface;

/**
 *
 * @author Administrator
 */
 @Path("/api/usr")
public class UIUser  implements jeiRestInterface
{
    BigInteger gUserId  = BigInteger.ZERO;
    EntityManager gem;
    ArrayList<String> gServiceGrants = new ArrayList<String>();
    private static final Logger logger = Logger.getLogger(UIUser.class.getName());

    public UIUser()
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
    @Path("/gast/{aid},"
                    + "{lng},"
                    + "{cnt},"//browser
                    + "{sid},"
                    + "{pgid}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse getAccountN_Settings( @PathParam("aid")                      String pAccId,
                                                @PathParam("lng")                      String psLang,
                                                @PathParam("cnt")                      String psCountry,
                                                @PathParam("sid")                      String psSessionId,
                                                @PathParam("pgid")                     String psPageId
                                                 //@PathParam("mid")                          String psMerchantId
                                               ) throws Exception
    {
        int iRec = 0;
        try
        {
            ssoAPIResponse Rsp = new ssoAPIResponse();

            //EntityManager em = DBPool.getSessionConnection(psUser_SessionInfo, Util.Methods.hash());
            ssoUserAccounts accConnected = new ssoUserAccounts();

            BigInteger lSessionUserId = gUserId;//for now
            BigInteger lTargetAccId   = new BigInteger(pAccId);

            accConnected = Users.connectAccount(gem, lSessionUserId, lTargetAccId, gServiceGrants);
            if(accConnected.bAccessGranted==false)
            {
                if(accConnected.bForce2Login==true)
                    Rsp.AuthenticationFailed = true;

                Rsp.Response = "err";
                Rsp.ResponseMsg = "Account invalid";
                return Rsp;
            }

            //long lMrcId = Long.parseLong(pAccId);
            ssoMerchantPreferences mrcPrefs = new ssoMerchantPreferences();
            
            mrcPrefs = DekontMisc.getShortMerchantPreferences(gem, accConnected.userId, accConnected.uid);
            
            Rsp.Content  = Util.JSON.Convert2JSON(mrcPrefs).toString();
            Rsp.Response = "ok";
            return Rsp;

        }
        catch(Exception e)
        {
            throw e;
        }
    }

    @GET
    @Path("/gub/{aid},"
                + "{lng},"
                + "{cnt},"
                + "{bid},"
                + "{sid},"
                + "{ip},"
                + "{rst}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse getUserBranches(  @PathParam("aid")                          String pAccId,
                                            @PathParam("lng")                          String psLang,
                                            @PathParam("cnt")                          String psCountry,
                                            @PathParam("bid")                          String psBrowserId,
                                            @PathParam("sid")                          String psSessionId,
                                            @PathParam("ip")                           String psIP,
                                            @PathParam("rst")                          String psResetCache
                                      ) throws Exception
    {
        try
        {
            ssoAPIResponse rsp = new ssoAPIResponse();
            ssoUserAccounts accConnected = new ssoUserAccounts();

            // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            //
            // This is SESSION USER specific api. Lists the accounts / branches
            // connected to the session user only. This won't give access to 
            // the root user that granted access to the account in case of
            // the user (session) is not ROOT user. In other words, delegated
            //
            // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

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

            ssoMainParams MainParams = new ssoMainParams();
            ArrayList<ssoMerchant> mrcList = new ArrayList<ssoMerchant>();
            ArrayList<ssoBrandDets> brandDets = new ArrayList<ssoBrandDets>();

            boolean bResetCache = false;
            if (psResetCache.trim().equals("Y")==true)
                bResetCache = true;

            //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            // WARNING: If passed all the security GUSERID MUST BE USED HERE
            // NOT accountConnected.userId
            // Because this is profile specific
            //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            mrcList = DictionaryOps.User.getListOfAccounts4User(gem, gUserId, bResetCache);

            rsp.Content = Util.JSON.Convert2JSON(mrcList).toString();
            rsp.Response = "ok";

            // THIS LOGIC MUST BE FULLY REVIEWED
            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    @GET
    @Path("/vun/{aid},"
                + "{lng},"
                + "{cnt},"
                + "{sid},"
                + "{ip},"
                + "{bid},"
                + "{unm}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    //@Token(VerificationType.MUST)//if token not receive means no UserId so it fails
    public ssoAPIResponse verifyUserName(   @PathParam("aid")                          String pAccId,
                                            @PathParam("lng")                          String psLang,
                                            @PathParam("cnt")                          String psCountry,
                                            @PathParam("sid")                          String psSessionId,
                                            @PathParam("ip")                           String psIP,
                                            @PathParam("bid")                          String psBrowserId,
                                            @PathParam("unm")                          String psUserName
                                      ) throws Exception
    {
        try
        {
            ssoAPIResponse rsp = new ssoAPIResponse();

            boolean rc = DictionaryOps.User.isUserNameValid4Registration(gem, gUserId, psUserName);
            if(rc==true)
                rsp.Response = "ok";
            else
                rsp.Response = "nok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    @GET
    @Path("/veml/{aid},"
                + "{lng},"
                + "{cnt},"
                + "{sid},"
                + "{ip},"
                + "{bid},"
                + "{eml}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    //@Token(VerificationType.MUST)//if token not receive means no UserId so it fails
    public ssoAPIResponse verifyEmail(   @PathParam("aid")                          String pAccId,
                                        @PathParam("lng")                          String psLang,
                                        @PathParam("cnt")                          String psCountry,
                                        @PathParam("sid")                          String psSessionId,
                                        @PathParam("ip")                           String psIP,
                                        @PathParam("bid")                          String psBrowserId,
                                        @PathParam("eml")                          String psEmail
                                  ) throws Exception
    {
        try
        {
            ssoAPIResponse rsp = new ssoAPIResponse();
            
            boolean rc = DictionaryOps.User.isEmailValid4Registration(gem, gUserId, psEmail);
            if(rc==true)
                rsp.Response = "ok";
            else
                rsp.Response = "nok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    @GET
    @Path("/svac/{aid},"
                            + "{lng},"
                            + "{cnt},"//browser
                            + "{bid},"
                            + "{sid},"
                            + "{pgid},"
                            //+ "{mid},"
                            + "{cur},"
                            + "{mcc},"
                            + "{ncc},"
                            + "{cc},"
                            + "{sc},"
                            + "{coc},"
                            + "{tx},"
                            + "{txr},"
                            + "{inr},"
                            + "{eml},"
                            + "{nm},"
                            + "{dm},"
            
                            + "{pc},"
                            + "{ses},"
                            + "{sbn}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse updateAccount(        @PathParam("aid")                          String pAccId,
                                                @PathParam("lng")                          String psLang,
                                                @PathParam("cnt")                          String psCountry,
                                                @PathParam("bid")                          String psBrowserId,
                                                @PathParam("sid")                          String psSessionId,
                                                @PathParam("pgid")                         String psPageId,
                                                //@PathParam("mid")                          String psMerchantId,
                                                @PathParam("cur")                          String psCurrency,
                                                @PathParam("mcc")                          String psMCC,
                                                @PathParam("ncc")                          String psNaceCode,
                                                @PathParam("cc")                           String psCountryCode,
                                                @PathParam("sc")                           String psStateCode,
                                                @PathParam("coc")                          String psPlaceNameUId,
                                                @PathParam("tx")                           String psTaxInPrice,
                                                @PathParam("txr")                          String psTaxRate,
                                                @PathParam("inr")                          String psInsDiffRate,
                                                @PathParam("eml")                          String psEmail,
                                                @PathParam("nm")                           String psProfileName,
                                                @PathParam("dm")                           String psDisplayName,
                                                @PathParam("pc")                           String psPrinterCode,

                                                @PathParam("ses")                          String psSeperateEStore,
                                                @PathParam("sbn")                          String psShowBrandName
                                                
                                              ) throws Exception
    {
        int iRec = 0;
        try
        {
            boolean bActivated = true;
            
            Util.Tomcat.print2TomcatLog(logger, "api/usr/svac starting", pAccId);
            
            ssoAPIResponse rsp = new ssoAPIResponse();
            ssoUserAccounts accConnected = new ssoUserAccounts();

            BigInteger lSessionUserId = gUserId;//for now
            BigInteger lTargetAccId   = new BigInteger(pAccId);
            
            //if(lTargetAccId!=-1)//means not a new account adding in other words updating existing one
            if(lTargetAccId.compareTo(BigInteger.valueOf(-1))!=0)
            {
                Util.Tomcat.print2TomcatLog(logger, "api/usr/svac Updating", pAccId);
                //--------------------------------------------------------------
                //
                //  UPDATE 
                //  
                //  Warning: Email can NOT be updated
                //_-------------------------------------------------------------

                accConnected = Users.connectAccount(gem, lSessionUserId, lTargetAccId, gServiceGrants);
                if(accConnected.bAccessGranted==false)
                {
                    if(accConnected.bForce2Login==true)
                        rsp.AuthenticationFailed = true;

                    rsp.Response = "err";
                    rsp.ResponseMsg = "Account invalid";
                    return rsp;
                }

                long lMrcId = Long.parseLong(pAccId);
                ssoMerchantPreferences mrcPrefs = new ssoMerchantPreferences();
                
                Util.Tomcat.print2TomcatLog(logger, "api/usr/svac > getAccountSettings", pAccId);
                
                mrcPrefs = AccountMisc.getAccountSettings(gem, accConnected.userId, accConnected.uid);

                if (mrcPrefs!=null)
                {
                    boolean bValid = true;//default
                    String sProfileName = mrcPrefs.profileName;
                    if(psProfileName.toLowerCase().trim().equals(mrcPrefs.profileName.toLowerCase().trim())==false)//IF PROFILE NAME CHANGED
                    {
                        bValid = DictionaryOps.User.isUserNameValid4Registration(gem, gUserId, psProfileName.trim());//
                        sProfileName = psProfileName;
                    }
                    
                    if(bValid==true)//check if profile name changed valid 
                    {
                        Util.Tomcat.print2TomcatLog(logger, "api/usr/svac > updating", pAccId);
                        
                        boolean bMainAccount = false;
                        if(accConnected.mainAccountNo.compareTo(BigInteger.ZERO)==0)
                            bMainAccount = true;


                        //update will be implemented here
                        //Query stmt = gem.createNamedQuery("SsUsrAccounts.updatePrefs", SsUsrAccounts.class);
                        //int index = 1;

                        //String sEmail = mrcPrefs.email;

                        //if (mrcPrefs.isActive.trim().equals("Y")==false)//if not activated, allow to change email
                        //    bActivated = false;

                        //if(bActivated==false)//if not activated, allow to change email
                        //    sEmail = psEmail; 
                        String[] aPrinterCodeParts = psPrinterCode.split(":");
                        String sPrinterCode = aPrinterCodeParts[0];
                        String sLabelWidth  = aPrinterCodeParts[1];
                        String sLabelHeight = aPrinterCodeParts[2];
                        /*
                        stmt.SetParameter(index++, sPrinterCode   , "PRINTER_CODE");
                        stmt.SetParameter(index++, sLabelWidth    , "LABEL_WIDTH_MM");
                        stmt.SetParameter(index++, sLabelHeight   , "LABEL_HEIGHT_MM");
                        stmt.SetParameter(index++, sProfileName   , "PROFILENAME"); 
                        stmt.SetParameter(index++, psCurrency     , "CURRENCY");
                        stmt.SetParameter(index++, psMCC          , "MCC");
                        stmt.SetParameter(index++, psStateCode    , "STATE_CODE");
                        stmt.SetParameter(index++, psPlaceNameUId , "PLACE_NAME_UID");//county_code => PLACE UID
                        stmt.SetParameter(index++, psCountryCode  , "COUNTRY_CODE");
                        stmt.SetParameter(index++, psTaxInPrice   , "IS_TAX_INC_PRICE");
                        stmt.SetParameter(index++, psTaxRate      , "TAX_RATE");
                        stmt.SetParameter(index++, psInsDiffRate  , "INS_DIFF_RATE");

                        stmt.SetParameter(index++, psSeperateEStore  , "SEPERATE_ESTORE");
                        stmt.SetParameter(index++, psShowBrandName   , "SHOW_BRAND_NAMES_ON_ESTORE");

                        stmt.SetParameter(index++, accConnected.uid         , "UID");//acc Id
                        stmt.SetParameter(index++, accConnected.userId      , "USER_ID");//This param will help reset the cache

                        //psTaxInPrice
                        stmt.executeUpdate();
                        */
                        StoredProcedureQuery SP = gem.createStoredProcedureQuery("SP_MRC_UPDATE_MERCHANT_PREFERENCES");

                        SP.registerStoredProcedureParameter("P_USR_ID"           , BigInteger.class     , ParameterMode.IN);
                        SP.registerStoredProcedureParameter("P_ACC_ID"           , BigInteger.class     , ParameterMode.IN);
                        SP.registerStoredProcedureParameter("P_MAIN_ACCOUNT_ID"  , BigInteger.class     , ParameterMode.IN);
                        
                        SP.registerStoredProcedureParameter("P_PRINTER_CODE"     , String.class       , ParameterMode.IN);
                        SP.registerStoredProcedureParameter("P_LABEL_WIDTH"      , String.class       , ParameterMode.IN);
                        SP.registerStoredProcedureParameter("P_LABEL_HEIGHT"     , String.class       , ParameterMode.IN);
                        SP.registerStoredProcedureParameter("P_PROFILE_NAME"     , String.class       , ParameterMode.IN);
                        SP.registerStoredProcedureParameter("P_DISPLAY_NAME"     , String.class       , ParameterMode.IN);
                        
                        SP.registerStoredProcedureParameter("P_CURRENCY_CODE"    , String.class       , ParameterMode.IN);
                        SP.registerStoredProcedureParameter("P_MCC"              , String.class       , ParameterMode.IN);
                        SP.registerStoredProcedureParameter("P_NACE_CODE"        , String.class       , ParameterMode.IN);
                        SP.registerStoredProcedureParameter("P_STATE_CODE"       , String.class       , ParameterMode.IN);
                        SP.registerStoredProcedureParameter("P_PLACE_NAME_ID"    , String.class       , ParameterMode.IN);
                        SP.registerStoredProcedureParameter("P_COUNTRY_CODE"     , Integer.class      , ParameterMode.IN);

                        SP.registerStoredProcedureParameter("P_B_IS_TAX_INC_PRICE"          , String.class     , ParameterMode.IN);
                        SP.registerStoredProcedureParameter("P_TAX_RATE"                    , BigDecimal.class , ParameterMode.IN);
                        SP.registerStoredProcedureParameter("P_INSTALLMENT_DIFFERENCE_RATE" , BigDecimal.class , ParameterMode.IN);
                        SP.registerStoredProcedureParameter("P_B_SEPERATE_ESTORE"           , String.class     , ParameterMode.IN);
                        SP.registerStoredProcedureParameter("P_B_SHOW_BRAND_NAMES_ON_ESTORE", String.class     , ParameterMode.IN);

                        int index = 1;
                        SP.SetParameter(index++, accConnected.userId        , "USER_ID");//acc Id
                        SP.SetParameter(index++, accConnected.uid           , "ACC_ID");//This param will help reset the cache
                        SP.SetParameter(index++, accConnected.mainAccountNo , "MAIN_ACCOUNT_ID");//This param will help reset the cache

                        SP.SetParameter(index++, sPrinterCode   , "PRINTER_CODE");
                        SP.SetParameter(index++, sLabelWidth    , "LABEL_WIDTH_MM");
                        SP.SetParameter(index++, sLabelHeight   , "LABEL_HEIGHT_MM");
                        SP.SetParameter(index++, sProfileName   , "PROFILENAME"); 
                        SP.SetParameter(index++, psDisplayName  , "DISPLAYNAME");
                        SP.SetParameter(index++, psCurrency     , "CURRENCY");
                        SP.SetParameter(index++, psMCC          , "MCC");
                        SP.SetParameter(index++, psNaceCode     , "NACE_CODE");
                        SP.SetParameter(index++, psStateCode    , "STATE_CODE");
                        SP.SetParameter(index++, psPlaceNameUId , "PLACE_NAME_UID");//county_code => PLACE UID
                        SP.SetParameter(index++, psCountryCode  , "COUNTRY_CODE");
                        SP.SetParameter(index++, psTaxInPrice   , "IS_TAX_INC_PRICE");
                        SP.SetParameter(index++, psTaxRate      , "TAX_RATE");
                        SP.SetParameter(index++, psInsDiffRate  , "INS_DIFF_RATE");

                        SP.SetParameter(index++, psSeperateEStore  , "SEPERATE_ESTORE");
                        SP.SetParameter(index++, psShowBrandName   , "SHOW_BRAND_NAMES_ON_ESTORE");


                        SP.execute();
                        

                        // RESET MERCHANT PREFERENCES
                        ssoCacheSplitKey keyUserId = new ssoCacheSplitKey();
                        keyUserId.column = "USER_ID";
                        keyUserId.value  = accConnected.userId;

                        gem.flush(SsUsrAccounts.class, keyUserId);

                        rsp.Content  = "";//Util.JSON.Convert2JSON('').toString();
                        rsp.Response = "ok";
                    }
                    else
                    {
                        Util.Tomcat.print2TomcatLog(logger, "api/usr/svac > username invalid", pAccId);
                        
                        rsp.Content = "user-name-invalid";
                        rsp.Response= "nok";
                    }

                }
            }
            else
            {
                //--------------------------------------------------------------
                //
                //  ADDING NEW ACCOUNT
                //  
                //--------------------------------------------------------------

                // IF EMAIL is not registered. Generate account and send new account request
                // ADD a new branch Account
                // 
                /*
                boolean bEmailNotRegistered = DictionaryOps.User.isEmailValid4Registration(gem, -1, psEmail);
                if(bEmailNotRegistered==false)
                {
                    //
                }
                */

                // USE THIS SP SP_SGN_ADD_NEW_ACCOUNT_2_USER
                
                /*
                Signup.generateNewAccountRequest("", //ip
                                                psBrowserId,
                                                "A", //U: User A: Account 
                                                 psProfileName, 
                                                 "", //last name
                                                 psCountryCode, 
                                                 "", //phone
                                                 "", //gender
                                                 "", //birthday
                                                 psEmail,
                                                 "",//pwd
                                                 "",//city
                                                 psStateCode,
                                                 psLang, 
                                                 "", 
                                                 "");
                */
                Util.Tomcat.print2TomcatLog(logger, "api/usr/svac user not found", pAccId);
                
                rsp.Content  = "User not found";//Util.JSON.Convert2JSON('').toString();
                rsp.Response = "nok";

            }

            AccountMisc.resetMemoryTables4Account(gem, accConnected.userId);

            return rsp;
        }
        catch(Exception e)
        {
            Util.Tomcat.print2TomcatLog(logger, "api/usr/svac exception: " + e.getMessage(), pAccId);
            throw e;
        }
    }

}
