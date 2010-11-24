package org.openspaces.jpa.openjpa;

import java.sql.SQLException;

import net.jini.core.transaction.server.TransactionManager;

import org.apache.openjpa.conf.OpenJPAConfigurationImpl;
import org.openspaces.core.space.UrlSpaceConfigurer;

import com.gigaspaces.client.transaction.ITransactionManagerProvider;
import com.gigaspaces.client.transaction.ITransactionManagerProvider.TransactionManagerType;
import com.gigaspaces.client.transaction.TransactionManagerConfiguration;
import com.gigaspaces.client.transaction.TransactionManagerProviderFactory;
import com.j_spaces.core.IJSpace;
import com.j_spaces.jdbc.driver.GConnection;

/**
 * Holds OpenJPA's configuration properties & GigaSpaces resources.
 * OpenJPA keeps a single instance of this class.
 * 
 * @author idan
 * @since 8.0
 * 
 */
public class SpaceConfiguration extends OpenJPAConfigurationImpl {

    private IJSpace _space;
    private ITransactionManagerProvider _transactionManagerProvider;
    private GConnection _connection;
    
    public SpaceConfiguration() {
        super();        
        // Default transaction timeout
        setLockTimeout(0);
        setOptimistic(false);
        setLockManager("none");
        setDynamicEnhancementAgent(false);
    }

    public void initialize() {
        // Set a space proxy using the provided connection url        
        _space = new UrlSpaceConfigurer(getConnectionURL()).space();
        
        // Create a transaction manager
        TransactionManagerConfiguration configuration = new TransactionManagerConfiguration(TransactionManagerType.DISTRIBUTED);
        try {
            _transactionManagerProvider = TransactionManagerProviderFactory.newInstance(_space, configuration);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    public GConnection getJdbcConnection() throws SQLException {
        if (_connection == null) {
            synchronized (this) {
                if (_connection == null) {
                    System.getProperties().put("com.gs.embeddedQP.enabled", "true");                    
                    _connection = GConnection.getInstance(_space);
                    if (!_connection.getAutoCommit())
                        _connection.setAutoCommit(true);
                }
            }
        }
        return _connection;        
    }
    
    public IJSpace getSpace() {
        return _space;
    }
    
    public TransactionManager getTransactionManager() {
        return _transactionManagerProvider.getTransactionManager();
    }
    
    
    
}
