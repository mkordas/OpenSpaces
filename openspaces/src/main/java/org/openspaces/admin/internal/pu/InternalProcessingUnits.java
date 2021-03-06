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
package org.openspaces.admin.internal.pu;

import org.openspaces.admin.internal.space.InternalSpace;
import org.openspaces.admin.pu.ProcessingUnit;
import org.openspaces.admin.pu.ProcessingUnits;
import org.openspaces.admin.pu.elastic.events.ElasticProcessingUnitEvent;

/**
 * @author kimchy
 */
public interface InternalProcessingUnits extends ProcessingUnits, InternalProcessingUnitInstancesAware {

    void addProcessingUnit(ProcessingUnit processingUnit);

    void removeProcessingUnit(String name);
    
    /**
     * If relevant raises events to relevant subscribers
     * @since 9.0.0
     * @author itaif
     */
    void processElasticScaleStrategyEvent(ElasticProcessingUnitEvent event);

    /**
     * Removes the reference to the specified space from the hosting processing unit.
     * @return the hosting processing unit from which the space was removed, or null if such a processing unit was not found
     * @since 9.1.0
     */
    ProcessingUnit removeEmbeddedSpace(InternalSpace space);

    /**
     * @return The planned number of instances. Works only for an elastic processing unit, returns null if no information is available.
     * @see ProcessingUnit#getPlannedNumberOfInstances()
     */
	Integer getPlannedNumberOfInstancesOfElasticPU(ProcessingUnit pu);
}
