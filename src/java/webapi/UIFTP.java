/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webapi;

import bb.app.account.AccountMisc;
import bb.app.dekonts.DekontFields;
import bb.app.dekonts.DekontMethods;
import bb.app.dekonts.DekontSummary;
import bb.app.obj.ssoMerchantPreferences;
import entity.mrc.SsMrcDataPosTxn;
import java.io.File;
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
import jaxesa.webapi.ssoAPIResponse;
import misc.DekontMisc;
import org.apache.pdfbox.pdmodel.PDDocument;
import restapi.jeiRestInterface;

/**
 *
 * @author Administrator
 */
@Path("/api/biz/bb/ftp")
public class UIFTP implements jeiRestInterface
{
    long gUserId  = -1;
    EntityManager gem;

    public UIFTP()
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
            gem.SetSessionUser(pUserId);

        }
        catch(Exception e)
        {
            
        }
    }

    //Sample: http://localhost:8080/bb-wapi-dekont-converter/rest/bulbuller/dekont/api/showreport/?userid=1&lang=en&country=tr&sessionid=123&mrcid=1&repid=2&filename=abc.pdf
    @GET
    @Path("/pfl/{userid},"
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
            mrcPrefs = AccountMisc.getAccountSettings(em, gUserId, lMrcId);

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
