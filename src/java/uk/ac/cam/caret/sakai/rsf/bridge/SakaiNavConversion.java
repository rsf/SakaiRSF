/*
 * Created on Feb 24, 2005
 */
package uk.ac.cam.caret.sakai.rsf.bridge;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.service.legacy.site.Site;
import org.sakaiproject.service.legacy.site.SitePage;
import org.sakaiproject.service.legacy.site.SiteService;
import org.sakaiproject.service.legacy.site.ToolConfiguration;

import uk.org.ponder.stringutil.CharWrap;
import uk.org.ponder.stringutil.URLEncoder;

/**
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 *  
 */
public class SakaiNavConversion {

  public static String portalURLForSitePage(String baseurl, Site site,
      SitePage page) {
    CharWrap togo = new CharWrap();
    togo.append(baseurl).append("/site/");
    String encodedsite = URLEncoder.encode(site.getId());
    togo.append(encodedsite);
    if (page != null) {
      togo.append("/page/");
      String encodedpage = URLEncoder.encode(page.getId());
      togo.append(encodedpage);
    }
    return togo.toString();

  }

  public static Site siteForPID(SiteService siteservice, String pid) {
    Site togo = null;
    try {
      ToolConfiguration tc = siteservice.findTool(pid);
      String siteID = tc.getSiteId();
      togo = siteservice.getSite(siteID);
    }
    catch (IdUnusedException iue) {
    }
    return togo;
  }
  
  public static SitePage pageForToolConfig(SiteService siteservice,
      ToolConfiguration tc) {
    SitePage page = tc.getContainingPage();
    if (page == null) {
      page = siteservice.findPage(tc.getPageId());
    }
    return page;
  }


}

