/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webapi;

import bb.app.dekonts.DekontFields;
import bb.app.dekonts.DekontMethods;
import bb.app.dekonts.DekontSummary;
import bb.app.dekonts.MonthSummary;
import entity.mrc.SsMrcPosReports;
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

/**
 *
 * @author Administrator
 */
@Path("/bulbuller/dekont")
public class UI 
{
    
    //Sample: http://localhost:8080/bb-wapi-dekont-converter/rest/bulbuller/dekont/api/showreport/?userid=1&lang=en&country=tr&sessionid=123&mrcid=1&repid=2&filename=abc.pdf
    
    @GET
    @Path("/api/showreport/{userid},"
                            + "{lang},"
                            + "{country},"
                            + "{sessionid}"
                            + "{mrcid}"
                            + "{repid}"
                            + "{filename}"
         )
    @Consumes()
    @Produces(MediaType.JSON)
    public sso_APIResponse showReport(  @PathParam("userid")                       String psUser_SessionInfo,
                                        @PathParam("lang")                         String psLang,
                                        @PathParam("country")                      String psCountry,
                                        @PathParam("sessionid")                    String psSessionId,
                                        @PathParam("mrcid")                        String psMrcId,
                                        @PathParam("repid")                        String psReportId,
                                        @PathParam("filename")                     String psFileName
                                     ) throws Exception
    {
        sso_APIResponse Rsp = new sso_APIResponse();
        
        EntityManager em = DBPool.getSessionConnection(psUser_SessionInfo, Util.Methods.hash());
        
        //String sInFilePath  = "/Users/esabil/Documents/files/KUVEYT_Musterino_6667543_Ekno_3900_201991015217_ekstre.pdf";
        //String sOutFilePath = "/Users/esabil/Documents/files/web_dekont_summary.txt";//output file
        String sInFilePath  = "C:\\NEOTEMP\\CODE\\SHIPSHUK\\files\\Musterino_6667543_Ekno_101_201993154652_ekstre.pdf";
        String sOutFilePath = "C:/NEOTEMP/CODE/SHIPSHUK/files/web_dekont_summary.txt";
        ArrayList<DekontFields> Records = DekontMethods.processFile(sInFilePath, sOutFilePath, true);

        SsMrcPosReports newReport = new SsMrcPosReports();
        for(DekontFields recordN: Records)
        {
            newReport.mrcId      = psMrcId;
            newReport.reportId   = psReportId;
            newReport.fileName   = psFileName;
            
            newReport.txnAmount  = recordN.Amount;
            newReport.txnBalance = recordN.Balance;
            newReport.txnDate    = recordN.Date;
            newReport.txnDesc    = recordN.Desc;
            newReport.txnMonthNo = recordN.MonthNo;
            newReport.txnReleaseDate = recordN.Release;
            newReport.txnTime    = recordN.Time;
            newReport.txnTraceNo = recordN.TraceNo;
            newReport.txnType    = recordN.TxnType;

            long lUID = em.persist(newReport);
        }

        //newReport.txnAmount = "1.10";
        DekontSummary summary         = new DekontSummary();
        MonthSummary NewMonthSummary  = new MonthSummary();
        MonthSummary TotMonthSummary  = new MonthSummary();
        NewMonthSummary.MonthNo        = 1;//jan
        NewMonthSummary.TotAll         = "120";
        NewMonthSummary.TotInstallment = "50";
        NewMonthSummary.TotUpfront     = "70";
        summary.LastReportName = "Dosya1";
        summary.MonthsLastReport.add(NewMonthSummary);
        summary.MonthsAllReports.add(TotMonthSummary);
        //long lUID = em.persist(newReport);
        //Rsp.Content = 
        Rsp.Content  = Util.JSON.Convert2JSON(summary).toString();
        Rsp.Response = "ok";

        return Rsp;
    }
    
}


