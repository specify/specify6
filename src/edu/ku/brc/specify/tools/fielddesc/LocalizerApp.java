/*
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.tools.fielddesc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.util.zip.ZipInputStream;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.betwixt.XMLIntrospector;
import org.apache.commons.betwixt.io.BeanWriter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.swing.JTextComponentSpellChecker;

import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UIHelper;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Sep 4, 2007
 *
 */
public class LocalizerApp extends LocalizableBaseApp
{
    private static final Logger log = Logger.getLogger(FieldDescApp.class);
 
    protected static String    fileName   = "field_desc.xml";
    
    protected Vector<Table>    tables     = new Vector<Table>();
    protected Hashtable<String, Table> tableHash  = new Hashtable<String, Table>();
    
    // Table Fields
    protected JList            tablesList;
    protected JTextArea        tblDescText   = new JTextArea();
    protected JTextField       tblNameText   = new JTextField();
    protected JLabel           tblDescLbl;
    protected JLabel           tblNameLbl;
    
    // Field Fields
    protected JList            fieldsList;
    protected JTextArea        fieldDescText = new JTextArea();
    protected JTextField       fieldNameText = new JTextField();
    protected JLabel           fieldDescLbl;
    protected JLabel           fieldNameLbl;
    protected DefaultListModel fieldsModel   = new DefaultListModel();
    protected JButton          nxtBtn;
    protected JButton          nxtEmptyBtn;
    
    protected Table            prevTable     = null;
    protected Field            prevField     = null;
    
    protected JStatusBar       statusBar     = new JStatusBar(new int[] {5});
    protected JButton          tblSpellChkBtn = null;
    protected JButton          fldSpellChkBtn = null;
    
    protected Hashtable<String, String>         resHash     = new Hashtable<String, String>();
    protected Hashtable<String, PackageTracker> packageHash = new Hashtable<String, PackageTracker>();
    protected Hashtable<String, Boolean>        nameHash    = new Hashtable<String, Boolean>();
    
    /**
     * 
     */
    public LocalizerApp()
    {
        tables = readTableList();
        
        new MacOSAppHandler(this);
        
        appName             = "Schema Localizer";
        appVersion          = "6.0";
        appBuildVersion     = "200706111309 (SVN: 2291)";
        
        setTitle(appName + " " + appVersion);// + "  -  "+ appBuildVersion);
    }
    
    public void createDisplay()
    {
        init();
        
        buildUI(); 
    }
    
    /**
     * @return the tables
     */
    public Vector<Table> getTables()
    {
        return tables;
    }

    /**
     * @return
     */
    protected Vector<Table> readTableList()
    {
        return readTables(XMLHelper.getConfigDir(fileName));
    }
    
    /**
     * @param srcLocale
     * @param dstLocale
     */
    protected void copy(final Locale srcLocale, final Locale dstLocale)
    {
        for (Table table : tables)
        {
            table.copyLocale(srcLocale, dstLocale);
            
            for (Field field : table.getFields())
            {
                field.copyLocale(srcLocale, dstLocale);
            }
        }
    }
    
    /**
     * 
     */
    public void init()
    {
        try
        {
            File           phoneticFile = XMLHelper.getConfigDir(phoneticFileName);
            File           file         = XMLHelper.getConfigDir(dictionaryFileName);
            ZipInputStream zip          = null;

            try
            {
                zip = new ZipInputStream(new FileInputStream(file));

            } catch (NullPointerException e)
            {
                FileInputStream fin = new FileInputStream(file);
                zip = new ZipInputStream(fin);
            }

            zip.getNextEntry();
            
            dictionary = new SpellDictionaryHashMap(new BufferedReader(new InputStreamReader(zip)), new FileReader(phoneticFile));
            File userDictFile = new File(userFileName);
            if (!userDictFile.exists())
            {
                userDictFile.createNewFile();
            }
            checker  = new JTextComponentSpellChecker(dictionary);
            userDict = new SpellDictionaryHashMap(userDictFile, phoneticFile);
            checker.setUserDictionary(userDict);
            
        } catch (MalformedURLException e)
        {
            e.printStackTrace();

        } catch (FileNotFoundException e)
        {
            e.printStackTrace();

        } catch (IOException e)
        {
            e.printStackTrace();
        }
        
    }

    /**
     * @param file
     * @return
     */
    @SuppressWarnings({ "unchecked", "unchecked" })
    public Vector<Table> readTables(final File file)
    {
        Vector<Table> list = null;
        Hashtable<String, Boolean> hash = new Hashtable<String, Boolean>();
        
        try
        {
            if (true)
            {
                list = new Vector<Table>();
                Element root = XMLHelper.readFileToDOM4J(file);
                for (Object obj : root.selectNodes("/database/table"))
                {
                    Element tbl = (Element)obj;
                    Table table = new Table(XMLHelper.getAttr(tbl, "name", null));
                    
                    DBTableIdMgr.TableInfo ti = DBTableIdMgr.getInstance().getInfoByTableName(table.getName());
                    tableHash.put(ti.getTableName(), table);
                    
                    if (ti != null)
                    {
                        list.add(table);
                        hash.put(table.getName(), true);
                        
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
                        
                        /*
                        for (DBTableIdMgr.TableRelationship rel : ti.getRelationships())
                        {
                            if (rel.getType() == DBTableIdMgr.RelationshipType.ManyToMany || rel.getType() == DBTableIdMgr.RelationshipType.ManyToOne)
                            {
                                table.getFields().add(new Field(rel.getName(), rel.getType().toString()));
                            }
                        }*/
                    } else
                    {
                     // Discarding old table.
                        log.warn("Discarding Old Table ["+table.getName()+"]");
                    }
                }
            } else
            {
                /*XStream xstream = new XStream();
                xstream.alias("table", Table.class);
                xstream.alias("desc", Desc.class);
                xstream.alias("field", Field.class);
                xstream.alias("database", Database.class);
                Database database = (Database)xstream.fromXML(new FileReader(file));
                list = database.getTables();
                */
            }
            
            // Add New Tables
            for (DBTableIdMgr.TableInfo ti : DBTableIdMgr.getInstance().getList())
            {
                if (hash.get(ti.getTableName()) == null)
                {
                    Table table = new Table(ti.getTableName());
                    list.add(table);
                    tableHash.put(ti.getTableName(), table);
                    
                    log.warn("Adding New Table ["+table.getName()+"]");
                    for (DBTableIdMgr.FieldInfo fi : ti.getFields())
                    {
                        Field field = new Field(fi.getName(), fi.getType());
                        table.getFields().add(field);
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
     * @param tableName
     * @return
     */
    public String getTableDescStr(final String tableName)
    {
        Table table = tableHash.get(tableName);
        if (table != null)
        {
           return getDescStrForCurrLocale(table);
        }
        log.error("Couldn't find table ["+tableName+"]");
        return null;
    }
    
    /**
     * @param tableName
     * @return
     */
    public Desc getTableDesc(final String tableName)
    {
        Table table = tableHash.get(tableName);
        if (table != null)
        {
           return getDescForCurrLocale(table);
        }
        log.error("Couldn't find table ["+tableName+"]");
        return null;
    }
    
    /**
     * @param tableName
     * @return
     */
    public Name getTableNameDesc(final String tableName)
    {
        Table table = tableHash.get(tableName);
        if (table != null)
        {
           return getNameDescForCurrLocale(table);
        }
        log.error("Couldn't find table ["+tableName+"]");
        return null;
    }
    
    /**
     * @param tableName
     * @return
     */
    public Desc getFieldDesc(final String tableName, final String fieldName)
    {
        Table table = tableHash.get(tableName);
        if (table != null)
        {
            for (Field f : table.getFields())
            {
                if (f.getName().equals(fieldName))
                {
                    return getDescForCurrLocale(f);
                }
            }
           
        }
        log.error("Couldn't find table ["+tableName+"]");
        return null;
    }
    
    /**
     * @param tableName
     * @return
     */
    public Name getFieldNameDesc(final String tableName, final String fieldName)
    {
        Table table = tableHash.get(tableName);
        if (table != null)
        {
            for (Field f : table.getFields())
            {
                if (f.getName().equals(fieldName))
                {
                    return getNameDescForCurrLocale(f);
                }
            }
        }
        log.error("Couldn't find table ["+tableName+"]");
        return null;
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
     * 
     */
    protected void buildUI()
    {
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        
        JMenu fileMenu = UIHelper.createMenu(menuBar, "File", "F");
        saveMenuItem = UIHelper.createMenuItem(fileMenu, "Save", "S", "", false, new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                write();
            }
        });
        //saveMenuItem.setEnabled(false);
        
        if (!UIHelper.isMacOS())
        {
            fileMenu.addSeparator();
            
            UIHelper.createMenuItem(fileMenu, "Exit", "x", "", true, new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    shutdown();
                }
            });
        }
        JMenu toolMenu = UIHelper.createMenu(menuBar, "Tools", "T");
        UIHelper.createMenuItem(toolMenu, "Create Resource Files", "C", "", true, new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                createResourceFiles();
            }
        });
        
        final JFrame thisFrame = this;
        menuBar.add(getLocaleMenu(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                int tableInx = tablesList.getSelectedIndex();
                int fieldInx = fieldsList.getSelectedIndex();
                
                tablesList.getSelectionModel().clearSelection();
                
                currLocale = getLocaleByName(((JCheckBoxMenuItem)e.getSource()).getText());
                checkLocaleMenu(currLocale);
                
                //System.out.println("New Locale: "+currLocale.getDisplayName());
                
                if (!isLocaleInUse(currLocale))
                {
                    int rv = JOptionPane.showConfirmDialog(thisFrame,
                            "Do you wish to copy a Locale?", "Locale is Empty", JOptionPane.YES_NO_OPTION);
                    if (rv == JOptionPane.YES_OPTION)
                    {
                        Locale localeToCopy = chooseNewLocale(getLocalesInUse());
                        if (localeToCopy != null)
                        {
                            copy(localeToCopy, currLocale);
                        }
                    }
                }
                
                if (tableInx != -1)
                {
                    tablesList.setSelectedIndex(tableInx);
                }
                if (fieldInx != -1)
                {
                    fieldsList.setSelectedIndex(fieldInx);
                }
                
                statusBar.setSectionText(0, currLocale.getDisplayName());
                boolean ok = currLocale.getLanguage().equals("en");
                tblSpellChkBtn.setEnabled(ok);
                fldSpellChkBtn.setEnabled(ok);
            }
        }));
        
        checkLocaleMenu(currLocale);
 
        tablesList = new JList(tables);
        fieldsList = new JList(fieldsModel);
        
        tablesList.setVisibleRowCount(10);
        tablesList.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {

            /* (non-Javadoc)
             * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
             */
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    fillFieldList();
                    getAllDataFromUI();
                    
                    Table tbl  = (Table)tablesList.getSelectedValue();
                    if (tbl != null)
                    {
                        tblDescText.setText(getDescStrForCurrLocale(tbl));
                        tblNameText.setText(getNameDescStrForCurrLocale(tbl));
    
                        if (doAutoSpellCheck)
                        {
                            checker.spellCheck(tblNameText);
                            checker.spellCheck(tblDescText);
                        }
                    } else
                    {
                        tblDescText.setText("");
                        tblNameText.setText("");
                        fieldsList.setSelectedIndex(-1);
                    }
                    
                    boolean ok = tbl != null;
                    tblDescText.setEnabled(ok);
                    tblNameText.setEnabled(ok);
                    tblNameLbl.setEnabled(ok);
                    tblDescLbl.setEnabled(ok);

                    prevTable = tbl;
                }
            }
            
        });
        
        fieldsList.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {

            /* (non-Javadoc)
             * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
             */
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    getAllDataFromUI();
                    fieldSelected();
                }
            }
            
        });
        fieldsList.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e)
            {
                super.focusLost(e);
                //lastIndex = fieldsList.getSelectedIndex();
            }
        });
        
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        
        setSize(800, 600);

        fieldDescText.setRows(5);
        fieldDescText.setLineWrap(true);
        
        CellConstraints cc = new CellConstraints();
        
        // Table Section Layout
        tblSpellChkBtn               = new JButton("Spell Check");
        JPanel      tpbbp            = ButtonBarFactory.buildCenteredBar(new JButton[] {tblSpellChkBtn});
        JScrollPane sp               = new JScrollPane(tblDescText, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tblDescText.setRows(8);
        tblDescText.setLineWrap(true);
        tblDescText.setWrapStyleWord(true);

        PanelBuilder topInner   = new PanelBuilder(new FormLayout("p,2px,f:p:g", "p,4px,p,4px,p"));
        topInner.add(tblDescLbl = new JLabel("Name:", SwingConstants.RIGHT), cc.xy(1, 1));
        topInner.add(tblNameText,        cc.xy(3, 1));
        topInner.add(tblNameLbl = new JLabel("Desc:", SwingConstants.RIGHT), cc.xy(1, 3));
        topInner.add(sp,                 cc.xy(3, 3));
        topInner.add(tpbbp,              cc.xywh(1, 5, 3, 1));

        
        JScrollPane tblsp = new JScrollPane(tablesList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        JScrollPane fldsp = new JScrollPane(fieldsList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        // Field
        PanelBuilder inner = new PanelBuilder(new FormLayout("max(200px;p),4px,p,2px,f:p:g", 
                                                             "p,2px,p,2px,p,2px,f:p:g"));
        inner.add(fldsp, cc.xywh(1, 1, 1, 7));
        inner.add(fieldNameLbl = new JLabel("Label:", SwingConstants.RIGHT), cc.xy(3, 1));
        inner.add(fieldNameText, cc.xy(5, 1));
        
        inner.add(fieldDescLbl = new JLabel("Desc:", SwingConstants.RIGHT), cc.xy(3, 3));
        sp = new JScrollPane(fieldDescText, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        inner.add(sp,   cc.xy(5, 3));
        fieldDescText.setLineWrap(true);
        fieldDescText.setRows(8);
        fieldDescText.setWrapStyleWord(true);
        
        nxtBtn         = new JButton("Next");
        nxtEmptyBtn    = new JButton("Next Empty");
        fldSpellChkBtn = new JButton("Spell Check");
        
        JPanel bbp = ButtonBarFactory.buildCenteredBar(new JButton[] {nxtEmptyBtn, nxtBtn, fldSpellChkBtn});
        inner.add(bbp,   cc.xywh(3, 5, 3, 1));

        
        //bbp = ButtonBarFactory.buildCenteredBar(new JButton[] {exitBtn, saveBtn});
        
        
        PanelBuilder pb = new PanelBuilder(new FormLayout("max(200px;p),4px,f:p:g", "p,4px,t:p,4px,p,4px,f:p:g,4px,p,4px,p"));
        pb.addSeparator("Tables",   cc.xywh(1, 1, 3, 1));
        pb.add(tblsp,               cc.xy  (1, 3));
        pb.add(topInner.getPanel(), cc.xy  (3, 3));
        pb.addSeparator("Fields",   cc.xywh(1, 5, 3, 1));
        pb.add(inner.getPanel(),    cc.xywh(1, 7, 3, 1));
        pb.add(statusBar,           cc.xywh(1, 11, 3, 1));

        pb.getPanel().setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        setContentPane(pb.getPanel());
        
        nxtBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                next();
            }
        });
        nxtEmptyBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                nextEmpty();
            }
        });
        
        fldSpellChkBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                checker.spellCheck(fieldDescText);
            }
            
        });
        
        tblSpellChkBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                checker.spellCheck(tblDescText);
            }
            
        });
        
        statusBar.setSectionText(0, currLocale.getDisplayName());
        
        fieldDescText.setEnabled(false);
        fieldNameText.setEnabled(false);
        
        tblDescText.setEnabled(false);
        tblNameText.setEnabled(false);
        
        fieldNameLbl.setEnabled(false);
        fieldDescLbl.setEnabled(false);
        
        tblNameLbl.setEnabled(false);
        tblDescLbl.setEnabled(false);
    }
    
    /**
     * 
     */
    protected void shutdown()
    {
        if (hasChanged)
        {
            int rv = JOptionPane.showConfirmDialog(this, "Save changes?", "Save Changes", JOptionPane.YES_NO_OPTION);
            if (rv == JOptionPane.YES_OPTION)
            {
                write();
            }
        }
        
        setVisible(false);
        System.exit(0);
    }
    
    /**
     * 
     */
    protected void updateBtns()
    {
        int     inx     = fieldsList.getSelectedIndex();
        boolean enabled = inx < fieldsModel.size() -1;
        nxtBtn.setEnabled(enabled);
        checkForMoreEmpties();
    }
    
    /**
     * 
     */
    protected void checkForMoreEmpties()
    {
        int inx = getNextEmptyIndex(fieldsList.getSelectedIndex());
        nxtEmptyBtn.setEnabled(inx != -1);
    }
    
    /**
     * @param inx
     * @return
     */
    protected int getNextEmptyIndex(final int inx)
    {
        if (inx > -1 && inx < fieldsModel.size())
        {
            for (int i=inx;i<fieldsModel.size();i++)
            {
                Field f = (Field)fieldsModel.get(i);
                if (f != null)
                {
                    Desc  desc = getDescForCurrLocale(f);
                    if (desc != null && StringUtils.isEmpty(desc.getText()))
                    {
                        return i;
                    }
                }
            }
        }
        return -1;
    }
    
    /**
     * 
     */
    protected void getAllDataFromUI()
    {
        getTableDataFromUI();
        getFieldDataFromUI();
    }
    
    protected void getTableDataFromUI()
    {
        if (prevTable != null)
        {
            setNameDescStrForCurrLocale(prevTable, tblNameText.getText());
            setDescStrForCurrLocale(prevTable,     tblDescText.getText());
        }
        
    }
    
    protected void getFieldDataFromUI()
    {
        if (prevField != null)
        {
            setNameDescStrForCurrLocale(prevField, fieldNameText.getText());
            setDescStrForCurrLocale(prevField,     fieldDescText.getText());
        }
    }
    
    /**
     * 
     */
    protected void next()
    {
        getFieldDataFromUI();
        int inx = fieldsList.getSelectedIndex();
        if (inx < fieldsModel.size()-1)
        {
            inx++;
        }
        fieldsList.setSelectedIndex(inx);
        updateBtns();
    }
    
    /**
     * 
     */
    protected void nextEmpty()
    {
        getFieldDataFromUI();
        
        int inx = getNextEmptyIndex(fieldsList.getSelectedIndex()+1);
        if (inx > -1)
        {
            fieldsList.setSelectedIndex(inx);
        }
        updateBtns();
    }
    
    /**
     * 
     */
    protected void fillFieldList()
    {
        fieldsModel.clear();
        
        Table tbl = (Table)tablesList.getSelectedValue();
        if (tbl != null)
        {
            for (Field f : tbl.getFields())
            {
                fieldsModel.addElement(f);
            }
            fieldsList.setSelectedIndex(0);
        }
        updateBtns();
    }
    
    /**
     * 
     */
    protected void fieldSelected()
    {
        Field fld  = (Field)fieldsList.getSelectedValue();
        if (fld != null)
        {
            fieldDescText.setText(getDescStrForCurrLocale(fld));
            fieldNameText.setText(getNameDescStrForCurrLocale(fld));
            
            if (doAutoSpellCheck)
            {
                checker.spellCheck(fieldDescText);
                checker.spellCheck(fieldNameText);
            }
        } else
        {
            fieldDescText.setText("");
            fieldNameText.setText(""); 
        }
        
        boolean ok = fld != null;
        fieldDescText.setEnabled(ok);
        fieldNameText.setEnabled(ok);
        fieldNameLbl.setEnabled(ok);
        fieldDescLbl.setEnabled(ok);

        prevField = fld;
        
        updateBtns();
    }
    
    /**
     * @return
     */
    protected boolean write()
    {
        statusBar.setText("Saving...");
        statusBar.paintImmediately(statusBar.getBounds());
        
        getAllDataFromUI();
        
        setHasChanged(false);
        
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
            return false;
        } finally
        {
            statusBar.setText("Saved.");
            statusBar.paintImmediately(statusBar.getBounds());
        }
    }

    
    protected void checkForLocales(final LocalizableNameDescIFace lndi, final Hashtable<String, String> localeHash)
    {
        for (Name nm : lndi.getNames())
        {
            localeHash.put(makeLocaleKey(nm.getLang(), nm.getCountry(), nm.getVariant()), "X");
        }
        for (Desc d : lndi.getDescs())
        {
            localeHash.put(makeLocaleKey(d.getLang(), d.getCountry(), d.getVariant()), "X");
        }
    }
    
    /**
     * @param locale
     * @return
     */
    public Vector<Locale> getLocalesInUse()
    {
        Hashtable<String, String> localeHash = new Hashtable<String, String>();
        for (Table table : getTables())
        {
            checkForLocales(table, localeHash);
            for (Field f : table.getFields())
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
     * @param locale
     * @return
     */
    public boolean isLocaleInUse(final Locale locale)
    {
        Hashtable<String, String> localeHash = new Hashtable<String, String>();
        for (Table table : getTables())
        {
            checkForLocales(table, localeHash);
            for (Field f : table.getFields())
            {
                checkForLocales(f, localeHash);
            }
        }
        //for (String key : localeHash.keySet())
        //{
        //    System.out.println("In Use: "+key);
        //}
        return localeHash.get(makeLocaleKey(locale)) != null;
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
    
    
    protected String findLineWith(final List<String> lines, final String key)
    {
        for (String line : lines)
        {
            if (line.indexOf(key) > -1)
            {
                return line;
            }
        }
        return null;
    }
    
    protected boolean isAllCaps(final String key)
    {
        for (int i=0;i<key.length();i++)
        {
            char c = key.charAt(i);
            if (Character.isLowerCase(c))
            {
                return false;
            }
        }
        return true;
    }
    
    protected void extractQuotedValues(final String line, Vector<String> keyNamesList)
    {
        String[] toks = StringUtils.split(line, "\"");
        int cnt = 1;
        for (String t : toks)
        {
            //System.err.println("T["+t+"]");
            if (cnt % 2 == 0)
            {
                keyNamesList.add(t);
            }
            cnt++;
        }
    }
    
    protected int parseForNames(final String line, 
                                final int sinx, 
                                final char termChar,
                                final Vector<String> keyNamesList,
                                final List<String> lines,
                                final int lineNum)
    {
        int einx = line.indexOf(termChar, sinx);
        if (einx == -1)
        {
            log.error("Couldn't closing '"+termChar+"' ["+line+"]");
            return -1;
            
        } else if (einx == sinx+1)
        {
            return -1;
        }
        
        if (line.charAt(sinx) == '\"') // Has Quotes
        {
            if (line.charAt(einx-1) == '\"')
            {
                einx--;
            } else
            {
                System.err.println("No Quote at end! ["+line+"] "+lineNum);
            }
            keyNamesList.add(line.substring(sinx+1, einx));
            
        } else
        {
            String key = line.substring(sinx, einx);
            int inx = key.indexOf('[');
            if (inx > -1) // Is an array
            {
                String srcLine = findLineWith(lines, key);
                if (srcLine != null)
                {
                    Vector<String> keys = new Vector<String>();
                    extractQuotedValues(key, keys);
                    if (keys.size() > 0)
                    {
                        keyNamesList.addAll(keys);
                    } else
                    {
                        System.err.println("1 - Source line for Key["+key+"]  src["+srcLine+"]");
                    }
                    //for (String nm : keyNamesList)
                    //{
                    //    System.err.println("Fnd["+nm+"]");
                    //}
                } else
                {
                    System.err.println("Couldn't find source line for ["+key+"] "+lineNum);
                }
            } else
            {
                inx = key.indexOf('\"');
                if (inx > -1) // Has one or more quote's
                {
                    Vector<String> keys = new Vector<String>();
                    extractQuotedValues(key, keys);
                    if (keys.size() > 0)
                    {
                        keyNamesList.addAll(keys);
                    } else
                    {
                        System.err.println("2 - Source line for Key["+key+"]  src["+key+"]");
                    }
                } else
                {
                    if (isAllCaps(key))
                    {
                        String srcLine = findLineWith(lines, key);
                        if (srcLine != null)
                        {
                            Vector<String> keys = new Vector<String>();
                            extractQuotedValues(srcLine, keys);
                            if (keys.size() > 0)
                            {
                                keyNamesList.addAll(keys);
                            } else
                            {
                                System.err.println("3 - Source line for Key["+key+"]  src["+srcLine+"]");
                            }
                        } else
                        {
                            System.err.println("Couldn't find source line for ["+key+"] "+lineNum);
                        }
                    } else
                    {
                        System.err.println("Not Sure what to do with ["+key+"] ["+line+"]"+lineNum);    
                    }
                }
            }
            
            //System.out.println(key);
            //keyNamesList.add(key);
        }
        return einx;
    }
    
    /**
     * 
     */
    protected boolean grep(final String term)
    {
        File dir = new File(".");
        try
        {
            Collection<?> files = FileUtils.listFiles(dir, new String[] {"java", "xml"  }, true);
            for (Object obj : files)
            {
                String contents = FileUtils.readFileToString((File)obj);
                if (contents.indexOf(term) > -1)
                {
                    return true;
                }
            }
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
        return false;
    }
    
    /**
     * 
     */
    @SuppressWarnings("unchecked")
    protected void collectResources()
    {
        String[] reskeys = {"getResourceString(", "getLocalizedMessage("};
        char[]   termChar = {')', ','};
        
        Vector<String> keyNamesList = new Vector<String>();
        
        String[] filesToSkip = {"PrefsToolbar",};
        Hashtable<String, Boolean> skipNameHash = new Hashtable<String, Boolean>();
        for (String nm : filesToSkip)
        {
            skipNameHash.put(nm, true);
        }
        
        File dir = new File("src");
        try
        {
            Collection<?> files = FileUtils.listFiles(dir, new String[] {"java"}, true);
            for (Object obj : files)
            {
                File file = (File)obj;
                
                //System.out.println(file.getAbsolutePath());
                if (file.getAbsolutePath().indexOf("/tools/") > -1)
                {
                    continue;
                }
                
                if (skipNameHash.get(FilenameUtils.getBaseName(file.getName())) != null)
                {
                    continue;
                }

                boolean firstTime = true;
                
                String         packName = getPackageName(file);
                PackageTracker pt       = packageHash.get(packName);
                if (pt == null)
                {
                    pt = new PackageTracker(packName);
                    packageHash.put(packName, pt);
                }
                
                FileTracker ft = pt.getFileHash().get(file);
                if (ft == null)
                {
                    ft = new FileTracker(file);
                    pt.getFileHash().put(file, ft);
                }
                
                int lineNum = 1;
                List<?> lines = FileUtils.readLines(file);
                for (String line : (List<String>)lines)
                {
                    //System.out.println(lineNum);
                    int  inx     = -1;
                    int  len     = 0;
                    char endChar = ' ';
                    for (int i=0;i<reskeys.length;i++)
                    {
                        inx = line.indexOf(reskeys[i]);
                        if (inx > -1)
                        {
                            len     = reskeys[i].length();
                            endChar = termChar[i];
                            break;
                        }
                    }
                    
                    while (inx > -1)
                    {
                        keyNamesList.clear();
                        int einx = parseForNames(line, inx + len, endChar, keyNamesList, (List<String>)lines, lineNum);
                        if (einx == -1)
                        {
                            break;
                        }
                        
                        if (keyNamesList.size() > 0 && firstTime)
                        {
                            System.out.println(file.getAbsolutePath());
                            firstTime = false;
                        }
                        for (String nm : keyNamesList)
                        {
                            if (nameHash.get(nm) == null)
                            {
                                nameHash.put(nm, true);
                                ft.getMapping().put(nm, nm);
                            } else
                            {
                                //log.warn("["+nm+"] name was found.");
                            }
                        }
                        
                        for (int i=0;i<reskeys.length;i++)
                        {
                            inx = line.indexOf(reskeys[i], einx);
                            if (inx > -1)
                            {
                                len     = reskeys[i].length();
                                endChar = termChar[i];
                                break;
                            }
                        }
                        if (inx > -1)
                        {
                            int x = 0;
                        }
                    } // while
                    lineNum++;
                }
            }
            
            File resFile = new File("src/resources_"+currLocale.getLanguage()+".properties");
            List<?> lines = FileUtils.readLines(resFile);
            for (String line : (List<String>)lines)
            {
                if (!line.startsWith("#"))
                {
                    int inx = line.indexOf("=");
                    if (inx > -1)
                    {
                        String[] toks = StringUtils.split(line, "=");
                        resHash.put(toks[0], toks[1]);
                    }
                }
            }
            
            System.out.println("nameHash ("+nameHash.size()+") resHash ("+resHash.size()+")");
            System.out.println("In Resource not in Source Code :");
            int cnt = 0;
            for (String key : resHash.keySet())
            {
                if (nameHash.get(key) == null)
                {
                    if (!grep(key))
                    {
                        System.out.println(key);
                        cnt++;
                    }
                }
            }
            System.out.println("Missing: "+cnt);
            System.out.println("In Source not in Resource:");
            for (String key : nameHash.keySet())
            {
                if (resHash.get(key) == null)
                {
                    System.out.println(key);
                }
            }
            
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * 
     */
    protected String getPackageName(final File f)
    {
        String name = f.getAbsolutePath();
        int    sinx = name.indexOf("src/") + 4;
        int    einx = StringUtils.lastIndexOf(name, "/");
        name = name.substring(sinx, einx);
        name = StringUtils.replaceChars(name, "/", ".");
        return name;
    }
    
    /**
     * 
     */
    protected void createResourceFiles()
    {
        
        Hashtable<String, String> localeHash = new Hashtable<String, String>();
        for (Table table : getTables())
        {
            checkForLocales(table, localeHash);
            for (Field f : table.getFields())
            {
                checkForLocales(f, localeHash);
            }
        }
        
        for (String key : localeHash.keySet())
        {
            System.out.println(key);
            
            String[] toks = StringUtils.split(key, '_');
            File resFile = new File("res_"+toks[0]+"_"+toks[1]+".properties");
            try
            {
                PrintWriter pw = new PrintWriter(resFile);
                for (Table table : getTables())
                {
                    printLocales(pw, null, table, toks[0], toks[1]);
                    for (Field f : table.getFields())
                    {
                        printLocales(pw, table, f, toks[0], toks[1]);
                    }
                }
                pw.close();
                
            } catch (IOException ex)
            {
                ex.printStackTrace();
            }
            
        }
    }
    
    class PackageTracker 
    {
        protected String packageName;
        protected Hashtable<File, FileTracker> fileHash = new Hashtable<File, FileTracker>();
        /**
         * @param packageName
         */
        public PackageTracker(String packageName)
        {
            super();
            this.packageName = packageName;
        }
        /**
         * @return the packageName
         */
        public String getPackageName()
        {
            return packageName;
        }
        /**
         * @return the fileHash
         */
        public Hashtable<File, FileTracker> getFileHash()
        {
            return fileHash;
        }
        
        
    }

    class FileTracker 
    {
        protected File file;
        protected Hashtable<String, String> mapping = new Hashtable<String, String>();
        /**
         * @param file
         */
        public FileTracker(File file)
        {
            super();
            this.file = file;
        }
        /**
         * @return the file
         */
        public File getFile()
        {
            return file;
        }
        /**
         * @return the mapping
         */
        public Hashtable<String, String> getMapping()
        {
            return mapping;
        }
    }

    public class MacOSAppHandler extends Application
    {
        protected WeakReference<LocalizerApp> app;

        public MacOSAppHandler(final LocalizerApp app)
        {
            this.app = new WeakReference<LocalizerApp>(app);

            addApplicationListener(new AppHandler());

            setEnabledPreferencesMenu(false);
        }

        class AppHandler extends ApplicationAdapter
        {
            public void handleAbout(ApplicationEvent event)
            {
                app.get().doAbout();
                event.setHandled(true);
            }

            public void handleAppPrefsMgr(ApplicationEvent event)
            {
                event.setHandled(true);
            }
            
            public void handlePreferences(ApplicationEvent event) 
            {
                event.setHandled(true);
            }

            public void handleQuit(ApplicationEvent event)
            {
                app.get().shutdown();
                event.setHandled(false);  // This is so bizarre that this needs to be set to false
                                          // It seems to work backwards compared to the other calls
             }
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                LocalizerApp fd = new LocalizerApp();
                
                fd.collectResources();
                
                //fd.createDisplay();
                //UIHelper.centerAndShow(fd);
            }
        });

    }

}
