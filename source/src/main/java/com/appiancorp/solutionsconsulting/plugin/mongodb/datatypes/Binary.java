package com.appiancorp.solutionsconsulting.plugin.mongodb.datatypes;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;


@XmlRootElement(namespace = "urn:com:appian:types:MongoDB", name = "Binary")
@XmlType(
        namespace = "urn:com:appian:types:MongoDB",
        name = Binary.LOCAL_PART,
        propOrder = {
                "binary",
                "type"
        })
@XmlSeeAlso({Binary.class})
public class Binary {
    public static final String LOCAL_PART = "Binary";
    public static final QName QNAME = new QName("urn:com:appian:types:MongoDB", LOCAL_PART);
    private static final long serialVersionUID = 1L;
    private String binary;
    private String type;

    public Binary(String binary, String type) {
        this();
        setBinary(binary);
        setType(type);
    }

    public Binary() {
    } // for serialization only


    @XmlElement
    public String getBinary() {
        return binary;
    }

    public void setBinary(String binary) {
        this.binary = binary;
    }

    @XmlElement
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
