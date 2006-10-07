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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.betwixt.XMLIntrospector;
import org.apache.commons.betwixt.io.BeanWriter;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

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
	 * Given and XML node, returns a Field object by grabbing the appropriate
	 * attribute values.
	 * 
	 * @param element the XML node
	 * @return Field object
	 */
	private Field createField(final Element element)
	{
		return new Field(element.attributeValue("name"), 
                element.attributeValue("type"), 
                element.attributeValue("column"), 
                element.attributeValue("length"));
	}
    
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
	 * Given and XML node, returns a Id object by grabbing the appropriate
	 * attribute values.
	 * 
	 * @param element the XML node
	 * @return Field object
	 */
	private Id createId(final Element element)
	{
		return new Id(element.attributeValue("name"), 
                        element.attributeValue("type"), 
                        element.attributeValue("column"), 
                        element.attributeValue("length"));
	}

	/**
	 * Given and XML node, returns a Table object by grabbing the appropriate
	 * attribute values.
	 * 
	 * @param element the XML node
	 * @return Table object
	 */
	private Table createTable(final Element element)
	{
        // get Class Name (or name) from HBM file
		String className = element.attributeValue("name");
		log.info("Processing: " + className);
        
        // Get Meta Data for HBM
		TableMetaData tableMetaData = tblMetaDataHash.get(className);
		if (tableMetaData == null)
        {
            // Throw exception if there is an HBM we don't have meta data for
			log.error("Could not retrieve TableMetaData from tblMetaDataHashtable for table: " + className);
            throw new RuntimeException("Could not retrieve TableMetaData from tblMetaDataHashtable for table: " + className);
        }
        
		return new Table(className, 
                         element.attributeValue("table"), 
                         element.attributeValue("lazy"), 
                         tableMetaData.getId(), 
                         tableMetaData.getDisplay());

	}

	/**
	 * Given and XML node, returns the value associated with the "class"
	 * attribute.
	 * 
	 * @param element  The XML node
	 * @return the class name
	 */
	private String getRelatedClassName(final Element element)
	{
		if (element != null)
		{
			return element.attributeValue("class");
		}
		return null;
	}

	/**
	 * Given and XML node, returns the value associated with the "name"
	 * attribute.
	 * 
	 * @param element The XML node
	 * @return the name
	 */
	private String getName(final Element element)
	{
		if (element != null)
		{
			return element.attributeValue("name");
		}
		return null;
	}

	/**
	 * Returns the last String token from a string that has the package name
	 * pre-appended to the class name i.e. edu.ku.brc.specify.datamodel.Locality -> Locality.
	 * 
	 * @param element The node from the XML document that containst the fully qualified name
	 * @return returns the simple class name
	 */
	private String getRelatedClassShortName(final Element element)
	{
		if (element != null)
		{
			String name = element.attribute("class").getValue();
			int inx = name.lastIndexOf('.');
			if (inx > 0)
			{
				return name.substring(inx + 1);
			}
		}
		return null;
	}

	/**
	 * Reads in hbm files and generates datamodel tree.
	 * @return List datamodel tree
	 */
	public List<Table> generateDatamodelTree(final List<Table> tableList, final String hbmPath)
	{
		try
		{
			log.debug("Preparing to read in hbm files from  path: " + hbmPath);
			File dir = new File(hbmPath);

			String path = dir.getAbsolutePath();
			dir = new File(path.substring(0, path.lastIndexOf(File.separator)));

			// This filter only returns directories
			FileFilter fileFilter = new FileFilter()
			{
				public boolean accept(File file)
				{
					return file.toString().indexOf(".hbm.xml") != -1;
				}
			};
			File[] files = dir.listFiles(fileFilter);
			int count = 0;
			for (File file : files)
			{
				log.debug("Reading    " + file.getAbsolutePath());
				FileInputStream fileInputStream = new FileInputStream(file);
				SAXReader reader = new SAXReader();
				reader.setValidation(false);
				EntityResolver resolver = new EntityResolver()
				{
					public InputSource resolveEntity(String publicId, String systemId)
					{
						if (publicId.equals("-//Hibernate/Hibernate Mapping DTD 3.0//EN"))
						{
							File filer = new File("hibernate-mapping-3.0.dtd");
							try
							{
								FileInputStream ino = new FileInputStream(filer);// getClass().getResourceAsStream();
								return new InputSource(ino);
                                
							} catch (Exception eer)
							{
								log.error("Trying to load Hibernate DTD from local file system, File not found");
							}

						}
						return null;
					}
				};
				reader.setEntityResolver(resolver);
				org.dom4j.Document doc = reader.read(fileInputStream);
				if (doc != null)
				{
					count++;
					log.debug("Processing " + count + " of " + files.length + "  " + file.getAbsolutePath());

					Element root = doc.getRootElement();
					if (root == null)
						log.error("Could not get root of document");

					Element classNode = (Element) root.selectSingleNode("class");
					if (classNode == null)
						log.error("Could not get class node of document");
					else
					{
						Table table = createTable(classNode);
						tableList.add(table);

						// iterate through child elements of propery ndoe
						for (Iterator i = classNode.elementIterator("property"); i.hasNext();)
						{
							Element element = (Element) i.next();
							table.addField(createField(element));
						}

						// iterate through child elements of id node
						for (Iterator i = classNode.elementIterator("id"); i.hasNext();)
						{
							Element element = (Element) i.next();
							table.addId(createId(element));
						}
						// iterate through child elements of set node
						for (Iterator i = classNode.elementIterator("set"); i.hasNext();)
						{
							Relationship rel = null;
							Element setSubNode = (Element) i.next();

							rel = processSetRelationship("one-to-many", setSubNode);
							if (rel != null)
								table.addRelationship(rel);

							rel = processSetRelationship("many-to-many", setSubNode);
							if (rel != null)
								table.addRelationship(rel);
						}

						// iterate through child elements of root
						for (Iterator i = classNode.elementIterator("many-to-one"); i.hasNext();)
						{
							Element element = (Element) i.next();
							String relClassName = getRelatedClassName(element);
							String relationshipName = getName(element);
							String columnName = getColumnName(element);
							table.addRelationship(new Relationship("many-to-one", relClassName, columnName,
									relationshipName));
						}

						// iterate through child elements of root
						for (Iterator i = classNode.elementIterator("one-to-one"); i.hasNext();)
						{
							Element element = (Element) i.next();
							String relClassName = getRelatedClassName(element);
							String relationshipName = getName(element);
							table.addRelationship(new Relationship("one-to-one", relClassName, "", relationshipName));
						}
					}
				}
				fileInputStream.close();
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
	 * Creates a Relationship Object.
	 * 
	 * @param type The relationship type i.e. one-to-many.
	 * @param subNode Node from XML document (hbm file) that contains the relationship information
	 * @return relationship object
	 */
	public Relationship processSetRelationship(String type, Element subNode)
	{
		String relationshipType = "";
		String relShortName     = "";
		String relClassName     = "";
		String columnName       = "";
		String relationshipName = subNode.attributeValue("name");

		for (Iterator i2 = subNode.elementIterator("key"); i2.hasNext();)
		{
			Element element = (Element) i2.next();
			columnName = getColumnName(element);
		}

		for (Iterator i2 = subNode.elementIterator(type); i2.hasNext();)
		{
			Element element = (Element) i2.next();
			relClassName = getRelatedClassName(element);
			relShortName = getRelatedClassShortName(element);
			if (relShortName != null)
			{
				relationshipType = type;
			}
			return new Relationship(relationshipType, relClassName, columnName, relationshipName);
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
					Element element    = (Element)i.next();
					String tablename   = element.attributeValue("name");
					String defaultView = element.attributeValue("view");
					String id          = element.attributeValue("id");
                    
					log.debug("Creating TableMetaData and putting in tblMetaDataHashtable for name: " + tablename + " id: " + id + " defaultview: " + defaultView);
                    
 					tblMetaDataHash.put(tablename, new TableMetaData(id, defaultView, createDisplay(element)));
                    
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
		List<Table>        tableList              = new ArrayList<Table>(100);
		DatamodelGenerator datamodelWriter        = new DatamodelGenerator();
		String             tableIdListingFilePath = DatamodelHelper.getTableIdFilePath();
        
		if (datamodelWriter.readTableMetadataFromFile(tableIdListingFilePath))
		{
			String hbmPath = DatamodelHelper.getHbmDirPath();
			tableList      = datamodelWriter.generateDatamodelTree(tableList, hbmPath);
            
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
	}

}
