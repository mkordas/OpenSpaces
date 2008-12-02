package org.openspaces.admin.lus.events;

import org.openspaces.admin.AdminEventListener;
import org.openspaces.admin.lus.LookupService;

/**
 * @author kimchy
 */
public interface LookupServiceAddedEventListener extends AdminEventListener {

    void lookupServiceAdded(LookupService lookupService);
}