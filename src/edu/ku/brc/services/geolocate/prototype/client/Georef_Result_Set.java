/**
 * Georef_Result_Set.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package edu.ku.brc.services.geolocate.prototype.client;

public class Georef_Result_Set  implements java.io.Serializable {
    private java.lang.String engineVersion;

    private int numResults;

    private double executionTimems;

    private edu.ku.brc.services.geolocate.prototype.client.Georef_Result[] resultSet;

    public Georef_Result_Set() {
    }

    public Georef_Result_Set(
           java.lang.String engineVersion,
           int numResults,
           double executionTimems,
           edu.ku.brc.services.geolocate.prototype.client.Georef_Result[] resultSet) {
           this.engineVersion = engineVersion;
           this.numResults = numResults;
           this.executionTimems = executionTimems;
           this.resultSet = resultSet;
    }


    /**
     * Gets the engineVersion value for this Georef_Result_Set.
     * 
     * @return engineVersion
     */
    public java.lang.String getEngineVersion() {
        return engineVersion;
    }


    /**
     * Sets the engineVersion value for this Georef_Result_Set.
     * 
     * @param engineVersion
     */
    public void setEngineVersion(java.lang.String engineVersion) {
        this.engineVersion = engineVersion;
    }


    /**
     * Gets the numResults value for this Georef_Result_Set.
     * 
     * @return numResults
     */
    public int getNumResults() {
        return numResults;
    }


    /**
     * Sets the numResults value for this Georef_Result_Set.
     * 
     * @param numResults
     */
    public void setNumResults(int numResults) {
        this.numResults = numResults;
    }


    /**
     * Gets the executionTimems value for this Georef_Result_Set.
     * 
     * @return executionTimems
     */
    public double getExecutionTimems() {
        return executionTimems;
    }


    /**
     * Sets the executionTimems value for this Georef_Result_Set.
     * 
     * @param executionTimems
     */
    public void setExecutionTimems(double executionTimems) {
        this.executionTimems = executionTimems;
    }


    /**
     * Gets the resultSet value for this Georef_Result_Set.
     * 
     * @return resultSet
     */
    public edu.ku.brc.services.geolocate.prototype.client.Georef_Result[] getResultSet() {
        return resultSet;
    }


    /**
     * Sets the resultSet value for this Georef_Result_Set.
     * 
     * @param resultSet
     */
    public void setResultSet(edu.ku.brc.services.geolocate.prototype.client.Georef_Result[] resultSet) {
        this.resultSet = resultSet;
    }

    public edu.ku.brc.services.geolocate.prototype.client.Georef_Result getResultSet(int i) {
        return this.resultSet[i];
    }

    public void setResultSet(int i, edu.ku.brc.services.geolocate.prototype.client.Georef_Result _value) {
        this.resultSet[i] = _value;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Georef_Result_Set)) return false;
        Georef_Result_Set other = (Georef_Result_Set) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.engineVersion==null && other.getEngineVersion()==null) || 
             (this.engineVersion!=null &&
              this.engineVersion.equals(other.getEngineVersion()))) &&
            this.numResults == other.getNumResults() &&
            this.executionTimems == other.getExecutionTimems() &&
            ((this.resultSet==null && other.getResultSet()==null) || 
             (this.resultSet!=null &&
              java.util.Arrays.equals(this.resultSet, other.getResultSet())));
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
        if (getEngineVersion() != null) {
            _hashCode += getEngineVersion().hashCode();
        }
        _hashCode += getNumResults();
        _hashCode += new Double(getExecutionTimems()).hashCode();
        if (getResultSet() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getResultSet());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getResultSet(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Georef_Result_Set.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.museum.tulane.edu/webservices/", "Georef_Result_Set"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("engineVersion");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.museum.tulane.edu/webservices/", "EngineVersion"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("numResults");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.museum.tulane.edu/webservices/", "NumResults"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("executionTimems");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.museum.tulane.edu/webservices/", "ExecutionTimems"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("resultSet");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.museum.tulane.edu/webservices/", "ResultSet"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.museum.tulane.edu/webservices/", "Georef_Result"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        elemField.setMaxOccursUnbounded(true);
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
