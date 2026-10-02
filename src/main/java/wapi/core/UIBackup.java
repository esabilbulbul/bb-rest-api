/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package wapi.core;

import Methods.Users;
import Objects.ssoUserAccounts;
import bb.app.account.AccountMisc;
import bb.app.account.ssoVendorStatementBills;
import bb.app.backup.BackupOps;
import bb.app.obj.backup.ssoBackupRow;
import bb.app.obj.backup.ssoInvBackupStatsItm;
import bb.app.obj.backup.ssoInvBackupStatsVnd;
import bb.app.obj.backup.ssoVndBackupSalesSummary;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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
@Path("/api/bckp")

public class UIBackup implements jeiRestInterface
{
    BigInteger gUserId  = BigInteger.ZERO;
    EntityManager gem;
    ArrayList<String> gServiceGrants = new ArrayList<String>();

    public UIBackup()
    {
        
    }

    @Override
    public void init(String pUserId, String pBrowserId, String p3rdPartyAPIKey, EntityManager pem, String psUserRoleReqs)
    {
        try
        {
            String s = "";

            gUserId = new BigInteger(pUserId);
            gem = pem;
            gem.SetSessionUser(pUserId);

            gServiceGrants.addAll(Arrays.asList(psUserRoleReqs.split(",")));
        }
        catch(Exception e)
        {
            
        }
    }

    @GET
    @Path("/gvsb/{aid},"
                + "{lng},"
                + "{cnt},"
                + "{sid},"
                + "{ip},"
                + "{vid}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse getVendorStmtBills(   @PathParam("aid")                          String pAccId,
                                                @PathParam("lng")                          String psLang,
                                                @PathParam("cnt")                          String psCountry,
                                                @PathParam("sid")                          String psSessionId,
                                                @PathParam("ip")                           String psIP,
                                                @PathParam("vid")                          String psVendorId
                                             ) throws Exception
    {
        try
        {
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

            BigInteger lVendorId = new BigInteger(psVendorId);

            // 4. Bills / Statements (txn_bills)
            //------------------------------------------------------
            ArrayList<ssoVendorStatementBills> bills = new ArrayList<ssoVendorStatementBills>();
            bills = AccountMisc.getStatementBills4Brand(gem,
                                                        accConnected.userId,
                                                        accConnected.uid,
                                                        lVendorId,
                                                        10//default last 10 years
                                                        );//-1 WILL BE FIXED

            rsp.Content = Util.JSON.Convert2JSON(bills).toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    @GET
    @Path("/gvsd/{aid},"
                + "{lng},"
                + "{cnt},"
                + "{sid},"
                + "{ip},"
                + "{vid},"
                + "{vnd}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse getVendorStats(   @PathParam("aid")                          String pAccId,
                                            @PathParam("lng")                          String psLang,
                                            @PathParam("cnt")                          String psCountry,
                                            @PathParam("sid")                          String psSessionId,
                                            @PathParam("ip")                           String psIP,
                                            @PathParam("vid")                          String psVendorId,
                                            @PathParam("vnd")                          String psVendor
                                         ) throws Exception
    {
        try
        {
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

            BigInteger lVendorId = new BigInteger(psVendorId);

            // 4. Bills / Statements (txn_bills)
            //------------------------------------------------------
            //Map<String, ssoInvBackupStatsVnd> vendor = new HashMap<>();//year, vndobj
            ArrayList<ssoBackupRow> backup = new ArrayList<ssoBackupRow>();
            backup = BackupOps.getVendorStatsData(  gem,
                                                    accConnected.uid,
                                                    lVendorId,
                                                    psVendor
                                                    );//-1 WILL BE FIXED

            rsp.Content = Util.JSON.Convert2JSON(backup).toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }
    
    @GET
    @Path("/gvif/{aid},"
                + "{lng},"
                + "{cnt},"
                + "{sid},"
                + "{ip},"
                + "{vid},"
                + "{vnd}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse getVendorInventoryFiles(  @PathParam("aid")                          String pAccId,
                                                    @PathParam("lng")                          String psLang,
                                                    @PathParam("cnt")                          String psCountry,
                                                    @PathParam("sid")                          String psSessionId,
                                                    @PathParam("ip")                           String psIP,
                                                    @PathParam("vid")                          String psVendorId,
                                                    @PathParam("vnd")                          String psVendor
                                                 ) throws Exception
    {
        try
        {
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

            BigInteger lVendorId = new BigInteger(psVendorId);

            // 4. Bills / Statements (txn_bills)
            //------------------------------------------------------
            ArrayList<ssoBackupRow> backup = new ArrayList<ssoBackupRow>();
            backup = BackupOps.getVendorItemStatsData(gem,
                                                        accConnected.uid,
                                                        lVendorId,
                                                        psVendor
                                                        );//-1 WILL BE FIXED


            rsp.Content = Util.JSON.Convert2JSON(backup).toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    @GET
    @Path("/gvsf/{aid},"
                + "{lng},"
                + "{cnt},"
                + "{sid},"
                + "{ip},"
                + "{vid},"
                + "{vnd}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse getVendorSalesSummaryFiles(   @PathParam("aid")                          String pAccId,
                                                        @PathParam("lng")                          String psLang,
                                                        @PathParam("cnt")                          String psCountry,
                                                        @PathParam("sid")                          String psSessionId,
                                                        @PathParam("ip")                           String psIP,
                                                        @PathParam("vid")                          String psVendorId,
                                                        @PathParam("vnd")                          String psVendor
                                                     ) throws Exception
    {
        try
        {
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

            BigInteger lVendorId = new BigInteger(psVendorId);

            // 4. Bills / Statements (txn_bills)
            //------------------------------------------------------
            ArrayList<ssoVndBackupSalesSummary> backup = new ArrayList<>();
            backup = BackupOps.getVendorSalesSummaryData(gem,
                                                        accConnected.uid,
                                                        lVendorId,
                                                        psVendor
                                                        );//-1 WILL BE FIXED


            rsp.Content = Util.JSON.Convert2JSON(backup).toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

}


