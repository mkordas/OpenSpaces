/*******************************************************************************
 * 
 * Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *  
 ******************************************************************************/
package org.openspaces.grid.gsm.machines.exceptions;

import org.openspaces.admin.internal.machine.events.DefaultElasticMachineProvisioningFailureEvent;
import org.openspaces.admin.internal.pu.elastic.events.InternalElasticProcessingUnitFailureEvent;
import org.openspaces.admin.machine.Machine;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.grid.gsm.machines.MachinesSlaUtils;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementFailure;
import org.openspaces.grid.gsm.sla.exceptions.SlaEnforcementLoggerBehavior;

public class ExpectedMachineWithMoreMemoryException extends MachinesSlaEnforcementInProgressException implements SlaEnforcementFailure , SlaEnforcementLoggerBehavior{

    private static final long serialVersionUID = 1L;
    
    public ExpectedMachineWithMoreMemoryException(ProcessingUnit pu, Exception cause) {
        super(pu, "Machine provisioning failed to start a new machine. Cause:" + cause.getMessage(), cause);
    }
    
    public ExpectedMachineWithMoreMemoryException(ProcessingUnit pu,
			Machine machine, long totalMB, long reservedMB, long containerMB) {
    	super(pu, message(machine, totalMB, reservedMB, containerMB));
	}

	private static String message(Machine machine, long totalMB, long reservedMB, long containerMB) {
		return "Expected machine with more memory. Machine " +MachinesSlaUtils.machineToString(machine) + " has been started with not enough memory. Actual total memory is " +
                + totalMB + "MB. Which is less than (reserved + container) = (" + reservedMB +"MB+" + 
				containerMB + "MB) = " + (reservedMB + containerMB) + "MB";
	}

	@Override
    public InternalElasticProcessingUnitFailureEvent toEvent() {
        DefaultElasticMachineProvisioningFailureEvent event = new DefaultElasticMachineProvisioningFailureEvent(); 
        event.setFailureDescription(getMessage());
        event.setProcessingUnitName(getProcessingUnitName());
        return event;
    }
    @Override
    public boolean isAlwaysLogStackTrace() {
        return true;
    }

    @Override
    public boolean isAlwaysLogDuplicateException() {
        return true;
    }
}
