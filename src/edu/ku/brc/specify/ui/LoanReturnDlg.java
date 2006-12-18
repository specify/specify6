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
package edu.ku.brc.specify.ui;

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Calendar;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.CollectionObjDef;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.Loan;
import edu.ku.brc.specify.datamodel.LoanPhysicalObject;
import edu.ku.brc.specify.datamodel.LoanReturnPhysicalObject;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.ui.ColorWrapper;
import edu.ku.brc.ui.DateWrapper;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.db.ViewBasedDisplayDialog;
import edu.ku.brc.ui.forms.FormViewObj;
import edu.ku.brc.ui.forms.MultiView;
import edu.ku.brc.ui.forms.ViewFactory;
import edu.ku.brc.ui.forms.Viewable;
import edu.ku.brc.ui.forms.persist.FormCell;
import edu.ku.brc.ui.forms.persist.FormCellField;
import edu.ku.brc.ui.forms.persist.View;
import edu.ku.brc.ui.validation.FormValidator;
import edu.ku.brc.ui.validation.ValComboBoxFromQuery;

/**
 * Creates a dialog representing all the Preparation objects being returned for a loan.
 * 
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Dec 15, 2006
 *
 */
public class LoanReturnDlg extends JDialog
{
    protected ColorWrapper           requiredfieldcolor = AppPrefsCache.getColorWrapper("ui", "formatting", "requiredfieldcolor");
    protected DateWrapper            scrDateFormat = AppPrefsCache.getDateWrapper("ui", "formatting", "scrdateformat");
    protected Loan                   loan;
    protected List<ColObjPanel>      colObjPanels = new Vector<ColObjPanel>();
    protected JButton                okBtn;
    protected JLabel                 summaryLabel;
    protected FormValidator          validator = new FormValidator();
    protected ValComboBoxFromQuery   agentCBX;
    
    /**
     * @param colObjs
     */
    public LoanReturnDlg(final Loan loan)
    {
        this.loan = loan;
        
        setTitle("Loan Return"); // I18N
         
        JPanel contentPanel = new JPanel(new BorderLayout());
        
        JPanel mainPanel = new JPanel();
        
        Hashtable<String, Vector<LoanPhysicalObject>> colObjHash = new Hashtable<String, Vector<LoanPhysicalObject>>();
        for (LoanPhysicalObject lpo : loan.getLoanPhysicalObjects())
        {
            CollectionObject colObj = lpo.getPreparation().getCollectionObject();
            Vector<LoanPhysicalObject> list = colObjHash.get(colObj);
            if (list == null)
            {
                list = new Vector<LoanPhysicalObject>();
                colObjHash.put(colObj.getIdentityTitle(), list);
            }
            list.add(lpo);
        }
        int colObjCnt = colObjHash.size();

        String          rowDef   = UIHelper.createDuplicateJGoodiesDef("p", "1px,p,4px", (colObjCnt*2)-1);
        PanelBuilder    pbuilder = new PanelBuilder(new FormLayout("f:p:g", rowDef), mainPanel);
        CellConstraints cc       = new CellConstraints();
        
        ActionListener al = new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                doEnableOKBtn();
            }
        };
 
        ChangeListener cl = new ChangeListener()
        {
            public void stateChanged(ChangeEvent ae)
            {
                doEnableOKBtn();
            }
        };
 
        int i = 0;
        int y = 1;

        Vector<String> keysList = new Vector<String>(colObjHash.keySet());
        Collections.sort(keysList);
        Iterator<String> it = keysList.iterator();
        while (it.hasNext()) 
        {

            if (i > 0)
            {
                pbuilder.addSeparator("", cc.xy(1,y));
            }
            y += 2;
            
            ColObjPanel panel = new ColObjPanel(this, colObjHash.get(it.next()));
            colObjPanels.add(panel);
            panel.addActionListener(al, cl);
            pbuilder.add(panel, cc.xy(1,y));
            y += 2;
            i++;

        }
        
        JButton selectAllBtn = new JButton("Select All"); // I18N
        okBtn = new JButton(getResourceString("OK"));
        JButton cancel = new JButton(getResourceString("Cancel"));
        
        summaryLabel = new JLabel("");
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(5, 1, 5, 1));
        p.add(summaryLabel, BorderLayout.CENTER);
        
        JPanel agentPanel = new JPanel(new BorderLayout());
        agentPanel.add(new JLabel("Agent:", JLabel.RIGHT), BorderLayout.WEST); // I18N
        agentPanel.add(agentCBX = createAgentCombobox(), BorderLayout.CENTER);
        p.add(agentPanel, BorderLayout.EAST); // I18N
        
        contentPanel.add(p, BorderLayout.NORTH);
        contentPanel.add(new JScrollPane(mainPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
        
        p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(5, 0, 2, 0));
        p.add(ButtonBarFactory.buildOKCancelApplyBar(okBtn, cancel, selectAllBtn), BorderLayout.CENTER);
        contentPanel.add(p, BorderLayout.SOUTH);
        
        setContentPane(contentPanel);
        
        doEnableOKBtn();

        //setIconImage(IconManager.getIcon("Preparation", IconManager.IconSize.Std16).getImage());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        doEnableOKBtn();
        
        okBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                setVisible(false);
            }
        });
        
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                setVisible(false);
            }
        });
        
        selectAllBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                selectAllItems();
            }
        });
        
        pack();
        
        Dimension size = getPreferredSize();
        size.width += 20;
        size.height = size.height > 500 ? 500 : size.height;
        setSize(size);
    }
    
    protected void doEnableOKBtn()
    {
        int count = 0;
        for (ColObjPanel pp : colObjPanels)
        {
            count += pp.getReturnCount();
        }
        okBtn.setEnabled(count > 0);
        //if (count > 0)
        //{
            summaryLabel.setText(String.format("%d items to be returned by", new Object[] {count})); // I18N
        //}
    }
    
    /**
     * @return
     */
    protected ValComboBoxFromQuery createAgentCombobox()
    {
        FormCellField fcf = new FormCellField(FormCell.CellType.field,
                                               "1", "agent", FormCellField.FieldType.querycbx, FormCellField.FieldType.querycbx, 
                                               "", "", "", true,
                                               1, 1, 1, 1, "Change", null, false);
        fcf.addProperty("name", "Agent");
        fcf.addProperty("title", "Agent doing Return"); // I18N
        return ViewFactory.getInstance().createQueryComboBox(validator, fcf);
    }
    
    
    /**
     * Sets all the spinners to there max values.
     */
    protected void selectAllItems()
    {
        for (ColObjPanel colObjPanel : colObjPanels)
        {
            for (PrepPanel pp : colObjPanel.getPanels())
            {
                pp.selectAllItems();
            }
        }
    }
    
    /**
     * Returns the agent that is doing the return.
     * @return the agent that is doing the return.
     */
    public Agent getAgent()
    {
        return (Agent)agentCBX.getValue();
    }
    
    /**
     * Returns a Hastable of Preparation to Count.
     * @return a Hastable of Preparation to Count.
     */
    public List<LoanReturnInfo> getLoanReturnInfo()
    {
        List<LoanReturnInfo> returns = new Vector<LoanReturnInfo>();
        
        for (ColObjPanel colObjPanel : colObjPanels)
        {
            for (PrepPanel pp : colObjPanel.getPanels())
            {
                if (pp.getCount() > 0)
                {
                    returns.add(pp.getLoanReturnInfo());
                }
            }
        }
        return returns;
    }

    //------------------------------------------------------------------------------------------
    //
    //------------------------------------------------------------------------------------------
    class ColObjPanel extends JPanel
    {
        protected CollectionObject  colObj;
        protected JCheckBox         checkBox;
        protected Vector<PrepPanel> panels = new Vector<PrepPanel>();       
        protected JDialog           dlgParent;
        
        /**
         * @param colObj
         */
        public ColObjPanel(final JDialog dlgParent, final List<LoanPhysicalObject> lpoList)
        {
            super();
            this.dlgParent = dlgParent;
            this.colObj    = lpoList.get(0).getPreparation().getCollectionObject();
            
            setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
            //setBorder(BorderFactory.createCompoundBorder(new CurvedBorder(new Color(160,160,160)), getBorder()));
            //setBorder(new CurvedBorder(new Color(160,160,160)));
     
            PanelBuilder    pbuilder = new PanelBuilder(new FormLayout("f:p:g", "p,5px,p"), this);
            CellConstraints cc      = new CellConstraints();
     
            String taxonName = "";
            for (Determination deter : colObj.getDeterminations())
            {
                if (deter.getStatus().getDeterminationStatusId() == null ? deter.getStatus().getName().equals("Current") : deter.getStatus().getDeterminationStatusId() == 2)
                {
                    if (deter.getTaxon().getFullName() == null)
                    {
                        Taxon parent = deter.getTaxon().getParent();
                        String genus = parent.getFullName() == null ? parent.getName() : parent.getFullName();
                        taxonName = genus + " " + deter.getTaxon().getName();
                        
                    } else
                    {
                        taxonName = deter.getTaxon().getFullName();
                    }

                    break;
                }
            }
            String descr = String.format("%6.0f - %s", new Object[]{colObj.getCatalogNumber(), taxonName});
            descr = StringUtils.stripToEmpty(descr);
            
            checkBox = new JCheckBox(descr);
            pbuilder.add(new JLabel(descr), cc.xy(1,1));
            checkBox.setSelected(true);
            
            JPanel outerPanel = new JPanel();
            outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));
            outerPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
            
            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            outerPanel.add(contentPanel);
            
            Color[] colors = new Color[] { new Color(255,255,255), new Color(235,235,255)};
            
            int i = 0;
            for (LoanPhysicalObject lpo : lpoList)
            {
                PrepPanel pp = new PrepPanel(dlgParent, lpo);
                panels.add(pp);
                pp.setBackground(colors[i % 2]);
                contentPanel.add(pp);
                i++;
            }
            pbuilder.add(outerPanel, cc.xy(1,3));
            
            checkBox.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    for (PrepPanel pp : panels)
                    {
                        pp.setEnabled(checkBox.isSelected());
                    }
                    System.out.println(getReturnCount());
                    repaint();
                }
            });
        }
        
        public void addActionListener(final ActionListener al, final ChangeListener cl)
        {
            checkBox.addActionListener(al);
            
            for (PrepPanel pp : panels)
            {
                pp.addChangeListener(cl);
            }
        }
        
        public JCheckBox getCheckBox()
        {
            return checkBox;
        }
        
        public int getReturnCount()
        {
            int count = 0;
            if (checkBox.isSelected())
            {
                for (PrepPanel pp : panels)
                {
                    count += pp.getCount();
                }
            }
            return count;
        }

        public Vector<PrepPanel> getPanels()
        {
            return panels;
        }
        
       
    }
   
    //------------------------------------------------------------------------------------------
    //
    //------------------------------------------------------------------------------------------
    class PrepPanel extends JPanel implements ActionListener
    {
        protected Preparation prep;
        protected LoanPhysicalObject lpo;
        protected JLabel      label    = null;
        protected JLabel      label2    = null;
        protected JComponent  prepInfoBtn    = null;
        protected JSpinner    spinner; 
        protected JDialog     parent;
        protected int         quantityReturned;
        protected int         maxValue = 0;
        protected boolean     unknownQuantity;
        protected JTextField  remarks;
        protected JCheckBox   resolved;

        /**
         * Constructs a pnale representing the Preparation being returned.
         * @param parent the parent dialog
         * @param lpo the LoanPhysicalObject being returned
         */
        public PrepPanel(final JDialog parent, final LoanPhysicalObject lpo)
        {
            super();
            this.prep   = lpo.getPreparation();
            this.lpo    = lpo;
            this.parent = parent;
            
            setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
            //setBorder(BorderFactory.createCompoundBorder(new CurvedBorder(new Color(160,160,160)), getBorder()));
     
            PanelBuilder    pbuilder = new PanelBuilder(new FormLayout("max(120px;p),2px,max(50px;p),2px,p,2px,p,10px,p:g", "p,2px,p"), this);
            CellConstraints cc      = new CellConstraints();
            
            
            pbuilder.add(label = new JLabel(prep.getPrepType().getName()), cc.xy(1,1));
            label.setOpaque(false);
            
            boolean allReturned = false;
            
            if (prep.getCount() !=  null)
            {
                int quantityLoaned    = lpo.getQuantity();
                quantityReturned      = lpo.getQuantityReturned();
                
                int quantityOut       = quantityLoaned - quantityReturned;
                
                if (quantityOut > 0 && !lpo.getIsResolved())
                {
                    maxValue = quantityLoaned;
                    
                    SpinnerModel model = new SpinnerNumberModel(quantityReturned, //initial value
                                               quantityReturned, //min
                                               quantityLoaned,   //max
                                               1);               //step
                    spinner = new JSpinner(model);
                    fixBGOfJSpinner(spinner);
                    pbuilder.add(spinner, cc.xy(3, 1));
                    
                    //String str = " of " + Integer.toString(quantityAvailable) + "  " + (quantityOut > 0 ? "(" + quantityOut + " on loan.)" : "");
                    String fmtStr = String.format(" of %3d  ", new Object[] {quantityLoaned}); // TODO I18N
                    pbuilder.add(label2 = new JLabel(fmtStr), cc.xy(5, 1));
                    if (quantityOut > 0)
                    {
                        fmtStr = quantityReturned == 0 ? "Nothing returned" : String.format("[%d returned]", new Object[] {quantityReturned}); // TODO I18N
                        prepInfoBtn = new LinkLabelBtn(this, fmtStr, IconManager.getIcon("InfoIcon"));
                        //prepInfoBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                        pbuilder.add(prepInfoBtn, cc.xy(7, 1));
                    }
                    
                    
                } else
                {
                    Calendar lastReturnDate = null;
                    for (LoanReturnPhysicalObject lrpo : lpo.getLoanReturnPhysicalObjects())
                    {
                        Calendar retDate = lrpo.getReturnedDate();
                        if (retDate != null)
                        {
                            if (lastReturnDate == null)
                            {
                                lastReturnDate = lrpo.getReturnedDate();
                            } else if (retDate.after(lastReturnDate))
                            {
                                lastReturnDate = retDate;
                            }
                        }
                    }
                        
                    String fmtStr = lastReturnDate == null ? "All Returned" : // I18N
                                 String.format("All Returned on %s", new Object[] {scrDateFormat.format(lastReturnDate)});
                    prepInfoBtn = new LinkLabelBtn(this, fmtStr, IconManager.getIcon("InfoIcon"));
                    pbuilder.add(prepInfoBtn, cc.xywh(3, 1, 7, 1));
                    //pbuilder.add(label2 = new JLabel(fmtStr), cc.xywh(3, 1, 7, 1));
                    allReturned = true;
                }

                
            } else
            {
                SpinnerModel model = new SpinnerNumberModel(0, //initial value
                        0,    //min
                        10000, //max
                        1);   //step
                spinner = new JSpinner(model);
                fixBGOfJSpinner(spinner);
                pbuilder.add(spinner, cc.xy(3, 1));
                pbuilder.add(label2 = new JLabel(" (Unknown Number Available)"), cc.xywh(5, 1, 2, 1)); // I18N
                unknownQuantity = true;
                maxValue = 1;
            }

            if (!allReturned)
            {
                resolved = new JCheckBox("Resolved"); // I18N
                resolved.setOpaque(false);
                //resolved.setBackground(this.getBackground());
                pbuilder.add(resolved, cc.xywh(9, 1, 1, 1));

                remarks = new RemarksText();
                pbuilder.add(remarks, cc.xywh(1, 3, 9, 1)); // I18N
            }
            
            if (spinner != null)
            {
                spinner.addChangeListener(new ChangeListener()
                {
                    public void stateChanged(ChangeEvent ae)
                    {
                        Integer val = (Integer)spinner.getValue();
                        resolved.setSelected(val >= maxValue);
                    }
                });
            }
        }
        
        /**
         * Changes the BG color fo the text field in the spinner to the required color.
         * @param spin the spinner to be changed
         */
        protected void fixBGOfJSpinner(final JSpinner spin)
        {
            JComponent edComp = spin.getEditor();
            for (int i=0;i<edComp.getComponentCount();i++)
            {
                Component c = edComp.getComponent(i);
                if (c instanceof JTextField)
                {
                    c.setBackground(requiredfieldcolor.getColor());
                }
            }
        }
        
        /**
         * Return whether their is an unknown quantity.
         * @return whether their is an unknown quantity.
         */
        public boolean isUnknownQuantity()
        {
            return unknownQuantity;
        }

        /**
         * Sets all the spinners to there max values.
         */
        public void selectAllItems()
        {
            if (spinner != null)
            {
                spinner.setValue(maxValue);
            }
        }

        /**
         * Returns the LoanPhysicalObject for this panel.
         * @return the LoanPhysicalObject for this panel.
         */
        public LoanPhysicalObject getLoanPhysicalObject()
        {
            return lpo;
        }

        /* (non-Javadoc)
         * @see javax.swing.JComponent#setEnabled(boolean)
         */
        public void setEnabled(final boolean enabled)
        {
            if (label != null)
            {
                label.setEnabled(enabled);
            }
            if (label2 != null)
            {
                label2.setEnabled(enabled);
            }
            if (prepInfoBtn != null)
            {
                prepInfoBtn.setEnabled(enabled);
            }
            if (spinner != null)
            {
                spinner.setEnabled(enabled);
            }
        }
        
        /**
         * Adds a change listener.
         * @param cl the change listener
         */
        public void addChangeListener(final ChangeListener cl)
        {
            if (spinner != null)
            {
                spinner.addChangeListener(cl);
            }
        }
        
        /**
         * Returns the count from the spinner, the count of items being returning.
         * @return the count from the spinner, the count of items being returning.
         */
        public int getCount()
        {
            if (spinner != null)
            {
                Object valObj = spinner.getValue();
               return valObj == null ? 0 : ((Integer)valObj).intValue() - quantityReturned;
                
            } else
            {
                return 0;
            }
        }
        
        /**
         * Returns the LoanReturnInfo describing the user input for the loan return.
         * @return the LoanReturnInfo describing the user input for the loan return
         */
        public LoanReturnInfo getLoanReturnInfo()
        {
            return new LoanReturnInfo(lpo, 
                                      resolved != null ? resolved.isSelected() : null, 
                                      remarks != null ? remarks.getText() : null,
                                      (short)getCount());
        }
        
        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e)
        {
            List<Loan> loans = new Vector<Loan>();
            
            for (LoanPhysicalObject loanPO : prep.getLoanPhysicalObjects())
            {
                loans.add(loanPO.getLoan());
            }
            
            View view  = ((SpecifyAppContextMgr)AppContextMgr.getInstance()).getView("Loan", CollectionObjDef.getCurrentCollectionObjDef());
            final ViewBasedDisplayDialog dlg = new ViewBasedDisplayDialog(parent,
                    view.getViewSetName(),
                    "Loan",
                    null,
                    getResourceString("LOAN_REVIEW"),
                    getResourceString("Close"),
                    null, // className,
                    null, // idFieldName,
                    false, // isEdit,
                    MultiView.RESULTSET_CONTROLLER);
            
            MultiView mv = dlg.getMultiView();
            Viewable currentViewable = mv.getCurrentView();
            if (currentViewable != null && currentViewable instanceof FormViewObj)
            {
                FormViewObj formViewObj = (FormViewObj)currentViewable;
                Component comp      = formViewObj.getControlByName("generateInvoice");
                if (comp instanceof JCheckBox)
                {
                    comp.setVisible(false);
                }

            }
            dlg.setModal(true);
            dlg.setData(loans);
            dlg.setVisible(true);
        }
    }

   
    //------------------------------------------------------------------------------------------
    //
    //------------------------------------------------------------------------------------------
    protected static final Cursor handCursor    = new Cursor(Cursor.HAND_CURSOR);
    protected static final Cursor defCursor     = new Cursor(Cursor.DEFAULT_CURSOR);

    class LinkLabelBtn extends JLabel
    {
        protected ActionListener al;
        
        public LinkLabelBtn(final ActionListener al, final String label, final ImageIcon imgIcon)
        {
            super(label, imgIcon, JLabel.LEFT);
            setHorizontalTextPosition(JLabel.LEFT);
            this.al = al;
            
            //setBorderPainted(false);
            //setBorder(BorderFactory.createEmptyBorder());
            //setOpaque(false);
            //setCursor(handCursor);
            
            //final LinkLabelBtn llb = this;

            addMouseListener(new MouseAdapter()
            {
                public void mouseClicked(MouseEvent e) 
                {
                    al.actionPerformed(new ActionEvent(this, 0, ""));
                }

                /**
                 * Invoked when a mouse button has been pressed on a component.
                 */
                public void mousePressed(MouseEvent e) {}

                /**
                 * Invoked when a mouse button has been released on a component.
                 */
                public void mouseReleased(MouseEvent e) {}

                /**
                 * Invoked when the mouse enters a component.
                 */
                public void mouseEntered(MouseEvent e) 
                {
                    //llb.setCursor(handCursor);
                }

                /**
                 * Invoked when the mouse exits a component.
                 */
                public void mouseExited(MouseEvent e) 
                {
                    //llb.setCursor(defCursor);
                }
            });
        }
    }
    
    //------------------------------------------------------------------------------------------
    //
    //------------------------------------------------------------------------------------------
    public class LoanReturnInfo
    {
        protected LoanPhysicalObject lpo;
        protected Boolean            isResolved;
        protected String             remarks;
        protected short              quantity;
        
        public LoanReturnInfo(LoanPhysicalObject lpo, Boolean isResolved, String remarks, short quantity)
        {
            super();
            this.lpo = lpo;
            this.isResolved = isResolved;
            this.remarks = remarks;
            this.quantity = quantity;
        }
        public Boolean isResolved()
        {
            return isResolved;
        }
        public LoanPhysicalObject getLoanPhysicalObject()
        {
            return lpo;
        }
        public short getQuantity()
        {
            return quantity;
        }
        public String getRemarks()
        {
            return remarks;
        }
    }
    
    class RemarksText extends JTextField
    {
        protected Insets inner;
        protected String bgStr     = "Remarks";  // I18N
        protected Point  pnt       = null;
        protected Color  textColor = new Color(0,0,0,64);
        
        public RemarksText()
        {
            inner = getInsets();
        }
        
        /* (non-Javadoc)
         * @see java.awt.Component#paint(java.awt.Graphics)
         */
        @Override
        public void paint(Graphics g)
        {
            super.paint(g);

            String text = getText();

            if (text == null || text.length() == 0)
            {
                if (pnt == null)
                {
                    FontMetrics fm   = g.getFontMetrics();
                    pnt = new Point(inner.left, inner.top + fm.getAscent());
                }

                g.setColor(textColor);
                g.drawString(bgStr, pnt.x, pnt.y);
            }

        }
    }


}
