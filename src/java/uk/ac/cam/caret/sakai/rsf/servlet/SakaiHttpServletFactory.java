/*
 * Created on Dec 2, 2005
 */
package uk.ac.cam.caret.sakai.rsf.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import uk.org.ponder.rsac.servlet.StaticHttpServletFactory;
import uk.org.ponder.util.Logger;

public class SakaiHttpServletFactory extends StaticHttpServletFactory {
  private HttpServletRequest request;
  private HttpServletResponse response;

  public void setHttpServletRequest(HttpServletRequest request) {
    this.request = request;
  }

  public void setHttpServletResponse(HttpServletResponse response) {
    this.response = response;
  }

  private String entityref = null;

  public void setEntityReference(String entityref) {
    this.entityref = entityref;
  }

  /**
   * Since it seems we can no longer apply servlet mappings in our web.xml as of
   * Sakai 2.0, we perform this feat manually, using the resourceurlbase init
   * parameter, and this string which represents the offset path for resource
   * handled by RSF. Any paths falling outside this will be treated as static,
   * and sent to the resourceurlbase.
   */
  public static final String FACES_PATH = "faces";
  private String extrapath;

  // Use old-style initialisation semantics since this bean is populated by
  // inchuck.
  private boolean initted = false;

  private void checkInit() {
    if (!initted) {
      initted = true;
      init();
    }
  }

  public void init() {
    // only need to perform request demunging if this has not come to us
    // via the AccessRegistrar.
    if (entityref.equals("")) {
      extrapath = computePathInfo(request);
      final StringBuffer requesturl = request.getRequestURL();
      // now handled with implicitNullPathRedirect in RSF proper
//      if (extrapath.equals("")) {
//        extrapath = defaultview;
//        requesturl.append('/').append(FACES_PATH).append(extrapath);
//      }

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
      request = wrapper;
    }
  }

  public HttpServletRequest getHttpServletRequest() {
    checkInit();
    return request;
  }

  public HttpServletResponse getHttpServletResponse() {
    checkInit();
    return response;
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

}
