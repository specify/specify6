/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tools.schemalocale;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;

import edu.ku.brc.af.core.SchemaI18NService;
import edu.ku.brc.dbsupport.DBFieldInfo;
import edu.ku.brc.dbsupport.DBInfoBase;
import edu.ku.brc.dbsupport.DBRelationshipInfo;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.specify.datamodel.SpLocaleBase;
import edu.ku.brc.specify.datamodel.SpLocaleContainer;
import edu.ku.brc.specify.datamodel.SpLocaleContainerItem;
import edu.ku.brc.specify.datamodel.SpLocaleItemStr;
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
public class SchemaLocalizerXMLHelper implements LocalizableIOIFace
{
    private static final Logger log = Logger.getLogger(SchemaLocalizerXMLHelper.class);
    
    protected static String    fileName[]   = {"schema_localization.xml", "wbschema_localization.xml"};
    
    protected static LocalizableStrFactory                 localizableStrFactory;
    protected Byte                                         schemaType;
    
    protected DBTableIdMgr                                 tableMgr;
    
    protected Vector<SpLocaleContainer>                    tables     = new Vector<SpLocaleContainer>();
    protected Hashtable<String, LocalizableContainerIFace> tableHash  = new Hashtable<String, LocalizableContainerIFace>();
    
    protected Vector<LocalizableJListItem>                 tableDisplayItems;
    protected Hashtable<String, LocalizableJListItem>      tableDisplayItemsHash = new Hashtable<String, LocalizableJListItem>();
    
    protected Hashtable<LocalizableJListItem, Vector<LocalizableJListItem>> itemJListItemsHash = new Hashtable<LocalizableJListItem, Vector<LocalizableJListItem>>();
    
    protected boolean                                      changesMadeDuringStartup = false;
    
    // Used for Caching the lists
    protected Vector<LocalizableStrIFace> namesList = new Vector<LocalizableStrIFace>();
    protected Vector<LocalizableStrIFace> descsList = new Vector<LocalizableStrIFace>();

    protected StringBuilder changesBuffer = new StringBuilder();
    

    /**
     * 
     */
    public SchemaLocalizerXMLHelper(final Byte         schemaType, 
                                    final DBTableIdMgr tableMgr)
    {
        this.schemaType = schemaType;
        this.tableMgr   = tableMgr;
    }
    
    /**
     * @return the localizableStrFactory
     */
    public static LocalizableStrFactory getLocalizableStrFactory()
    {
        return localizableStrFactory;
    }

    /**
     * @param localizableStrFactory the localizableStrFactory to set
     */
    public static void setLocalizableStrFactory(LocalizableStrFactory localizableStrFactory)
    {
        SchemaLocalizerXMLHelper.localizableStrFactory = localizableStrFactory;
    }

    /**
     * @return
     */
    public Vector<SpLocaleContainer> getSpLocaleContainers()
    {
        return tables;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#load()
     */
    @SuppressWarnings("unchecked")
    public boolean load()
    {
        XStream xstream = new XStream();
        configXStream(xstream);
        
        try
        {
            File file = XMLHelper.getConfigDir(fileName[schemaType]);
            if (file.exists())
            {
                tables = (Vector<SpLocaleContainer>)xstream.fromXML(new FileReader(file));
            }
            
            for (SpLocaleContainer ct : tables)
            {
                Hashtable<String, Boolean> hash = new Hashtable<String, Boolean>();
                for (SpLocaleContainerItem item : new Vector<SpLocaleContainerItem>(ct.getItems()))
                {
                    if (hash.get(item.getName()) == null)
                    {
                        hash.put(item.getName(), true);
                    } else
                    {
                        log.debug("Removing Duplicate["+item.getName()+"]");
                        ct.getItems().remove(item);
                    }
                }
            }
            
            tableDisplayItems = new Vector<LocalizableJListItem>();
            for (SpLocaleContainer cont : tables)
            {
                LocalizableJListItem item = new LocalizableJListItem(cont.getName(), cont.getId(), null);
                tableDisplayItems.add(item);
                tableDisplayItemsHash.put(cont.getName(), item);
                
                tableHash.put(cont.getName(), cont);
            }
            
            log.info("Syncing with Datamodel.... (ignore errors)");
            changesBuffer.append("<Center><table border=\"1\">");
            
            String lang = SchemaI18NService.getCurrentLocale().getLanguage();
            
            log.info("Adding New Tables and fields....");
            for (DBTableInfo ti : tableMgr.getTables())
            {
                SpLocaleContainer container = (SpLocaleContainer)tableHash.get(ti.getName());
                if (container == null)
                {
                    // OK, table has been Localized, so add it.
                    container = new SpLocaleContainer();
                    container.initialize();
                    container.setName(ti.getName());
                    SpLocaleItemStr nameStr = new SpLocaleItemStr();
                    nameStr.initialize();
                    nameStr.setText(UIHelper.makeNamePretty(ti.getShortClassName()));
                    nameStr.setLanguage(lang);
                    container.addName(nameStr);
                    log.info("Adding Table ["+ti.getName()+"]");
                    changesMadeDuringStartup = true;
                    
                    changesBuffer.append("<tr><td align=\"center\">Added</td>");
                    changesBuffer.append("<td align=\"center\">");
                    changesBuffer.append(ti.getName());
                    changesBuffer.append("</td><td>&nbsp;</td></tr>");
                    
                    tableHash.put(container.getName(), container);
                    tables.add(container);
                    
                    LocalizableJListItem jItem = new LocalizableJListItem(container.getName(), container.getId(), null);
                    tableDisplayItems.add(jItem);
                    tableDisplayItemsHash.put(container.getName(), jItem);
                                       
                    for (DBFieldInfo fi : ti.getFields())
                    {
                        SpLocaleContainerItem item = new SpLocaleContainerItem();
                        item.initialize();
                        item.setName(fi.getName());
                        nameStr = new SpLocaleItemStr();
                        nameStr.initialize();
                        //nameStr.setText(UIHelper.makeNamePretty(fi.getDataClass().getSimpleName()));
                        nameStr.setText(UIHelper.makeNamePretty(fi.getName()));
                        nameStr.setLanguage(lang);
                        item.addName(nameStr);
                        log.info("  Adding Field ["+fi.getName()+"]");
                        changesBuffer.append("<tr><td align=\"center\">Added</td>");
                        changesBuffer.append("<td align=\"center\">&nbsp;</td><td align=\"center\">");
                        changesBuffer.append(fi.getName());
                        changesBuffer.append("</td></tr>");
                        container.addItem(item);
                    }
                    
                    for (DBRelationshipInfo ri : ti.getRelationships())
                    {
                        SpLocaleContainerItem item = new SpLocaleContainerItem();
                        item.initialize();
                        item.setName(ri.getName());
                        log.info("  Adding Field ["+ri.getName()+"]");
                        changesBuffer.append("<tr><td align=\"center\">Added</td>");
                        changesBuffer.append("<td align=\"center\">&nbsp;</td><td align=\"center\">");
                        changesBuffer.append(ri.getName());
                        changesBuffer.append("</td></tr>");
                        container.addItem(item);
                    }
                    
                } else
                {
                    // Look for existing Field
                    for (DBFieldInfo fi : ti.getFields())
                    {
                        SpLocaleContainerItem item = (SpLocaleContainerItem)container.getItemByName(fi.getName());
                        if (item == null)
                        {
                            item = new SpLocaleContainerItem();
                            item.initialize();
                            item.setName(fi.getName());
                            SpLocaleItemStr nameStr = new SpLocaleItemStr();
                            nameStr.initialize();
                            nameStr.setText(UIHelper.makeNamePretty(fi.getDataClass().getSimpleName()));
                            nameStr.setLanguage(lang);
                            item.addName(nameStr);
                            container.addItem(item);
                            log.info("For Table["+ti.getName()+"] Adding Field ["+fi.getName()+"]");
                            changesMadeDuringStartup = true;
                            changesBuffer.append("<tr><td align=\"center\">Added</td>");
                            changesBuffer.append("<td align=\"center\">"+ti.getName()+"</td><td align=\"center\">");
                            changesBuffer.append(fi.getName());
                            changesBuffer.append("</td></tr>");
                        }
                    }
                    
                    for (DBRelationshipInfo ri : ti.getRelationships())
                    {
                        SpLocaleContainerItem item = (SpLocaleContainerItem)container.getItemByName(ri.getName());
                        if (item == null)
                        {
                            item = new SpLocaleContainerItem();
                            item.initialize();
                            item.setName(ri.getName());
                            container.addItem(item);
                            SpLocaleItemStr nameStr = new SpLocaleItemStr();
                            nameStr.initialize();
                            nameStr.setText(UIHelper.makeNamePretty(ri.getName()));
                            nameStr.setLanguage(lang);
                            item.addName(nameStr);
                            log.info("For Table["+ti.getName()+"] Adding Rel ["+ri.getName()+"]");
                            changesMadeDuringStartup = true;
                            changesBuffer.append("<tr><td align=\"center\">Added</td>");
                            changesBuffer.append("<td align=\"center\">"+ti.getName()+"</td><td align=\"center\">");
                            changesBuffer.append(ri.getName());
                            changesBuffer.append("</td></tr>");
                        }
                    }
                }
            }
            
            log.info("Removing Old Tables and fields....");
            for (SpLocaleContainer container : new Vector<SpLocaleContainer>(tables))
            {
                DBTableInfo ti = tableMgr.getInfoByTableName(container.getName());
                if (ti == null)
                {
                    log.info("Removing Table ["+container.getName()+"] from Schema");
                    tables.remove(container);
                    tableHash.remove(container.getName());
                    changesMadeDuringStartup = true;
                    changesBuffer.append("<tr><td align=\"center\">Removed</td>");
                    changesBuffer.append("<td align=\"center\">");
                    changesBuffer.append(container.getName());
                    changesBuffer.append("</td><td>&nbsp;</td></tr>");
                    
                } else
                {
                    for (LocalizableItemIFace itemIF : new Vector<LocalizableItemIFace>(container.getContainerItems()))
                    {
                        SpLocaleContainerItem item     = (SpLocaleContainerItem)itemIF;
                        DBInfoBase            baseItem = ti.getItemByName(item.getName());
                        if (baseItem == null)
                        {
                            container.removeItem(item);
                            log.info("For Table["+ti.getName()+"] Removing Rel ["+item.getName()+"]");
                            changesMadeDuringStartup = true;
                            changesBuffer.append("<tr><td align=\"center\" color=\"red\">Removed</td>");
                            changesBuffer.append("<td align=\"center\">"+ti.getName()+"</td><td align=\"center\">");
                            changesBuffer.append(item.getName());
                            changesBuffer.append("</td></tr>");
                        }
                    }
                }
            }
            changesBuffer.append("</table>");
            return true;
            
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
        return false;
    }
    
    /**
     * @return returns a HTML document of what happened during the load.
     */
    public String getChangesBuffer()
    {
        return changesBuffer.toString();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#didModelChangeDuringLoad()
     */
    public boolean didModelChangeDuringLoad()
    {
        return changesMadeDuringStartup;
    }

    /**
     * @param name
     * @return
     */
    public LocalizableContainerIFace getContainer(final String name)
    {
        return tableHash.get(name);
    }
    
    protected void escapeForXML()
    {
        Vector<LocalizableContainerIFace> containers = new Vector<LocalizableContainerIFace>();
        
        for (SpLocaleContainer ctr : tables)
        {
            ctr.setName(StringEscapeUtils.escapeXml(ctr.getName()));
            for (SpLocaleItemStr str : ctr.getNames())
            {
                str.setText(StringEscapeUtils.escapeXml(str.getText()));
            }
            
            for (SpLocaleItemStr str : ctr.getDescs())
            {
                str.setText(StringEscapeUtils.escapeXml(str.getText()));
            }

            for (SpLocaleContainerItem item : ctr.getItems())
            {
                item.setName(StringEscapeUtils.escapeXml(item.getName()));
                for (SpLocaleItemStr str : item.getNames())
                {
                    str.setText(StringEscapeUtils.escapeXml(str.getText()));
                }
                
                for (SpLocaleItemStr str : item.getDescs())
                {
                    str.setText(StringEscapeUtils.escapeXml(str.getText()));
                }

            }
        }
    }

    /*
    protected void dumpAsNew(Vector<LocalizableContainerIFace> contrs)
    {
        Vector<LocalizableContainerIFace> containers = new Vector<LocalizableContainerIFace>();
        
        for (LocalizableContainerIFace ctr : contrs)
        {
            LocalizableContainerIFace container = new LocalizableContainerIFace();
            container.initialize();
            container.setName(ctr.getName());
            container.setType(ctr.getType());
            containers.add(container);
            
            for (Name nm : ctr.getNames())
            {
                SpLocaleItemStr cDesc = new SpLocaleItemStr();
                cDesc.setText(nm.getText());
                cDesc.setCountry(nm.getCountry());
                cDesc.setLanguage(nm.getLang());
                cDesc.setVariant(nm.getVariant());
                container.getNames().add(cDesc);
            }
            for (Desc nm : ctr.getDescs())
            {
                SpLocaleItemStr cDesc = new SpLocaleItemStr();
                cDesc.setText(nm.getText());
                cDesc.setCountry(nm.getCountry());
                cDesc.setLanguage(nm.getLang());
                cDesc.setVariant(nm.getVariant());
                container.getDescs().add(cDesc);
            }

            for (LocalizableItemIFace lndi:  ctr.getItems())
            {
                LocalizableItemIFace item = new LocalizableItemIFace();
                item.initialize();
                item.setName(lndi.getName());
                item.setType(lndi.getType());
                container.getItems().add(item);
                
                for (Name nm : lndi.getNames())
                {
                    SpLocaleItemStr cDesc = new SpLocaleItemStr();
                    cDesc.setText(nm.getText());
                    cDesc.setCountry(nm.getCountry());
                    cDesc.setLanguage(nm.getLang());
                    cDesc.setVariant(nm.getVariant());
                    item.getNames().add(cDesc);
                }
                for (Desc nm : lndi.getDescs())
                {
                    SpLocaleItemStr cDesc = new SpLocaleItemStr();
                    cDesc.setText(nm.getText());
                    cDesc.setCountry(nm.getCountry());
                    cDesc.setLanguage(nm.getLang());
                    cDesc.setVariant(nm.getVariant());
                    item.getDescs().add(cDesc);
                }
            }
        }

        XStream xstream = new XStream();
        
        xstream.alias("container", LocalizableContainerIFace.class);
        xstream.alias("item",      LocalizableItemIFace.class);
        xstream.alias("str",       SpLocaleItemStr.class);
        
        xstream.useAttributeFor(SpLocaleBase.class, "name");
        xstream.useAttributeFor(SpLocaleBase.class, "type");
        
        xstream.useAttributeFor(SpLocaleItemStr.class, "country");
        xstream.useAttributeFor(SpLocaleItemStr.class, "language");
        xstream.useAttributeFor(SpLocaleItemStr.class, "variant");
        
        xstream.omitField(LocalizableContainerIFace.class,  "spLocaleContainerId");
        xstream.omitField(LocalizableItemIFace.class,  "spLocaleContainerItemId");
        xstream.omitField(SpLocaleItemStr.class,  "spLocaleItemStrId");
        
        try
        {
            FileUtils.writeStringToFile(new File("schema_localization.xml"), xstream.toXML(containers));
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
    */
    
    protected void configXStream(final XStream xstream)
    {
        xstream.alias("container", SpLocaleContainer.class);
        xstream.alias("item",      SpLocaleContainerItem.class);
        xstream.alias("str",       SpLocaleItemStr.class);
        
        xstream.useAttributeFor(SpLocaleBase.class, "name");
        xstream.useAttributeFor(SpLocaleBase.class, "type");
        
        xstream.useAttributeFor(SpLocaleItemStr.class, "country");
        xstream.useAttributeFor(SpLocaleItemStr.class, "language");
        xstream.useAttributeFor(SpLocaleItemStr.class, "variant");
        
        xstream.omitField(SpLocaleContainer.class,      "spLocaleContainerId");
        xstream.omitField(SpLocaleContainer.class,      "containerItems");
        xstream.omitField(SpLocaleContainerItem.class,  "spLocaleContainerItemId");
        xstream.omitField(SpLocaleContainerItem.class,  "container");
        xstream.omitField(SpLocaleItemStr.class,        "spLocaleItemStrId");
        
        xstream.omitField(DataModelObjBase.class,  "timestampCreated");
        xstream.omitField(DataModelObjBase.class,  "timestampModified");
        xstream.omitField(DataModelObjBase.class,  "lastEditedBy");
        
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#save()
     */
    protected boolean save(final File outFile)
    {
        try
        {
            if (tables == null)
            {
                log.error("Datamodel information is null - datamodel file will not be written!!");
                return false;
            }
            
            //escapeForXML();
            
            log.info("Writing descriptions to file: " + outFile.getAbsolutePath());
            
            XStream xstream = new XStream();
            
            configXStream(xstream);
            
            FileUtils.writeStringToFile(outFile, xstream.toXML(tables));
            
            return true;
            
        } catch (IOException ex)
        {
            log.error("error writing writeTree", ex);
        }
        return false;

    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#save()
     */
    public boolean save()
    {
        return save(XMLHelper.getConfigDir(fileName[schemaType]));
    }

    /**
     * @param pw
     * @param parent
     * @param lndi
     * @param lang
     * @param country
     */
    protected void printLocales(final PrintWriter pw,
                                final LocalizableItemIFace parent, 
                                final LocalizableItemIFace lndi, 
                                final String lang, 
                                final String country)
    {
        lndi.fillNames(namesList);
        lndi.fillNames(descsList);
        
        for (LocalizableStrIFace nm : namesList)
        {
            if (nm.getLanguage().equals(lang) && nm.getCountry().equals(country))
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
        for (LocalizableStrIFace d : descsList)
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
    public boolean createResourceFiles()
    {
        Hashtable<String, Boolean> localeHash = new Hashtable<String, Boolean>();
        for (LocalizableContainerIFace table : tables)
        {
            checkForLocales(table, localeHash);
            for (LocalizableItemIFace f : table.getContainerItems())
            {
                checkForLocales(f, localeHash);
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
                for (LocalizableContainerIFace table : tables)
                {
                    printLocales(pw, null, table, lang, country);
                    for (LocalizableItemIFace f : table.getContainerItems())
                    {
                        printLocales(pw, table, f, lang, country);
                    }
                }
                pw.close();
                
                return true;
                
            } catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
        return false;
    }



    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#getContainer(edu.ku.brc.specify.tools.schemalocale.LocalizableJListItem)
     */
    public LocalizableContainerIFace getContainer(LocalizableJListItem item)
    {
        return tableHash.get(item.getName());
    }



    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#getContainerDisplayItems()
     */
    public Vector<LocalizableJListItem> getContainerDisplayItems()
    {
        return tableDisplayItems;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#getItem(edu.ku.brc.specify.tools.schemalocale.LocalizableContainerIFace, edu.ku.brc.specify.tools.schemalocale.LocalizableJListItem)
     */
    public LocalizableItemIFace getItem(LocalizableContainerIFace container,
                                        LocalizableJListItem      item)
    {
        for (LocalizableItemIFace cItem : container.getContainerItems())
        {
            if (cItem.getName().equals(item.getName()))
            {
                return cItem;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#getDisplayItems(edu.ku.brc.specify.tools.schemalocale.LocalizableJListItem)
     */
    public Vector<LocalizableJListItem> getDisplayItems(LocalizableJListItem container)
    {
        Vector<LocalizableJListItem> items = itemJListItemsHash.get(container);
        if (items == null)
        {
            LocalizableContainerIFace cont = tableHash.get(container.getName());
            if (cont != null)
            {
                items = new Vector<LocalizableJListItem>();
                for (LocalizableItemIFace item : cont.getContainerItems())
                {
                    SpLocaleContainerItem cItem = (SpLocaleContainerItem)item;
                    items.add(new LocalizableJListItem(cItem.getName(), cItem.getId(), null));
                    //System.out.println(cItem.getName());
                }
                itemJListItemsHash.put(container, items);
                Collections.sort(items);
                
            } else
            {
                log.error("Couldn't find container ["+container.getName()+"]");
            }
        }
        return items;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#realize(edu.ku.brc.specify.tools.schemalocale.LocalizableItemIFace)
     */
    public LocalizableItemIFace realize(LocalizableItemIFace item)
    {
        return item;
    }
    
    
    /**
     * @param lang
     * @param country
     * @param variant
     * @return
     */
    public static String makeLocaleKey(final String lang, final String country, final String variant)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(lang);
        sb.append(StringUtils.isNotEmpty(country) ? ("_" + country) : "");
        sb.append(StringUtils.isNotEmpty(variant) ? ("_" + variant) : "");
        System.out.println(sb.toString());
        return sb.toString();
    }
    
    /**
     * @param locale
     * @return
     */
    protected static String makeLocaleKey(final Locale locale)
    {
        return makeLocaleKey(locale.getLanguage(), locale.getCountry(), locale.getVariant());
    }
    
    /**
     * @param lndi
     * @param localeHash
     */
    public static void checkForLocales(final LocalizableItemIFace lndi, final Hashtable<String, Boolean> localeHash)
    {
        Vector<LocalizableStrIFace> namesList = new Vector<LocalizableStrIFace>();
        Vector<LocalizableStrIFace> descsList = new Vector<LocalizableStrIFace>();
        
        lndi.fillDescs(descsList);
        lndi.fillNames(namesList);
        
        for (LocalizableStrIFace nm : namesList)
        {
            localeHash.put(makeLocaleKey(nm.getLanguage(), nm.getCountry(), nm.getVariant()), true);
        }
        for (LocalizableStrIFace d : descsList)
        {
            localeHash.put(makeLocaleKey(d.getLanguage(), d.getCountry(), d.getVariant()), true);
        }
    }
    
    /**
     * @param locale
     * @return
     */
    public boolean isLocaleInUse(final Locale locale)
    {
        Hashtable<String, Boolean> localeHash = new Hashtable<String, Boolean>();
        for (SpLocaleContainer container : tables)
        {
            checkForLocales(container, localeHash);
            for (LocalizableItemIFace f : container.getContainerItems())
            {
                checkForLocales(f, localeHash);
            }
        }
        return localeHash.get(makeLocaleKey(locale)) != null;
    }
    
    /**
     * @param locale
     * @return
     */
    public Vector<Locale> getLocalesInUse()
    {
        Hashtable<String, Boolean> localeHash = new Hashtable<String, Boolean>();
        for (SpLocaleContainer container : tables)
        {
            checkForLocales(container, localeHash);
            for (LocalizableItemIFace f : container.getContainerItems())
            {
                checkForLocales(f, localeHash);
            }
        }
        Vector<Locale> inUseLocales = new Vector<Locale>(localeHash.keySet().size()+10);
        for (String key : localeHash.keySet())
        {
            String[] toks = StringUtils.split(key, "_");
            inUseLocales.add(new Locale(toks[0], "", ""));
        }
        return inUseLocales;
    }
    
    
    /**
     * @param item
     * @param srcLocale
     * @param dstLocale
     */
    public void copyLocale(final LocalizableItemIFace item, final Locale srcLocale, final Locale dstLocale)
    {
        item.fillDescs(descsList);
        item.fillNames(namesList);
        
        LocalizableStrIFace srcName = null;
        for (LocalizableStrIFace nm : namesList)
        {
            if (nm.isLocale(srcLocale))
            {
                srcName = nm;
                break;
            }
        }
        
        if (srcName != null)
        {
            LocalizableStrIFace name = localizableStrFactory.create(srcName.getText(), dstLocale);
            item.addName(name);
        }

        LocalizableStrIFace srcDesc = null;
        for (LocalizableStrIFace d : descsList)
        {
            if (d.isLocale(srcLocale))
            {
                srcDesc = d;
                break;
            }
        }
        
        if (srcDesc != null)
        {
            LocalizableStrIFace desc = localizableStrFactory.create(srcDesc.getText(), dstLocale);
            item.addDesc(desc);
        }                 
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#copyLocale(java.util.Locale, java.util.Locale)
     */
    public void copyLocale(Locale srcLocale, Locale dstLocale)
    {
        for (LocalizableJListItem listItem : getContainerDisplayItems())
        {
            LocalizableContainerIFace table = getContainer(listItem);
            
            copyLocale(table, srcLocale, dstLocale);
            
            for (LocalizableItemIFace field : table.getContainerItems())
            {
                copyLocale(field, srcLocale, dstLocale);
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#export(java.io.File)
     */
    public boolean export(File expportFile)
    {
        return save(expportFile);
    }

}
