/*
 * Created on 18 May 2007
 */
package uk.ac.cam.caret.sakai.rsf.entitybroker;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.entitybroker.EntityParse;
import org.sakaiproject.entitybroker.access.AccessProvider;
import org.sakaiproject.entitybroker.access.AccessProviderManager;

import uk.org.ponder.beanutil.WriteableBeanLocator;
import uk.org.ponder.rsac.RSACBeanLocator;
import uk.org.ponder.rsac.servlet.RSACUtils;
import uk.org.ponder.util.Logger;
import uk.org.ponder.util.UniversalRuntimeException;

public class AccessRegistrar implements AccessProvider {
  private AccessProviderManager accessProviderManager;
  private RSACBeanLocator rsacbl;
  private String[] prefixes;

  public void setAccessProviderManager(
      AccessProviderManager accessProviderManager) {
    this.accessProviderManager = accessProviderManager;
  }

  public void setRSACBeanLocator(RSACBeanLocator rsacbl) {
    this.rsacbl = rsacbl;
  }

  public void registerPrefixes(String[] prefixes) {
    this.prefixes = prefixes;
    for (int i = 0; i < prefixes.length; ++i) {
      accessProviderManager.registerProvider(prefixes[i], this);
    }
  }

  public void destroy() {
    if (prefixes != null) {
      for (int i = 0; i < prefixes.length; ++i) {
        accessProviderManager.unregisterProvider(prefixes[i], this);
      }
    }
  }

  // Despite our very low profile of earlyRequestParser, it is still easier to
  // fake out pathInfo this way here. It is considered the authoritative
  // determiner by getBaseURL2.
  private HttpServletRequest wrapRequest(HttpServletRequest req) {
    String oldpathinfo = req.getPathInfo();
    EntityParse parsed = new EntityParse(oldpathinfo);
    int extent = parsed.toString().length();
    final String newpathinfo = extent < oldpathinfo.length()? 
         oldpathinfo.substring(parsed.toString().length()) : "";
    
    return new HttpServletRequestWrapper(req) {
      public String getPathInfo() {
        return newpathinfo; 
      }
    };
  }
  
  public void handleAccess(HttpServletRequest req, HttpServletResponse res,
      String reference) {
    try {
      rsacbl.startRequest();
      // A request bean locator just good for this request.
      WriteableBeanLocator rbl = rsacbl.getBeanLocator();
      // inchuck entityReference
      rbl.set("sakai-entityReference", reference);
      RSACUtils.startServletRequest(wrapRequest(req), res, rsacbl,
          RSACUtils.HTTP_SERVLET_FACTORY);
      // pass the request to RSF.
      rbl.locateBean("rootHandlerBean");
    }
    catch (Throwable t) {
      // Access servlet performs no useful logging, do it here.
      Logger.log.error("Error handling access request", t);
      throw UniversalRuntimeException.accumulate(t, "Error handling access request");
    }
    finally {
      rsacbl.endRequest();
    }
  }

}
