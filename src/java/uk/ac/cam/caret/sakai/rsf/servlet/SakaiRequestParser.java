/*
 * Created on Dec 2, 2005
 */
package uk.ac.cam.caret.sakai.rsf.servlet;

import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Tool;

import uk.ac.cam.caret.sakai.rsf.bridge.SakaiNavConversion;
import uk.ac.cam.caret.sakai.rsf.producers.FrameAdjustingProducer;
import uk.ac.cam.caret.sakai.rsf.template.SakaiBodyTPI;
import uk.ac.cam.caret.sakai.rsf.template.SakaiPortalMatterSCR;
import uk.org.ponder.rsf.renderer.ComponentRenderer;
import uk.org.ponder.rsf.renderer.scr.FlatSCR;
import uk.org.ponder.rsf.renderer.scr.StaticRendererCollection;
import uk.org.ponder.rsf.viewstate.BaseURLProvider;
import uk.org.ponder.stringutil.URLUtil;
import uk.org.ponder.util.Logger;
import uk.org.ponder.webapputil.ConsumerInfo;
import uk.org.ponder.xml.NameValue;

/** Parses the servlet request and general Sakai environment for appropriate
 * Sakai request-scope beans. Do not try to make any use of URL information
 * from the request outside the EarlyRequestParser.
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 *
 */

public class SakaiRequestParser {
  private HttpServletRequest request;

  private Site site;
  
  private SitePage sitepage;
  private SiteService siteservice;
  
  private StaticRendererCollection src;

  private ConsumerInfo consumerinfo;
  
  private Placement placement;

  private BaseURLProvider sbup;

  private TimeService timeservice; 
  
  public void setHttpServletRequest(HttpServletRequest request) {
    this.request = request;
  }
  
  public void setSiteService(SiteService siteservice) {
    this.siteservice = siteservice;
  }
  
  public void setTimeService(TimeService timeservice) {
    this.timeservice = timeservice;
  }
  
  public void setBaseURLProvider(BaseURLProvider bup) {
    this.sbup = bup;
  }
  
  public void init() {
    Tool tool = (Tool) request.getAttribute("sakai.tool");
    placement = (Placement) request.getAttribute("sakai.tool.placement");
    String toolid = tool.getId();
    String toolinstancepid = placement.getId();
    
    String frameid = FrameAdjustingProducer.deriveFrameTitle(toolinstancepid);
    
    // Deliver the rewrite rule to the renderer that will invoke the relevant
    // Javascript magic to resize our frame.
    FlatSCR bodyscr = new FlatSCR();
    bodyscr.setName(SakaiBodyTPI.SAKAI_BODY);
    String sakaionload = 
      (String) request.getAttribute("sakai.html.body.onload");
    String hname = "addSakaiRSFDomModifyHook";
    String fullonload = "if (" + hname + "){" + hname + "(" + frameid + ");};" + sakaionload;
    bodyscr.addNameValue(new NameValue("onload", fullonload));
    bodyscr.tag_type = ComponentRenderer.NESTING_TAG;
    
    SakaiPortalMatterSCR matterscr = new SakaiPortalMatterSCR();
    matterscr.setHeadMatter((String) request.getAttribute("sakai.html.head"));
    
    src = new StaticRendererCollection();
    src.addSCR(bodyscr);
    src.addSCR(matterscr);

    Logger.log.info("Got tool dispatcher id of " + toolid
        + " resourceBaseURL " + sbup.getResourceBaseURL() 
        + " baseURL " + sbup.getBaseURL() + " and Sakai PID "
        + toolinstancepid);
    
    // Compute the ConsumerInfo object.
    site = SakaiNavConversion.siteForPID(siteservice, toolinstancepid);
    // tc will be null for Mercury portal
    ToolConfiguration tc = siteservice.findTool(toolinstancepid);
    if (tc != null) {
      sitepage = SakaiNavConversion.pageForToolConfig(siteservice, tc);
    }

    consumerinfo = new ConsumerInfo();
    consumerinfo.urlbase = sbup.getBaseURL();
    consumerinfo.resourceurlbase = sbup.getResourceBaseURL();
    consumerinfo.consumertype = "sakai";

    consumerinfo.externalURL = sitepage == null? consumerinfo.urlbase : 
      URLUtil.deSpace(sitepage.getUrl());
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

  public ConsumerInfo getConsumerInfo() {
    return consumerinfo;
  }
  
  public TimeZone getTimeZone() {
    return timeservice.getLocalTimeZone();
  }
  
  public Placement getPlacement() {
    return placement;
  }
}
