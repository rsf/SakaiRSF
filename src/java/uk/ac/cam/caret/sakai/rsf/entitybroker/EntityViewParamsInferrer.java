/*
 * Created on 18 May 2007
 */
package uk.ac.cam.caret.sakai.rsf.entitybroker;

import uk.org.ponder.rsf.viewstate.ViewParameters;

public interface EntityViewParamsInferrer {
  
  public String[] getHandledPrefixes();
  public ViewParameters inferDefaultViewParameters(String reference);
}
