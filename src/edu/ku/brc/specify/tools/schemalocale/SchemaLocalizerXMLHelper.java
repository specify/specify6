/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.tools.schemalocale;

import static edu.ku.brc.specify.config.init.DataBuilder.createPickList;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;

import edu.ku.brc.af.core.SchemaI18NService;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBRelationshipInfo;
import edu.ku.brc.af.core.db.DBTableChildIFace;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.config.DisciplineType;
import edu.ku.brc.specify.config.init.BldrPickList;
import edu.ku.brc.specify.config.init.BldrPickListItem;
import edu.ku.brc.specify.config.init.DataBuilder;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.specify.datamodel.PickList;
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
    protected static boolean   doFixNames = true;
    
    protected static LocalizableStrFactory                 localizableStrFactory;
    protected Byte                                         schemaType;
    
    protected DBTableIdMgr                                 tableMgr;
    
    protected Vector<DisciplineBasedContainer>             tables     = new Vector<DisciplineBasedContainer>();
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
    public Vector<DisciplineBasedContainer> getSpLocaleContainers()
    {
        return tables;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#load()
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean load()
    {
        tables = load(null);
        boolean loadedOk = tables != null;
        if (loadedOk)
        {
            
            for (DisciplineType disciplineType : DisciplineType.getDisciplineList())
            {
                Vector<DisciplineBasedContainer> dispContainers = load(disciplineType.getName());
                addDisplineBasedContainers(disciplineType.getName(), dispContainers);
            }
        }
        return loadedOk;
    }
    
    /**
     * Merges a disciplineType set of containers into the baseline.
     * @param disciplineType the disciplineType to be added
     * @param dispContainers  the containers for that disciplineType
     */
    protected void addDisplineBasedContainers(final String discipline, 
                                              final Vector<DisciplineBasedContainer> dispContainers)
    {
        if (dispContainers != null && dispContainers.size() > 0)
        {
            for (DisciplineBasedContainer dspContainer : dispContainers)
            {
                DisciplineBasedContainer container = (DisciplineBasedContainer)tableHash.get(dspContainer.getName());
                if (container != null)
                {
                    for (SpLocaleContainerItem item : dspContainer.getItems())
                    {
                        container.add(discipline, item);
                    }
                } else
                {
                    log.info("Couldn't find continer ["+dspContainer.getName()+"]");
                }
            }
        }
    }

    /**
     * @param discipline
     * @return
     */
    @SuppressWarnings("unchecked")
    protected Vector<DisciplineBasedContainer> load(final String discipline)
    {
        Vector<DisciplineBasedContainer> containers = null;
        
        XStream xstream = new XStream();
        configXStream(xstream);
        
        try
        {
            String fullPath = (discipline != null ? (discipline + File.separator) : "") + fileName[schemaType];
            File file = XMLHelper.getConfigDir(fullPath);
            if (file.exists())
            {
                containers = (Vector<DisciplineBasedContainer>)xstream.fromXML(new FileReader(file));
                
                if (discipline != null)
                {
                    return containers;
                }
            }
            
             // remove non-english locales
            if (false)
            {
                if (discipline == null)
                {
                    for (DisciplineBasedContainer dbc : containers)
                    {
                        for (SpLocaleContainerItem sci : dbc.getItems())
                        {
                            for (SpLocaleItemStr n : new Vector<SpLocaleItemStr>(sci.getNames()))
                            {
                                if (!n.getLanguage().equals("en"))
                                {
                                    sci.getNames().remove(n);
                                    changesMadeDuringStartup = true;
                                }
                            }
                            for (SpLocaleItemStr d : new Vector<SpLocaleItemStr>(sci.getDescs()))
                            {
                                if (!d.getLanguage().equals("en"))
                                {
                                    sci.getDescs().remove(d);
                                    changesMadeDuringStartup = true;
                                }
                            }
                        }
                        
                        for (SpLocaleItemStr n : new Vector<SpLocaleItemStr>(dbc.getNames()))
                        {
                            if (!n.getLanguage().equals("en"))
                            {
                                dbc.getNames().remove(n);
                                changesMadeDuringStartup = true;
                            }
                        }
                        for (SpLocaleItemStr d : new Vector<SpLocaleItemStr>(dbc.getDescs()))
                        {
                            if (!d.getLanguage().equals("en"))
                            {
                                dbc.getDescs().remove(d);
                                changesMadeDuringStartup = true;
                            }
                        }
                    }
                }
            }
            
            if (false)
            {
                if (discipline == null)
                {
                    Hashtable<String, Boolean> fieldsToHideHash = new Hashtable<String, Boolean>();
                    String[] fields = { "version", 
                            "timestampCreated", 
                            "timestampModified", 
                            "createdByAgent", 
                            "modifiedByAgent", 
                            "collectionMemberId", 
                            "visibility", 
                            "visibilitySetBy"};

                    for (String fieldName : fields)
                    {
                        fieldsToHideHash.put(fieldName, true);
                    }
                    
                    for (DisciplineBasedContainer dbc : containers)
                    {
                        for (SpLocaleContainerItem sci : dbc.getItems())
                        {
                            String nm = sci.getName();
                            if (fieldsToHideHash.get(nm) != null)
                            {
                                sci.setIsHidden(true);
                            } else if (nm.startsWith("yesNo"))
                            {
                                sci.setIsHidden(true);
                            } else if (nm.startsWith("text") && StringUtils.isNumeric(nm.substring(nm.length()-1, nm.length())))
                            {
                                //System.out.println(nm);
                                sci.setIsHidden(true);
                            } else if (nm.startsWith("number") && StringUtils.isNumeric(nm.substring(nm.length()-1, nm.length())))
                            {
                                System.out.println(nm);
                                sci.setIsHidden(true);
                            }
                        }
                    }
                }
            }

            if (containers != null)
            {
                for (SpLocaleContainer ct : containers)
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
                for (SpLocaleContainer cont : containers)
                {
                    LocalizableJListItem item = new LocalizableJListItem(cont.getName(), cont.getId(), null);
                    tableDisplayItems.add(item);
                    //System.out.println("["+cont.getName()+"]");
                    tableDisplayItemsHash.put(cont.getName(), item);
                    
                    tableHash.put(cont.getName(), cont);
                }
                
                Collections.sort(tableDisplayItems);
                
                log.info("Syncing with Datamodel.... (ignore errors)");
                changesBuffer.append("<Center><table border=\"1\">");
                
                String lang = SchemaI18NService.getCurrentLocale().getLanguage();
                
                log.info("Adding New Tables and fields....");
                for (DBTableInfo ti : tableMgr.getTables())
                {
                    DisciplineBasedContainer container = (DisciplineBasedContainer)tableHash.get(ti.getName());
                    if (container == null)
                    {
                        // OK, table has been Localized, so add it.
                        container = new DisciplineBasedContainer();
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
                        containers.add(container);
                        
                        LocalizableJListItem jItem = new LocalizableJListItem(container.getName(), container.getId(), null);
                        tableDisplayItems.add(jItem);
                        tableDisplayItemsHash.put(container.getName(), jItem);
                                           
                        for (DBFieldInfo fi : ti.getFields())
                        {
                            SpLocaleContainerItem item = new SpLocaleContainerItem();
                            item.initialize();
                            item.setName(fi.getName());
                            item.setWebLinkName(fi.getWebLinkName());
                            item.setIsRequired(fi.isRequired());
                            item.setIsHidden(fi.isHidden());
                            
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
                            
                            item.setIsRequired(fi.isRequired());
                            
                            container.addItem(item);
                        }
                        
                        for (DBRelationshipInfo ri : ti.getRelationships())
                        {
                            SpLocaleContainerItem item = new SpLocaleContainerItem();
                            item.initialize();
                            item.setName(ri.getName());
                            item.setIsRequired(false);
                            
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
                                item.setIsRequired(fi.isRequired());
                                item.setIsHidden(fi.isHidden());
                                
                                SpLocaleItemStr nameStr = new SpLocaleItemStr();
                                nameStr.initialize();
                                nameStr.setText(UIHelper.makeNamePretty(fi.getName()));
                                nameStr.setLanguage(lang);
                                item.addName(nameStr);
                                container.addItem(item);
                                log.info("For Table["+ti.getName()+"] Adding Field ["+fi.getName()+"]");
                                changesMadeDuringStartup = true;
                                changesBuffer.append("<tr><td align=\"center\">Added</td>");
                                changesBuffer.append("<td align=\"center\">"+ti.getName()+"</td><td align=\"center\">");
                                changesBuffer.append(fi.getName());
                                changesBuffer.append("</td></tr>");
                                
                            } else if (doFixNames)
                            {
                                Class<?> cls = fi.getDataClass();
                                if (cls != null)
                                {
                                    String name = UIHelper.makeNamePretty(fi.getDataClass().getSimpleName());
                                    for (SpLocaleItemStr str : item.getNames())
                                    {
                                        if (name.equals(str.getText()))
                                        {
                                            str.setText(UIHelper.makeNamePretty(fi.getName()));
                                            
                                            changesMadeDuringStartup = true;
                                            changesBuffer.append("<tr><td align=\"center\">Fixed Name</td>");
                                            changesBuffer.append("<td align=\"center\">"+ti.getName()+"</td><td align=\"center\">");
                                            changesBuffer.append(fi.getName());
                                            changesBuffer.append("</td></tr>");
                                        }
                                    }
                                } else
                                {
                                    log.error("Data Class is null for field["+fi.getColumn()+"]");
                                }
                            } else
                            {
                                //item.setIsRequired(fi.isRequired());
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
                                
                            } else 
                            {
                                if (item.getNames().size() == 0)
                                {
                                    SpLocaleItemStr nameStr = new SpLocaleItemStr();
                                    nameStr.initialize();
                                    nameStr.setText(UIHelper.makeNamePretty(ri.getName()));
                                    nameStr.setLanguage(lang);
                                    item.addName(nameStr);
                                    
                                    changesMadeDuringStartup = true;
                                    changesBuffer.append("<tr><td align=\"center\">Added</td>");
                                    changesBuffer.append("<td align=\"center\">"+ti.getName()+"</td><td align=\"center\">");
                                    changesBuffer.append(ri.getName());
                                    changesBuffer.append("</td></tr>");
                                }
                            }
                        } 
                    }
                }
                
                log.info("Removing Old Tables and fields....");
                for (SpLocaleContainer container : new Vector<DisciplineBasedContainer>(containers))
                {
                    DBTableInfo ti = tableMgr.getInfoByTableName(container.getName());
                    if (ti == null)
                    {
                        log.info("Removing Table ["+container.getName()+"] from Schema");
                        containers.remove(container);
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
                            DBTableChildIFace     tblChild = ti.getItemByName(item.getName());
                            if (tblChild == null)
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
            } else
            {
                log.info("There were no containers for ["+file.getAbsolutePath()+"]");
            }
            
            // Force the hidden of special fields
            if (false)
            {
                String[] fieldsToHide = {"timestampCreated","timestampModified",
                                        "createdByAgent","modifiedByAgent","version",
                                        "collectionMemberId"};
                Hashtable<String, Boolean> hash = new Hashtable<String, Boolean>();
                for (String fName : fieldsToHide)
                {
                    hash.put(fName, Boolean.TRUE);
                }
                for (DBTableInfo ti : tableMgr.getTables())
                {
                    DisciplineBasedContainer container = (DisciplineBasedContainer)tableHash.get(ti.getName());
                    if (container != null)
                    {
                        for (SpLocaleContainerItem item : container.getItems())
                        {
                            if (hash.get(item.getName()) != null)
                            {
                                item.setIsHidden(Boolean.TRUE);
                            }
                        }
                    }
                }
            }
            
        } catch (IOException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SchemaLocalizerXMLHelper.class, ex);
            ex.printStackTrace();
        }
        return containers;
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
    @Override
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
    
    /**
     * 
     */
    protected void escapeForXML()
    {
        //Vector<LocalizableContainerIFace> containers = new Vector<LocalizableContainerIFace>();
        
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
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SchemaLocalizerXMLHelper.class, ex);
            ex.printStackTrace();
        }
    }
    */
    
    /**
     * @param xstream
     */
    protected void configXStream(final XStream xstream)
    {
        xstream.alias("container", DisciplineBasedContainer.class);
        xstream.alias("item",      SpLocaleContainerItem.class);
        xstream.alias("str",       SpLocaleItemStr.class);
        
        xstream.useAttributeFor(SpLocaleBase.class, "name");
        xstream.useAttributeFor(SpLocaleBase.class, "type");
        xstream.useAttributeFor(SpLocaleBase.class, "format");
        xstream.useAttributeFor(SpLocaleBase.class, "isUIFormatter");
        xstream.useAttributeFor(SpLocaleBase.class, "pickListName");
        
        xstream.useAttributeFor(SpLocaleItemStr.class, "country");
        xstream.useAttributeFor(SpLocaleItemStr.class, "language");
        xstream.useAttributeFor(SpLocaleItemStr.class, "variant");
        
        xstream.useAttributeFor(SpLocaleContainerItem.class, "isRequired");
        
        xstream.omitField(DisciplineBasedContainer.class,      "disciplineHashItems");
        
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
    @Override
    public boolean save()
    {
        return save(XMLHelper.getConfigDirPath(null));
    }
    
    
    /**
     * Saves the base file and all the disciplines to a directory.
     * @param basePath the base path to the directory (must end with '/')
     * @return whether evrything was saved.
     */
    public boolean save(final String basePath)
    {
        boolean savedOk = save(basePath, null, tables);
        if (savedOk)
        {
            for (DisciplineType disciplineType : DisciplineType.getDisciplineList())
            {
                save(basePath, disciplineType.getName(), null);
            }
        }
        return savedOk;
    }
    
    /**
     * Saves the base set of containers OR 'filters out all the disciplineType-based
     * containers and saves them.
     * @param baseDir the base directory for the base set of containers (the baseline)
     * @param disciplineType the disciplineType to be saved, null if it is suppose to save the baseline. 
     * @param containers the list of containers
     * @return true on success
     */
    protected boolean save(final String baseDir, 
                           final String discipline, 
                           final Vector<DisciplineBasedContainer> containers)
    {
        Vector<DisciplineBasedContainer> localeContainers = containers;
        
        String fullPath = baseDir + (discipline != null ? (discipline + File.separator) : "") + fileName[schemaType];
        File file = new File(fullPath);
        
        // Filter oput just the containers for this disciplineType
        if (discipline != null)
        {
            localeContainers = filterForDisplineContainers(discipline);
        }
        
        return saveContainers(file, localeContainers);
    }
    
    
    /**
     * Saves a list of containers to a directory.
     * @param outFile the file it is to be saved to
     * @param containers the list of containers
     * @return on success
     */
    protected boolean saveContainers(final File outFile, 
                                     final Vector<DisciplineBasedContainer> containers)
    {
        try
        {
            if (containers == null)
            {
                log.error("Datamodel information is null - datamodel file will not be written!!");
                return false;
            }
            
            //escapeForXML();
            if (containers.size() > 0)
            {
            
                log.info("Writing descriptions to file: " + outFile.getAbsolutePath());
                
                XStream xstream = new XStream();
                
                configXStream(xstream);
                
                FileUtils.writeStringToFile(outFile, xstream.toXML(containers));
                
            } /*else
            {
                log.info("There were no items to write to ["+outFile.getAbsolutePath()+"]");
            }*/
            
            return true;
            
        } catch (IOException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SchemaLocalizerXMLHelper.class, ex);
            log.error("error writing writeTree", ex);
        }
        return false;

    }
    
    /**
     * @param discipline
     * @return
     */
    protected Vector<DisciplineBasedContainer> filterForDisplineContainers(final String discipline)
    {
        Vector<DisciplineBasedContainer> disciplineContainers = new Vector<DisciplineBasedContainer>();
        for (DisciplineBasedContainer container : tables)
        {
            if (container.hasDiscipline(discipline))
            {
                DisciplineBasedContainer dbc = (DisciplineBasedContainer)container.clone(); // Shallow Clone
                dbc.getItems().addAll(container.getDisciplineItems(discipline));
                disciplineContainers.add(dbc);
            }
        }
        return disciplineContainers;
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#createResourceFiles()
     */
    @Override
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
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SchemaLocalizerXMLHelper.class, ex);
                ex.printStackTrace();
            }
        }
        return false;
    }



    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#getContainer(edu.ku.brc.specify.tools.schemalocale.LocalizableJListItem)
     */
    @Override
    public void getContainer(final LocalizableJListItem item, final LocalizableIOIFaceListener l)
    {
        if (l != null)
        {
            l.containterRetrieved(tableHash.get(item.getName()));
        }
    }



    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#getContainerDisplayItems()
     */
    @Override
    public Vector<LocalizableJListItem> getContainerDisplayItems()
    {
        return tableDisplayItems;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#getItem(edu.ku.brc.specify.tools.schemalocale.LocalizableContainerIFace, edu.ku.brc.specify.tools.schemalocale.LocalizableJListItem)
     */
    @Override
    public LocalizableItemIFace getItem(final LocalizableContainerIFace container,
                                        final LocalizableJListItem      item)
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
    @Override
    public Vector<LocalizableJListItem> getDisplayItems(final LocalizableJListItem container)
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
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#containerChanged(edu.ku.brc.specify.tools.schemalocale.LocalizableContainerIFace)
     */
    @Override
    public void containerChanged(LocalizableContainerIFace container)
    {
        // no op - doesn't matter
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
        //System.out.println(sb.toString());
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#isLocaleInUse(java.util.Locale)
     */
    @Override
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#getLocalesInUse()
     */
    @Override
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
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#copyLocale(java.util.Locale, java.util.Locale, java.beans.PropertyChangeListener)
     */
    @Override
    public void copyLocale(final Locale srcLocale, final Locale dstLocale, final PropertyChangeListener pcl)
    {
        /*for (LocalizableJListItem listItem : getContainerDisplayItems())
        {
            LocalizableContainerIFace table = getContainer(listItem);
            
            copyLocale(table, srcLocale, dstLocale);
            
            for (LocalizableItemIFace field : table.getContainerItems())
            {
                copyLocale(field, srcLocale, dstLocale);
            }
        }*/
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#export(java.io.File)
     */
    @Override
    public boolean exportToDirectory(final File expportDirectory)
    {
        return save(expportDirectory.getAbsolutePath() + File.separator);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#getPickLists()
     */
    @Override
    public List<PickList> getPickLists(final String disciplineName)
    {
        List<PickList>     pickLists     = new Vector<PickList>();
        List<BldrPickList> bdlrPickLists;
        
        if (disciplineName == null)
        {
            bdlrPickLists = DataBuilder.getBldrPickLists("common");
            
            Hashtable<String, Boolean> nameHash = new Hashtable<String, Boolean>();
            for (DisciplineType dt : DisciplineType.getDisciplineList())
            {
                List<BldrPickList> list = DataBuilder.getBldrPickLists(dt.getName());
                if (list != null)
                {
                    for (BldrPickList bpl : list)
                    {
                       if (nameHash.get(bpl.getName()) == null)
                       {
                           nameHash.put(bpl.getName(), true);
                           bdlrPickLists.add(bpl);
                       }
                    }
                }
            }
            
        } else
        {
            bdlrPickLists = DataBuilder.getBldrPickLists(disciplineName != null ? disciplineName : "common");
        }
            
        
        for (BldrPickList pl : bdlrPickLists)
        {
            PickList pickList = createPickList(pl.getName(), pl.getType(), pl.getTableName(),
                                               pl.getFieldName(), pl.getFormatter(), pl.getReadOnly(), 
                                               pl.getSizeLimit(), pl.getIsSystem(), pl.getSortType(), null);
            for (BldrPickListItem item : pl.getItems())
            {
                pickList.addItem(item.getTitle(), item.getValue());
            }
            pickLists.add(pickList);
        }
        return pickLists;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#hasUpdatablePickLists()
     */
    @Override
    public boolean hasUpdatablePickLists()
    {
        return false;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#shouldIncludeAppTables()
     */
    @Override
    public boolean shouldIncludeAppTables()
    {
        return true;
    }

    /**
     * @param itemStrs
     * @param locale
     * @return
     */
    protected SpLocaleItemStr getNameForLocale(final Set<SpLocaleItemStr> itemStrs, final Locale locale)
    {
        for (SpLocaleItemStr itemStr : itemStrs)
        {
            if (itemStr.isLocale(locale))
            {
                return itemStr;
            }
        }
        return null;
    }
    
    /**
     * 
     */
    /**
     * 
     */
    public void setTitlesIntoSchema()
    {
        Locale locale = Locale.getDefault();
        
        for (DBTableInfo ti : tableMgr.getTables())
        {
            DisciplineBasedContainer container = (DisciplineBasedContainer)tableHash.get(ti.getName());
            SpLocaleItemStr itemStr = getNameForLocale(container.getNames(), locale);
            if (itemStr != null)
            {
                ti.setTitle(itemStr.getText());
            }
            
            Set<SpLocaleContainerItem> fieldContainers = container.getItems();
            for (DBFieldInfo fi : ti.getFields())
            {
                for (SpLocaleContainerItem item : fieldContainers)
                {
                    if (item.getName().equals(fi.getName()))
                    {
                        itemStr = getNameForLocale(item.getNames(), locale);
                        if (itemStr != null)
                        {
                            fi.setTitle(itemStr.getText());
                        }
                    }
                }
            }
        }
    }

}
