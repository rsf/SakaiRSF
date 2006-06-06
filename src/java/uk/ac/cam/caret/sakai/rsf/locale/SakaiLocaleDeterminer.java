/*
 * Created on May 29, 2006
 */
package uk.ac.cam.caret.sakai.rsf.locale;

import java.util.Locale;

import javax.servlet.ServletRequest;

import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.util.ResourceLoader;
import org.springframework.beans.factory.FactoryBean;

/**
 * Determins the correct locale for the current request by searching the
 * following sources in order of preference:
 * <ol>
 * <li>The User's preferences
 * <li>The current ServletRequest
 * <li>The Sakai Session
 * </ol>
 * @author Antranig Basman (amb26@ponder.org.uk)
 * 
 */
// See http://dev.ulan.jp/sakai/wiki/ResourceLoader
public class SakaiLocaleDeterminer implements FactoryBean {
  // Copied from Sakai UserPrefsTool
  public static final Locale getLocaleFromString(String localeString) {
    String[] locValues = localeString.trim().split("_");
    if (locValues.length > 1)
      return new Locale(locValues[0], locValues[1]); // language, country
    else if (locValues.length == 1)
      return new Locale(locValues[0]); // just language
    else
      return null;
  }

  private SessionManager sessionmanager;
  private ServletRequest servletrequest;
  private PreferencesService prefsservice;

  private Locale getPreferencesLocale() {
    String userid = sessionmanager.getCurrentSessionUserId();
    Preferences prefs = prefsservice.getPreferences(userid);
    ResourceProperties props = prefs
        .getProperties(ResourceLoader.APPLICATION_ID);
    String prefLocale = props.getProperty(ResourceLoader.LOCALE_KEY);
    return getLocaleFromString(prefLocale);
  }

  public void setPreferencesService(PreferencesService prefsservice) {
    this.prefsservice = prefsservice;
  }

  public void setSessionManager(SessionManager sessionmanager) {
    this.sessionmanager = sessionmanager;
  }

  public void setServletRequest(ServletRequest servletrequest) {
    this.servletrequest = servletrequest;
  }

  public Object getObject() {
    Locale togo  = getPreferencesLocale();
    if (togo == null) {
      try {
       togo = servletrequest.getLocale();
      }
      catch (Exception e) {}
    }
    if (togo == null) {
      try {
       togo = (Locale) sessionmanager.getCurrentSession().getAttribute("locale");
      }
      catch (Exception e) {}
    }
    if (togo == null) {
      togo = Locale.getDefault();
    }
    return togo;
  }  

  public Class getObjectType() {
    return Locale.class;
  }

  public boolean isSingleton() {
    return true;
  }
}
