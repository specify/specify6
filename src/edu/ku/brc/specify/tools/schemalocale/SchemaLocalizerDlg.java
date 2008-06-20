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
/**
 * 
 */
package edu.ku.brc.specify.tools.schemalocale;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.SchemaI18NService;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.PickList;
import edu.ku.brc.specify.datamodel.SpLocaleContainer;
import edu.ku.brc.specify.datamodel.SpLocaleContainerItem;
import edu.ku.brc.specify.datamodel.SpLocaleItemStr;
import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.ToggleButtonChooserDlg;
import edu.ku.brc.ui.ToggleButtonChooserPanel;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Sep 27, 2007
 *
 */
public class SchemaLocalizerDlg extends CustomDialog implements LocalizableIOIFace, PropertyChangeListener
{
    private static final Logger log = Logger.getLogger(SchemaLocalizerDlg.class);
    private static String SCHEMALOCDLG = "SchemaLocalizerDlg";
    
    protected Byte                                         schemaType;
    protected DBTableIdMgr                                 tableMgr;
    
    protected SchemaLocalizerPanel                         schemaLocPanel;
    protected LocalizableIOIFace                           localizableIOIFace;
    protected LocalizableStrFactory                        localizableStrFactory;
    
    protected Vector<SpLocaleContainer>                     tables     = new Vector<SpLocaleContainer>();
    protected Hashtable<Integer, LocalizableContainerIFace> tableHash  = new Hashtable<Integer, LocalizableContainerIFace>();
    
    protected Vector<LocalizableJListItem>                 tableDisplayItems;
    protected Hashtable<String, LocalizableJListItem>      tableDisplayItemsHash = new Hashtable<String, LocalizableJListItem>();
    
    protected Hashtable<LocalizableJListItem, Vector<LocalizableJListItem>> itemJListItemsHash = new Hashtable<LocalizableJListItem, Vector<LocalizableJListItem>>();

    // Used for Copying Locales
    protected Vector<LocalizableStrIFace> namesList = new Vector<LocalizableStrIFace>();
    protected Vector<LocalizableStrIFace> descsList = new Vector<LocalizableStrIFace>();
    
    protected List<PickList> pickLists = null;

    /**
     * @param frame
     * @param schemaType
     * @throws HeadlessException
     */
    public SchemaLocalizerDlg(final Frame        frame, 
                              final Byte         schemaType,
                              final DBTableIdMgr tableMgr) throws HeadlessException
    {
        super(frame, "", true, OKCANCELAPPLYHELP, null);
        this.schemaType = schemaType;
        this.tableMgr   = tableMgr;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        setApplyLabel(getResourceString("SL_CHANGE_LOCALE"));
        
        super.createUI();
        
        localizableStrFactory = new LocalizableStrFactory() {
            public LocalizableStrIFace create()
            {
                SpLocaleItemStr str = new SpLocaleItemStr();
                str.initialize();
                return str;
            }
            public LocalizableStrIFace create(String text, Locale locale)
            {
                return new SpLocaleItemStr(text, locale); // no initialize needed for this constructor
            }
        };
        
        LocalizerBasePanel.setLocalizableStrFactory(localizableStrFactory);
        
        //DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        //List<SpLocaleContainer> list = session.getDataList(SpLocaleContainer.class);
        
        localizableIOIFace = this;
        localizableIOIFace.load();
        
        schemaLocPanel = new SchemaLocalizerPanel(this);
        schemaLocPanel.setLocalizableIO(localizableIOIFace);
        schemaLocPanel.setUseDisciplines(false);
        
        okBtn.setEnabled(false);
        schemaLocPanel.setSaveBtn(okBtn);

        
        schemaLocPanel.setStatusBar(UIRegistry.getStatusBar());
        schemaLocPanel.buildUI();
        schemaLocPanel.setHasChanged(localizableIOIFace.didModelChangeDuringLoad());
        
        contentPanel   = schemaLocPanel;
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        setTitle();
        
        HelpMgr.setHelpID(getHelpBtn(), getResourceString("SL_HELP_CONTEXT"));
        
        pack();
    }
    
    /**
     * 
     */
    public void setTitle()
    {
        super.setTitle(getResourceString("SCHEMA_CONFIG") +" - " + SchemaI18NService.getCurrentLocale().getDisplayName());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#okButtonPressed()
     */
    @Override
    protected void okButtonPressed()
    {
        saveAndShutdown();
    }
    
    /**
     * 
     */
    protected void finishedSaving()
    {
        super.okButtonPressed();
    }
    
    /**
     * 
     */
    protected void saveAndShutdown()
    {
        schemaLocPanel.setSaveBtn(null);
        
        enabledDlgBtns(false);
        
        UIRegistry.getStatusBar().setText(getResourceString("SL_SAVING_SCHEMA_LOC"));
        UIRegistry.getStatusBar().setIndeterminate(SCHEMALOCDLG, true);
        
        SwingWorker workerThread = new SwingWorker()
        {
            @Override
            public Object construct()
            {
                save();
                
                //SchemaI18NService.getInstance().loadWithLocale(new Locale("de", "", ""));
                int disciplineeId = AppContextMgr.getInstance().getClassObject(Discipline.class).getDisciplineId();
                SchemaI18NService.getInstance().loadWithLocale(schemaType, disciplineeId, tableMgr, Locale.getDefault());
                
                return null;
            }
            
            @Override
            public void finished()
            {
                enabledDlgBtns(true);
                UIRegistry.getStatusBar().setProgressDone(SCHEMALOCDLG);
                UIRegistry.getStatusBar().setText("");
                finishedSaving();
            }
        };
        
        // start the background task
        workerThread.start();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#applyButtonPressed()
     */
    @Override
    protected void applyButtonPressed()
    {
        Vector<DisplayLocale> list = new Vector<DisplayLocale>();
        for (Locale locale : Locale.getAvailableLocales())
        {
            if (StringUtils.isEmpty(locale.getCountry()))
            {
                list.add(new DisplayLocale(locale));
            }
        }
        Collections.sort(list);

        ToggleButtonChooserDlg<DisplayLocale> dlg = new ToggleButtonChooserDlg<DisplayLocale>(null, 
                                                   "CHOOSE_LOCALE", list, ToggleButtonChooserPanel.Type.RadioButton);
        dlg.setUseScrollPane(true);
        dlg.setVisible(true);
        
        if (!dlg.isCancelled())
        {
            schemaLocPanel.localeChanged(dlg.getSelectedObject().getLocale());
            setTitle();
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#createResourceFiles()
     */
    public boolean createResourceFiles()
    {
        return false;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#didModelChangeDuringLoad()
     */
    public boolean didModelChangeDuringLoad()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#getContainerDisplayItems()
     */
    public Vector<LocalizableJListItem> getContainerDisplayItems()
    {
        Connection connection = null;
        Statement stmt        = null;
        ResultSet rs          = null;
        try
        {
            tableDisplayItems = new Vector<LocalizableJListItem>();
            
            connection = DBConnection.getInstance().createConnection();
            stmt       = connection.createStatement();
            rs         = stmt.executeQuery("select SpLocaleContainerID, Name from splocalecontainer WHERE DisciplineID = "+AppContextMgr.getInstance().getClassObject(Discipline.class).getDisciplineId()+" order by name");
            
            while (rs.next())
            {
                tableDisplayItems.add(new LocalizableJListItem(rs.getString(2), rs.getInt(1), null));
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            try
            {
                if (rs != null)
                {
                    rs.close();
                }
                if (stmt != null)
                {
                    stmt.close();
                }
                if (connection != null)
                {
                    connection.close();
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return tableDisplayItems;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#getContainer(edu.ku.brc.specify.tools.schemalocale.LocalizableJListItem)
     */
    public LocalizableContainerIFace getContainer(LocalizableJListItem item)
    {
        LocalizableContainerIFace tmpContainer = tableHash.get(item.getId().intValue());
        if (tmpContainer == null)
        {
            log.error("Coulent' find container["+item.getId()+"]");
                
        }
        return tmpContainer;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#getDisplayItems(edu.ku.brc.specify.tools.schemalocale.LocalizableJListItem)
     */
    public Vector<LocalizableJListItem> getDisplayItems(LocalizableJListItem containerArg)
    {
        Vector<LocalizableJListItem> items = itemJListItemsHash.get(containerArg);
        if (items == null)
        {
            LocalizableContainerIFace cont = tableHash.get(containerArg.getId().intValue());
            if (cont != null)
            {
                SpLocaleContainer container = (SpLocaleContainer)cont;
                items = new Vector<LocalizableJListItem>();
                for (LocalizableItemIFace item : container.getContainerItems())
                {
                    SpLocaleContainerItem cItem = (SpLocaleContainerItem)item;
                    cItem.getNames();
                    cItem.getDescs();
                    
                    items.add(new LocalizableJListItem(cItem.getName(), cItem.getId(), null));
                }
                itemJListItemsHash.put(containerArg, items);
                Collections.sort(items);
                
            } else
            {
                log.error("Couldn't find container ["+containerArg.getName()+"]");
            }
        }
       
        return items;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#getItem(edu.ku.brc.specify.tools.schemalocale.LocalizableContainerIFace, edu.ku.brc.specify.tools.schemalocale.LocalizableJListItem)
     */
    public LocalizableItemIFace getItem(LocalizableContainerIFace containerArg, LocalizableJListItem item)
    {
        SpLocaleContainer container = (SpLocaleContainer)containerArg;
        
        // Make sure the items are loaded before getting them.
        if (container != null)
        {
            for (LocalizableItemIFace cItem : container.getItems())
            {
                if (cItem.getName().equals(item.getName()))
                {
                    return cItem;
                }
            }
            
        } else
        {
            log.error("Couldn't merge container ["+containerArg.getName()+"]");
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#load()
     */
    public boolean load()
    {
        enabledDlgBtns(false);
        
        UIRegistry.getStatusBar().setIndeterminate(SCHEMALOCDLG, true);
        UIRegistry.getStatusBar().setText(UIRegistry.getResourceString("LOADING_SCHEMA"));
        
        SwingWorker workerThread = new SwingWorker()
        {
            @Override
            public Object construct()
            {
                DataProviderSessionIFace session = null;
                try
                {
                    session = DataProviderFactory.getInstance().createSession();
                    List<?> list = session.getDataList("SELECT count(disciplineId) FROM SpLocaleContainer WHERE disciplineId = "+AppContextMgr.getInstance().getClassObject(Discipline.class).getDisciplineId());
                    //double total = -1.0;
                    if (list.get(0) != null && list.get(0) instanceof Integer)
                    {
                        int total = (Integer)list.get(0);
                        //UIRegistry.getStatusBar().setProgressDone(SCHEMALOCDLG);
                        //UIRegistry.getStatusBar().getProgressBar().setVisible(true);
                        UIRegistry.getStatusBar().setProgressRange(SCHEMALOCDLG, 0, total);
                    } else
                    {
                        UIRegistry.getStatusBar().setProgressDone(SCHEMALOCDLG);
                    }
                    
                    int cnt = 0;
                    list = session.getDataList("FROM SpLocaleContainer WHERE disciplineId = "+AppContextMgr.getInstance().getClassObject(Discipline.class).getDisciplineId());
                    for (Object containerObj : list)
                    {
                        UIRegistry.getStatusBar().setValue(SCHEMALOCDLG, cnt++);
                        
                        SpLocaleContainer container = (SpLocaleContainer)containerObj;
                        tables.add(container);
                        tableHash.put(container.getId().intValue(), container);
                        
                        for (SpLocaleContainerItem item : container.getItems())
                        {
                            // force Load of lazy collections
                            container.getDescs().size();
                            container.getNames().size();
                            item.getDescs().size();
                            item.getNames().size();
                        }
                    }
                } catch (Exception e)
                {
                    e.printStackTrace();
                    
                } finally
                {
                    if (session != null)
                    {
                        session.close();
                    }
                }
                return null;
            }
            
            @Override
            public void finished()
            {
                enabledDlgBtns(true);
                UIRegistry.getStatusBar().setProgressDone(SCHEMALOCDLG);
                UIRegistry.getStatusBar().setText("");
                schemaLocPanel.getContainerList().setEnabled(true);
            }
        };
        
        // start the background task
        workerThread.start();

        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#save()
     */
    public boolean save()
    {
        schemaLocPanel.getAllDataFromUI();
        try
        {
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            session.beginTransaction();
            for (SpLocaleContainer container : tables)
            {
                SpLocaleContainer c = session.merge(container);
                session.saveOrUpdate(c);
                for (SpLocaleItemStr str : c.getNames())
                {
                    session.saveOrUpdate(session.merge(str));
                    //System.out.println(str.getText());
                }
                
                for (SpLocaleContainerItem item : c.getItems())
                {
                    SpLocaleContainerItem i = session.merge(item);
                    session.saveOrUpdate(i);
                    for (SpLocaleItemStr str : i.getNames())
                    {
                        session.saveOrUpdate(session.merge(str));
                        //System.out.println(str.getText());
                    }
                }
            }
            session.commit();
            
            session.close();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return true;
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#realize(edu.ku.brc.specify.tools.schemalocale.LocalizableItemIFace)
     */
    public LocalizableItemIFace realize(final LocalizableItemIFace liif)
    {
        return liif;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#isLocaleInUse(java.util.Locale)
     */
    public boolean isLocaleInUse(final Locale locale)
    {
        // First check the aDatabase because the extra locales are not loaded automatically.
        if (isLocaleInUseInDB(schemaType, locale))
        {
            return true;
        }
        
        // Now check memory
        Hashtable<String, Boolean> localeHash = new Hashtable<String, Boolean>();
        for (SpLocaleContainer container : tables)
        {
            SchemaLocalizerXMLHelper.checkForLocales(container, localeHash);
            for (LocalizableItemIFace f : container.getContainerItems())
            {
                SchemaLocalizerXMLHelper.checkForLocales(f, localeHash);
            }
        }
        return localeHash.get(SchemaLocalizerXMLHelper.makeLocaleKey(locale)) != null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#getLocalesInUse()
     */
    public Vector<Locale> getLocalesInUse()
    {
        Hashtable<String, Boolean> localeHash = new Hashtable<String, Boolean>();
        
        // Add any from the Database
        Vector<Locale> localeList = getLocalesInUseInDB(schemaType);
        for (Locale locale : localeList)
        {
            localeHash.put(SchemaLocalizerXMLHelper.makeLocaleKey(locale.getLanguage(), locale.getCountry(), locale.getVariant()), true);
        }
        
        for (SpLocaleContainer container : tables)
        {
            SchemaLocalizerXMLHelper.checkForLocales(container, localeHash);
            for (LocalizableItemIFace f : container.getContainerItems())
            {
                SchemaLocalizerXMLHelper.checkForLocales(f, localeHash);
            }
        }
        
        localeList.clear();
        for (String key : localeHash.keySet())
        {
            String[] toks = StringUtils.split(key, "_");
            localeList.add(new Locale(toks[0], "", ""));
        }
        return localeList;
    }
    
    /**
     * Return the locales in the database
     * @return the list of locale
     */
    public static Vector<Locale> getLocalesInUseInDB(final Byte schemaType)
    {
        Vector<Locale> locales = new Vector<Locale>();
        
        Session session = HibernateUtil.getNewSession();
        try
        {
            Query   query = session.createQuery("SELECT DISTINCT nms.language FROM SpLocaleContainer as ctn INNER JOIN ctn.items as itm INNER JOIN itm.names nms WHERE nms.language <> NULL AND ctn.schemaType = "+ schemaType);
            List<?> list  = query.list();
            for (Object lang : list)
            {
                locales.add(new Locale(lang.toString(), "", ""));
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        } finally
        {
            session.close();
        }
        return locales;
    }

    /**
     * Check the Database to see if the Locale is being used.
     * @param schemaTypeArg the which set of locales
     * @param locale the locale in question
     * @return true/false
     */
    public boolean isLocaleInUseInDB(final Byte schemaTypeArg, final Locale locale)
    {
        
        Session session = HibernateUtil.getNewSession();
        try
        {
            Query   query = session.createQuery("SELECT DISTINCT nms.language FROM SpLocaleContainer as ctn INNER JOIN ctn.items as itm INNER JOIN itm.names nms WHERE nms.language = '"+locale.getLanguage()+"' AND ctn.schemaType = "+ schemaTypeArg);
            List<?> list  = query.list();
            return list.size() > 0;
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        } finally
        {
            session.close();
        }

 
        /*
        Connection connection = null;
        Statement stmt        = null;
        ResultSet rs          = null;
        try
        {
            tableDisplayItems = new Vector<LocalizableJListItem>();
            
            connection = DBConnection.getInstance().createConnection();
            stmt       = connection.createStatement();
            rs         = stmt.executeQuery("select Language from splocaleitemstr where Country = '"+locale.getLanguage()+"'");
            
            return rs.first();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            try
            {
                if (rs != null)
                {
                    rs.close();
                }
                if (stmt != null)
                {
                    stmt.close();
                }
                if (connection != null)
                {
                    connection.close();
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
*/
        return false;
    }

    
    /**
     * @param enable
     */
    protected void enabledDlgBtns(final boolean enable)
    {
        if (enable)
        {
            okBtn.setEnabled(schemaLocPanel != null ? schemaLocPanel.hasChanged() : false);
        } else
        {
            okBtn.setEnabled(false);
        }
        okBtn.setEnabled(enable ? (schemaLocPanel != null ? schemaLocPanel.hasChanged() : false) : false);
        cancelBtn.setEnabled(enable);
        applyBtn.setEnabled(enable);
        helpBtn.setEnabled(enable);
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
        UIRegistry.getStatusBar().setProgressRange(SCHEMALOCDLG, 0, getContainerDisplayItems().size());

        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            for (LocalizableJListItem listItem : tableDisplayItems)
            {
                SpLocaleContainer container = (SpLocaleContainer)tableHash.get(listItem.getId().intValue());
                if (container != null)
                {
                    copyLocale(container, srcLocale, dstLocale);
                    
                    for (LocalizableItemIFace field : container.getItems())
                    {
                        copyLocale(field, srcLocale, dstLocale);
                    }
                    
                } else
                {
                    log.error("Couldn't find Container["+listItem.getId()+"]");
                }
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        } finally
        {
            session.close();
            UIRegistry.getStatusBar().setProgressDone(SCHEMALOCDLG);
        }
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#getPickLists(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    public List<PickList> getPickLists(final String disciplineName)
    {
        if (pickLists == null)
        {
            DataProviderSessionIFace session = null;
            try
            {
                session = DataProviderFactory.getInstance().createSession();
    
                // unchecked warning: Criteria results are always the requested class
                String sql = "FROM PickList WHERE collectionId = "+ AppContextMgr.getInstance().getClassObject(Collection.class).getCollectionId();
                pickLists = (List<PickList>)session.getDataList(sql);
                
            } catch (Exception ex)
            {
                log.error(ex);
                ex.printStackTrace();
                
            } finally 
            {
                if (session != null)
                {
                    session.close();
                }
            }
        }
        return pickLists;

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#exportToDirectory(java.io.File)
     */
    public boolean exportToDirectory(File expportDir)
    {
        throw new RuntimeException("Export is not implemented.");
    }

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        if (evt.getPropertyName().equals("copyStart"))
        {
            enabledDlgBtns(false);
            
        } else if (evt.getPropertyName().equals("copyEnd"))
        {
            enabledDlgBtns(true);
        }
    }
    


}
