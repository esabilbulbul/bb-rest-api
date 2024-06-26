/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package misc;

import bb.app.account.AccountMisc;
import bb.app.dekonts.DekontSummaryYear;
import bb.app.obj.ssoInvBrandItemCodes;
import bb.app.obj.ssoMerchant;
import bb.app.obj.ssoMerchantPreferences;
import entity.mrc.SsMrcMerchants;
import entity.user.SsUsrAccounts;
import java.util.ArrayList;
import java.util.List;
import jaxesa.persistence.EntityManager;
import jaxesa.persistence.Query;
import jaxesa.persistence.StoredProcedureQuery;
import jaxesa.persistence.annotations.ParameterMode;
import jaxesa.persistence.misc.RowColumn;
import jaxesa.util.Util;

/**
 *
 * @author esabil
 */
public final class DekontMisc
{
/*
    public static String getUserDefaultMerchant()
    {
        
    }
    */

    // User = User Id + Account Id
    // User might have multiple accounts 
    // Default account type is Individual (I) other availables are "Business"
    /*
    public static ssoMerchantPreferences getMerchantPreferences(EntityManager pem, long pUserId, long pAccountId) throws Exception
    {
        ssoMerchantPreferences mrcPref = new ssoMerchantPreferences();

        try
        {
            //pem.cacheable("P_USR_ID, P_ACC_ID");
            //StoredProcedureQuery SP = pem.createStoredProcedureQuery("SP_MRC_GET_MERCHANT_PREFERENCES");
            Query stmt = pem.createNamedQuery("SsUsrAccounts.getMerchantPreferences", SsUsrAccounts.class);
            int index = 1;
            stmt.SetParameter(index++, pUserId          , "USER_ID");
            stmt.SetParameter(index++, pAccountId       , "ACCOUNT_ID");

            //SP.registerStoredProcedureParameter("P_USR_ID"    , Long.class     , ParameterMode.IN);
            //SP.registerStoredProcedureParameter("P_ACC_ID"    , Long.class     , ParameterMode.IN);

            //int Colindex = 1;
            //SP.SetParameter(Colindex++, pUserId             , "P_USR_ID");
            //SP.SetParameter(Colindex++, pAccountId          , "P_ACC_ID");

            //SP.execute();

            //List<List<RowColumn>> rs =  SP.getResultList();
            List<List<RowColumn>> rs = stmt.getResultList();
            if (rs.size()>0)
            {
                List<RowColumn> RowN = rs.get(0);

                DekontSummaryYear newYear = new DekontSummaryYear();

                mrcPref.Id              = Long.parseLong(Util.Database.getValString(RowN, "UID").toString());
                mrcPref.version         = Integer.parseInt(Util.Database.getValString(RowN, "VERSION").toString());
                mrcPref.MerchantName    = Util.Database.getValString(RowN, "PROFILENAME");
                //mrcPref.MerchantName    = Util.Database.getValString(RowN, "PROFILE_NAME");
                mrcPref.CurrencyCode    = Util.Database.getValString(RowN, "CURRENCY_CODE");
                mrcPref.CurrencyName    = Util.Database.getValString(RowN, "CURRENCY_NAME");
                mrcPref.MCC             = Util.Database.getValString(RowN, "MCC");
                mrcPref.MCCName         = Util.Database.getValString(RowN, "MCC_NAME");
                mrcPref.CountryCode     = Util.Database.getValString(RowN, "COUNTRY_CODE");
                mrcPref.CountryName     = Util.Database.getValString(RowN, "COUNTRY_NAME");
                mrcPref.StateCode       = Util.Database.getValString(RowN, "STATE_CODE");
                mrcPref.StateName       = Util.Database.getValString(RowN, "STATE_NAME");
                //mrcPref.CountyCode      = Util.Database.getValString(RowN, "COUNTY_CODE");
                mrcPref.PlaceNameUID    = Util.Database.getValString(RowN, "PLACE_NAME_UID");
                mrcPref.PlaceName       = Util.Database.getValString(RowN, "PLACE_NAME");
                mrcPref.email           = Util.Database.getValString(RowN, "EMAIL");
                mrcPref.profileName       = Util.Database.getValString(RowN, "PROFILENAME");
                mrcPref.isTaxInSalesPrice = Util.Database.getValString(RowN, "IS_TAX_INC_PRICE");
                mrcPref.taxRate           = Util.Database.getValString(RowN, "TAX_RATE");

            }
            else
            {
                return null;
            }

            return mrcPref;
        }
        catch(Exception e)
        {
            throw e;
        }
        
    }
*/
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //
    // This only returns the codes of Merchant Preferences
    //
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    public static ssoMerchantPreferences getShortMerchantPreferences(EntityManager pem, long pUserId, long pAccountId) throws Exception
    {
        return AccountMisc.getAccountSettings(pem, pUserId, pAccountId);
    }

    public static String generateReportId()
    {
        long lDateTime = Util.DateTime.GetDateTime("YYYYMMddHHmmssS");

        return Long.toString(lDateTime);
    }

    //This returns the name of file by the id from FTP
    //psFileName = Id
    public static String getFilePathbyId(String psFileName, String psRootPath, String psUserId)
    {
        //Root folder + userId + YYYYMMDD
        String sYYYYMMDD   = psFileName.substring(0,8);
        String sFilePath = psRootPath + "/" + psUserId + "/" + sYYYYMMDD + "/" + psFileName;

        return sFilePath;
    }

    public static boolean isRecordAdded(EntityManager pem, String pRefNo, int pBankCode)
    {
        try
        {
            String sQuery = "SELECT COUNT(1) as CNT FROM ss_mrc_data_eod WHERE BANK_CODE = ? AND TXN_TRACE_NO = ? ";

            //pem.gDriver

            Query stmtQry = pem.CreateQuery(sQuery);

            int ParIndex = 1;
            stmtQry.SetParameter(ParIndex++, pBankCode, "P_BANK_CODE");
            stmtQry.SetParameter(ParIndex++, pRefNo, "P_TXN_TRACE_NO");

            List<List<RowColumn>> rs =  stmtQry.getResultList();
            if (rs.size()==1)
            {
                List<RowColumn> RowN = rs.get(0);
                
                long lCnt =  Long.parseLong(Util.Database.getValString(RowN, "CNT"));
                
                if (lCnt>0)
                    return true;
                
            }
            //for (List<RowColumn> RowN:rs)
            //{
                //String sCNT = Util.Database.getValString(RowN, "CNT");
                
                
            //}
            
            return false;
        }
        catch(Exception e)
        {
            return true;
        }

    }

}


