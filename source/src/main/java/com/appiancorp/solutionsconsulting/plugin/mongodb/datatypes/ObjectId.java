package com.appiancorp.solutionsconsulting.plugin.mongodb.datatypes;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;


@XmlRootElement(namespace = "urn:com:appian:types:MongoDB", name = "ObjectId")
@XmlType(
        namespace = ObjectId.NAMESPACE_URI,
        name = ObjectId.LOCAL_PART,
        propOrder = {
                "oid"
        })
@XmlSeeAlso({ObjectId.class})
public class ObjectId {
    public static final String LOCAL_PART = "ObjectId";
    public static final String NAMESPACE_URI = "urn:com:appian:types:MongoDB";
    public static final QName QNAME = new QName(NAMESPACE_URI, LOCAL_PART);
    private static final long serialVersionUID = 1L;
    private String oid;

    public ObjectId(String oid) {
        this();
        setOid(oid);
    }

    public ObjectId() {
    } // for serialization only

    @XmlElement
    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }
}
