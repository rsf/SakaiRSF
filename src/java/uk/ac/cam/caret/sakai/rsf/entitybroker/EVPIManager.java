/*
 * Created on 18 May 2007
 */
package uk.ac.cam.caret.sakai.rsf.entitybroker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.entitybroker.EntityParse;

import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;
import uk.org.ponder.stringutil.StringGetter;
import uk.org.ponder.stringutil.StringList;

public class EVPIManager implements ViewParamsReporter {
  private List inferrers;
  private AccessRegistrar accessRegistrar;
  private StringGetter reference;
  private Map inferrermap = new HashMap();
  
  public void setAccessRegistrar(AccessRegistrar accessRegistrar) {
    this.accessRegistrar = accessRegistrar;
  }

  public void setEntityViewParamsInferrers(List inferrers) {
    this.inferrers = inferrers;
  }
  
  public void init() {
    StringList allprefixes = new StringList();
    if (inferrers != null && inferrers.size() > 0) {
      for (int i = 0; i < inferrers.size(); ++ i) {
        EntityViewParamsInferrer evpi = (EntityViewParamsInferrer) inferrers.get(i);
        String[] prefixes = evpi.getHandledPrefixes();
        allprefixes.append(prefixes);
        for (int j = 0; j < prefixes.length; ++ j) {
          inferrermap.put(prefixes[j], evpi);
        }
      }
    }
    accessRegistrar.registerPrefixes(allprefixes.toStringArray());
  }

  public void setSakaiReference(StringGetter reference) {
    this.reference = reference;
  }

  public ViewParameters getViewParameters() {
    String requestref = reference.get();
    EntityViewParamsInferrer evpi = null;
    if (!(requestref.equals(""))) {
      EntityParse parsed = new EntityParse(requestref);
      evpi = (EntityViewParamsInferrer) inferrermap.get(parsed.prefix);
    }
    return evpi == null? null : evpi.inferDefaultViewParameters(requestref);
  }
}
