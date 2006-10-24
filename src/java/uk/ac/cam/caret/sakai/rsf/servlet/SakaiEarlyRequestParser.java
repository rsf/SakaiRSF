/*
 * Created on Dec 2, 2005
 */
package uk.ac.cam.caret.sakai.rsf.servlet;

import uk.org.ponder.rsf.servlet.ServletEarlyRequestParser;
import uk.org.ponder.util.Logger;

public class SakaiEarlyRequestParser extends ServletEarlyRequestParser {
  
  /** Since it seems we can no longer apply servlet mappings in our web.xml
   * as of Sakai 2.0, we perform this feat manually, using the resourceurlbase
   * init parameter, and this string which represents the offset path for
   * resource handled by RSF. Any paths falling outside this will be treated
   * as static, and sent to the resourceurlbase.
   */
  public static final String FACES_PATH = "faces";
  private String defaultview;

  public void setDefaultView(String defaultview) {
    this.defaultview = defaultview;
  }
  
  public String getPathInfo() {
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
    if (extrapath.equals("")) {
      extrapath = defaultview;
    }
    Logger.log
        .info("Beginning ToolSinkTunnelServlet service with requestURL of "
            + requesturl + " and extra path of " + extrapath);
    
  
    return extrapath;
  }
}
