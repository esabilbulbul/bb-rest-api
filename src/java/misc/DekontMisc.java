/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package misc;

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
}


