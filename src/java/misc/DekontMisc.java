/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package misc;

import bb.app.dekonts.DekontSummaryYear;
import bb.app.pages.ssoMerchant;
import bb.app.pages.ssoMerchantPreferences;
import entity.mrc.SsMrcMerchants;
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
    public static ArrayList<ssoMerchant> getListOfMerchants4User(EntityManager pem, long pUserId) throws Exception
    {
        ArrayList<ssoMerchant> mrcList = new ArrayList<ssoMerchant>();

        try
        {

            StoredProcedureQuery SP = pem.createStoredProcedureQuery("SP_BB_GET_USER_MRCLIST");

            SP.registerStoredProcedureParameter("P_USR_ID"    , Long.class     , ParameterMode.IN);

            int Colindex = 1;
            SP.SetParameter(Colindex++, pUserId             , "P_USR_ID");

            SP.execute();

            List<List<RowColumn>> rs =  SP.getResultList();

            if (rs.size()>0)
            {
                for (List<RowColumn> RowN:rs)
                {
                    ssoMerchant newMrc = new ssoMerchant();

                    newMrc.name  = Util.Database.getValString(RowN, "MRC_NAME");
                    newMrc.id    = Util.Database.getValString(RowN, "UID");
                    
                    mrcList.add(newMrc);
                }

            }

            return mrcList;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    public static SsMrcMerchants getMerchantPreferences(EntityManager pem, long pMrcId) throws Exception
    {
        SsMrcMerchants mrcPrefs = new SsMrcMerchants();
        
        try
        {
            mrcPrefs = pem.find(SsMrcMerchants.class, pMrcId);
            
            return mrcPrefs;
        }
        catch(Exception e)
        {
            throw e;
        }
    }

    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    //
    // This only returns the codes of Merchant Preferences
    //
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    public static ssoMerchantPreferences getShortMerchantPreferences(EntityManager pem, long pMrcId) throws Exception
    {
        ssoMerchantPreferences mrcPref = new ssoMerchantPreferences();

        try
        {

            StoredProcedureQuery SP = pem.createStoredProcedureQuery("SP_MRC_GET_MERCHANT_PREFERENCES");

            SP.registerStoredProcedureParameter("P_MRC_ID"    , Long.class     , ParameterMode.IN);

            int Colindex = 1;
            SP.SetParameter(Colindex++, pMrcId             , "P_MRC_ID");

            SP.execute();

            List<List<RowColumn>> rs =  SP.getResultList();

            //for (List<RowColumn> RowN:rs)
            if (rs.size()>0)
            {
                List<RowColumn> RowN = rs.get(0);

                DekontSummaryYear newYear = new DekontSummaryYear();

                mrcPref.MerchantName    = Util.Database.getValString(RowN, "MRC_NAME");
                mrcPref.CurrencyCode    = Util.Database.getValString(RowN, "CURRENCY_CODE");
                mrcPref.CurrencyName    = Util.Database.getValString(RowN, "CURRENCY_NAME");
                mrcPref.MCC             = Util.Database.getValString(RowN, "MCC");
                mrcPref.MCCName         = Util.Database.getValString(RowN, "MCC_NAME");
                mrcPref.CountryCode     = Util.Database.getValString(RowN, "COUNTRY_CODE");
                mrcPref.CountryName     = Util.Database.getValString(RowN, "COUNTRY_NAME");
                mrcPref.StateCode       = Util.Database.getValString(RowN, "STATE_CODE");
                mrcPref.StateName       = Util.Database.getValString(RowN, "STATE_NAME");
                mrcPref.CountyCode      = Util.Database.getValString(RowN, "COUNTY_CODE");
                mrcPref.CountyName      = Util.Database.getValString(RowN, "COUNTY_NAME");
                
            }
            
            
            return mrcPref;
        }
        catch(Exception e)
        {
            throw e;
        }
        
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


