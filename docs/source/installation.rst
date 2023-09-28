Installation
============

------------------------
PIXI and Related Plugins
------------------------
XNAT provides a rich set of features that are extended for preclinical imaging by the PIXI plugin as well as other plugins.
An XNAT plugin is a jar file that will be placed in a plugins folder as part of the deployment process. You will need to
install the `PIXI plugin`_ as part of XNAT installation.
Core XNAT plugins are found on the `XNAT Downloads page`_.
In addition to the PIXI plugin, you will find these plugins useful:

- `XNAT OHIF Viewer`_: The XNAT OHIF Viewer allows users to view, create and edit ROI collections and annotations on XNAT image sessions. This viewer was developed by the Institute for Cancer Research, London and is based on the Javascript-based OHIF Viewer.
- `Container Service`_: The Container Service plugin allows you to execute external processing tools as lightweight containers, mounting and reading XNAT data, then posting outputs back to your XNAT project. This plugin is needed by PIXI for splitting hotel image sessions into individual image sessions.
- `Batch Launch`_: The Batch Launch plugin is a companion to the Container Service and allows you to launch containers or pipelines in batches based on project listings or search listings of XNAT data.
- `JupyterHub Integration`_: The JupyterHub Integration plugin allows your XNAT to connect to a running JupyterHub server and creates integration points for launching and saving Jupyter Notebooks.

---------------
XNAT Deployment
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

.. _XNAT Downloads page: https://www.xnat.org/download/
.. _PIXI plugin: https://github.com/preclinical-imaging/pixi-plugin/releases
.. _XNAT OHIF Viewer: https://wiki.xnat.org/xnat-ohif-viewer
.. _Container Service: https://wiki.xnat.org/container-service/
.. _Batch Launch: https://wiki.xnat.org/xnat-tools/batch-launch-plugin
.. _JupyterHub Integration: https://wiki.xnat.org/jupyter-integration

.. _XNAT Docker Compose: https://github.com/NrgXnat/xnat-docker-compose
.. _XNAT In Vagrant Virtual Environment: https://wiki.xnat.org/documentation/running-xnat-in-a-vagrant-virtual-machine
.. _XNAT Installation: https://wiki.xnat.org/documentation/xnat-installation-guide