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
package org.openspaces.admin.space;

/**
 * Aggregated transaction details of all the currently discovered {@link org.openspaces.admin.space.SpaceInstance}s.
 * <p>
 * In case of failover, using the API will always return results from currently discovered instances.
 * 
 * @since 9.0.0
 * @author moran
 */
public interface SpaceTransactionDetails {

    int getActiveTransactionCount();
    
    /*
     * TODO expose:
     * //returns all of the transactions
     * TransactionDetails[] getTransactionDetails()
     * 
     * //returns all of the transactions
     * TransactionDetails[] getTransactionDetails(TransactionState, TransactionType)
     * where TransactionState: ACTIVE, COMMITTED, ABORTED, PREPARED... (see TransactionConstants)
     * and TransactionType: ALL, JINI, LOCAL, XA
     */
}
