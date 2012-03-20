/**
 * Georef_Result.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package edu.ku.brc.services.geolocate.prototype.client;

import java.math.BigDecimal;

public class Georef_Result  implements java.io.Serializable {
    private edu.ku.brc.services.geolocate.prototype.client.GeographicPoint WGS84Coordinate;

    private java.lang.String parsePattern;

    private java.lang.String precision;

    private int score;

    private java.lang.String uncertaintyRadiusMeters;

    private java.lang.String uncertaintyPolygon;

    private java.lang.String referenceLocation;

    private double displacedDistanceMiles;

    private double displacedHeadingDegrees;
    
    private String errorPolygon;
    private BigDecimal errorEstimate;


    private java.lang.String debug;

    public Georef_Result() {
    }

    public Georef_Result(
           edu.ku.brc.services.geolocate.prototype.client.GeographicPoint WGS84Coordinate,
           java.lang.String parsePattern,
           java.lang.String precision,
           int score,
           java.lang.String uncertaintyRadiusMeters,
           java.lang.String uncertaintyPolygon,
           java.lang.String referenceLocation,
           double displacedDistanceMiles,
           double displacedHeadingDegrees,
           String errorPolygon,
           BigDecimal errorEstimate,
           java.lang.String debug) 
    {
           this.WGS84Coordinate = WGS84Coordinate;
           this.parsePattern = parsePattern;
           this.precision = precision;
           this.score = score;
           this.uncertaintyRadiusMeters = uncertaintyRadiusMeters;
           this.uncertaintyPolygon = uncertaintyPolygon;
           this.referenceLocation = referenceLocation;
           this.displacedDistanceMiles = displacedDistanceMiles;
           this.displacedHeadingDegrees = displacedHeadingDegrees;
           this.debug = debug;
           this.errorPolygon = errorPolygon;
           this.errorEstimate = errorEstimate;
    }


    /**
     * Gets the WGS84Coordinate value for this Georef_Result.
     * 
     * @return WGS84Coordinate
     */
    public edu.ku.brc.services.geolocate.prototype.client.GeographicPoint getWGS84Coordinate() {
        return WGS84Coordinate;
    }


    /**
     * Sets the WGS84Coordinate value for this Georef_Result.
     * 
     * @param WGS84Coordinate
     */
    public void setWGS84Coordinate(edu.ku.brc.services.geolocate.prototype.client.GeographicPoint WGS84Coordinate) {
        this.WGS84Coordinate = WGS84Coordinate;
    }


    /**
     * Gets the parsePattern value for this Georef_Result.
     * 
     * @return parsePattern
     */
    public java.lang.String getParsePattern() {
        return parsePattern;
    }


    /**
     * Sets the parsePattern value for this Georef_Result.
     * 
     * @param parsePattern
     */
    public void setParsePattern(java.lang.String parsePattern) {
        this.parsePattern = parsePattern;
    }


    /**
     * Gets the precision value for this Georef_Result.
     * 
     * @return precision
     */
    public java.lang.String getPrecision() {
        return precision;
    }


    /**
     * Sets the precision value for this Georef_Result.
     * 
     * @param precision
     */
    public void setPrecision(java.lang.String precision) {
        this.precision = precision;
    }


    /**
     * Gets the score value for this Georef_Result.
     * 
     * @return score
     */
    public int getScore() {
        return score;
    }


    /**
     * Sets the score value for this Georef_Result.
     * 
     * @param score
     */
    public void setScore(int score) {
        this.score = score;
    }


    /**
     * Gets the uncertaintyRadiusMeters value for this Georef_Result.
     * 
     * @return uncertaintyRadiusMeters
     */
    public java.lang.String getUncertaintyRadiusMeters() {
        return uncertaintyRadiusMeters;
    }


    /**
     * Sets the uncertaintyRadiusMeters value for this Georef_Result.
     * 
     * @param uncertaintyRadiusMeters
     */
    public void setUncertaintyRadiusMeters(java.lang.String uncertaintyRadiusMeters) {
        this.uncertaintyRadiusMeters = uncertaintyRadiusMeters;
    }


    /**
     * Gets the uncertaintyPolygon value for this Georef_Result.
     * 
     * @return uncertaintyPolygon
     */
    public java.lang.String getUncertaintyPolygon() {
        return uncertaintyPolygon;
    }


    /**
     * Sets the uncertaintyPolygon value for this Georef_Result.
     * 
     * @param uncertaintyPolygon
     */
    public void setUncertaintyPolygon(java.lang.String uncertaintyPolygon) {
        this.uncertaintyPolygon = uncertaintyPolygon;
    }


    /**
     * Gets the referenceLocation value for this Georef_Result.
     * 
     * @return referenceLocation
     */
    public java.lang.String getReferenceLocation() {
        return referenceLocation;
    }


    /**
     * Sets the referenceLocation value for this Georef_Result.
     * 
     * @param referenceLocation
     */
    public void setReferenceLocation(java.lang.String referenceLocation) {
        this.referenceLocation = referenceLocation;
    }


    /**
     * Gets the displacedDistanceMiles value for this Georef_Result.
     * 
     * @return displacedDistanceMiles
     */
    public double getDisplacedDistanceMiles() {
        return displacedDistanceMiles;
    }


    /**
     * Sets the displacedDistanceMiles value for this Georef_Result.
     * 
     * @param displacedDistanceMiles
     */
    public void setDisplacedDistanceMiles(double displacedDistanceMiles) {
        this.displacedDistanceMiles = displacedDistanceMiles;
    }


    /**
     * Gets the displacedHeadingDegrees value for this Georef_Result.
     * 
     * @return displacedHeadingDegrees
     */
    public double getDisplacedHeadingDegrees() {
        return displacedHeadingDegrees;
    }


    /**
     * Sets the displacedHeadingDegrees value for this Georef_Result.
     * 
     * @param displacedHeadingDegrees
     */
    public void setDisplacedHeadingDegrees(double displacedHeadingDegrees) {
        this.displacedHeadingDegrees = displacedHeadingDegrees;
    }


    /**
     * @return the errorPolygon
     */
    public String getErrorPolygon()
    {
        return errorPolygon;
    }

    /**
     * @param errorPolygon the errorPolygon to set
     */
    public void setErrorPolygon(String errorPolygon)
    {
        this.errorPolygon = errorPolygon;
    }

    /**
     * @return the errorEstimate
     */
    public BigDecimal getErrorEstimate()
    {
        return errorEstimate;
    }

    /**
     * @param errorEstimate the errorEstimate to set
     */
    public void setErrorEstimate(BigDecimal errorEstimate)
    {
        this.errorEstimate = errorEstimate;
    }

    /**
     * Gets the debug value for this Georef_Result.
     * 
     * @return debug
     */
    public java.lang.String getDebug() {
        return debug;
    }


    /**
     * Sets the debug value for this Georef_Result.
     * 
     * @param debug
     */
    public void setDebug(java.lang.String debug) {
        this.debug = debug;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof Georef_Result)) return false;
        Georef_Result other = (Georef_Result) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.WGS84Coordinate==null && other.getWGS84Coordinate()==null) || 
             (this.WGS84Coordinate!=null &&
              this.WGS84Coordinate.equals(other.getWGS84Coordinate()))) &&
            ((this.parsePattern==null && other.getParsePattern()==null) || 
             (this.parsePattern!=null &&
              this.parsePattern.equals(other.getParsePattern()))) &&
            ((this.precision==null && other.getPrecision()==null) || 
             (this.precision!=null &&
              this.precision.equals(other.getPrecision()))) &&
            this.score == other.getScore() &&
            ((this.uncertaintyRadiusMeters==null && other.getUncertaintyRadiusMeters()==null) || 
             (this.uncertaintyRadiusMeters!=null &&
              this.uncertaintyRadiusMeters.equals(other.getUncertaintyRadiusMeters()))) &&
            ((this.uncertaintyPolygon==null && other.getUncertaintyPolygon()==null) || 
             (this.uncertaintyPolygon!=null &&
              this.uncertaintyPolygon.equals(other.getUncertaintyPolygon()))) &&
            ((this.referenceLocation==null && other.getReferenceLocation()==null) || 
             (this.referenceLocation!=null &&
              this.referenceLocation.equals(other.getReferenceLocation()))) &&
            this.displacedDistanceMiles == other.getDisplacedDistanceMiles() &&
            this.displacedHeadingDegrees == other.getDisplacedHeadingDegrees() &&
            ((this.debug==null && other.getDebug()==null) || 
             (this.debug!=null &&
              this.debug.equals(other.getDebug())));
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
        if (getWGS84Coordinate() != null) {
            _hashCode += getWGS84Coordinate().hashCode();
        }
        if (getParsePattern() != null) {
            _hashCode += getParsePattern().hashCode();
        }
        if (getPrecision() != null) {
            _hashCode += getPrecision().hashCode();
        }
        _hashCode += getScore();
        if (getUncertaintyRadiusMeters() != null) {
            _hashCode += getUncertaintyRadiusMeters().hashCode();
        }
        if (getUncertaintyPolygon() != null) {
            _hashCode += getUncertaintyPolygon().hashCode();
        }
        if (getReferenceLocation() != null) {
            _hashCode += getReferenceLocation().hashCode();
        }
        _hashCode += new Double(getDisplacedDistanceMiles()).hashCode();
        _hashCode += new Double(getDisplacedHeadingDegrees()).hashCode();
        if (getDebug() != null) {
            _hashCode += getDebug().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(Georef_Result.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.museum.tulane.edu/webservices/", "Georef_Result"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("WGS84Coordinate");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.museum.tulane.edu/webservices/", "WGS84Coordinate"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.museum.tulane.edu/webservices/", "GeographicPoint"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("parsePattern");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.museum.tulane.edu/webservices/", "ParsePattern"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("precision");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.museum.tulane.edu/webservices/", "Precision"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("score");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.museum.tulane.edu/webservices/", "Score"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("uncertaintyRadiusMeters");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.museum.tulane.edu/webservices/", "UncertaintyRadiusMeters"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("uncertaintyPolygon");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.museum.tulane.edu/webservices/", "UncertaintyPolygon"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("referenceLocation");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.museum.tulane.edu/webservices/", "ReferenceLocation"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("displacedDistanceMiles");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.museum.tulane.edu/webservices/", "DisplacedDistanceMiles"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("displacedHeadingDegrees");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.museum.tulane.edu/webservices/", "DisplacedHeadingDegrees"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "double"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("debug");
        elemField.setXmlName(new javax.xml.namespace.QName("http://www.museum.tulane.edu/webservices/", "Debug"));
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
