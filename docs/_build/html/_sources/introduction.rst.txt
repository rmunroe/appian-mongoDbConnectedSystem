############
Introduction
############

This plugin implements an Appian `Connected System <https://docs.appian.com/suite/help/latest/Connected_System.html>`__ for `MongoDB <https://www.mongodb.com>`__ using the `MongoDB Java Driver <https://mongodb.github.io/mongo-java-driver/3.12/>`__ in synchronous mode.

Its official distribution location is this entry in the `Appian Community App Market <https://community.appian.com/b/appmarket/posts/mongodb-connected-system>`_.


.. _Project Info:

************
Project Info
************

This plugin was created by me, `Rob Munroe <https://www.linkedin.com/in/robertmunroe/>`_, Principal Solutions Architect at `Appian <https://appian.com>`_. I have been in software development since the late 90s, have been at Appian for nearly a decade now, and before joining was an Appian customer.

I have also been a fan of MongoDB since it first arrived in the late-00s. I feel that the speed and power of both Appian and MongoDB are a potent combination, and that both platforms pair very well given the strong support for JSON and dynamic data structures. Many customers have already integrated the two platforms using the standard `HTTP/REST Connected System <https://docs.appian.com/suite/help/latest/Integration_Object.html>`_, however I wanted to make it even easier to use MongoDB inside of Appian as well as take advantage of what the MongoDB Java Driver had to offer.

The benefits of using this plugin versus MongoDB’s REST API include:

-  Most common MongoDB operations are provided as low-code `Integration objects <https://docs.appian.com/suite/help/latest/Integration_Object.html>`_, making it very easy to quickly integrate Appian and MongoDB

-  Uses connection pooling to reduce authentication time per operation

-  Uses the `MongoDB Wire Protocol <https://docs.mongodb.com/manual/reference/mongodb-wire-protocol/>`__ with `BSON <http://bsonspec.org>`_ instead of HTTP/REST with JSON for more efficient communication

-  Automatically handles the conversion of MongoDB BSON Documents to Appian Dictionaries, or alternatively provides MongoDB-created JSON representations

-  Can import from and export to Appian Documents containing JSON

-  Configured using a single `Connection String <https://docs.mongodb.com/manual/reference/connection-string/>`__, which is masked and encrypted

This project and `its source <https://github.com/rmunroe/appian-mongoDbConnectedSystem>`_ are open to all, however it is best that any enhancements or bug fixes are submitted to me, so that I may update the released version on the `Appian App Market <https://community.appian.com/b/appmarket/posts/mongodb-connected-system>`_ page. This will significantly speed up the process of getting your enhancements onto your Appian Cloud instance. Self-managed customers can build and deploy the code themselves, but I hope you will contribute your fixes and enhancements for the greater good.

If you are interested in contributing to this project, contact me via my Appian email address [munroe at appian dot com] or via `LinkedIn <https://www.linkedin.com/in/robertmunroe/>`_.


********
Audience
********

Users of this Connected System plugin are expected to be familiar with the `core concepts of MongoDB <https://docs.mongodb.com/manual/introduction/>`__, including topics such as querying and aggregation syntax. Additionally, it is expected that you are familiar with your own Databases, Collections, Document schemas, and MongoDB server infrastructure.

Users are also expected to be familiar with Appian, building Applications, and how and why `Connected Systems <https://docs.appian.com/suite/help/latest/Connected_System.html>`__ and `Integration Objects <https://docs.appian.com/suite/help/latest/Integration_Object.html>`__ are used.


*************
Compatibility
*************

This plugin was built and tested on Appian versions 19.4 and 20.2.

Version 1.2 of this plugin uses the `MongoDB Java Driver version 3.12.5 <https://mongodb.github.io/mongo-java-driver/3.12/>`__ and should be compatible with any version of MongoDB from version 2.6 to 4.2 (the latest as of this writing). See this `compatibility chart <https://mongodb.github.io/mongo-java-driver/3.12/upgrading/#compatibility>`__ for full details.

This plugin was tested against a `MongoDB Atlas <https://www.mongodb.com/cloud/atlas>`__ a 3-node Replica Set instance running version 4.2.8 as well as a Standalone MongoDB 4.2.8 Community instance running on Ubuntu 20.04.

It is expected that your MongoDB instance(s) allow network connections from your Appian instance(s).

************
Installation
************

If installing to a fully-managed Appian Cloud instance, install using the `Plugins <https://docs.appian.com/suite/help/latest/Appian_Administration_Console.html#plug-ins>`__ panel of the `Administration Console <https://docs.appian.com/suite/help/latest/Appian_Administration_Console.html>`__.

.. figure:: media/image2.png

.. figure:: media/image3.png

If installing to a self-managed Appian instance, copy the ``ps-plugin-MongoDbConnectedSystem-X.X.jar`` file to the ``<APPIAN_HOME>/_admin/plugins`` directory.


************************
Plugin Design Principles
************************

This plugin was designed to mirror the functionality provided by MongoDB’s Java Driver. We have implemented the most common functionality as individual Integration Operations and are striving for 100% feature completeness over time. Please let `me <Project Info>`_ know if there are missing features that you require.

Many of the operations of the MongoDB Java Driver take as arguments MongoDB BSON Documents. As such, Integration Operations that require BSON Documents will instead accept JSON strings, which the plugin handles converting to BSON. We have included a full suite of `JSON Query Expression Functions <#_JSON_Query_Expression_1>`__ to cleanly and easily generate MongoDB JSON-based queries.


.. _Special Considerations:

**********************
Special Considerations
**********************

Due to how the MongoDB Java Driver returns Object IDs and Binary data types, special handling must be performed by the Connected System as described below.

.. important:: It is critical that you understand these special cases and design for them accordingly


Dates and Times
===============

Dates and times in MongoDB are different from most other databases in that it stores them in UTC by default and will convert any local time representations into this form. Applications that must operate or report on some unmodified local time value must store the time zone offset alongside the UTC timestamp (e.g. as a separate field) and compute the original local time in their application logic.

.. important:: It is up to you and your Application to account for this.

When storing Date and Time values from Appian to MongoDB, MongoDB will convert them to UTC. Therefore, any of your date queries should be against UTC dates and times. Use Appian’s built-in Date and Time `functions <https://docs.appian.com/suite/help/latest/Appian_Functions.html>`__ to ensure the dates being sent to MongoDB make sense in this regard.

Also note that unlike Appian, there is no date *without time* data type in MongoDB. When wishing to store a date without time, use midnight UTC of that date (e.g. ``2020-07-01T00:00:00.000Z``) and query accordingly. The `JSON Query Expression Functions <#_JSON_Query_Expression_1>`__ will take this into account and convert Appian Dates to this format.


Object IDs
==========

While the `MongoDB Object ID <https://docs.mongodb.com/manual/reference/method/ObjectId/>`__ data type is most often represented as a string (e.g.  "5efa0b06fc13ae730e00024a"), it is stored internally as 12-byte values broken down into several data points. It is far easier to work with the string value in Appian, so this Connected System will return a sub-Dictionary of the below form for each Object ID in the resulting dataset.

.. note:: This transformation only applies to results returned as Appian Dictionaries. JSON results use the MongoDB JSON notation.

A MongoDB Document representing this value:

.. code-block:: JSON

  {
    _id: ObjectId("5efa0b06fc13ae730e00024a")
    ...
  }

Would be returned as an Appian Dictionary like this:

.. code-block:: JSON

    {
      _id: {
        oid: "5efa0b06fc13ae730e00024a"
      }
      ...
    }

The key detail here is that Object ID fields will be accessed like this
in Appian:::

    local!theObjectId: local!myDocument._id.oid


Object ID CDT
-------------


This plugin contains a CDT named ``{urn:com:appian:types:MongoDB}ObjectId`` that can be used to represent these values in a consistent manner.  MongoDB Document properties in Dictionaries can be cast directly to this CDT.

It is highly recommended that you use this CDT when creating your own CDTs that represent the MongoDB Documents used in your application.  Using it also helps convert Appian Dictionaries representing MongoDB Documents to Mongo-friendly JSON using the ``mdb_tojson`` function, and the ``mdb_tojson`` function will return this value as well.

See the ``{urn:com:appian:types:MCSD}MCSD_Customer`` CDT in the demo application for example.


Binary
======

MongoDB’s Binary data type allows you to store chunks of binary data in a MongoDB Document, however Appian does not support storing binary data in Dictionaries. To work around this any binary results will be returned as Base64 encoded text.

.. note:: This transformation only applies to results returned as Appian Dictionaries. JSON results use the MongoDB JSON notation.

.. warning:: Returning large amounts of Base64 encoded binary data to Appian can have severe impacts on the performance of the Appian environment. A best practice would be to use a projection and eliminate the binary field from the MongoDB Document.

A MongoDB Document representing this value: ::

    {
      binaryField: Binary("... Binary data value ...", 0)
      ...
    }

Would be returned as an Appian Dictionary like this: ::

    {
      binaryField: {
        binary: "...Base64 encoded data...",
        type: "0"
      }
      ...
    }


Binary CDT
----------

Similarly to Object ID, this plugin contains a CDT named ``{urn:com:appian:types:MongoDB}Binary`` that can be used to represent these values in a consistent manner. MongoDB Document properties in Dictionaries can be cast directly to this CDT.

It is highly recommended that you use this CDT when creating your own CDTs that represent the MongoDB Documents used in your application.  Using it also helps convert Appian Dictionaries representing MongoDB Documents to Mongo-friendly JSON using the ``mdb_tojson`` function, and the ``mdb_tojson`` function will return this value as well.

See the ``{urn:com:appian:types:MCSD}MCSD_Customer`` CDT in the demo application for example.
