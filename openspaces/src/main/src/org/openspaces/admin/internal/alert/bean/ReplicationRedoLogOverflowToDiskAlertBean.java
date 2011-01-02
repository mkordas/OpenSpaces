package org.openspaces.admin.internal.alert.bean;

import java.util.Map;

import org.openspaces.admin.Admin;
import org.openspaces.admin.alert.Alert;
import org.openspaces.admin.alert.AlertFactory;
import org.openspaces.admin.alert.AlertSeverity;
import org.openspaces.admin.alert.AlertStatus;
import org.openspaces.admin.alert.alerts.ReplicationRedoLogOverflowToDiskAlert;
import org.openspaces.admin.alert.alerts.ReplicationRedoLogSizeAlert;
import org.openspaces.admin.alert.config.ReplicationRedoLogSizeAlertBeanConfig;
import org.openspaces.admin.internal.alert.AlertHistory;
import org.openspaces.admin.internal.alert.AlertHistoryDetails;
import org.openspaces.admin.internal.alert.InternalAlertManager;
import org.openspaces.admin.space.ReplicationStatus;
import org.openspaces.admin.space.ReplicationTarget;
import org.openspaces.admin.space.SpaceInstance;
import org.openspaces.admin.space.events.SpaceInstanceRemovedEventListener;
import org.openspaces.admin.space.events.SpaceInstanceStatisticsChangedEvent;
import org.openspaces.admin.space.events.SpaceInstanceStatisticsChangedEventListener;

import com.gigaspaces.cluster.activeelection.SpaceMode;
import com.j_spaces.core.filters.ReplicationStatistics;
import com.j_spaces.core.filters.ReplicationStatistics.OutgoingReplication;

public class ReplicationRedoLogOverflowToDiskAlertBean implements AlertBean, SpaceInstanceRemovedEventListener, SpaceInstanceStatisticsChangedEventListener {

    public static final String beanUID = "3519ba78-08e6de85-87dc-4c10-8d08-ef03fe7b5d76";
    public static final String ALERT_NAME = "Replication Redo log Overflow";
    
    private final ReplicationRedoLogSizeAlertBeanConfig config = new ReplicationRedoLogSizeAlertBeanConfig();

    private Admin admin;

    public ReplicationRedoLogOverflowToDiskAlertBean() {
    }

    public void afterPropertiesSet() throws Exception {
        validateProperties();
        admin.getSpaces().getSpaceInstanceRemoved().add(this);
        admin.getSpaces().getSpaceInstanceStatisticsChanged().add(this);
        admin.getSpaces().startStatisticsMonitor();
    }

    public void destroy() throws Exception {
        admin.getSpaces().getSpaceInstanceRemoved().remove(this);
        admin.getSpaces().getSpaceInstanceStatisticsChanged().remove(this);
        admin.getSpaces().stopStatisticsMonitor();
    }

    public Map<String, String> getProperties() {
        return config.getProperties();
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public void setProperties(Map<String, String> properties) {
        config.setProperties(properties);
    }

    private void validateProperties() {
    }
    
    public void spaceInstanceRemoved(SpaceInstance spaceInstance) {
        final String groupUid = generateGroupUid(spaceInstance.getUid());
        AlertFactory factory = new AlertFactory();
        factory.name(ALERT_NAME);
        factory.groupUid(groupUid);
        factory.description("Replication redo log is unvailable; " + getSpaceName(spaceInstance) + " has been removed.");
        factory.severity(AlertSeverity.WARNING);
        factory.status(AlertStatus.NA);
        factory.componentUid(spaceInstance.getUid());
        factory.config(config.getProperties());

        Alert alert = factory.toAlert();
        admin.getAlertManager().fireAlert( new ReplicationRedoLogOverflowToDiskAlert(alert));
    }
    
    
    public void spaceInstanceStatisticsChanged(SpaceInstanceStatisticsChangedEvent event) {
        
        final SpaceInstance source = event.getSpaceInstance();
        final ReplicationStatistics replicationStatistics = event.getStatistics().getReplicationStatistics();
        if (replicationStatistics == null) return;
        
        OutgoingReplication outgoingReplication = replicationStatistics.getOutgoingReplication();
        long redoLogSize = outgoingReplication.getRedoLogSize();
        long redoLogSizeInDisk = outgoingReplication.getRedoLogExternalStoragePacketCount();
        long redoLogSizeInMemory = outgoingReplication.getRedoLogMemoryPacketCount();
        
        if (redoLogSizeInDisk > 0) {
            final String groupUid = generateGroupUid(source.getUid());
            AlertFactory factory = new AlertFactory();
            factory.name(ALERT_NAME);
            factory.groupUid(groupUid);
            factory.description("Replication redo-log has overflown to disk, for " + getSpaceName(source));
            factory.severity(AlertSeverity.WARNING);
            factory.status(AlertStatus.RAISED);
            factory.componentUid(source.getUid());
            factory.config(config.getProperties());
            
            factory.putProperty(ReplicationRedoLogSizeAlert.HOST_ADDRESS, source.getMachine().getHostAddress());
            factory.putProperty(ReplicationRedoLogSizeAlert.HOST_NAME, source.getMachine().getHostName());
            factory.putProperty(ReplicationRedoLogSizeAlert.CPU_UTILIZATION, String.valueOf(source.getOperatingSystem().getStatistics().getCpuPerc()*100.0));
            factory.putProperty(ReplicationRedoLogSizeAlert.HEAP_UTILIZATION, String.valueOf(source.getVirtualMachine().getStatistics().getMemoryHeapUsedPerc()));
            
            factory.putProperty(ReplicationRedoLogSizeAlert.REPLICATION_STATUS, getReplicationStatus(source));
            factory.putProperty(ReplicationRedoLogSizeAlert.SOURCE_UID, source.getUid());
            
            factory.putProperty(ReplicationRedoLogSizeAlert.REDO_LOG_SIZE, String.valueOf(redoLogSize));
            factory.putProperty(ReplicationRedoLogSizeAlert.REDO_LOG_MEMORY_SIZE, String.valueOf(redoLogSizeInMemory));
            factory.putProperty(ReplicationRedoLogSizeAlert.REDO_LOG_SWAP_SIZE, String.valueOf(redoLogSizeInDisk));

            Alert alert = factory.toAlert();
            admin.getAlertManager().fireAlert( new ReplicationRedoLogOverflowToDiskAlert(alert));
            
        } else if (redoLogSizeInDisk == 0 && redoLogSize >= 0){
            final String groupUid = generateGroupUid(source.getUid());
            AlertHistory alertHistory = ((InternalAlertManager)admin.getAlertManager()).getAlertRepository().getAlertHistoryByGroupUid(groupUid);
            AlertHistoryDetails alertHistoryDetails = alertHistory.getDetails();
            if (alertHistoryDetails != null && !alertHistoryDetails.getLastAlertStatus().isResolved()) {
                AlertFactory factory = new AlertFactory();
                factory.name(ALERT_NAME);
                factory.groupUid(groupUid);
                factory.description("Replication redo-log no longer uses the disk, for " + getSpaceName(source));
                factory.severity(AlertSeverity.WARNING);
                factory.status(AlertStatus.RESOLVED);
                factory.componentUid(event.getSpaceInstance().getUid());
                factory.config(config.getProperties());
                
                factory.putProperty(ReplicationRedoLogSizeAlert.HOST_ADDRESS, source.getMachine().getHostAddress());
                factory.putProperty(ReplicationRedoLogSizeAlert.HOST_NAME, source.getMachine().getHostName());
                factory.putProperty(ReplicationRedoLogSizeAlert.CPU_UTILIZATION, String.valueOf(source.getOperatingSystem().getStatistics().getCpuPerc()*100.0));
                factory.putProperty(ReplicationRedoLogSizeAlert.HEAP_UTILIZATION, String.valueOf(source.getVirtualMachine().getStatistics().getMemoryHeapUsedPerc()));
                
                factory.putProperty(ReplicationRedoLogSizeAlert.REPLICATION_STATUS, getReplicationStatus(source));
                factory.putProperty(ReplicationRedoLogSizeAlert.SOURCE_UID, source.getUid());
                
                factory.putProperty(ReplicationRedoLogSizeAlert.REDO_LOG_SIZE, String.valueOf(redoLogSize));
                factory.putProperty(ReplicationRedoLogSizeAlert.REDO_LOG_MEMORY_SIZE, String.valueOf(redoLogSizeInMemory));
                factory.putProperty(ReplicationRedoLogSizeAlert.REDO_LOG_SWAP_SIZE, String.valueOf(redoLogSizeInDisk));

                Alert alert = factory.toAlert();
                admin.getAlertManager().fireAlert(new ReplicationRedoLogOverflowToDiskAlert(alert));
            }
        }
    }
    
    private String getReplicationStatus(SpaceInstance source) {
        for (ReplicationTarget target : source.getReplicationTargets()) {
            if (!ReplicationStatus.ACTIVE.equals(target.getReplicationStatus())) {
                return target.getReplicationStatus().name();
            }
        }
        return ReplicationStatus.ACTIVE.name();
    }

    private String getSpaceName(SpaceInstance source) {
        StringBuilder sb = new StringBuilder();
        SpaceMode sourceSpaceMode = source.getMode();
        sb.append(sourceSpaceMode.toString().toLowerCase()).append(" Space ");
        sb.append(source.getSpace().getName() + "." + source.getInstanceId() + " ["+(source.getBackupId()+1)+"]");
        return sb.toString();
    }

    private String generateGroupUid(String uid) {
        return beanUID.concat("-").concat(uid);
    }
}
