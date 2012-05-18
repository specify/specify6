/**
 * GetAllElevationsResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package edu.ku.brc.services.usgs.elevation;

public class GetAllElevationsResponse  implements java.io.Serializable {
    private edu.ku.brc.services.usgs.elevation.GetAllElevationsResponseGetAllElevationsResult getAllElevationsResult;

    public GetAllElevationsResponse() {
    }

    public GetAllElevationsResponse(
           edu.ku.brc.services.usgs.elevation.GetAllElevationsResponseGetAllElevationsResult getAllElevationsResult) {
           this.getAllElevationsResult = getAllElevationsResult;
    }


    /**
     * Gets the getAllElevationsResult value for this GetAllElevationsResponse.
     * 
     * @return getAllElevationsResult
     */
    public edu.ku.brc.services.usgs.elevation.GetAllElevationsResponseGetAllElevationsResult getGetAllElevationsResult() {
        return getAllElevationsResult;
    }


    /**
     * Sets the getAllElevationsResult value for this GetAllElevationsResponse.
     * 
     * @param getAllElevationsResult
     */
    public void setGetAllElevationsResult(edu.ku.brc.services.usgs.elevation.GetAllElevationsResponseGetAllElevationsResult getAllElevationsResult) {
        this.getAllElevationsResult = getAllElevationsResult;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetAllElevationsResponse)) return false;
        GetAllElevationsResponse other = (GetAllElevationsResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.getAllElevationsResult==null && other.getGetAllElevationsResult()==null) || 
             (this.getAllElevationsResult!=null &&
              this.getAllElevationsResult.equals(other.getGetAllElevationsResult())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getGetAllElevationsResult() != null) {
            _hashCode += getGetAllElevationsResult().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GetAllElevationsResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://gisdata.usgs.gov/XMLWebServices2/", ">getAllElevationsResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("getAllElevationsResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://gisdata.usgs.gov/XMLWebServices2/", "getAllElevationsResult"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://gisdata.usgs.gov/XMLWebServices2/", ">>getAllElevationsResponse>getAllElevationsResult"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
