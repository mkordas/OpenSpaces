package org.openspaces.hibernate.cache;

import com.j_spaces.core.client.FinderException;
import com.j_spaces.core.client.cache.ISpaceLocalCache;
import com.j_spaces.map.CacheFinder;
import com.j_spaces.map.IMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.CacheProvider;
import org.hibernate.cache.Timestamper;
import org.openspaces.core.util.SpaceUtils;

import java.rmi.RemoteException;
import java.util.Properties;

/**
 * Base class for pure Space (no OpenSpaces components) Hibernate cache provider. Uses the
 * property <code>gigaspace.hibernate.cache.url</code> to retrieve the Space/Cache url. Also
 * passes the properites object to the Space/Cache construction allowing to fully configure
 * the cache using Hibernate cache proeprties (for example, have "xpath" configuration set
 * there).
 *
 * <p>Stops the local cache when this cache provider stops. Also stops the master Space if it
 * was started in an embedded mode.
 *
 * @author kimchy
 */
public abstract class AbstractMapCacheProvider implements CacheProvider {

    protected final Log log = LogFactory.getLog(getClass());

    public static final String CACHE_URL_PROPERTY = "gigaspace.hibernate.cache.url";

    private IMap map;

    /**
     * Starts the pure GigaSpace Hibernate cache provider. Uses <code>gigaspace.hibernate.cache.url</code>
     * property to fetch the cache url. Also passes the proeprties to the {@link com.j_spaces.map.CacheFinder#find(String, java.util.Properties)}
     * so any additional Space related properties can be set.
     */
    public void start(Properties properties) throws CacheException {
        String url = properties.getProperty(CACHE_URL_PROPERTY);
        if (url == null) {
            throw new CacheException("[" + CACHE_URL_PROPERTY + "] must be set");
        }
        if (log.isInfoEnabled()) {
            log.info("Using GigaSpace as Hibernate second level cache with url [" + url + "]");
        }
        try {
            map = (IMap) CacheFinder.find(url, properties);
        } catch (FinderException e) {
            throw new CacheException("Failed to find/create cache [" + url + "]", e);
        }
        doStart(properties);
    }

    /**
     * Subclasses can implement this method in order to perform additional operations
     * during startup.
     */
    protected void doStart(Properties properties) throws CacheException {

    }

    /**
     * Stops the local cache. If the master Space was started in embedded mode, will also
     * stop the master space.
     */
    public void stop() {
        doStop();
        if (map.getLocalSpace() instanceof ISpaceLocalCache) {
            ((ISpaceLocalCache) map.getLocalSpace()).close();
        }
        // if it is an embedde Space, stop it
        if (!SpaceUtils.isRemoteProtocol(map.getMasterSpace())) {
            try {
                map.getMasterSpace().getContainer().shutdown();
            } catch (RemoteException e) {
                log.warn("Failed to shutdown master space", e);
            }
        }
    }

    protected void doStop() {

    }

    public long nextTimestamp() {
        return Timestamper.next();
    }

    public boolean isMinimalPutsEnabledByDefault() {
        return true;
    }

    protected IMap getMap() {
        return this.map;
    }
}
