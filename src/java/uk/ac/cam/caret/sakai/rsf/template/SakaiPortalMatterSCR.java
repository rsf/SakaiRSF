/*
 * Created on 14 Dec 2006
 */
package uk.ac.cam.caret.sakai.rsf.template;


import uk.org.ponder.rsf.renderer.ComponentRenderer;
import uk.org.ponder.rsf.renderer.RenderUtil;
import uk.org.ponder.rsf.renderer.scr.BasicSCR;
import uk.org.ponder.rsf.template.XMLLump;
import uk.org.ponder.xml.XMLWriter;

public class SakaiPortalMatterSCR implements BasicSCR {
  private String headmatter;

  public String getName() {
    return "portal-matter";
  }

  public void setHeadMatter(String headmatter) {
    this.headmatter = headmatter;
  }
  
  public int render(XMLLump lump, XMLWriter xmlw) {
    if (RenderUtil.isFirstSCR(lump, getName())) {
      xmlw.writeRaw(headmatter);
    }
    return ComponentRenderer.NESTING_TAG;
  }

}
