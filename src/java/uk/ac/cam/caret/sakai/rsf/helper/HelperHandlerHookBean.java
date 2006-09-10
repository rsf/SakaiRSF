package uk.ac.cam.caret.sakai.rsf.helper;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.tool.api.ActiveTool;
import org.sakaiproject.tool.api.ActiveToolManager;
import org.sakaiproject.tool.api.ToolException;

import uk.org.ponder.beanutil.BeanLocator;
import uk.org.ponder.beanutil.BeanModelAlterer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterpreter;
import uk.org.ponder.rsf.preservation.StatePreservationManager;
import uk.org.ponder.rsf.state.TokenStateHolder;
import uk.org.ponder.rsf.view.View;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.view.ViewResolver;
import uk.org.ponder.rsf.viewstate.BaseURLProvider;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewStateHandler;
import uk.org.ponder.util.Logger;
import uk.org.ponder.util.UniversalRuntimeException;

/**
 * 
 * @author Andrew Thornton
 */
public class HelperHandlerHookBean {
	
	private static final String TOKEN_STATE_PREFIX = "HelperHandlerHook";
	private static final String HELPER_FINISHED_PATH = "/done";
	private static final String IN_HELPER_PATH = "/tool";
	
	private HttpServletResponse response;
	private HttpServletRequest request;
	private ViewParameters viewParameters;
	private ViewResolver viewResolver;
	private StatePreservationManager statePreservationManager;
	private TokenStateHolder tsh;
	private ViewStateHandler vsh;
	private BeanModelAlterer bma;
	private BeanLocator beanLocator;
	private ActionResultInterpreter ari;
	private ActiveToolManager activeToolManager;
	private BaseURLProvider bup;
	private String pathInfo;

	public boolean handle() {
		String viewID = viewParameters.viewID;
		Logger.log.info("Handling view: " + viewID);
		
		String pathBeyondViewID = "" ;
		if (pathInfo.length() > viewID.length() + 1) {
			pathBeyondViewID = pathInfo.substring(viewParameters.viewID.length() + 1);
		}
		if (Logger.log.isInfoEnabled()) {
			Logger.log.info("pathInfo: " + pathInfo + " pathBeyondViewID: " + pathBeyondViewID);
		}
		if (pathBeyondViewID.startsWith(HELPER_FINISHED_PATH)) {
			return handleHelperDone();
		}
		
		if (pathBeyondViewID.startsWith(IN_HELPER_PATH)) {
			return handleHelperHelper(pathBeyondViewID);
		}
		
		return handleHelperStart();
	}
	
	
	private boolean handleHelperDone() {
		Logger.log.info("Done handling helper in view: " + viewParameters.viewID);
		
		String methodBinding = (String) tsh.getTokenState(TOKEN_STATE_PREFIX + viewParameters.viewID + HelperViewParameters.POST_HELPER_BINDING);
		statePreservationManager.scopeRestore();
		Object beanReturn = methodBinding == null ? null : bma.invokeBeanMethod(methodBinding, beanLocator);
		
		ARIResult ariresult = ari.interpretActionResult(viewParameters, beanReturn);
		
		String urlToRedirectTo = vsh.getFullURL(ariresult.resultingview);
		try {
			response.sendRedirect(urlToRedirectTo);
		} catch (IOException e) {
			throw UniversalRuntimeException.accumulate(e, "Error redirecting to view: " + ariresult.resultingview.viewID + " url: " + urlToRedirectTo);
		}
		return true;
	}

	private boolean handleHelperHelper(final String pathBeyondViewID) {
		Logger.log.info("Handling helper in view: " + viewParameters.viewID);
		
		String helperId = (String) tsh.getTokenState(TOKEN_STATE_PREFIX + viewParameters.viewID + HelperViewParameters.HELPER_ID);
		
		ActiveTool helperTool = activeToolManager.getActiveTool(helperId);

		String baseUrl = bup.getBaseURL();
		int hostStart = baseUrl.indexOf("://") + 3;
		int hostEnd = baseUrl.indexOf('/', hostStart);
		
		
		String contextPath = baseUrl.substring(hostEnd);
		contextPath += viewParameters.viewID;
		contextPath += IN_HELPER_PATH;
		String helperPathInfo = pathInfo.substring(1 + viewParameters.viewID.length() + IN_HELPER_PATH.length());
		
		// this is the forward call
		try {
			helperTool.help(request, response, contextPath, helperPathInfo);
		} catch (ToolException e) {
			throw UniversalRuntimeException.accumulate(e, "ToolException when trying to call help. HelperId: " + helperId + " contextPath: " + contextPath + " pathInfo: " + pathInfo);
		}
		
		return true;
	}
	
	private boolean handleHelperStart() {
		Logger.log.info("Handling helper start in view: " + viewParameters.viewID);
		View view = new View();
		List producersList = viewResolver.getProducers(viewParameters.viewID);
		if (producersList.size() != 1) {
			throw new IllegalArgumentException("There is not exactly one view producer for the view: " + viewParameters.viewID);
		}
		ViewComponentProducer vp = (ViewComponentProducer) producersList.get(0);
		
		statePreservationManager.scopeRestore();
		vp.fillComponents(view.viewroot, viewParameters, null);
		statePreservationManager.scopePreserve();
		UIOutput helperId = (UIOutput) view.viewroot.getComponent(HelperViewParameters.HELPER_ID);
		UICommand helperBinding = (UICommand) view.viewroot.getComponent(HelperViewParameters.POST_HELPER_BINDING);
		tsh.putTokenState(TOKEN_STATE_PREFIX + viewParameters.viewID + HelperViewParameters.HELPER_ID, helperId.getValue());
		if (helperBinding != null) {
			tsh.putTokenState(TOKEN_STATE_PREFIX + viewParameters.viewID + HelperViewParameters.POST_HELPER_BINDING, helperBinding.methodbinding);
		}

		String helperToolPath = bup.getBaseURL() + viewParameters.viewID + IN_HELPER_PATH;
		
		try {
			response.sendRedirect(helperToolPath);
		} catch (IOException e) {
			throw UniversalRuntimeException.accumulate(e, "IOException when trying to redirect to helper tool");
		}
		
		return true;
	}

	public void setActiveToolManager(ActiveToolManager activeToolManager) {
		this.activeToolManager = activeToolManager;
	}

	public void setActionResultInterpreter(ActionResultInterpreter ari) {
		this.ari = ari;
	}

	public void setBeanLocator(BeanLocator beanLocator) {
		this.beanLocator = beanLocator;
	}

	public void setBeanModelAlterer(BeanModelAlterer bma) {
		this.bma = bma;
	}

	public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
		this.request = httpServletRequest;
	}

	public void setHttpServletResponse(HttpServletResponse response) {
		this.response = response;
	}

	public void setStatePreservationManager(
			StatePreservationManager statePreservationManager) {
		this.statePreservationManager = statePreservationManager;
	}

	public void setTokenStateHolder(TokenStateHolder tsh) {
		this.tsh = tsh;
	}

	public void setViewParameters(ViewParameters viewParameters) {
		this.viewParameters = viewParameters;
	}

	public void setViewResolver(ViewResolver viewResolver) {
		this.viewResolver = viewResolver;
	}

	public void setViewStateHandler(ViewStateHandler vsh) {
		this.vsh = vsh;
	}

	public void setBaseURLProvider(BaseURLProvider bup) {
		this.bup = bup;
	}
	
	public void setPathInfo(String pathInfo) {
		this.pathInfo = pathInfo;
	}
}
