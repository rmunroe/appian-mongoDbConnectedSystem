package com.appiancorp.solutionsconsulting.plugin.mongodb.datatypes;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import java.io.Serializable;

/**
 * A custom data type (CDT) for representing MongoDB's <em>ObjectID</em> field in an Appian-friendly way.
 *
 * <p>This ensures smooth integration with workflows that need to handle the MongoDB <em>_id</em> field
 * through standard Appian objects.</p>
 *
 * <p>Note: If the underlying MongoDB collection has documents without a traditional ObjectID or uses
 * alternative key fields, this class will still provide a consistent pattern for referencing identifiers
 * in Appian.</p>
 *
 * @author Rob Munroe
 * @since 1.0
 */
@XmlRootElement(namespace = "urn:com:appian:types:MongoDB", name = "ObjectId")
@XmlType(
        namespace = ObjectId.NAMESPACE_URI,
        name = ObjectId.LOCAL_PART,
        propOrder = {
                "oid"
        })
@XmlSeeAlso({ObjectId.class})
public class ObjectId implements Serializable {

    /**
     * The element name (local part) for the {@link ObjectId} CDT.
     */
    public static final String LOCAL_PART = "ObjectId";

    /**
     * The namespace URI used by this CDT.
     */
    public static final String NAMESPACE_URI = "urn:com:appian:types:MongoDB";

    /**
     * A qualified name (QName) associating this class with its namespace and local part.
     */
    public static final QName QNAME = new QName(NAMESPACE_URI, LOCAL_PART);

    private static final long serialVersionUID = 1L;

    /**
     * A string representation of MongoDB's ObjectID (e.g., "6076fa3ab7f57c0477c34773").
     */
    private String oid;

    /**
     * Constructs a new {@code ObjectId} object with the given string representation.
     *
     * @param oid the string representation of MongoDB's ObjectID
     */
    public ObjectId(String oid) {
        this();
        setOid(oid);
    }

    /**
     * Default no-argument constructor required for serialization/deserialization.
     */
    public ObjectId() {
        // For serialization only
    }

    /**
     * Gets the MongoDB ObjectID string.
     *
     * @return the ObjectID as a {@code String}
     */
    @XmlElement
    public String getOid() {
        return oid;
    }

    /**
     * Sets the MongoDB ObjectID string.
     *
     * @param oid the new ObjectID string to set
     */
    public void setOid(String oid) {
        this.oid = oid;
    }
}