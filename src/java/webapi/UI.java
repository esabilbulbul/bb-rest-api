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
import entity.mrc.SsMrcDataEod;
import entity.mrc.SsMrcDataEod;
import entity.mrc.SsMrcDataPosTxn;
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
    @Path("/api/getmonthstats/{userid},"
                                + "{lang},"
                                + "{country},"
                                + "{sessionid}"
                                + "{mrcid},"
                                + "{year},"
                                + "{month}"
         )
    @Consumes()
    @Produces(MediaType.JSON)
    public sso_APIResponse getMonthStats(  @PathParam("userid")                       String psUser_SessionInfo,
                                            @PathParam("lang")                         String psLang,
                                            @PathParam("country")                      String psCountry,
                                            @PathParam("sessionid")                    String psSessionId,
                                            @PathParam("mrcid")                        String psMrcId,
                                            @PathParam("year")                         String psYear,
                                            @PathParam("month")                        String psMonth
                                         ) throws Exception
    {
        int iRec = 0;
        try
        {
            sso_APIResponse Rsp = new sso_APIResponse();
            String sTargetMonth = psMonth;
            
            DekontSummary summary = new DekontSummary();

            EntityManager em = DBPool.getSessionConnection(psUser_SessionInfo, Util.Methods.hash());
            
            //Decide the year
            // If the month is larger than this year's month then the year goes back as reference 
            /*
            int iTargetMonth = Integer.parseInt(sTargetMonth);
            String sTargetYear = "";
            String sCurrentYear  = Util.DateTime.GetDateTime_s().substring(0,4);
            int iCurrentYear = Integer.parseInt(sCurrentYear);
            String sCurrentMonth = Util.DateTime.GetDateTime_s().substring(4,6);
            int iCurrentMonth = Integer.parseInt(sCurrentMonth);
            
            if(iTargetMonth>iCurrentMonth)
            {
                sTargetYear  = Integer.toString(iCurrentYear - 1);
            }
            else
            {
                sTargetYear = sCurrentYear;
            }
            */

            String sCurrentYear  = Util.DateTime.GetDateTime_s().substring(0,4);
            sTargetMonth = Util.Str.leftPad(sTargetMonth, "0", 2);
            summary.currentMonth.days  = calculateSummaryDays(em, "-1", sTargetMonth);

            summary.currentMonth.weeks = calculateSummaryWeeksOfMonth(em, "-1", sTargetMonth);
            
            summary.currentMonth.dayAvgs = bb.app.dekonts.DekontMisc.calculateSummaryTargetMonthDayAverages(em, sCurrentYear, sTargetMonth);

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

            //int iBankCode = Integer.parseInt(psBankCode);
            //int iYear     = Integer.parseInt(psYear);
            //int iMonth    = Integer.parseInt(psMonthNo);
            //summary = bb.app.dekonts.DekontMisc.calculateSummary(em,iBankCode, iYear, iMonth);
            summary.banks = bb.app.dekonts.DekontMisc.calculateSummaryBankSubtotals(em, psYear, -1);

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
            summary = bb.app.dekonts.DekontMisc.calculateSummary(em,iBankCode, -1, -1);

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
    
    
    //Sample: http://localhost:8080/bb-wapi-dekont-converter/rest/bulbuller/dekont/api/showreport/?userid=1&lang=en&country=tr&sessionid=123&mrcid=1&repid=2&filename=abc.pdf
    
    @GET
    @Path("/api/processfile/{userid},"
                            + "{lang},"
                            + "{country},"
                            + "{sessionid}"
                            + "{mrcid}"
                            + "{repid}"
                            + "{filename}"
         )
    @Consumes()
    @Produces(MediaType.JSON)
    public sso_APIResponse processfile(  @PathParam("userid")                       String psUser_SessionInfo,
                                        @PathParam("lang")                         String psLang,
                                        @PathParam("country")                      String psCountry,
                                        @PathParam("sessionid")                    String psSessionId,
                                        @PathParam("mrcid")                        String psMrcId,
                                        @PathParam("repid")                        String psReportId,
                                        @PathParam("filename")                     String psFileName
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


        // pass the path to the file as a parameter 
            //FileReader fr = new FileReader(sInFilePath); 

            //int i; 
            //while ((i=fr.read()) != -1) 
              //System.out.print((char) i); 

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

            summary = bb.app.dekonts.DekontMisc.calculateSummary(em, -1, -1, -1);//-1 all in

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


