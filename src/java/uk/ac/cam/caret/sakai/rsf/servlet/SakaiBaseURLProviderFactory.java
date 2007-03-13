/*
 * Created on 21 Nov 2006
 */
package uk.ac.cam.caret.sakai.rsf.servlet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.util.Web;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.WebApplicationContext;

import uk.org.ponder.rsf.viewstate.BaseURLProvider;
import uk.org.ponder.rsf.viewstate.StaticBaseURLProvider;
import uk.org.ponder.servletutil.ServletUtil;

public class SakaiBaseURLProviderFactory implements ApplicationContextAware, FactoryBean {
  private HttpServletRequest request;

  private WebApplicationContext wac;
  
  public void setApplicationContext(ApplicationContext applicationContext) {
    wac = (WebApplicationContext) applicationContext;
  }
  
  public void setHttpServletRequest(HttpServletRequest request) {
    this.request = request;
  }
  
  private String resourceurlbase;
  
  // Sakai is a very poor URL environment, where the return from
  // req.getRequestURL() may not actually be valid to access this server. 
  // This method adjusts the  protocol and port from a "correctly" computed 
  // URL to be closer to reality, via the hackery from Sakai "Web" utils.
  public static String fixSakaiURL(HttpServletRequest req, String computed) {
    String serverURL = Web.serverUrl(req);
    int endprotpos = computed.indexOf("://");
    int slashpos = computed.indexOf('/', endprotpos + 3);
    return serverURL + computed.substring(slashpos);
  }
  
  public StaticBaseURLProvider computeBaseURLProvider(HttpServletRequest request) {
    ServletContext servletcontext = wac.getServletContext();
    // yes, these two fields are not request-scope, but not worth creating
    // a whole new class and bean file for them.
    resourceurlbase = servletcontext.getInitParameter("resourceurlbase");
    if (resourceurlbase == null) {
      resourceurlbase = ServletUtil.computeContextName(servletcontext);
    }

    // compute the baseURLprovider.
    StaticBaseURLProvider sbup = new StaticBaseURLProvider();
    String baseurl = fixSakaiURL(request, ServletUtil.getBaseURL2(request));
    sbup.setResourceBaseURL(computeResourceURLBase(baseurl));
//    baseurl += SakaiEarlyRequestParser.FACES_PATH + "/";
    sbup.setBaseURL(baseurl);
    return sbup;
  }

  // The argument to this is what Sakai "claims" is our base URL. The true
  // resource URL will be somewhat unrelated in that it will share (at most)
  // the host name and port of this URL.
  private String computeResourceURLBase(String baseurl) {
    if (resourceurlbase.charAt(0) == '/') {
      int endprotpos = baseurl.indexOf("://");
      int firstslashpos = baseurl.indexOf('/', endprotpos + 3);
      return baseurl.substring(0, firstslashpos) + resourceurlbase;
    }
    else { // it is an absolute URL
      return resourceurlbase;
    }
  }

  public Object getObject() throws Exception {
    return computeBaseURLProvider(request);
  }

  public Class getObjectType() {
    return BaseURLProvider.class;
  }

  public boolean isSingleton() {
    return true;
  }

}
