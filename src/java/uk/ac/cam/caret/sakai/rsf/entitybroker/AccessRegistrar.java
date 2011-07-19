/*
 * Created on 18 May 2007
 */
package uk.ac.cam.caret.sakai.rsf.entitybroker;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.access.EntityViewAccessProvider;
import org.sakaiproject.entitybroker.access.EntityViewAccessProviderManager;
import org.springframework.beans.factory.DisposableBean;

import uk.org.ponder.beanutil.WBLAcceptor;
import uk.org.ponder.beanutil.WriteableBeanLocator;
import uk.org.ponder.rsac.RSACBeanLocator;
import uk.org.ponder.rsac.servlet.RSACUtils;
import uk.org.ponder.util.Logger;
import uk.org.ponder.util.UniversalRuntimeException;

public class AccessRegistrar implements EntityViewAccessProvider, DisposableBean {

	private EntityViewAccessProviderManager accessProviderManager;
	private EntityBroker entityBroker;
	private RSACBeanLocator rsacbl;
	private String[] prefixes;
	private CommonAccessHandler accessHandler;

	public void setEntityBroker(EntityBroker entityBroker) {
		this.entityBroker = entityBroker;
	}

	public void setAccessProviderManager(
			EntityViewAccessProviderManager accessProviderManager) {
		this.accessProviderManager = accessProviderManager;
	}

	public void setRSACBeanLocator(RSACBeanLocator rsacbl) {
		this.rsacbl = rsacbl;
	}

	public void init() {
		accessHandler = makeCommonAccessHandler();
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
				accessProviderManager.unregisterProvider(prefixes[i]);
			}
		}
	}

	// Despite our very low profile of earlyRequestParser, it is still easier to
	// fake out pathInfo this way here. It is considered the authoritative
	// determiner by getBaseURL2.
	private HttpServletRequest wrapRequest(HttpServletRequest req) {
		String oldpathinfo = req.getPathInfo();
		EntityReference parsed = entityBroker.parseReference(oldpathinfo);
		int extent = parsed.toString().length();
		final String newpathinfo = extent < oldpathinfo.length()? 
				oldpathinfo.substring(parsed.toString().length()) : "";

				return new HttpServletRequestWrapper(req) {
					public String getPathInfo() {
						return newpathinfo; 
					}
				};
	}

	public void handleAccess (EntityView view, HttpServletRequest req, HttpServletResponse res) {

		EntityReference reference = view.getEntityReference();
		accessHandler.handleAccess(req, res, reference, null);
	}

	public CommonAccessHandler getCommonAccessHandler() {
		return accessHandler;
	}

	private CommonAccessHandler makeCommonAccessHandler() {
		return new CommonAccessHandler() {

			public void handleAccess(HttpServletRequest req, HttpServletResponse res,
					EntityReference reference, WBLAcceptor acceptor) {
				try {
					rsacbl.startRequest();
					// A request bean locator just good for this request.
					WriteableBeanLocator rbl = rsacbl.getBeanLocator();
					// inchuck entityReference
					rbl.set("sakai-EntityReference", reference.toString());
					if (acceptor != null) {
						acceptor.acceptWBL(rbl);
					}
					RSACUtils.startServletRequest(wrapRequest(req), res, rsacbl,
							RSACUtils.HTTP_SERVLET_FACTORY);
					// pass the request to RSF.
					rbl.locateBean("rootHandlerBean");
				}
				catch (Exception t) {
					// Access servlet performs no useful logging, do it here.
					Logger.log.error("Error handling access request", t);
					Throwable unwrapped = UniversalRuntimeException.unwrapException(t);
					if (unwrapped instanceof RuntimeException) {
						throw ((RuntimeException) unwrapped);
					}
					else 
						throw UniversalRuntimeException.accumulate(unwrapped, "Error handling access request");
				}
				finally {
					rsacbl.endRequest();
				}
			}

		};
	}


}
