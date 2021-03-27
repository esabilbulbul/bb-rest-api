/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webapi;

import Methods.Users;
import bb.app.inv.InventoryOps;
import bb.app.pages.ssoInvBrandItemCodes;
import bb.app.pages.ssoInvCategory;
import bb.app.pages.ssoInvParams;
import entity.user.SsUsrAccounts;
import java.util.ArrayList;
import jaxesa.annotations.Consumes;
import jaxesa.annotations.GET;
import jaxesa.annotations.MediaType;
import jaxesa.annotations.Path;
import jaxesa.annotations.PathParam;
import jaxesa.annotations.Produces;
import jaxesa.annotations.Token;
import jaxesa.annotations.VerificationType;
import jaxesa.persistence.EntityManager;
import jaxesa.util.Util;
import jaxesa.webapi.ssoAPIResponse;
import restapi.jeiRestInterface;

/**
 *
 * @author Administrator
 */
@Path("/bulbuller/params")
public class UI_Param implements jeiRestInterface

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
    @Path("/api/gprminvtxn/{lng},"
                        + "{cnt},"
                        + "{bid},"
                        + "{sid},"
                        + "{ip},"
                        + "{aid}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    public ssoAPIResponse getParamPageNewInventoryTxn(  @PathParam("lng")         String psLang,
                                                        @PathParam("cnt")         String psCountry,
                                                        @PathParam("bid")         String psBrowserId,
                                                        @PathParam("sid")         String psUISessionId,//don't change the paramter name (sid) 
                                                        @PathParam("ip")          String psIp,
                                                        @PathParam("aid")         String pAccId
                                                     ) throws Exception
    {
        // This API returns brand list and category list for the account requesting

        int iRec = 0;
        try
        {
            ssoAPIResponse rsp = new ssoAPIResponse();

            SsUsrAccounts acc = new SsUsrAccounts();

            long lAccId = Long.parseLong(pAccId);
            acc = Users.getAccountOrLinkedAccountInfo(gem, gUserId, lAccId);
            if (acc==null)
            {
                rsp.Response = "err";
                rsp.ResponseMsg = "Account invalid";
                return rsp;
            }

            ssoInvParams params = new ssoInvParams();

            //Get Brand / Firm List
            params.brandItemCodes = InventoryOps.getBrandList(gem, lAccId);

            //Get Category List
            params.categories = InventoryOps.getCategoryList(gem, lAccId);

            rsp.Content = Util.JSON.Convert2JSON(params).toString();
            rsp.Response = "ok";

            return rsp;

        }
        catch(Exception e)
        {
            throw e;
        }
    }

    @GET
    @Path("/api/delinvctg/{lng},"
                        + "{cnt},"
                        + "{bid},"
                        + "{sid},"
                        + "{ip},"
                        + "{aid},"
                        + "{ctg}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    public ssoAPIResponse removeParam_Category( @PathParam("lng")         String psLang,
                                                @PathParam("cnt")         String psCountry,
                                                @PathParam("bid")         String psBrowserId,
                                                @PathParam("sid")         String psUISessionId,//don't change the paramter name (sid) 
                                                @PathParam("ip")          String psIp,
                                                @PathParam("aid")         String pAccId,
                                                @PathParam("ctg")         String psCategory
                                             ) throws Exception
    {

        try
        {
            ssoAPIResponse rsp = new ssoAPIResponse();
            
            SsUsrAccounts acc = new SsUsrAccounts();

            long lAccId = Long.parseLong(pAccId);
            acc = Users.getAccountOrLinkedAccountInfo(gem, gUserId, lAccId);
            if (acc==null)
            {
                rsp.Response = "err";
                rsp.ResponseMsg = "Account invalid";
                return rsp;
            }
            
            InventoryOps.deleteCategory(gem, lAccId, psCategory);

            rsp.Content = "";
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }

    }

    @GET
    @Path("/api/delinvbrnd/{lng},"
                        + "{cnt},"
                        + "{bid},"
                        + "{sid},"
                        + "{ip},"
                        + "{aid},"
                        + "{brd}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    public ssoAPIResponse removeParam_Brand(@PathParam("lng")         String psLang,
                                            @PathParam("cnt")         String psCountry,
                                            @PathParam("bid")         String psBrowserId,
                                            @PathParam("sid")         String psUISessionId,//don't change the paramter name (sid) 
                                            @PathParam("ip")          String psIp,
                                            @PathParam("aid")         String pAccId,
                                            @PathParam("brd")         String psBrand
                                           ) throws Exception
    {

        try
        {
            ssoAPIResponse rsp = new ssoAPIResponse();

            SsUsrAccounts acc = new SsUsrAccounts();

            long lAccId = Long.parseLong(pAccId);
            acc = Users.getAccountOrLinkedAccountInfo(gem, gUserId, lAccId);
            if (acc==null)
            {
                rsp.Response = "err";
                rsp.ResponseMsg = "Account invalid";
                return rsp;
            }

            InventoryOps.deleteBrand(gem, lAccId, psBrand);

            rsp.Content = "";
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }

    }

    @GET
    @Path("/api/delinvitmcd/{lng},"
                                + "{cnt},"
                                + "{bid},"
                                + "{sid},"
                                + "{ip},"
                                + "{aid},"
                                + "{brd},"
                                + "{icd}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    public ssoAPIResponse removeParam_ItemCode( @PathParam("lng")         String psLang,
                                                @PathParam("cnt")         String psCountry,
                                                @PathParam("bid")         String psBrowserId,
                                                @PathParam("sid")         String psUISessionId,//don't change the paramter name (sid) 
                                                @PathParam("ip")          String psIp,
                                                @PathParam("aid")         String pAccId,
                                                @PathParam("brd")         String pBrand,
                                                @PathParam("icd")         String pItemCode
                                               ) throws Exception
    {

        try
        {
            ssoAPIResponse rsp = new ssoAPIResponse();

            SsUsrAccounts acc = new SsUsrAccounts();

            long lAccId = Long.parseLong(pAccId);
            acc = Users.getAccountOrLinkedAccountInfo(gem, gUserId, lAccId);
            if (acc==null)
            {
                rsp.Response = "err";
                rsp.ResponseMsg = "Account invalid";
                return rsp;
            }

            InventoryOps.deleteItemCode(gem, lAccId, pBrand, pItemCode);

            rsp.Content = "";
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }

    }
    
}
