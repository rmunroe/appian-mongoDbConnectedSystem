import re

from pygments.lexer import RegexLexer, include, words
from pygments.token import String, Comment, Keyword, Name, Number, Text, Operator, Punctuation

class MongoJsonLexer(RegexLexer):
    # started as a copy of the JsonLexer class

    name = 'mongo'

    flags = re.DOTALL

    built_in_functions = words(
        (
            'ISODate', 'ObjectId', 'Binary'
        ),
        prefix = r'\b',
        suffix = r'\('
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
            include('whitespace'),
            (r',', Punctuation),
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

            (r'_id', Name.Tag, 'objectattribute'), # Added for MongoDB's bare _id attribute

            (r'"(\\(["\\/bfnrt]|u[a-fA-F0-9]]{4})|[^\\"])*"', Name.Tag, 'objectattribute'),
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

            (r'\.\.\.', Comment.Single), # added for yada, yada, yada
            (built_in_functions, Name.Function.Magic, 'mongofunction'), # MongoDB Function
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


