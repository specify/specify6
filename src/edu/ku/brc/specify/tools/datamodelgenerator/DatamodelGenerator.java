/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package edu.ku.brc.specify.tools.datamodelgenerator;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.betwixt.XMLIntrospector;
import org.apache.commons.betwixt.io.BeanWriter;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.util.DatamodelHelper;

/**
 * This generates the specify datamodel file
 * 
 * @code_status Alpha
 * 
 * @author rods
 * 
 */
public class DatamodelGenerator
{
    private static final Logger log = Logger.getLogger(DatamodelGenerator.class);

    Hashtable<String, TableMetaData> tblMetaDataHash = new Hashtable<String, TableMetaData>();

    /**
     * Looks for a child node "display" and creates the appropriate object or returns null.
     * @param element the "table".
     * @return null or a Display object
     */
    private Display createDisplay(final Element element)
    {
        if (element != null)
        {
            Element fdElement = (Element)element.selectSingleNode("display");
            if (fdElement != null)
            {
                return new Display(fdElement.attributeValue("objtitle"), 
                                        fdElement.attributeValue("view"), 
                                        fdElement.attributeValue("dataobjformatter"), 
                                        fdElement.attributeValue("uiformatter"), 
                                        fdElement.attributeValue("searchdlg"), 
                                        fdElement.attributeValue("newobjdlg"));
            }
        }
        return null;
    }

    /**
     * Given and XML node, returns a Table object by grabbing the appropriate
     * attribute values.
     * 
     * @param element the XML node
     * @return Table object
     */
    private Table createTable(final String className, final String tableName)
    {
        // get Class Name (or name) from HBM file
        log.info("Processing: " + className);
        
        // Get Meta Data for HBM
        TableMetaData tableMetaData = tblMetaDataHash.get(className);
        if (tableMetaData == null)
        {
            // Throw exception if there is an HBM we don't have meta data for
            log.error("Could not retrieve TableMetaData from tblMetaDataHashtable for table: " + className);
            throw new RuntimeException("Could not retrieve TableMetaData from tblMetaDataHashtable for table: " + className 
                    + " check to see if table is listed in the file: " + DatamodelHelper.getTableIdFilePath());
        }
        
        return new Table(className, tableName, null, tableMetaData.getId(), tableMetaData.getDisplay(), tableMetaData.isForQuery(), tableMetaData.getBusinessRule());

    }

    /**
     * @param method
     * @return
     */
    protected String getReturnType(final Method method)
    {
        Class classObj = method.getReturnType();
        // If there is a better way, PLEASE help me!
        if (classObj == Set.class)
        {
            ParameterizedType type = (ParameterizedType)method.getGenericReturnType();
            for (Type t : type.getActualTypeArguments())
            {
                String cls = t.toString();
                return cls.substring(6, cls.length());
            }
        }
        return classObj.getName();
    }
    
    /**
     * @param method
     * @return
     */
    protected String getNameFromMethod(final Method method)
    {
        String name = method.getName();
        name = name.substring(3, 4).toLowerCase() + name.substring(4, name.length());

        return name;
    }
    
    /**
     * @param method
     * @param type
     * @param joinCol
     * @return
     */
    public Relationship createRelationsip(final Method method, 
                                          final String type,
                                          final javax.persistence.JoinColumn joinCol)
    {
        return new Relationship(type, getReturnType(method), joinCol != null ? joinCol.name() : "", getNameFromMethod(method));
    }


    /**
     * @param method
     * @param col
     * @return
     */
    public Id createId(final Method method, 
                       final javax.persistence.Column col)
    {
        return new Id(getNameFromMethod(method), getReturnType(method), col.name(), "");
    }

    /**
     * @param method
     * @param col
     * @return
     */
    public Field createField(final Method method, 
                             final javax.persistence.Column col,
                             final boolean isLob)
    {
        String retType;
        String len;
        
        if (isLob)
        {
            retType = "text";
            len     = "";
        } else
        {
            retType = isLob ? "text" : getReturnType(method);
            len = retType.equals("java.lang.String") ? Integer.toString(col.length()) : (col.length() != 255 ? Integer.toString(col.length()) : "");
        }
        return new Field(getNameFromMethod(method), retType, col.name(), len);
    }

    /**
     * Reads in hbm files and generates datamodel tree.
     * @return List datamodel tree
     */
    @SuppressWarnings("unchecked")
    public List<Table> generateDatamodelTree(final List<Table> tableList, final String dataModelPath)
    {
        try
        {
            log.debug("Preparing to read in DataModel Classes files from  path: " + dataModelPath);
            File dir = new File(dataModelPath);

            String path = dir.getAbsolutePath();
            log.info(path);
            //dir = new File(path.substring(0, path.lastIndexOf(File.separator)));

            // This filter only returns directories
            FileFilter fileFilter = new FileFilter()
            {
                public boolean accept(File file)
                {
                    return file.toString().indexOf(".java") != -1;
                }
            };
            
            String PACKAGE = "package ";
            String CLASS   = "public class ";
            
            File[] files = dir.listFiles(fileFilter);
            int count = 0;
            for (File file : files)
            {
                log.debug("Reading    " + file.getAbsolutePath());
                List<?> lines = FileUtils.readLines(file);
                count++;
                log.debug("Processing " + count + " of " + lines.size() + "  " + file.getAbsolutePath());

                String  packageName = null;
                String  tableName   = null;
                Table   table       = null; 
                String  className   = null;
                
                for (Object lineObj : lines)
                {
                    String line = ((String)lineObj).trim();
                    //System.out.println(line);
                    if (line.startsWith(PACKAGE))
                    {
                        packageName = line.substring(PACKAGE.length(), line.length()-1);
                    }
                    
                    if (line.startsWith(CLASS))
                    {
                        int eInx = line.substring(CLASS.length()).indexOf(' ') + CLASS.length();
                        if (eInx > -1)
                        {
                            className = line.substring(CLASS.length(), eInx);
                        }
                        break;
                    }
                }
                
                if (className != null)
                {
                    try
                    {
                        
                        Class classObj = Class.forName(packageName + "." + className);
                        
                        if (classObj.isAnnotationPresent(javax.persistence.Table.class))
                        {
                            javax.persistence.Table tableAnno = (javax.persistence.Table)classObj.getAnnotation(javax.persistence.Table.class);
                            tableName = tableAnno.name();
                            
                            table = createTable(packageName + "." + className, tableName);
                            tableList.add(table);
                        }
                        
                        if (table != null)
                        {
                            boolean isLob = false;
                            for (Method method : classObj.getMethods())
                            {
                                if (method.isAnnotationPresent(javax.persistence.Lob.class))
                                {
                                    isLob = true;
                                }

                                if (method.isAnnotationPresent(javax.persistence.Column.class))
                                {
                                    if (method.isAnnotationPresent(javax.persistence.Id.class))
                                    {
                                        table.addId(createId(method, (javax.persistence.Column)method.getAnnotation(javax.persistence.Column.class)));
                                    } else
                                    {
                                        table.addField(createField(method, (javax.persistence.Column)method.getAnnotation(javax.persistence.Column.class), isLob));
                                    }
                                    
                                } else if (method.isAnnotationPresent(javax.persistence.ManyToOne.class))
                                {
                                    javax.persistence.JoinColumn join = method.isAnnotationPresent(javax.persistence.JoinColumn.class) ? (javax.persistence.JoinColumn)method.getAnnotation(javax.persistence.JoinColumn.class) : null;
                                    table.addRelationship(createRelationsip(method, "many-to-one", join));
                                    
                                } else if (method.isAnnotationPresent(javax.persistence.ManyToMany.class))
                                {
                                    javax.persistence.JoinColumn join = method.isAnnotationPresent(javax.persistence.JoinColumn.class) ? (javax.persistence.JoinColumn)method.getAnnotation(javax.persistence.JoinColumn.class) : null;
                                    table.addRelationship(createRelationsip(method, "many-to-many", join));
                                    
                                } else if (method.isAnnotationPresent(javax.persistence.OneToMany.class))
                                {
                                    javax.persistence.JoinColumn join = method.isAnnotationPresent(javax.persistence.JoinColumn.class) ? (javax.persistence.JoinColumn)method.getAnnotation(javax.persistence.JoinColumn.class) : null;
                                    table.addRelationship(createRelationsip(method, "one-to-many", join));
                                    
                                } else if (method.isAnnotationPresent(javax.persistence.OneToOne.class))
                                {
                                    javax.persistence.JoinColumn join = method.isAnnotationPresent(javax.persistence.JoinColumn.class) ? (javax.persistence.JoinColumn)method.getAnnotation(javax.persistence.JoinColumn.class) : null;
                                    table.addRelationship(createRelationsip(method, "one-to-one", join));
                                    
                                }
                                isLob = false;
                            }
                        }
                                    
                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
            }
            return tableList;

        } catch (Exception ex)
        {
            ex.printStackTrace();
            log.fatal(ex);
        }
        return null;
    }


    /**
     * Gets column name.
     * @param element the element
     * @return the name of the column
     */
    public String getColumnName(final Element element)
    {
        String columnName = null;
        for (Iterator i2 = element.elementIterator("column"); i2.hasNext();)
        {
            Element element1 = (Element) i2.next();
            columnName = element1.attributeValue("name");
        }
        return columnName;
    }

    /**
     * Takes a list and prints out datamodel file using betwixt.
     * @param classesList the class list
     */
    public boolean writeTree(final List classesList)
    {

        try
        {
            if (classesList == null)
            {
                log.error("Datamodel information is null - datamodel file will not be written!!");
                return false;
            }
            log.info("writing data model tree to file: " + DatamodelHelper.getDatamodelFilePath());
            
            //Element root = XMLHelper.readFileToDOM4J(new FileInputStream(XMLHelper.getConfigDirPath(datamodelOutputFileName)));
            File file = new File(DatamodelHelper.getDatamodelFilePath());
            FileWriter fw = new FileWriter(file);
            fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            fw.write("<!-- \n");
            fw.write("    Do Not Edit this file!\n");
            fw.write("    Run DatamodelGenerator \n");
            Date date = new Date();
            fw.write("    Generated: "+date.toString()+"\n");
            fw.write("-->\n");
            
            //using betwixt for writing out datamodel file.  associated .betwixt files allow you to map and define 
            //output format of attributes in xml file.
            BeanWriter      beanWriter    = new BeanWriter(fw);
            XMLIntrospector introspector = beanWriter.getXMLIntrospector();
            
            introspector.getConfiguration().setWrapCollectionsInElement(false);
            
            beanWriter.getBindingConfiguration().setMapIDs(false);
            beanWriter.setWriteEmptyElements(false);
            beanWriter.enablePrettyPrint();
            beanWriter.write("database", classesList);
            
            fw.close();
            
            return true;
            
        } catch (Exception ex)
        {
            log.error("error writing writeTree", ex);
            return false;
        }
    }

    /**
     * Reads in file that provides listing of tables with their respective Id's and default views.
     * @return boolean true if reading of tableId file was successful.
     */
    private boolean readTableMetadataFromFile(final String tableIdListingFilePath)
    {
        log.info("Preparing to read in Table and TableID listing from file: " + tableIdListingFilePath);
        try
        {
            File tableIdFile = new File(tableIdListingFilePath);
            FileInputStream fileInputStream = new FileInputStream(tableIdFile);
            SAXReader reader = new SAXReader();
            reader.setValidation(false);
            org.dom4j.Document doc = reader.read(fileInputStream);
            Element root = doc.getRootElement();
            Element dbNode = (Element) root.selectSingleNode("database");
            if (dbNode != null)
            {
                for (Iterator i = dbNode.elementIterator("table"); i.hasNext();)
                {
                    Element element     = (Element)i.next();
                    String tablename    = element.attributeValue("name");
                    String defaultView  = element.attributeValue("view");
                    String id           = element.attributeValue("id");
                    boolean isQuery     = XMLHelper.getAttr(element, "query", false);
                    
                    String busRule      = "";
                    Element brElement = (Element)element.selectSingleNode("businessrule");
                    if (brElement != null)
                    {
                        busRule = brElement.getTextTrim();
                    }
                    log.debug("Creating TableMetaData and putting in tblMetaDataHashtable for name: " + tablename + " id: " + id + " defaultview: " + defaultView);
                    
                     tblMetaDataHash.put(tablename, new TableMetaData(id, defaultView, createDisplay(element), isQuery, busRule));
                    
                }
                
            } else
            {
                log.debug("Ill-formatted file for reading in Table and TableID listing.  Filename:"
                        + tableIdFile.getAbsolutePath());
            }
            fileInputStream.close();
            return true;
        } catch (Exception ex)
        {
            ex.printStackTrace();
            log.fatal(ex);
        }
        return false;

    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        System.out.println("Starting...");
        List<Table>        tableList              = new ArrayList<Table>(100);
        DatamodelGenerator datamodelWriter        = new DatamodelGenerator();
        String             tableIdListingFilePath = DatamodelHelper.getTableIdFilePath();
        
        if (datamodelWriter.readTableMetadataFromFile(tableIdListingFilePath))
        {
            String dmSrc = DatamodelHelper.getDataModelSrcDirPath();
            tableList    = datamodelWriter.generateDatamodelTree(tableList, dmSrc);
            
            // Sort all the elements by class name
            Collections.sort(tableList);

            boolean didWrite = datamodelWriter.writeTree(tableList);
            if (!didWrite)
            {
                log.error("Failed to write out datamodel document");
            }
        } else
        {
            log.error("Could not find table/ID listing file for input ");
        }
        log.info("Done.");
        System.out.println("Done.");
    }

}
