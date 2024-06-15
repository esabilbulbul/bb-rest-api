/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webapi;

import Methods.Users;
import api.methods.Signup;
import bb.app.account.AccountMisc;
import bb.app.dict.DictionaryOps;
import bb.app.obj.ssoBrandDets;
import bb.app.obj.ssoMainParams;
import bb.app.obj.ssoMerchant;
import bb.app.obj.ssoMerchantPreferences;
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
import jaxesa.persistence.Query;
import jaxesa.persistence.EntityManager;
import jaxesa.util.Util;
import jaxesa.webapi.ssoAPIResponse;
import misc.DekontMisc;
import restapi.jeiRestInterface;

/**
 *
 * @author Administrator
 */
 @Path("/api/biz/bb/usr")
public class UIUser  implements jeiRestInterface
{
    long gUserId  = -1;
    EntityManager gem;
    ArrayList<String> gServiceGrants = new ArrayList<String>();

    public UIUser()
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
    public ssoAPIResponse getAccountN_Settings(   @PathParam("aid")                      String pAccId,
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
            SsUsrAccounts accConnected = new SsUsrAccounts();

            long lSessionUserId = gUserId;//for now
            long lTargetAccId   = Long.parseLong(pAccId);

            accConnected = Users.connectAccount(gem, lSessionUserId, lTargetAccId, gServiceGrants);
            if(accConnected==null)
            {
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
            SsUsrAccounts accConnected = new SsUsrAccounts();

            // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            //
            // This is SESSION USER specific api. Lists the accounts / branches
            // connected to the session user only. This won't give access to 
            // the root user that granted access to the account in case of
            // the user (session) is not ROOT user. In other words, delegated
            //
            // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

            long lSessionUserId = gUserId;//for now
            long lTargetAccId   = Long.parseLong(pAccId);

            accConnected = Users.connectAccount(gem, lSessionUserId, lTargetAccId, gServiceGrants);
            if(accConnected==null)
            {
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
    @Path("/veml/{aid},"
                + "{lng},"
                + "{cnt},"
                + "{bid},"
                + "{sid},"
                + "{ip},"
                + "{eml}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    //@Token(VerificationType.MUST)//if token not receive means no UserId so it fails
    public ssoAPIResponse verifyEmail(   @PathParam("aid")                          String pAccId,
                                        @PathParam("lng")                          String psLang,
                                        @PathParam("cnt")                          String psCountry,
                                        @PathParam("bid")                          String psBrowserId,
                                        @PathParam("sid")                          String psSessionId,
                                        @PathParam("ip")                           String psIP,
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
                            + "{sid}"
                            + "{pgid},"
                            //+ "{mid},"
                            + "{cur},"
                            + "{mcc},"
                            + "{cc},"
                            + "{sc},"
                            + "{coc},"
                            + "{tx},"
                            + "{txr},"
                            + "{inr},"
                            + "{eml},"
                            + "{nm}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse updateAccount(        @PathParam("aid")                          String pAccId,
                                                @PathParam("lng")                          String psLang,
                                                @PathParam("cnt")                          String psCountry,
                                                @PathParam("sid")                          String psSessionId,
                                                @PathParam("pgid")                         String psPageId,
                                                //@PathParam("mid")                          String psMerchantId,
                                                @PathParam("cur")                          String psCurrency,
                                                @PathParam("mcc")                          String psMCC,
                                                @PathParam("cc")                           String psCountryCode,
                                                @PathParam("sc")                           String psStateCode,
                                                @PathParam("coc")                          String psPlaceNameUId,
                                                @PathParam("tx")                           String psTaxInPrice,
                                                @PathParam("txr")                          String psTaxRate,
                                                @PathParam("inr")                          String psInsDiffRate,
                                                @PathParam("eml")                          String psEmail,
                                                @PathParam("nm")                           String psProfileName
                                              ) throws Exception
    {
        int iRec = 0;
        try
        {
            boolean bActivated = true;

            ssoAPIResponse rsp = new ssoAPIResponse();
            SsUsrAccounts accConnected = new SsUsrAccounts();

            long lSessionUserId = gUserId;//for now
            long lTargetAccId   = Long.parseLong(pAccId);
            
            if(lTargetAccId!=-1)//means not a new account adding in other words updating existing one
            {
                //--------------------------------------------------------------
                //
                //  UPDATE 
                //  
                //  Warning: Email can NOT be updated
                //_-------------------------------------------------------------

                accConnected = Users.connectAccount(gem, lSessionUserId, lTargetAccId, gServiceGrants);
                if(accConnected==null)
                {
                    rsp.Response = "err";
                    rsp.ResponseMsg = "Account invalid";
                    return rsp;
                }

                long lMrcId = Long.parseLong(pAccId);
                ssoMerchantPreferences mrcPrefs = new ssoMerchantPreferences();

                //mrcPrefs = AccountMisc.getAccountSettings(gem, accConnected.userId, accConnected.uid);

                //if (mrcPrefs!=null)
                //{
                    //update will be implemented here
                    Query stmt = gem.createNamedQuery("SsUsrAccounts.updatePrefs", SsUsrAccounts.class);
                    int index = 1;

                    //String sEmail = mrcPrefs.email;

                    //if (mrcPrefs.isActive.trim().equals("Y")==false)//if not activated, allow to change email
                    //    bActivated = false;

                    //if(bActivated==false)//if not activated, allow to change email
                    //    sEmail = psEmail; 

                    //stmt.SetParameter(index++, sEmail         , "EMAIL");
                    stmt.SetParameter(index++, psCurrency     , "CURRENCY");
                    stmt.SetParameter(index++, psMCC          , "MCC");
                    stmt.SetParameter(index++, psStateCode    , "STATE_CODE");
                    stmt.SetParameter(index++, psPlaceNameUId , "PLACE_NAME_UID");//county_code => PLACE UID
                    stmt.SetParameter(index++, psCountryCode  , "COUNTRY_CODE");
                    stmt.SetParameter(index++, psTaxInPrice   , "IS_TAX_INC_PRICE");
                    stmt.SetParameter(index++, psTaxRate      , "TAX_RATE");
                    stmt.SetParameter(index++, psInsDiffRate  , "INS_DIFF_RATE");

                    stmt.SetParameter(index++, accConnected.uid         , "UID");//acc Id
                    stmt.SetParameter(index++, accConnected.userId      , "USER_ID");//This param will help reset the cache

                    //psTaxInPrice
                    stmt.executeUpdate();

                //}
            }
            else
            {
                //--------------------------------------------------------------
                //
                //  ADDING NEW ACCOUNT
                //  
                //_-------------------------------------------------------------

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
                
                Signup.generateNewAccountRequest("", //ip
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
            }

            AccountMisc.resetMemoryTables4Account(gem, accConnected.userId);

            rsp.Content  = "";//Util.JSON.Convert2JSON('').toString();
            rsp.Response = "ok";
            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

}
