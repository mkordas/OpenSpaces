/*******************************************************************************
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
 *******************************************************************************/
package org.openspaces.admin.zone.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


/**
 * @author elip
 * @since 9.1.0
 */
public class AtLeastOneZoneConfigurer implements ZonesConfigurer<AtLeastOneZoneConfig>{
    
    AtLeastOneZoneConfig config;
    Set<String> zones = new HashSet<String>();
    
    public AtLeastOneZoneConfigurer() {
        config = new AtLeastOneZoneConfig();
    }
    
    public AtLeastOneZoneConfigurer addZones(Set<String> zones) {
        this.zones.addAll(zones);
        return this;
    }
    
    public AtLeastOneZoneConfigurer addZones(String... zones) {
        this.zones.addAll(Arrays.asList(zones));
        return this;
    }
    
    public AtLeastOneZoneConfigurer addZone(String zone) {
        this.zones.add(zone);
        return this;
    }
    
    public AtLeastOneZoneConfig create() {
        if (zones.isEmpty()) {
            throw new IllegalArgumentException("No Zones Defined");
        }
        config.setZones(zones);
        config.validate();
        return config;
    }
}
