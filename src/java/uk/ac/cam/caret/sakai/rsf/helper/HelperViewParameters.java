package uk.ac.cam.caret.sakai.rsf.helper;


public interface HelperViewParameters {
	/**
	 * Name of the component, the value of which is the name of the sakai helper to call
	 */
	String HELPER_ID = "helper-id";
	
	/**
	 * Name of the component whose value is the method binding to call after the helper has returned.
	 * This is in order to infer the action result, if any is required.
	 */
	String POST_HELPER_BINDING = "helper-binding"; 
}
