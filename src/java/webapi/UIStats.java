/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webapi;

import Methods.Users;
import bb.app.obj.ssoInvBrandItemCodes;
import bb.app.obj.ssoVendorItemStats;
import bb.app.obj.ssoVendorStats;
import bb.app.vendor.VendorOps;
import entity.acc.SsAccInvVendorStats;
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
@Path("/api/biz/bb/stts")
public class UIStats implements jeiRestInterface
{
    // SESSION VARIABLES
    long gUserId  = -1;
    EntityManager gem;
    ArrayList<String> gServiceGrants = new ArrayList<String>();

    public UIStats()
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
    @Path("/gis/{aid},"
                + "{lng},"
                + "{cnt},"
                + "{sid},"
                + "{ip},"
                + "{bid},"
                + "{icd},"
                + "{rst}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse getItemStatistics( @PathParam("aid")                             String pAccId,
                                                @PathParam("lng")                          String psLang,
                                                @PathParam("cnt")                          String psCountry,
                                                @PathParam("sid")                          String psSessionId,
                                                @PathParam("ip")                           String psIP,
                                                @PathParam("bid")                          String psBrandId,
                                                @PathParam("icd")                          String psItemCode,//if empty that means all items
                                                @PathParam("rst")                          String psReset
                                              ) throws Exception
    {
        try
        {
            ArrayList<ssoInvBrandItemCodes> aItemsFound = new ArrayList<ssoInvBrandItemCodes>();

            ssoAPIResponse rsp = new ssoAPIResponse();
            SsUsrAccounts accConnected = new SsUsrAccounts();

            long lSessionUserId = gUserId;//for now
            long lTargetAccId   = Long.parseLong(pAccId);
            long lBrandId       = Long.parseLong(psBrandId);

            boolean bReset = false;
            if(psReset.equals("Y")==true)
                bReset = true;

            accConnected = Users.connectAccount(gem, lSessionUserId, lTargetAccId, gServiceGrants);
            if(accConnected==null)
            {
                rsp.Response = "err";
                rsp.ResponseMsg = "Account invalid";
                return rsp;
            }

            ArrayList<ssoVendorItemStats> aItemStats = new ArrayList<ssoVendorItemStats>();

            aItemStats = VendorOps.getItemStats4Vendor(gem, accConnected.uid, lBrandId, psItemCode, bReset);

            rsp.Content = Util.JSON.Convert2JSON(aItemStats).toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    @GET
    @Path("/gvs/{aid},"
                + "{lng},"
                + "{cnt},"
                + "{sid},"
                + "{ip},"
                + "{bid},"
                + "{rst}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse getVendorStatistics(  @PathParam("aid")                          String pAccId,
                                                @PathParam("lng")                          String psLang,
                                                @PathParam("cnt")                          String psCountry,
                                                @PathParam("sid")                          String psSessionId,
                                                @PathParam("ip")                           String psIP,
                                                @PathParam("bid")                          String psBrandId,
                                                @PathParam("rst")                          String psReset
                                              ) throws Exception
    {
        try
        {
            ArrayList<ssoInvBrandItemCodes> aItemsFound = new ArrayList<ssoInvBrandItemCodes>();

            ssoAPIResponse rsp = new ssoAPIResponse();
            SsUsrAccounts accConnected = new SsUsrAccounts();

            long lSessionUserId = gUserId;//for now
            long lTargetAccId   = Long.parseLong(pAccId);
            long lBrandId       = Long.parseLong(psBrandId);

            boolean bReset = false;
            if(psReset.equals("Y")==true)
                bReset = true;

            accConnected = Users.connectAccount(gem, lSessionUserId, lTargetAccId, gServiceGrants);
            if(accConnected==null)
            {
                rsp.Response = "err";
                rsp.ResponseMsg = "Account invalid";
                return rsp;
            }

            SsAccInvVendorStats oStats = new SsAccInvVendorStats();
            ssoVendorStats vendorStats = new ssoVendorStats();

            oStats = VendorOps.getVendorStats(gem, accConnected.uid, lBrandId, bReset);
            
            if(oStats!=null)
            {
                vendorStats = VendorOps.convertUIVendorStats(oStats);

                rsp.Content = Util.JSON.Convert2JSON(vendorStats).toString();
                rsp.Response = "ok";
            }
            else
            {
                rsp.Content = "Vendor not found";
                rsp.Response = "nok";
            }
                
            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

}

