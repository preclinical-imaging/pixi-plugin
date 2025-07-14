package org.nrg.xnatx.plugins.pixi.xenografts.services;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xnatx.plugins.pixi.xenografts.handlers.Importsfrom;
import org.nrg.xnatx.plugins.pixi.xenografts.handlers.ModelJsonHandler;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class XenograftModelImporterHandlerService {

    @Autowired
    public XenograftModelImporterHandlerService() {
        final Reflections reflections = new Reflections(ModelJsonHandler.class.getPackage().getName());
        importers.putAll(reflections.getSubTypesOf(ModelJsonHandler.class).stream()
                .filter(ReflectionUtils.withAnnotation(Importsfrom.class))
                .collect(Collectors.toMap(IMPORTER_HANDLES, IMPORTER_CONSTRUCTOR)));
    }

    @Nullable
    public ModelJsonHandler getImporter(final String url) {
        log.debug("Searching for importer for url {}", url);
        final Constructor<? extends ModelJsonHandler> constructor = find(url);
        if (constructor == null) {
           log.debug("No importer found for operation " + url);
           return null;
        }
        log.debug("Found handler for url {}", url);
        try {
            return constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("An error occurred trying to instantiate a prearchive operation handler.", e);
        }
    }

    private Constructor<? extends ModelJsonHandler> find(final String url) {
        for (String u: importers.keySet()) {
            if (url.startsWith(u)) {
                return importers.get(u);
            }
        }
        return null;
    }

    private static final Function<Class<? extends ModelJsonHandler>, String>  IMPORTER_HANDLES   = handler -> {
        final String url = handler.getAnnotation(Importsfrom.class).url();
        log.debug("Found importer for {} url: {}", url, handler.getName());
        return url;
    };

    private static final Function<Class<? extends ModelJsonHandler>, Constructor<? extends ModelJsonHandler>> IMPORTER_CONSTRUCTOR = handler -> {
        try {
            return handler.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("No proper constructor found for " + handler.getName() + " class. It must have a constructor that accepts a " + ModelJsonHandler.class.getName() + " object.");
        }
    };

    private final Map<String, Constructor<? extends ModelJsonHandler>> importers    = new HashMap<>();


}
