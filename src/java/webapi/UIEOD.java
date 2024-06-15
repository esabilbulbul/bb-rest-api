/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webapi;

import Methods.Users;
import bb.app.eod.ssEOD;
import bb.app.eod.ssoEODDets;
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
import jaxesa.persistence.EntityManager;
import jaxesa.util.Util;
import jaxesa.webapi.ssoAPIResponse;
import restapi.jeiRestInterface;

/**
 *
 * @author Administrator
 */
@Path("/api/biz/bb/eod")
public class UIEOD implements jeiRestInterface
{
    long gUserId  = -1;
    EntityManager gem;
    ArrayList<String>    gServiceGrants = new ArrayList<String>();

    public UIEOD()
    {
        
    }

    @Override
    public void init(String pUserId, EntityManager pem, String psUserRoleReqs)//pRolesRequired
    {
        try
        {
            String s = "";

            gem = pem;
            gUserId = Long.parseLong(pUserId);
            gem.SetSessionUser(pUserId);

            gServiceGrants.addAll(Arrays.asList(psUserRoleReqs.split(",")));

        }
        catch(Exception e)
        {
            
        }
    }
 
        /* INFO : THIS Interface identically same with "neweodentry". This is only for UI use. The other one is for internal use */
    @GET
    @Path("/neweod_ui/{aid},"
                        + "{lng},"
                        + "{cnt},"
                        + "{sid},"
                        + "{ip},"
                        + "{yy},"
                        + "{mm},"
                        + "{dd},"
                        + "{csh},"
                        + "{crd},"
                        + "{wre},"
                        + "{onl}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    public ssoAPIResponse addNewEndOfDayUI(  @PathParam("aid")                    String pAccountId,
                                             @PathParam("lng")                    String psLang,
                                             @PathParam("cnt")                    String psCountry,
                                             @PathParam("sid")                    String psSessionId,
                                             @PathParam("ip")                     String psIp,

                                             @PathParam("yy")                     String psYear,
                                             @PathParam("mm")                     String psMonth,
                                             @PathParam("dd")                     String psDay,

                                             //@PathParam("src")                          String psSource,
                                             @PathParam("csh")                          String psCashTotal,
                                             @PathParam("crd")                          String psCardTotal,
                                             @PathParam("wre")                          String psWireTotal,
                                             @PathParam("onl")                          String psInternetTotal
                                            ) throws Exception
    {
        return addNewEndOfDay(pAccountId, 
                              psLang, 
                              psCountry, 
                              psSessionId, 
                              psYear, 
                              psMonth, 
                              psDay, 
                              "", 
                              psCashTotal,
                              psCardTotal,
                              psWireTotal,
                              psInternetTotal,
                              "0");
    }

    /* INFO : THIS Interface identically same with "neweod". This is only for internal use. The other one is for UI use */
    @GET
    @Path("/neweodentry/{aid},"
                        + "{lang},"
                        + "{country},"
                        + "{sessionid},"
                        + "{year},"
                        + "{month},"
                        + "{day},"
                        + "{src},"
                        + "{csh},"
                        + "{crd},"
                        + "{wre},"
                        + "{int},"
                        + "{oth}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.NONE)//NONE for test only
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse addNewEndOfDay(   @PathParam("aid")                          String pAccountId,
                                            @PathParam("lang")                         String psLang,
                                            @PathParam("country")                      String psCountry,
                                            @PathParam("sessionid")                    String psSessionId,
                                            @PathParam("year")                         String psYear,
                                            @PathParam("month")                        String psMonth,
                                            @PathParam("day")                          String psDay,
                                            
                                            @PathParam("src")                          String psSource,
                                            @PathParam("csh")                          String psCashTotal,
                                            @PathParam("crd")                          String psCardTotal,
                                            @PathParam("wre")                          String psWireTotal,
                                            @PathParam("int")                          String psInternetTotal,
                                            @PathParam("oth")                          String psOtherTotal

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
            long lSessionUserId = gUserId;
            long lTargetAccId = Long.parseLong(pAccountId);
            SsUsrAccounts accConnected = new SsUsrAccounts();

            //long lUserId = 38482644;//for now
            //acc = Users.getUserAccount(gem, UserId, lAccId);
            accConnected = Users.connectAccount(gem, gUserId, lTargetAccId, gServiceGrants);
            if (accConnected==null)
            {
                // IMPORTANT
                //
                // RULE : A linked account can be viewed but can't be edited. 
                //

                Rsp.Response = "err";
                Rsp.ResponseMsg = "Account invalid";
                return Rsp;
            }

            //String sTot = psTot.substring(0, psTot.length()-3);//removing decimal digits (temporary solution)
            /*
            int indexDecimal = sTot.indexOf(".");
            if (indexDecimal>0)
            {
                sTot = sTot.replace(".", "");
            }

            indexDecimal = sTot.indexOf(",");
            if (indexDecimal>0)
            {
                sTot = sTot.replace(",", "");
            }
            */
            String sMonth = psMonth;
            String sDay   = psDay;
            String sYear  = psYear;

            if (psYear.trim().length()==0)
                throw new Exception("Date Format Incorrect - Month");

            if (psMonth.trim().length()==0)
                throw new Exception("Date Format Incorrect - Month");

            if (psDay.trim().length()==0)
                throw new Exception("Date Format Incorrect - Day");

            if (psYear.trim().length()<4)
                sYear = "0" + psYear;
            
            if (psMonth.trim().length()<2)
                sMonth = "0" + psMonth;

            if (psDay.trim().length()<2)
                sDay = "0" + psDay;

            boolean rc = ssEOD.addNewEOD(   gem, 
                                            lSessionUserId,
                                            accConnected.uid, 
                                            sDay, 
                                            sMonth, 
                                            sYear, 
                                            psCashTotal, 
                                            psCardTotal, 
                                            psWireTotal, 
                                            psInternetTotal, 
                                            psOtherTotal);

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
    @Path("/geodlst/{aid},"
                        + "{lng},"
                        + "{cnt},"
                        + "{sid},"
                        + "{ip},"
                        + "{ky},"
                        + "{rst},"
                        + "{alr},"
                        + "{rwi}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.NONE)//NONE for test only
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse getEndOfDayList(  @PathParam("aid")                       String pAccountId,
                                            @PathParam("lng")                       String psLang,
                                            @PathParam("cnt")                       String psCountry,
                                            @PathParam("sid")                       String psSessionId,
                                            @PathParam("ip")                        String psIP,
                                            @PathParam("ky")                        String psKeyword,
                                            @PathParam("rst")                       String pbInit,//reset cache
                                            @PathParam("alr")                       String pbAllRows,//loads max data
                                            @PathParam("rwi")                       String psRowIndex//the current row index on UI
                                            ) throws Exception
    {
        int iRec = 0;

        try
        {
            ssoAPIResponse Rsp = new ssoAPIResponse();

            // CHECK IF ACCOUNT (coming w request) VALID UNDER THE USER (COMING w TOKEN)
            SsUsrAccounts accConnected = new SsUsrAccounts();

            long lSessionUserId = gUserId;//for now
            long lTargetAccId = Long.parseLong(pAccountId);

            accConnected = Users.connectAccount(gem, gUserId, lTargetAccId, gServiceGrants);
            if (accConnected==null)
            {
                // IMPORTANT
                //
                // RULE : A linked account can be viewed but can't be edited. 
                //

                Rsp.Response = "err";
                Rsp.ResponseMsg = "Account invalid";
                return Rsp;
            }

            int lStartRowIndex = Integer.parseInt(psRowIndex);
            boolean bInit      = Boolean.parseBoolean(pbInit);
            boolean bAllRows   = Boolean.parseBoolean(pbAllRows);

            String sDateYYYYMMDD = "";
            if(psKeyword.trim().length()>0)
            {
                String[] aDateParts = psKeyword.split("\\.");
                if(aDateParts.length==1)
                {
                    sDateYYYYMMDD = aDateParts[0];
                }
                else if(aDateParts.length==2)
                {
                    sDateYYYYMMDD = aDateParts[1] + aDateParts[0];
                }
                else
                {
                    if(aDateParts[2].length()==4)//20xx
                        sDateYYYYMMDD = aDateParts[2] + aDateParts[1] + aDateParts[0];
                    else
                        sDateYYYYMMDD = "20" + aDateParts[2] + aDateParts[1] + aDateParts[0];
                }
            }

            ArrayList<ssoEODDets> eodHistory = new ArrayList<ssoEODDets>();
            eodHistory = ssEOD.getEODHistory(gem, 
                                             accConnected.uid, 
                                             bInit, 
                                             bAllRows, 
                                             lStartRowIndex,
                                             sDateYYYYMMDD);

            Rsp.Content = Util.JSON.Convert2JSON(eodHistory).toString();
            Rsp.Response = "ok";
            return Rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

}
