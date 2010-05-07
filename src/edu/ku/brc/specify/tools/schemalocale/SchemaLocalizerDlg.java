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
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.af.ui.weblink.WebLinkMgr;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.config.SpecifyWebLinkMgr;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.PickList;
import edu.ku.brc.specify.datamodel.SpLocaleContainer;
import edu.ku.brc.specify.datamodel.SpLocaleContainerItem;
import edu.ku.brc.specify.datamodel.SpLocaleItemStr;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.ToggleButtonChooserDlg;
import edu.ku.brc.ui.ToggleButtonChooserPanel;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.dnd.SimpleGlassPane;

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

    //CommandAction stuff
    public final static String      SCHEMA_LOCALIZER = "SCHEMA_LOCALIZER";

    private static String SCHEMALOCDLG = "SchemaLocalizerDlg";
    
    protected Byte                                         schemaType;
    protected DBTableIdMgr                                 tableMgr;
    
    // used to hold changes to formatters before committing them to DB
    protected DataObjFieldFormatMgr dataObjFieldFormatMgrCache = new DataObjFieldFormatMgr(DataObjFieldFormatMgr.getInstance()); 
    protected UIFieldFormatterMgr   uiFieldFormatterMgrCache   = new UIFieldFormatterMgr(UIFieldFormatterMgr.getInstance());
    protected SpecifyWebLinkMgr     webLinkMgrCache            = new SpecifyWebLinkMgr((SpecifyWebLinkMgr)WebLinkMgr.getInstance());

    protected SchemaLocalizerPanel                         schemaLocPanel;
    protected LocalizableIOIFace                           localizableIOIFace;
    protected LocalizableStrFactory                        localizableStrFactory;
    
    protected Vector<SpLocaleContainer>                     tables     = new Vector<SpLocaleContainer>();
    protected Hashtable<Integer, LocalizableContainerIFace> tableHash  = new Hashtable<Integer, LocalizableContainerIFace>();
    
    protected Vector<SpLocaleContainer>                     changedTables     = new Vector<SpLocaleContainer>();
    protected Hashtable<Integer, LocalizableContainerIFace> changedTableHash  = new Hashtable<Integer, LocalizableContainerIFace>();
    
    protected Vector<LocalizableJListItem>                 tableDisplayItems;
    protected Hashtable<String, LocalizableJListItem>      tableDisplayItemsHash = new Hashtable<String, LocalizableJListItem>();
    
    protected Hashtable<LocalizableJListItem, Vector<LocalizableJListItem>> itemJListItemsHash = new Hashtable<LocalizableJListItem, Vector<LocalizableJListItem>>();

    // Used for Copying Locales
    protected Vector<LocalizableStrIFace> namesList = new Vector<LocalizableStrIFace>();
    protected Vector<LocalizableStrIFace> descsList = new Vector<LocalizableStrIFace>();
    
    protected List<PickList> pickLists = null;
    protected boolean        wasSaved  = false;
    

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
        
        helpContext = "SL_HELP_CONTEXT";
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
        
        schemaLocPanel = new SchemaLocalizerPanel(this, dataObjFieldFormatMgrCache, uiFieldFormatterMgrCache, webLinkMgrCache);
        schemaLocPanel.setLocalizableIO(localizableIOIFace);
        schemaLocPanel.setUseDisciplines(false);
        
        okBtn.setEnabled(false);
        schemaLocPanel.setSaveBtn(okBtn);
        
        schemaLocPanel.setStatusBar(UIRegistry.getStatusBar());
        schemaLocPanel.buildUI();
        schemaLocPanel.setHasChanged(localizableIOIFace.didModelChangeDuringLoad());
        
        contentPanel = schemaLocPanel;
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        setTitle();
        
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
     * Saves the changes.
     */
    protected void saveAndShutdown()
    {
        // Check to make sure whether the current container has changes.
        if (schemaLocPanel.hasTableInfoChanged() && schemaLocPanel.getCurrentContainer() != null)
        {
            localizableIOIFace.containerChanged(schemaLocPanel.getCurrentContainer());
        }
        
        schemaLocPanel.setSaveBtn(null);
        
        enabledDlgBtns(false);
        
        UIRegistry.getStatusBar().setText(getResourceString("SL_SAVING_SCHEMA_LOC"));
        UIRegistry.getStatusBar().setIndeterminate(SCHEMALOCDLG, true);
        
        final SimpleGlassPane glassPane = new SimpleGlassPane(getResourceString("SchemaLocalizerFrame.SAVING"), 18);
        setGlassPane(glassPane);
        glassPane.setVisible(true);
        
        getOkBtn().setEnabled(false);
        
        SwingWorker workerThread = new SwingWorker()
        {
            @Override
            public Object construct()
            {
                save();
                
                //SchemaI18NService.getInstance().loadWithLocale(new Locale("de", "", ""));
                int disciplineeId = AppContextMgr.getInstance().getClassObject(Discipline.class).getDisciplineId();
                SchemaI18NService.getInstance().loadWithLocale(schemaType, disciplineeId, tableMgr, Locale.getDefault());
                
                SpecifyAppContextMgr.getInstance().setForceReloadViews(true);
                
                UIFieldFormatterMgr.getInstance().load();
                WebLinkMgr.getInstance().reload();
                DataObjFieldFormatMgr.getInstance().load();
                
                return null;
            }
            
            @Override
            public void finished()
            {
                glassPane.setVisible(false);
                enabledDlgBtns(true);
                UIRegistry.getStatusBar().setProgressDone(SCHEMALOCDLG);
                UIRegistry.getStatusBar().setText("");
                finishedSaving();
            }
        };
        
        // start the background task
        workerThread.start();
    }
    
    /**
     * @return
     */
    public boolean wasSaved()
    {
        return wasSaved;
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
    @Override
    public boolean createResourceFiles()
    {
        return false;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#didModelChangeDuringLoad()
     */
    @Override
    public boolean didModelChangeDuringLoad()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#getContainerDisplayItems()
     */
    @Override
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
                String tblName = rs.getString(2);
                if (shouldIncludeAppTables() || 
                    !(tblName.startsWith("sp") || 
                            tblName.startsWith("attachment") || 
                            tblName.startsWith("autonum") || 
                            tblName.equals("picklist") || 
                            tblName.equals("attributedef") || 
                            tblName.equals("recordset") || 
                            tblName.equals("inforequest") || 
                            tblName.startsWith("workbench") || 
                            tblName.endsWith("treedef") || 
                            tblName.endsWith("treedefitem") || 
                            tblName.endsWith("attachment") || 
                            tblName.endsWith("attr") || 
                            tblName.endsWith("reltype")))
                {
                    tableDisplayItems.add(new LocalizableJListItem(rs.getString(2), rs.getInt(1), null));
                }
            }
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SchemaLocalizerDlg.class, ex);
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
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SchemaLocalizerDlg.class, e);
                e.printStackTrace();
            }
        }
        return tableDisplayItems;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#getContainer(edu.ku.brc.specify.tools.schemalocale.LocalizableJListItem, edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFaceListener)
     */
    @Override
    public LocalizableContainerIFace getContainer(LocalizableJListItem item, LocalizableIOIFaceListener l)
    {
        loadTable(item.getId(), item.getName(), l);
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#getDisplayItems(edu.ku.brc.specify.tools.schemalocale.LocalizableJListItem)
     */
    @Override
    public Vector<LocalizableJListItem> getDisplayItems(LocalizableJListItem containerArg)
    {
        Vector<LocalizableJListItem> items = itemJListItemsHash.get(containerArg);
        if (items == null)
        {
            LocalizableContainerIFace cont = tableHash.get(containerArg.getId());
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
    @Override
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
    @Override
    public boolean load()
    {
        enabledDlgBtns(true);
        
        return true;
    }
    
    /**
     * @param sessionArg
     * @param containerId
     * @return
     */
    protected SpLocaleContainer loadTable(final DataProviderSessionIFace sessionArg,
                                          final int    containerId)
    {
        SpLocaleContainer        container = null;
        DataProviderSessionIFace session   = null;
        try
        {
            session = sessionArg != null ? sessionArg : DataProviderFactory.getInstance().createSession();
            
            String sql = "FROM SpLocaleContainer WHERE disciplineId = "+
            AppContextMgr.getInstance().getClassObject(Discipline.class).getDisciplineId() + " AND spLocaleContainerId = " + containerId;
            container = (SpLocaleContainer)session.getData(sql);
            tables.add(container);
            tableHash.put(container.getId(), container);

            for (SpLocaleContainerItem item : container.getItems())
            {
                // force Load of lazy collections
                container.getDescs().size();
                container.getNames().size();
                item.getDescs().size();
                item.getNames().size();
            }
            return container;
            
        } catch (Exception e)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SchemaLocalizerDlg.class, e);
            e.printStackTrace();
            
        } finally
        {
            if (session != null && sessionArg == null)
            {
                session.close();
            }
        }
        
        return null;
    }
    
    /**
     * @param containerId
     */
    protected void loadTable(final int    containerId,
                             final String name,
                             final LocalizableIOIFaceListener l)
    {
        LocalizableContainerIFace cntr = tableHash.get(containerId);
        if (cntr != null)
        {
            l.containterRetrieved(cntr);
            return;
        }
        
        UIRegistry.getStatusBar().setIndeterminate(name, true);
        UIRegistry.getStatusBar().setText(UIRegistry.getResourceString("LOADING_SCHEMA"));
        
        enabledDlgBtns(false);
        schemaLocPanel.enableUIControls(false);
        
        SwingWorker workerThread = new SwingWorker()
        {
            protected SpLocaleContainer container = null;
            
            @Override
            public Object construct()
            {
                container = loadTable(null, containerId);
                return null;
            }
            
            @Override
            public void finished()
            {
                l.containterRetrieved(container);
                
                enabledDlgBtns(true);
                //schemaLocPanel.enableUIControls(true);

                UIRegistry.getStatusBar().setProgressDone(name);
                UIRegistry.getStatusBar().setText("");
                schemaLocPanel.getContainerList().setEnabled(true);
                
            }
        };
        
        // start the background task
        workerThread.start();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#save()
     */
    @Override
    public boolean save()
    {
        schemaLocPanel.getAllDataFromUI();
        
        SimpleGlassPane glassPane = (SimpleGlassPane)getGlassPane();
        if (glassPane != null)
        {
            glassPane.setProgress(0);
        }
        
        // apply changes to formatters and save them to db
        DataObjFieldFormatMgr.getInstance().applyChanges(dataObjFieldFormatMgrCache);
        UIFieldFormatterMgr.getInstance().applyChanges(uiFieldFormatterMgrCache);
        WebLinkMgr.getInstance().applyChanges(webLinkMgrCache);

        if (changedTables.size() > 0)
        {
            DataProviderSessionIFace session = null;
            try
            {
                session = DataProviderFactory.getInstance().createSession();
                
                double step  = 100.0 / (double)changedTables.size();
                double total = 0.0;
                
                session.beginTransaction();
                for (SpLocaleContainer container : changedTables)
                {
                    SpLocaleContainer dbContainer = session.merge(container);
                    session.saveOrUpdate(dbContainer);
                    for (SpLocaleItemStr str : dbContainer.getNames())
                    {
                        session.saveOrUpdate(session.merge(str));
                        //System.out.println(c.getName()+" - "+str.getText());
                    }
                    for (SpLocaleItemStr str : dbContainer.getDescs())
                    {
                        session.saveOrUpdate(session.merge(str));
                        //System.out.println(c.getName()+" - "+str.getText());
                    }
                    
                    for (SpLocaleContainerItem item : dbContainer.getItems())
                    {
                        SpLocaleContainerItem i = session.merge(item);
                        session.saveOrUpdate(i);
                        
                        for (SpLocaleItemStr str : i.getNames())
                        {
                            session.saveOrUpdate(session.merge(str));
                            //System.out.println(i.getName()+" - "+str.getText());
                        }
                        
                        for (SpLocaleItemStr str : i.getDescs())
                        {
                            session.saveOrUpdate(session.merge(str));
                            //System.out.println(i.getName()+" - "+str.getText());
                        }
                    }
                    
                    total += step;
                    System.err.println(total+"  "+step);
                    if (glassPane != null)
                    {
                        glassPane.setProgress((int)total);
                    }
                }
                session.commit();
                session.flush();
                
                if (glassPane != null)
                {
                    glassPane.setProgress(100);
                }
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SchemaLocalizerDlg.class, ex);
                
            } finally
            {
                if (session != null)
                {
                    session.close();
                }
            }
            //notify app of localizer changes
            CommandAction cmd = new CommandAction(SCHEMA_LOCALIZER, SCHEMA_LOCALIZER, null);
            CommandDispatcher.dispatch(cmd);
            
        } else
        {
            log.warn("No Changes were saved!");
        }
        return true;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#containerChanged(edu.ku.brc.specify.tools.schemalocale.LocalizableContainerIFace)
     */
    @Override
    public void containerChanged(LocalizableContainerIFace container)
    {
        if (container instanceof SpLocaleContainer)
        {
            if (changedTableHash.get(container.getId()) == null)
            {
                changedTables.add((SpLocaleContainer)container);
                changedTableHash.put(container.getId(), container);
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#isLocaleInUse(java.util.Locale)
     */
    @Override
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
    @Override
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#shouldIncludeAppTables()
     */
    @Override
    public boolean shouldIncludeAppTables()
    {
        return false;
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
            String sql = "SELECT DISTINCT nms.language FROM SpLocaleContainer as ctn INNER JOIN ctn.items as itm INNER JOIN itm.names nms WHERE nms.language <> NULL AND ctn.schemaType = "+ schemaType;
            log.debug(sql);
            Query   query = session.createQuery(sql);
            List<?> list  = query.list();
            for (Object lang : list)
            {
                locales.add(new Locale(lang.toString(), "", ""));
            }
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SchemaLocalizerDlg.class, ex);
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
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SchemaLocalizerDlg.class, ex);
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
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SchemaLocalizerDlg.class, ex);
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
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SchemaLocalizerDlg.class, e);
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
    public void copyLocale(final LocalizableItemIFace item, 
                           final Locale srcLocale, 
                           final Locale dstLocale)
    {
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

        item.fillDescs(descsList);
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
    public void copyLocale(final LocalizableIOIFaceListener lcl, 
                           Locale srcLocale, Locale dstLocale, 
                           final PropertyChangeListener pcl)
    {
        UIRegistry.getStatusBar().setProgressRange(SCHEMALOCDLG, 0, getContainerDisplayItems().size());

        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            double step  = 100.0 / (double)tableDisplayItems.size();
            double total = 0.0;
            
            for (LocalizableJListItem listItem : tableDisplayItems)
            {
                //System.out.println(listItem.getName());
                
                SpLocaleContainer container = (SpLocaleContainer)tableHash.get(listItem.getId());
                if (container == null)
                {
                    container = loadTable(session, listItem.getId());
                    tableHash.put(listItem.getId(), container);
                }
                
                if (container != null)
                {
                    copyLocale(container, srcLocale, dstLocale);
                    
                    for (LocalizableItemIFace field : container.getItems())
                    {
                        //System.out.println("  "+field.getName());
                        copyLocale(field, srcLocale, dstLocale);
                    }
                    containerChanged(container);
                    
                    if (pcl != null)
                    {
                        pcl.propertyChange(new PropertyChangeEvent(this, "progress", total, (int)(total+step)));
                    }
                } else
                {
                    log.error("Couldn't find Container["+listItem.getId()+"]");
                }
                total += step;
            }
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SchemaLocalizerDlg.class, ex);
            ex.printStackTrace();
        } finally
        {
            session.close();
            UIRegistry.getStatusBar().setProgressDone(SCHEMALOCDLG);
        }
        
        pcl.propertyChange(new PropertyChangeEvent(this, "progress", 99, 100));
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#getPickLists(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<PickList> getPickLists(final String disciplineName)
    {
        if (StringUtils.isNotEmpty(disciplineName))
        {
            return null;
        }
        
        if (pickLists == null)
        {
            DataProviderSessionIFace session = null;
            try
            {
                session = DataProviderFactory.getInstance().createSession();
    
                pickLists = new Vector<PickList>();
                // unchecked warning: Criteria results are always the requested class
                String sql = "SELECT name, type, tableName, fieldName, pickListId FROM PickList WHERE collectionId = "+ 
                            AppContextMgr.getInstance().getClassObject(Collection.class).getCollectionId() + 
                            " ORDER BY name";
                List<?> list = session.getDataList(sql);
                for (Object row : list)
                {
                    Object[] data = (Object[])row;
                    PickList pl = new PickList();
                    pl.initialize();
                    pl.setName((String)data[0]);
                    pl.setType((Byte)data[1]);
                    pl.setTableName((String)data[2]);
                    pl.setFieldName((String)data[3]);
                    pl.setPickListId((Integer)data[4]);
                    pickLists.add(pl);
                }
                //pickLists = (List<PickList>)session.getDataList(sql);
                
            } catch (Exception ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SchemaLocalizerDlg.class, ex);
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
    @Override
    public boolean exportToDirectory(File expportDir)
    {
        throw new RuntimeException("Export is not implemented.");
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#hasUpdatablePickLists()
     */
    @Override
    public boolean hasUpdatablePickLists()
    {
        return true;
    }
    
    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
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
