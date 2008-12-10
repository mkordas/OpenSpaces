package org.openspaces.admin.internal.gsc;

import com.gigaspaces.grid.gsc.GSC;
import net.jini.core.lookup.ServiceID;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.internal.support.InternalAgentGridComponent;
import org.openspaces.admin.pu.ProcessingUnitInstance;

/**
 * @author kimchy
 */
public interface InternalGridServiceContainer extends GridServiceContainer, InternalAgentGridComponent {

    ServiceID getServiceID();

    GSC getGSC();

    void addProcessingUnitInstance(ProcessingUnitInstance processingUnitInstance);

    void removeProcessingUnitInstance(String uid);
}