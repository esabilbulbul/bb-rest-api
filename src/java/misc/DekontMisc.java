/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package misc;

import java.util.List;
import jaxesa.persistence.EntityManager;
import jaxesa.persistence.Query;
import jaxesa.persistence.StoredProcedureQuery;
import jaxesa.persistence.misc.RowColumn;
import jaxesa.util.Util;

/**
 *
 * @author esabil
 */
public final class DekontMisc
{
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


