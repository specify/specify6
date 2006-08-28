package edu.ku.brc.specify.tools.datamodelgenerator;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.commons.betwixt.XMLIntrospector;
import org.apache.commons.betwixt.io.BeanWriter;
import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import edu.ku.brc.helpers.DatamodelHelper;

/*
 * @code_status Alpha
 **
 * @author megkumin
 *
 */
public class DatamodelGenerator
{
	private static final Logger log = Logger.getLogger(DatamodelGenerator.class);
	
	//private String datamodelFilename = XMLHelper.getConfigDirPath("specify_datamodel.xml");
	//private String tableIdFilename = XMLHelper.getConfigDirPath("specify_tableid_listing.xml");

	Hashtable<String, TableMetaData> hash = new Hashtable<String, TableMetaData>();

	/**
	 * Given and XML node, returns a Field object by grabbing the appropriate
	 * attribute values
	 * 
	 * @param aElementnthe
	 *            XML node
	 * @return Field object
	 */
	private Field createField(Element aElement)
	{
		return new Field(aElement.attributeValue("name"), aElement.attributeValue("type"), aElement
				.attributeValue("column"), aElement.attributeValue("length"));
	}

	/**
	 * Given and XML node, returns a Id object by grabbing the appropriate
	 * attribute values
	 * 
	 * @param aElement
	 *            the XML node
	 * @return Field object
	 */
	private Id createId(Element aElement)
	{
		return new Id(aElement.attributeValue("name"), aElement.attributeValue("type"), aElement
				.attributeValue("column"), aElement.attributeValue("length"));
	}

	/**
	 * Given and XML node, returns a Table object by grabbing the appropriate
	 * attribute values
	 * 
	 * @param aElement
	 *            the XML node
	 * @return Table object
	 */
	private Table createTable(Element aElement)
	{
		//log.info("createTable");
		TableMetaData tableData = (TableMetaData) hash.get(aElement.attributeValue("name"));
		String id = tableData.getId();
		String view = tableData.getDefaultView();
		return new Table(aElement.attributeValue("name"), aElement.attributeValue("table"),
				aElement.attributeValue("lazy"),id, view);

	}

	/**
	 * Given and XML node, returns the value associated with the "class"
	 * attribute
	 * 
	 * @param element
	 *            The XML node
	 * @return the class name
	 */
	private String getRelatedClassName(Element element)
	{
		//log.info("getRelatedClassName - " +element.getName());//+ element.toString());
		if (element != null)
		{
			return element.attributeValue("class");
		}
		return null;
	}

	/**
	 * Given and XML node, returns the value associated with the "name"
	 * attribute
	 * 
	 * @param element
	 *            The XML node
	 * @return the name
	 */
	private String getName(Element element)
	{
		//log.info("getName - " + element.getName());
		if (element != null)
		{
			return element.attributeValue("name");
		}
		return null;
	}

	/**
	 * Returns the last String token from a string that has the package name
	 * pre-appended to the class name i.e. edu.ku.brc.specify.datamodel.Locality ->
	 * Locality
	 * 
	 * @param element
	 *            The node from the XML document that containst the fully
	 *            qualified name
	 * @return returns the simple class name
	 */
	private String getRelatedClassShortName(Element element)
	{
		//log.info("getRelatedTable");
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
	 * Reads in hbm files and generates datamodel tree
	 * @return
	 * java.util.List 
	 * 			datamodel tree
	 */
	public java.util.List<Table> generateDatamodelTree()
	{
		
		java.util.List<Table> tableList = new ArrayList<Table>();
		
		try
		{
			//String relPathOfHbmFiles = "../../datamodel/hbm/";
			String hbmPath = DatamodelHelper.getHbmDirPath();
			log.debug("Preparing to read in hbm files from  path: " + hbmPath);
			//System.out.println("looking for " + hbmPath);
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

				org.dom4j.Document doc = reader.read(fileInputStream);
				if (doc != null)
				{
					count++;
					log.debug("Processing " + count + " of " + files.length + "  "
							+ file.getAbsolutePath());

					Element root = doc.getRootElement();
					if(root == null)log.error("Could not get root of document");
					
					Element classNode = (Element) root.selectSingleNode("class");
					if(classNode == null)log.error("Could not get class node of document");
					else
					{
						Table table = createTable((Element) classNode);
						tableList.add(table);

						// iterate through child elements of propery ndoe
						for (Iterator i = classNode.elementIterator("property"); i.hasNext();)
						{
							Element element = (Element) i.next();
							table.addField(createField((Element) element));
						}

						// iterate through child elements of id node
						for (Iterator i = classNode.elementIterator("id"); i.hasNext();)
						{
							Element element = (Element) i.next();
							table.addId(createId((Element) element));
						}
						// iterate through child elements of set node
						for (Iterator i = classNode.elementIterator("set"); i.hasNext();)
						{
							Relationship rel = null;
							Element setSubNode = (Element) i.next();

							rel = processSetRelationship("one-to-many", setSubNode);
							if (rel != null) table.addRelationship(rel);

							rel = processSetRelationship("many-to-many", setSubNode);
							if (rel != null) table.addRelationship(rel);
						}

						// iterate through child elements of root
						for (Iterator i = classNode.elementIterator("many-to-one"); i.hasNext();)
						{
							Element element = (Element) i.next();
							String relClassName = getRelatedClassName(element);
							String relationshipName = getName(element);
							String columnName = getColumnName(element);
							table.addRelationship(new Relationship("many-to-one", relClassName,
									columnName, relationshipName));
						}

						// iterate through child elements of root
						for (Iterator i = classNode.elementIterator("one-to-one"); i.hasNext();)
						{
							Element element = (Element) i.next();
							String relClassName = getRelatedClassName(element);
							String relationshipName = getName(element);
							table.addRelationship(new Relationship("one-to-one", relClassName, "",
									relationshipName));
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
	 * Creates a Relationship Object
	 * 
	 * @param type
	 *            The relationship type i.e. one-to-many
	 * @param subNode
	 *            Node from XML document (hbm file) that contains the
	 *            relationship information
	 * @return relationship object
	 */
	public Relationship processSetRelationship(String type, Element subNode)
	{
		String relationshipType = "";
		String relShortName = "";
		String relClassName = "";
		String columnName = "";
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
	 * @param element
	 * @return the name of the column
	 */
	public String getColumnName(Element element)
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
	 * @param aClassesList
	 */
	public void writeTree(java.util.List aClassesList)
	{
		log.info("writing data model tree to file: " + DatamodelHelper.getDatamodelFilePath());
		try
		{
			
			//Element root = XMLHelper.readFileToDOM4J(new FileInputStream(XMLHelper.getConfigDirPath(datamodelOutputFileName)));
			File file = new File(DatamodelHelper.getDatamodelFilePath());
			FileWriter fw = new FileWriter(file);
			fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			//fw.
			//using betwixt for writing out datamodel file.  associated .betwixt files allow you to map and define 
			//output format of attributes in xml file.
			BeanWriter beanWriter = new BeanWriter(fw);
			//beanWriter.
			XMLIntrospector introspector = beanWriter.getXMLIntrospector();
			introspector.getConfiguration().setWrapCollectionsInElement(false);
			beanWriter.getBindingConfiguration().setMapIDs(false);
			beanWriter.setWriteEmptyElements(false);
			beanWriter.enablePrettyPrint();
			beanWriter.write("database", aClassesList);
			fw.close();
		} catch (Exception ex)
		{
			log.error("error writing writeTree", ex);
		}
	}



	/**
	 * Reads in file that provides listing of tables with their respective Id's and default views
	 * @return
	 * boolean true if reading of tableId file was successful.
	 */
	private boolean readTableMetadataFromFile()
	{
		log.info("Preparing to read in Table and TableID listing from file: " + DatamodelHelper.getTableIdPath() );
		try
		{
			//File file = new File(XMLHelper.getConfigDirPath(tableIdFilename));
			File tableIdFile = new File(DatamodelHelper.getTableIdPath());
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
					Element element = (Element) i.next();
					String tablename = element.attributeValue("name");
					String defaultView = element.attributeValue("view");
					String id = element.attributeValue("id");
					hash.put(tablename, new TableMetaData(id, defaultView));
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
		DatamodelGenerator datamodelWriter = new DatamodelGenerator();
		if (datamodelWriter.readTableMetadataFromFile())
		{
			java.util.List<Table> tableList = datamodelWriter.generateDatamodelTree();
			datamodelWriter.writeTree(tableList);
		} else
		{
			log.error("Could not find table/ID listing file for input ");
		}
	}

}
