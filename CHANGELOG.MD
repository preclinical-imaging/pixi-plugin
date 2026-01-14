# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.5.0] - 2025-12-4

This release includes ability to:

  - import BioDistribution datatype

  - ability to generate Statistics Summary for the BioDistribution data [Statistics Dashboard Plugin](https://bitbucket.org/xnatx/statisticsdashboard_plugin)

  - integration of Contrast Data implementation within PIXI which was added to core XNAT in version 1.9.3

  - bug fixes relating to PDX and cell line datatypes

  
### Added
- [PIXI-159](https://radiologics.atlassian.net/browse/PIXI-159): Add a Biodistribution subject assessor
- [PIXI-163](https://radiologics.atlassian.net/browse/PIXI-163): Create importer for Biodistribution data from CSV input
- [PIXI-164](https://radiologics.atlassian.net/browse/PIXI-164): Create Biodistribution Report with Summary Statistics

### Fixed
- [PIXI-197](https://radiologics.atlassian.net/browse/PIXI-197): BLI Session and Scans do not contain the modality field
- [PIXI-215](https://radiologics.atlassian.net/browse/PIXI-215): Editing of PDX and removing passage number does not work via the Action -> Edit 
- [PIXI-228](https://radiologics.atlassian.net/browse/PIXI-228): XFT cast error when saving cell line

### Changed
- [PIXI-204](https://radiologics.atlassian.net/browse/PIXI-204): Remove Statistics Dashboard files from Pixi Plugin. This is now located in the dedicated [Statistics Dashboard Plugin](https://bitbucket.org/xnatx/statisticsdashboard_plugin)
- [PIXI-217](https://radiologics.atlassian.net/browse/PIXI-217): Tumor data model should be handled by Data Manager Role rather than the site admin

## [1.4.0] - 2024-12-19

This release mainly addresses compatibility issues with XNAT 1.9. Prior releases may not work with XNAT 1.9.

### Added

- [PIXI-156]: Add injection date to the hotel scan record form. Sometimes animals are injected on a different day than 
              the scan is performed.

### Fixed

- [PIXI-141]: A few internal changes to support XNAT 1.9 compatibility. The plugin is now works with XNAT 1.9.

### Changed

- [PIXI-157]: Rename Patient-Derived Xenografts table to Patient-Derived Tumors in the plugin settings. The table name
              was misleading and did not reflect that we are storing tumor model data, not animal model data.

## [1.3.1] - 20204-11-19

### Added

- [PIXI-153]: Add scan time to the hotel scan record form.
- [PIXI-155]: Adds a technician perspective field to the hotel scan record form as some scanners are viewed/labeled
              based on the technician viewing from the back of the scanner. The hotel splitter will use this field to
              determine the correct orientation when splitting the hotel session into individual image sessions.

### Fixed

- [PIXI-140]: Fix issue with empty strings and date parsing with Inveon image imports.
- [PIXI-144]: Session and scan time were not being populated when building Inveon PET/CT sessions.
- [PIXI-145]: Setpoint Temperature id misspelled in display xml and throws DisplayFieldNotFoundException in logs.
- [PIXI-151]: Inveon session builder was not extracting the doseage and injection time into the database.

### Changed

- [PIXI-147]: Change validation logic in hotel scan record to prevent user from accidentally creating a scan record when
              the subject ID is not set.
- [PIXI-150]: Update display XML listings for PET and CT to make them more user-friendly.
- [PIXI-152]: Allow for single mouses/rat hotels in the hotel scan record form.
- [PIXI-154]: Update PCIF naming scheme in the Inveon importer to include the timepoint as part of the XNAT session name

## [1.3.0] - 2024-06-24

### Added

- [PIXI-131][]: Add xml display documents for BLI
- [PIXI-132][]: Add velocity vm search templates for BLI
- [PIXI-133][]: Extract scanner model, software version, time point, view (prone/supine/dorsal/ventral), and time point
                from AnalyzeClickInfo.txt during BLI scan building
- [PIXI-139][]: Adds plugin description, visible in the installed plugins site administration page in XNAT.

### Changed

- [PIXI-134][]: Refactored the BLI upload UI to enhance user experience. Simplified the process by removing the need for
                pre-defined mappings. Would still like to add a site-wide and project level setting feature but this is
                more user-friendly than the previous version.
- [PIXI-135][]: Update default generated search.vm files for PIXI datatypes
- [PIXI-137][]: Update the native Inveon file importer to accept more than one session at a time.
- [PIXI-138][]: Usability improvements to the Hotel Scan Record Form UI. Subject ID selector is now scrollable and
                searchable.

## [1.2.0] - 2024-04-18

### Added

- [PIXI-119][]: Adds single session upload of native Inveon PET/CT images. Enable in the plugin preferences.
- [PIXI-127][]: Capture animal date of death and endpoint.

###  Changed

- [PIXI-125][]: Bioluminescence is too long for the upload nav link. Revert back to 'BLI' for the upload link.
- [PIXI-128][]: Add IVIS subdirectory to SCANS folder for BLI imports to match XNAT's expected directory structure.
                Refactor BLI importer by removing some dependencies and simplifying import logic.
- [PIXI-113][]: Usability improvements to the Hotel Scan Record Form UI. 'Select Image Session' is now scrollable and
                searchable.

## [1.1.0] - 2024-02-19

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
- [PIXI-117][]: During BLI session building, throw exception instead of just logging error. BLI sessions were being
                built for DICOM and Inveon images because the plugin was not throwing an exception when it should have.
                More exception handling may be needed to distinguish between non BLI images and BLI sessions that are
                not being built correctly.
- [PIXI-125][]: Bioluminescence Imaging was misspelled. Change upload link to Bioluminescence Imaging instead of 'BLI' 
                for clarity.

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
[PIXI-113]: https://radiologics.atlassian.net/browse/PIXI-113
[PIXI-116]: https://radiologics.atlassian.net/browse/PIXI-116
[PIXI-117]: https://radiologics.atlassian.net/browse/PIXI-117
[PIXI-119]: https://radiologics.atlassian.net/browse/PIXI-119
[PIXI-120]: https://radiologics.atlassian.net/browse/PIXI-120
[PIXI-123]: https://radiologics.atlassian.net/browse/PIXI-123
[PIXI-124]: https://radiologics.atlassian.net/browse/PIXI-124
[PIXI-125]: https://radiologics.atlassian.net/browse/PIXI-125
[PIXI-127]: https://radiologics.atlassian.net/browse/PIXI-127
[PIXI-128]: https://radiologics.atlassian.net/browse/PIXI-128
[PIXI-131]: https://radiologics.atlassian.net/browse/PIXI-131
[PIXI-132]: https://radiologics.atlassian.net/browse/PIXI-132
[PIXI-133]: https://radiologics.atlassian.net/browse/PIXI-133
[PIXI-134]: https://radiologics.atlassian.net/browse/PIXI-134
[PIXI-135]: https://radiologics.atlassian.net/browse/PIXI-135
[PIXI-137]: https://radiologics.atlassian.net/browse/PIXI-137
[PIXI-138]: https://radiologics.atlassian.net/browse/PIXI-138
[PIXI-139]: https://radiologics.atlassian.net/browse/PIXI-139
[PIXI-140]: https://radiologics.atlassian.net/browse/PIXI-140
[PIXI-141]: https://radiologics.atlassian.net/browse/PIXI-141
[PIXI-144]: https://radiologics.atlassian.net/browse/PIXI-144
[PIXI-145]: https://radiologics.atlassian.net/browse/PIXI-145
[PIXI-147]: https://radiologics.atlassian.net/browse/PIXI-147
[PIXI-150]: https://radiologics.atlassian.net/browse/PIXI-150
[PIXI-151]: https://radiologics.atlassian.net/browse/PIXI-151
[PIXI-152]: https://radiologics.atlassian.net/browse/PIXI-152
[PIXI-153]: https://radiologics.atlassian.net/browse/PIXI-153
[PIXI-154]: https://radiologics.atlassian.net/browse/PIXI-154
[PIXI-155]: https://radiologics.atlassian.net/browse/PIXI-155
[PIXI-156]: https://radiologics.atlassian.net/browse/PIXI-156
[PIXI-157]: https://radiologics.atlassian.net/browse/PIXI-157