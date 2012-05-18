/**
 * GetAllElevations.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package edu.ku.brc.services.usgs.elevation;

public class GetAllElevations  implements java.io.Serializable {
    private java.lang.String x_Value;

    private java.lang.String y_Value;

    private java.lang.String elevation_Units;

    public GetAllElevations() {
    }

    public GetAllElevations(
           java.lang.String x_Value,
           java.lang.String y_Value,
           java.lang.String elevation_Units) {
           this.x_Value = x_Value;
           this.y_Value = y_Value;
           this.elevation_Units = elevation_Units;
    }


    /**
     * Gets the x_Value value for this GetAllElevations.
     * 
     * @return x_Value
     */
    public java.lang.String getX_Value() {
        return x_Value;
    }


    /**
     * Sets the x_Value value for this GetAllElevations.
     * 
     * @param x_Value
     */
    public void setX_Value(java.lang.String x_Value) {
        this.x_Value = x_Value;
    }


    /**
     * Gets the y_Value value for this GetAllElevations.
     * 
     * @return y_Value
     */
    public java.lang.String getY_Value() {
        return y_Value;
    }


    /**
     * Sets the y_Value value for this GetAllElevations.
     * 
     * @param y_Value
     */
    public void setY_Value(java.lang.String y_Value) {
        this.y_Value = y_Value;
    }


    /**
     * Gets the elevation_Units value for this GetAllElevations.
     * 
     * @return elevation_Units
     */
    public java.lang.String getElevation_Units() {
        return elevation_Units;
    }


    /**
     * Sets the elevation_Units value for this GetAllElevations.
     * 
     * @param elevation_Units
     */
    public void setElevation_Units(java.lang.String elevation_Units) {
        this.elevation_Units = elevation_Units;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetAllElevations)) return false;
        GetAllElevations other = (GetAllElevations) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.x_Value==null && other.getX_Value()==null) || 
             (this.x_Value!=null &&
              this.x_Value.equals(other.getX_Value()))) &&
            ((this.y_Value==null && other.getY_Value()==null) || 
             (this.y_Value!=null &&
              this.y_Value.equals(other.getY_Value()))) &&
            ((this.elevation_Units==null && other.getElevation_Units()==null) || 
             (this.elevation_Units!=null &&
              this.elevation_Units.equals(other.getElevation_Units())));
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
        if (getX_Value() != null) {
            _hashCode += getX_Value().hashCode();
        }
        if (getY_Value() != null) {
            _hashCode += getY_Value().hashCode();
        }
        if (getElevation_Units() != null) {
            _hashCode += getElevation_Units().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GetAllElevations.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://gisdata.usgs.gov/XMLWebServices2/", ">getAllElevations"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("x_Value");
        elemField.setXmlName(new javax.xml.namespace.QName("http://gisdata.usgs.gov/XMLWebServices2/", "X_Value"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("y_Value");
        elemField.setXmlName(new javax.xml.namespace.QName("http://gisdata.usgs.gov/XMLWebServices2/", "Y_Value"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("elevation_Units");
        elemField.setXmlName(new javax.xml.namespace.QName("http://gisdata.usgs.gov/XMLWebServices2/", "Elevation_Units"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
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
