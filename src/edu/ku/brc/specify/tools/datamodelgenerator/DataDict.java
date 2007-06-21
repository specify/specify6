/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tools.datamodelgenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.commons.betwixt.XMLIntrospector;
import org.apache.commons.betwixt.io.BeanWriter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.helpers.XMLHelper;
/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jun 20, 2007
 *
 */
public class DataDict
{
    protected Vector<TableDef> tables    = new Vector<TableDef>();
    protected Vector<TableDef> sp5Tables = new Vector<TableDef>();
    
    /**
     * @param node
     * @param level
     */
    protected void print(Element node, int level)
    {
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<level;i++)
        {
            sb.append("  ");
        }
        
        for (Object k : node.elements())
        {
            System.out.println(sb.toString()+((Element)k).getName());
            print((Element)k, level+1);
        }
    }
    
    /**
     * @param text
     * @return
     */
    protected String getText(final String text)
    {
        String desc = StringUtils.replace(StringUtils.remove(text, '\n'), "     ", " ");
        desc = StringUtils.replace(desc, "  ", " ");
        desc = StringUtils.replace(desc, "  ", " ");
        desc = StringEscapeUtils.escapeXml(desc);
        desc = StringUtils.replace(desc, "&amp;apos;", "'");
        desc = StringUtils.replace(desc, "&#160;", " ");
        desc = StringUtils.replace(desc, "&#160;", " ");
        
        
        if (StringUtils.isNotBlank(desc))
        {
            return desc;
        }
        
        return "";
    }
    
    /**
     * @param e
     * @return
     */
    protected String getName(final Element e)
    {
        Element node = (Element)e.selectSingleNode("a/strong/u");
        if (node == null)
        {
            node = (Element)e.selectSingleNode("a/strong");
        }
        //print(e, 0);
        return getText(node.getText());
    }
    
    /**
     * @param e
     * @return
     */
    protected String getCol1(final Element e)
    {
        Element node = (Element)e.selectSingleNode("strong");
        if (node == null)
        {
            node = (Element)e;
        }
        System.out.println("1 "+e.getName()+" ["+node.getText()+"]");
        return getText(node.getText());
    }
    
    /**
     * @param e
     * @param isRel
     * @return
     */
    @SuppressWarnings("unchecked")
    protected String getCol2(final Element e, final boolean isRel)
    {
        if (isRel)
        {
            StringBuilder sb = new StringBuilder();
            for (Element link : (List<Element>)e.selectNodes("a"))
            {
                if (sb.length() > 0) sb.append(" ");
                sb.append(getText(link.getText()));
            }
            return sb.toString();
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(e.getText());
        
        Element node = (Element)e.selectSingleNode("em");
        if (node != null)
        {
            sb.append(" ");
            sb.append(node.getText());
        }
        System.out.println(e.getName()+" ["+sb.toString()+"]");
        return getText(sb.toString());
    }
    
    /**
     * @param table
     */
    @SuppressWarnings("unchecked")
    public void processSpecify5Schema()
    {
        File file = new File("src/edu/ku/brc/specify/tools/datamodelgenerator/Specify5Tables.txt");
        try
        {
            TableDef         tableDef = null;
            Vector<FieldDef> fields   = null;
            
            boolean processingTable = false;
            
            for (String line : (List<String>)FileUtils.readLines(file))
            {
                if (processingTable)
                {
                    if (line.startsWith(") ON ["))
                    {
                        processingTable = false;
                    } else
                    {
                        String[] tokens = StringUtils.split(line, "[]");
                        //System.out.println(line);
                        String notNull = tokens[4];
                        
                        FieldDef fieldDef = new FieldDef(tokens[1], tokens[3], "", notNull != null && notNull.indexOf("NOT NULL") > -1 ? "true" : "false", "");
                        fields.add(fieldDef);
                        tokens = StringUtils.split(line, "()");
                        if (tokens.length > 1)
                        {
                            fieldDef.setLength(tokens[1]);
                        }
                    }
                    
                } else if (line.startsWith("CREATE TABLE"))
                {
                    String[] tokens = StringUtils.split(line, "[]");
                    tableDef = new TableDef(tokens[3], "");
                    sp5Tables.add(tableDef);
                    fields        = tableDef.getFields();
                    processingTable = true;
                }
            }
        }
        catch (IOException ex)
        {
            System.err.println(file.getAbsolutePath());
            ex.printStackTrace();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * @param table
     */
    @SuppressWarnings("unchecked")
    public void processTable(Element table)
    {
        // cheesy, but who cares for now
        String desc = StringUtils.replace(StringUtils.remove(table.selectSingleNode("caption/strong").getText(), '\n'), "     ", " ");
        desc = StringUtils.replace(desc, "  ", " ");
        desc = StringUtils.replace(desc, "  ", " ");
        
        String[] parts = StringUtils.split(desc, "'");
        
        TableDef         tableDef = new TableDef(desc, parts[1]);
        Vector<FieldDef> fields   = tableDef.getFields();
        Vector<IndexDef> indexes  = tableDef.getIndexes();
        Vector<RelationshipDef> relationships  = tableDef.getRelationships();
        
        tables.add(tableDef);
        
        for (Element tr : (List<Element>)table.selectNodes("tr"))
        {
            List<Element> tds = (List<Element>)tr.selectNodes("td");
            if (tds.size() == 5)
            {
                String type = getText(tds.get(1).getText());
                String[] tokens = StringUtils.split(type, "()");
                FieldDef fd = new FieldDef(getName(tds.get(0)), type, getText(tds.get(2).getText()), getText(tds.get(3).getText()), getText(tds.get(4).getText()));
                if (tokens.length > 1)
                {
                    fd.setLength(tokens[1]);
                }
                fields.add(fd);
                
            } else  if (tds.size() == 2)
            {
                String col1 = getCol1(tds.get(0));
                int    inx  = col1.indexOf(':');
                if (inx > -1)
                {
                    col1 = col1.substring(0, inx);
                }
                if (StringUtils.isNotBlank(col1))
                {
                    if (inx == -1)
                    {
                        relationships.add(new RelationshipDef(col1, getCol2(tds.get(1), true)));
                    } else
                    {
                        indexes.add(new IndexDef(col1, getCol2(tds.get(1), false)));      
                    }
                }
            }
        }
    }
    
    /**
     * @return
     */
    public void writeSp5Tables()
    {
        writeTables("datadictSp5.xml", sp5Tables);
    }
    
    /**
     * @return
     */
    public void writeTables()
    {
        writeTables("datadict.xml", tables);
    }
    
    /**
     * @return
     */
    public boolean writeTables(final String outFileName, final Vector<TableDef> tbls)
    {

        try
        {
            if (tables == null)
            {
                System.err.println("Datamodel information is null - datamodel file will not be written!!");
                return false;
            }
            System.err.println("writing data model tree to file: " +outFileName);
            
            //Element root = XMLHelper.readFileToDOM4J(new FileInputStream(XMLHelper.getConfigDirPath(datamodelOutputFileName)));
            File file = new File(outFileName);
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
            beanWriter.write("database", tbls);
            
            fw.close();
            
            return true;
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            return false;
        }
    }
    
    /**
     * @param tableDef
     * @param tableInfo
     * @param okTables
     */
    protected void compareFields(final TableDef tableDef, final DBTableIdMgr.TableInfo tableInfo, final Vector<String> okTables)
    {
        Hashtable<String, DBTableIdMgr.FieldInfo> fieldHash = new Hashtable<String, DBTableIdMgr.FieldInfo>();
        for (DBTableIdMgr.FieldInfo fi : tableInfo.getFields())
        {
            fieldHash.put(fi.getName().toLowerCase(), fi);
        }
        
        boolean hasMissing = false;
        for (FieldDef fieldDef : tableDef.getFields())
        {
            if (!fieldDef.getName().endsWith("ID"))
            {
                String fieldName = fieldDef.getName().toLowerCase();
                for (int i=0;i<2;i++)
                {
                    DBTableIdMgr.FieldInfo fieldInfo = fieldHash.get(fieldName);
                    if (fieldInfo != null)
                    {
                        //System.err.println("["+fieldDef.getLength()+"]");
                        int fdLen = StringUtils.isNotEmpty(fieldDef.getLength()) && fieldDef.getLength().indexOf(',') == -1 ? 
                                Integer.parseInt(fieldDef.getLength()) : -1;
                        if (fdLen != -1 && fdLen != fieldInfo.getLength() && fdLen > fieldInfo.getLength())
                        {
                            if (!hasMissing)
                            {
                                System.err.println("\nComparing "+tableDef.getName());
                            }
                            System.err.println("  "+fieldDef.getName()+" Length: "+fdLen+" != "+fieldInfo.getLength()); 
                        }
                        break;
                        
                    } else
                    {
                        if (i == 1)
                        {
                            if (!hasMissing)
                            {
                                System.err.println("\nComparing "+tableDef.getName());
                            }
                            System.err.println("  "+fieldDef.getName()+" is missing.");
                            hasMissing = true;
                        }
                    }
                    fieldName = "is" + fieldName;
                }
            }
        }
        
        if (!hasMissing)
        {
            okTables.add(tableDef.getName());
        }
    }
    
    protected void compareSchemas()
    {
        String[] skipTables = {
                //"AccessionAgents",
                //"AccessionAuthorizations",
                "AgentAddress",
                //"Authors",
                "BiologicalObjectAttributes",
                "BiologicalObjectRelation",
                "BiologicalObjectRelationType",
                "BorrowAgents",
                "BorrowShipments",
                "CatalogSeriesDefinition",
                "Collection",
                //"CollectionObjectCatalog",
                "CollectionObjectType",
                "CollectionTaxonomyTypes",
                //"Collectors",
                "DATAVIEWS",
                //"DeaccessionAgents",
                //"DeaccessionCollectionObject",
                "GeologicTimeBoundary",
                //"GroupPersons",
                "Habitat",
                "Image",
                "ImageAgents",
                "ImageCollectionObjects",
                "ImageLocalities",
                //"LoanAgents",
                "Observation",
                "Preparation",
                "REPORTS",
                "RaveProject",
                "Sound",
                "SoundEventStorage",
                "TaxonName",
                "TaxonomicUnitType",
                "TaxonomyType",
                "WebAdmin",
                
        };
        
        String[] nameMapArray = {
                // Old Name                New Name
                "AccessionAgents",         "AccessionAgent", 
                "AccessionAuthorizations", "AccessionAuthorization", 
                "Authors",                 "Author", 
                "BorrowAgents",            "BorrowAgent", 
                "Collectors",              "Collector", 
                "CollectionObjectCatalog", "CollectionObject", 
                "CollectionObject",        "CollectionObject", 
                "DeaccessionAgents",       "DeaccessionAgent",
                "GroupPersons",            "GroupPerson", 
                "LoanAgents",              "LoanAgent", 
                "TaxonName",               "Taxon", 
                
        };
        
        Hashtable<String, String> nameMap = new Hashtable<String, String>();
        for (int i=0;i<nameMapArray.length;i++)
        {
            nameMap.put(nameMapArray[i], nameMapArray[i+1]);
            i++;
        }
        
        Hashtable<String, String> tablesToSkip = new Hashtable<String, String>();
        for (int i=0;i<skipTables.length;i++)
        {
            tablesToSkip.put(skipTables[i], skipTables[i]);
        }
        
        Vector<String> okTables = new Vector<String>();

        DBTableIdMgr mgr = DBTableIdMgr.getInstance();
        
        for (TableDef tableDef : sp5Tables)
        {
            if (!tableDef.getName().startsWith("USYS") && tablesToSkip.get(tableDef.getName()) == null)
            {
                String tblName = nameMap.get(tableDef.getName());
                if (tblName == null)
                {
                    tblName = tableDef.getName();
                }
                DBTableIdMgr.TableInfo tableInfo = mgr.getInfoByTableName(tblName.toLowerCase());
                if (tableInfo != null)
                {
                    compareFields(tableDef, tableInfo, okTables);
                    
                } else
                {
                    System.err.println("Missing table ["+tableDef.getName()+"]");
                }
            }
        }
        
        System.out.println("\nTables passed:");
        for (String name : okTables)
        {
            System.out.println(name);
        }
        
        System.out.println("\nTables Skipped:");
        for (String name : skipTables)
        {
            System.out.println(name);
        }
    }

    /**
     * @param args
     */
    @SuppressWarnings("unchecked")
    public static void main(String[] args)
    {
        try
        {
            DataDict dd = new DataDict();
            dd.processSpecify5Schema();
            
            dd.compareSchemas();
            
            //dd.writeSp5Tables();
            
            
            Vector<String> names = new Vector<String>();
            
            Element doc = XMLHelper.readFileToDOM4J(new File("src/edu/ku/brc/specify/tools/datamodelgenerator/datadict.xml"));
            Element div = (Element)doc.selectSingleNode("//div[@class='plain']");
            if (div != null)
            {
                for (Object linkObj : div.selectNodes("p/a"))
                {
                    //System.out.println(linkObj);
                    
                    String href = XMLHelper.getAttr((Element)linkObj, "href", null);
                    if (StringUtils.isNotEmpty(href))
                    {
                        //System.out.println("["+href+"]");
                        names.add(href);
                    }
                }
            }
            for (Element table : (List<Element>)div.selectNodes("table"))
            {
                dd.processTable(table);
                break;
            }
            //dd.writeTables();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    class TableDef 
    {
        protected String name;
        protected String desc;
        protected Vector<FieldDef> fields  = new Vector<FieldDef>();
        protected Vector<IndexDef> indexes = new Vector<IndexDef>();
        protected Vector<RelationshipDef> relationships = new Vector<RelationshipDef>();
        
        public TableDef(String name, String desc)
        {
            super();
            this.name = name;
            this.desc = desc;
        }
        /**
         * @return the desc
         */
        public String getDesc()
        {
            return desc;
        }
        /**
         * @param desc the desc to set
         */
        public void setDesc(String desc)
        {
            this.desc = desc;
        }
        /**
         * @return the name
         */
        public String getName()
        {
            return name;
        }
        /**
         * @param name the name to set
         */
        public void setName(String name)
        {
            this.name = name;
        }
        /**
         * @return the fields
         */
        public Vector<FieldDef> getFields()
        {
            return fields;
        }
        /**
         * @return the index
         */
        public Vector<IndexDef> getIndexes()
        {
            return indexes;
        }
        /**
         * @return the relationships
         */
        public Vector<RelationshipDef> getRelationships()
        {
            return relationships;
        }
        
    }
    
    class FieldDef 
    {
        protected String name;
        protected String type;
        protected String desc;
        protected String required;
        protected String index;
        protected String length;
        
        public FieldDef(String name, String type, String desc, String required, String index)
        {
            super();
            this.name = name;
            this.type = type;
            this.desc = desc;
            this.required = required;
            this.index = index;
            this.length = "";
        }

        /**
         * @return the desc
         */
        public String getDesc()
        {
            return desc;
        }

        /**
         * @param desc the desc to set
         */
        public void setDesc(String desc)
        {
            this.desc = desc;
        }

        /**
         * @return the index
         */
        public String getIndex()
        {
            return index;
        }

        /**
         * @return the length
         */
        public String getLength()
        {
            return length;
        }

        /**
         * @param length the length to set
         */
        public void setLength(String length)
        {
            this.length = length;
        }

        /**
         * @param index the index to set
         */
        public void setIndex(String index)
        {
            this.index = index;
        }

        /**
         * @return the name
         */
        public String getName()
        {
            return name;
        }

        /**
         * @param name the name to set
         */
        public void setName(String name)
        {
            this.name = name;
        }

        /**
         * @return the required
         */
        public String getRequired()
        {
            return required;
        }

        /**
         * @param required the required to set
         */
        public void setRequired(String required)
        {
            this.required = required;
        }

        /**
         * @return the type
         */
        public String getType()
        {
            return type;
        }

        /**
         * @param type the type to set
         */
        public void setType(String type)
        {
            this.type = type;
        }
    }
    
    class IndexDef 
    {
        protected String name;
        protected String properties;
        
        public IndexDef(String name, String properties)
        {
            super();
            this.name = name;
            this.properties = properties;
        }
        /**
         * @return the name
         */
        public String getName()
        {
            return name;
        }
        /**
         * @param name the name to set
         */
        public void setName(String name)
        {
            this.name = name;
        }
        /**
         * @return the proeprties
         */
        public String getProperties()
        {
            return properties;
        }
        /**
         * @param proeprties the proeprties to set
         */
        public void setProperties(String properties)
        {
            this.properties = properties;
        }
    }
    
    class RelationshipDef extends IndexDef
    {
        public RelationshipDef(String name, String properties)
        {
            super(name, properties);
        }
    }

}
