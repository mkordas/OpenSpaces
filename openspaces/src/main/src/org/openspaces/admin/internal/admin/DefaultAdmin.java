package org.openspaces.admin.internal.admin;

import com.gigaspaces.lrmi.nio.info.TransportConfiguration;
import net.jini.core.discovery.LookupLocator;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.GridServiceContainers;
import org.openspaces.admin.GridServiceManagers;
import org.openspaces.admin.LookupServices;
import org.openspaces.admin.Machines;
import org.openspaces.admin.Transports;
import org.openspaces.admin.internal.admin.gsc.DefaultGridServiceContainers;
import org.openspaces.admin.internal.admin.gsc.InternalGridServiceContainer;
import org.openspaces.admin.internal.admin.gsc.InternalGridServiceContainers;
import org.openspaces.admin.internal.admin.gsm.DefaultGridServiceManagers;
import org.openspaces.admin.internal.admin.gsm.InternalGridServiceManager;
import org.openspaces.admin.internal.admin.gsm.InternalGridServiceManagers;
import org.openspaces.admin.internal.admin.lus.DefaultLookupServices;
import org.openspaces.admin.internal.admin.lus.InternalLookupService;
import org.openspaces.admin.internal.admin.lus.InternalLookupServices;
import org.openspaces.admin.internal.admin.machine.DefaultMachine;
import org.openspaces.admin.internal.admin.machine.DefaultMachines;
import org.openspaces.admin.internal.admin.machine.InternalMachine;
import org.openspaces.admin.internal.admin.machine.InternalMachineAware;
import org.openspaces.admin.internal.admin.machine.InternalMachines;
import org.openspaces.admin.internal.admin.transport.DefaultTransport;
import org.openspaces.admin.internal.admin.transport.DefaultTransports;
import org.openspaces.admin.internal.admin.transport.InternalTransport;
import org.openspaces.admin.internal.admin.transport.InternalTransportInfoProvider;
import org.openspaces.admin.internal.admin.transport.InternalTransports;
import org.openspaces.admin.internal.discovery.DiscoveryService;

import java.rmi.RemoteException;

/**
 * @author kimchy
 */
public class DefaultAdmin implements InternalAdmin {

    private final DiscoveryService discoveryService;

    private final InternalLookupServices lookupServices = new DefaultLookupServices();

    private final InternalMachines machines = new DefaultMachines();

    private final InternalGridServiceManagers gridServiceManagers = new DefaultGridServiceManagers();

    private final InternalGridServiceContainers gridServiceContainers = new DefaultGridServiceContainers();

    private final InternalTransports transports = new DefaultTransports();

    public DefaultAdmin(String[] groups, LookupLocator[] locators) {
        this.discoveryService = new DiscoveryService(groups, locators, this);
    }

    public void start() {
        discoveryService.start();
    }

    public void stop() {
        discoveryService.stop();
    }

    public LookupServices getLookupServices() {
        return this.lookupServices;
    }

    public GridServiceManagers getGridServiceManagers() {
        return this.gridServiceManagers;
    }

    public GridServiceContainers getGridServiceContainers() {
        return this.gridServiceContainers;
    }

    public Machines getMachines() {
        return this.machines;
    }

    public Transports getTransports() {
        return this.transports;
    }

    public synchronized void addLookupService(InternalLookupService lookupService) {
        lookupServices.addLookupService(lookupService);
        TransportConfiguration txConfig = null;
        try {
            txConfig = lookupService.getTransportConfiguration();
        } catch (RemoteException e) {
            throw new AdminException("Failed to get transport configuration for lookup service [" + lookupService.getUID() + "]", e);
        }

        InternalTransport transport = processTransportOnServiceAddition(txConfig, lookupService);

        InternalMachine machine = processMachineOnServiceAddition(txConfig, lookupService, transport);

        ((InternalLookupServices) machine.getLookupServices()).addLookupService(lookupService);
    }

    public synchronized void removeLookupService(String uid) {
        InternalLookupService lookupService = lookupServices.removeLookupService(uid);
        if (lookupService != null) {
            processTransportOnServiceRemoval(lookupService, lookupService);
            ((InternalLookupServices) ((InternalMachine) lookupService.getMachine()).getLookupServices()).removeLookupService(uid);
        }
    }

    public synchronized void addGridServiceManager(InternalGridServiceManager gridServiceManager) {
        gridServiceManagers.addGridServiceManager(gridServiceManager);

        TransportConfiguration txConfig = null;
        try {
            txConfig = gridServiceManager.getTransportConfiguration();
        } catch (RemoteException e) {
            throw new AdminException("Failed to get transport configuration for grid service manager [" + gridServiceManager.getUID() + "]", e);
        }

        InternalTransport transport = processTransportOnServiceAddition(txConfig, gridServiceManager);

        InternalMachine machine = processMachineOnServiceAddition(txConfig, gridServiceManager, transport);

        ((InternalGridServiceManagers) machine.getGridServiceManagers()).addGridServiceManager(gridServiceManager);
    }

    public synchronized void removeGridServiceManager(String uid) {
        InternalGridServiceManager gridServiceManager = gridServiceManagers.removeGridServiceManager(uid);
        if (gridServiceManager != null) {
            processTransportOnServiceRemoval(gridServiceManager, gridServiceManager);
            ((InternalGridServiceManagers) ((InternalMachine) gridServiceManager.getMachine()).getGridServiceManagers()).removeGridServiceManager(uid);
        }
    }

    public synchronized void addGridServiceContainer(InternalGridServiceContainer gridServiceContainer) {
        gridServiceContainers.addGridServiceContainer(gridServiceContainer);
        TransportConfiguration txConfig = null;
        try {
            txConfig = gridServiceContainer.getTransportConfiguration();
        } catch (RemoteException e) {
            throw new AdminException("Failed to get transport configuration for grid service container [" + gridServiceContainer.getUID() + "]", e);
        }

        InternalTransport transport = processTransportOnServiceAddition(txConfig, gridServiceContainer);

        InternalMachine machine = processMachineOnServiceAddition(txConfig, gridServiceContainer, transport);

        ((InternalGridServiceContainers) machine.getGridServiceContainers()).addGridServiceContainer(gridServiceContainer);
    }

    public synchronized void removeGridServiceContainer(String uid) {
        InternalGridServiceContainer gridServiceContainer = gridServiceContainers.removeGridServiceContainer(uid);
        if (gridServiceContainer != null) {
            processTransportOnServiceRemoval(gridServiceContainer, gridServiceContainer);
            ((InternalGridServiceContainers) ((InternalMachine) gridServiceContainer.getMachine()).getGridServiceContainers()).removeGridServiceContainer(uid);
        }
    }

    private void processTransportOnServiceRemoval(InternalTransportInfoProvider txProvider, InternalMachineAware machineAware) {
        InternalTransport transport = ((InternalTransport) txProvider.getTransport());
        transport.removeTransportInfoProvider(txProvider);
        if (!transport.hasTransportInfoProviders()) {
            transports.removeTransport(transport.getUID());
            ((InternalTransports) machineAware.getMachine().getTransports()).removeTransport(transport.getUID());
        }
    }

    private InternalMachine processMachineOnServiceAddition(TransportConfiguration txConfig, InternalMachineAware machineAware, InternalTransport transport) {
        InternalMachine machine = (InternalMachine) machines.getMachineByHost(txConfig.getHost());
        if (machine == null) {
            machine = new DefaultMachine(txConfig.getHost(), txConfig.getHost());
            machines.addMachine(machine);
        }
        ((InternalTransports) machine.getTransports()).addTransport(transport);
        machineAware.setMachine(machine);
        return machine;
    }

    private InternalTransport processTransportOnServiceAddition(TransportConfiguration txConfig, InternalTransportInfoProvider txProvider) {
        InternalTransport transport = (InternalTransport) transports.getTransportByHostAndPort(txConfig.getHost(), txConfig.getPort());
        if (transport == null) {
            transport = new DefaultTransport(txConfig);
            transports.addTransport(transport);
        }
        transport.addTransportInfoProvider(txProvider);
        txProvider.setTransport(transport);
        return transport;
    }
}
