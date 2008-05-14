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

package org.openspaces.pu.container.servicegrid;

import com.sun.jini.config.Config;
import net.jini.config.ConfigurationException;
import net.jini.config.ConfigurationProvider;
import org.jini.rio.resources.client.AbstractFaultDetectionHandler;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A processing unit fault detection handler. Invokes {@link PUServiceBean#isAlive()}.
 *
 * @author kimchy
 */
public class PUFaultDetectionHandler extends AbstractFaultDetectionHandler {

    public static final String INVOCATION_DELAY_KEY = "invocationDelay";
    /**
     * Component name, used for config and logger
     */
    private static final String COMPONENT =
            "org.openspaces.pu.container.servicegrid.PUFaultDetectionHandler";
    /**
     * A Logger
     */
    static Logger logger = Logger.getLogger(COMPONENT);

    /**
     * @see org.jini.rio.core.FaultDetectionHandler#setConfiguration
     */
    public void setConfiguration(String[] configArgs) {
        if (configArgs == null)
            throw new NullPointerException("configArgs is null");
        try {
            this.configArgs = new String[configArgs.length];
            System.arraycopy(configArgs, 0, this.configArgs, 0, configArgs.length);

            this.config = ConfigurationProvider.getInstance(configArgs);

            invocationDelay = Config.getLongEntry(config,
                    COMPONENT,
                    INVOCATION_DELAY_KEY,
                    DEFAULT_INVOCATION_DELAY,
                    0,
                    Long.MAX_VALUE);
            retryCount = Config.getIntEntry(config,
                    COMPONENT,
                    RETRY_COUNT_KEY,
                    DEFAULT_RETRY_COUNT,
                    0,
                    Integer.MAX_VALUE);
            retryTimeout = Config.getLongEntry(config,
                    COMPONENT,
                    RETRY_TIMEOUT_KEY,
                    DEFAULT_RETRY_TIMEOUT,
                    0,
                    Long.MAX_VALUE);

            if (logger.isLoggable(Level.CONFIG)) {
                StringBuffer buffer = new StringBuffer();
                buffer.append("PUFaultDetectionHandler Properties : ");
                buffer.append("\n invocation delay=" + invocationDelay);
                buffer.append("\n retry count=" + retryCount + ", ");
                buffer.append("\n retry timeout=" + retryTimeout);
                buffer.append("\n configArgs: " + Arrays.toString( configArgs));
                logger.config(buffer.toString());
            }
        } catch (ConfigurationException e) {
            logger.log(Level.SEVERE, "Setting Configuration", e);
        }
    }

    /**
     * Get the class which implements the ServiceMonitor
     */
    protected ServiceMonitor getServiceMonitor() throws Exception {
        return new ServiceAdminManager();
    }

    class ServiceAdminManager implements ServiceMonitor {

        /**
         * Its all over
         */
        public void drop() {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Dropping monitor for service: [" + getServiceID() +"]" );
            }
        }

        /**
         * Verify service can be reached. If the service cannot be reached
         * return false
         */
        public boolean verify() {
            try {
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("Requesting isAlive() on service: [" + getServiceID() +"]" );
                }
                
                final boolean isAlive = ((PUServiceBean) proxy).isAlive();

                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("isAlive() succesfully returned: ["+isAlive+"] for service: [" + getServiceID() +"]" );
                }
                
                return isAlive;
            } catch (RemoteException e) {
                if(logger.isLoggable(Level.FINER)) {
                    logger.log( Level.FINER, "RemoteException reaching service: ["
                            + getServiceID() + "]  - service cannot be reached", e);
                }
                return false;
            } catch (Exception e) {
                if(logger.isLoggable(Level.WARNING)) {
                    logger.log( Level.WARNING, "Exception reaching service: ["
                            +  getServiceID() + "] ", e);
                }
                return false;
            }
        }
    }

}
