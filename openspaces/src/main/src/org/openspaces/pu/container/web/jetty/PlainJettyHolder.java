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

package org.openspaces.pu.container.web.jetty;

import org.mortbay.jetty.Server;

/**
 * A plain wrapper around jetty. Simply deleagate the lifecycle calls directory to jetty.
 *
 * @author kimchy
 */
public class PlainJettyHolder implements JettyHolder {

    private Server server;

    public PlainJettyHolder(Server server) {
        this.server = server;
    }

    public void start() throws Exception {
        server.start();
    }

    public void stop() throws Exception {
        server.stop();
    }

    public Server getServer() {
        return server;
    }
}
