/*
 * Created on May 29, 2006
 */
package uk.ac.cam.caret.sakai.rsf.locale;

import java.util.Locale;

import org.sakaiproject.tool.api.SessionManager;
import org.springframework.beans.factory.FactoryBean;
import org.sakaiproject.util.ResourceLoader;

/**
 * Determins the correct locale for the current request by calling the Sakai ResourceLoader
 * 
 * @author Antranig Basman (amb26@ponder.org.uk)
 * @author Matthew Jones (matthew@longsight.com)
 * 
 */
// See http://dev.ulan.jp/sakai/wiki/ResourceLoader
public class SakaiLocaleDeterminer implements FactoryBean {

  private SessionManager sessionmanager;

  public void setSessionManager(SessionManager sessionmanager) {
    this.sessionmanager = sessionmanager;
  }

  public Object getObject() {
	  String userid = sessionmanager.getCurrentSessionUserId();
	  Locale loc = Locale.getDefault();
	  if (userid != null) {
		  ResourceLoader rl = new ResourceLoader(); 
		  loc = rl.getLocale();
	  }
	  return loc;
  }
		
  public Class getObjectType() {
    return Locale.class;
  }

  public boolean isSingleton() {
    return true;
  }
}
