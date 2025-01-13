package com.appiancorp.solutionsconsulting.plugin.mongodb.datatypes;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import java.io.Serializable;

/**
 * A custom data type (CDT) for representing MongoDB's binary data in an Appian-friendly format.
 *
 * <p>This ensures smooth integration with workflows that need to handle
 * MongoDB's binary data types—like images, documents, or other binary payloads—through standard
 * Appian objects.</p>
 *
 * @author Rob Munroe
 * @since 1.0
 */
@XmlRootElement(namespace = "urn:com:appian:types:MongoDB", name = "Binary")
@XmlType(
        namespace = Binary.NAMESPACE_URI,
        name = Binary.LOCAL_PART,
        propOrder = {
                "binary",
                "type"
        })
@XmlSeeAlso({Binary.class})
public class Binary implements Serializable {
    /**
     * The local part (element name) for the {@link Binary} CDT.
     */
    public static final String LOCAL_PART = "Binary";

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
     * A base64-encoded representation of the binary data.
     */
    private String binary;

    /**
     * The subtype or format that the binary data represents (e.g., for special encodings or file types).
     */
    private String type;

    /**
     * Constructs a new {@code Binary} object with the given base64-encoded data and type.
     *
     * @param binary the base64-encoded binary data
     * @param type   the subtype/format for the binary data
     */
    public Binary(String binary, String type) {
        this();
        setBinary(binary);
        setType(type);
    }

    /**
     * Default no-arguments constructor required for serialization/deserialization.
     */
    public Binary() {
        // For serialization only
    }

    /**
     * Gets the base64-encoded binary data.
     *
     * @return the base64-encoded binary string
     */
    @XmlElement
    public String getBinary() {
        return binary;
    }

    /**
     * Sets the base64-encoded binary data.
     *
     * @param binary the base64-encoded binary string to set
     */
    public void setBinary(String binary) {
        this.binary = binary;
    }

    /**
     * Gets the subtype/format of the binary data.
     *
     * @return the subtype string
     */
    @XmlElement
    public String getType() {
        return type;
    }

    /**
     * Sets the subtype/format of the binary data.
     *
     * @param type the subtype string to set
     */
    public void setType(String type) {
        this.type = type;
    }
}