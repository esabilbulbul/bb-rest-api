/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webapi;

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
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import jaxesa.annotations.Consumes;
import jaxesa.annotations.GET;
import jaxesa.annotations.MediaType;
import jaxesa.annotations.Path;
import jaxesa.annotations.PathParam;
import jaxesa.annotations.Produces;
import jaxesa.persistence.DBPool;
import jaxesa.persistence.EntityManager;
import jaxesa.util.Util;
import jaxesa.webapi.sso_APIResponse;
import misc.DekontMisc;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 *
 * @author Administrator
 */
@Path("/bulbuller/dekont")
public class UI 
{

    @GET
    @Path("/api/getpageparams_mystats/{userid},"
                                    + "{lang},"
                                    + "{country},"
                                    + "{sessionid}"
         )
    @Consumes()
    @Produces(MediaType.JSON)
    public sso_APIResponse getMerchantListOfUsers(  @PathParam("userid")                       String psUser_SessionInfo,
                                                    @PathParam("lang")                         String psLang,
                                                    @PathParam("country")                      String psCountry,
                                                    @PathParam("sessionid")                    String psSessionId
                                                 ) throws Exception
    {
        int iRec = 0;
        try
        {
            sso_APIResponse Rsp = new sso_APIResponse();

            EntityManager em = DBPool.getSessionConnection(psUser_SessionInfo, Util.Methods.hash());

            //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            // 
            // Page Params for "mystats"
            // 1. List of Merchants linked to the user
            // 2. Market Data 
            //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            ArrayList<ssoMerchant> mrcList = new ArrayList<ssoMerchant>();
            
            mrcList = DekontMisc.getListOfMerchants4User(em, 0);

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
    @Path("/api/getmonthstats/{userid},"
                                + "{lang},"
                                + "{country},"
                                + "{sessionid}"
                                + "{mrcid},"
                                + "{year},"
                                + "{month},"
                                + "{currency}"
         )
    @Consumes()
    @Produces(MediaType.JSON)
    public sso_APIResponse getMonthStats(  @PathParam("userid")                       String psUser_SessionInfo,
                                            @PathParam("lang")                         String psLang,
                                            @PathParam("country")                      String psCountry,
                                            @PathParam("sessionid")                    String psSessionId,
                                            @PathParam("mrcid")                        String psMrcId,
                                            @PathParam("year")                         String psYear,
                                            @PathParam("month")                        String psMonth,
                                            @PathParam("currency")                     String psCurrency
                                         ) throws Exception
    {
        int iRec = 0;
        try
        {
            sso_APIResponse Rsp = new sso_APIResponse();
            String sTargetMonth = psMonth;
            
            DekontSummary summary = new DekontSummary();

            EntityManager em = DBPool.getSessionConnection(psUser_SessionInfo, Util.Methods.hash());
            
            long lMrcId = Long.parseLong(psMrcId);
            SsMrcMerchants mrcPrefs = new SsMrcMerchants();
            mrcPrefs = DekontMisc.getMerchantPreferences(em, lMrcId);
            
            String baseCurrency   = mrcPrefs.currency;
            String targetCurrency = psCurrency;


            String sCurrentYear  = Util.DateTime.GetDateTime_s().substring(0,4);
            sTargetMonth = Util.Str.leftPad(sTargetMonth, "0", 2);
            summary.currentMonth.days  = calculateSummaryDays(em, baseCurrency, targetCurrency, "-1", sTargetMonth);

            summary.currentMonth.weeks = calculateSummaryWeeksOfMonth(em, baseCurrency, targetCurrency, "-1", sTargetMonth);
            
            summary.currentMonth.dayAvgs = bb.app.dekonts.DekontMisc.calculateSummaryTargetMonthDayAverages(em, baseCurrency, targetCurrency, "-1", sTargetMonth);

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
    @Path("/api/neweodentry/{userid},"
                                + "{lang},"
                                + "{country},"
                                + "{sessionid}"
                                + "{mrcid},"
                                + "{year},"
                                + "{month},"
                                + "{day},"
                                + "{tot}"
         )
    @Consumes()
    @Produces(MediaType.JSON)
    public sso_APIResponse addNewEndOfDay(  @PathParam("userid")                       String psUser_SessionInfo,
                                            @PathParam("lang")                         String psLang,
                                            @PathParam("country")                      String psCountry,
                                            @PathParam("sessionid")                    String psSessionId,
                                            @PathParam("mrcid")                        String psMrcId,
                                            @PathParam("year")                         String psYear,
                                            @PathParam("month")                        String psMonth,
                                            @PathParam("day")                          String psDay,
                                            @PathParam("tot")                          String psTot
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
            sso_APIResponse Rsp = new sso_APIResponse();

            EntityManager em = DBPool.getSessionConnection(psUser_SessionInfo, Util.Methods.hash());

            String sDate = psYear + "-" + psMonth + "-" + psDay;
            boolean rc = bb.app.dekonts.DekontMisc.isEODAdded(em, "-1", sDate);
            SsMrcDataEod cashTxn = new SsMrcDataEod();
            if (rc==false)
            {
                //New Insert
                cashTxn.stat        = 1;
                cashTxn.mrcId       = "1";
                cashTxn.txnAmount   = psTot;

                cashTxn.txnDesc     = "";
                cashTxn.txnDate     = sDate;
                cashTxn.txnMonthNo  = psMonth;
                cashTxn.txnType     = "KASA";
                cashTxn.txnAmount   = psTot;

                long lUID = em.persist(cashTxn);
            }
            else
            {
                rc = bb.app.dekonts.DekontMisc.updateEOD(em, "-1", sDate, psTot);
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
    @Path("/api/getreportcell/{userid},"
                                        + "{lang},"
                                        + "{country},"
                                        + "{sessionid}"
                                        + "{mrcid},"
                                        + "{bnkcode}"
                                        + "{year}"
                                        + "{monthno}"
         )
    @Consumes()
    @Produces(MediaType.JSON)
    public sso_APIResponse getReportByCell( @PathParam("userid")                       String psUser_SessionInfo,
                                            @PathParam("lang")                         String psLang,
                                            @PathParam("country")                      String psCountry,
                                            @PathParam("sessionid")                    String psSessionId,
                                            @PathParam("mrcid")                        String psMrcId,
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
            sso_APIResponse Rsp = new sso_APIResponse();

            EntityManager em = DBPool.getSessionConnection(psUser_SessionInfo, Util.Methods.hash());

            DekontSummary summary = new DekontSummary();

            String baseCurrency   = "TL";//For now
            String targetCurrency = "TL";

            //int iBankCode = Integer.parseInt(psBankCode);
            //int iYear     = Integer.parseInt(psYear);
            //int iMonth    = Integer.parseInt(psMonthNo);
            //summary = bb.app.dekonts.DekontMisc.calculateSummary(em,iBankCode, iYear, iMonth);
            summary.banks = bb.app.dekonts.DekontMisc.calculateSummaryBankSubtotals(em, baseCurrency, targetCurrency, psYear, -1);

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
    @Path("/api/getmrcprefs/{userid},"
                            + "{lang},"
                            + "{country},"//browser
                            + "{sessionid}"
                            + "{pgid},"
                            + "{mid}"
         )
    @Consumes()
    @Produces(MediaType.JSON)
    public sso_APIResponse getMerchantPreferences(  @PathParam("userid")                       String psUser_SessionInfo,
                                                    @PathParam("lang")                         String psLang,
                                                    @PathParam("country")                      String psCountry,
                                                    @PathParam("sessionid")                    String psSessionId,
                                                    @PathParam("pgid")                         String psPageId,
                                                    @PathParam("mid")                          String psMerchantId
                                                  ) throws Exception
    {
        int iRec = 0;
        try
        {
            sso_APIResponse Rsp = new sso_APIResponse();

            EntityManager em = DBPool.getSessionConnection(psUser_SessionInfo, Util.Methods.hash());
            
            long lMrcId = Long.parseLong(psMerchantId);
            ssoMerchantPreferences mrcPrefs = new ssoMerchantPreferences();
            
            mrcPrefs = DekontMisc.getShortMerchantPreferences(em, lMrcId);
            
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
    @Path("/api/uprefs/{userid},"
                            + "{lang},"
                            + "{country},"//browser
                            + "{sessionid}"
                            + "{pgid},"
                            + "{cur},"
                            + "{mcc},"
                            + "{cc},"
                            + "{sc},"
                            + "{coc},"
                            + "{ae}"
         )
    @Consumes()
    @Produces(MediaType.JSON)
    public sso_APIResponse updatePreferences(   @PathParam("userid")                       String psUser_SessionInfo,
                                                @PathParam("lang")                         String psLang,
                                                @PathParam("country")                      String psCountry,
                                                @PathParam("sessionid")                    String psSessionId,
                                                @PathParam("pgid")                         String psPageId,
                                                @PathParam("mid")                          String psMerchantId,
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
            sso_APIResponse Rsp = new sso_APIResponse();

            EntityManager em = DBPool.getSessionConnection(psUser_SessionInfo, Util.Methods.hash());

            long lMrcId = Long.parseLong(psMerchantId);
            SsMrcMerchants mrcPrefs = new SsMrcMerchants();
            
            mrcPrefs = DekontMisc.getMerchantPreferences(em, lMrcId);
            
            if (mrcPrefs!=null)
            {
                mrcPrefs.currency    = psCurrency;
                mrcPrefs.mcc         = psMCC;
                mrcPrefs.countryCode = psCountryCode;
                mrcPrefs.stateCode   = psStateCode;
                mrcPrefs.countyCode  = psCountyCode;// NOT Count(r)y
                mrcPrefs.expensesProfileId = 0;

                em.merge(mrcPrefs, "SsMrcPreferences.updateAll");
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
    @Produces(MediaType.JSON)
    public sso_APIResponse getCountyCodes(  @PathParam("userid")                       String psUser_SessionInfo,
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
            sso_APIResponse Rsp = new sso_APIResponse();

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
    @Produces(MediaType.JSON)
    public sso_APIResponse getPostCodes(    @PathParam("userid")                       String psUser_SessionInfo,
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
            sso_APIResponse Rsp = new sso_APIResponse();

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
    @Produces(MediaType.JSON)
    public sso_APIResponse getParams(@PathParam("userid")                        String psUser_SessionInfo,
                                     @PathParam("lang")                         String psLang,
                                     @PathParam("country")                      String psCountry,
                                     @PathParam("sessionid")                    String psSessionId,
                                     @PathParam("pgid")                         String psPageId
                                   ) throws Exception
    {
        int iRec = 0;
        try
        {
            sso_APIResponse Rsp = new sso_APIResponse();

            EntityManager em = DBPool.getSessionConnection(psUser_SessionInfo, Util.Methods.hash());

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
    @Produces(MediaType.JSON)
    public sso_APIResponse getReportByBank(@PathParam("userid")                       String psUser_SessionInfo,
                                            @PathParam("lang")                         String psLang,
                                            @PathParam("country")                      String psCountry,
                                            @PathParam("sessionid")                    String psSessionId,
                                            @PathParam("mrcid")                        String psMrcId,
                                            @PathParam("bnkcode")                      String psBankCode) throws Exception
    {
        int iRec = 0;
        try
        {
            sso_APIResponse Rsp = new sso_APIResponse();

            EntityManager em = DBPool.getSessionConnection(psUser_SessionInfo, Util.Methods.hash());

            DekontSummary summary = new DekontSummary();

            int iBankCode = Integer.parseInt(psBankCode);
            //int iYear     = Integer.parseInt(psYear);
            //int iMonth    = Integer.parseInt(psMonthNo);
            String baseCurrency   = "TL";//For now
            String targetCurrency = "TL";

            summary = bb.app.dekonts.DekontMisc.calculateSummary(em,baseCurrency, targetCurrency, iBankCode, -1, -1);

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
    @Path("/api/getmystats/{userid},"
                        + "{lang},"
                        + "{country},"
                        + "{sessionid},"
                        + "{mrcid},"
                        + "{currency}"
         )
    @Consumes()
    @Produces(MediaType.JSON)
    public sso_APIResponse getMyStats(  @PathParam("userid")                       String psUser_SessionInfo,
                                        @PathParam("lang")                         String psLang,
                                        @PathParam("country")                      String psCountry,
                                        @PathParam("sessionid")                    String psSessionId,
                                        @PathParam("mrcid")                        String psMrcId,
                                        @PathParam("currency")                     String psCurrency
                                     ) throws Exception
    {
        int iRec = 0;
        try
        {
            sso_APIResponse Rsp = new sso_APIResponse();

            EntityManager em = DBPool.getSessionConnection(psUser_SessionInfo, Util.Methods.hash());

            long lMrcId = Long.parseLong(psMrcId);
            SsMrcMerchants mrcPrefs = new SsMrcMerchants();
            mrcPrefs = DekontMisc.getMerchantPreferences(em, lMrcId);
            
            String baseCurrency   = mrcPrefs.currency;
            String targetCurrency = psCurrency;

            DekontSummary summary = new DekontSummary();

            summary = bb.app.dekonts.DekontMisc.calculateSummary(em, baseCurrency, targetCurrency, -1, -1, -1);//-1 all in

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
    @Produces(MediaType.JSON)
    public sso_APIResponse processfile( @PathParam("userid")                       String psUser_SessionInfo,
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
            sso_APIResponse Rsp = new sso_APIResponse();

            EntityManager em = DBPool.getSessionConnection(psUser_SessionInfo, Util.Methods.hash());
            
            String sUserId = "123466";//for now
            String sFilePath = DekontMisc.getFilePathbyId(psFileName, "/Users/esabil/Documents/uploads", sUserId);
            
            //String sInFilePath  = "/Users/esabil/Documents/files/KUVEYT_Musterino_6667543_Ekno_1_2019910152032_ekstre.pdf";
            String sInFilePath  = sFilePath;
            //String sInFilePath  = "/Users/esabil/Documents/files/KUVEYT_Musterino_6667543_Ekno_1_2019910152032_ekstre.pdf";
            String sOutFilePath = "/Users/esabil/Documents/files/web_dekont_summary.txt";//output file

            long lMrcId = Long.parseLong(psMrcId);
            SsMrcMerchants mrcPrefs = new SsMrcMerchants();
            mrcPrefs = DekontMisc.getMerchantPreferences(em, lMrcId);
            
            String baseCurrency   = mrcPrefs.currency;
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
                newPosTxn.mrcId      = psMrcId;
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

            summary = bb.app.dekonts.DekontMisc.calculateSummary(em, baseCurrency, targetCurrency, -1, -1, -1);//-1 all in

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


