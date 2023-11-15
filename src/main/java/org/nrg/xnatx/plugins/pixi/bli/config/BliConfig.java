package org.nrg.xnatx.plugins.pixi.bli.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({
        "org.nrg.xnatx.plugins.pixi.bli.helpers",
        "org.nrg.xnatx.plugins.pixi.bli.helpers.impl",
        "org.nrg.xnatx.plugins.pixi.bli.initialize",
        "org.nrg.xnatx.plugins.pixi.bli.repositories",
        "org.nrg.xnatx.plugins.pixi.bli.services",
        "org.nrg.xnatx.plugins.pixi.bli.services.impl",
        "org.nrg.xnatx.plugins.pixi.bli.entities",
        "org.nrg.xnatx.plugins.pixi.bli.factories",
        "org.nrg.xnatx.plugins.pixi.bli.factories.impl",
        "org.nrg.xnatx.plugins.pixi.bli.rest",
})
public class BliConfig {
}
