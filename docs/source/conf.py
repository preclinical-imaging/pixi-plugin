# Configuration file for the Sphinx documentation builder.

# -- Project information

project = 'PIXI'
copyright = '2024, Washington University in St. Louis'
author = 'Kooresh Shoghi'

release = '1.3'
version = '1.3.2-SNAPSHOT'

# -- General configuration

extensions = [
    'sphinx.ext.duration',
    'sphinx.ext.doctest',
    'sphinx.ext.autodoc',
    'sphinx.ext.autosummary',
    'sphinx.ext.autosectionlabel',
    'sphinx.ext.intersphinx',
]

intersphinx_mapping = {
    'python': ('https://docs.python.org/3/', None),
    'sphinx': ('https://www.sphinx-doc.org/en/master/', None),
}
intersphinx_disabled_domains = ['std']

templates_path = ['_templates']

# -- Options for HTML output

html_theme = 'sphinx_rtd_theme'
html_logo = 'images/pixi-logo-tagline.png'

# -- Options for EPUB output
epub_show_urls = 'footnote'
