package org.openspaces.admin.vm;

/**
 * @author kimchy
 */
public interface VirtualMachines extends Iterable<VirtualMachine> {

    VirtualMachine[] getVirtualMachines();

    VirtualMachine getVirtualMachineByUID(String uid);

    int size();
}
