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
package edu.ku.brc.specify.utilapps.sp5utils;

import static edu.ku.brc.helpers.XMLHelper.getAttr;
import static edu.ku.brc.helpers.XMLHelper.getConfigDir;
import static edu.ku.brc.helpers.XMLHelper.readFileToDOM4J;
import static edu.ku.brc.specify.utilapps.sp5utils.FormInfo.getUniqueKey;
import static edu.ku.brc.ui.UIHelper.calcColumnWidths;
import static edu.ku.brc.ui.UIHelper.centerAndShow;
import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIHelper.createCheckBox;
import static edu.ku.brc.ui.UIHelper.createComboBox;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.createScrollPane;
import static edu.ku.brc.ui.UIHelper.createTextArea;
import static edu.ku.brc.ui.UIHelper.createTextField;
import static edu.ku.brc.ui.UIHelper.tryLogin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.DesertBlue;
import com.thoughtworks.xstream.XStream;

import edu.ku.brc.af.core.FrameworkAppIFace;
import edu.ku.brc.af.core.MacOSAppHandler;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBRelationshipInfo;
import edu.ku.brc.af.core.db.DBTableChildIFace;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.conversion.SpecifyDBConverter;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;
import edu.ku.brc.util.Triple;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Aug 26, 2007
 *
 */
public class Sp5Forms extends JFrame implements FrameworkAppIFace
{
    protected static final Logger log = Logger.getLogger(Sp5Forms.class);
    
    protected String                    hostName;
    protected Pair<String, String>      itUsrPwd;
    protected Vector<FormInfo>          forms         = new Vector<FormInfo>();
    protected Vector<String>            missingFields = new Vector<String>();
    protected HashMap<String, FormInfo> formHash      = new HashMap<String, FormInfo>();
    
    protected HashMap<String, TableInfoMapping> tblNameOldToNewHash   = new HashMap<String, TableInfoMapping>();
    protected HashMap<String, TableInfoMapping> tblNameNewToOldHash   = new HashMap<String, TableInfoMapping>();
    
    protected JList                     formsList;
    protected JList                     missingFieldsList;
    protected FieldCellModel            fieldsTableModel;
    
    protected JTable                    formsTable;
    protected JTable                    fieldsTable;
    protected FormInfo                  selectedForm = null;
    
    protected JPanel                    mainPanel;
    protected JFrame                    formFrame = null;
    protected JButton                   reportBtn;
    protected JButton                   schemaBtn;
    protected JButton                   showBtn;
    
    protected Sp6FieldComboBoxEditor    sp6FieldEditor;
    protected Sp6FieldComboBoxRenderer  sp6FieldRenderer;
    
    protected boolean                   hasChanged = false;
        
    /**
     * 
     */
    public Sp5Forms()
    {
        
    }
    
    /**
     * 
     */
    protected void createUI()
    {
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("f:p:g,p,p:g", "p,2px,f:p:g,4px,p,2px,p,10px,p"));

        missingFieldsList = new JList(new DefaultListModel());
        
        formsTable = new JTable(new FormCellModel(forms));
        formsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (formsTable.getSelectedRow() > -1)
                {
                    formSelected();
                }
            }
        });
        
        fieldsTable = new JTable(fieldsTableModel = new FieldCellModel());
        
        TableColumn col = fieldsTable.getColumnModel().getColumn(1);
        col.setCellEditor(sp6FieldEditor = new Sp6FieldComboBoxEditor());
        
        // If the cell should appear like a combobox in its
        // non-editing state, also set the combobox renderer
        //col.setCellRenderer(sp6FieldRenderer = new Sp6FieldComboBoxRenderer());

        
        calcColumnWidths(formsTable);
        
        PanelBuilder pbBtn = new PanelBuilder(new FormLayout("f:p:g,p,f:p:g,p,f:p:g,p,f:p:g", "p"));
        reportBtn = createButton("Report");
        showBtn   = createButton("Show");
        schemaBtn = createButton("Schema");
        pbBtn.add(reportBtn, cc.xy(2, 1));
        pbBtn.add(schemaBtn, cc.xy(4, 1));
        pbBtn.add(showBtn, cc.xy(6, 1));
        
        pb.add(createLabel("Forms", SwingConstants.CENTER),          cc.xy(1, 1));
        pb.add(createLabel("Missing Fields", SwingConstants.CENTER), cc.xy(3, 1));
        
        pb.add(createScrollPane(formsTable),        cc.xy(1,  3));
        pb.add(createScrollPane(missingFieldsList), cc.xy(3,  3));
        
        pb.add(createLabel("Form Fields", SwingConstants.CENTER), cc.xyw(1, 5, 3));
        pb.add(createScrollPane(fieldsTable),                     cc.xyw(1, 7, 3));
        
        pb.add(pbBtn.getPanel(),                    cc.xyw(1, 9, 3));
        pb.setDefaultDialogBorder();
       
        setContentPane(new JScrollPane(pb.getPanel()));
        
        showBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                showForm();
            }
        });
        schemaBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                doSchema();
            }
        });
    }
    
    protected void doSchema()
    {
        SchemaLocaleUpdater dlg = new SchemaLocaleUpdater(selectedForm);
        dlg.createUI();
        dlg.setSize(800, 550);
        dlg.pack();
        UIHelper.centerAndShow(dlg);
    }
    
    /**
     * 
     */
    protected void formSelected()
    {
        selectedForm = forms.get(formsTable.getSelectedRow());
        
        // Missing Fields List
        DefaultListModel model = (DefaultListModel)missingFieldsList.getModel();
        model.clear();
        for (FormFieldInfo fi : selectedForm.getFields())
        {
            if (fi.getSp6FieldName() == null)
            {
                model.addElement(fi.getSp5FieldName());
            }
        }

        DefaultComboBoxModel cbxModel = (DefaultComboBoxModel)((JComboBox)sp6FieldEditor.getComponent()).getModel();
        cbxModel.removeAllElements();
        cbxModel.addElement("");
        
        // Setup Sp6 Fields
        DBTableInfo tblInfo = selectedForm.getTblInfo();
        if (tblInfo != null)
        {
            for (DBFieldInfo fldInfo : tblInfo.getFields())
            {
                if (StringUtils.isNotEmpty(fldInfo.getColumn()))
                {
                    cbxModel.addElement(fldInfo.getColumn());
                }
            }
            for (DBRelationshipInfo relInfo : tblInfo.getRelationships())
            {
                if (StringUtils.isNotEmpty(relInfo.getColName()))
                {
                    cbxModel.addElement(relInfo.getColName());
                }
            }
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run()
            {
                fieldsTableModel.setSelectedForm(selectedForm);
                fieldsTableModel.fireTableDataChanged();
            }
        });

    }
    
    /**
     * @param fi
     * @return
     */
    protected Triple<JPanel, Integer, Integer> createPanel(final FormInfo formInfo)
    {
    	CellConstraints cc = new CellConstraints();
    	
        JPanel panel      = new JPanel(null);
        int maxWidth      = 0;
        int maxHeight     = 0;
        int maxCellWidth  = 0;
        int maxCellHeight = 0;
        for (FormFieldInfo fi : formInfo.getFields())
        {
            System.out.println(fi.getCaption());
            boolean    addLbl = true;
            JComponent comp   = null;
            switch (fi.getControlTypeNum())
            {
                case 4 : comp = createComboBox(); // 'Picklist'
                    break;
                    
                case 5 : 
                    {
                        JComboBox cbx = createComboBox(); //new ValComboBoxFromQuery(DBTableIdMgr.getInstance().getInfoById(1), "catalogNumber","CatalogNumber","CatalogNumber"," "," "," "," "," ",ValComboBoxFromQuery.CREATE_ALL);// 'QueryCombo'
                        cbx.setEditable(true);
                        cbx.getEditor().setItem(fi.getCaption());
                        JPanel cPanel = new JPanel(new BorderLayout());
                        cPanel.add(cbx, BorderLayout.CENTER);
                        cPanel.add(createElipseBtn(), BorderLayout.EAST);
                        comp   = cPanel;
                        addLbl = false;
                        break;
                    }
                    
                case 7 : 
                    {
                        String   uniqueKey = getUniqueKey(fi.getRelatedTableName(), "Embedded", fi.getParent().getFormType());
                        FormInfo subForm   = formHash.get(uniqueKey);
                        if (subForm == null)
                        {
                            uniqueKey = getUniqueKey(fi.getRelatedTableName(), "Embedded", null);
                            subForm   = formHash.get(uniqueKey);
                        }
                        if (subForm != null)
                        {
                            Vector<String> headers = new Vector<String>();
                            for (int i=0;i<subForm.getFields().size();i++)
                            {
                                headers.add(subForm.getFields().get(i).getCaption());
                            }
                            JPanel p = new JPanel(new BorderLayout());
                            p.add(UIHelper.createScrollPane(new JTable(new Vector<Vector<Object>>(), headers)), BorderLayout.CENTER); // 'Grid'
                            comp = p;
                            addLbl = false;
                        }
                        break;
                    }
                    
                case 8 : // 'EmbeddedForm'
                {
                    String   uniqueKey = getUniqueKey(fi.getRelatedTableName(), "Embedded", fi.getParent().getFormType());
                    FormInfo subForm   = formHash.get(uniqueKey);
                    if (subForm == null)
                    {
                        uniqueKey = getUniqueKey(fi.getRelatedTableName(), "Embedded", null);
                        subForm   = formHash.get(uniqueKey);
                    }
                    comp = (subForm != null ? createPanel(subForm).first : new JPanel());
                    addLbl = fi.getControlTypeNum() != 8;
                    break;
                }
                case 9 : 
                    {
                        comp = createElipseBtn();
                        break;
                    }
                    
                case 20 : comp = createScrollPane(createTextArea()); // 'Memo'
                    break;
                    
                case 21 : comp = null;//createComboBox(); // 'MenuItem'
                    break;
                    
                case 46 : comp = createTextField("URL"); // 'URL'
                    break;
                    
                default:
                    if (fi.getDataTypeNum() == 4)
                    {
                        comp = createCheckBox(" ");
                    } else
                    {
                        comp = createTextField();
                    }
            } // switch
            
            if (comp != null)
            {
                String toolTip = "Field: "+fi.getSp5FieldName() + (StringUtils.isNotEmpty(fi.getSp6FieldName()) && fi.getSp6FieldName().equalsIgnoreCase(fi.getSp5FieldName()) ? " Sp6: "+fi.getSp6FieldName() : "");
                comp.setToolTipText(toolTip);
                
                PanelBuilder pb = new PanelBuilder(new FormLayout("p,1px,f:p:g", "f:p:g,p,f:p:g"));
                
                pb.getPanel().setToolTipText(toolTip);
                pb.getPanel().setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                if (addLbl)
                {
                    pb.add(createLabel(fi.getCaption()), cc.xy(1,2));
                }
                pb.add(comp, cc.xy(3,2));
                panel.add(pb.getPanel());
                
                maxWidth  = Math.max(maxWidth, fi.getLeft()+fi.getWidth());
                maxHeight = Math.max(maxHeight, fi.getTop()+fi.getHeight());
                
                maxCellWidth  = Math.max(maxWidth, fi.getCellX()+fi.getCellWidth());
                maxCellHeight = Math.max(maxHeight, fi.getCellY()+fi.getCellHeight());
                
                boolean newWay = false;
                if (newWay)
                {
                    Rectangle r = fi.getBoundsFromCellDim();
                    pb.getPanel().setLocation(r.x, r.y);
                    pb.getPanel().setSize(r.width, r.height);
                    
                } else
                {
                    pb.getPanel().setLocation(fi.getLeft(), fi.getTop());
                    pb.getPanel().setSize(fi.getWidth(), fi.getHeight());
                }
                
                System.out.println("MaxW: "+maxWidth+"  "+maxCellWidth);
                System.out.println("MaxH: "+maxHeight+"  "+maxCellHeight);
            }
        }
        
        boolean newWay = false;
        if (newWay)
        {
            int cw = FormFieldInfo.getSegWidth();
            panel.setPreferredSize(new Dimension(maxCellWidth*cw, maxCellHeight*cw));
            panel.setSize(new Dimension(maxCellWidth*cw, maxCellHeight*cw));
        } else
        {
            panel.setPreferredSize(new Dimension(maxWidth, maxHeight));
            panel.setSize(new Dimension(maxWidth, maxHeight));
        }
        
        System.out.println("MaxW: "+maxWidth+"  "+maxCellWidth);
        System.out.println("MaxH: "+maxHeight+"  "+maxCellHeight);
        
        return new Triple<JPanel, Integer, Integer>(panel, maxWidth, maxHeight);
    }
    
    /**
     * @return
     */
    protected JPanel createElipseBtn()
    {
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("f:p:g,p,f:p:g", "f:p:g,p,f:p:g"));
        JButton btn = new JButton("...");
        btn.setMargin(new Insets(2,4,2,4));
        btn.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        
        pb.add(btn, cc.xy(2, 2));
        return pb.getPanel();
    }
    
    /**
     * 
     */
    protected void showForm()
    {
        if (selectedForm != null)
        {
            if (formFrame != null)
            {
                formFrame.setVisible(false);
                formFrame.dispose();
            }
            
            formFrame = new JFrame();
            
            Triple<JPanel, Integer, Integer> triple = createPanel(selectedForm);
            JPanel panel = triple.first;
            
            formFrame.setContentPane(panel);
            
            panel.addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e)
                {
                    //System.out.println(e.getPoint());
                }
            });
            
            formFrame.setVisible(true);
            formFrame.setSize(new Dimension(triple.second+10, triple.third+25));
        }
    }
    
    /**
     * @return
     */
    protected String getFormQuery()
    {
        File file = getConfigDir("../demo_files/Sp5ConvInfo.xml");
        try
        {
            Element root = readFileToDOM4J(file);
            if (root != null)
            {
                for (Object tblObj : root.selectNodes("mappings/table")) //$NON-NLS-1$
                {
                    Element tblNode = (Element)tblObj;
                    TableInfoMapping tblInfoMap = new TableInfoMapping(getAttr(tblNode, "newname", null), getAttr(tblNode, "oldname", null));
                    
                    for (Object fldObj : tblNode.selectNodes("field")) //$NON-NLS-1$
                    {
                        Element fldNode = (Element)fldObj;
                        tblInfoMap.addFieldMapping(getAttr(fldNode, "newname", null), getAttr(fldNode, "oldname", null));
                    }
                    
                    tblNameOldToNewHash.put(tblInfoMap.getOldTableName().toLowerCase(), tblInfoMap);
                    tblNameNewToOldHash.put(tblInfoMap.getNewTableName().toLowerCase(), tblInfoMap);
                    
                    for (DBTableInfo ti : DBTableIdMgr.getInstance().getTables())
                    {
                        if (tblNameNewToOldHash.get(ti.getName()) == null)
                        {
                            tblNameNewToOldHash.put(ti.getName(), new TableInfoMapping(ti.getName(), ti.getName()));
                        }
                    }
                }
                
                // query[@name='Form']
                for (Object qryObj : root.selectNodes("queries/query")) //$NON-NLS-1$
                {
                    Element queryNode = (Element)qryObj;                
                    System.out.println(queryNode.getText());
                    return queryNode.getText();
                }

            }
        } catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * @param str
     * @return
     */
    protected String getStr(final Object str)
    {
        return str == null ? "" : str.toString();
    }
    
    /**
     * 
     */
    protected void process()
    {
        forms.clear();
        
        Connection connection = DBConnection.getInstance().createConnection();
     
        String sql = getFormQuery();
        if (sql != null)
        {
            Vector<Object[]> rows = BasicSQLUtils.query(connection, sql);
            
            DBTableInfo tblInfo    = null;
            FormInfo    formInfo   = null;
            String      curTblName = "";
            
            for (Object[] row : rows)
            {
                String tableName = row[0].toString();
                String uniqueKey = getUniqueKey(tableName, getStr(row[1]), getStr(row[12]));
                if (!curTblName.equals(uniqueKey))
                {
                    tblInfo = getTableInfo(tableName);

                    formInfo = new FormInfo(tableName, getStr(row[1]), getStr(row[12]), tblInfo);
                    forms.add(formInfo);
                    curTblName = uniqueKey;
                    formHash.put(uniqueKey, formInfo);
                    System.out.println(uniqueKey);
                }
                
                String sp5FieldName     = getStr(row[2]);
                String sp6FieldName     = null; 
                String caption          = getStr(row[3]);
                String controlType      = getStr(row[4]);
                String datatype         = getStr(row[5]);
                String relatedTableName = getStr(row[6]);
                Integer left            = (Integer)row[7];
                Integer top             = (Integer)row[8]; 
                Integer width           = (Integer)row[9]; 
                Integer height          = (Integer)row[10]; 
                
                Integer dataTypeNum     = (Integer)row[16]; 
                Integer controlTypeNum  = (Integer)row[17]; 
                
                FormFieldInfo fld = new FormFieldInfo(sp5FieldName, sp6FieldName, caption,
                                                controlType, datatype, relatedTableName, top, left,
                                                width, height, controlTypeNum, dataTypeNum);
                
                if (tblInfo != null)
                {
                    DBTableChildIFace childInfo = getFieldInfo(tblInfo, fld);
                    if (childInfo != null)
                    {
                        if (childInfo instanceof DBFieldInfo)
                        {
                            sp6FieldName = ((DBFieldInfo)childInfo).getColumn();
                        } else
                        {
                            sp6FieldName = ((DBRelationshipInfo)childInfo).getName(); 
                        }
                        fld.setSp6FieldName(sp6FieldName);
                    }
                }

                fld.setParent(formInfo);
                formInfo.getFields().add(fld);
            }
        }
        
        try
        {
            connection.close();
        } catch (SQLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    /**
     * @param tblName
     * @return
     */
    protected DBTableInfo getTableInfo(final String oldTblName)
    {
        TableInfoMapping timp = tblNameOldToNewHash.get(oldTblName.toLowerCase());
        String tblName = timp == null ? oldTblName : timp.getNewTableName();
        
        DBTableIdMgr mgr = DBTableIdMgr.getInstance();
        
        DBTableInfo tblInfo = mgr.getByShortClassName(tblName);
        if (tblInfo == null)
        {
            TableInfoMapping tblMapping = tblNameOldToNewHash.get(tblName);
            if (tblMapping != null)
            {
                tblInfo = mgr.getByShortClassName(tblMapping.getNewTableName());
                if (tblInfo == null)
                {
                    log.error("getTableInfo - Old Table Name: "+tblName+" was not found in DBTableIdMgr");
                }
            } else
            {
                log.error("getTableInfo - Old Table Name: "+tblName+" was not mapped in Hash.");
            }
        }
        
        return tblInfo;
    }
    
    /**
     * @param tblInfo
     * @param fieldName
     * @return
     */
    protected DBTableChildIFace getFieldInfo(final DBTableInfo tblInfo,
                                             final FormFieldInfo   fieldInfo)
    {
        String oldFieldName = fieldInfo.getSp5FieldName();
        if (fieldInfo.getDataTypeNum() == 6 || // ManyToOne
            fieldInfo.getDataTypeNum() == 7 || // OneToOne
            fieldInfo.getDataTypeNum() == 8)   // OneToMany
        {
            oldFieldName = fieldInfo.getRelatedTableName();
            DBTableChildIFace childInfo = getFieldInfoInternal(tblInfo, fieldInfo, oldFieldName);
            if (childInfo == null && oldFieldName.endsWith("s"))
            {
                oldFieldName = oldFieldName.substring(0, oldFieldName.length()-1);
                return getFieldInfoInternal(tblInfo, fieldInfo, oldFieldName);
            }
            return childInfo;
        }
        return getFieldInfoInternal(tblInfo, fieldInfo, oldFieldName);
    }
    
    /**
     * @param tblInfo
     * @param fieldName
     * @return
     */
    protected DBTableChildIFace getFieldInfoInternal(final DBTableInfo tblInfo,
                                                     @SuppressWarnings("unused") final FormFieldInfo   fieldInfo,
                                                     final String oldFieldName)
    {
        DBTableChildIFace childInfo = null;
        if (tblInfo != null)
        {
            childInfo = tblInfo.getItemByName(oldFieldName);
            if (childInfo == null)
            {
                TableInfoMapping tblMapping = tblNameNewToOldHash.get(tblInfo.getName());
                if (tblMapping != null)
                {
                    String newChildName = tblMapping.getNewName(oldFieldName);
                    if (newChildName != null)
                    {
                        childInfo = tblInfo.getItemByName(newChildName);
                        if (childInfo == null)
                        {
                            log.error("getFieldInfo - New Child Name that was mapped wasn't found in DBTableInfo: ["+newChildName + "] tbl: [" + tblInfo.getName()+"]");
                        }
                    } else
                    {
                        log.error("getFieldInfo - Old Child Name wasn't mapped: ["+oldFieldName + "] tbl: [" + tblInfo.getName()+"]");
                    }
                } else
                {
                    log.error("getFieldInfo - New Table Name: "+tblInfo.getName()+" was not mapped in Hash.");
                }
            }
        }
        return childInfo;
    }
    
    /**
     * 
     */
    protected void startup()
    {
        Pair<String, String> namePair = null;
        try
        {
            final SpecifyDBConverter converter = new  SpecifyDBConverter();
            if (converter.selectedDBsToConvert(true))
            {
                namePair = converter.chooseTable("Select a DB for Forms", true);
                hostName = converter.getHostName();
                itUsrPwd = converter.getItUsrPwd();
            }
               
        } catch (SQLException ex)
        {
            ex.printStackTrace();
            JOptionPane.showConfirmDialog(null, "The Converter was unable to login.", "Error", JOptionPane.CLOSED_OPTION);
        }
        
        if (namePair != null)
        {
            DatabaseDriverInfo driverInfo = DatabaseDriverInfo.getDriver("MySQL");
            String oldConnStr = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, hostName, namePair.second, itUsrPwd.first, itUsrPwd.second, driverInfo.getName());
            
            // This will log us in and return true/false
            // This will connect without specifying a DB, which allows us to create the DB
            if (!tryLogin(driverInfo.getDriverClassName(), 
                    driverInfo.getDialectClassName(), 
                    namePair.second, 
                    oldConnStr,
                    itUsrPwd.first, 
                    itUsrPwd.second))
            {
                log.error("Failed connection string: "  +driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, hostName, namePair.second, itUsrPwd.first, itUsrPwd.second, driverInfo.getName()) );
                throw new RuntimeException("Couldn't login into ["+namePair.second+"] "+DBConnection.getInstance().getErrorMsg());
            }
            
            process();
            
        } else
        {
            JOptionPane.showConfirmDialog(null, "The Converter was unable to login.", "Error", JOptionPane.CLOSED_OPTION);
            System.exit(0);
        }
    }
    
    protected static DBConnection getNewDBConnection()
    {
        Pair<String, String> namePair = null;
        Pair<String, String> usrNmPwd = null; 
        final SpecifyDBConverter converter = new  SpecifyDBConverter();
        try
        {
           
            if (converter.selectedDBsToConvert(true))
            {
                namePair = converter.chooseTable("Select Specify 6 Database", false);
                usrNmPwd = converter.getItUsrPwd();
            }
               
        } catch (SQLException ex)
        {
            ex.printStackTrace();
            JOptionPane.showConfirmDialog(null, "The Sp5Forms was unable to login.", "Error", JOptionPane.CLOSED_OPTION);
        }
        
        if (namePair != null)
        {
            DatabaseDriverInfo driverInfo = DatabaseDriverInfo.getDriver("MySQL");
            String oldConnStr = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, converter.getHostName(), namePair.second, usrNmPwd.first, usrNmPwd.second, driverInfo.getName());
            
            DBConnection dbConn = DBConnection.createInstance(driverInfo.getDriverClassName(), 
                    driverInfo.getDialectClassName(), 
                    namePair.second, 
                    oldConnStr,
                    usrNmPwd.first, 
                    usrNmPwd.second);

            Connection connection = dbConn.createConnection();
            if (connection != null)
            {
                try
                {
                    connection.close();
                    return dbConn;
                    
                } catch (SQLException ex)
                {
                    // do nothing
                }
            }
        }
        return null;
    }
    
    private void openDB()
    {
        startup(); 
        
        formsTable.setModel(new FormCellModel(forms));
    }
    
    @SuppressWarnings({ "unchecked"})
    private void openXML()
    {
        FileDialog fileDlg = new FileDialog((Frame)UIRegistry.getTopWindow(), "", FileDialog.LOAD);
        
        fileDlg.setFilenameFilter(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name)
            {
                return name.toLowerCase().endsWith(".xml");
            }
        });
        fileDlg.setVisible(true);
        
        String fileName = fileDlg.getFile();
        if (fileName != null)
        {
            File    iFile   = new File(fileDlg.getDirectory() + File.separator + fileName);
            
            XStream xstream = new XStream();
            FormInfo.configXStream(xstream);
            FormFieldInfo.configXStream(xstream);
            
            try
            {
                forms = (Vector<FormInfo>)xstream.fromXML(FileUtils.openInputStream(iFile));
                formsTable.setModel(new FormCellModel(forms));
                
            } catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
        
    private void saveXML()
    {
        XStream xstream = new XStream();
        FormInfo.configXStream(xstream);
        FormFieldInfo.configXStream(xstream);
        
        try
        {
            String dbName = DBConnection.getInstance().getDatabaseName() + ".xml";
            PrintWriter pw = new PrintWriter(new File(dbName));
            xstream.toXML(forms, pw);
            
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    //----------------------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.FrameworkAppIFace#doAbout()
     */
    @Override
    public void doAbout()
    {
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.FrameworkAppIFace#doExit(boolean)
     */
    @Override
    public boolean doExit(boolean doAppExit)
    {
        System.exit(0);
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.FrameworkAppIFace#doPreferences()
     */
    @Override
    public void doPreferences()
    {
    }
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        
     // Set App Name, MUST be done very first thing!
        UIRegistry.setAppName("Specify");  //$NON-NLS-1$

        log.debug("********* Current ["+(new File(".").getAbsolutePath())+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        
        UIRegistry.setEmbeddedDBPath(UIRegistry.getDefaultEmbeddedDBPath()); // on the local machine
        
        for (String s : args)
        {
            String[] pairs = s.split("="); //$NON-NLS-1$
            if (pairs.length == 2)
            {
                if (pairs[0].startsWith("-D")) //$NON-NLS-1$
                {
                    //System.err.println("["+pairs[0].substring(2, pairs[0].length())+"]["+pairs[1]+"]");
                    System.setProperty(pairs[0].substring(2, pairs[0].length()), pairs[1]);
                } 
            } else
            {
                String symbol = pairs[0].substring(2, pairs[0].length());
                //System.err.println("["+symbol+"]");
                System.setProperty(symbol, symbol);
            }
        }
        
        // Now check the System Properties
        String appDir = System.getProperty("appdir");
        if (StringUtils.isNotEmpty(appDir))
        {
            UIRegistry.setDefaultWorkingPath(appDir);
        }
        
        String appdatadir = System.getProperty("appdatadir");
        if (StringUtils.isNotEmpty(appdatadir))
        {
            UIRegistry.setBaseAppDataDir(appdatadir);
        }
        
        
        Logger logger = LogManager.getLogger("edu.ku.brc");
        if (logger != null)
        {
            logger.setLevel(Level.ALL);
            System.out.println("Setting "+ logger.getName() + " to " + logger.getLevel());
        }
        
        logger = LogManager.getLogger(edu.ku.brc.dbsupport.HibernateUtil.class);
        if (logger != null)
        {
            logger.setLevel(Level.INFO);
            System.out.println("Setting "+ logger.getName() + " to " + logger.getLevel());
        }
        
        // Create Specify Application
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                try
                {
                    if (!System.getProperty("os.name").equals("Mac OS X"))
                    {
                        UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
                        PlasticLookAndFeel.setPlasticTheme(new DesertBlue());
                    }
                    
                    
                    final Sp5Forms frame = new Sp5Forms();
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    frame.setBounds(0, 0, 800, 800);
                    
                    new MacOSAppHandler(frame);
                    
                    frame.startup();
                    
                    frame.createUI();
                    JMenuBar mb       = new JMenuBar();
                    JMenu    fileMenu = new JMenu("File");
                    
                    JMenuItem openMI = new JMenuItem("Open XML File");
                    fileMenu.add(openMI);
                    openMI.addActionListener(new ActionListener(){
                        @Override
                        public void actionPerformed(ActionEvent e) 
                        {
                            frame.openXML();
                        }
                    });
                    
                    JMenuItem openDBMI = new JMenuItem("Open Database");
                    fileMenu.add(openDBMI);
                    openDBMI.addActionListener(new ActionListener(){
                        @Override
                        public void actionPerformed(ActionEvent e) 
                        {
                            frame.openDB();
                        }
                    });
                    
                    JMenuItem saveMI = new JMenuItem("Save");
                    fileMenu.add(saveMI);
                    saveMI.addActionListener(new ActionListener(){
                        @Override
                        public void actionPerformed(ActionEvent e) 
                        {
                            frame.saveXML();
                        }
                    });
                    
                    if (!UIHelper.isMacOS())
                    {
                        fileMenu.addSeparator();
                        JMenuItem mi = new JMenuItem("Exit");
                        fileMenu.add(mi);
                        
                        
                        mi.addActionListener(new ActionListener(){
            				@Override
            				public void actionPerformed(ActionEvent e) 
            				{
            					System.exit(0);
            				}
                        });
                    }
                    
                    mb.add(fileMenu);
                    frame.setJMenuBar(mb);
                    
                    centerAndShow(frame);
                }
                catch (Exception e)
                {
                    log.error("Can't change L&F: ", e);
                }
            }
        });
    }
    
    
    //------------------------------------------------------------------------------
    public class Sp6FieldComboBoxRenderer extends JComboBox implements TableCellRenderer
    {
        public Sp6FieldComboBoxRenderer()
        {
            super(new DefaultComboBoxModel());
        }

        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column)
        {
            if (isSelected)
            {
                setForeground(table.getSelectionForeground());
                super.setBackground(table.getSelectionBackground());
            } else
            {
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }

            // Select the current value
            setSelectedItem(value);
            return this;
        }
    }

    public class Sp6FieldComboBoxEditor extends DefaultCellEditor
    {
        public Sp6FieldComboBoxEditor()
        {
            super(new JComboBox(new DefaultComboBoxModel()));
        }
    }
}

