/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webapi;

import Methods.Users;
import Objects.ssoMessage2Us;
//import apicallbacks.cbInventoryBill_FirstEntry;
import bb.app.account.AccountMisc;
import bb.app.dekonts.DekontEarningStats;
import bb.app.dekonts.DekontFields;
import bb.app.dekonts.DekontMethods;
import static bb.app.account.AccountMisc.calculateSummaryBankSubtotals;
import static bb.app.account.AccountMisc.calculateSummaryDays;
import static bb.app.account.AccountMisc.calculateSummaryWeeksOfMonth;
import bb.app.account.ssoAccInvBalanceCore;
import bb.app.account.ssoAccStmtCore;
import bb.app.account.ssoUIBalanceItem;
import bb.app.account.ssoVendorPayment;
import bb.app.dekonts.DekontSummary;
import bb.app.dict.DictionaryOps;
import bb.app.eod.ssEOD;
import bb.app.inv.InventoryOps;
import bb.app.inv.InventoryParams;
import bb.app.stmt.InventoryStatement;
import bb.app.vendor.VendorOps;
import bb.app.obj.ssoCityCode;
import bb.app.obj.ssoPostCode;
import bb.app.obj.ssoCountryCodes;
import bb.app.obj.ssoCountyCode;
import bb.app.obj.ssoMCC;
import bb.app.obj.ssoMerchant;
import bb.app.obj.ssoMerchantPreferences;
import bb.app.obj.ssoPageParams;
import entity.inv.SsInvAccounting;
import entity.inv.SsInvItemsOptions;
import entity.mrc.SsMrcCashRegEod;
import entity.mrc.SsMrcDataPosTxn;
import entity.mrc.SsMrcMerchants;
import entity.prm.SsPrmCountryStates;
import entity.prm.SsPrmCountryCodes;
import entity.prm.SsPrmCountryPostcodes;
import entity.user.SsUsrAccounts;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import jaxesa.annotations.Consumes;
import jaxesa.annotations.GET;
import jaxesa.annotations.MediaType;
import jaxesa.annotations.Path;
import jaxesa.annotations.PathParam;
import jaxesa.annotations.Produces;
import jaxesa.annotations.Token;
import jaxesa.annotations.VerificationType;
import jaxesa.defs.ShipShuk;
import jaxesa.persistence.DBPool;
import jaxesa.persistence.EntityManager;
import jaxesa.util.Util;
import jaxesa.webapi.ssoAPIResponse;
import misc.DekontMisc;
import bb.app.obj.ssoInventoryParams;
import bb.app.obj.ssoInvBrandItemCodes;
import bb.app.obj.ssoPrintByBillData;
import bb.reports.ssReportSearchBalances;
import bb.reports.ssReportSearchInventory;
import java.math.BigInteger;
import java.util.Arrays;
import jaxesa.annotations.ThreadActionType;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.json.simple.JSONObject;
import redis.clients.jedis.Jedis;
import restapi.jeiRestInterface;
import jaxesa.annotations.Callback;
import jaxesa.persistence.Query;

/**
 *
 * @author Administrator
 */
//@Path("/bulbuller/dekont")
//@Path("/biz/bb")
@Path("/api/biz/bb/UI")
public class UI implements jeiRestInterface
{
    long gUserId  = -1;
    EntityManager gem;
    ArrayList<String> gServiceGrants = new ArrayList<String>();

    public UI()
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
            
            gServiceGrants.addAll(Arrays.asList(psUserRoleReqs.split(",")));
        }
        catch(Exception e)
        {
            
        }
    }


    // IMPORTANT
    //
    // RULE : A linked account can be viewed but can't be edited. 
    //



    

}

