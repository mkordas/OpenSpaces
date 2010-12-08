package org.openspaces.admin.internal.pu.elastic.config;

import org.openspaces.core.util.StringProperties;

public class ScaleStrategyConfigUtils {

    private static final String MAX_NUMBER_OF_CONTAINERS_PER_MACHINE_KEY = "max-number-of-containers-per-machine";
    private static final int MAX_NUMBER_OF_CONTAINERS_PER_MACHINE_DEFAULT = Integer.MAX_VALUE;
    private static final String MIN_NUMBER_OF_CONTAINERS_PER_MACHINE_KEY = "min-number-of-containers-per-machine";
    private static final int MIN_NUMBER_OF_CONTAINERS_PER_MACHINE_DEFAULT = 0;
    private static final String MAX_NUMBER_OF_CONTAINERS_KEY = "max-number-of-containers";
    private static final String MIN_NUMBER_OF_CONTAINERS_KEY = "min-number-of-containers";
    private static final int MAX_NUMBER_OF_CONTAINERS_DEFAULT = Integer.MAX_VALUE;
    private static final int MIN_NUMBER_OF_CONTAINERS_DEFAULT = 0;
    
    public static void setMaxNumberOfContainersPerMachine(
            StringProperties properties,
            int maxNumberOfContainersPerMachine) {
        
        properties.putInteger(MAX_NUMBER_OF_CONTAINERS_PER_MACHINE_KEY, maxNumberOfContainersPerMachine);
    }

    public static int getMaxNumberOfContainersPerMachine(
            StringProperties properties) {
        
        return properties.getInteger(MAX_NUMBER_OF_CONTAINERS_PER_MACHINE_KEY, MAX_NUMBER_OF_CONTAINERS_PER_MACHINE_DEFAULT);
    }

    public static void setMinNumberOfContainersPerMachine(
            StringProperties properties,
            int minNumberOfContainersPerMachine) {
        
        properties.putInteger(MIN_NUMBER_OF_CONTAINERS_PER_MACHINE_KEY, minNumberOfContainersPerMachine);
    }

    public static int getMinNumberOfContainersPerMachine(
            StringProperties properties) {
    
        return properties.getInteger(MIN_NUMBER_OF_CONTAINERS_PER_MACHINE_KEY, MIN_NUMBER_OF_CONTAINERS_PER_MACHINE_DEFAULT);
    }
    
    public static void setMaxNumberOfContainers(
            StringProperties properties,
            int maxNumberOfContainers) {
     
        properties.putInteger(MAX_NUMBER_OF_CONTAINERS_PER_MACHINE_KEY, maxNumberOfContainers);
    }

    public static int getMaxNumberOfContainers(
            StringProperties properties) {
     
        return properties.getInteger(MAX_NUMBER_OF_CONTAINERS_KEY, MAX_NUMBER_OF_CONTAINERS_DEFAULT);
    }

    public static void setMinNumberOfContainers(
            StringProperties properties,
            int minNumberOfContainers) {
        
        properties.putInteger(MIN_NUMBER_OF_CONTAINERS_KEY, minNumberOfContainers);
    }

    public static int getMinNumberOfContainers(
            StringProperties properties) {
        
        return properties.getInteger(MIN_NUMBER_OF_CONTAINERS_KEY, MIN_NUMBER_OF_CONTAINERS_DEFAULT);
    }
}
