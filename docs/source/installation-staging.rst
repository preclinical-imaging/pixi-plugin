Installation
============

1. `Understand XNAT Plugin Model`_
2. `Select and Download XNAT Plugins`_
3. `Determine Installation Environment`_
4. `Install XNAT + Plugins in Docker Environment`_



----------------------------
Understand XNAT Plugin Model
----------------------------
XNAT provides a `rich set of features and functions <https://www.xnat.org/about>`_ for managing imaging data.
It is designed to be extensible and customizable and supports `plugins <https://wiki.xnat.org/documentation/developing-xnat-plugins>`_ that extend features and functions in core
XNAT:

  A plugin is a compiled, self-contained package separate from your XNAT server but installed
  into the XNAT plugins folder. Once installed, the plugin runs in the same process space with
  XNAT as a fully integrated extension to the core XNAT server.

Plugins are written and contributed by the core XNAT team, the PIXI development team and
by individual sites with specific needs that are not found in core XNAT.

------------------------
Select and Download XNAT Plugins
------------------------

Plugins are Java jar files that are added to the operating environment during the installation process.
You can select and download the jar files as part of preparation for installation.

The `PIXI plugin <https://github.com/preclinical-imaging/pixi-plugin/releases>`_ contains the essential
software that extends XNAT for preclinical imaging.
Please include that plugin as part of the installation process.
The table below lists other plugins commonly used in the PIXI environment.
These are available on the `XNAT Downloads Page <https://www.xnat.org/download>`_

+-----------------------------+------------------------------------+
| Documentation               | Comments                           |
+=============================+====================================+
| `XNAT OHIF Viewer`_         | Highly Recommended                 |
+-----------------------------+------------------------------------+
| `Container Service`_        | Highly Recommended                 |
+-----------------------------+------------------------------------+
| `Batch Launch`_             | Extends Container Service          |
+-----------------------------+------------------------------------+
| `JupyterHub Integration`_   | XNAT/Python Integrated Environment |
+-----------------------------+------------------------------------+


---------------
Determine Installation Environment
---------------
XNAT can be deployed to support different environments ranging from a single user running on a laptop to a larger, enterprise-level system.
This section provides overviews of the small and large environments.

- The Dockerized version of XNAT can run on both Docker Desktop and the professional versions of Docker.
  XNAT provides a `Docker Compose <https://docs.docker.com/compose>`_ file that defines the environment for running the application.
  This is an appropriate environment for individuals to run on their laptop or desktop for evaluation and can also
  serve as the basis for a larger deployment.
  Instructions for this environment are provided in the next section.

- Users with IT support who want a system that is available to a larger audience will want to understand
  the `XNAT Administration <https://wiki.xnat.org/documentation/xnat-administration>`_ documentation.
  This describes an environment where the XNAT software is running as a system service in a Linux environment and is available 24x7.
  Instructions are included for managing user authentication using an LDAP server (Windows Active Directory)
  and/or running XNAT behind a firewall with HTTPS-protected connections.
  For the steps listed on the `XNAT Installation Guide page. <https://wiki.xnat.org/documentation/xnat-installation-guide>`_

We recommend starting with the Dockerized version.
You will have a functioning PIXI system within a few minutes and can learn the administrative functions
and the application itself before investing more time with a production installation.

===================
Install XNAT + Plugins in Docker Environment
===================

1. Gather the plugins described above. We include shell scripts below to simplify that process.
  - You do not need to download the XNAT war file. It will be included as part of the Docker container.
2. Clone the `xnat-docker-compose <https://github.com/NrgXnat/xnat-docker-compose>`_ repository:
  - git clone https://github.com/NrgXnat/xnat-docker-compose
3. Initialize the files needed for this environment
  - cd xnat-docker-compose
  - cp default.environment .env
  - Review .env file and make local changes as appropriate
  - Copy plugin files to xnat/plugins folder. See scripts below to simplify this process.
4. Launch the container in detached mode:
  - docker-compose up -d
5. Browse to http://localhost
  - Login with username/password admin/admin
  - Complete `XNAT Setup - First Time Configuration <https://wiki.xnat.org/documentation/xnat-setup-first-time-configuration>`_.
    Because these are the instructions for the Dockerized version, you should accept the default values for Data Storage such as
    those provided for Archive Path or Prearchive Path.

===================
Shell Scripts to Retrieve/Install Relevant Plugins
===================

These commands can be used to download plugin files and place them in the proper folder.
Execute these commands from the *xnat-docker-compose* folder previously created.

|    wget -q -P ./xnat/plugins/ https://github.com/preclinical-imaging/pixi-plugin/releases/download/v1.0.0/pixi-plugin-1.0.0.jar
|    wget -q -P ./xnat/plugins/ https://api.bitbucket.org/2.0/repositories/icrimaginginformatics/ohif-viewer-xnat-plugin/downloads/ohif-viewer-3.6.0.jar
|    wget -q -P ./xnat/plugins/ https://api.bitbucket.org/2.0/repositories/xnatdev/container-service/downloads/container-service-3.4.1-fat.jar
|    wget -q -P ./xnat/plugins/ https://api.bitbucket.org/2.0/repositories/xnatx/xnatx-batch-launch-plugin/downloads/batch-launch-0.6.0.jar
|    wget -q -P ./xnat/plugins/ https://api.bitbucket.org/2.0/repositories/xnatx/xnat-jupyterhub-plugin/downloads/xnat-jupyterhub-plugin-1.0.0.jar


.. _XNAT OHIF Viewer: https://wiki.xnat.org/xnat-ohif-viewer
.. _Container Service: https://wiki.xnat.org/container-service/
.. _Batch Launch: https://wiki.xnat.org/xnat-tools/batch-launch-plugin
.. _JupyterHub Integration: https://wiki.xnat.org/jupyter-integration

