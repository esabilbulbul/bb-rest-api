/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package wapi.core;

import Methods.Users;
import bb.app.account.CachingOps_Account;
import bb.app.inv.InventoryOps;
import bb.app.obj.ssoInventoryParams;
import bb.app.stmt.InventoryStatement;
import entity.user.SsUsrAccounts;
import java.util.ArrayList;
import java.util.Arrays;
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
import redis.clients.jedis.Jedis;
import restapi.jeiRestInterface;

/**
 *
 * @author Administrator
 */
@Path("/api/inv")
public class UIInventoryEntry implements jeiRestInterface   
{
    // SESSION VARIABLES
    long gUserId  = -1;
    EntityManager gem;
    ArrayList<String> gServiceGrants = new ArrayList<String>();

    public UIInventoryEntry()
    {
        
    }

    @Override
    public void init(String pUserId, String pBrowserId, String p3rdPartyAPIKey, EntityManager pem,String psUserRoleReqs)
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

}


