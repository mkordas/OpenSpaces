package org.openspaces.grid.gsm.strategy;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminException;
import org.openspaces.admin.alert.AlertFactory;
import org.openspaces.admin.alert.AlertSeverity;
import org.openspaces.admin.alert.AlertStatus;
import org.openspaces.admin.bean.BeanConfigurationException;
import org.openspaces.admin.gsc.GridServiceContainer;
import org.openspaces.admin.internal.admin.InternalAdmin;
import org.openspaces.admin.internal.pu.elastic.GridServiceContainerConfig;
import org.openspaces.admin.internal.pu.elastic.ProcessingUnitSchemaConfig;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.elastic.config.EagerScaleConfig;
import org.openspaces.grid.gsm.GridServiceContainerConfigAware;
import org.openspaces.grid.gsm.LogPerProcessingUnit;
import org.openspaces.grid.gsm.ProcessingUnitAware;
import org.openspaces.grid.gsm.SingleThreadedPollingLog;
import org.openspaces.grid.gsm.capacity.AggregatedAllocatedCapacity;
import org.openspaces.grid.gsm.containers.ContainersSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.containers.ContainersSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.containers.ContainersSlaPolicy;
import org.openspaces.grid.gsm.machines.EagerMachinesSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.machines.EagerMachinesSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.machines.EagerMachinesSlaPolicy;
import org.openspaces.grid.gsm.rebalancing.RebalancingSlaEnforcementEndpoint;
import org.openspaces.grid.gsm.rebalancing.RebalancingSlaEnforcementEndpointAware;
import org.openspaces.grid.gsm.rebalancing.RebalancingSlaPolicy;
import org.openspaces.grid.gsm.sla.ServiceLevelAgreementEnforcementEndpointDestroyedException;

public class EagerScaleStrategyBean 

    implements ScaleStrategyBean, 
               RebalancingSlaEnforcementEndpointAware , 
               ContainersSlaEnforcementEndpointAware, 
               EagerMachinesSlaEnforcementEndpointAware,
               ProcessingUnitAware,
               GridServiceContainerConfigAware,
               Runnable {

    private static final String rebalancingAlertGroupUidPrefix = "4499C1ED-1584-4387-90CF-34C5EC236644";
    private static final String containersAlertGroupUidPrefix = "47A94111-5665-4214-9F7A-2962D998DD12";
    private static final String machinesAlertGroupUidPrefix = "3BA87E89-449A-4abc-A632-4732246A9EE4";
    private static final int MAX_NUMBER_OF_MACHINES = 1000; // a very large number representing max number of machines per pu

    // injected 
    private InternalAdmin admin;
    private EagerScaleConfig slaConfig;
    private EagerMachinesSlaEnforcementEndpoint machinesEndpoint;
    private ContainersSlaEnforcementEndpoint containersEndpoint;
    private RebalancingSlaEnforcementEndpoint rebalancingEndpoint;
    private ProcessingUnit pu;
    private GridServiceContainerConfig containersConfig;
    private ProcessingUnitSchemaConfig schemaConfig;

    // created by afterPropertiesSet()
    private Log logger;
    private ScheduledFuture<?> scheduledTask;
    
    public Map<String, String> getProperties() {
        return slaConfig.getProperties();
    }

    public void setProcessingUnit(ProcessingUnit pu) {
        this.pu = pu;
    }

    public void setProcessingUnitSchema(ProcessingUnitSchemaConfig schemaConfig) {
        this.schemaConfig = schemaConfig;
    }
    
    public void setAdmin(Admin admin) {
        this.admin = (InternalAdmin) admin;
    }

    public void setEagerMachinesSlaEnforcementEndpoint(EagerMachinesSlaEnforcementEndpoint endpoint) {
        this.machinesEndpoint = endpoint;
    }
    
    public void setContainersSlaEnforcementEndpoint(ContainersSlaEnforcementEndpoint containersService) {
        this.containersEndpoint = containersService;
    }
    
    public void setRebalancingSlaEnforcementEndpoint(RebalancingSlaEnforcementEndpoint relocationService) {
        this.rebalancingEndpoint = relocationService;
    }

    public void setGridServiceContainerConfig(GridServiceContainerConfig containersConfig) {
         this.containersConfig = containersConfig;
    }
         
    public void afterPropertiesSet() {
        if (slaConfig == null) {
            throw new IllegalStateException("slaConfig cannot be null.");
        }
        
        logger = new LogPerProcessingUnit(
                    new SingleThreadedPollingLog(
                            LogFactory.getLog(EagerScaleStrategyBean.class)),
                    pu);
        logger.info("sla properties: "+slaConfig.toString());
        
        if (!schemaConfig.isPartitionedSync2BackupSchema() &&
                !schemaConfig.isDefaultSchema()) {
                throw new BeanConfigurationException(
                        "Processing Unit " + pu.getName() + " cannot scale eagerly, "+
                        "since it is not stateless, not stateful and not a datagrid (it is " + schemaConfig.getSchema() +") . "+
                        "Choose a different scale algorithm.");
            }

        scheduledTask = 
        (admin).scheduleWithFixedDelayNonBlockingStateChange(
                this, 
       0L, slaConfig.getPollingIntervalSeconds(), TimeUnit.SECONDS);
       logger.debug(pu.getName() + " is being monitored for SLA violations every " + slaConfig.getPollingIntervalSeconds() + " seconds");
    }

    public void destroy() {
        
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
            scheduledTask = null;
        }
    }

    public void setProperties(Map<String, String> properties) {
        slaConfig = new EagerScaleConfig(properties);       
    }

    public void run() {
        
        logger.debug("Enforcing sla for processing unit " + pu.getName());
        //TODO: Move this check to EsmImpl, this component should not be aware it is running in an ESM
        //TODO: Raise an alert
        int numberOfEsms = admin.getElasticServiceManagers().getSize();
        if (numberOfEsms != 1) {
            logger.error("Number of ESMs must be 1. Currently " + numberOfEsms + " running.");
            return;
        }
        
        try {
            logger.debug("Enforcing machines SLA.");
            boolean machinesSlaEnforced = enforceMachinesSla();
            if (logger.isDebugEnabled()) {
                if (!machinesSlaEnforced) {
                    logger.debug("Machines SLA has not been reached");
                }
            }
            if (machinesSlaEnforced) { 

                logger.debug("Enforcing containers SLA.");
                boolean containersSlaEnforced = enforceContainersSla();
                if (logger.isDebugEnabled()) {
                    if (!containersSlaEnforced) {
                        logger.debug("Containers SLA has not been reached");
                    }
                }
                
                if (containersSlaEnforced || 
                    containersEndpoint.isContainersPendingDeallocation()) {
                    logger.debug("Enforcing rebalancing SLA.");
                    boolean rebalancingSlaEnforced = enforceRebalancingSla(containersEndpoint.getContainers());
                    if (logger.isDebugEnabled()) {
                        if (!rebalancingSlaEnforced) {
                            logger.debug("Rebalancing SLA has not been reached");
                        }
                    }
                }
            }
        }
        catch (ServiceLevelAgreementEnforcementEndpointDestroyedException e) {
            logger.debug("AdminService was destroyed",e);
        }
        catch (AdminException e) {
            logger.warn("Unhandled AdminException",e);
        }
        catch (Exception e) {
            logger.error("Unhandled Exception",e);
        }
        
    }

    private boolean enforceMachinesSla() {
        
        final EagerMachinesSlaPolicy sla = getEagerMachinesSlaPolicy();

        boolean reachedSla = machinesEndpoint.enforceSla(sla);
        
        if (reachedSla) {
            triggerAlert(
                AlertSeverity.WARNING,
                AlertStatus.RESOLVED,
                machinesAlertGroupUidPrefix,
                "Machines Capacity SLA",
                "Machines Eager SLA has been reached. Using " + 
                machinesEndpoint.getAllocatedCapacity().getAgentUids().size() + " machines");
        }
        else {
            triggerAlert(
                AlertSeverity.WARNING,
                AlertStatus.RAISED,
                containersAlertGroupUidPrefix,
                "Machines Capacity SLA",
                "Machines Eager SLA has not reached its target");
        }
        
        return reachedSla;

    }

    private boolean enforceContainersSla() throws ServiceLevelAgreementEnforcementEndpointDestroyedException {
        
        AggregatedAllocatedCapacity allocatedCapacity = machinesEndpoint.getAllocatedCapacity();
        
        final ContainersSlaPolicy sla = new ContainersSlaPolicy();
        sla.setNewContainerConfig(containersConfig);
        sla.setAllocatedCapacity(allocatedCapacity);
        sla.setReservedMemoryCapacityPerMachineInMB(slaConfig.getReservedMemoryCapacityPerMachineInMB());
        
        // eager CPU
        // =========
        // We allow the case of a machine having only a backup instance, 
        // by calling getTotalNumberOfInstances() instead of getNumberOfInstances()
        // this check is not necessary since machinesEndpoint 
        // is already limiting number of machines by total number of instances.
        // but we do it anyway due to separation of concerns.
        int numberOfMachines = Math.min(pu.getTotalNumberOfInstances(),allocatedCapacity.getAgentUids().size());
        sla.setMinimumNumberOfMachines(numberOfMachines); 

        //eager memory
        //============
        //We ask for all available machines' memory.
        //If there are too many machines, we ask to spread out to the maximum number of containers (primaries+backups) 
        long memory = Math.min(
                machinesEndpoint.getAllocatedCapacity().getTotalAllocatedCapacity().getMemoryInMB(), 
                sla.getNewContainerConfig().getMaximumJavaHeapSizeInMB() * pu.getTotalNumberOfInstances());
        sla.setMemoryCapacityInMB(memory);

        if (logger.isDebugEnabled()) {
            logger.debug("Containers Eager SLA Policy: "+
                    "#gridServiceAgents=" + sla.getAllocatedCapacity().getAgentUids().size() + " "+
                    "#minimumNumberOfMachines="+sla.getMinimumNumberOfMachines()+" "+
                    "memoryCapacityInMB="+sla.getMemoryCapacityInMB()+" " +
                    "reservedMemoryCapacityPerMachineInMB="+sla.getReservedMemoryCapacityPerMachineInMB() + " "+
                    "newContainerConfig.maximumJavaHeapSizeInMB="+sla.getNewContainerConfig().getMaximumJavaHeapSizeInMB());
        }
        boolean reachedSla = containersEndpoint.enforceSla(sla);
        
        if (reachedSla) {
            triggerAlert(
                AlertSeverity.WARNING,
                AlertStatus.RESOLVED,
                containersAlertGroupUidPrefix,
                "Containers Capacity SLA",
                "Eager contains capacity for " + pu.getName() + " has been reached");
        }
        else {
            triggerAlert(
                AlertSeverity.WARNING,
                AlertStatus.RAISED,
                containersAlertGroupUidPrefix,
                "Containers Capacity SLA",
                "Contains capacity for " + pu.getName() + " " + 
                "Eager contains capacity for " + pu.getName() + " has not been reached yet.");
        }
        
        return reachedSla;
    }
    
    private boolean enforceRebalancingSla(GridServiceContainer[] containers) 
        throws ServiceLevelAgreementEnforcementEndpointDestroyedException 
    {
        RebalancingSlaPolicy sla = new RebalancingSlaPolicy();
        sla.setContainers(containers);
        sla.setMaximumNumberOfConcurrentRelocationsPerMachine(slaConfig.getMaxConcurrentRelocationsPerMachine());
        sla.setSchemaConfig(schemaConfig);
        boolean slaEnforced = rebalancingEndpoint.enforceSla(sla);
        
        if (slaEnforced) {
            triggerAlert(
                AlertSeverity.WARNING,
                AlertStatus.RESOLVED,
                rebalancingAlertGroupUidPrefix,
                "Processing Unit Rebalancing SLA",
                "Rebalancing of " + pu.getName() + " is complete.");
        }
        else {
            triggerAlert(
                AlertSeverity.WARNING,
                AlertStatus.RAISED,
                rebalancingAlertGroupUidPrefix,
                "Processing Unit Rebalancing SLA",
                "Rebalancing of " + pu.getName() + " is in progress.");
        }
        
        return slaEnforced;
    }

    private void triggerAlert(AlertSeverity severity, AlertStatus status, String alertGroupUidPrefix, String alertName, String alertDescription) {
        AlertFactory alertFactory = new AlertFactory();
        alertFactory.name(alertName);
        alertFactory.description(alertDescription);
        alertFactory.severity(severity);    
        alertFactory.status(status);
        alertFactory.componentUid(pu.getName());
        alertFactory.groupUid(alertGroupUidPrefix + "-" + pu.getName());
        admin.getAlertManager().triggerAlert(alertFactory.toAlert());
        logger.debug(alertDescription);
    }
    
    private int calcMinNumberOfMachinesForPartitionedDeployment(ProcessingUnit pu) {
        int minNumberOfMachines;
        if (pu.getMaxInstancesPerMachine() == 0) {
            minNumberOfMachines = 1;
            logger.info("minNumberOfMachines=1 (since max instances from same partition per machine is not defined)");
        }
        
        else {
            minNumberOfMachines = (int)Math.ceil(
                    (1 + pu.getNumberOfBackups())/(1.0*pu.getMaxInstancesPerMachine()));
            logger.info("minNumberOfMachines= " +
                    "ceil((1+backupsPerPartition)/maxInstancesPerPartitionPerMachine)= "+
                    "ceil("+(1+pu.getNumberOfBackups())+"/"+pu.getMaxInstancesPerMachine() + ")= " +
                    minNumberOfMachines);
        }
        
        return minNumberOfMachines;
    }
    
    private EagerMachinesSlaPolicy getEagerMachinesSlaPolicy() {
        final EagerMachinesSlaPolicy sla = new EagerMachinesSlaPolicy();
        sla.setAllowDeploymentOnManagementMachine(!slaConfig.getDedicatedManagementMachines());
        
        // calculate minimum number of machines
        
        if (schemaConfig.isPartitionedSync2BackupSchema()) {
            sla.setMaximumNumberOfMachines(pu.getTotalNumberOfInstances());
            sla.setMinimumNumberOfMachines(calcMinNumberOfMachinesForPartitionedDeployment(pu));
        }
        else {
            sla.setMinimumNumberOfMachines(1);
            sla.setMaximumNumberOfMachines(MAX_NUMBER_OF_MACHINES);
        }
        sla.setReservedMemoryCapacityPerMachineInMB(slaConfig.getReservedMemoryCapacityPerMachineInMB());
        sla.setContainerMemoryCapacityInMB(containersConfig.getMaximumJavaHeapSizeInMB());
        sla.setMachineZones(new HashSet<String>(Arrays.asList(slaConfig.getMachineZones())));
        return sla;
    }

}
