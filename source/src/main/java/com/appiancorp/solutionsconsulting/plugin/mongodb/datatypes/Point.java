package com.appiancorp.solutionsconsulting.plugin.mongodb.datatypes;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;


@XmlRootElement(namespace = "urn:com:appian:types:MongoDB", name = "Point")
@XmlType(
        namespace = Point.NAMESPACE_URI,
        name = Point.LOCAL_PART,
        propOrder = {
                "longitude",
                "latitude"
        })
@XmlSeeAlso({Point.class})
public class Point {
    public static final String LOCAL_PART = "Point";
    public static final String NAMESPACE_URI = "urn:com:appian:types:MongoDB";
    public static final QName QNAME = new QName(NAMESPACE_URI, LOCAL_PART);
    private static final long serialVersionUID = 1L;

    private Double longitude;
    private Double latitude;

    public Point(Double longitude, Double latitude) {
        this();
        setLongitude(longitude);
        setLatitude(latitude);
    }

    public Point() {
    } // for serialization only

    /**
     * Returns a string representation of the object. In general, the
     * {@code toString} method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     * It is recommended that all subclasses override this method.
     * <p>
     * The {@code toString} method for class {@code Object}
     * returns a string consisting of the name of the class of which the
     * object is an instance, the at-sign character `{@code @}', and
     * the unsigned hexadecimal representation of the hash code of the
     * object. In other words, this method returns a string equal to the
     * value of:
     * <blockquote>
     * <pre>
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     * </pre></blockquote>
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "[ " + longitude + ", " + latitude + " ]";
    }

    @XmlElement
    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @XmlElement
    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
}
