package uk.ac.cam.caret.sakai.rsf.helper;

import uk.org.ponder.rsf.processor.HandlerHook;
import uk.org.ponder.rsf.viewstate.ViewParameters;

public class HelperHandlerHook implements HandlerHook {

	private HandlerHook hh;
	private ViewParameters viewParameters;
	private HelperHandlerHookBean hhhb;
	
	public boolean handle() {
		if (viewParameters instanceof HelperViewParameters) {
			return hhhb.handle();
		}
		return ((hh != null) ? hh.handle() : false);
	}
	
	public void setHandlerHook(HandlerHook handlerhook) {
		this.hh = handlerhook;
	}

	public void setViewParameters(ViewParameters viewParameters) {
		this.viewParameters = viewParameters;
	}
	
	public void setHelperHandlerHookBean(HelperHandlerHookBean hhhb) {
		this.hhhb = hhhb;
	}
	

}
