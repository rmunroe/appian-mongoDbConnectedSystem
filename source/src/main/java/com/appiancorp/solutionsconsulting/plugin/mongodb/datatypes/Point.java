package com.appiancorp.solutionsconsulting.plugin.mongodb.datatypes;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import java.io.Serializable;

/**
 * A custom data type (CDT) for representing a geospatial <em>Point</em> in MongoDB.
 *
 * <p>This ensures smooth integration with workflows that need to handle
 * location-based or geospatial data (e.g., for queries that use $geoWithin,
 * $near, or other MongoDB geospatial operators) through standard Appian objects.</p>
 *
 * <p>Note: For these operations, MongoDB typically expects a
 * <strong>GeoJSON</strong> structure like:
 * <pre>{@code
 * {
 *   "type": "Point",
 *   "coordinates": [ <longitude>, <latitude> ]
 * }
 * }</pre>
 * The fields in this CDT facilitate this representation when interacting
 * with MongoDB.</p>
 *
 * @author Rob Munroe
 * @since 1.0
 */
@XmlRootElement(namespace = "urn:com:appian:types:MongoDB", name = "Point")
@XmlType(
        namespace = Point.NAMESPACE_URI,
        name = Point.LOCAL_PART,
        propOrder = {
                "latitude",
                "longitude"
        })
@XmlSeeAlso({Point.class})
public class Point implements Serializable {

    /**
     * The local part (element name) for the {@link Point} CDT.
     */
    public static final String LOCAL_PART = "Point";

    /**
     * The namespace URI for this CDT.
     */
    public static final String NAMESPACE_URI = "urn:com:appian:types:MongoDB";

    /**
     * A qualified name (QName) associating this class with its namespace and local part.
     */
    public static final QName QNAME = new QName(NAMESPACE_URI, LOCAL_PART);

    private static final long serialVersionUID = 1L;

    /**
     * The latitude component of the Point (in decimal degrees).
     */
    private double latitude;

    /**
     * The longitude component of the Point (in decimal degrees).
     */
    private double longitude;

    /**
     * Constructs a new {@code Point} with the specified latitude and longitude.
     *
     * @param latitude  the latitude in decimal degrees
     * @param longitude the longitude in decimal degrees
     */
    public Point(double latitude, double longitude) {
        this();
        setLatitude(latitude);
        setLongitude(longitude);
    }

    /**
     * Default no-argument constructor required for serialization/deserialization.
     */
    public Point() {
        // For serialization only
    }

    /**
     * Gets the latitude of this Point.
     *
     * @return the latitude in decimal degrees
     */
    @XmlElement
    public double getLatitude() {
        return latitude;
    }

    /**
     * Sets the latitude for this Point.
     *
     * @param latitude the latitude in decimal degrees
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * Gets the longitude of this Point.
     *
     * @return the longitude in decimal degrees
     */
    @XmlElement
    public double getLongitude() {
        return longitude;
    }

    /**
     * Sets the longitude for this Point.
     *
     * @param longitude the longitude in decimal degrees
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}