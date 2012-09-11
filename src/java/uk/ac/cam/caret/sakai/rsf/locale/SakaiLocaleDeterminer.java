/*
 * Created on May 29, 2006
 */
package uk.ac.cam.caret.sakai.rsf.locale;

import java.util.Locale;

import javax.servlet.ServletRequest;

import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.i18n.InternationalizedMessages;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.Preferences;
import org.sakaiproject.user.api.PreferencesService;
import org.sakaiproject.entity.api.Reference;
import org.springframework.beans.factory.FactoryBean;

import uk.org.ponder.localeutil.LocaleUtil;

/**
 * Determins the correct locale for the current request by searching the following sources
 * in order of preference:
 * <ol>
 * <li>The Site Language
 * <li>The User's preferences
 * <li>The current ServletRequest
 * <li>The Sakai Session
 * </ol>
 * 
 * @author Antranig Basman (amb26@ponder.org.uk)
 * 
 */
// See http://dev.ulan.jp/sakai/wiki/ResourceLoader
public class SakaiLocaleDeterminer implements FactoryBean {

  private SessionManager sessionmanager;
  private ServletRequest servletrequest;
  private PreferencesService prefsservice;
  private String siteEntityReference;
  private SiteService siteservice;
  private EntityManager entitymanager;

  private Locale getPreferencesLocale() {
    String userid = sessionmanager.getCurrentSessionUserId();
    Preferences prefs = prefsservice.getPreferences(userid);
    ResourceProperties props = prefs
        .getProperties(InternationalizedMessages.APPLICATION_ID);
    String prefLocale = props.getProperty(InternationalizedMessages.LOCALE_KEY);
    return prefLocale == null ? null
        : LocaleUtil.parseLocale(prefLocale);
  }

    public void setSiteService(SiteService siteservice) {
        this.siteservice = siteservice;
    }

    public void setEntityManager(EntityManager entitymanager) {
        this.entitymanager = entitymanager;
    }


  private Locale getSiteLanguage() {
    Site site;
    try {
      Reference reference = entitymanager.newReference(siteEntityReference);
      site = siteservice.getSite(reference.getId());
    }
    catch (IdUnusedException e) {
        return null;
    }
    ResourceProperties props = site.getProperties();
    String locale_string = props.getProperty("locale_string");
    return locale_string == null ? null
        : LocaleUtil.parseLocale(locale_string);
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

  public void setSiteEntityReference(String siteEntityReference) {
    this.siteEntityReference = siteEntityReference;
  }

  public Object getObject() {
    Locale togo = getSiteLanguage();
    if (togo == null) {
      togo = getPreferencesLocale();
    }
    if (togo == null) {
      try {
        togo = (Locale) sessionmanager.getCurrentSession().getAttribute("locale");
      }
      catch (Exception e) {
      }
    }
    if (togo == null) {
      try {
        togo = servletrequest.getLocale();
      }
      catch (Exception e) {
      }
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
