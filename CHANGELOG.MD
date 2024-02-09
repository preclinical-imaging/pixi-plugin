# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- [PIXI-70][]:  Adds BLI Importer Mappings. This feature allows users to control how XNAT maps BLI headers to XNAT
                metadata.  
- [PIXI-111][]: Add link from split hotel image sessions to the hotel scan record. Bi-directional link should improve
                navigation between the split hotel image sessions and the hotel scan record.
- [PIXI-124][]: Adds CHANGELOG.md to the project repo to document changes to the plugin.
- Add GitHub workflows to build the develop branch and publish releases. Move the build process to GitHub Actions and
  from the Jenkins build process.
- Adds documentation for the plugin in /docs. The published documentation is available at
  [https://pixi-documentation.readthedocs.io/en/latest/](https://pixi-documentation.readthedocs.io/en/latest/).

### Changed

- [PIXI-112][]: Change display of four mouse hotel scan record from one row of four subjects to two rows of two 
                subjects. Content will be the same, but the layout is more readable.
- [PIXI-117][]: Change log level to debug to troubleshoot issue with the DICOM hotel splitter creating BLI sessions 
                instead of PET/CT sessions.

### Fixed

- [PIXI-116][]: Fixes spring bean name conflict with the XNAT JupyterHub plugin. 
- [PIXI-120][]: Fixes NPE when copying the image acquisition context from the hotel scan record to the split hotel 
                image session.
- [PIXI-123][]: Fix issue with UI elements overlapping in the advanced search UI page.

## [1.0.0] - 2023-09-01

Initial release of the PIXI XNAT plugin. Compatible with XNAT version 1.8.9. Add the plugin jar file to your XNAT 
plugins directory and restart XNAT.

[PIXI-70]: https://radiologics.atlassian.net/browse/PIXI-70
[PIXI-111]: https://radiologics.atlassian.net/browse/PIXI-111
[PIXI-112]: https://radiologics.atlassian.net/browse/PIXI-112
[PIXI-116]: https://radiologics.atlassian.net/browse/PIXI-116
[PIXI-117]: https://radiologics.atlassian.net/browse/PIXI-117
[PIXI-120]: https://radiologics.atlassian.net/browse/PIXI-120
[PIXI-123]: https://radiologics.atlassian.net/browse/PIXI-123
[PIXI-124]: https://radiologics.atlassian.net/browse/PIXI-124

