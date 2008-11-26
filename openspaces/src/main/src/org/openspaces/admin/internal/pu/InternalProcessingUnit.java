package org.openspaces.admin.internal.pu;

import org.openspaces.admin.gsm.GridServiceManager;
import org.openspaces.admin.pu.ProcessingUnit;

/**
 * @author kimchy
 */
public interface InternalProcessingUnit extends ProcessingUnit {

    void setManagingGridServiceManager(GridServiceManager gridServiceManager);

    void addBackupGridServiceManager(GridServiceManager backupGridServiceManager);

    void removeBackupGridServiceManager(String gsmUID);

    boolean setStatus(int statusCode);
}
