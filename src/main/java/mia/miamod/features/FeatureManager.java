package mia.miamod.features;

import mia.miamod.features.impl.general.ConfigScreenFeature;
import mia.miamod.features.impl.general.merowwwaadfa;
import mia.miamod.features.impl.internal.server.ServerManager;
import mia.miamod.features.listeners.AbstractEventListener;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@SuppressWarnings({"unchecked"})
public abstract class FeatureManager {
    private static HashMap<FeatureListener, List<? extends AbstractEventListener>> listeners;
    private static HashMap<Class<? extends Feature>, Feature> features;

    // config has to load b4 features are
    public static void init() {
        listeners = new HashMap<>();
        features = new HashMap<>();
        initFeatures();

        // register listeners
        FeatureListener.getFeatureIdentifiers().forEach(featureListener -> listeners.put(featureListener, getFeaturesByIdentifier(featureListener.getIdentifier())));
    }

    private static void initFeatures() {
        add(new ConfigScreenFeature());
        add(new merowwwaadfa());
        initInternalFeatures();
    }

    private static void initInternalFeatures() {
        add(new ServerManager());
    }

    private static void add(Feature feature) {
        features.put(feature.getClass(), feature);
    }

    public static Feature getFeature(Class<? extends Feature> identifier) { return features.get(identifier); }
    public static Collection<Feature> getFeatures() { return getFeatureMap().values(); }
    public static HashMap<Class<? extends Feature>, Feature> getFeatureMap() { return features; }

    public static <T extends AbstractEventListener> List<T> getFeaturesByIdentifier(Class<T> listener) {
       return getFeatures().stream().filter((listener::isInstance)).map((feature -> (T) feature)).collect(Collectors.toList());
    }

    public static <T extends AbstractEventListener> void implementFeatureListener(Class<T> listener, Consumer<T> consumer) {
        getFeaturesByIdentifier(listener).forEach(consumer);
    }

    //public static List<? extends AbstractEventListener> getFeaturesByListener(FeatureListener featureListener) { return getFeaturesByIdentifier(featureListener.getIdentifier()); }
}
