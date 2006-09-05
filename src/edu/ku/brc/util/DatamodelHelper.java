package edu.ku.brc.util;
 
import java.io.File;
 
import org.apache.log4j.Logger;

import edu.ku.brc.helpers.XMLHelper;
 
public class DatamodelHelper
{
    // Static Data Members
    private static final Logger log = Logger.getLogger(DatamodelHelper.class);
 public DatamodelHelper()
 {
  super();
  // TODO Auto-generated constructor stub
 }
    /**
     * Returns full path to file in hbm directory
     * @param fileName the name of the file to be read
     * @return the path to the file
     */
     public static String getHbmDirPath()
     {
      String fileName = "Accession.hbm.xml";
         String path = new File(".").getAbsolutePath();
         if (path.endsWith("."))
         {
             path = path.substring(0, path.length() - 2);
         }
         return path + File.separator +  
         File.separator + "src" +
         File.separator + "edu" +
         File.separator + "ku" +
         File.separator + "brc" +
         File.separator + "specify" +
         File.separator + "datamodel"+
         File.separator + "hbm"+(fileName != null ? (File.separator + fileName) : "");
     } 
     
     public static String getUiHbmDirPath()
     {
      String fileName = "PickList.hbm.xml";
         String path = new File(".").getAbsolutePath();
         if (path.endsWith("."))
         {
             path = path.substring(0, path.length() - 2);
         }
         return path + File.separator +  
         File.separator + "src" +
         File.separator + "edu" +
         File.separator + "ku" +
         File.separator + "brc" +
         File.separator + "ui" +
         File.separator + "db"+(fileName != null ? (File.separator + fileName) : "");
     } 
          
     /**
      * Returns full path to file in datamodel File
      * @return the path to the file
      */
      public static String getDatamodelFilePath()
      {
       return XMLHelper.getConfigDirPath("specify_datamodel.xml");
      }      
      
      /**
       * Returns full path to file in tableId listing file
       * @return the path to the file
       */
       public static String getTableIdFilePath()
       {
        return  XMLHelper.getConfigDirPath("specify_tableid_listing.xml");
       }       
 /**
  * @param args
  */
 public static void main(String[] args)
 {
  // TODO Auto-generated method stub
 
 }
 
}
 

