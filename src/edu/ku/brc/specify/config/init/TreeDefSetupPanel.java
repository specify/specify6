/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.specify.config.init;

import static edu.ku.brc.helpers.XMLHelper.getAttr;
import static edu.ku.brc.helpers.XMLHelper.getConfigDir;
import static edu.ku.brc.helpers.XMLHelper.readFileToDOM4J;
import static edu.ku.brc.ui.UIHelper.createI18NFormLabel;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.createScrollPane;
import static edu.ku.brc.ui.UIHelper.makeTableHeadersCentered;
import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.showLocalizedMsg;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.thoughtworks.xstream.XStream;

import edu.ku.brc.af.core.SchemaI18NService;
import edu.ku.brc.specify.config.DisciplineType;
import edu.ku.brc.specify.config.DisciplineType.STD_DISCIPLINES;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.datamodel.StorageTreeDef;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.util.Pair;

/**
 * @author rod
 *
 * @code_status Beta
 *
 * Feb 7, 2009
 *
 */
public class TreeDefSetupPanel extends BaseSetupPanel
{
    protected Class<?>                         classType;
    protected String                           classTitle;
    protected JTable                           table;
    protected DefaultTableModel                model;
    protected JComboBox                        directionCBX;
    protected JEditorPane                      fullnameDisplayTxt;
    protected DisciplinePanel                  disciplinePanel;
    
    protected Vector<TreeDefRow>               treeDefList = new Vector<TreeDefRow>();
    
    /**
     * @param classType the class of the TreeDef
     * @param classTitle the already localized title of the actual tree
     * @param panelName the name of the panel
     * @param descKey L10N key to label description above the table
     * @param nextBtn the next button
     * @param dbPanel the 
     */
    public TreeDefSetupPanel(final Class<?>      classType,
                             final String        classTitle,
                             final String        panelName,
                             final String        helpContext,
                             final String        descKey,
                             final JButton       nextBtn,
                             final JButton       prevBtn,
                             final DisciplinePanel disciplinePanel)
    {
        super(panelName, helpContext, nextBtn, prevBtn);
        
        this.classType       = classType;
        this.classTitle      = classTitle;
        this.disciplinePanel = disciplinePanel;
        
        if (classType == TaxonTreeDef.class || 
            classType == GeographyTreeDef.class || 
            classType == StorageTreeDef.class)
        {
            model = new TreeDefTableModel();
             if (classType != TaxonTreeDef.class)
            {
                loadTree(disciplinePanel != null && disciplinePanel.getDisciplineType() != null ? 
                        disciplinePanel.getDisciplineType().getDisciplineType() : null);
            }
            

            
            table              = new JTable(model);
            directionCBX       = UIHelper.createComboBox(new String[] {getResourceString("FORWARD"), getResourceString("REVERSE")});
            fullnameDisplayTxt = new JEditorPane();
            fullnameDisplayTxt.setBackground(Color.WHITE);
            fullnameDisplayTxt.setOpaque(true);
            fullnameDisplayTxt.setContentType( "text/html" );
            fullnameDisplayTxt.setEditable(false);
            
            table.setRowSelectionAllowed(false);
            table.setColumnSelectionAllowed(false);
            //table.setDefaultRenderer(String.class, new BiColorTableCellRenderer(false));

            model.addTableModelListener(new TableModelListener() {
                @Override
                public void tableChanged(TableModelEvent e)
                {
                    updateBtnUI();
                }
            });
            
            JScrollPane     spex = new JScrollPane(fullnameDisplayTxt, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            CellConstraints cc   = new CellConstraints();
            PanelBuilder    pb   = new PanelBuilder(new FormLayout("p,2px,p,250px,f:p:g", "p,4px,min(p;250px),4px,p,4px,65px,2px,p"), this);
            
            JScrollPane sp = createScrollPane(table);
            sp.getViewport().setBackground(Color.WHITE);
            
            String lbl = getLocalizedMessage(descKey, classTitle);
            pb.add(createLabel(lbl, SwingConstants.CENTER), cc.xyw(1, 1, 4));
            pb.add(sp, cc.xyw(1, 3, 5));
            
            pb.add(createI18NFormLabel("DIRECTION"), cc.xy(1, 5));
            pb.add(directionCBX, cc.xy(3, 5));
            
            pb.add(createI18NFormLabel("EXAMPLE"), cc.xy(1, 7));
            pb.add(spex, cc.xywh(3, 7, 2, 3));
            
            makeTableHeadersCentered(table, true);
            
            directionCBX.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    updateBtnUI();
                }
            });
            
            table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e)
                {
                    if (!e.getValueIsAdjusting())
                    {
                        updateBtnUI();

                        int inx = table.getSelectedColumn();
                        if (inx > -1)
                        {
                            TreeDefRow row    = treeDefList.get(table.getSelectedRow());
                            String     msgKey = null;
                            
                            if (row.isRequired() && (inx == 1 || inx == 3))
                            {
                                msgKey = inx == 1 ? "NO_CHANGE_INCL" : "NO_CHANGE_REQ";
                                
                            } else if (inx == 4 && !row.isIncluded())
                            {
                                msgKey = "NO_CHANGE_INFN";
                            }
                            
                            if (msgKey != null)
                            {
                                final String mk = msgKey;
                                SwingUtilities.invokeLater(new Runnable() {
                                    @Override
                                    public void run()
                                    {
                                        showLocalizedMsg(mk);
                                    }
                                });
                            }
                        }
                    }
                }
            });
            
            updateBtnUI();
        }
    }
    
    /**
     * @return the classType
     */
    public Class<?> getClassType()
    {
        return classType;
    }

    /**
     * @param disciplineType
     */
    private void loadTree(final STD_DISCIPLINES disciplineType)
    {
        treeDefList.clear();
        
        DisciplineType dType  = disciplineType == null ? null : DisciplineType.getDiscipline(disciplineType);
        
        String fileName = null;
        if (classType == TaxonTreeDef.class)
        {
            fileName = dType.getFolder()+ File.separator + "taxon_init.xml";
            
        } else if (classType == GeographyTreeDef.class)
        {
            fileName = "common" + File.separator + "geography_init.xml";
            
        } else if (classType == StorageTreeDef.class)
        {
            fileName = "common" + File.separator + "storage_init.xml";
        }
        
        Locale currLocale = SchemaI18NService.getCurrentLocale();
        
        File file = getConfigDir(fileName);
        if (file.exists())
        {
            try
            {
                Element root = readFileToDOM4J(file);
                for (Object levelObj : root.selectNodes("/tree/treedef/level"))
                {
                    Element level        = (Element)levelObj;
                    String  name         = getAttr(level, "name", null);
                    int     rank         = getAttr(level, "rank", -1);
                    boolean enforced     = getAttr(level, "enforced", false);
                    boolean isInFullName = getAttr(level, "infullname", false);

                    String  title        = name;
                    
                    String text = null;
                    for (Object localeObj : level.selectNodes("locale"))
                    {
                        Element locale  = (Element)localeObj;
                        String  lang    = getAttr(locale, "lang", null);
                        String  country = getAttr(locale, "country", null);
                        String  var     = getAttr(locale, "var", null);
                        
                        if (StringUtils.isNotEmpty(lang) && StringUtils.isNotEmpty(currLocale.getLanguage()) && lang.equals(currLocale.getLanguage()))
                        {
                            title = getAttr(locale, "text", null);
                            if (StringUtils.isNotEmpty(country) && StringUtils.isNotEmpty(currLocale.getCountry()) && country.equals(currLocale.getCountry()))
                            {
                                title = getAttr(locale, "text", null);
                                if (StringUtils.isNotEmpty(var) && StringUtils.isNotEmpty(currLocale.getVariant()) && var.equals(currLocale.getVariant()))
                                {
                                    title = getAttr(locale, "text", null);
                                }
                            }
                        }
                    }
                    
                    if (rank > -1)
                    {
                        boolean required = false;
                        if (classType == TaxonTreeDef.class)
                        {
                            required = TaxonTreeDef.isDefaultRequiredLevel(rank) || rank == 0;
                            
                        } else if (classType == GeographyTreeDef.class)
                        {
                            required = GeographyTreeDef.isStdRequiredLevel(rank) || rank == 0;
                        } else
                        {
                            required = rank == 0;
                        }
                        String sep = classType == TaxonTreeDef.class ? " " : ", ";
                        treeDefList.add(new TreeDefRow(name, title, rank, required, enforced, required && isInFullName, required || rank == 0, sep));
                    }
                }
                
                if (model != null)
                {
                    model.fireTableDataChanged();
                }
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#doingNext()
     */
    @Override
    public void doingNext()
    {
        if (disciplinePanel != null)
        {
            loadTree(disciplinePanel.getDisciplineType().getDisciplineType());
        }
        
        updateBtnUI();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#doingPrev()
     */
    @Override
    public void doingPrev()
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#getUIComponent()
     */
    @Override
    public Component getUIComponent()
    {
        return this;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#getValues(java.util.Properties)
     */
    @Override
    public void getValues(final Properties props)
    {
        XStream xstream = new XStream();
        TreeDefRow.configXStream(xstream);
        props.put(classType.getSimpleName()+".treedefs", xstream.toXML(treeDefList));
        
        props.put("treedir."+classType.getSimpleName(), directionCBX.getSelectedIndex() == 0 ? "forward" : "reverse");
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#isUIValid()
     */
    @Override
    public boolean isUIValid()
    {
        boolean isEnforced   = false;
        boolean isInFullName = false;
        for (TreeDefRow row : treeDefList)
        {
            if (row.isEnforced())
            {
                isEnforced = true;
            }
            if (row.isInFullName())
            {
                isInFullName = true;
            }
        }
        return isEnforced && isInFullName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#setValues(java.util.Properties)
     */
    @Override
    public void setValues(Properties values)
    {
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#updateBtnUI()
     */
    @Override
    public void updateBtnUI()
    {
        nextBtn.setEnabled(isUIValid());
        
        String        lastSep = "";
        StringBuilder sb      = new StringBuilder("<html>");
        StringBuilder subbdlr = new StringBuilder();
        for (TreeDefRow row : treeDefList)
        {
            if (row.isInFullName())
            {
                subbdlr.setLength(0);
                subbdlr.append("<i>");
                subbdlr.append(row.getTitle());
                subbdlr.append("</i>");
                subbdlr.append(row.getSeparator());
                lastSep = row.getSeparator();
                
                if (directionCBX.getSelectedIndex() == 0)
                {
                    sb.append(subbdlr.toString());
                } else
                {
                    sb.insert(6, subbdlr.toString());
                }
            }
        }
        
        if (sb.length() > 0)
        {
            sb.setLength(sb.length()-lastSep.length());
        }
        
        if (fullnameDisplayTxt != null)
        {
            fullnameDisplayTxt.setText(sb.toString());
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.SetupPanelIFace#getSummary()
     */
    @Override
    public List<Pair<String, String>> getSummary()
    {
        List<Pair<String, String>> list = new ArrayList<Pair<String, String>>();
        for (int i=0;i<model.getRowCount();i++)
        {
            if (((Boolean)model.getValueAt(i, 1)))
            {
                list.add(new Pair<String, String>(classTitle, model.getValueAt(i, 0).toString()));
            }
        }
        return list;

    }
    
    //--------------------------------------------------------------
    //--
    //--------------------------------------------------------------

    class TreeDefTableModel extends DefaultTableModel
    {
        protected String[] header = null;
        
        /**
         * 
         */
        public TreeDefTableModel()
        {
            header = new String[] {"LEVEL", "INCLUDED", "ISREQ", "ISENFORCED", "ISFULLNAME", "SEPARATOR"};
            for (int i=0;i<header.length;i++)
            {
                header[i] = getResourceString(header[i]);
            }
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getColumnCount()
         */
        @Override
        public int getColumnCount()
        {
            return header != null ? header.length : 0;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getColumnName(int)
         */
        @Override
        public String getColumnName(int column)
        {
            return header[column];
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getRowCount()
         */
        @Override
        public int getRowCount()
        {
            return treeDefList.size();
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getValueAt(int, int)
         */
        @Override
        public Object getValueAt(int row, int column)
        {
            TreeDefRow trd = treeDefList.get(row);
            switch (column)
            {
                case 0: 
                    return trd.getTitle();
                    
                case 1: 
                    return trd.isIncluded();
                    
                case 2: 
                    return trd.isRequired() ? getResourceString("YES")  :  "  ";
                    
                case 3: 
                    return trd.isRequired() ? true : trd.isEnforced();
                    
                case 4: 
                    return trd.isInFullName();
                    
                case 5:
                    return trd.getSeparator();
            }
            return "";
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
         */
        @Override
        public boolean isCellEditable(int row, int column)
        {
            TreeDefRow trd = treeDefList.get(row);
            switch (column)
            {
                case 0: 
                case 2:
                    return false;
                    
                case 1:
                    return !trd.isRequired();
                    
                case 3: 
                    return !trd.isRequired() && trd.isIncluded();
                    
                case 4: 
                    return trd.isIncluded();
                    
                case 5:
                    return trd.isIncluded();
            }
            return false;
        }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#setValueAt(java.lang.Object, int, int)
         */
        @Override
        public void setValueAt(Object value, int row, int column)
        {
            TreeDefRow trd = treeDefList.get(row);
            switch (column)
            {
                case 1: 
                {
                    Boolean isOn = (Boolean)value;
                    trd.setIncluded(isOn);
                    if (!isOn)
                    {
                        trd.setEnforced(false);
                        fireTableCellUpdated(row, 3);

                        trd.setInFullName(false);
                        fireTableCellUpdated(row, 4);
                    }
                    break;
                }
                    
                case 0: 
                case 2:
                    break;
                    
                case 3: 
                    trd.setEnforced((Boolean)value);
                    break;
                    
                case 4: 
                    trd.setInFullName((Boolean)value);
                    break;
                    
                case 5:
                    trd.setSeparator((String)value);
                    break;
            }
            fireTableCellUpdated(row, column);
        }

        /* (non-Javadoc)
         * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
         */
        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            switch (columnIndex)
            {
                case 0: 
                case 2:
                case 5:
                    return String.class;
                    
                case 1: 
                case 3: 
                case 4: 
                    return Boolean.class;
            }
            return String.class;
        }
    }
}
