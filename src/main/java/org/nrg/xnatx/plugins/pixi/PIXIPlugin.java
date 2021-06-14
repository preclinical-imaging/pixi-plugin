package org.nrg.xnatx.plugins.pixi;

import org.nrg.framework.annotations.XnatPlugin;
import org.springframework.context.annotation.ComponentScan;

@XnatPlugin(value = "PIXIPlugin", name = "PIXI Plugin",
            logConfigurationFile = "pixi-logback.xml",
            entityPackages = "org.nrg.xnatx.plugins.pixi.entities")
@ComponentScan({"org.nrg.xnatx.plugins.pixi.repositories",
                "org.nrg.xnatx.plugins.pixi.services.impl",
                "org.nrg.xnatx.plugins.pixi.rest"})
public class PIXIPlugin {
}
