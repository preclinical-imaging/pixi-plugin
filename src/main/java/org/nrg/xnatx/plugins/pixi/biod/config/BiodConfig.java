package org.nrg.xnatx.plugins.pixi.biod.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({
        "org.nrg.xnatx.plugins.pixi.biod.rest",
        "org.nrg.xnatx.plugins.pixi.biod.services",
        "org.nrg.xnatx.plugins.pixi.biod.services.impl",
        "org.nrg.xnatx.plugins.pixi.biod.helpers",
        "org.nrg.xnatx.plugins.pixi.biod.helpers.impl",
})
public class BiodConfig {
}
