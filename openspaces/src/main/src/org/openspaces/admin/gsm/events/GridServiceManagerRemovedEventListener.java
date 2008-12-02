package org.openspaces.admin.gsm.events;

import org.openspaces.admin.AdminEventListener;
import org.openspaces.admin.gsm.GridServiceManager;

/**
 * @author kimchy
 */
public interface GridServiceManagerRemovedEventListener extends AdminEventListener {

    void gridServiceManagerRemoved(GridServiceManager gridServiceManager);
}