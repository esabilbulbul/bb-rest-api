/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package wapi.text;

import Methods.Users;
import Objects.ssoUserAccounts;
import bb.app.obj.ssoInvBrandItemCodes;
import bb.app.report.invsearch.ssoUIBalanceReport;
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
import wapi.core.UIHome;

/**
 *
 * @author Administrator
 */
@Path("/api/wa")
public class UIWhatsapp implements jeiRestInterface
{
    // SESSION VARIABLES
    BigInteger gUserId  = BigInteger.ZERO;
    EntityManager gem;
    ArrayList<String> gServiceGrants = new ArrayList<String>();

    private static final Logger logger = Logger.getLogger(UIHome.class.getName());

    public UIWhatsapp()
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
    @Path("/cctmp/{aid},"
                + "{lng},"
                + "{cnt},"
                + "{sid},"
                + "{ip},"
                + "{ky},"
                + "{pg},"
                + "{rst}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    //@Token(VerificationType.MUST)
    //@UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse WhatsAppWebHook(   @PathParam("aid")                          String pAccId,
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

            rsp.Content = "";//Util.JSON.Convert2JSON("").toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    @GET
    @Path("/cctmp/{aid},"
                + "{lng},"
                + "{cnt},"
                + "{sid},"
                + "{ip},"
                + "{ky},"
                + "{pg},"
                + "{rst}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse createCarouselTemplate(   @PathParam("aid")                          String pAccId,
                                                    @PathParam("lng")                          String psLang,
                                                    @PathParam("cnt")                          String psCountry,
                                                    @PathParam("sid")                          String psSessionId,
                                                    @PathParam("ip")                           String psIP
                                                  ) throws Exception
    {
        try
        {
            ArrayList<ssoInvBrandItemCodes> aItemsFound = new ArrayList<ssoInvBrandItemCodes>();

            Util.Tomcat.print2TomcatLog(logger, "estore.searchItems", pAccId);
            
            ssoAPIResponse rsp = new ssoAPIResponse();
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

            //rsp.Content = Util.JSON.Convert2JSON(aItemsFound).toString();
            //ArrayList<ssoUIBalanceReportGeneric> balances = new ArrayList<ssoUIBalanceReportGeneric>();

            rsp.Content = "";//Util.JSON.Convert2JSON(aRows).toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            Util.Tomcat.print2TomcatLog(logger, "estore.searchItems exception: " + e.getMessage(), pAccId);
            throw e;
        }
    }

}