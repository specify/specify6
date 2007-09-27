/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tools.fielddesc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.commons.betwixt.XMLIntrospector;
import org.apache.commons.betwixt.io.BeanWriter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.ui.UIHelper;

/**
 * THis is a helper class for reading and writing the Schema Description XML to a file.
 * 
 * @author rod
 *
 * @code_status Beta
 *
 * Sep 26, 2007
 *
 */
public class SchemaLocalizerXMLHelper
{
    private static final Logger log = Logger.getLogger(SchemaLocalizerXMLHelper.class);
    
    protected static String    fileName   = "field_desc.xml";
    
    protected Vector<LocalizerContainerIFace>            tables     = new Vector<LocalizerContainerIFace>();
    protected Hashtable<String, LocalizerContainerIFace> tableHash  = new Hashtable<String, LocalizerContainerIFace>();
    
    protected boolean changesMadeDuringStartup = false;


    /**
     * 
     */
    public SchemaLocalizerXMLHelper()
    {
        
    }
    
    /**
     * @return the tables
     */
    public Vector<LocalizerContainerIFace> getTables()
    {
        return tables;
    }

    /**
     * @return
     */
    protected Vector<LocalizerContainerIFace> readTableList()
    {
        return readTables(XMLHelper.getConfigDir(fileName));
    }
    
    /**
     * @return the tableHash
     */
    public Hashtable<String, LocalizerContainerIFace> getTableHash()
    {
        return tableHash;
    }

    /**
     * @return the changesMadeDuringStartup
     */
    public boolean isChangesMadeDuringStartup()
    {
        return changesMadeDuringStartup;
    }

    /**
     * @param name
     * @return
     */
    public LocalizerContainerIFace getContainer(final String name)
    {
        return tableHash.get(name);
    }
    
    /**
     * Loads name descriptions
     * @param lndi the LocalizableNameDescIFace
     * @param parent the DOM node
     */
    protected void loadNamesDesc(final LocalizableNameDescIFace lndi, final Element parent)
    {
        List<?> list = parent.selectNodes("desc");
        if (list != null)
        {
            for (Object dobj : list)
            {
                Element de = (Element)dobj;
                
                String country = XMLHelper.getAttr(de, "country", "");
                Desc desc = new Desc(de.getTextTrim(),
                                        country,
                                        XMLHelper.getAttr(de, "lang", ""),
                                        XMLHelper.getAttr(de, "variant", ""));
                lndi.getDescs().add(desc);
            }
        }
        
        list = parent.selectNodes("name");
        if (list != null)
        {
            for (Object nobj : list)
            {
                Element nm = (Element)nobj;
                
                String country = XMLHelper.getAttr(nm, "country", "");
                Name nameDesc = new Name(nm.getTextTrim(),
                        country,
                        XMLHelper.getAttr(nm, "lang", ""),
                        XMLHelper.getAttr(nm, "variant", ""));
                lndi.getNames().add(nameDesc);
            }
        }
    }
    


    /**
     * @param file
     * @return
     */
    @SuppressWarnings({ "unchecked", "unchecked" })
    public Vector<LocalizerContainerIFace> readTables(final File file)
    {
        Vector<LocalizerContainerIFace> list = null;
        
        try
        {
            list = new Vector<LocalizerContainerIFace>();
            Element root = XMLHelper.readFileToDOM4J(file);
            for (Object obj : root.selectNodes("/database/table"))
            {
                Element tbl = (Element)obj;
                Table table = new Table(XMLHelper.getAttr(tbl, "name", null));
                
                DBTableIdMgr.TableInfo ti = DBTableIdMgr.getInstance().getInfoByTableName(table.getName());
                
                if (ti != null)
                {
                    list.add(table);
                    tableHash.put(table.getName(), table);
                    
                    loadNamesDesc(table, tbl);
                    
                    //setNameDescStrForCurrLocale(table, UIRegistry.getResourceString(ti.getClassObj().getSimpleName()));
                    
                    for (Object fobj : tbl.selectNodes("field"))
                    {
                        Element fld = (Element)fobj;
                        
                        String name = XMLHelper.getAttr(fld, "name", null);
                        String type = XMLHelper.getAttr(fld, "type", null); 
                        
                        Field field = new Field(name, type);
                        table.getFields().add(field);
                        
                        //String nm = field.getName();
                        //nm = nm.substring(0,1).toUpperCase() + nm.substring(1);
                        //setNameDescStrForCurrLocale(field, UIHelper.makeNamePretty(nm));
                        
                        DBTableIdMgr.FieldInfo fldInfo = null;
                        for (DBTableIdMgr.FieldInfo fi : ti.getFields())
                        {
                            if (fi.getName().equals(field.getName()))
                            {
                                field.setName(fi.getName()); 
                                field.setType(fi.getType()); 
                                fldInfo = fi;
                                break;
                            }
                        }
                        if (fldInfo == null)
                        {
                            log.error("Can't find field by name ["+field.getName()+"]");
                        }
                        
                        loadNamesDesc(field, fld);
                    }
                    
                    // Do the Same for relationships
                    for (Object robj : tbl.selectNodes("relationship"))
                    {
                        Element rel = (Element)robj;
                        
                        String name = XMLHelper.getAttr(rel, "name", null);
                        String type = XMLHelper.getAttr(rel, "type", null); 
                        
                        DBTableIdMgr.TableRelationship relInfo = null;
                        for (DBTableIdMgr.TableRelationship ri : ti.getRelationships())
                        {
                            if (ri.getName().equals(name))
                            {
                                Relationship reltn = new Relationship(name, type);
                                table.getRelationships().add(reltn);
                                relInfo = ri;
                                
                                loadNamesDesc(reltn, rel);
                                break;
                            }
                        }
                        if (relInfo == null)
                        {
                            log.error("Dropping Field ["+name+"]");
                            changesMadeDuringStartup = true;
                            
                        }
                    }
                    
                    for (DBTableIdMgr.TableRelationship ri : ti.getRelationships())
                    {
                        if (ri.getName().equals(ri.getName()))
                        {
                            Relationship reltn = new Relationship(ri.getName(), ri.getType().toString());
                            table.getRelationships().add(reltn);
                            String nm = ri.getName();
                            nm = nm.substring(0,1).toUpperCase() + nm.substring(1);
                            Name nameObj = new Name(UIHelper.makeNamePretty(nm), LocalizerBasePanel.getCurrLocale());
                            reltn.getNames().add(nameObj);
                            changesMadeDuringStartup = true;
                            
                        }
                    }

                } else
                {
                 // Discarding old table.
                    log.warn("Discarding Old Table ["+table.getName()+"]");
                }
            }
            
            
            // Add New Tables
            for (DBTableIdMgr.TableInfo ti : DBTableIdMgr.getInstance().getList())
            {
                if (tableHash.get(ti.getTableName()) == null)
                {
                    Table table = new Table(ti.getTableName());
                    list.add(table);
                    changesMadeDuringStartup = true;
                    
                    log.warn("Adding New Table ["+table.getName()+"]");
                    for (DBTableIdMgr.FieldInfo fi : ti.getFields())
                    {
                        Field field = new Field(fi.getName(), fi.getType());
                        table.getFields().add(field);
                        field.setName(fi.getName()); 
                        field.setType(fi.getType()); 
                        
                        Name nameObj = new Name(UIHelper.makeNamePretty(fi.getColumn()), LocalizerBasePanel.getCurrLocale());
                        field.getNames().add(nameObj);
                        
                    }
                    for (DBTableIdMgr.TableRelationship ri : ti.getRelationships())
                    {
                        Relationship reltn = new Relationship(ri.getName(), ri.getType().toString());
                        table.getRelationships().add(reltn);
                        String nm = ri.getName();
                        
                        nm = nm.substring(0,1).toUpperCase() + nm.substring(1);
                        Name nameObj = new Name(UIHelper.makeNamePretty(nm), LocalizerBasePanel.getCurrLocale());
                        reltn.getNames().add(nameObj);

                    }
                }
            }

            Collections.sort(list);
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
     
        return list;
    }
    
    /**
     * @return
     */
    protected boolean write()
    {
        try
        {
            if (tables == null)
            {
                log.error("Datamodel information is null - datamodel file will not be written!!");
                return false;
            }
            
            File file = XMLHelper.getConfigDir(fileName);
            log.info("Writing descriptions to file: " + file.getAbsolutePath());
            
            //Element root = XMLHelper.readFileToDOM4J(new FileInputStream(XMLHelper.getConfigDirPath(datamodelOutputFileName)));
            
            FileWriter fw = new FileWriter(file);
            fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            /*fw.write("<!-- \n");
            fw.write("    Do Not Edit this file!\n");
            fw.write("    Run DatamodelGenerator \n");
            Date date = new Date();
            fw.write("    Generated: "+date.toString()+"\n");
            fw.write("-->\n");*/
            
            //using betwixt for writing out datamodel file.  associated .betwixt files allow you to map and define 
            //output format of attributes in xml file.
            BeanWriter      beanWriter    = new BeanWriter(fw);
            XMLIntrospector introspector = beanWriter.getXMLIntrospector();
            
            introspector.getConfiguration().setWrapCollectionsInElement(false);
            
            beanWriter.getBindingConfiguration().setMapIDs(false);
            beanWriter.setWriteEmptyElements(false);
            beanWriter.enablePrettyPrint();
            beanWriter.write("database", tables);
            
            fw.close();
            
            return true;
            
        } catch (Exception ex)
        {
            log.error("error writing writeTree", ex);
        }
        return false;
    }

    protected void printLocales(final PrintWriter pw,
                                final LocalizableNameDescIFace parent, 
                                final LocalizableNameDescIFace lndi, 
                                final String lang, final String country)
    {
        for (Name nm : lndi.getNames())
        {
            if (nm.getLang().equals(lang) && nm.getCountry().equals(country))
            {
                if (parent != null)
                {
                    pw.write(parent.getName() + "_");
                }
                pw.write(lndi.getName());
                pw.write("=");
                pw.write(nm.getText());
                pw.write("\n");
            }
        }
        for (Desc d : lndi.getDescs())
        {
            if (parent != null)
            {
                pw.write(parent.getName() + "_");
            }
            pw.write(lndi.getName());
            pw.write("_desc");
            pw.write("=");
            pw.write(d.getText());
            pw.write("\n");
        }
    }
    
    /**
     * 
     */
    public void createResourceFiles()
    {
        Hashtable<String, String> localeHash = new Hashtable<String, String>();
        for (LocalizerContainerIFace table : getTables())
        {
            SchemaLocalizerPanel.checkForLocales(table, localeHash);
            for (LocalizableNameDescIFace f : table.getItems())
            {
                SchemaLocalizerPanel.checkForLocales(f, localeHash);
            }
        }
        
        for (String key : localeHash.keySet())
        {
            String[] toks = StringUtils.split(key, '_');
            
            String lang    = toks[0];
            String country = toks.length > 1 && StringUtils.isNotEmpty(toks[1]) ? toks[1] : "";
            
            //System.out.println("["+key+"] "+lang+" "+country);
            
            File resFile = new File("db_resources" +
                    (StringUtils.isNotEmpty(lang) ? ("_"+lang)  : "") +
                    (StringUtils.isNotEmpty(country) ? ("_"+country)  : "") + 
                    ".properties");
            
            try
            {
                PrintWriter pw = new PrintWriter(resFile);
                for (LocalizerContainerIFace table : getTables())
                {
                    printLocales(pw, null, table, lang, country);
                    for (LocalizableNameDescIFace f : table.getItems())
                    {
                        printLocales(pw, table, f, lang, country);
                    }
                }
                pw.close();
                
            } catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
        
    }

}
