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
package edu.ku.brc.specify.plugins;

import static edu.ku.brc.ui.UIHelper.createIconBtn;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.ui.DBObjSearchPanel;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.db.PickListDBAdapterFactory;
import edu.ku.brc.ui.db.PickListDBAdapterIFace;
import edu.ku.brc.ui.forms.FormViewObj;
import edu.ku.brc.ui.forms.validation.ValComboBox;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Jul 18, 2007
 *
 */
public class TaxonLabelFormatting extends UIPluginBase
{
    private static final Logger           log      = Logger.getLogger(TaxonLabelFormatting.class);
    
    public enum FormatType {Plain, Italic}

    protected Taxon             taxon = null;
    
    protected ValComboBox       formatCBX;
    protected JList             authorsList;
    protected DBObjSearchPanel  searchPanel;
    protected JLabel            refWorkLabel;
    protected SpecialLabel      specialLabel;
    
    protected JButton           mapToBtn;
    protected JButton           unmapBtn;
    protected JButton           upBtn;
    protected JButton           downBtn;
    protected JButton           newAgentBtn;
    
    protected ResourceBundle    resourceBundle;
    
    protected Vector<TextDrawInfo> drawList = new Vector<TextDrawInfo>();
    
    /**
     * Constructor.
     */
    public TaxonLabelFormatting()
    {
        super();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.UIPluginBase#initialize(java.util.Properties, boolean)
     */
    @Override
    public void initialize(Properties propertiesArg, boolean isViewModeArg)
    {
        super.initialize(propertiesArg, isViewModeArg);
        
        String plName = "TaxonLabelFormatter";
        PickListDBAdapterIFace adapter = PickListDBAdapterFactory.getInstance().create(plName, false);
        if (adapter == null || adapter.getPickList() == null)
        {
            throw new RuntimeException("PickList Adapter ["+plName+"] cannot be null!");
        }
        formatCBX = new ValComboBox(adapter);
        formatCBX.getComboBox().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                doFormatting();
            }
        });
        formatCBX.getComboBox().addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e)
            {
                doFormatting();
            }
        });
        
        newAgentBtn = new JButton(); // Label set when new Resource Bundle is installed (below)
        searchPanel = new DBObjSearchPanel("Search", "AgentNameSearch", "AgentNameSearch", "edu.ku.brc.specify.datamodel.Agent", "agentId", SwingConstants.BOTTOM);
        searchPanel.getScrollPane().setMinimumSize(new Dimension(100, 200));
        searchPanel.getScrollPane().setPreferredSize(new Dimension(100, 150));
        ((FormViewObj)searchPanel.getForm()).getPanel().setBorder(null);
            
        try
        {
            UIRegistry.loadAndPushResourceBundle("specify_plugins");
            
            newAgentBtn.setText(getResourceString("NewAgent"));
            
            authorsList = new JList(new DefaultListModel());
            authorsList.setVisibleRowCount(10);
            authorsList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e)
                {
                    if (!e.getValueIsAdjusting())
                    {
                        Object selObj = authorsList.getSelectedValue();
                        if (selObj != null)
                        {
                            
                        }
                        updateEnabledState();
                    }
                }
            });
            JScrollPane scrollPane = new JScrollPane(authorsList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            
            mapToBtn = createIconBtn("Map", "ADD_AUTHOR_NAME_TT", new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    Object agent = searchPanel.getSelectedObject();
                    ((DefaultListModel)authorsList.getModel()).addElement(agent);
                    doFormatting();
                }
            });
            unmapBtn = createIconBtn("Unmap", "REMOVE_AUTHOR_NAME_TT", new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    int index = authorsList.getSelectedIndex();
                    if (index > -1)
                    {
                        DefaultListModel model = (DefaultListModel)authorsList.getModel();
                        model.remove(index);
                        updateEnabledState();
                        doFormatting();
                    }
                }
            });
            
            upBtn = createIconBtn("ReorderUp", "MOVE_AUTHOR_NAME_UP", new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    DefaultListModel model = (DefaultListModel)authorsList.getModel();
                    int    index = authorsList.getSelectedIndex();
                    Object item  = authorsList.getSelectedValue();
                    model.remove(index);
                    model.insertElementAt(item, index-1);
                    authorsList.setSelectedIndex(index-1);
                    updateEnabledState();
                }
            });
            downBtn = createIconBtn("ReorderDown", "MOVE_AUTHOR_NAME_DOWN", new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    DefaultListModel model = (DefaultListModel)authorsList.getModel();
                    int    index = authorsList.getSelectedIndex();
                    Object item  = authorsList.getSelectedValue();
                    model.remove(index);
                    model.insertElementAt(item, index+1);
                    authorsList.setSelectedIndex(index+1);
                    updateEnabledState();
                }
            });
            
            PanelBuilder    bldr = new PanelBuilder(new FormLayout("p, 5px, p, 5px, f:p:g, 2px, p", 
                    "p, 4px, p, 2px, f:p:g, 4px, p, 4px, p, 2px, p, 2px, p, 2px, p"), this);
            CellConstraints cc   = new CellConstraints();
            
            PanelBuilder upDownPanel = new PanelBuilder(new FormLayout("p", "p, 2px, p, f:p:g"));        
            upDownPanel.add(upBtn,          cc.xy(1, 1));
            upDownPanel.add(downBtn,        cc.xy(1, 3));
    
            PanelBuilder middlePanel = new PanelBuilder(new FormLayout("c:p:g", "f:p:g, p, 2px, p, f:p:g"));
            middlePanel.add(mapToBtn, cc.xy(1, 2));
            middlePanel.add(unmapBtn, cc.xy(1, 4));
            
            PanelBuilder rwPanel = new PanelBuilder(new FormLayout("p, 2px, f:p:g", "p"));
            refWorkLabel = new JLabel(getResourceString("None"));
            rwPanel.add(new JLabel(getResourceString("ReferenceWork")+":"), cc.xy(1, 1));
            rwPanel.add(refWorkLabel, cc.xy(3, 1));
            
            int y = 1;
            
            bldr.add(rwPanel.getPanel(), cc.xywh(1, y, 7, 1));  y += 2;
            
            bldr.add(searchPanel, cc.xywh(1, y, 1, 3));  
            bldr.addSeparator(getResourceString("Authors"), cc.xy(5, y)); y += 2;
            
            bldr.add(middlePanel.getPanel(), cc.xy(3, y));
            bldr.add(scrollPane, cc.xywh(5, y, 1, 3));  
            bldr.add(upDownPanel.getPanel(), cc.xy(7, y));  y += 2;
            
            PanelBuilder newAgentPanel = new PanelBuilder(new FormLayout("f:p:g,p", "p"));
            newAgentPanel.add(newAgentBtn, cc.xy(2,1));
            
            bldr.add(newAgentPanel.getPanel(), cc.xy(1, y));  y += 2;
            
            JLabel fmtLabel = new JLabel(getResourceString("LabelFormat"));
            bldr.add(fmtLabel, cc.xy(1, y));  y += 2;
            bldr.add(formatCBX, cc.xywh(1, y, 7, 1));  y += 2;
            
            Font plain = fmtLabel.getFont();
            specialLabel = new SpecialLabel(plain, new Font(plain.getName(), Font.ITALIC, plain.getSize()));
            specialLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            bldr.add(new JLabel(getResourceString("SampleOutput") + ":"), cc.xywh(1, y, 7, 1));  y += 2;
            bldr.add(specialLabel, cc.xywh(1, y, 7, 1));
            
            searchPanel.setOKBtn(mapToBtn);
        } catch (Exception ex)
        {
            log.error(ex);
            ex.printStackTrace();
        }
        UIRegistry.popResourceBundle();
        
    }

    /**
     * Enables the UI buttons given the selection state.
     */
    protected void updateEnabledState()
    {
        int size   = authorsList.getModel().getSize();
        int selInx = authorsList.getSelectedIndex();
        
        unmapBtn.setEnabled(selInx != -1);
        upBtn.setEnabled(selInx > 0);
        downBtn.setEnabled(selInx < size - 1);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.UIPluginBase#setValue(java.lang.Object, java.lang.String)
     */
    @Override
    public void setValue(Object value, String defaultValue)
    {
        super.setValue(value, defaultValue);
        
        if (value instanceof Taxon)
        {
            taxon = (Taxon)value;
            
            Taxon parent = taxon.getParent();
            while (parent != null)
            {
                parent = parent.getParent();
            }
        }
    }
    
    /**
     * Walks up the Taxon tree to find a specifically ranked parent
     * @param tx the current node
     * @param rankId the rank of the desired node
     * @return the parent with the desired rank
     */
    protected Taxon getByRank(final Taxon tx, final int rankId)
    {
        Taxon txn = tx;
        while (txn != null && txn.getRankId() != rankId)
        {
            txn = txn.getParent();
        }
        return txn;
    }

    /**
     * Formats the same String.
     */
    protected void doFormatting()
    {
        drawList.clear();
        
        Object fmtObj = formatCBX.getValue();
        if (fmtObj == null)
        {
            return;
        }
        
        String format = fmtObj.toString();
        System.out.println("["+format+"]");
        if (StringUtils.isNotEmpty(format) && format.length() > 0)
        {
            DefaultListModel model = (DefaultListModel)authorsList.getModel();
            
            StringBuilder chars = new StringBuilder();
            StringBuilder exp   = new StringBuilder();
            int    len = format.length();
            StringBuilder  pat = new StringBuilder();
            for (int i=0;i<len;i++)
            {
                char ch = format.charAt(i);
                if (ch == '%')
                {
                    if (chars.length() > 0)
                    {
                        drawList.add(new TextDrawInfo(FormatType.Plain, chars.toString()));
                        chars.setLength(0);
                    }
                    ch = format.charAt(++i);
                    pat.setLength(0);
                    pat.append('%');
                    do
                    {
                        if (Character.isLetter(ch) || Character.isDigit(ch))
                        {
                            pat.append(ch);
                        } else
                        {
                            i--;
                            break;
                        }
                        i++;
                        if (i < len)
                        {
                            ch = format.charAt(i);
                        }
                    } while (i < len);
                    
                    FormatType ft = FormatType.Plain;
                    String val = "";
                    String token = pat.toString();
                    if (token.equals("%S"))
                    {
                        val = getByRank(taxon, 220).getName();
                        ft = FormatType.Italic;
                        
                    } else if (token.equals("%G"))
                    {
                        val = getByRank(taxon, 180).getName();
                        ft = FormatType.Italic;
                        
                    } else if (token.charAt(1) == 'A')
                    {
                        int authNum = Integer.parseInt(token.substring(2))-1;
                        if (authNum < model.getSize())
                        {
                            val = ((Agent)model.get(authNum)).getLastName();
                        } else
                        {
                            val = token;
                        }
                    }
                    exp.append(val);
                    drawList.add(new TextDrawInfo(ft, val));
                } else
                {
                    exp.append(ch);
                    chars.append(ch);
                }
            }
            if (chars.length() > 0)
            {
                drawList.add(new TextDrawInfo(FormatType.Plain, chars.toString()));
                chars.setLength(0);
            }
            specialLabel.repaint();
        }
    }
    
    // This holds the formatting information
    class TextDrawInfo
    {
        protected FormatType type;
        protected String     text;
        public TextDrawInfo(FormatType type, String text)
        {
            this.type = type;
            this.text = text;
        }
        public FormatType getType()
        {
            return type;
        }
        public String getText()
        {
            return text;
        }
    }
    
    // This renders the label with formatting
    class SpecialLabel extends JLabel
    {
        protected Font plainFont;
        protected Font italicFont;

        public SpecialLabel(Font plainFont, Font italicFont)
        {
            super(" ");
            this.plainFont = plainFont;
            this.italicFont = italicFont;
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);
            
            g.setColor(Color.BLACK);
            Dimension size = getSize();
            Insets insets = getBorder().getBorderInsets(this);
            
            int x = 5;
            int y = 0;
            for (TextDrawInfo tdi : drawList)
            {
                Font f = tdi.getType() == FormatType.Plain ? plainFont : italicFont;
                g.setFont(f);
                FontMetrics fm = g.getFontMetrics();
                y = size.height - fm.getDescent() - insets.bottom - fm.getLeading() - 1;
                g.drawString(tdi.getText(), x, y);
                System.out.println(tdi.getType()+" ["+tdi.getText()+"] "+x+", "+y);
                x += fm.stringWidth(tdi.getText());
            }
        }
        
    }
    
}
