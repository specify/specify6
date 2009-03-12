/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.utilapps;

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIHelper.createCheckBox;
import static edu.ku.brc.ui.UIHelper.createComboBox;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.createPasswordField;
import static edu.ku.brc.ui.UIHelper.createTextField;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.thoughtworks.xstream.XStream;

import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.Specify;
import edu.ku.brc.specify.config.DisciplineType;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Aug 11, 2008
 *
 */
class SetUpBuildDlg extends CustomDialog
{
    protected String                                   databaseName;
    protected DatabaseDriverInfo                       dbDriver;
    protected boolean                                  isCancelled = false;
    protected String                                   dbDriverName;
    protected Pair<String, String>                     dbUser; 
    protected Pair<String, String>                     saUser;  
    protected Pair<String, String>                     cmUser;  
    
    protected JTextField                               dbUsernameTxtFld;
    protected JPasswordField                           dbPasswdTxtFld;
    protected JTextField                               saUsernameTxtFld;
    protected JPasswordField                           saPasswdTxtFld;
    protected JTextField                               usrUsernameTxtFld;
    protected JPasswordField                           usrPasswdTxtFld;
    
    protected JTextField                               databaseNameTxt;
    protected JComboBox                                drivers;
    protected JCheckBox                                extraCollectionsChk;
    protected Vector<DatabaseDriverInfo>               driverList;
    protected boolean                                  wasClosed = false;
    protected BuildSampleDatabase                      bldSampleDatabase;
    
    protected JTable                                   choiceTable;
    
    protected Vector<UIFieldFormatterIFace>            catNumFmtList;
    
    protected Hashtable<String, UIFieldFormatterIFace> catNumFmtHash  = new Hashtable<String, UIFieldFormatterIFace>();
    protected Vector<CollectionChoice>                 collChoiceList = new Vector<CollectionChoice>();
    
    protected Hashtable<String, UIFieldFormatterIFace> accNumFmtHash  = new Hashtable<String, UIFieldFormatterIFace>();
    protected Vector<UIFieldFormatterIFace>            accNumFmtList;
    
    protected Hashtable<String, String>                catNumGrpHash  = new Hashtable<String, String>();
    protected Vector<String>                           catNumGrpList;
    
    protected Hashtable<String, String>                accNumGrpHash  = new Hashtable<String, String>();
    protected Vector<String>                           accNumGrpList;
    
    /**
     * Creates a dialog for entering database name and selecting the appropriate driver.
     * @param databaseName 
     * @param dbDriverName
     */
    public SetUpBuildDlg(final String               databaseName, 
                         final String               dbDriverName, 
                         final Pair<String, String> dbUser, 
                         final Pair<String, String> saUser, 
                         final Pair<String, String> cmUser, 
                         final BuildSampleDatabase  bldSampleDatabase)
    {
        super(null, "Setup Collection", true, null);
        
        this.bldSampleDatabase = bldSampleDatabase;
        this.databaseName      = databaseName;
        this.dbDriverName      = dbDriverName;
        
        this.dbUser      = dbUser;
        this.saUser      = saUser;
        this.cmUser      = cmUser;
        
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    public void createUI()
    {
        super.createUI();
        
        Specify.setUpSystemProperties();
        UIFieldFormatterMgr.setDoingLocal(true);
        
        driverList = DatabaseDriverInfo.getDriversList();
        int inx = Collections.binarySearch(driverList, new DatabaseDriverInfo(dbDriverName, null, null));
        
        drivers = createComboBox(driverList);
        drivers.setSelectedIndex(inx);
        
        catNumFmtList = (Vector<UIFieldFormatterIFace>)UIFieldFormatterMgr.getInstance().getFormatterList(CollectionObject.class, "catalogNumber");
        Collections.sort(catNumFmtList, new Comparator<UIFieldFormatterIFace>() {
            @Override
            public int compare(UIFieldFormatterIFace o1, UIFieldFormatterIFace o2)
            {
                return o1.getTitle().compareTo(o2.getTitle());
            }
        });
        for (UIFieldFormatterIFace fmt : catNumFmtList)
        {
            catNumFmtHash.put(fmt.getName(), fmt);
        }
        
        accNumFmtList = (Vector<UIFieldFormatterIFace>)UIFieldFormatterMgr.getInstance().getFormatterList(Accession.class, "accessionNumber");
        Collections.sort(accNumFmtList, new Comparator<UIFieldFormatterIFace>() {
            @Override
            public int compare(UIFieldFormatterIFace o1, UIFieldFormatterIFace o2)
            {
                return o1.getTitle().compareTo(o2.getTitle());
            }
        });
        for (UIFieldFormatterIFace fmt : accNumFmtList)
        {
            accNumFmtHash.put(fmt.getName(), fmt);
        }

        catNumGrpList = new Vector<String>();
        catNumGrpList.add("None");
        catNumGrpList.add("Cat Global");
        catNumGrpList.add("Cat Test Group #1");
        catNumGrpList.add("Cat Test Group #2");
        for (DisciplineType d : DisciplineType.getDisciplineList())
        {
            catNumGrpList.add("Cat "+d.getTitle() + " Group");
        }
        
        accNumGrpList = new Vector<String>();
        accNumGrpList.add("None");
        accNumGrpList.add("Acc Global");
        accNumGrpList.add("Acc Test Group #1");
        accNumGrpList.add("Acc Test Group #2");
        for (DisciplineType d : DisciplineType.getDisciplineList())
        {
            accNumGrpList.add("Acc "+d.getTitle() + " Group");
        }
        
        databaseNameTxt     = createTextField(databaseName, 15);
        dbUsernameTxtFld    = createTextField(dbUser.first, 15);
        dbPasswdTxtFld      = createPasswordField(dbUser.second, 15);
        
        saUsernameTxtFld   = createTextField(saUser.first, 15);
        saPasswdTxtFld     = createPasswordField(saUser.second, 15);
        
        usrUsernameTxtFld   = createTextField(cmUser.first, 15);
        usrPasswdTxtFld     = createPasswordField(cmUser.second, 15);
        
        extraCollectionsChk = createCheckBox("Create Extra Collections");
        extraCollectionsChk.setSelected(true);
        
        PanelBuilder    builder    = new PanelBuilder(new FormLayout("p,2px,p,10px,p,2px,p,10px,p,2px,p,p:g", UIHelper.createDuplicateJGoodiesDef("P", "2px", 9)+",f:p:g,4px,p"));
        CellConstraints cc         = new CellConstraints();
        int y = 1;
        builder.addSeparator("Username and Passwords",                      cc.xyw(1,y,12)); y += 2;
        builder.add(createLabel("IT Username:", SwingConstants.RIGHT),      cc.xy(1,y));
        builder.add(dbUsernameTxtFld,                                       cc.xy(3,y)); 
        builder.add(createLabel("Master Username:", SwingConstants.RIGHT),    cc.xy(5,y));
        builder.add(saUsernameTxtFld,                                       cc.xy(7,y)); 
        builder.add(createLabel("User Username:", SwingConstants.RIGHT), cc.xy(9,y));
        builder.add(usrUsernameTxtFld,                                      cc.xy(11,y));  y += 2;
        
        builder.add(createLabel("IT Password:", SwingConstants.RIGHT),      cc.xy(1,y));
        builder.add(dbPasswdTxtFld,                                         cc.xy(3,y)); 
        builder.add(createLabel("Master Password:", SwingConstants.RIGHT),   cc.xy(5,y));
        builder.add(saPasswdTxtFld,                                         cc.xy(7,y)); 
        builder.add(createLabel("User Password:", SwingConstants.RIGHT), cc.xy(9,y)); 
        builder.add(usrPasswdTxtFld,                                        cc.xy(11,y)); y += 2;
        
        builder.addSeparator("Database Info",                            cc.xyw(1,y,12)); y += 2;
        builder.add(createLabel("Database Name:", SwingConstants.RIGHT), cc.xy(1,y));
        builder.add(databaseNameTxt,                                     cc.xy(3,y)); y += 2;
        builder.add(createLabel("Driver:", SwingConstants.RIGHT),        cc.xy(1,y));
        builder.add(drivers,                                             cc.xy(3,y)); y += 2;
        builder.addSeparator("Collection Options",                       cc.xyw(1,y,12)); y += 2;
        
        collChoiceList = loadPersistedChoices();
        
        fillChoicesWithDefaults();
        
        choiceTable = new JTable(new DisciplineSetupModel());
        choiceTable.setRowHeight((new JComboBox()).getPreferredSize().height);
        
        TableColumn col = choiceTable.getColumnModel().getColumn(2);
        col.setCellEditor(new MyComboBoxEditor(catNumFmtList, catNumFmtHash));
        col.setCellRenderer(new MyLabelRenderer(catNumFmtHash));
        
        col = choiceTable.getColumnModel().getColumn(3);
        col.setCellEditor(new MyComboBoxStringEditor(catNumGrpList));
        
        col = choiceTable.getColumnModel().getColumn(4);
        col.setCellEditor(new MyComboBoxEditor(accNumFmtList, accNumFmtHash));
        
        col = choiceTable.getColumnModel().getColumn(5);
        col.setCellEditor(new MyComboBoxStringEditor(accNumGrpList));
        //col.setCellRenderer(new MyComboBoxRenderer(catNumFmtList));
        
        UIHelper.makeTableHeadersCentered(choiceTable, false);
        calcColumnWidths(choiceTable);
        builder.add(UIHelper.createScrollPane(choiceTable), cc.xywh(1,y,12,1)); y += 2;
        
        
        final JButton catGblBtn    = createButton("Global Cat Nums");
        final JButton accGblBtn    = createButton("Global Acc Nums");
        final JButton selectAllBtn = createButton("Select All");
        final JButton deSelectAll  = createButton("Deselect All");
        final JButton defBtn       = createButton("Revert");
        
        PanelBuilder btnBar = new PanelBuilder(new FormLayout("f:p:g,"+UIHelper.createDuplicateJGoodiesDef("p", "4px", 5), "p"));
        btnBar.add(catGblBtn, cc.xy(2,1));
        btnBar.add(accGblBtn, cc.xy(4,1));
        btnBar.add(selectAllBtn, cc.xy(6,1));
        btnBar.add(deSelectAll, cc.xy(8,1));
        btnBar.add(defBtn, cc.xy(10,1));
        builder.add(btnBar.getPanel(), cc.xywh(1,y,12,1));  y += 2;
        
        catGblBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                setCatNumGroup("Cat Group");
            }
         });
         
        accGblBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                setAccNumGroup("Acc Group");
            }
         });

        selectAllBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                selectAll(true);
            }
         });
         
        deSelectAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                selectAll(false);
            }
         });

        cancelBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                closeDlg(true);
            }
         });
         
        okBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                saveChoices(collChoiceList);

                closeDlg(false);
            }
         });
        
        defBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                resetDefaults(collChoiceList);
            }
         });
        
        
        // make sure closing the window does the same thing as clicking cancel
        this.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosed(WindowEvent e)
            {
                cancelBtn.doClick();
            }
        });
        
        builder.setDefaultDialogBorder();
        
        contentPanel = builder.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        pack();
        Dimension size = getSize();
        size.width = Math.max(size.width, 900);
        
        setSize(size);
    }
    
    /**
     * 
     */
    protected void fillChoicesWithDefaults()
    {
        if (collChoiceList == null || collChoiceList.size() == 0)
        {
            CollectionChoice[] choicesArray = {
                    new CollectionChoice(DisciplineType.STD_DISCIPLINES.fish, false, true),
                    new CollectionChoice(DisciplineType.STD_DISCIPLINES.fish, true, true),
                    new CollectionChoice(DisciplineType.STD_DISCIPLINES.botany, false, true),
                    new CollectionChoice(DisciplineType.STD_DISCIPLINES.invertpaleo, false, true),
            };
            
            collChoiceList = new Vector<CollectionChoice>();
            Collections.addAll(collChoiceList, choicesArray);
            
            for (DisciplineType.STD_DISCIPLINES disp : DisciplineType.STD_DISCIPLINES.values())
            {
                if (disp != DisciplineType.STD_DISCIPLINES.botany &&
                    disp != DisciplineType.STD_DISCIPLINES.invertpaleo &&
                    disp != DisciplineType.STD_DISCIPLINES.fish)
                {
                    DisciplineType dType = DisciplineType.getDiscipline(disp);
                    File file = XMLHelper.getConfigDir(dType.getName()+ File.separator + "taxon_init.xml");
                    if (file != null && file.exists())
                    {
                        CollectionChoice collChoice = new CollectionChoice(disp, false, true);
                        collChoiceList.add(collChoice);
                    }
                }
            }
            
            for (CollectionChoice collChoice : collChoiceList)
            {
                collChoice.setCatalogNumberingFmtName("CatalogNumberNumeric");
                collChoice.setAccessionNumberingFmtName("AccessionNumber");
                DisciplineType dType = DisciplineType.getDiscipline(collChoice.getType());
                collChoice.setCatNumGroup("Cat "+dType.getTitle() + " Group");
                collChoice.setAccNumGroup("Acc "+dType.getTitle() + " Group");
            }
        }
    }
    
    protected void selectAll(final boolean isSelected)
    {
        for (CollectionChoice collChoice : collChoiceList)
        {
            collChoice.setSelected(isSelected);
        } 
        ((DisciplineSetupModel)choiceTable.getModel()).fireChanged();
    }
    
    protected void setCatNumGroup(final String name)
    {
        for (CollectionChoice collChoice : collChoiceList)
        {
            collChoice.setCatNumGroup(name);
        }
        ((DisciplineSetupModel)choiceTable.getModel()).fireChanged();
    }
    
    protected void setAccNumGroup(final String name)
    {
        for (CollectionChoice collChoice : collChoiceList)
        {
            collChoice.setAccNumGroup(name);
        } 
        ((DisciplineSetupModel)choiceTable.getModel()).fireChanged();
    }
    
    public List<CollectionChoice> getSelectedColleectionChoices()
    {
        Vector<CollectionChoice> items = new Vector<CollectionChoice>();
        for (CollectionChoice cc : collChoiceList)
        {
            if (cc.isSelected())
            {
                items.add(cc);
            }
        }
        return items;
    }
    
    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    protected Vector<CollectionChoice> loadPersistedChoices()
    {
        XStream xstream = new XStream();
        CollectionChoice.config(xstream);
        
        File file = new File(UIRegistry.getDefaultWorkingPath()+File.separator+"bld_coll_choices.xml");
        if (file.exists())
        {
            try
            {
                Vector<CollectionChoice> list = (Vector<CollectionChoice>)xstream.fromXML(FileUtils.readFileToString(file));
                for (CollectionChoice cc : new Vector<CollectionChoice>(list))
                {
                    try
                    {
                        cc.initialize();
                        
                    } catch (Exception ex)
                    {
                        list.remove(cc);
                    }
                }
                return list;
                
            } catch (IOException ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SetUpBuildDlg.class, ex);
                ex.printStackTrace();
            }
        }
        return new Vector<CollectionChoice>();
    }
    
    
    
    /**
     * @param choices
     */
    public void resetDefaults(final Vector<CollectionChoice> choices)
    {
        choices.clear();
        fillChoicesWithDefaults();
        ((DisciplineSetupModel)choiceTable.getModel()).fireChanged();
    }
    
    /**
     * @param choices
     */
    public void saveChoices(final Vector<CollectionChoice> choices)
    {
        XStream xstream = new XStream();
        CollectionChoice.config(xstream);
        
        System.out.println("Start");
        File file = new File(UIRegistry.getAppDataDir()+File.separator+"bld_coll_choices.xml");
        try
        {
            FileUtils.writeStringToFile(file, xstream.toXML(choices));
            
        } catch (IOException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SetUpBuildDlg.class, ex);
            ex.printStackTrace();
        }
        System.out.println("Stop");
    }
    
    public void closeDlg(final boolean wasCancelled)
    {
        if (!wasClosed)
        {
            isCancelled = wasCancelled;
            if (!isCancelled)
            {
                databaseName = databaseNameTxt.getText();
                if (StringUtils.isEmpty(databaseName))
                {
                    isCancelled = true;
                    
                } else if (drivers.getSelectedIndex() > -1)
                {
                    dbDriver = (DatabaseDriverInfo)drivers.getSelectedItem();
                } else
                {
                    isCancelled = true;
                }
            }
            setVisible(false);
            
            if (!isCancelled)
            {
                try
                {
                    dbUser.first  = dbUsernameTxtFld.getText();
                    dbUser.second = new String(dbPasswdTxtFld.getPassword());
                    
                    saUser.first  = saUsernameTxtFld.getText();
                    saUser.second = new String(saPasswdTxtFld.getPassword());
                    
                    cmUser.first  = usrUsernameTxtFld.getText();
                    cmUser.second = new String(usrPasswdTxtFld.getPassword());
                    
                    bldSampleDatabase.startBuild(databaseName, 
                                                 dbDriver.getName(), 
                                                 dbUser, 
                                                 saUser, 
                                                 cmUser, 
                                                 getSelectedColleectionChoices());
                    dispose();
                    
                } catch (Exception ex)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SetUpBuildDlg.class, ex);
                    ex.printStackTrace();
                }
            } else
            {
                System.exit(0);
            }
            wasClosed = true;

        }
    }
    
    class DisciplineSetupModel extends DefaultTableModel
    {
        protected String[] titles = null;
        
        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getColumnCount()
         */
        @Override
        public int getColumnCount()
        {
            if (titles == null)
            {
                titles = new String[] {" ", 
                                       "Name", 
                                       "Catalog Numbering", 
                                       "Catalog Group", 
                                       "Accession Numbering", 
                                       "Accession Group"};
            }
            return titles.length;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getColumnName(int)
         */
        @Override
        public String getColumnName(int column)
        {
            return titles[column];
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getRowCount()
         */
        @Override
        public int getRowCount()
        {
            return collChoiceList.size();
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getValueAt(int, int)
         */
        @Override
        public Object getValueAt(int row, int column)
        {
            CollectionChoice choice = collChoiceList.get(row);
            switch (column)
            {
                case 0 : 
                    return choice.isSelected;
                    
                case 1 : 
                    return choice.toString();
                    
                case 2 : 
                    return choice.getCatalogNumberingFmtName();
                    
                case 3 : 
                    return choice.getCatNumGroup();
                    
                case 4 : 
                    return choice.getAccessionNumberingFmtName();
                    
                case 5 : 
                    return choice.getAccNumGroup();
            }
            return null;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
         */
        @Override
        public boolean isCellEditable(int row, int column)
        {
            return column != 1;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
         */
        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            switch (columnIndex)
            {
                case 0 : return Boolean.class;
                case 1 : return String.class;
                case 2 : return String.class;
                case 3 : return String.class;
                case 4 : return String.class;
                case 5 : return String.class;
            }
            return null;
        }
        
        protected void fixCatNumsForGroup(final String groupName, final String fmtName)
        {
            for (CollectionChoice choice : collChoiceList)
            {
                if (choice.getCatNumGroup().equals(groupName))
                {
                    choice.setCatalogNumberingFmtName(fmtName);
                }
            }
        }

        protected void fixAccNumsForGroup(final String groupName, final String fmtName)
        {
            for (CollectionChoice choice : collChoiceList)
            {
                if (choice.getAccNumGroup().equals(groupName))
                {
                    choice.setAccessionNumberingFmtName(fmtName);
                }
            }
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#setValueAt(java.lang.Object, int, int)
         */
        @Override
        public void setValueAt(Object value, int row, int column)
        {
            CollectionChoice choice = collChoiceList.get(row);
            switch (column)
            {
                case 0 : 
                    choice.isSelected = (Boolean)value;
                    break;
                    
                case 1 :
                    break;
                    
                case 2 :
                    choice.setCatalogNumberingFmtName(((UIFieldFormatterIFace)value).getName());
                    if (choice.getCatNumberFormatter().getAutoNumber() == null)
                    {
                        choice.setCatNumGroup(catNumGrpList.get(0));
                    }
                    fixCatNumsForGroup(choice.getCatNumGroup(), choice.getCatalogNumberingFmtName());
                    break;
                    
                case 3 :
                    choice.setCatNumGroup((String)value);
                    fixCatNumsForGroup(choice.getCatNumGroup(), choice.getCatalogNumberingFmtName());
                    break;
                    
                case 4 :
                    choice.setAccessionNumberingFmtName(((UIFieldFormatterIFace)value).getName());
                    if (choice.getAccNumberFormatter().getAutoNumber() == null)
                    {
                        choice.setAccNumGroup(accNumGrpList.get(0));
                    }
                    fixCatNumsForGroup(choice.getCatNumGroup(), choice.getCatalogNumberingFmtName());
                    break;

                case 5 :
                    choice.setAccNumGroup((String)value);
                    fixAccNumsForGroup(choice.getAccNumGroup(), choice.getAccessionNumberingFmtName());
                    break;

            }
        }
        
        public void fireChanged()
        {
            super.fireTableDataChanged();
        }
    }
    
    /**
     * @param table
     */
    public static void calcColumnWidths(JTable table)
    {
        JTableHeader header = table.getTableHeader();

        TableCellRenderer defaultHeaderRenderer = null;

        if (header != null)
        {
            defaultHeaderRenderer = header.getDefaultRenderer();
        }

        TableColumnModel columns = table.getColumnModel();
        TableModel data = table.getModel();

        int margin = columns.getColumnMargin(); // only JDK1.3

        int rowCount = data.getRowCount();

        int totalWidth = 0;

        for (int i = columns.getColumnCount() - 1; i >= 0; --i)
        {
            TableColumn column = columns.getColumn(i);

            int columnIndex = column.getModelIndex();

            int width = -1;

            TableCellRenderer h = column.getHeaderRenderer();

            if (h == null)
                h = defaultHeaderRenderer;

            if (h != null) // Not explicitly impossible
            {
                Component c = h.getTableCellRendererComponent
                       (table, column.getHeaderValue(),
                        false, false, -1, i);

                width = c.getPreferredSize().width;
            }

            for (int row = rowCount - 1; row >= 0; --row)
            {
                TableCellRenderer r = table.getCellRenderer(row, i);

                Component c = r.getTableCellRendererComponent(table,
                                                    data.getValueAt(row, columnIndex),
                                                    false, false, row, i);

                    width = Math.max(width, c.getPreferredSize().width+10); // adding an arbitray 10 pixels to make it look nicer
            }

            if (width >= 0)
            {
                column.setPreferredWidth(width + margin); // <1.3: without margin
            }
            else
            {
                // ???
            }

            totalWidth += column.getPreferredWidth();
        }
    }
    
    public class MyComboBoxStringEditor extends DefaultCellEditor 
    {
        public MyComboBoxStringEditor(final Vector<?> items) 
        {
            super(new JComboBox(items));
        }
    }
    
    public class MyComboBoxEditor extends DefaultCellEditor 
    {
        protected Hashtable<String, UIFieldFormatterIFace> hash;
        
        public MyComboBoxEditor(final Vector<?> items, final Hashtable<String, UIFieldFormatterIFace> hash) 
        {
            super(new JComboBox(items));
            //setOpaque(true);
            this.hash = hash;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table,
                                                     Object value,
                                                     boolean isSelected,
                                                     int row,
                                                     int column)
        {
            JComboBox cbx =  (JComboBox)super.getTableCellEditorComponent(table, value, isSelected, row, column);
            UIFieldFormatterIFace fmt = hash.get(value);
            cbx.setSelectedItem(fmt);
            return cbx;
        }
    }
    
    public class MyLabelRenderer extends DefaultTableCellRenderer
    {
        protected Hashtable<String, UIFieldFormatterIFace> hash;
        
        public MyLabelRenderer(final Hashtable<String, UIFieldFormatterIFace> hash) 
        {
            //setOpaque(true);
            this.hash = hash;
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column)
        {
            JLabel                label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            UIFieldFormatterIFace fmt   = hash.get(value);
            if (fmt != null)
            {
                label.setText(fmt.getTitle());
            }
            return label;
        }
    }
    
    public class MyComboBoxRenderer extends JComboBox implements TableCellRenderer 
    {
        protected Hashtable<String, UIFieldFormatterIFace> hash;
        
        public MyComboBoxRenderer(final Vector<?> items, 
                                  final Hashtable<String, UIFieldFormatterIFace> hash) 
        {
            super(items);
            setOpaque(true);
            this.hash = hash;
        }
    
        public Component getTableCellRendererComponent(JTable table, 
                                                       Object value,
                                                       boolean isSelected, 
                                                       boolean hasFocus, 
                                                       int row, 
                                                       int column) 
        {
            setEnabled(isSelected);
            
            if (isSelected) 
            {
                //setForeground(table.getSelectionForeground());
                setForeground(Color.BLACK);
                setBackground(table.getSelectionBackground());
                
            } else 
            {
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }
    
            // Select the current value
            UIFieldFormatterIFace fmt = hash.get(value);
            setSelectedItem(fmt);
            return this;
        }

    }


}
