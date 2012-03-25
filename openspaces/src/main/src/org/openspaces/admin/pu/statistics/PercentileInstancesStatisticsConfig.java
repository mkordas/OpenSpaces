package org.openspaces.admin.pu.statistics;

import java.util.HashMap;
import java.util.Map;

import org.openspaces.admin.internal.pu.statistics.StatisticsObjectList;
import org.openspaces.admin.internal.pu.statistics.StatisticsObjectListFunction;

/**
 * Picks the Nth percentile of cluster instance values.
 * The default percentile value is 50 (median)
 * @since 9.0.0
 * @author itaif
 *
 */
public class PercentileInstancesStatisticsConfig 
    extends AbstractInstancesStatisticsConfig 
    implements StatisticsObjectListFunction , InstancesStatisticsConfig {

    private static final String PERCENTILE_KEY = "percentile";
    private static final double PERCENTILE_DEFAULT = 50.0;

    public PercentileInstancesStatisticsConfig() {
        this(new HashMap<String,String>());
    }
    
    public PercentileInstancesStatisticsConfig(Map<String,String> properties) {
        super(properties);
    }
    
    public double getPercentile() {
        return super.getStringProperties().getDouble(PERCENTILE_KEY,PERCENTILE_DEFAULT);
    }

    public void setPercentile(double percentile) {
        super.getStringProperties().putDouble(PERCENTILE_KEY, percentile);
    }

    @Override
    public void validate() throws IllegalStateException {
        
        if (getPercentile() <0 || getPercentile() > 100) {
            throw new IllegalArgumentException("percentile ("+getPercentile()+") must between 0 and 100 (inclusive)");
        }
    }

    @Override
    public Object calc(StatisticsObjectList values) {
        return values.getPercentile(getPercentile());
    }

}
