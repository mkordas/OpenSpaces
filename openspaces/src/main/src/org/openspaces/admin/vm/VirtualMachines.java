package org.openspaces.admin.vm;

import org.openspaces.admin.AdminAware;
import org.openspaces.admin.vm.events.VirtualMachineAddedEventManager;
import org.openspaces.admin.vm.events.VirtualMachineRemovedEventManager;

import java.util.Map;

/**
 * @author kimchy
 */
public interface VirtualMachines extends AdminAware, Iterable<VirtualMachine> {

    VirtualMachine[] getVirtualMachines();

    VirtualMachine getVirtualMachineByUID(String uid);

    Map<String, VirtualMachine> getUids();

    int getSize();

    boolean isEmpty();

    VirtualMachineAddedEventManager getVirtualMachineAdded();

    VirtualMachineRemovedEventManager getVirtualMachineRemoved();
}
