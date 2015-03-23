/* Copyright (C) 2015, University of Kansas Center for Research
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
package edu.ku.brc.specify.ui;

import static edu.ku.brc.ui.UIHelper.createCheckBox;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.setControlSize;
import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

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
import java.util.Comparator;
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
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.af.ui.db.ViewBasedDisplayDialog;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.persist.ViewIFace;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.Loan;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.busrules.AccessionBusRules;
import edu.ku.brc.ui.ColorWrapper;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * Creates a dialog listing all the Preparations that are available to be loaned or gifted.
 * 
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Oct 10, 2008
 *
 */
public class SelectPrepsDlg extends CustomDialog
{
    protected ColorWrapper              requiredfieldcolor = AppPrefsCache.getColorWrapper("ui", "formatting", "requiredfieldcolor"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    protected List<CollectionObject>    colObjs;
    protected List<ColObjPanel>         colObjPanels = new Vector<ColObjPanel>();
    protected JLabel                    summaryLabel;
    
    protected Hashtable<Integer, String>     prepTypeHash;
    protected Hashtable<Integer, ColObjInfo> coToPrepHash;
    
    /**
     * @param colObjs
     * @param prepProvider
     */
    public SelectPrepsDlg(final Hashtable<Integer, ColObjInfo> coToPrepHash,
                          final Hashtable<Integer, String>     prepTypeHash,
                          final String                         title)
    {
        super((Frame)UIRegistry.getTopWindow(), getLocalizedMessage("LoanSelectPrepsDlg.CREATE_FR_PREP", title),//$NON-NLS-1$
                true, OKCANCELAPPLYHELP, null);
        
        this.coToPrepHash = coToPrepHash;
        this.prepTypeHash = prepTypeHash;
    }

        
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        applyLabel = getResourceString("SELECTALL");//$NON-NLS-1$
        
        super.createUI();
        
        Vector<ColObjInfo> coList = new Vector<ColObjInfo>(coToPrepHash.values());
        Collections.sort(coList, new Comparator<ColObjInfo>() {
            @Override
            public int compare(ColObjInfo o1, ColObjInfo o2)
            {
                return o1.getCatNo().compareTo(o2.getCatNo());
            }
        });
        
        int cnt = 0;
        Vector<ColObjInfo> coFilteredList = new Vector<ColObjInfo>();
        for (ColObjInfo colObjInfo : coList)
        {
            if (StringUtils.isNotEmpty(colObjInfo.getCatNo()) &&
                colObjInfo.getPreps() != null &&
                colObjInfo.getPreps().size() > 0)
            {
                coFilteredList.add(colObjInfo);
                cnt += colObjInfo.getPreps().size();
            }
        }
        
        String rowDef = UIHelper.createDuplicateJGoodiesDef("p", "1px,p,4px", (cnt*2)-1) + ",10px,p"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
        
        DBTableInfo colObjTI = DBTableIdMgr.getInstance().getInfoById(CollectionObject.getClassTableId());
        DBFieldInfo colObjFI = colObjTI.getFieldByColumnName("CatalogNumber");
        
        int i = 0;
        int y = 1;
        for (ColObjInfo colObjInfo : coFilteredList)
        {
            if (i > 0)
            {
                pbuilder.addSeparator("", cc.xy(1,y)); //$NON-NLS-1$
            }
            y += 2;
            
            colObjInfo.setCatNo((String)colObjFI.getFormatter().formatToUI(colObjInfo.getCatNo()));
            ColObjPanel panel = new ColObjPanel(this, colObjInfo);
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
    public Hashtable<Integer, Integer> getPreparationCounts()
    {
        Hashtable<Integer, Integer> hash = new Hashtable<Integer, Integer>();
        
        for (ColObjPanel colObjPanel : colObjPanels)
        {
            if (colObjPanel.isColObjEnabled())
            {
                for (PrepPanel pp : colObjPanel.getPanels())
                {
                    if (pp.getCount() > 0)
                    {
                        hash.put(pp.getPrepId(), pp.getCount());
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
        protected ColObjInfo        colObjInfo;
        protected JCheckBox         checkBox;
        protected Vector<PrepPanel> panels = new Vector<PrepPanel>();       
        protected JDialog           dlgParent;
        
        /**
         * @param dlgParent
         * @param colObjInfo
         */
        public ColObjPanel(final JDialog    dlgParent, 
                           final ColObjInfo colObjInfo)
        {
            super();
            this.dlgParent  = dlgParent;
            this.colObjInfo = colObjInfo;
            
            setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
            //setBorder(BorderFactory.createCompoundBorder(new CurvedBorder(new Color(160,160,160)), getBorder()));
            //setBorder(new CurvedBorder(new Color(160,160,160)));
     
            PanelBuilder    pbuilder = new PanelBuilder(new FormLayout("f:p:g", "p,5px,p"), this); //$NON-NLS-1$ //$NON-NLS-2$
            CellConstraints cc      = new CellConstraints();
     
            String taxonName = colObjInfo.getTaxonName();
            if (StringUtils.isEmpty(taxonName))
            {
                taxonName = getResourceString("LoanSelectPrepsDlg.UNDET");
            }
            String descr     = String.format(getResourceString("LoanSelectPrepsDlg.TITLE_PAIR"), colObjInfo.getCatNo(), taxonName); //$NON-NLS-1$
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
            
            int i = 0;
            for (PrepInfo prepInfo : colObjInfo.getPreps().values())
            {
                PrepPanel pp = new PrepPanel(dlgParent, prepInfo);
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
            String taxonName = null;
            if (deter.getPreferredTaxon() != null)
            {
                if (deter.getPreferredTaxon().getFullName() == null)
                {
                    Taxon  parent = deter.getPreferredTaxon().getParent();
                    String genus  = parent.getFullName() == null ? parent.getName() : parent.getFullName();
                    taxonName = genus + " " + deter.getPreferredTaxon().getName(); //$NON-NLS-1$
                    
                } else
                {
                    taxonName = deter.getPreferredTaxon().getFullName();
                }
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
        protected PrepInfo    prepInfo;
        protected JLabel      label       = null;
        protected JLabel      label2      = null;
        protected JComponent  prepInfoBtn = null;
        protected JSpinner    spinner; 
        protected JDialog     parent;
        protected int         maxValue = 0;
        protected boolean     unknownQuantity;

        /**
         * @param prep
         */
        public PrepPanel(final JDialog parent, 
                         final PrepInfo prepInfo)
        {
            super();
            this.prepInfo = prepInfo;
            this.parent   = parent;

            setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
            //setBorder(BorderFactory.createCompoundBorder(new CurvedBorder(new Color(160,160,160)), getBorder()));
     
            PanelBuilder    pbuilder = new PanelBuilder(new FormLayout("max(120px;p),2px,max(50px;p),2px,p,2px,p:g", "c:p"), this); //$NON-NLS-1$ //$NON-NLS-2$
            CellConstraints cc      = new CellConstraints();
            
            label = createLabel(prepTypeHash.get(prepInfo.getType()));
                    
            pbuilder.add(label, cc.xy(1,1));
            label.setOpaque(false);
            
            int quantityAvailable = prepInfo.getAvailable();
            if (quantityAvailable > 0)
            {
                maxValue = quantityAvailable;
                
                SpinnerModel model = new SpinnerNumberModel(0, //initial value
                                           0,                  //min
                                           quantityAvailable,  //max
                                           1);                 //step
                spinner = new JSpinner(model);
                fixBGOfJSpinner(spinner);
                pbuilder.add(spinner, cc.xy(3, 1));
                String fmtStr = getLocalizedMessage("LoanSelectPrepsDlg.OF_QUANT_OUT", quantityAvailable); //$NON-NLS-1$
                pbuilder.add(label2 = createLabel(fmtStr), cc.xy(5, 1));
                
                int onLoanQty = prepInfo.getQtyPrep() - quantityAvailable;
                if (onLoanQty > 0)
                {
                    fmtStr = getLocalizedMessage("LoanSelectPrepsDlg.NUM_ON_LOAN", onLoanQty); //$NON-NLS-1$
                    prepInfoBtn = new LinkLabelBtn(this, fmtStr, IconManager.getIcon("InfoIcon", IconManager.IconSize.Std16)); //$NON-NLS-1$
                    pbuilder.add(prepInfoBtn, cc.xy(7, 1));
                }
                
            } else
            {
                pbuilder.add(label2 = createLabel(getResourceString("LoanSelectPrepsDlg.NONE_AVAIL")), cc.xywh(3, 1, 5, 1));//$NON-NLS-1$
            }
        }
        
        /**
         * @param spin
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
         * @return
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
         * @return
         */
        public Integer getPrepId()
        {
            return prepInfo.getPrepId();
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
        
        public void actionPerformed(final ActionEvent e)
        {
            final JStatusBar statusBar = UIRegistry.getStatusBar();
            statusBar.setIndeterminate("LoanLoader", true);
            
            UIRegistry.writeSimpleGlassPaneMsg(getResourceString("NEW_INTER_LOADING_PREP"), 24);
 
            LoanLoader loanLoader = new LoanLoader(parent, prepInfo.getPrepId());
            loanLoader.execute();
            
        }
    }
    
    protected static final Cursor handCursor    = new Cursor(Cursor.HAND_CURSOR);
    protected static final Cursor defCursor     = new Cursor(Cursor.DEFAULT_CURSOR);

    //-----------------------------------------------------
    class LinkLabelBtn extends JLabel
    {
        protected ActionListener al;
        
        public LinkLabelBtn(final ActionListener al, final String label, final ImageIcon imgIcon)
        {
            super(label, imgIcon, SwingConstants.LEFT);
            setHorizontalTextPosition(SwingConstants.LEFT);
            this.al = al;
            setControlSize(this);
            
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
    
    //--------------------------------------------------------------
    // Background loader class for loading a large number of loan preparations
    //--------------------------------------------------------------
    class LoanLoader extends javax.swing.SwingWorker<Integer, Integer>
    {
        private int        prepId;
        private List<Loan> loans = null;
        private JDialog    parent;
        
        /**
         * @param prepId
         */
        public LoanLoader(final JDialog parent, final int prepId)
        {
            this.parent = parent;
            this.prepId = prepId;
        }

        /* (non-Javadoc)
         * @see javax.swing.SwingWorker#doInBackground()
         */
        @SuppressWarnings("unchecked")
        @Override
        protected Integer doInBackground() throws Exception
        {
            
            String sql = "SELECT loan.LoanID FROM preparation p "+
            "INNER JOIN loanpreparation lp ON p.PreparationID = lp.PreparationID " +
            "INNER JOIN loan ON lp.LoanID = loan.LoanID " +
            "WHERE p.PreparationID = " + prepId;
            
            //System.out.println(sql);
            //System.out.println(" prep.getPreparationId() "+prep.getPreparationId());
            
            StringBuilder sb = new StringBuilder();
            Vector<Object[]> rows = BasicSQLUtils.query(sql);
            for (Object[] cols : rows)
            {
               if (sb.length() > 0) sb.append(',');
               sb.append(cols[0]);
            }
            
            
            DataProviderSessionIFace session = null;
            try
            {
               session = DataProviderFactory.getInstance().createSession();
               loans = (List<Loan>)session.getDataList("FROM Loan WHERE loanId in("+sb.toString()+")");
               
            } catch (Exception ex)
            {
               edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(AccessionBusRules.class, ex);
               ex.printStackTrace();
               UsageTracker.incrNetworkUsageCount();
               
            } finally
            {
               if (session != null)
               {
                   session.close();
               }
            }

            return 0;
        }

        /* (non-Javadoc)
         * @see javax.swing.SwingWorker#done()
         */
        @Override
        protected void done()
        {
            super.done();
            UIRegistry.getStatusBar().setProgressDone("LoanLoader");
            UIRegistry.clearSimpleGlassPaneMsg();
            
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

}
