/*
 * Created on 31 Oct 2006
 */
package uk.ac.cam.caret.sakai.rsf.util;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.tool.api.ToolManager;

public class PermissionChecker {
  private SecurityService securityService = null;
  private ToolManager toolmanager;

  public void setSecurityService(SecurityService service) {
    securityService = service;
  }

  public void setToolManager(ToolManager toolmanager) {
    this.toolmanager = toolmanager;
  }
  
  public boolean checkLockOnCurrentUserAndContext(String authzfunction) {
    String context = toolmanager.getCurrentPlacement().getContext();
    return securityService.unlock(authzfunction, context);
  }

}
