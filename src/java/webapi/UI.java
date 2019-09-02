/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package webapi;

import jaxesa.annotations.Consumes;
import jaxesa.annotations.GET;
import jaxesa.annotations.MediaType;
import jaxesa.annotations.Path;
import jaxesa.annotations.PathParam;
import jaxesa.annotations.Produces;
import jaxesa.persistence.DBPool;
import jaxesa.persistence.EntityManager;
import jaxesa.util.Util;
import jaxesa.webapi.sso_APIResponse;

/**
 *
 * @author Administrator
 */
@Path("/bulbuller/dekont")
public class UI 
{
    
    //Sample: http://localhost:8080/bb-wapi-dekont-converter/rest/bulbuller/dekont/api/showreport/?username=1&lang=en&country=tr&sessionid=123
    
    @GET
    @Path("/api/showreport/{userid},"
                            + "{lang},"
                            + "{country},"
                            + "{sessionid}"
         )
    @Consumes()
    @Produces(MediaType.JSON)
    public sso_APIResponse showReport(  @PathParam("userid")                       String psUser_SessionInfo,
                                        @PathParam("lang")                         String psLang,
                                        @PathParam("country")                      String psCountry,
                                        @PathParam("sessionid")                    String psSessionId
                                     ) throws Exception
    {
        sso_APIResponse Rsp = new sso_APIResponse();
        
        EntityManager em = DBPool.getSessionConnection(psUser_SessionInfo, Util.Methods.hash());
        
        return Rsp;
    }
    
}


