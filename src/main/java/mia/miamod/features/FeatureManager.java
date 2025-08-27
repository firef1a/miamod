package mia.miamod.features;

import mia.miamod.features.impl.development.cpudisplay.CPUDisplay;
import mia.miamod.features.impl.general.AutoTip;
import mia.miamod.features.impl.general.DotSlashBypass;
import mia.miamod.features.impl.general.title.JoinButton;
import mia.miamod.features.impl.internal.ConfigScreenFeature;
import mia.miamod.features.impl.development.ItemTagViewer;
import mia.miamod.features.impl.internal.commands.CommandAliaser;
import mia.miamod.features.impl.internal.commands.CommandScheduler;
import mia.miamod.features.impl.internal.server.ServerManager;
import mia.miamod.features.impl.moderation.reports.clickonreportsinchattoteleporttothem;
import mia.miamod.features.impl.support.AutoQueue;
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
        add(new JoinButton(Categories.GENERAL));
        add(new AutoTip(Categories.GENERAL));
        //add(new DotSlashBypass(Categories.GENERAL));

        add(new CPUDisplay(Categories.DEV));
        add(new ItemTagViewer(Categories.DEV));

        add(new AutoQueue(Categories.SUPPORT));

        add(new clickonreportsinchattoteleporttothem(Categories.MODERATION));

        initInternalFeatures();
    }

    private static void initInternalFeatures() {
        add(new ServerManager(Categories.INTERNAL));
        add(new ConfigScreenFeature(Categories.INTERNAL));
        add(new CommandAliaser(Categories.INTERNAL));
        add(new CommandScheduler(Categories.INTERNAL));
    }

    private static void add(Feature feature) {
        features.put(feature.getClass(), feature);
    }

    public static Feature getFeature(Class<? extends Feature> identifier) { return features.get(identifier); }
    public static Collection<Feature> getFeatures() { return getFeatureMap().values(); }
    public static HashMap<Class<? extends Feature>, Feature> getFeatureMap() { return features; }

    public static <T extends AbstractEventListener> List<T> getFeaturesByIdentifier(Class<T> listener) {
       return getFeatures().stream().filter((listener::isInstance)).map((feature -> (T) feature)).filter(feature -> ((Feature) feature).getEnabled()).collect(Collectors.toList());
    }

    public static <T extends AbstractEventListener> void implementFeatureListener(Class<T> listener, Consumer<T> consumer) {
        getFeaturesByIdentifier(listener).forEach(consumer);
    }

    //public static List<? extends AbstractEventListener> getFeaturesByListener(FeatureListener featureListener) { return getFeaturesByIdentifier(featureListener.getIdentifier()); }
}
