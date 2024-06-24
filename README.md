# PIXI XNAT Plugin

This is the repository for the PIXI XNAT plugin. PIXI is designed to help manage and analyze preclinical imaging data.

For detailed information, installation instructions, and usage guidelines, please visit [pixi.org](https://pixi.org).

## Build

To build the PIXI XNAT plugin, run the following command:

```bash
./gradlew clean jar
```

The plugin jar file will be located in the `build/libs` directory.


To build the documentation locally, run the following command:

```bash
sphinx-build sphinx-build -b html docs/source docs/build 
```
