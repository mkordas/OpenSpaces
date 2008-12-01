package org.openspaces.admin.internal.lus;

import com.gigaspaces.jvm.JVMDetails;
import com.gigaspaces.jvm.JVMInfoProvider;
import com.gigaspaces.jvm.JVMStatistics;
import com.gigaspaces.lrmi.nio.info.NIODetails;
import com.gigaspaces.lrmi.nio.info.NIOInfoProvider;
import com.gigaspaces.lrmi.nio.info.NIOStatistics;
import com.gigaspaces.operatingsystem.OSDetails;
import com.gigaspaces.operatingsystem.OSInfoProvider;
import com.gigaspaces.operatingsystem.OSStatistics;
import net.jini.core.lookup.ServiceID;
import net.jini.core.lookup.ServiceRegistrar;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.support.AbstractGridComponent;

import java.rmi.RemoteException;

/**
 * @author kimchy
 */
public class DefaultLookupService extends AbstractGridComponent implements InternalLookupService {

    private ServiceRegistrar registrar;

    private ServiceID serviceID;

    public DefaultLookupService(ServiceRegistrar registrar, ServiceID serviceID, InternalAdmin admin) {
        super(admin);
        this.registrar = registrar;
        this.serviceID = serviceID;
    }

    public String getUid() {
        return getServiceID().toString();
    }

    public ServiceID getServiceID() {
        return this.serviceID;
    }

    public ServiceRegistrar getRegistrar() {
        return this.registrar;
    }

    public NIODetails getNIODetails() throws RemoteException {
        return ((NIOInfoProvider) registrar.getRegistrar()).getNIODetails();
    }

    public NIOStatistics getNIOStatistics() throws RemoteException {
        return ((NIOInfoProvider) registrar.getRegistrar()).getNIOStatistics();
    }

    public OSDetails getOSDetails() throws RemoteException {
        return ((OSInfoProvider) registrar.getRegistrar()).getOSConfiguration();
    }

    public OSStatistics getOSStatistics() throws RemoteException {
        return ((OSInfoProvider) registrar.getRegistrar()).getOSStatistics();
    }

    public JVMDetails getJVMDetails() throws RemoteException {
        return ((JVMInfoProvider) registrar.getRegistrar()).getJVMDetails();
    }

    public JVMStatistics getJVMStatistics() throws RemoteException {
        return ((JVMInfoProvider) registrar.getRegistrar()).getJVMStatistics();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultLookupService that = (DefaultLookupService) o;
        return serviceID.equals(that.serviceID);
    }

    @Override
    public int hashCode() {
        return serviceID.hashCode();
    }
}
