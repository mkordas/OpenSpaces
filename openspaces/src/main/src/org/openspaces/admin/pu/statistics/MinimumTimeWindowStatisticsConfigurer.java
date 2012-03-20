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
package org.openspaces.admin.pu.statistics;

import java.util.concurrent.TimeUnit;

import org.openspaces.admin.internal.pu.statistics.DefaultTimeWindowStatisticsConfigUtils;

/**
 * Creates a new {@link MinimumTimeWindowStatisticsConfig} object
 * @author itaif
 * @since 9.0.0
 */
public class MinimumTimeWindowStatisticsConfigurer {

    private MinimumTimeWindowStatisticsConfig config = new MinimumTimeWindowStatisticsConfig();
    
    public MinimumTimeWindowStatisticsConfigurer timeWindow(long timeWindow, TimeUnit timeUnit) {
        DefaultTimeWindowStatisticsConfigUtils.timeWindow(config, timeWindow, timeUnit);
        return this;
    }
    
    public MinimumTimeWindowStatisticsConfigurer minimumTimeWindow(long timeWindow, TimeUnit timeUnit) {
        DefaultTimeWindowStatisticsConfigUtils.minimumTimeWindow(config, timeWindow, timeUnit);
        return this;
    }
    
    public MinimumTimeWindowStatisticsConfigurer maximumTimeWindow(long timeWindow, TimeUnit timeUnit) {
        DefaultTimeWindowStatisticsConfigUtils.maximumTimeWindow(config, timeWindow, timeUnit);
        return this;
    }
        
    public MinimumTimeWindowStatisticsConfig create() {
        DefaultTimeWindowStatisticsConfigUtils.create(config);
        return config;
    }

}
