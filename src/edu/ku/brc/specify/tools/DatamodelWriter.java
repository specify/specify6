package edu.ku.brc.specify.tools;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.betwixt.XMLIntrospector;
import org.apache.commons.betwixt.io.BeanWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import edu.ku.brc.specify.tools.datamodelparser.Field;
import edu.ku.brc.specify.tools.datamodelparser.Relationship;
import edu.ku.brc.specify.tools.datamodelparser.Table;

/**
 * @author megkumin
 *
 */
public class DatamodelWriter {
	private static Log log = LogFactory.getLog(DatamodelWriter.class);
	private java.util.List<Table> classesList = new ArrayList<Table>();
	private String fileName = "SpecifyDataModel.xml";

	/**
	 * Given and XML node, returns a Field object, by grabbing the appropriate attribute values
	 * @param aElement the XML node
	 * @return Field object
	 */
	private Field createField(Element aElement) {
		//aElement.addAttribute()
		return new Field(aElement.attributeValue("name"), 
				aElement.attributeValue("type"), 
				aElement.attributeValue("column"),
				aElement.attributeValue("length"));
	}

	/**
	 * Given and XML node, returns a Table object, by grabbing the appropriate attribute values
	 * @param aElement the XML node
	 * @return Table object
	 */
	private Table createClass(Element aElement) {
		log.info("createClass");
		return new Table(aElement.attributeValue("name"), 
				aElement.attributeValue("table"), 
				aElement.attributeValue("lazy"));
	}

	/**
	 * Given and XML node, returns the value associated with the "class" attribute
	 * @param element The XML node
	 * @return the class name
	 */
	private String getRelatedClassName(Element element) {
		log.info("getRelationshipFromSetElement - " + element.toString());
		if (element != null) {
			return element.attributeValue("class");
		}
		return null;
	}

	/**
	 * Given and XML node, returns the value associated with the "name" attribute
	 * @param element The XML node
	 * @return the name
	 */
	private String getName(Element element) {
		log.info("getName - " + element.toString());
		if (element != null) {
			return element.attributeValue("name");
		}
		return null;
	}	
	/**
	 * Returns the last String token from a string that has the package name 
	 * pre-appended to the class name i.e. edu.ku.brc.specify.datamodel.Locality -> Locality
	 * 
	 * @param element The node from the XML document that containst the fully qualified name
	 * @return returns the simple class name
	 */
	private String getRelatedClassShortName(Element element) {
		log.info("getRelatedTable");
		if (element != null) {
			String name = element.attribute("class").getValue();
			int inx = name.lastIndexOf('.');
			if (inx > 0) {
				return name.substring(inx + 1);
			}
		}
		return null;
	}

	/**
	 * 
	 *
	 */
	public void makeTree() {
		log.info("makeTree");
		try {
			File dir = new File(DatamodelWriter.class.getResource(
					"../datamodel/hbm/Accession.hbm.xml").getFile());
			String path = dir.getAbsolutePath();
			dir = new File(path.substring(0, path.lastIndexOf(File.separator)));

			// This filter only returns directories
			FileFilter fileFilter = new FileFilter() {
				public boolean accept(File file) {
					return file.toString().indexOf(".hbm.xml") != -1;
				}
			};
			File[] files = dir.listFiles(fileFilter);
			int count = 0;
			for (File file : files) {
				log.info("Reading    " + file.getAbsolutePath());
				FileInputStream fileInputStream = new FileInputStream(file);
				SAXReader reader = new SAXReader();
				reader.setValidation(false);

				org.dom4j.Document doc = reader.read(fileInputStream);
				if (doc != null) {
					log.info("Processing " + count + " of " + files.length
							+ "  " + file.getAbsolutePath());

					Element root = doc.getRootElement();
					Element classNode = (Element) root
							.selectSingleNode("class");

					if (classNode != null) {
						Table cls = createClass((Element) classNode);
						classesList.add(cls);

						// iterate through child elements of propery ndoe 
						for (Iterator i = classNode.elementIterator("property"); i
								.hasNext();) {
							Element element = (Element) i.next();
							cls.addField(createField((Element) element));
						}

						// iterate through child elements of id node
						for (Iterator i = classNode.elementIterator("id"); i
								.hasNext();) {
							Element element = (Element) i.next();
							cls.addField(createField((Element) element));
						}
						// iterate through child elements of set node 
						for (Iterator i = classNode.elementIterator("set"); i
								.hasNext();) {
							Relationship rel = null;
							Element setSubNode = (Element) i.next();

							rel = processRelationship("one-to-many", setSubNode);
							if (rel != null) cls.addRelationship(rel);

							rel = processRelationship("many-to-one", setSubNode);
							if (rel != null) cls.addRelationship(rel);

							rel = processRelationship("one-to-one", setSubNode);
							if (rel != null)cls.addRelationship(rel);

							rel = processRelationship("many-to-many",setSubNode);
							if (rel != null)cls.addRelationship(rel);

						}

						// iterate through child elements of root
						for (Iterator i = classNode.elementIterator("many-to-one"); i.hasNext();) {
							Element element = (Element) i.next();							
							String relClassName = getRelatedClassName(element);
							String relShortName = getRelatedClassShortName(element);
							String relationshipName = getName(element);
							String columnName = getColumnName(element);
							cls.addRelationship(new Relationship("many-to-one",
									relClassName, columnName,relationshipName));
						}

						// iterate through child elements of root
						for (Iterator i = classNode.elementIterator("one-to-one"); i.hasNext();) {
							Element element = (Element) i.next();
							String relClassName = getRelatedClassName(element);
							String relShortName = getRelatedClassShortName(element);
							String relationshipName = getName(element);
							cls.addRelationship(new Relationship("one-to-one",
									 relClassName, "", relationshipName));
						}
					}

					count++;
					if (count > 1000) {
						break;
					}
					writeTree(classesList);

				}
				fileInputStream.close();
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			log.fatal(ex);
		}
	}

	/**
	 * Creates a Relationship Object
	 * @param type The relationship type i.e. one-to-many
	 * @param subNode Node from XML document (hbm file) that contains the relationship information 
	 * @return relationship object
	 */
	public Relationship processRelationship(String type, Element subNode) {
		String relationshipType = "";
		String relShortName = "";
		String relClassName = "";
		String columnName = "";
		String relationshipName = subNode.attributeValue("name");

		for (Iterator i2 = subNode.elementIterator("key"); i2.hasNext();) {
			Element element = (Element) i2.next();
			columnName = getColumnName(element);
		}

		for (Iterator i2 = subNode.elementIterator(type); i2.hasNext();) {
			Element element = (Element) i2.next();
			relClassName = getRelatedClassName(element);
			relShortName = getRelatedClassShortName(element);
			if (relShortName != null) {
				relationshipType = type;
			}
			return new Relationship(relationshipType,
					relClassName, columnName, relationshipName);
		}
		return null;
	}

	/**
	 * @param element
	 * @return the name of the column
	 */
	public String getColumnName(Element element) {
		String columnName = null;
		for (Iterator i2 = element.elementIterator("column"); i2.hasNext();) {
			Element element1 = (Element) i2.next();
			columnName = element1.attributeValue("name");
		}

		return columnName;
	}

	/**
	 * 
	 * @param aClassesList
	 */
	public void writeTree(java.util.List aClassesList) {
		log.info("writing data model tree to file: " + fileName);
		try {
			File file = new File(fileName);
			FileWriter fw = new FileWriter(file);
			fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			BeanWriter beanWriter = new BeanWriter(fw);
			XMLIntrospector introspector = beanWriter.getXMLIntrospector();
			introspector.getConfiguration().setWrapCollectionsInElement(false);
			beanWriter.getBindingConfiguration().setMapIDs(false);
			beanWriter.setWriteEmptyElements(false);
			beanWriter.enablePrettyPrint();
			beanWriter.write("Database", aClassesList);
			fw.close();
		} catch (Exception ex) {
			log.error("error writing writeTree", ex);
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DatamodelWriter DatamodelWriter = new DatamodelWriter();
		DatamodelWriter.makeTree();
	}

}
