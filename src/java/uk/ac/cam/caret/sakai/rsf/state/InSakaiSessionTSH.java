/*
 * Created on 06-Mar-2006
 */
package uk.ac.cam.caret.sakai.rsf.state;

import org.sakaiproject.api.kernel.session.SessionManager;
import org.sakaiproject.api.kernel.session.ToolSession;

import uk.org.ponder.rsf.state.TokenStateHolder;

/** A TokenStateHolder that stores flow state in the Sakai Tool-specific session.
 *
 * <p>This is, unexpectedly, an application scope bean. The ToolSession object
 * stored inside is an AOP alliance proxy for the actual request-scope session.
 * It is app-scope since alternative TSH implementations are also app-scope,
 * and cross-scope overriding is not supported. Also having one less request bean
 * is desirable.
 * <p>NB Expiryseconds not yet implemented. Would require *extra* server-side
 * storage of map of tokens to sessions, in order to save long-term storage 
 * within sessions - awaiting research from performance clients.
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 *
 */
public class InSakaiSessionTSH implements TokenStateHolder {
 // NB - this is a proxy of the request-scope session!!!
  private SessionManager sessionmanager;
  private int expiryseconds;

  public void setSessionManager(SessionManager sessionmanager) {
    this.sessionmanager = sessionmanager;
  }
  
  public Object getTokenState(String tokenID) {
    return sessionmanager.getCurrentToolSession().getAttribute(tokenID);
  }

  public void putTokenState(String tokenID, Object trs) {
    sessionmanager.getCurrentToolSession().setAttribute(tokenID, trs);
  }

  public void clearTokenState(String tokenID) {
    sessionmanager.getCurrentToolSession().removeAttribute(tokenID);
  }

  public void setExpirySeconds(int seconds) {
    this.expiryseconds = seconds;
  }
  
}
