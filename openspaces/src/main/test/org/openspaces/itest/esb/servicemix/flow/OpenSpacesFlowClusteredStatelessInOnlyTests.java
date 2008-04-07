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
package org.openspaces.itest.esb.servicemix.flow;

import org.apache.servicemix.jbi.container.ActivationSpec;
import org.apache.servicemix.jbi.resolver.ServiceNameEndpointResolver;
import org.apache.servicemix.tck.ReceiverComponent;

/**
 * @author yitzhaki
 */
public class OpenSpacesFlowClusteredStatelessInOnlyTests extends OpenSpacesFlowAbstractTest {

    /**
     * Test case async Stateless message sending.
     * meaning that clustered sender can get the response.
     *
     * @throws Exception
     */
    public void test() throws Exception {
        final StatelessSenderComponent sender1 = new StatelessSenderComponent();
        final StatelessSenderComponent sender2 = new StatelessSenderComponent();
        final ReceiverComponent receiver1 = new ReceiverComponent();
        final ReceiverComponent receiver2 = new ReceiverComponent();
        sender2.setResolver(new ServiceNameEndpointResolver(ReceiverComponent.SERVICE));

        receiverContainer.activateComponent(new ActivationSpec("sender", sender1));
        senderContainer.activateComponent(new ActivationSpec("sender", sender2));
        senderContainer.activateComponent(new ActivationSpec("receiver", receiver1));
        receiverContainer.activateComponent(new ActivationSpec("receiver", receiver2));
        Thread.sleep(1000);

        sender2.sendMessages(NUM_MESSAGES);
        Thread.sleep(3000);

        assertTrue(receiver1.getMessageList().hasReceivedMessage());
        assertTrue(receiver2.getMessageList().hasReceivedMessage());
        receiver1.getMessageList().flushMessages();
        receiver2.getMessageList().flushMessages();

        assertTrue(sender1.getResponseMessageList().hasReceivedMessage());
        assertTrue(sender2.getResponseMessageList().hasReceivedMessage());
    }

}
