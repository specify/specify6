package edu.ku.brc.specify.tools;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.commons.betwixt.XMLIntrospector;
import org.apache.commons.betwixt.io.BeanWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

//import edu.ku.brc.specify.dbsupport.DBTableIdMgr.TableInfo;
//import edu.ku.brc.specify.dbsupport.DBTableIdMgr.TableInfo;
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
	int classCounter = 10;
	Hashtable<String, Integer> hash = new Hashtable<String, Integer>();
	
	public void populateTableIds() {

		hash.put("edu.ku.brc.specify.datamodel.Accession", 7);
		hash.put("edu.ku.brc.specify.datamodel.AccessionAgents", 12);
		hash.put("edu.ku.brc.specify.datamodel.AccessionAuthorizations", 13);
		hash.put("edu.ku.brc.specify.datamodel.Address", 8);
		hash.put("edu.ku.brc.specify.datamodel.Agent", 5);
		hash.put("edu.ku.brc.specify.datamodel.AttributeDef", 16);
		hash.put("edu.ku.brc.specify.datamodel.Authors", 17);
		hash.put("edu.ku.brc.specify.datamodel.Borrow", 18);
		hash.put("edu.ku.brc.specify.datamodel.BorrowAgents", 19);
		hash.put("edu.ku.brc.specify.datamodel.BorrowMaterial", 20);
		hash.put("edu.ku.brc.specify.datamodel.BorrowReturnMaterial", 21);
		hash.put("edu.ku.brc.specify.datamodel.BorrowShipments", 22);
		hash.put("edu.ku.brc.specify.datamodel.CatalogSeries", 23);
		hash.put("edu.ku.brc.specify.datamodel.CollectingEvent", 10);
		hash.put("edu.ku.brc.specify.datamodel.CollectingEventAttr", 25);
		hash.put("edu.ku.brc.specify.datamodel.CollectionObjDef", 26);
		hash.put("edu.ku.brc.specify.datamodel.CollectionObject", 1);
		hash.put("edu.ku.brc.specify.datamodel.CollectionObjectAttr", 28);
		hash.put("edu.ku.brc.specify.datamodel.CollectionObjectCitation", 29);
		hash.put("edu.ku.brc.specify.datamodel.Collectors", 30);
		hash.put("edu.ku.brc.specify.datamodel.Container", 31);
		hash.put("edu.ku.brc.specify.datamodel.ContainerItem", 32);
		hash.put("edu.ku.brc.specify.datamodel.DataType", 33);
		hash.put("edu.ku.brc.specify.datamodel.Deaccession", 34);
		hash.put("edu.ku.brc.specify.datamodel.DeaccessionAgents", 35);
		hash.put("edu.ku.brc.specify.datamodel.DeaccessionCollectionObject", 36);
		hash.put("edu.ku.brc.specify.datamodel.Determination", 9);
		hash.put("edu.ku.brc.specify.datamodel.DeterminationCitation", 38);
		hash.put("edu.ku.brc.specify.datamodel.ExchangeIn", 39);
		hash.put("edu.ku.brc.specify.datamodel.ExchangeOut", 40);
		hash.put("edu.ku.brc.specify.datamodel.ExternalResource", 41);
		hash.put("edu.ku.brc.specify.datamodel.ExternalResourceAttr", 42);
		hash.put("edu.ku.brc.specify.datamodel.Geography", 3);
		hash.put("edu.ku.brc.specify.datamodel.GeographyTreeDef", 44);
		hash.put("edu.ku.brc.specify.datamodel.GeographyTreeDefItem", 45);
		hash.put("edu.ku.brc.specify.datamodel.GeologicTimePeriod", 46);
		hash.put("edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDef", 47);
		hash.put("edu.ku.brc.specify.datamodel.GeologicTimePeriodTreeDefItem", 48);
		hash.put("edu.ku.brc.specify.datamodel.GroupPersons", 49);
		hash.put("edu.ku.brc.specify.datamodel.InfoRequest", 50);
		hash.put("edu.ku.brc.specify.datamodel.Journal", 51);
		hash.put("edu.ku.brc.specify.datamodel.Loan", 52);
		hash.put("edu.ku.brc.specify.datamodel.LoanAgents", 53);
		hash.put("edu.ku.brc.specify.datamodel.LoanPhysicalObject", 54);
		hash.put("edu.ku.brc.specify.datamodel.LoanReturnPhysicalObject", 55);
		hash.put("edu.ku.brc.specify.datamodel.Locality", 2);
		hash.put("edu.ku.brc.specify.datamodel.LocalityCitation", 57);
		hash.put("edu.ku.brc.specify.datamodel.Location", 58);
		hash.put("edu.ku.brc.specify.datamodel.LocationTreeDef", 59);
		hash.put("edu.ku.brc.specify.datamodel.LocationTreeDefItem", 60);
		hash.put("edu.ku.brc.specify.datamodel.OtherIdentifier", 61);
		hash.put("edu.ku.brc.specify.datamodel.Permit", 6);
		hash.put("edu.ku.brc.specify.datamodel.Preparation", 63);
		hash.put("edu.ku.brc.specify.datamodel.PreparationAttr", 64);
		hash.put("edu.ku.brc.specify.datamodel.PrepType", 65);
		hash.put("edu.ku.brc.specify.datamodel.Project", 66);
		hash.put("edu.ku.brc.specify.datamodel.ProjectCollectionObject", 67);
		hash.put("edu.ku.brc.specify.datamodel.RecordSet", 68);
		hash.put("edu.ku.brc.specify.datamodel.ReferenceWork", 69);
		hash.put("edu.ku.brc.specify.datamodel.RepositoryAgreement", 70);
		hash.put("edu.ku.brc.specify.datamodel.Shipment", 71);
		hash.put("edu.ku.brc.specify.datamodel.SpecifyUser", 72);
		hash.put("edu.ku.brc.specify.datamodel.Stratigraphy", 73);
		hash.put("edu.ku.brc.specify.datamodel.Taxon", 4);
		hash.put("edu.ku.brc.specify.datamodel.TaxonCitation", 75);
		hash.put("edu.ku.brc.specify.datamodel.TaxonTreeDef", 76);
		hash.put("edu.ku.brc.specify.datamodel.TaxonTreeDefItem", 77);
		hash.put("edu.ku.brc.specify.datamodel.UserGroup", 78);
		hash.put("edu.ku.brc.specify.datamodel.Workbench", 79);
		hash.put("edu.ku.brc.specify.datamodel.WorkbenchDataItem", 80);
		hash.put("edu.ku.brc.specify.datamodel.WorkbenchTemplate", 81);
		hash.put("edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem", 82);
        
        //hash.put("edu.ku.brc.specify.datamodel.InfoRequest",80);
        hash.put("edu.ku.brc.specify.ui.db.PickList",500);
		
        //Integer i = hash.get( "edu.ku.brc.specify.datamodel.CollectionsdObject");
        //.out.println(i.toString());

	}	
	
	/**
	 * Given and XML node, returns a Field object, by grabbing the appropriate attribute values
	 * @param aElement the XML node
	 * @return Field object
	 */
	private Field createField(Element aElement, String isPrimaryKey) {
		//aElement.addAttribute()
		return new Field(aElement.attributeValue("name"), 
				aElement.attributeValue("type"), 
				aElement.attributeValue("column"),
				aElement.attributeValue("length"),
				isPrimaryKey);
	}

	/**
	 * Given and XML node, returns a Table object, by grabbing the appropriate attribute values
	 * @param aElement the XML node
	 * @return Table object
	 */
	private Table createClass(Element aElement) {
		log.info("createClass");
		//classCounter++;
		return new Table(aElement.attributeValue("name"), 
				aElement.attributeValue("table"), 
				aElement.attributeValue("lazy"), hash);//, classCounter);
		
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
							cls.addField(createField((Element) element, "false"));
						}

						// iterate through child elements of id node
						for (Iterator i = classNode.elementIterator("id"); i
								.hasNext();) {
							Element element = (Element) i.next();
							cls.addField(createField((Element) element, "true"));
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
	
	private void readInClassnameAndIds() {
		
		
		log.info("readInClassnameAndIds");
		try {
			String filename = "../../../../../../SpecifyDataModel.xml";
			File file = new File(filename);
			FileInputStream fileInputStream = new FileInputStream(file);
			SAXReader reader = new SAXReader();
			reader.setValidation(false);
			
			org.dom4j.Document doc = reader.read(fileInputStream);    			
			Element root = doc.getRootElement();
			Element dbNode = (Element) root.selectSingleNode("Database");
			
			if (dbNode != null) {
				
				
				//(int tableId, String className, String tableName, String primaryKeyName, String defaultFormName)
				// iterate through child elements of propery ndoe 
				for (Iterator i = dbNode.elementIterator("table"); i.hasNext();) {
					Element element = (Element) i.next();
					String className = element.attributeValue("classname");
					String tableName = element.attributeValue("table");
					String tableId = element.attributeValue("tableid");
					////cls.addField(createField((Element) element));
					for (Iterator i2 = dbNode.elementIterator("field"); i2.hasNext();) {
						System.out.println("sldkfjlsdjfljsdlfjlsdjfljsdljf#######");
						//Element element = (Element) i2.next();
						//String className = element.attributeValue("classname");
						//String tableName = element.attributeValue("table");
						//String tableId = element.attributeValue("tableid");
						////cls.addField(createField((Element) element));
					}   					
				}    				
			}
			
			writeTree(classesList);
			fileInputStream.close();
			//}
			
		} catch (Exception ex) {
			ex.printStackTrace();
			log.fatal(ex);
		}
		
	}	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//classesListsss.
		DatamodelWriter datamodelWriter = new DatamodelWriter();
		datamodelWriter.populateTableIds();
		datamodelWriter.makeTree();
		//datamodelWriter.readInClassnameAndIds();
	}

}
