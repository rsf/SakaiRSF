/*
 * Created on Dec 2, 2005
 */
package uk.ac.cam.caret.sakai.rsf.servlet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.sakaiproject.util.Web;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.WebApplicationContext;

import uk.org.ponder.rsf.servlet.ServletContextCUP;
import uk.org.ponder.rsf.servlet.ServletEarlyRequestParser;
import uk.org.ponder.rsf.viewstate.BaseURLProvider;
import uk.org.ponder.rsf.viewstate.StaticBaseURLProvider;
import uk.org.ponder.servletutil.ServletUtil;
import uk.org.ponder.util.Logger;

public class SakaiEarlyRequestParser extends ServletEarlyRequestParser
    implements ApplicationContextAware {
  private WebApplicationContext wac;
  
  /**
   * Since it seems we can no longer apply servlet mappings in our web.xml as of
   * Sakai 2.0, we perform this feat manually, using the resourceurlbase init
   * parameter, and this string which represents the offset path for resource
   * handled by RSF. Any paths falling outside this will be treated as static,
   * and sent to the resourceurlbase.
   */
  public static final String FACES_PATH = "faces";
  private String defaultview;
  private String extrapath;

  public void setDefaultView(String defaultview) {
    this.defaultview = defaultview;
  }

  public void init() {
    extrapath = computePathInfo(request);
    final StringBuffer requesturl = request.getRequestURL();
    if (extrapath.equals("")) {
      extrapath = defaultview;
      requesturl.append('/').append(FACES_PATH).append(extrapath);
    }

    HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request) {
      public String getPathInfo() {
        return extrapath;
      }
      public StringBuffer getRequestURL() {
        StringBuffer togo = new StringBuffer();
        togo.append(requesturl);
        return togo;
      }
    };
    this.request = wrapper;
    
    super.init();
  }

  public void setApplicationContext(ApplicationContext applicationContext) {
    wac = (WebApplicationContext) applicationContext;
  }

  public static String computePathInfo(HttpServletRequest request) {
    String requesturl = request.getRequestURL().toString();
    String extrapath = request.getPathInfo();
    if (extrapath == null) {
      extrapath = "";
    }
    if (extrapath.length() > 0 && extrapath.charAt(0) == '/') {
      extrapath = extrapath.substring(1);
    }
    int earlypos = requesturl.indexOf('/' + FACES_PATH);
    // within a Sakai helper, the request wrapper is even FURTHER screwed up
    if ("".equals(extrapath) && earlypos >= 0) {
      extrapath = requesturl.substring(earlypos + 1);
    }
    // Now, the Sakai "PathInfo" is *longer* than we would expect were we
    // mapped properly, since it will include what we call the "FACES_PATH",
    // as inserted there by RequestParser when asked for the baseURL.
    if (extrapath.startsWith(FACES_PATH)) {
      extrapath = extrapath.substring(FACES_PATH.length());
    }

    Logger.log
        .info("Beginning ToolSinkTunnelServlet service with requestURL of "
            + requesturl + " and extra path of " + extrapath);

    return extrapath;
  }

  // SAKAI URL handling is ridiculous, in that the URL returned from
  // req.getRequestURL() may simply be *wrong*. This method adjusts the
  // protocol and port from a "correctly" computed URL to be closer to
  // reality, via the hackery from Sakai "Web" utils.
  public static String fixSakaiURL(HttpServletRequest req, String computed) {
    String serverURL = Web.serverUrl(req);
    int endprotpos = computed.indexOf("://");
    int slashpos = computed.indexOf('/', endprotpos + 3);
    return serverURL + computed.substring(slashpos);
  }

  
  private String resourceurlbase;
  
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

  public StaticBaseURLProvider computeBaseURLProvider(HttpServletRequest request) {
    ServletContext servletcontext = wac.getServletContext();
    // yes, these two fields are not request-scope, but not worth creating
    // a whole new class and bean file for them.
    resourceurlbase = servletcontext.getInitParameter("resourceurlbase");
    if (resourceurlbase == null) {
      resourceurlbase = ServletContextCUP.computeContextName(servletcontext);
    }

    // compute the baseURLprovider.
    StaticBaseURLProvider sbup = new StaticBaseURLProvider();
    String baseurl = fixSakaiURL(request, ServletUtil.getBaseURL2(request));
    sbup.setResourceBaseURL(computeResourceURLBase(baseurl));
//    baseurl += SakaiEarlyRequestParser.FACES_PATH + "/";
    sbup.setBaseURL(baseurl);
    return sbup;
  }

  public BaseURLProvider getBaseURLProvider() {
    return computeBaseURLProvider(request);
  }
  
}
