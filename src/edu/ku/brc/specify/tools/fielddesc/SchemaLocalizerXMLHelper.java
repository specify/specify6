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
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;

import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.specify.datamodel.SpLocaleBase;
import edu.ku.brc.specify.datamodel.SpLocaleContainer;
import edu.ku.brc.specify.datamodel.SpLocaleContainerItem;
import edu.ku.brc.specify.datamodel.SpLocaleItemStr;

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
    
    protected static String    fileName   = "schema_localization.xml";
    
    protected Vector<SpLocaleContainer>                    tables     = new Vector<SpLocaleContainer>();
    protected Hashtable<String, LocalizableContainerIFace> tableHash  = new Hashtable<String, LocalizableContainerIFace>();
    
    protected Vector<LocalizableJListItem>                 tableDisplayItems;
    protected Hashtable<String, LocalizableJListItem>      tableDisplayItemsHash = new Hashtable<String, LocalizableJListItem>();
    
    protected Hashtable<LocalizableJListItem, Vector<LocalizableJListItem>> itemJListItemsHash = new Hashtable<LocalizableJListItem, Vector<LocalizableJListItem>>();
    
    protected boolean changesMadeDuringStartup = false;
    
    // Used for Caching the lists
    protected Vector<LocalizableStrIFace> namesList = new Vector<LocalizableStrIFace>();
    protected Vector<LocalizableStrIFace> descsList = new Vector<LocalizableStrIFace>();



    /**
     * 
     */
    public SchemaLocalizerXMLHelper()
    {
        
    }
    
    public Vector<SpLocaleContainer> getSpLocaleContainers()
    {
        return tables;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.fielddesc.LocalizableIOIFace#load()
     */
    @SuppressWarnings("unchecked")
    public boolean load()
    {
        XStream xstream = new XStream();
        configXStream(xstream);
        
        try
        {
            File file = XMLHelper.getConfigDir(fileName);
            tables = (Vector<SpLocaleContainer>)xstream.fromXML(new FileReader(file));
            
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
                        System.out.println("Removing Duplicate["+item.getName()+"]");
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
            
            return true;
            
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.fielddesc.LocalizableIOIFace#didModelChangeDuringLoad()
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
        
        xstream.omitField(SpLocaleContainer.class,  "spLocaleContainerId");
        xstream.omitField(SpLocaleContainer.class,  "containerItems");
        xstream.omitField(SpLocaleContainerItem.class,  "spLocaleContainerItemId");
        xstream.omitField(SpLocaleItemStr.class,  "spLocaleItemStrId");
        
        xstream.omitField(DataModelObjBase.class,  "timestampCreated");
        xstream.omitField(DataModelObjBase.class,  "timestampModified");
        xstream.omitField(DataModelObjBase.class,  "lastEditedBy");
        
        
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.fielddesc.LocalizableIOIFace#save()
     */
    public boolean save()
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
            
            XStream xstream = new XStream();
            
            configXStream(xstream);
            
            FileUtils.writeStringToFile(file, xstream.toXML(tables));
            
            return true;
            
        } catch (IOException ex)
        {
            log.error("error writing writeTree", ex);
        }
        return false;
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
        Hashtable<String, String> localeHash = new Hashtable<String, String>();
        for (LocalizableContainerIFace table : tables)
        {
            SchemaLocalizerPanel.checkForLocales((LocalizableItemIFace)table, localeHash);
            for (LocalizableItemIFace f : table.getContainerItems())
            {
                SchemaLocalizerPanel.checkForLocales((LocalizableItemIFace)f, localeHash);
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
     * @see edu.ku.brc.specify.tools.fielddesc.LocalizableIOIFace#getContainer(edu.ku.brc.specify.tools.fielddesc.LocalizableJListItem)
     */
    public LocalizableContainerIFace getContainer(LocalizableJListItem item)
    {
        return tableHash.get(item.getName());
    }



    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.fielddesc.LocalizableIOIFace#getContainerDisplayItems()
     */
    public Vector<LocalizableJListItem> getContainerDisplayItems()
    {
        return tableDisplayItems;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.fielddesc.LocalizableIOIFace#getItem(edu.ku.brc.specify.tools.fielddesc.LocalizableContainerIFace, edu.ku.brc.specify.tools.fielddesc.LocalizableJListItem)
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
     * @see edu.ku.brc.specify.tools.fielddesc.LocalizableIOIFace#getDisplayItems(edu.ku.brc.specify.tools.fielddesc.LocalizableJListItem)
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
    
    

}
