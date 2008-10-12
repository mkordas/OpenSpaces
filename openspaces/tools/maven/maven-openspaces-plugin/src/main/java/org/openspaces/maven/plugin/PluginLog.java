/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openspaces.maven.plugin;

import org.apache.maven.plugin.logging.Log;

/**
 * PluginLog
 * 
 * Convenience class to provide access to the plugin
 * Log for non-mojo classes.
 *
 */
public class PluginLog
{
    private static Log log = null;
    
    public static void setLog(Log l)
    {
        log = l;
    }
    
    public static Log getLog()
    {
        return log;
    }

}
