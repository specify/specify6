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

import static edu.ku.brc.ui.UIHelper.createCheckBox;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.setControlSize;
import static edu.ku.brc.ui.UIRegistry.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.af.ui.db.ViewBasedDisplayDialog;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.persist.ViewIFace;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.DeterminationStatus;
import edu.ku.brc.specify.datamodel.Loan;
import edu.ku.brc.specify.datamodel.LoanPreparation;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.ui.ColorWrapper;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * Creates a dialog listing all the Preparations that are available to be loaned.
 * 
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Nov 21, 2006
 *
 */
public class LoanSelectPrepsDlg extends CustomDialog
{
    protected ColorWrapper           requiredfieldcolor = AppPrefsCache.getColorWrapper("ui", "formatting", "requiredfieldcolor"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    protected List<CollectionObject> colObjs;
    protected List<ColObjPanel>      colObjPanels = new Vector<ColObjPanel>();
    protected JLabel                 summaryLabel;
    protected int                    totalCntLoanablePreps = 0;
    
    /**
     * @param colObjs
     */
    public LoanSelectPrepsDlg(List<CollectionObject> colObjs)
    {
        super((Frame)UIRegistry.getTopWindow(), getResourceString("LoanSelectPrepsDlg.CREATE_LN_FR_PREP"),//$NON-NLS-1$
                true, OKCANCELAPPLYHELP, null);
        this.colObjs = colObjs;
    }
        
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        applyLabel = getResourceString("SELECTALL");//$NON-NLS-1$
        
        super.createUI();
        
        Vector<CollectionObject> availCOsToLoan = new Vector<CollectionObject>();
        for (CollectionObject co : colObjs)
        {
            if (getCurrentDetermination(co) != null)
            {
                int cntLoanablePreps = 0;
                for (Preparation prep : co.getPreparations())
                {
                    if (prep.getPrepType().getIsLoanable())
                    {
                        cntLoanablePreps++;
                    }
                }
                if (cntLoanablePreps > 0)
                {
                    availCOsToLoan.add(co);
                }
                totalCntLoanablePreps += cntLoanablePreps;
            }
        }
        
        String rowDef = UIHelper.createDuplicateJGoodiesDef("p", "1px,p,4px", (colObjs.size()*2)-1) + ",10px,p"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        PanelBuilder    pbuilder = new PanelBuilder(new FormLayout("f:p:g", rowDef)); //$NON-NLS-1$
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
        
        Collections.sort(availCOsToLoan);
        
        int i = 0;
        int y = 1;
        for (CollectionObject co : availCOsToLoan)
        {
            if (i > 0)
            {
                pbuilder.addSeparator("", cc.xy(1,y)); //$NON-NLS-1$
            }
            y += 2;
            
            ColObjPanel panel = new ColObjPanel(this, co);
            colObjPanels.add(panel);
            panel.addActionListener(al, cl);
            pbuilder.add(panel, cc.xy(1,y));
            y += 2;
            i++;
        }
        
        applyBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae)
            {
                selectAllItems();
            }
        });

        JPanel tPanel = new JPanel(new BorderLayout());
        summaryLabel = createLabel(""); //$NON-NLS-1$
        tPanel.setBorder(BorderFactory.createEmptyBorder(5, 1, 5, 1));
        tPanel.add(summaryLabel, BorderLayout.NORTH);
        
        JScrollPane sp = new JScrollPane(pbuilder.getPanel(), ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        tPanel.add(sp, BorderLayout.CENTER);
        
        contentPanel = tPanel;
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        pack();
        
        doEnableOKBtn();

        Dimension size = getPreferredSize();
        size.width += 20;
        size.height = size.height > 500 ? 500 : size.height;
        setSize(size);
    }
    
    /**
     * @return whether there is at least one prep that can be loaned.
     */
    public boolean hasAvaliblePrepsToLoan()
    {
        return totalCntLoanablePreps > 0;
    }
    
    /**
     * Returns the current Determination for a Collection Object
     * @param colObj the collection object
     * @return the current Determination
     */
    protected Determination getCurrentDetermination(final CollectionObject colObj)
    {
        for (Determination d : colObj.getDeterminations())
        {
            if (d.getStatus().getType() == DeterminationStatus.CURRENT)
            {
                return d;
            }
        }
        return null;
    }
    
    /**
     * Enables the OK btn depending on what is activated.
     */
    protected void doEnableOKBtn()
    {
        int count = 0;
        for (ColObjPanel pp : colObjPanels)
        {
            if (pp.isColObjEnabled())
            {
                count += pp.getNewLoanCount();
            }
        }
        okBtn.setEnabled(count > 0);
        //if (count > 0)
        //{
            summaryLabel.setText(String.format(getResourceString("LoanSelectPrepsDlg.NUM_PREP_SEL"), new Object[] {count})); //$NON-NLS-1$
        //}
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
     * Returns a Hashtable of Preparation to Count.
     * @return a Hashtable of Preparation to Count.
     */
    public Hashtable<Preparation, Integer> getPreparationCounts()
    {
        Hashtable<Preparation, Integer> hash = new Hashtable<Preparation, Integer>();
        
        for (ColObjPanel colObjPanel : colObjPanels)
        {
            if (colObjPanel.isColObjEnabled())
            {
                for (PrepPanel pp : colObjPanel.getPanels())
                {
                    if (pp.getCount() > 0)
                    {
                        hash.put(pp.getPreparation(), pp.getCount());
                    }
                }
            }
        }
        return hash;
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
        public ColObjPanel(final JDialog dlgParent, final CollectionObject colObj)
        {
            super();
            this.dlgParent = dlgParent;
            this.colObj    = colObj;
            
            setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
            //setBorder(BorderFactory.createCompoundBorder(new CurvedBorder(new Color(160,160,160)), getBorder()));
            //setBorder(new CurvedBorder(new Color(160,160,160)));
     
            PanelBuilder    pbuilder = new PanelBuilder(new FormLayout("f:p:g", "p,5px,p"), this); //$NON-NLS-1$ //$NON-NLS-2$
            CellConstraints cc      = new CellConstraints();
     
            String taxonName = getResourceString("LoanSelectPrepsDlg.NO_DET"); // This title should never happen //$NON-NLS-1$
            for (Determination deter : colObj.getDeterminations())
            {
                if (deter.getStatus().getType() == DeterminationStatus.CURRENT)
                {
                    taxonName = getTaxonName(deter);
                    break;
                }
            }
            String descr = String.format(getResourceString("LoanSelectPrepsDlg.TITLE_PAIR"), colObj.getIdentityTitle(), taxonName); //$NON-NLS-1$
            descr = StringUtils.stripToEmpty(descr);
            
            pbuilder.add(checkBox = createCheckBox(descr), cc.xy(1,1));
            //builder.add(createLabel(String.format("%6.0f", new Object[]{colObj.getCatalogNumber()})), cc.xy(1,1));
            checkBox.setSelected(true);
            
            JPanel outerPanel = new JPanel();
            outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));
            outerPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
            
            JPanel containerPane = new JPanel();
            containerPane.setLayout(new BoxLayout(containerPane, BoxLayout.Y_AXIS));
            outerPanel.add(containerPane);
            
            Color[] colors = new Color[] { new Color(255,255,255), new Color(235,235,255)};
            
            Vector<Preparation> prepsList = new Vector<Preparation>();
            for (Preparation prep : colObj.getPreparations())
            {
                if (prep.getPrepType().getIsLoanable())
                {
                    prepsList.add(prep);
                }
            }
            Collections.sort(prepsList);
            
            int i = 0;
            for (Preparation prep : prepsList)
            {
                PrepPanel pp = new PrepPanel(dlgParent, prep);
                panels.add(pp);
                pp.setBackground(colors[i % 2]);
                containerPane.add(pp);
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
                    repaint();
                }
            });
        }
        
        public String getTaxonName(final Determination deter)
        {
            String taxonName;
            if (deter.getTaxon().getFullName() == null)
            {
                Taxon  parent = deter.getTaxon().getParent();
                String genus  = parent.getFullName() == null ? parent.getName() : parent.getFullName();
                taxonName = genus + " " + deter.getTaxon().getName(); //$NON-NLS-1$
                
            } else
            {
                taxonName = deter.getTaxon().getFullName();
            }
            return taxonName;
        }
        
        public void addActionListener(final ActionListener al, final ChangeListener cl)
        {
            checkBox.addActionListener(al);
            
            for (PrepPanel pp : panels)
            {
                pp.addChangeListener(cl);
            }
        }
        
        public boolean isColObjEnabled()
        {
            return checkBox.isSelected();
        }
        
        public int getNewLoanCount()
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
        protected JLabel      label    = null;
        protected JLabel      label2    = null;
        protected JComponent  prepInfoBtn    = null;
        protected JSpinner    spinner; 
        protected JDialog     parent;
        protected int         maxValue = 0;
        protected boolean     unknownQuantity;

        /**
         * @param prep
         */
        public PrepPanel(final JDialog parent, final Preparation prep)
        {
            super();
            this.prep = prep;
            this.parent = parent;

            
            setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
            //setBorder(BorderFactory.createCompoundBorder(new CurvedBorder(new Color(160,160,160)), getBorder()));
     
            PanelBuilder    pbuilder = new PanelBuilder(new FormLayout("max(120px;p),2px,max(50px;p),2px,p,2px,p:g", "c:p"), this); //$NON-NLS-1$ //$NON-NLS-2$
            CellConstraints cc      = new CellConstraints();
            
            pbuilder.add(label = createLabel(prep.getPrepType().getName()), cc.xy(1,1));
            label.setOpaque(false);
            
            if (prep.getCount() !=  null)
            {
                int count       = prep.getCount() == null ? 0 : prep.getCount();
                int quantityOut = prep.getLoanQuantityOut();  
                
                int quantityAvailable = count - quantityOut;
                if (quantityAvailable > 0)
                {
                    maxValue = quantityAvailable;
                    
                    SpinnerModel model = new SpinnerNumberModel(0, //initial value
                                               0, //min
                                               quantityAvailable, //max
                                               1);                //step
                    spinner = new JSpinner(model);
                    fixBGOfJSpinner(spinner);
                    pbuilder.add(spinner, cc.xy(3, 1));
                    //String str = " of " + Integer.toString(quantityAvailable) + "  " + (quantityOut > 0 ? "(" + quantityOut + " on loan.)" : "");
                    String fmtStr = getLocalizedMessage("LoanSelectPrepsDlg.OF_QUANT_OUT", quantityAvailable);
                    pbuilder.add(label2 = createLabel(fmtStr), cc.xy(5, 1));
                    if (quantityOut > 0)
                    {
                        fmtStr = getLocalizedMessage("LoanSelectPrepsDlg.OF_QUANT_OUT", quantityOut); //$NON-NLS-1$
                        prepInfoBtn = new LinkLabelBtn(this, fmtStr, IconManager.getIcon("InfoIcon")); //$NON-NLS-1$
                        //prepInfoBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                        pbuilder.add(prepInfoBtn, cc.xy(7, 1));
                    }
                    
                } else
                {
                    pbuilder.add(label2 = createLabel(getResourceString("LoanSelectPrepsDlg.NONE_AVAIL")), cc.xywh(3, 1, 5, 1));//$NON-NLS-1$
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
                pbuilder.add(label2 = createLabel(getResourceString("LoanSelectPrepsDlg.UNKN_NUM_AVAIL")), cc.xywh(5, 1, 2, 1)); //$NON-NLS-1$
                unknownQuantity = true;
                maxValue = 1;
            }
            //pbuilder.add(contentPanel, cc.xy(1,3));
        }
        
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
         * @return
         */
        public Preparation getPreparation()
        {
            return prep;
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
         * @param cl
         */
        public void addChangeListener(final ChangeListener cl)
        {
            if (spinner != null)
            {
                spinner.addChangeListener(cl);
            }

        }

        
        /**
         * @return
         */
        public int getCount()
        {
            if (spinner != null)
            {
                Object valObj = spinner.getValue();
                return valObj == null ? 0 : ((Integer)valObj).intValue();
                
            }
            // else
            return 0;
        }
        
        public void actionPerformed(ActionEvent e)
        {
            List<Loan> loans = new Vector<Loan>();
            
            for (LoanPreparation lpo : prep.getLoanPreparations())
            {
                loans.add(lpo.getLoan());
            }
            
            ViewIFace view  = AppContextMgr.getInstance().getView("Loan"); //$NON-NLS-1$
            final ViewBasedDisplayDialog dlg = new ViewBasedDisplayDialog(parent,
                    view.getViewSetName(),
                    "Loan", //$NON-NLS-1$
                    null,
                    getResourceString("LoanSelectPrepsDlg.IAT_LOAN_REVIEW"), //$NON-NLS-1$
                    getResourceString("CLOSE"), //$NON-NLS-1$
                    null, // className,
                    null, // idFieldName,
                    false, // isEdit,
                    MultiView.RESULTSET_CONTROLLER);
            dlg.setHelpContext("LOAN_REVIEW");
            
            MultiView mv = dlg.getMultiView();
            Viewable currentViewable = mv.getCurrentView();
            if (currentViewable != null && currentViewable instanceof FormViewObj)
            {
                FormViewObj formViewObj = (FormViewObj)currentViewable;
                Component comp      = formViewObj.getControlByName("generateInvoice"); //$NON-NLS-1$
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
    
    protected static final Cursor handCursor    = new Cursor(Cursor.HAND_CURSOR);
    protected static final Cursor defCursor     = new Cursor(Cursor.DEFAULT_CURSOR);

    class LinkLabelBtn extends JLabel
    {
        protected ActionListener al;
        
        public LinkLabelBtn(final ActionListener al, final String label, final ImageIcon imgIcon)
        {
            super(label, imgIcon, SwingConstants.LEFT);
            setHorizontalTextPosition(SwingConstants.LEFT);
            this.al = al;
            setControlSize(this);
            
            //setBorderPainted(false);
            //setBorder(BorderFactory.createEmptyBorder());
            //setOpaque(false);
            //setCursor(handCursor);
            
            //final LinkLabelBtn llb = this;

            addMouseListener(new MouseAdapter()
            {
                public void mouseClicked(MouseEvent e) 
                {
                    al.actionPerformed(new ActionEvent(this, 0, "")); //$NON-NLS-1$
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


}
