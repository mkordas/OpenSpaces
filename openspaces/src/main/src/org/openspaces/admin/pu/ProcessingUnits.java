package org.openspaces.admin.pu;

import org.openspaces.admin.Admin;
import org.openspaces.admin.pu.events.BackupGridServiceManagerChangedEventManager;
import org.openspaces.admin.pu.events.ManagingGridServiceManagerChangedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitAddedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceAddedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceLifecycleEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitInstanceRemovedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitLifecycleEventListener;
import org.openspaces.admin.pu.events.ProcessingUnitRemovedEventManager;
import org.openspaces.admin.pu.events.ProcessingUnitStatusChangedEventManager;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author kimchy
 */
public interface ProcessingUnits extends Iterable<ProcessingUnit> {

    Admin getAdmin();

    int getSize();

    boolean isEmpty();

    ProcessingUnit[] getProcessingUnits();

    ProcessingUnit getProcessingUnit(String name);

    Map<String, ProcessingUnit> getNames();

    ProcessingUnit waitFor(String processingUnitName);

    ProcessingUnit waitFor(String processingUnitName, long timeout, TimeUnit timeUnit);

    void addLifecycleListener(ProcessingUnitLifecycleEventListener eventListener);

    void removeLifecycleListener(ProcessingUnitLifecycleEventListener eventListener);

    void addLifecycleListener(ProcessingUnitInstanceLifecycleEventListener eventListener);

    void removeLifecycleListener(ProcessingUnitInstanceLifecycleEventListener eventListener);

    ProcessingUnitAddedEventManager getProcessingUnitAdded();

    ProcessingUnitRemovedEventManager getProcessingUnitRemoved();

    ManagingGridServiceManagerChangedEventManager getManagingGridServiceManagerChanged();

    BackupGridServiceManagerChangedEventManager getBackupGridServiceManagerChanged();

    ProcessingUnitStatusChangedEventManager getProcessingUnitStatusChanged();

    ProcessingUnitInstanceAddedEventManager getProcessingUnitInstanceAdded();

    ProcessingUnitInstanceRemovedEventManager getProcessingUnitInstanceRemoved();
}
