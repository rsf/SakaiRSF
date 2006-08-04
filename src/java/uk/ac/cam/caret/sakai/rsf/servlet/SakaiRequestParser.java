/*
 * Created on Dec 2, 2005
 */
package uk.ac.cam.caret.sakai.rsf.servlet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.util.Web;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.WebApplicationContext;

import uk.ac.cam.caret.sakai.rsf.bridge.SakaiNavConversion;
import uk.org.ponder.rsf.renderer.BasicSCR;
import uk.org.ponder.rsf.renderer.ComponentRenderer;
import uk.org.ponder.rsf.renderer.StaticRendererCollection;
import uk.org.ponder.rsf.viewstate.BaseURLProvider;
import uk.org.ponder.rsf.viewstate.StaticBaseURLProvider;
import uk.org.ponder.servletutil.ServletUtil;
import uk.org.ponder.stringutil.URLUtil;
import uk.org.ponder.util.Logger;
import uk.org.ponder.webapputil.ConsumerInfo;
import uk.org.ponder.xml.NameValue;

public class SakaiRequestParser implements ApplicationContextAware {
  private HttpServletRequest request;
  private String resourceurlbase;

  private Site site;
  
  private SitePage sitepage;
  private SiteService siteservice;
  
  private BasicSCR bodyscr;
  private StaticRendererCollection src;
  
  private WebApplicationContext wac;
  private StaticBaseURLProvider sbup;
  private ConsumerInfo consumerinfo;
  private Placement placement; 
  
  public void setHttpServletRequest(HttpServletRequest request) {
    this.request = request;
  }
  
  public void setSiteService(SiteService siteservice) {
    this.siteservice = siteservice;
  }
  
  public void setApplicationContext(ApplicationContext applicationContext) {
    wac = (WebApplicationContext) applicationContext;
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
  
  public void init() {
    ServletContext servletcontext = wac.getServletContext();
    // yes, these two fields are not request-scope, but not worth creating
    // a whole new class and bean file for them.
    resourceurlbase = servletcontext.getInitParameter("resourceurlbase");
    
    // compute the baseURLprovider.
    sbup = new StaticBaseURLProvider();
    String baseurl = fixSakaiURL(request, ServletUtil.getBaseURL2(request));
    sbup.setResourceBaseURL(computeResourceURLBase(baseurl));
    baseurl += SakaiEarlyRequestParser.FACES_PATH + "/";
    sbup.setBaseURL(baseurl);

    // Deliver the rewrite rule to the renderer that will invoke the relevant
    // Javascript magic to resize our frame.
    bodyscr = new BasicSCR();
    bodyscr.setName("sakai-body");
    bodyscr.addNameValue(new NameValue("onload", (String) request
        .getAttribute("sakai.html.body.onload")));
    bodyscr.tag_type = ComponentRenderer.NESTING_TAG;
    
    src = new StaticRendererCollection();
    src.addSCR(bodyscr);


    Tool tool = (Tool) request.getAttribute("sakai.tool");
    placement = (Placement) request.getAttribute("sakai.tool.placement");
    String toolid = tool.getId();
    String toolinstancepid = placement.getId();
    

    Logger.log.info("Got tool dispatcher id of " + toolid
        + " resourceBaseURL " + sbup.getResourceBaseURL() 
        + " baseURL " + sbup.getBaseURL() + " and Sakai PID "
        + toolinstancepid);
    
    // Compute the ConsumerInfo object.
    site = SakaiNavConversion.siteForPID(siteservice, toolinstancepid);
    ToolConfiguration tc = siteservice.findTool(toolinstancepid);
    sitepage = SakaiNavConversion.pageForToolConfig(siteservice, tc);
    
    consumerinfo = new ConsumerInfo();
    consumerinfo.urlbase = sbup.getBaseURL();
    consumerinfo.resourceurlbase = sbup.getResourceBaseURL();
    consumerinfo.consumertype = "sakai";
    consumerinfo.extraparameters = "&panel=Main";

    consumerinfo.externalURL = URLUtil.deSpace(sitepage.getUrl());
  }

  public Site getSite() {
    return site;
  }
  
  public SitePage getSitePage() {
    return sitepage;
  }

  public StaticRendererCollection getConsumerStaticRenderers() {
    return src;
  }

  public BaseURLProvider getBaseURLProvider() {
    return sbup;
  }
  
  public ConsumerInfo getConsumerInfo() {
    return consumerinfo;
  }
  
  public Placement getPlacement() {
    return placement;
  }
}
