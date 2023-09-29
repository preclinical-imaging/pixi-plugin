Installation
============

1. `Understand XNAT Plugin Model`_
2. `Select and Download XNAT Plugins`_
3. `Determine Installation Environment`_
4. `Install XNAT + Plugins`_



------------------------
Understand XNAT Plugin Model
------------------------
XNAT provides a rich set of features that are described in `About XNAT`_.
XNAT supports `plugins`_ that extend features and functions in core XNAT.

::

  A plugin is a compiled, self-contained package separate from your XNAT server but installed into the XNAT plugins folder.
  Once installed, the plugin runs in the same process space with XNAT as a fully integrated extension to the core XNAT server.

::

Plugins are written and contributed by the core XNAT team, the PIXI development team and
by individual sites with specific needs that are not found in core XNAT.

------------------------
Select and Download XNAT Plugins
------------------------

Plugins are Java jar files that are added to the operating environment during the installation process.
You can select and download the jar files as part of preparation for installation.

The table below lists common plugins for running the PIXI software.
You will find yet other plugins on the `XNAT Downloads page`_.

+-----------------------------+------------------------------------+----------------------------------------+
| Documentation               | Comments                           | Current Plugin Jar                     |
+=============================+====================================+========================================+
| `PIXI`_                     | Essential                          | `PIXI plugin`_                         |
+-----------------------------+------------------------------------+----------------------------------------+
| `XNAT OHIF Viewer`_         | Highly Recommended                 | `XNAT Downloads page`_                 |
+-----------------------------+------------------------------------+----------------------------------------+
| `Container Service`_        | Highly Recommended                 | `XNAT Downloads page`_                 |
+-----------------------------+------------------------------------+----------------------------------------+
| `Batch Launch`_             | Extends Container Service          | `XNAT Downloads page`_                 |
+-----------------------------+------------------------------------+----------------------------------------+
| `JupyterHub Integration`_   | XNAT/Python Integrated Environment | `XNAT Downloads page`_                 |
+-----------------------------+------------------------------------+----------------------------------------+


---------------
Determine Installation Environment
---------------
XNAT can be deployed to support different environments ranging from a single user running on a laptop to a larger, enterprise-level system.
Please review descriptions below and choose the deployment environment that best suits your needs.
The broad categories provide suggestions about the intended owner and administrator of the system.
When you select an installation procedure and work through the steps, please remember it will be useful to have the plugin jar files before you start.

+---------------------------------------------+-----------------+----------------------------------------+
| Broad Category                              | Comments        | Installation Instructions              |
+=============================================+=================+========================================+
| Student / Personal Data Management          | Docker Desktop  | `XNAT Docker Compose`_                 |
+---------------------------------------------+-----------------+----------------------------------------+
| Local Deployment / Developer                | Docker Desktop  | `XNAT Docker Compose`_                 |
+---------------------------------------------+-----------------+----------------------------------------+
| Lab Installation / No Digital Certificates  |Tomcat / Postgres| `XNAT Installation`_                   |
+---------------------------------------------+-----------------+----------------------------------------+
| Enterprise Level / Comprehensive IT Support | Reverse Proxy   | `XNAT Installation`_                   |
+---------------------------------------------+-----------------+----------------------------------------+


------------------------
Install XNAT + Plugins
------------------------

.. _About XNAT: https://www.xnat.org/about
.. _plugins: https://wiki.xnat.org/documentation/developing-xnat-plugins
.. _XNAT Downloads page: https://www.xnat.org/download/
.. _PIXI: https://github.com/preclinical-imaging/pixi-plugin/releases
.. _PIXI plugin: https://github.com/preclinical-imaging/pixi-plugin/releases
.. _XNAT OHIF Viewer: https://wiki.xnat.org/xnat-ohif-viewer
.. _Container Service: https://wiki.xnat.org/container-service/
.. _Batch Launch: https://wiki.xnat.org/xnat-tools/batch-launch-plugin
.. _JupyterHub Integration: https://wiki.xnat.org/jupyter-integration

.. _XNAT Docker Compose: https://github.com/NrgXnat/xnat-docker-compose
.. _XNAT In Vagrant Virtual Environment: https://wiki.xnat.org/documentation/running-xnat-in-a-vagrant-virtual-machine
.. _XNAT Installation: https://wiki.xnat.org/documentation/xnat-installation-guide
