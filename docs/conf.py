# Configuration file for the Sphinx documentation builder.
#
# This file only contains a selection of the most common options. For a full
# list see the documentation:
# https://www.sphinx-doc.org/en/master/usage/configuration.html

# -- Path setup --------------------------------------------------------------

# If extensions (or modules to document with autodoc) are in another directory,
# add these directories to sys.path here. If the directory is relative to the
# documentation root, use os.path.abspath to make it absolute, like shown here.
#
# import os
# import sys
# sys.path.insert(0, os.path.abspath('.'))

import sphinx_rtd_theme


# -- Project information -----------------------------------------------------

project = 'MongoDB Connected System'
copyright = '2020, Rob Munroe / Appian Corporation'
author = 'Rob Munroe, Principal Solutions Architect, Appian Corporation'

# The full version, including alpha/beta/rc tags
release = '1.2'


# -- General configuration ---------------------------------------------------

# Add any Sphinx extension module names here, as strings. They can be
# extensions coming with Sphinx (named 'sphinx.ext.*') or your custom
# ones.
extensions = [
    'sphinx_copybutton',
    'sphinx_rtd_theme',
    # 'sphinx.ext.autosectionlabel'
]

# Add any paths that contain templates here, relative to this directory.
templates_path = ['_templates']

# List of patterns, relative to source directory, that match files and
# directories to ignore when looking for source files.
# This pattern also affects html_static_path and html_extra_path.
exclude_patterns = ['_build', 'Thumbs.db', '.DS_Store']


# -- Options for HTML output -------------------------------------------------

# The theme to use for HTML and HTML Help pages.  See the documentation for
# a list of builtin themes.
#
html_theme = 'sphinx_rtd_theme'

# Add any paths that contain custom static files (such as style sheets) here,
# relative to this directory. They are copied after the builtin static files,
# so a file named "default.css" will overwrite the builtin "default.css".
html_static_path = ['_static']

html_context = {
    'css_files': [
        '_static/copybutton.css',  # override copybutton CSS
        '_static/theme_overrides.css',  # override wide tables in RTD theme
        ],
     }
     
html_js_files = [
    'js/custom.js'
]

master_doc = 'index'


### Lexers for Appian and MongoDB

import re

from pygments.lexer import RegexLexer, include, words
from pygments.token import String, Comment, Keyword, Name, Number, Text, Operator, Punctuation
from sphinx.highlighting import lexers

class AppianLexer(RegexLexer):
    name = 'appian'
    
    keywords = words(
        (
            'false', 'true', 'null'
        ),
        suffix = r'\b',
        prefix = r'\b'
    )

    built_in_functions = words(
        (
            'now', 'today'
        ),
        prefix=r'\b',
        suffix=r'\('
    )

    operators = ('+', '-', '*', ">", ">=", "<", "<=", "=", "<>")

    tokens = {
        'root': [
            (r'/\*.*?\*/', Comment.Multiline),
            (r'(rule|cons)![a-zA-Z_]+', Name.Variable),
            (r'M_[a-zA-Z]+', Name.Function), # plugin functions
            (built_in_functions, Name.Function),
            (keywords, Keyword.Type),
            (r'fn![a-zA-Z_]+', Name.Function),
            (words(operators), Operator),
            (r'[][(),:{}\\.]', Punctuation),
            (r'"(\\\\|\\"|[^"])*"', String),
            (r'(local!)?[a-zA-Z_]+', Name.Variable),
            (r'[0-9]+', Number.Integer),
            (r'[0-9.]+', Number.Decimal),
            (r'[0-9.]+', Number.Decimal),
            (r'\s+?', Text),  # Whitespace
        ]
    }


class MongoJsonLexer(RegexLexer):
    # started as a copy of the JsonLexer class

    name = 'mongo'

    flags = re.DOTALL

    built_in_functions = words(
        (
            'ISODate', 'ObjectId'
        ),
        prefix=r'\b',
        suffix=r'\('
    )

    # integer part of a number
    int_part = r'-?(0|[1-9]\d*)'

    # fractional part of a number
    frac_part = r'\.\d+'

    # exponential part of a number
    exp_part = r'[eE](\+|-)?\d+'

    tokens = {
        'whitespace': [
            (r'\s+', Text),
        ],

        # represents a simple terminal value
        'simplevalue': [
            (r'(true|false|null)\b', Keyword.Constant),
            (('%(int_part)s(%(frac_part)s%(exp_part)s|'
              '%(exp_part)s|%(frac_part)s)') % vars(),
             Number.Float),
            (int_part, Number.Integer),
            (r'"(\\(["\\/bfnrt]|u[a-fA-F0-9]]{4})|[^\\"])*"', String.Double),
        ],

        'mongofunction': [
            (r'\(', Punctuation),
            include('simplevalue'),
            (r'\)', Punctuation, '#pop'),
        ],

        # the right hand side of an object, after the attribute name
        'objectattribute': [
            include('value'),
            (r':', Punctuation),
            # comma terminates the attribute but expects more
            (r',', Punctuation, '#pop'),
            # a closing bracket terminates the entire object, so pop twice
            (r'\}', Punctuation, '#pop:2'),
        ],

        # a json object - { attr, attr, ... }
        'objectvalue': [
            include('whitespace'),
            (r'"(\\(["\\/bfnrt]|u[a-fA-F0-9]]{4})|[^\\"])*"', Name.Tag, 'objectattribute'),
            (r'_id', Name.Tag, 'objectattribute'), # Added to support Mongo's bare _id attr
            (r'\}', Punctuation, '#pop'),
        ],

        # json array - [ value, value, ... }
        'arrayvalue': [
            include('whitespace'),
            include('value'),
            (r',', Punctuation),
            (r'\]', Punctuation, '#pop'),
        ],

        # a json value - either a simple value or a complex value (object or array)
        'value': [
            include('whitespace'),
            (built_in_functions, Name.Function, 'mongofunction'), # MongoDB Function
            (r'\/.*\/[imsx]+?', String.Regex), # MongoDB Regex

            include('simplevalue'),
            (r'\{', Punctuation, 'objectvalue'),
            (r'\[', Punctuation, 'arrayvalue'),
        ],

        # the root of a json document whould be a value
        'root': [
            (r'^"\w+"', Name.Tag), # added to handle "JSON segment"
            (r':', Punctuation), # added to handle "JSON segment"
            include('value'),
        ],
    }

lexers['appian'] = AppianLexer(startinline=True)
lexers['mongo'] = MongoJsonLexer(startinline=True)