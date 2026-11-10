/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package wapi.text;

import Methods.Users;
import Objects.ssoUserAccounts;
import bb.app.obj.ssoInvBrandItemCodes;
import bb.app.report.invsearch.ssoUIBalanceReport;
import bb.app.sms.SMSOps;
import static bb.app.sms.SMSOps.getSMSSettings;
import bb.app.sms.ssoSMSPackage;
import bb.app.sms.ssoSMSReportRow;
import bb.estore.ssEStoreOps;
import bb.estore.ssoEItemRow;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;
import jaxesa.annotations.Callback;
import jaxesa.annotations.Consumes;
import jaxesa.annotations.GET;
import jaxesa.annotations.MediaType;
import jaxesa.annotations.Path;
import jaxesa.annotations.PathParam;
import jaxesa.annotations.Produces;
import jaxesa.annotations.ThreadActionType;
import jaxesa.annotations.Token;
import jaxesa.annotations.UserGrants;
import jaxesa.annotations.UserRole;
import jaxesa.annotations.VerificationType;
import jaxesa.persistence.EntityManager;
import jaxesa.util.Util;
import jaxesa.webapi.ssoAPIResponse;
import restapi.jeiRestInterface;
import ss.sms.ssoSMSRequest;
import wapi.core.UIHome;

/**
 *
 * @author Administrator
 */
@Path("/api/sms")
public class UISMS implements jeiRestInterface
{
    // SESSION VARIABLES
    BigInteger gUserId  = BigInteger.ZERO;
    EntityManager gem;
    ArrayList<String> gServiceGrants = new ArrayList<String>();

    private static final Logger logger = Logger.getLogger(UIHome.class.getName());

    public UISMS()
    {
        
    }

    @Override
    public void init(String pUserId, String pBrowserId, String p3rdPartyAPIKey, EntityManager pem, String psUserRoleReqs)
    {
        try
        {
            String s = "";

            gUserId = new BigInteger(pUserId);
            gem     = pem;
            gem.SetSessionUser(pUserId);

            gServiceGrants.addAll(Arrays.asList(psUserRoleReqs.split(",")));
        }
        catch(Exception e)
        {
            
        }
    }

    @GET
    @Path("/smstst/{aid},"
                    + "{lng},"
                    + "{cnt},"
                    + "{sid},"
                    + "{ip},"
                    + "{txt}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse sendTestSMS(  @PathParam("aid")                          String pAccId,
                                        @PathParam("lng")                          String psLang,
                                        @PathParam("cnt")                          String psCountry,
                                        @PathParam("sid")                          String psSessionId,
                                        @PathParam("ip")                           String psIP,
                                        @PathParam("txt")                          String psText
                                      ) throws Exception
    {
        try
        {
            // This is the api that whatsapp calls you when a customer responds to your text message
            ssoAPIResponse rsp = new ssoAPIResponse();

            Util.Tomcat.print2TomcatLog(logger, "sms.sendTestSMS", pAccId);

            ssoUserAccounts accConnected = new ssoUserAccounts();

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

            String sResp = SMSOps.createTestSMSRequest(gem, psLang, gUserId, accConnected.uid, psText);

            rsp.Content = sResp;//Util.JSON.Convert2JSON("").toString();

            if(sResp.trim().length()>0)
                rsp.Response = "err";
            else
                rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }
    
    @GET
    @Path("/sms2cst/{aid},"
                    + "{lng},"
                    + "{cnt},"
                    + "{sid},"
                    + "{ip},"
                    + "{txt}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse sendSMS2Customers(    @PathParam("aid")                          String pAccId,
                                                @PathParam("lng")                          String psLang,
                                                @PathParam("cnt")                          String psCountry,
                                                @PathParam("sid")                          String psSessionId,
                                                @PathParam("ip")                           String psIP,
                                                @PathParam("txt")                          String psText
                                              ) throws Exception
    {
        try
        {
            // This is the api that whatsapp calls you when a customer responds to your text message
            ssoAPIResponse rsp = new ssoAPIResponse();

            Util.Tomcat.print2TomcatLog(logger, "sms.sendTestSMS", pAccId);

            ssoUserAccounts accConnected = new ssoUserAccounts();

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

            if(psText.trim().length()==0)
                throw new Exception("no text entered");

            String sResp = SMSOps.createSMSRequest4Customers(gem, psLang, gUserId, accConnected.uid, psText);

            rsp.Content = sResp;//Util.JSON.Convert2JSON("").toString();
            if(sResp.trim().length()>0)
                rsp.Response = "err";
            else
                rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    @GET
    @Path("/sms2sel/{aid},"
                    + "{lng},"
                    + "{cnt},"
                    + "{sid},"
                    + "{ip},"
                    + "{txt},"
                    + "{nms}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse sendSMS2SelectedNumbers(  @PathParam("aid")                          String pAccId,
                                                    @PathParam("lng")                          String psLang,
                                                    @PathParam("cnt")                          String psCountry,
                                                    @PathParam("sid")                          String psSessionId,
                                                    @PathParam("ip")                           String psIP,
                                                    @PathParam("txt")                          String psText,
                                                    @PathParam("nms")                          String psPhoneNumbers
                                                  ) throws Exception
    {
        try
        {
            // This is the api that whatsapp calls you when a customer responds to your text message
            ssoAPIResponse rsp = new ssoAPIResponse();

            Util.Tomcat.print2TomcatLog(logger, "sms.sendTestSMS", pAccId);

            ssoUserAccounts accConnected = new ssoUserAccounts();

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

            if(psText.trim().length()==0)
                throw new Exception("no text entered");

            String sResp = SMSOps.createSMSRequest4SelectedNumbers(gem, psLang, gUserId, accConnected.uid, psText, psPhoneNumbers);

            rsp.Content = sResp;//Util.JSON.Convert2JSON("").toString();
            if(sResp.trim().length()>0)
                rsp.Response = "err";
            else
                rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }
    
    @GET
    @Path("/gsmsc/{aid},"
                    + "{lng},"
                    + "{cnt},"
                    + "{sid},"
                    + "{ip}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse getSMSCredit( @PathParam("aid")                          String pAccId,
                                        @PathParam("lng")                          String psLang,
                                        @PathParam("cnt")                          String psCountry,
                                        @PathParam("sid")                          String psSessionId,
                                        @PathParam("ip")                           String psIP
                                      ) throws Exception
    {
        try
        {
            // This is the api that whatsapp calls you when a customer responds to your text message
            ssoAPIResponse rsp = new ssoAPIResponse();

            Util.Tomcat.print2TomcatLog(logger, "sms.getSMSCredit", pAccId);

            ssoUserAccounts accConnected = new ssoUserAccounts();

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

            ssoSMSPackage smsPackage = new ssoSMSPackage();
            
            smsPackage = getSMSSettings(gem, gUserId, accConnected.uid);

            rsp.Content = Long.toString(smsPackage.smsBalance);//Util.JSON.Convert2JSON("").toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    @GET
    @Path("/gssrp/{aid},"
                    + "{lng},"
                    + "{cnt},"
                    + "{sid},"
                    + "{ip}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse getSMSSendinrReport(  @PathParam("aid")                          String pAccId,
                                                @PathParam("lng")                          String psLang,
                                                @PathParam("cnt")                          String psCountry,
                                                @PathParam("sid")                          String psSessionId,
                                                @PathParam("ip")                           String psIP
                                             ) throws Exception
    {
        try
        {
            // This is the api that whatsapp calls you when a customer responds to your text message
            ssoAPIResponse rsp = new ssoAPIResponse();

            Util.Tomcat.print2TomcatLog(logger, "sms.getSMSSendinrReport", pAccId);

            ssoUserAccounts accConnected = new ssoUserAccounts();

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

            ArrayList<ssoSMSReportRow> smsReport = new ArrayList<ssoSMSReportRow>();

            smsReport = SMSOps.getSMSSendingReport(gem, gUserId, accConnected.uid);

            rsp.Content = Util.JSON.Convert2JSON(smsReport).toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }
    
}
