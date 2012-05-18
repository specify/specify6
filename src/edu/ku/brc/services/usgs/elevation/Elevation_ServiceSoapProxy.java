package edu.ku.brc.services.usgs.elevation;

public class Elevation_ServiceSoapProxy implements edu.ku.brc.services.usgs.elevation.Elevation_ServiceSoap {
  private String _endpoint = null;
  private edu.ku.brc.services.usgs.elevation.Elevation_ServiceSoap elevation_ServiceSoap = null;
  
  public Elevation_ServiceSoapProxy() {
    _initElevation_ServiceSoapProxy();
  }
  
  public Elevation_ServiceSoapProxy(String endpoint) {
    _endpoint = endpoint;
    _initElevation_ServiceSoapProxy();
  }
  
  private void _initElevation_ServiceSoapProxy() {
    try {
      elevation_ServiceSoap = (new edu.ku.brc.services.usgs.elevation.Elevation_ServiceLocator()).getElevation_ServiceSoap();
      if (elevation_ServiceSoap != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)elevation_ServiceSoap)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)elevation_ServiceSoap)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (elevation_ServiceSoap != null)
      ((javax.xml.rpc.Stub)elevation_ServiceSoap)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public edu.ku.brc.services.usgs.elevation.Elevation_ServiceSoap getElevation_ServiceSoap() {
    if (elevation_ServiceSoap == null)
      _initElevation_ServiceSoapProxy();
    return elevation_ServiceSoap;
  }
  
  public edu.ku.brc.services.usgs.elevation.GetElevationResponseGetElevationResult getElevation(java.lang.String x_Value, java.lang.String y_Value, java.lang.String elevation_Units, java.lang.String source_Layer, java.lang.String elevation_Only) throws java.rmi.RemoteException{
    if (elevation_ServiceSoap == null)
      _initElevation_ServiceSoapProxy();
    return elevation_ServiceSoap.getElevation(x_Value, y_Value, elevation_Units, source_Layer, elevation_Only);
  }
  
  public edu.ku.brc.services.usgs.elevation.GetAllElevationsResponseGetAllElevationsResult getAllElevations(java.lang.String x_Value, java.lang.String y_Value, java.lang.String elevation_Units) throws java.rmi.RemoteException{
    if (elevation_ServiceSoap == null)
      _initElevation_ServiceSoapProxy();
    return elevation_ServiceSoap.getAllElevations(x_Value, y_Value, elevation_Units);
  }
  
  
}