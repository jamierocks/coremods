package net.minecraftforge.coremod;

import cpw.mods.modlauncher.api.*;
import jdk.nashorn.api.scripting.*;
import net.minecraftforge.forgespi.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.script.*;
import java.util.*;
import java.util.stream.*;

public class CoreModEngine {
    private static final Logger LOGGER = LogManager.getLogger("CoreMod");
    private List<CoreMod> coreMods = new ArrayList<>();

    void loadCoreMod(ICoreModFile coremod) {
        // We have a factory per coremod, to provide namespace and functional isolation between coremods
        final NashornScriptEngineFactory nashornScriptEngineFactory = new NashornScriptEngineFactory();
        final ScriptEngine scriptEngine = nashornScriptEngineFactory.getScriptEngine(s -> s.startsWith("org.objectweb.asm."));
        coreMods.add(new CoreMod(coremod, scriptEngine));
    }

    public List<ITransformer<?>> initializeCoreMods() {
        coreMods.forEach(this::initialize);
        return coreMods.stream().map(CoreMod::buildTransformers).flatMap(List::stream).collect(Collectors.toList());
    }

    private void initialize(final CoreMod coreMod) {
        LOGGER.debug("Loading CoreMod from {}", coreMod.getPath());
        coreMod.initialize();
        if (coreMod.hasError()) {
            LOGGER.error("Error occurred initializing CoreMod", coreMod.getError());
        } else {
            LOGGER.debug("CoreMod loaded successfully");
        }
    }
}
