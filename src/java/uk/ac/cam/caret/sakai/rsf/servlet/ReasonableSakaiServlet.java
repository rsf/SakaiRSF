/*
 * Created on Feb 28, 2005
 */
package uk.ac.cam.caret.sakai.rsf.servlet;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.tool.api.Tool;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import uk.org.ponder.beanutil.BeanLocator;
import uk.org.ponder.rsac.RSACBeanLocator;
import uk.org.ponder.rsac.servlet.RSACUtils;
import uk.org.ponder.util.Logger;
import uk.org.ponder.util.UniversalRuntimeException;

/**
 * The main servlet to be used when handling an RSF servlet request through
 * Sakai.
 * @author Antranig Basman (antranig@caret.cam.ac.uk)
 * 
 */
public class ReasonableSakaiServlet extends HttpServlet {
  private RSACBeanLocator rsacbl;

  public void init(ServletConfig config) {
    try {
      super.init(config);

      WebApplicationContext wac = WebApplicationContextUtils
          .getWebApplicationContext(getServletContext());
      rsacbl = (RSACBeanLocator) wac.getBean("RSACBeanLocator");
    }
    catch (Throwable t) {
      Logger.log.warn("Error initialising tunnel servlet: ", t);
    }
  }

  protected void service(HttpServletRequest req, HttpServletResponse res) {
    try {
      req.setCharacterEncoding("UTF-8");
      // This line was added for Sakai 2.0
      req.setAttribute(Tool.NATIVE_URL, Tool.NATIVE_URL);

      String panelparam = req.getParameter("panel");
      if (panelparam != null && panelparam.equals("Title")) {
        String targetPage = "/jsp/Title.jsp";
        RequestDispatcher rd = getServletContext().getRequestDispatcher(
            targetPage);
        rd.forward(req, res);
        return;
      }
      
      RSACUtils.startServletRequest(req, res, 
          rsacbl, RSACUtils.HTTP_SERVLET_FACTORY);
      //A request bean locator just good for this request.
      BeanLocator rbl = rsacbl.getBeanLocator();
     
      // pass the request to RSF. 
      rbl.locateBean("rootHandlerBean");
    }
    catch (Throwable t) {
      Logger.log.warn("Error servicing RSF request ", t);
      try {
        res.getWriter().print(
            "[An error occurred handling this RSF request]");
        res.getWriter().close();
      }
      catch (Exception e) {
      }
      throw UniversalRuntimeException.accumulate(t,
          "Error servicing ToolSinkTunnel request ");
    }
    finally {
      rsacbl.endRequest();
    }

  }
}