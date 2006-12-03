/*
 * Created on Dec 2, 2006
 */
package uk.ac.cam.caret.sakai.rsf.genericdao;

import java.io.Serializable;

import org.sakaiproject.genericdao.api.GenericDao;

import uk.org.ponder.rsf.state.entity.EntityHandler;

public class GenericDAOEntityHandler implements EntityHandler {
  private GenericDao genericDAO;
  private Class persistentclass;

  public void setGenericDAO(GenericDao genericDAO) {
    this.genericDAO = genericDAO;
  }
  
  public void setPersistentClass(Class persistentclass) {
    this.persistentclass = persistentclass;
  }
  
  public boolean delete(Object key) {
    Object todelete = get(key);
    if (todelete != null) {
      genericDAO.delete(key);
      return true;
    }
    else return false;
  }

  public Object get(Object key) {
    return genericDAO.findById(persistentclass, (Serializable) key);
  }

  public void save(Object tosave) {
    genericDAO.save(tosave);
  }

}
