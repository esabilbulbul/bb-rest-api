/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webapi;

import Methods.Users;
import Objects.ssoMessage2Us;
import bb.app.dekonts.DekontEarningStats;
import bb.app.dekonts.DekontFields;
import bb.app.dekonts.DekontMethods;
import static bb.app.dekonts.DekontMisc.calculateSummaryBankSubtotals;
import static bb.app.dekonts.DekontMisc.calculateSummaryDays;
import static bb.app.dekonts.DekontMisc.calculateSummaryWeeksOfMonth;
import bb.app.dekonts.DekontSummary;
import bb.app.pages.ParamsMisc;
import bb.app.pages.ssoCityCode;
import bb.app.pages.ssoPostCode;
import bb.app.pages.ssoCountryCodes;
import bb.app.pages.ssoCountyCode;
import bb.app.pages.ssoMCC;
import bb.app.pages.ssoMerchant;
import bb.app.pages.ssoMerchantPreferences;
import bb.app.pages.ssoPageParams;
import entity.mrc.SsMrcDataEod;
import entity.mrc.SsMrcDataPosTxn;
import entity.mrc.SsMrcMerchants;
import entity.prm.SsPrmCountryStates;
import entity.prm.SsPrmCountryCodes;
import entity.prm.SsPrmCountryPostcodes;
import entity.user.SsUsrAccounts;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import jaxesa.annotations.Consumes;
import jaxesa.annotations.GET;
import jaxesa.annotations.MediaType;
import jaxesa.annotations.Path;
import jaxesa.annotations.PathParam;
import jaxesa.annotations.Produces;
import jaxesa.annotations.Token;
import jaxesa.annotations.VerificationType;
import jaxesa.defs.ShipShuk;
import jaxesa.persistence.DBPool;
import jaxesa.persistence.EntityManager;
import jaxesa.util.Util;
import jaxesa.webapi.ssoAPIResponse;
import misc.DekontMisc;
import org.apache.pdfbox.pdmodel.PDDocument;
import redis.clients.jedis.Jedis;
import restapi.jeiRestInterface;

/**
 *
 * @author Administrator
 */
@Path("/bulbuller/dekont")
public class UI implements jeiRestInterface
{
    long gUserId  = -1;
    EntityManager gem;

    @Override
    public void init(String pUserId, EntityManager pem)
    {
        try
        {
            String s = "";

            gUserId = Long.parseLong(pUserId);
            gem = pem;
            gem.SetSessionUser(pUserId);

        }
        catch(Exception e)
        {
            
        }
    }
    
    @GET
    @Path("/api/getpageparams_mystats/{aid},"
                                    + "{lang},"
                                    + "{country},"
                                    + "{sessionid}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    public ssoAPIResponse getMerchantListOfUsers(   @PathParam("aid")                           String psUser_SessionInfo,//user account id
                                                    @PathParam("lang")                         String psLang,
                                                    @PathParam("country")                      String psCountry,
                                                    @PathParam("sessionid")                    String psSessionId
                                                 ) throws Exception
    {
        int iRec = 0;
        try
        {
            ssoAPIResponse Rsp = new ssoAPIResponse();

            //EntityManager em = DBPool.getSessionConnection(psUser_SessionInfo, Util.Methods.hash());
            EntityManager em = gem;

            //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            // 
            // Page Params for "mystats"
            // 1. List of Merchants linked to the user
            // 2. Market Data 
            //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            ArrayList<ssoMerchant> mrcList = new ArrayList<ssoMerchant>();
            
            // This gets linked accounts/retailers to the users 
            mrcList = DekontMisc.getListOfMerchants4User(gem, gUserId);

            Rsp.Content = Util.JSON.Convert2JSON(mrcList).toString();
            Rsp.Response = "ok";

            return Rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    
    @GET
    @Path("/api/getmonthstats/{aid},"
                                + "{lang},"
                                + "{country},"
                                + "{sessionid}"
                                //+ "{mrcid},"
                                + "{year},"
                                + "{month},"
                                + "{currency}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    public ssoAPIResponse getMonthStats(  @PathParam("aid")                            String pAccountId,
                                            @PathParam("lang")                         String psLang,
                                            @PathParam("country")                      String psCountry,
                                            @PathParam("sessionid")                    String psSessionId,
                                            //@PathParam("mrcid")                        String psMrcId,//accid
                                            @PathParam("year")                         String psYear,
                                            @PathParam("month")                        String psMonth,
                                            @PathParam("currency")                     String psCurrency
                                         ) throws Exception
    {
        int iRec = 0;
        try
        {
            ssoAPIResponse Rsp = new ssoAPIResponse();
            String sTargetMonth = psMonth;
            
            SsUsrAccounts acc = new SsUsrAccounts();

            DekontSummary summary = new DekontSummary();

            //EntityManager em = DBPool.getSessionConnection(psUser_SessionInfo, Util.Methods.hash());
            //long lMrcId = Long.parseLong(pAccountId);

            long lAccId = Long.parseLong(pAccountId);
            acc = Users.getAccountOrLinkedAccountInfo(gem, gUserId, lAccId);
            if (acc==null)
            {
                Rsp.Response = "err";
                Rsp.ResponseMsg = "Account invalid";
                return Rsp;
            }

            // MrcId = acc.uid
            ssoMerchantPreferences mrcPrefs = new ssoMerchantPreferences();
            mrcPrefs = DekontMisc.getMerchantPreferences(gem, gUserId, acc.uid);

            String baseCurrency   = mrcPrefs.CurrencyCode;
            String targetCurrency = psCurrency;


            String sCurrentYear  = Util.DateTime.GetDateTime_s().substring(0,4);
            sTargetMonth = Util.Str.leftPad(sTargetMonth, "0", 2);
            summary.currentMonth.days  = calculateSummaryDays(gem, acc.uid, baseCurrency, targetCurrency, "-1", sTargetMonth);

            summary.currentMonth.weeks = calculateSummaryWeeksOfMonth(gem, acc.uid, baseCurrency, targetCurrency, "-1", sTargetMonth);

            summary.currentMonth.dayAvgs = bb.app.dekonts.DekontMisc.calculateSummaryTargetMonthDayAverages(gem, acc.uid, baseCurrency, targetCurrency, "-1", sTargetMonth);

            Rsp.Content = Util.JSON.Convert2JSON(summary).toString();
            Rsp.Response = "ok";
            return Rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    /* INFO : THIS Interface identically same with "neweodentry". This is only for UI use. The other one is for internal use */
    @GET
    @Path("/api/neweod_ui/{aid},"
                        + "{lang},"
                        + "{country},"
                        + "{sessionid}"
                        //+ "{mrcid},"
                        + "{year},"
                        + "{month},"
                        + "{day},"
                        + "{tot},"
                        + "{onl}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    public ssoAPIResponse addNewEndOfDayUI(  @PathParam("aid")                         String pAccountId,
                                            @PathParam("lang")                         String psLang,
                                            @PathParam("country")                      String psCountry,
                                            @PathParam("sessionid")                    String psSessionId,
                                            //@PathParam("mrcid")                        String psMrcId,//=>accId
                                            @PathParam("year")                         String psYear,
                                            @PathParam("month")                        String psMonth,
                                            @PathParam("day")                          String psDay,
                                            @PathParam("tot")                          String psTot,//TOTAL-ALL
                                            @PathParam("onl")                          String psTotOnline
                                            ) throws Exception
    {
        return addNewEndOfDay(pAccountId, psLang, psCountry, psSessionId, psYear, psMonth, psDay, psTot, psTotOnline);
    }
    
    /* INFO : THIS Interface identically same with "neweod". This is only for internal use. The other one is for UI use */
    @GET
    @Path("/api/neweodentry/{aid},"
                                + "{lang},"
                                + "{country},"
                                + "{sessionid}"
                                //+ "{mrcid},"
                                + "{year},"
                                + "{month},"
                                + "{day},"
                                + "{tot},"
                                + "{onl}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    public ssoAPIResponse addNewEndOfDay(   @PathParam("aid")                           String pAccountId,
                                            @PathParam("lang")                         String psLang,
                                            @PathParam("country")                      String psCountry,
                                            @PathParam("sessionid")                    String psSessionId,
                                            //@PathParam("mrcid")                        String psMrcId,//accid
                                            @PathParam("year")                         String psYear,
                                            @PathParam("month")                        String psMonth,
                                            @PathParam("day")                          String psDay,
                                            @PathParam("tot")                          String psTot,//TOTAL-ALL
                                            @PathParam("onl")                          String psTotOnline
                                            ) throws Exception
    {
        int iRec = 0;
        try
        {
            //New Kasa Entry
            //--------------------------------------------------
            //
            // This only calculates the subtotals that will be 
            // used in graph chart
            //
            //--------------------------------------------------
            ssoAPIResponse Rsp = new ssoAPIResponse();

            // CHECK IF ACCOUNT (coming w request) VALID UNDER THE USER (COMING w TOKEN)
            long lAccId = Long.parseLong(pAccountId);
            SsUsrAccounts acc = new SsUsrAccounts();

            acc = Users.getUserAccount(gem, gUserId, lAccId);
            if (acc==null)
            {
                // IMPORTANT
                //
                // RULE : A linked account can be viewed but can't be edited. 
                //

                Rsp.Response = "err";
                Rsp.ResponseMsg = "Account invalid";
                return Rsp;
            }

            String sTot = psTot.substring(0, psTot.length()-3);//removing decimal digits (temporary solution)
            sTot = sTot.replace(",", "");
            sTot = sTot.replace(".", "");

            String sDate = psYear + "-" + psMonth + "-" + psDay;
            boolean rc = bb.app.dekonts.DekontMisc.isEODAdded(gem, pAccountId, sDate);
            SsMrcDataEod cashTxn = new SsMrcDataEod();
            if (rc==false)
            {
                //Calculate Last 12 months total (from txnDate)

                //New Insert
                cashTxn.stat        = 1;
                cashTxn.accountId   = pAccountId;
                cashTxn.txnAmount   = sTot;

                cashTxn.txnDesc     = "";
                cashTxn.txnDate     = sDate;
                cashTxn.txnMonthNo  = psMonth;
                cashTxn.txnType     = "KASA";
                //cashTxn.txnAmount   = psTot;

                long lUID = gem.persist(cashTxn);
            }
            else
            {
                rc = bb.app.dekonts.DekontMisc.updateEOD(gem, pAccountId, sDate, sTot);
            }

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
    @Path("/api/getreportcell/{aid},"
                                        + "{lang},"
                                        + "{country},"
                                        + "{sessionid}"
                                        //+ "{mrcid},"
                                        + "{bnkcode}"
                                        + "{year}"
                                        + "{monthno}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    public ssoAPIResponse getReportByCell( @PathParam("aid")                       String pAccId,//this is mrcId. User Id comes in token
                                            @PathParam("lang")                         String psLang,
                                            @PathParam("country")                      String psCountry,
                                            @PathParam("sessionid")                    String psSessionId,
                                            //@PathParam("mrcid")                        String psMrcId,
                                            @PathParam("bnkcode")                      String psBankCode,
                                            @PathParam("year")                         String psYear,
                                            @PathParam("monthno")                      String psMonthNo
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

            SsUsrAccounts acc = new SsUsrAccounts();
            
            //EntityManager em = DBPool.getSessionConnection(psUser_SessionInfo, Util.Methods.hash());
            long lAccId = Long.parseLong(pAccId);
            acc = Users.getAccountOrLinkedAccountInfo(gem, gUserId, lAccId);
            if (acc==null)
            {
                Rsp.Response = "err";
                Rsp.ResponseMsg = "Account invalid";
                return Rsp;
            }

            DekontSummary summary = new DekontSummary();

            String baseCurrency   = acc.currencyCode;//"TL";//For now
            String targetCurrency = "TL";
            long lMrcId = Long.parseLong(pAccId);

            //int iBankCode = Integer.parseInt(psBankCode);
            //int iYear     = Integer.parseInt(psYear);
            //int iMonth    = Integer.parseInt(psMonthNo);
            //summary = bb.app.dekonts.DekontMisc.calculateSummary(em,iBankCode, iYear, iMonth);
            summary.banks = bb.app.dekonts.DekontMisc.calculateSummaryBankSubtotals(gem, lMrcId, baseCurrency, targetCurrency, psYear, -1);

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
    @Path("/api/getmrcprefs/{aid},"
                            + "{lang},"
                            + "{country},"//browser
                            + "{sessionid}"
                            + "{pgid},"
                            //+ "{mid}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    public ssoAPIResponse getMerchantPreferences(  @PathParam("aid")                           String pAccId,
                                                    @PathParam("lang")                         String psLang,
                                                    @PathParam("country")                      String psCountry,
                                                    @PathParam("sessionid")                    String psSessionId,
                                                    @PathParam("pgid")                         String psPageId
                                                    //@PathParam("mid")                          String psMerchantId
                                                  ) throws Exception
    {
        int iRec = 0;
        try
        {
            ssoAPIResponse Rsp = new ssoAPIResponse();

            //EntityManager em = DBPool.getSessionConnection(psUser_SessionInfo, Util.Methods.hash());
            EntityManager em = gem;//asigned at init
            
            long lMrcId = Long.parseLong(pAccId);
            ssoMerchantPreferences mrcPrefs = new ssoMerchantPreferences();
            
            mrcPrefs = DekontMisc.getShortMerchantPreferences(em, gUserId, lMrcId);
            
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
    @Path("/api/uprefs/{aid},"
                            + "{lang},"
                            + "{country},"//browser
                            + "{sessionid}"
                            + "{pgid},"
                            //+ "{mid},"
                            + "{cur},"
                            + "{mcc},"
                            + "{cc},"
                            + "{sc},"
                            + "{coc},"
                            + "{ae}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    public ssoAPIResponse updatePreferences(    @PathParam("aid")                          String pAccId,
                                                @PathParam("lang")                         String psLang,
                                                @PathParam("country")                      String psCountry,
                                                @PathParam("sessionid")                    String psSessionId,
                                                @PathParam("pgid")                         String psPageId,
                                                //@PathParam("mid")                          String psMerchantId,
                                                @PathParam("cur")                          String psCurrency,
                                                @PathParam("mcc")                          String psMCC,
                                                @PathParam("cc")                           String psCountryCode,
                                                @PathParam("sc")                           String psStateCode,
                                                @PathParam("coc")                          String psCountyCode,
                                                @PathParam("ae")                           String psAvgMonthExpense
                                              ) throws Exception
    {
        int iRec = 0;
        try
        {
            ssoAPIResponse Rsp = new ssoAPIResponse();

            //EntityManager em = DBPool.getSessionConnection(psUser_SessionInfo, Util.Methods.hash());
            EntityManager em = gem;

            long lMrcId = Long.parseLong(pAccId);
            ssoMerchantPreferences mrcPrefs = new ssoMerchantPreferences();

            mrcPrefs = DekontMisc.getMerchantPreferences(em, gUserId, lMrcId);

            if (mrcPrefs!=null)
            {
                //update will be implemented here
                SsUsrAccounts account = new SsUsrAccounts();
                account.uid = mrcPrefs.Id;
                account.byuser = pAccId;
                account.version = mrcPrefs.version;
                account.currencyCode = psCurrency;
                account.mcc          = psMCC;
                account.countryCode  = psCountryCode;
                account.stateCode    = psStateCode;
                account.countyCode   = psCountyCode;

                em.merge(account, "SsUsrAccounts.updatePrefs");

                /*
                mrcPrefs.CurrencyCode    = psCurrency;
                mrcPrefs.MCC         = psMCC;
                mrcPrefs.CountryCode = psCountryCode;
                mrcPrefs.StateCode   = psStateCode;
                mrcPrefs.countyCode  = psCountyCode;// NOT Count(r)y
                mrcPrefs.expensesProfileId = 0;

                em.merge(mrcPrefs, "SsMrcPreferences.updateAll");
                */
            }
            
            Rsp.Content  = "";//Util.JSON.Convert2JSON('').toString();
            Rsp.Response = "ok";
            return Rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    @GET
    @Path("/api/gcnts/{userid},"
                            + "{lang},"
                            + "{country},"//browser
                            + "{sessionid},"
                            + "{pgid},"
                            + "{countrycode},"//selected
                            + "{citycode}"//selected
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    public ssoAPIResponse getCountyCodes(  @PathParam("userid")                       String psUser_SessionInfo,
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

            ArrayList<ssoCountyCode> countyCodes = new ArrayList<ssoCountyCode>();
            //ArrayList<ssoPostCode> UICodes = new ArrayList<ssoPostCode>();//to the client

            countyCodes = ParamsMisc.getCountyCodes(em, sCountryCode, sCityCode);

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
    
    
    @GET
    @Path("/api/gpstcds/{userid},"
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

            postCodes = ParamsMisc.getPostCodes(em, sCountryCode, sCityCode);

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
    
    /*
        THIS FUNCTION SENDS THE PARAMETERS TO THE PAGE AUTO SELECTION OF COUNTRY
    */
    @GET
    @Path("/api/gprms/{userid},"
                            + "{lang},"
                            + "{country},"//browser
                            + "{sessionid}"
                            + "{pgid}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    public ssoAPIResponse getParams(@PathParam("userid")                        String psUser_SessionInfo,
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

            countryCodes = ParamsMisc.getCountryCodes(em);

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
            cityCodes = ParamsMisc.getCityCodes(em, psCountry);
            for (ssoCityCode CN:cityCodes)
            {
                ssoCityCode newCode = new ssoCityCode();
                newCode.code = CN.code;
                newCode.lang = "en";
                newCode.name = CN.name;

                pageParams.Cities.add(newCode);
            }

            // MCC 
            //------------------------------------------------------------------
            
            ArrayList<ssoMCC> MCCs = new ArrayList<ssoMCC>();
            MCCs = ParamsMisc.getMCCs(em);
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
    @Path("/api/getreportbank/{userid},"
                                        + "{lang},"
                                        + "{country},"
                                        + "{sessionid}"
                                        + "{mrcid},"
                                        + "{bnkcode}"
                                        //+ "{year}"
                                        //+ "{monthno}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    public ssoAPIResponse getReportByBank(@PathParam("userid")                       String psUser_SessionInfo,
                                            @PathParam("lang")                         String psLang,
                                            @PathParam("country")                      String psCountry,
                                            @PathParam("sessionid")                    String psSessionId,
                                            @PathParam("mrcid")                        String psMrcId,
                                            @PathParam("bnkcode")                      String psBankCode) throws Exception
    {
        int iRec = 0;
        try
        {
            ssoAPIResponse Rsp = new ssoAPIResponse();

            EntityManager em = DBPool.getSessionConnection(psUser_SessionInfo, Util.Methods.hash());

            DekontSummary summary = new DekontSummary();

            int iBankCode = Integer.parseInt(psBankCode);
            //int iYear     = Integer.parseInt(psYear);
            //int iMonth    = Integer.parseInt(psMonthNo);
            long pMrcId = Long.parseLong(psMrcId);
            String baseCurrency   = "TL";//For now
            String targetCurrency = "TL";

            summary = bb.app.dekonts.DekontMisc.calculateSummary(em, pMrcId, "", baseCurrency, targetCurrency, iBankCode, -1, -1, 30);//default 30 but this part won't be used

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

    // IMPORTANT
    //
    // RULE : A linked account can be viewed but can't be edited. 
    //
    @GET
    @Path("/api/getmystats/{aid},"
                        + "{lang},"
                        + "{country},"
                        + "{sessionid},"
                        //+ "{mrcid},"
                        + "{currency},"
                        + "{yel}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    public ssoAPIResponse getMyStats(   @PathParam("aid")                          String pAccountId,// This is accId linked to the userId (for example; merchantId)
                                        @PathParam("lang")                         String psLang,
                                        @PathParam("country")                      String psCountry,
                                        @PathParam("sessionid")                    String psSessionId,
                                        @PathParam("currency")                     String psCurrency,
                                        @PathParam("yel")                          String psYearEarningLength//inmonths
                                     ) throws Exception
    {
        int iRec = 0;
        try
        {
            ssoAPIResponse Rsp = new ssoAPIResponse();

            //EntityManager em = DBPool.getSessionConnection(psUser_SessionInfo, Util.Methods.hash());
            //EntityManager em = gem;

            SsUsrAccounts acc = new SsUsrAccounts();

            long lAccId = Long.parseLong(pAccountId);
            acc = Users.getAccountOrLinkedAccountInfo(gem, gUserId, lAccId);
            if (acc==null)
            {
                Rsp.Response = "err";
                Rsp.ResponseMsg = "Account invalid";
                return Rsp;
            }

            ssoMerchantPreferences mrcPrefs = new ssoMerchantPreferences();
            //mrcPrefs = DekontMisc.getMerchantPreferences(gem, 38482644, 38482645);
            mrcPrefs = DekontMisc.getMerchantPreferences(gem, gUserId, lAccId);
            
            String baseCurrency   = mrcPrefs.CurrencyCode;
            String targetCurrency = psCurrency;
            int    iYearEarningLength = Integer.parseInt(psYearEarningLength);

            DekontSummary summary = new DekontSummary();

            summary = bb.app.dekonts.DekontMisc.calculateSummary(gem, lAccId, mrcPrefs.MerchantName, baseCurrency, targetCurrency, -1, -1, -1, iYearEarningLength);//-1 all in

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
    @Path("/api/getyestats/{aid},"
                        + "{lang},"
                        + "{country},"
                        + "{sessionid},"
                        //+ "{mrcid},"
                        + "{currency},"
                        + "{yel}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    public ssoAPIResponse getYearEarningStats(  @PathParam("aid")                       String pAccId,//mrcId comes here, user Id comes in token
                                                @PathParam("lang")                         String psLang,
                                                @PathParam("country")                      String psCountry,
                                                @PathParam("sessionid")                    String psSessionId,
                                                //@PathParam("mrcid")                        String psMrcId,
                                                @PathParam("currency")                     String psCurrency,
                                                @PathParam("yel")                          String psYearEarningLength//inmonths
                                             ) throws Exception
    {
        int iRec = 0;
        try
        {
            ssoAPIResponse Rsp = new ssoAPIResponse();

            //EntityManager em = DBPool.getSessionConnection(psUser_SessionInfo, Util.Methods.hash());
            //EntityManager em = gem;

            //long lMrcId = Long.parseLong(psMrcId);
            SsUsrAccounts acc = new SsUsrAccounts();
            
            long lAccId = Long.parseLong(pAccId);
            acc = Users.getAccountOrLinkedAccountInfo(gem, gUserId, lAccId);
            if (acc==null)
            {
                Rsp.Response = "err";
                Rsp.ResponseMsg = "Account invalid";
                return Rsp;
            }
            
            ssoMerchantPreferences mrcPrefs = new ssoMerchantPreferences();
            mrcPrefs = DekontMisc.getMerchantPreferences(gem, gUserId, lAccId);
            
            String baseCurrency   = mrcPrefs.CurrencyCode;
            String targetCurrency = psCurrency;
            int    iYearEarningLength = Integer.parseInt(psYearEarningLength);
            
            //DekontSummary summary = new DekontSummary();
            ArrayList<DekontEarningStats> AllEarningStats = new ArrayList<DekontEarningStats>();

            AllEarningStats = bb.app.dekonts.DekontMisc.calculateSummary_YearEarningOnly(gem, lAccId, mrcPrefs.MerchantName, baseCurrency, targetCurrency, -1, -1, -1, iYearEarningLength);//-1 all in

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
    
    //Sample: http://localhost:8080/bb-wapi-dekont-converter/rest/bulbuller/dekont/api/showreport/?userid=1&lang=en&country=tr&sessionid=123&mrcid=1&repid=2&filename=abc.pdf
    @GET
    @Path("/api/processfile/{userid},"
                            + "{lang},"
                            + "{country},"
                            + "{sessionid},"
                            + "{mrcid},"
                            + "{repid},"
                            + "{filename},"
                            + "{currency}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    //This method will no long be USED
    //This used process the file thru an API and send the summary back to the UI.
    //However, the process split into 2. 1st sys process the file
    //2nd UI request for the summary
    public ssoAPIResponse processfile(  @PathParam("userid")                       String psUser_SessionInfo,
                                        @PathParam("lang")                         String psLang,
                                        @PathParam("country")                      String psCountry,
                                        @PathParam("sessionid")                    String psSessionId,
                                        @PathParam("mrcid")                        String psMrcId,
                                        @PathParam("repid")                        String psReportId,
                                        @PathParam("filename")                     String psFileName,
                                        @PathParam("currency")                     String psCurrency
                                     ) throws Exception
    {
        int iRec = 0;
        try
        {
            ssoAPIResponse Rsp = new ssoAPIResponse();

            EntityManager em = DBPool.getSessionConnection(psUser_SessionInfo, Util.Methods.hash());

            String sUserId = "123466";//for now
            String sFilePath = DekontMisc.getFilePathbyId(psFileName, "/Users/esabil/Documents/uploads", sUserId);

            //String sInFilePath  = "/Users/esabil/Documents/files/KUVEYT_Musterino_6667543_Ekno_1_2019910152032_ekstre.pdf";
            String sInFilePath  = sFilePath;
            //String sInFilePath  = "/Users/esabil/Documents/files/KUVEYT_Musterino_6667543_Ekno_1_2019910152032_ekstre.pdf";
            String sOutFilePath = "/Users/esabil/Documents/files/web_dekont_summary.txt";//output file

            long lMrcId = Long.parseLong(psMrcId);
            ssoMerchantPreferences mrcPrefs = new ssoMerchantPreferences();
            mrcPrefs = DekontMisc.getMerchantPreferences(em, gUserId, lMrcId);

            String baseCurrency   = mrcPrefs.CurrencyCode;
            String targetCurrency = "USD";

            //File x = new File(sInFilePath);
            PDDocument document = null;
            document = PDDocument.load(new File(sInFilePath));
            //document = PDDocument.load(new File(sInFilePath));
            document.getClass();

            ArrayList<DekontFields> Records = DekontMethods.processFile(sInFilePath, sOutFilePath, true, "NECATİ BÜLBÜL");

            SsMrcDataPosTxn newPosTxn = new SsMrcDataPosTxn();
            for(DekontFields recordN: Records)
            {
                iRec++;
                
                if (iRec==7)
                    iRec = iRec;
                
                newPosTxn.stat       = 1;
                newPosTxn.accountId  = psMrcId;
                newPosTxn.reportId   = psReportId;
                newPosTxn.fileName   = psFileName;
                newPosTxn.bankCode   = Integer.parseInt(recordN.bankCode);
                newPosTxn.txnAmount  = recordN.Amount.replace(",", "");//this is needed because txn_amount_num col calculated without comma
                newPosTxn.txnBalance = recordN.Balance;
                newPosTxn.txnDate    = recordN.Date;
                newPosTxn.txnDesc    = recordN.Desc;
                newPosTxn.txnMonthNo = recordN.MonthNo;
                newPosTxn.txnReleaseDate = recordN.Release;
                newPosTxn.txnTime    = recordN.Time;
                newPosTxn.txnTraceNo = recordN.TraceNo;
                newPosTxn.txnType    = recordN.TxnType;

                //Check if the record exist
                boolean rc = DekontMisc.isRecordAdded(em, newPosTxn.txnTraceNo, newPosTxn.bankCode );

                if (rc==false)
                {
                    long lUID = em.persist(newPosTxn);
                }
                
            }

            //newReport.txnAmount = "1.10";
            DekontSummary summary = new DekontSummary();

            //DEPRECIATED (THIS API)
            //summary = bb.app.dekonts.DekontMisc.calculateSummary(em, lMrcId, "", baseCurrency, targetCurrency, -1, -1, -1);//-1 all in

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

}


