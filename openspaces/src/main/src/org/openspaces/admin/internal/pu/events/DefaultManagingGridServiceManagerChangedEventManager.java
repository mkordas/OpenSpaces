package org.openspaces.admin.internal.pu.events;

import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.support.GroovyHelper;
import org.openspaces.admin.pu.events.ManagingGridServiceManagerChangedEvent;
import org.openspaces.admin.pu.events.ManagingGridServiceManagerChangedEventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author kimchy
 */
public class DefaultManagingGridServiceManagerChangedEventManager implements InternalManagingGridServiceManagerChangedEventManager {

    private final InternalAdmin admin;

    private final List<ManagingGridServiceManagerChangedEventListener> listeners = new CopyOnWriteArrayList<ManagingGridServiceManagerChangedEventListener>();

    public DefaultManagingGridServiceManagerChangedEventManager(InternalAdmin admin) {
        this.admin = admin;
    }

    public void processingUnitManagingGridServiceManagerChanged(final ManagingGridServiceManagerChangedEvent event) {
        for (final ManagingGridServiceManagerChangedEventListener listener : listeners) {
            admin.pushEvent(listener, new Runnable() {
                public void run() {
                    listener.processingUnitManagingGridServiceManagerChanged(event);
                }
            });
        }
    }

    public void add(ManagingGridServiceManagerChangedEventListener eventListener) {
        listeners.add(eventListener);
    }

    public void remove(ManagingGridServiceManagerChangedEventListener eventListener) {
        listeners.add(eventListener);
    }

    public void plus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            add(new ClosureManagingGridServiceManagerChangedEventListener(eventListener));
        } else {
            add((ManagingGridServiceManagerChangedEventListener) eventListener);
        }
    }

    public void leftShift(Object eventListener) {
        plus(eventListener);
    }

    public void minus(Object eventListener) {
        if (GroovyHelper.isClosure(eventListener)) {
            remove(new ClosureManagingGridServiceManagerChangedEventListener(eventListener));
        } else {
            remove((ManagingGridServiceManagerChangedEventListener) eventListener);
        }
    }

    public void rightShift(Object eventListener) {
        minus(eventListener);
    }
}
