/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package wapi.estore;

import Methods.Users;
import Objects.ssoUserAccounts;
import bb.app.inv.InventoryParams;
import bb.app.obj.ssoInvBrandItemCodes;
import bb.app.obj.ssoInvCategory;
import bb.estore.ssEStoreOps;
import bb.estore.ssoCategoryParams;
import bb.estore.ssoHashTag;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import entity.dct.SsDctInvCategories;
import java.math.BigInteger;
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
@Path("/api/est/params")
public class UIParams implements jeiRestInterface
{
    // SESSION VARIABLE 
    BigInteger gUserId  = BigInteger.ZERO;
    EntityManager gem;
    ArrayList<String> gServiceGrants = new ArrayList<String>();

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
    @Path("/uctg/{aid},"
                    + "{lng},"
                    + "{cnt},"
                    + "{sid},"
                    + "{ip},"
                    + "{cc}"
    )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    public ssoAPIResponse updateCategoryChanges(@PathParam("aid")                          String pAccId,
                                                @PathParam("lng")                          String psLang,
                                                @PathParam("cnt")                          String psCountry,
                                                @PathParam("sid")                          String psSessionId,
                                                @PathParam("ip")                           String psIP,
                                                @PathParam("cc")                           String psCategoryChanges//[{Id:, name:, nameUI:, htId:, ftId:}]
                                                   /*
                                                   @PathParam("ctgid")                        String psCategoryId,
                                                   @PathParam("ctgnm")                        String psCategoryName,
                                                   @PathParam("ctgui")                        String psCategoryNameUI,
                                                   @PathParam("ctghid")                       String psCategoryHashTagId,
                                                   @PathParam("ctgfid")                       String psCategoryFeatureId
                                                   */
                                                 ) throws Exception
    {
        ArrayList<ssoInvCategory>       categories     = new ArrayList<ssoInvCategory>();

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

            ArrayList<bb.estore.ssoCategory> aChanges = new ArrayList<bb.estore.ssoCategory>();
            JsonArray jsaCategoryChanges = (JsonArray)Util.JSON.toArray(psCategoryChanges);
            for (int j=0; j<jsaCategoryChanges.size();j++)
            {
                JsonObject jsChangeN = (JsonObject)jsaCategoryChanges.get(j);

                bb.estore.ssoCategory changeN = new bb.estore.ssoCategory();

                changeN.Id          = jsChangeN.get("Id").getAsString();
                changeN.name        = jsChangeN.get("name").getAsString();
                changeN.nameUI      = jsChangeN.get("nameUI").getAsString();
                changeN.hashTagId   = jsChangeN.get("htId").getAsString();
                changeN.featureId   = jsChangeN.get("ftId").getAsString();

                aChanges.add(changeN);

            }

            /*
            BigInteger biCategoryId    = new BigInteger(psCategoryId);
            BigInteger biHashTagId     = new BigInteger(psCategoryHashTagId);
            BigInteger biFeatureTagId  = new BigInteger(psCategoryFeatureId);
            String sCategoryNameUI     = psCategoryNameUI;
            */

            ssEStoreOps.updateCateogry(gem, gUserId, aChanges);

            rsp.Content  = "";
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    
    }
    
    
    @GET
    @Path("/nwht/{aid},"
                    + "{lng},"
                    + "{cnt},"
                    + "{sid},"
                    + "{ip},"
                    + "{hnm},"
                    + "{hid},"
                    + "{hgp},"
                    + "{hts}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    public ssoAPIResponse saveHashtagGroup(@PathParam("aid")                            String pAccId,
                                             @PathParam("lng")                          String psLang,
                                             @PathParam("cnt")                          String psCountry,
                                             @PathParam("sid")                          String psSessionId,
                                             @PathParam("ip")                           String psIP,

                                             @PathParam("hnm")                          String psHashTagGroupName,
                                             @PathParam("hid")                          String psHashTagGroupId,
                                             @PathParam("hgp")                          String psHashTagGroup,// Category / Item
                                             @PathParam("hts")                          String psHashTags
                                           ) throws Exception
    {
        ArrayList<ssoInvCategory>       categories     = new ArrayList<ssoInvCategory>();

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
            
            //String sHashTagGroupName = "";
            //BigInteger biHashTagGroupId = new BigInteger(psHashTagGroupId);
            ssoHashTag oHashTag = new ssoHashTag();
            oHashTag.Id = new BigInteger(psHashTagGroupId);
            oHashTag.name = psHashTagGroupName;//by default
            
            if(oHashTag.Id.compareTo(BigInteger.ZERO)==0)
                oHashTag = ssEStoreOps.isHashTagGroupNameExists(gem, gUserId, psHashTagGroupName.trim().toLowerCase());

            if(oHashTag.Id.compareTo(BigInteger.ZERO)==0)
            {

                oHashTag = ssEStoreOps.createNewHashtagGroup(gem, 
                                                                    accConnected.userId,
                                                                    accConnected.uid,
                                                                    psHashTagGroup,
                                                                    psHashTagGroupName,
                                                                    psHashTags);
            }
            else
            {
                ssEStoreOps.updateNewHashtagGroup(gem, 
                                                  accConnected.userId,
                                                  accConnected.uid,
                                                  oHashTag.Id,
                                                  psHashTags);
            }

            rsp.Content  = Util.JSON.Convert2JSON(oHashTag).toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }

    }

    @GET
    @Path("/nwft/{aid},"
                    + "{lng},"
                    + "{cnt},"
                    + "{sid},"
                    + "{ip},"
                    + "{fnm},"
                    + "{fid},"
                    + "{fgp},"
                    + "{fts}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    public ssoAPIResponse saveFeaturesGroup(@PathParam("aid")                            String pAccId,
                                             @PathParam("lng")                          String psLang,
                                             @PathParam("cnt")                          String psCountry,
                                             @PathParam("sid")                          String psSessionId,
                                             @PathParam("ip")                           String psIP,

                                             @PathParam("fnm")                          String psFeatureGroupName,
                                             @PathParam("fid")                          String psFeatureGroupId,
                                             @PathParam("fgp")                          String psFeatureGroup,// Category / Item
                                             @PathParam("fts")                          String psFeatures //HashTags
                                           ) throws Exception
    {
        ArrayList<ssoInvCategory>       categories     = new ArrayList<ssoInvCategory>();

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

            //String sHashTagGroupName = "";
            //BigInteger biHashTagGroupId = new BigInteger(psHashTagGroupId);
            ssoHashTag oFeature = new ssoHashTag();
            oFeature.Id = new BigInteger(psFeatureGroupId);
            oFeature.name = psFeatureGroupName;//by default

            if(oFeature.Id.compareTo(BigInteger.ZERO)==0)
                oFeature = ssEStoreOps.isFeatureGroupNameExists(gem, gUserId, psFeatureGroupName.trim().toLowerCase());

            if(oFeature.Id.compareTo(BigInteger.ZERO)==0)
            {

                oFeature = ssEStoreOps.createNewFeaturesGroup(gem, 
                                                              accConnected.userId,
                                                              accConnected.uid,
                                                              psFeatureGroup,
                                                              psFeatureGroupName,
                                                              psFeatures);
            }
            else
            {
                ssEStoreOps.updateNewFeatureGroup(gem, 
                                                  accConnected.userId,
                                                  accConnected.uid,
                                                  oFeature.Id,
                                                  psFeatures);
            }

            rsp.Content  = Util.JSON.Convert2JSON(oFeature).toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }

    }

    @GET
    @Path("/ckht/{aid},"
                    + "{lng},"
                    + "{cnt},"
                    + "{sid},"
                    + "{ip},"
                    + "{hnm}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    public ssoAPIResponse checkHashtagName(@PathParam("aid")                            String pAccId,
                                            @PathParam("lng")                          String psLang,
                                            @PathParam("cnt")                          String psCountry,
                                            @PathParam("sid")                          String psSessionId,
                                            @PathParam("ip")                           String psIP,
                                            @PathParam("hnm")                          String psHashTagGroupName
                                          ) throws Exception
    {
        ArrayList<ssoInvCategory>       categories     = new ArrayList<ssoInvCategory>();

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
            
            //BigInteger biHTGroupId = BigInteger.ZERO;
            ssoHashTag oHashTag = new ssoHashTag();
            oHashTag = ssEStoreOps.isHashTagGroupNameExists(gem, gUserId, psHashTagGroupName);
            
            rsp.Content  = Util.JSON.Convert2JSON(oHashTag).toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    
    }

    @GET
    @Path("/gcpm/{aid},"
                    + "{lng},"
                    + "{cnt},"
                    + "{bid},"
                    + "{sid},"
                    + "{ip},"
                    + "{grp},"
                    + "{rst}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    public ssoAPIResponse getCategoryParams(@PathParam("aid")                          String pAccId,
                                            @PathParam("lng")                          String psLang,
                                            @PathParam("cnt")                          String psCountry,
                                            @PathParam("bid")                          String psSessionId,
                                            @PathParam("sid")                          String psBrowserId,
                                            @PathParam("ip")                           String psIP,

                                            @PathParam("grp")                          String psGroup,
                                            @PathParam("rst")                          String psReset
                                          ) throws Exception
    {
        ssoCategoryParams ctgParams = new ssoCategoryParams();

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

            
            ctgParams.hashTags = ssEStoreOps.getHashtagGroups(gem, accConnected.userId, psGroup);

            rsp.Content  = Util.JSON.Convert2JSON(ctgParams).toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }

    }

    @GET
    @Path("/gctg/{aid},"
                    + "{lng},"
                    + "{cnt},"
                    + "{sid},"
                    + "{ip},"
                    + "{ky},"
                    + "{rst}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    public ssoAPIResponse getCategoryDetails(@PathParam("aid")                          String pAccId,
                                             @PathParam("lng")                          String psLang,
                                             @PathParam("cnt")                          String psCountry,
                                             @PathParam("sid")                          String psSessionId,
                                             @PathParam("ip")                           String psIP,

                                             @PathParam("ky")                           String psKeyword,
                                             @PathParam("rst")                          String psReset
                                           ) throws Exception
    {
        ArrayList<ssoInvCategory>       categories     = new ArrayList<ssoInvCategory>();

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

            boolean bReset = false;
            if(psReset.trim().toLowerCase().equals("y")==true)
                bReset = true;

            categories = InventoryParams.getCategoryDetails(gem, accConnected.userId, bReset);

            rsp.Content  = Util.JSON.Convert2JSON(categories).toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }

    }

    @GET
    @Path("/svctg/{lng},"
                        + "{cnt},"
                        + "{bid},"
                        + "{sid},"
                        + "{ip},"
                        + "{aid},"
                        + "{ctg},"
                        + "{cui}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse saveCategory(@PathParam("lng")         String psLang,
                                         @PathParam("cnt")         String psCountry,
                                         @PathParam("bid")         String psBrowserId,
                                         @PathParam("sid")         String psUISessionId,//don't change the paramter name (sid) 
                                         @PathParam("ip")          String psIp,
                                         @PathParam("aid")         String pAccId,
                                         @PathParam("ctg")         String pCategoryName,
                                         @PathParam("cui")         String pCategoryUIName
                                     ) throws Exception
    {
        // This API returns brand list and category list for the account requesting 

        int iRec = 0;
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

            String sCategoryUI = pCategoryName;
            if(pCategoryUIName.trim().length()>0)
                sCategoryUI = pCategoryUIName;

            SsDctInvCategories ctg = new SsDctInvCategories();
            ctg.userId     = gUserId;
            ctg.category   = pCategoryName;
            ctg.categoryUI = sCategoryUI;

            BigInteger biCtgUID = gem.persist(ctg);

            rsp.Content = biCtgUID.toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    @GET
    @Path("/rse/{aid},"
                + "{lng},"
                + "{cnt},"
                + "{sid},"
                + "{ip},"
                + "{iid}"
         )
    @Consumes()
    @Produces(MediaType.JSON_PLUS)
    @Token(VerificationType.MUST)
    @UserGrants({UserRole.ADMIN, UserRole.MANAGER})
    public ssoAPIResponse resetSearchEngine(    @PathParam("aid")                          String pAccId,
                                                @PathParam("lng")                          String psLang,
                                                @PathParam("cnt")                          String psCountry,
                                                @PathParam("sid")                          String psSessionId,
                                                @PathParam("ip")                           String psIP,
                                                @PathParam("iid")                          String psItemId
                                              ) throws Exception
    {
        try
        {
            ArrayList<ssoInvBrandItemCodes> aItemsFound = new ArrayList<ssoInvBrandItemCodes>();

            ssoAPIResponse rsp = new ssoAPIResponse();
            ssoUserAccounts accConnected = new ssoUserAccounts();

            BigInteger lSessionUserId = gUserId;//for now
            BigInteger lTargetAccId   = new BigInteger(pAccId);
            BigInteger biItemUID      = new BigInteger(psItemId);

            accConnected = Users.connectAccount(gem, lSessionUserId, lTargetAccId, gServiceGrants);
            if(accConnected.bAccessGranted==false)
            {
                if(accConnected.bForce2Login==true)
                    rsp.AuthenticationFailed = true;

                rsp.Response = "err";
                rsp.ResponseMsg = "Account invalid";
                return rsp;
            }

            //String sItemName = ssEStoreOps.autoGenerateI temName(gem, gUserId, accConnected.uid, biItemUID);

            //rsp.Content = Util.JSON.Convert2JSON(sItemName).toString();
            rsp.Response = "ok";

            return rsp;
        }
        catch(Exception e)
        {
            throw e;
        }
    }    
}
