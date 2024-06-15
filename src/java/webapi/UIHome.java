/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webapi;

import Methods.Users;
import bb.app.account.AccountMisc;
import static bb.app.account.AccountMisc.calculateSummaryDays;
import static bb.app.account.AccountMisc.calculateSummaryWeeksOfMonth;
import bb.app.dekonts.DekontEarningStats;
import bb.app.dekonts.DekontSummary;
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
import jaxesa.persistence.DBPool;
import jaxesa.persistence.EntityManager;
import jaxesa.util.Util;
import jaxesa.webapi.ssoAPIResponse;
import misc.DekontMisc;
import restapi.jeiRestInterface;

/**
 *
 * @author Administrator
 */
@Path("/api/biz/bb/home")
public class UIHome implements jeiRestInterface
{
    // SESSION VARIABLES
    long gUserId  = -1;
    EntityManager gem;
    ArrayList<String> gServiceGrants = new ArrayList<String>();

    public UIHome()
    {
        
    }

    @Override
    public void init(String pUserId, EntityManager pem, String psUserRoleReqs)
    {
        try
        {
            String s = "";
            
            gem = pem;
            gUserId = Long.parseLong(pUserId);
            gem.SetSessionUser(pUserId);//Session User and Connected Account can be different users'

            gServiceGrants.addAll(Arrays.asList(psUserRoleReqs.split(",")));

        }
        catch(Exception e)
        {
            
        }
    }

    @GET

    //@Path("/getmonthstats/{aid},"
    @Path("/gmms/{aid},"
                + "{lng},"
                + "{cnt},"
                + "{sid}"
                + "{y},"
                + "{m},"
                + "{c}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse getMonthStats(@PathParam("aid")                    String pAccountId,
                                        @PathParam("lng")                    String psLang,
                                        @PathParam("cnt")                    String psCountry,
                                        @PathParam("sid")                    String psSessionId,
                                        //@PathParam("mrcid")                        String psMrcId,//accid
                                        @PathParam("y")                      String psYear,
                                        @PathParam("m")                      String psMonth,
                                        @PathParam("c")                      String psCurrency
                                     ) throws Exception
    {
        int iRec = 0;
        try
        {
            ssoAPIResponse Rsp = new ssoAPIResponse();
            String sTargetMonth = psMonth;
            DekontSummary summary = new DekontSummary();
            
            SsUsrAccounts accConnected = new SsUsrAccounts();

            long lSessionUserId = gUserId;//for now
            long lTargetAccId   = Long.parseLong(pAccountId);

            accConnected = Users.connectAccount(gem, gUserId, lTargetAccId, gServiceGrants);
            if(accConnected==null)
            {
                Rsp.Response = "err";
                Rsp.ResponseMsg = "Account invalid";
                return Rsp;
            }
            
            long lAccId = accConnected.uid;
            // MrcId = acc.uid
            ssoMerchantPreferences mrcPrefs = new ssoMerchantPreferences();
            mrcPrefs = AccountMisc.getAccountSettings(gem, gUserId, lAccId);

            String baseCurrency   = mrcPrefs.CurrencyCode;
            String targetCurrency = psCurrency;


            String sCurrentYear  = Util.DateTime.GetDateTime_s().substring(0,4);
            sTargetMonth = Util.Str.leftPad(sTargetMonth, "0", 2);

            summary.currentMonth.days  = calculateSummaryDays(  gem, 
                                                                accConnected.uid, 
                                                                baseCurrency, 
                                                                targetCurrency, 
                                                                "-1", 
                                                                sTargetMonth);

            summary.currentMonth.weeks = calculateSummaryWeeksOfMonth(  gem, 
                                                                        accConnected.uid, 
                                                                        baseCurrency, 
                                                                        targetCurrency, 
                                                                        "-1", 
                                                                        sTargetMonth);

            summary.currentMonth.dayAvgs = bb.app.account.AccountMisc.calculateSummaryTargetMonthDayAverages(gem, 
                                                                                                             accConnected.uid, 
                                                                                                             baseCurrency, 
                                                                                                             targetCurrency, 
                                                                                                             "-1", 
                                                                                                             sTargetMonth);

            Rsp.Content = Util.JSON.Convert2JSON(summary).toString();
            Rsp.Response = "ok";
            return Rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    @GET
    @Path("/grc/{aid},"
                + "{lng},"
                + "{cnt},"
                + "{sid}"
                //+ "{mrcid},"
                + "{bc}"
                + "{y}"
                + "{mn}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse getReportByCell(  @PathParam("aid")                        String pAccId,//this is mrcId. User Id comes in token
                                            @PathParam("lng")                       String psLang,
                                            @PathParam("cnt")                       String psCountry,
                                            @PathParam("sid")                       String psSessionId,
                                            //@PathParam("mrcid")                   String psMrcId,
                                            @PathParam("bc")                        String psBankCode,
                                            @PathParam("y")                         String psYear,
                                            @PathParam("mn")                        String psMonthNo
                                            ) throws Exception
    {
        int iRec = 0;
        try
        {
            //--------------------------------------------------
            //
            // This only calculates the subtotals that will be 
            // used in graph chart
            //
            //--------------------------------------------------
            ssoAPIResponse Rsp = new ssoAPIResponse();

            SsUsrAccounts accConnected = new SsUsrAccounts();

            long lSessionUserId = gUserId;//for now
            long lTargetAccId   = Long.parseLong(pAccId);

            accConnected = Users.connectAccount(gem, gUserId, lTargetAccId, gServiceGrants);
            if(accConnected==null)
            {
                Rsp.Response = "err";
                Rsp.ResponseMsg = "Account invalid";
                return Rsp;
            }

            DekontSummary summary = new DekontSummary();

            String baseCurrency   = "TL";//accConnected.currencyCode;//"TL";//For now
            String targetCurrency = "TL";
            long lMrcId = Long.parseLong(pAccId);

            //int iBankCode = Integer.parseInt(psBankCode);
            //int iYear     = Integer.parseInt(psYear);
            //int iMonth    = Integer.parseInt(psMonthNo);
            //summary = bb.app.dekonts.DekontMisc.calculateSummary(em,iBankCode, iYear, iMonth);
            summary.banks = bb.app.account.AccountMisc.calculateSummaryBankSubtotals(gem, lMrcId, baseCurrency, targetCurrency, psYear, -1);

            //long lUID = em.persist(newReport);
            Rsp.Content = Util.JSON.Convert2JSON(summary).toString();
            Rsp.Response = "ok";
            return Rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    @GET
    @Path("/grb/{aid},"
                + "{lng},"
                + "{cnt},"
                + "{sid}"
                + "{bc}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    public ssoAPIResponse getReportByBank(  @PathParam("aid")                     String psAccId,
                                            @PathParam("lng")                     String psLang,
                                            @PathParam("cnt")                     String psCountry,
                                            @PathParam("sid")                     String psSessionId,
                                            @PathParam("bc")                      String psBankCode) throws Exception
    {
        int iRec = 0;
        try
        {
            ssoAPIResponse Rsp = new ssoAPIResponse();
            
            /*
            EntityManager em = DBPool.getSessionConnection(psUser_SessionInfo, Util.Methods.hash());

            DekontSummary summary = new DekontSummary();

            int iBankCode = Integer.parseInt(psBankCode);
            //int iYear     = Integer.parseInt(psYear);
            //int iMonth    = Integer.parseInt(psMonthNo);
            long pMrcId = Long.parseLong(psMrcId);
            String baseCurrency   = "TL";//For now
            String targetCurrency = "TL";

            summary = bb.app.account.AccountMisc.calculateSummary(em, pMrcId, "", baseCurrency, targetCurrency, iBankCode, -1, -1, 30);//default 30 but this part won't be used
            */
            //long lUID = em.persist(newReport);
            //Rsp.Content = Util.JSON.Convert2JSON(summary).toString();
            Rsp.Response = "ok";
            return Rsp;

        }
        catch(Exception e)
        {
            throw e;
        }
    }

    @GET
    @Path("/gmis/{aid},"
                        + "{lng},"
                        + "{cnt},"
                        + "{sid},"
                        + "{cur},"
                        + "{yel}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse getMyStats(   @PathParam("aid")                         String pAccountId,// This is accId linked to the userId (for example; merchantId)
                                        @PathParam("lng")                         String psLang,
                                        @PathParam("cnt")                         String psCountry,
                                        @PathParam("sid")                         String psSessionId,
                                        @PathParam("cur")                         String psCurrency,
                                        @PathParam("yel")                         String psYearEarningLength//inmonths
                                     ) throws Exception
    {
        int iRec = 0;
        try
        {
            ssoAPIResponse Rsp = new ssoAPIResponse();

            //EntityManager em = DBPool.getSessionConnection(psUser_SessionInfo, Util.Methods.hash());
            //EntityManager em = gem;

            SsUsrAccounts accConnected = new SsUsrAccounts();

            long lSessionUserId = gUserId;//for now
            long lTargetAccId   = Long.parseLong(pAccountId);


            accConnected = Users.connectAccount(gem, gUserId, lTargetAccId, gServiceGrants);
            if(accConnected==null)
            {
                Rsp.Response = "err";
                Rsp.ResponseMsg = "Account invalid";
                return Rsp;
            }

            ssoMerchantPreferences mrcPrefs = new ssoMerchantPreferences();
            //mrcPrefs = DekontMisc.getMerchantPreferences(gem, 38482644, 38482645);
            
            //mrcPrefs = AccountMisc.getAccountSettings(gem, accConnected.userId, accConnected.uid);
            mrcPrefs = DekontMisc.getShortMerchantPreferences(gem, accConnected.userId, accConnected.uid);
            
            String baseCurrency   = mrcPrefs.CurrencyCode;
            String targetCurrency = psCurrency;
            int    iYearEarningLength = Integer.parseInt(psYearEarningLength);

            DekontSummary summary = new DekontSummary();
            
            summary = bb.app.account.AccountMisc.calculateSummary(gem, 
                                                                  accConnected.uid, 
                                                                  mrcPrefs.MerchantName, 
                                                                  baseCurrency, 
                                                                  targetCurrency, 
                                                                  -1, 
                                                                  -1, 
                                                                  -1, 
                                                                  iYearEarningLength);//-1 all in
            summary.accPrefs = mrcPrefs;

            //long lUID = em.persist(newReport);
            Rsp.Content = Util.JSON.Convert2JSON(summary).toString();
            Rsp.Response = "ok";
            return Rsp;

        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
            throw e;
        }
    }
    

    @GET
    @Path("/gys/{aid},"
                    + "{lng},"
                    + "{cnt},"
                    + "{sid},"
                    //+ "{mrcid},"
                    + "{cur},"
                    + "{yel}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse getYearEarningStats(  @PathParam("aid")                       String pAccId,//mrcId comes here, user Id comes in token
                                                @PathParam("lng")                       String psLang,
                                                @PathParam("cnt")                       String psCountry,
                                                @PathParam("sid")                       String psSessionId,
                                                @PathParam("cur")                       String psCurrency,
                                                @PathParam("yel")                       String psYearEarningLength//inmonths
                                             ) throws Exception
    {
        int iRec = 0;
        try
        {
            ssoAPIResponse Rsp = new ssoAPIResponse();

            //long lMrcId = Long.parseLong(psMrcId);
            SsUsrAccounts accConnected = new SsUsrAccounts();

            long lSessionUserId = gUserId;//for now
            long lTargetAccId   = Long.parseLong(pAccId);

            accConnected = Users.connectAccount(gem, gUserId, lTargetAccId, gServiceGrants);
            if(accConnected==null)
            {
                Rsp.Response = "err";
                Rsp.ResponseMsg = "Account invalid";
                return Rsp;
            }
            
            ssoMerchantPreferences mrcPrefs = new ssoMerchantPreferences();
            mrcPrefs = AccountMisc.getAccountSettings(gem, accConnected.userId, accConnected.uid);
            
            String baseCurrency   = mrcPrefs.CurrencyCode;
            String targetCurrency = psCurrency;
            int    iYearEarningLength = Integer.parseInt(psYearEarningLength);
            
            //DekontSummary summary = new DekontSummary();
            ArrayList<DekontEarningStats> AllEarningStats = new ArrayList<DekontEarningStats>();

            AllEarningStats = bb.app.account.AccountMisc.calculateSummary_YearEarningOnly(gem, 
                                                                                          accConnected.uid, 
                                                                                          mrcPrefs.MerchantName, 
                                                                                          baseCurrency, 
                                                                                          targetCurrency, 
                                                                                          -1, 
                                                                                          -1, 
                                                                                          -1, 
                                                                                          iYearEarningLength);//-1 all in

            //long lUID = em.persist(newReport);
            Rsp.Content = Util.JSON.Convert2JSON(AllEarningStats).toString();
            Rsp.Response = "ok";
            return Rsp;

        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    
}
