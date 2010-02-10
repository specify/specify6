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
package edu.ku.brc.specify.ui;

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIHelper.createCheckBox;
import static edu.ku.brc.ui.UIHelper.createI18NLabel;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.setControlSize;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
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
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.af.ui.db.ViewBasedDisplayDialog;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.ViewFactory;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.persist.FormCellField;
import edu.ku.brc.af.ui.forms.persist.FormCellFieldIFace;
import edu.ku.brc.af.ui.forms.persist.FormCellIFace;
import edu.ku.brc.af.ui.forms.persist.ViewIFace;
import edu.ku.brc.af.ui.forms.validation.FormValidator;
import edu.ku.brc.af.ui.forms.validation.UIValidatable;
import edu.ku.brc.af.ui.forms.validation.UIValidator;
import edu.ku.brc.af.ui.forms.validation.ValComboBoxFromQuery;
import edu.ku.brc.af.ui.forms.validation.ValFormattedTextFieldSingle;
import edu.ku.brc.af.ui.forms.validation.ValidationListener;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.Loan;
import edu.ku.brc.specify.datamodel.LoanPreparation;
import edu.ku.brc.specify.datamodel.LoanReturnPreparation;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.ui.ColorWrapper;
import edu.ku.brc.ui.DateWrapper;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.VerticalSeparator;
import edu.ku.brc.util.Pair;

/**
 * Creates a dialog representing all the Preparation objects being returned for a loan.
 * TODO: Convert to use CustomDialog
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
    protected FormValidator          validator = new FormValidator(null);
    protected ValComboBoxFromQuery   agentCBX;
    protected boolean                isCancelled = true;
    protected ValFormattedTextFieldSingle dateClosed;
    
    /**
     * Constructor.
     * @param loan the loan
     */
    public LoanReturnDlg(final Loan loan)
    {
        this.loan = loan;
        
        ImageIcon appIcon = IconManager.getIcon("AppIcon"); //$NON-NLS-1$
        if (appIcon != null)
        {
            setIconImage(appIcon.getImage());
        }

        
    }
    
    /**
     * @return
     */
    public boolean createUI()
    {
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            
            loan = session.merge(loan);
        
            setTitle(getResourceString("LOANRET_TITLE"));
            
            validator.addValidationListener(new ValidationListener() {
                public void wasValidated(UIValidator val)
                {
                    doEnableOKBtn();
                }
            });
            
             
            JPanel contentPanel = new JPanel(new BorderLayout());
            
            JPanel mainPanel = new JPanel();
            
            System.out.println("Num Loan Preps for Loan: "+loan.getLoanPreparations());
            
            HashMap<Integer, Pair<CollectionObject, Vector<LoanPreparation>>> colObjHash = new HashMap<Integer, Pair<CollectionObject, Vector<LoanPreparation>>>();
            for (LoanPreparation loanPrep : loan.getLoanPreparations())
            {
                CollectionObject        colObj = loanPrep.getPreparation().getCollectionObject();
                System.out.println("For LoanPrep ColObj Is: "+colObj.getIdentityTitle());
                
                Vector<LoanPreparation> list = null;
                Pair<CollectionObject, Vector<LoanPreparation>> pair = colObjHash.get(colObj.getId());
                if (pair == null)
                {
                    list = new Vector<LoanPreparation>();
                    colObjHash.put(colObj.getId(), new Pair<CollectionObject, Vector<LoanPreparation>>(colObj, list));
                } else
                {
                     list = pair.second;

                }
                list.add(loanPrep);
            }
            
            int             colObjCnt = colObjHash.size();
            String          rowDef    = UIHelper.createDuplicateJGoodiesDef("p", "1px,p,4px", (colObjCnt*2)-1);
            PanelBuilder    pbuilder  = new PanelBuilder(new FormLayout("f:p:g", rowDef), mainPanel);
            CellConstraints cc        = new CellConstraints();
            
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
    
            Vector<Pair<CollectionObject, Vector<LoanPreparation>>> pairList = new Vector<Pair<CollectionObject, Vector<LoanPreparation>>>(colObjHash.values());
            
            Collections.sort(pairList, new Comparator<Pair<CollectionObject, Vector<LoanPreparation>>>()
            {
                @Override
                public int compare(Pair<CollectionObject, Vector<LoanPreparation>> o1,
                                   Pair<CollectionObject, Vector<LoanPreparation>> o2)
                {
                    return o1.first.getIdentityTitle().compareTo(o2.first.getIdentityTitle());
                }
            });

            for (Pair<CollectionObject, Vector<LoanPreparation>> pair : pairList)
            {
                CollectionObject co = pair.first;
                
                if (i > 0)
                {
                    pbuilder.addSeparator("", cc.xy(1,y));
                    y += 2;
                }
                
                ColObjPanel panel = new ColObjPanel(session, this, co, colObjHash.get(co.getId()).second);
                colObjPanels.add(panel);
                panel.addActionListener(al, cl);
                pbuilder.add(panel, cc.xy(1,y));
                y += 2;
                i++;
            }
            
            JButton selectAllBtn = createButton(getResourceString("SELECTALL"));
            okBtn = createButton(getResourceString("SAVE"));
            JButton cancel = createButton(getResourceString("CANCEL"));
            
            PanelBuilder pb = new PanelBuilder(new FormLayout("p,2px,p,2px,p,2px,p,2px,p,2px,p", "p"));
            
            dateClosed = new ValFormattedTextFieldSingle("Date", false, false, 10);
            dateClosed.setNew(true);
            dateClosed.setValue(null, "");
            dateClosed.setRequired(true);
            validator.hookupTextField(dateClosed,
                    "2",
                    true,
                    UIValidator.Type.Changed,  
                    "", 
                    false);
            summaryLabel = createLabel("");
            pb.add(summaryLabel,                     cc.xy(1, 1));
            pb.add(createI18NLabel("LOANRET_AGENT"), cc.xy(3, 1));
            pb.add(agentCBX = createAgentCombobox(), cc.xy(5, 1));
            pb.add(createI18NLabel("ON"),            cc.xy(7, 1));
            pb.add(dateClosed,                       cc.xy(9, 1));

            contentPanel.add(pb.getPanel(), BorderLayout.NORTH);
            contentPanel.add(new JScrollPane(mainPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER));
            
            JPanel p = new JPanel(new BorderLayout());
            p.setBorder(BorderFactory.createEmptyBorder(5, 0, 2, 0));
            p.add(ButtonBarFactory.buildOKCancelApplyBar(okBtn, cancel, selectAllBtn), BorderLayout.CENTER);
            contentPanel.add(p, BorderLayout.SOUTH);
            contentPanel.setBorder(BorderFactory.createEmptyBorder(4, 12, 2, 12));
            
            setContentPane(contentPanel);
            
            doEnableOKBtn();
    
            //setIconImage(IconManager.getIcon("Preparation", IconManager.IconSize.Std16).getImage());
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            
            doEnableOKBtn();
            
            okBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae)
                {
                    setVisible(false);
                    isCancelled = false;
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
            
            return true;
        
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(LoanReturnDlg.class, ex);
            // Error Dialog
            ex.printStackTrace();
            
        } finally 
        {
            if (session != null)
            {
                session.close();
            }
        }
        return false;
    }
    
    /**
     * 
     */
    protected void doEnableOKBtn()
    {
        int retCnt = 0;
        int resCnt = 0;
        for (ColObjPanel colObjPanel : colObjPanels)
        {
            retCnt += colObjPanel.getReturnedCount();
            resCnt += colObjPanel.getResolvedCount();
        }
        okBtn.setEnabled((retCnt > 0 || resCnt > 0) && agentCBX.getValue() != null && dateClosed.getValue() != null);

        summaryLabel.setText(String.format(getResourceString("LOANRET_NUM_ITEMS_2B_RET_FMT"), retCnt, resCnt));
    }
    
    /**
     * @return the return agent combobox
     */
    protected ValComboBoxFromQuery createAgentCombobox()
    {
        FormCellField fcf = new FormCellField(FormCellIFace.CellType.field,
                                               "1", "agent", FormCellFieldIFace.FieldType.querycbx, 
                                               FormCellFieldIFace.FieldType.querycbx, 
                                               "", "Agent", "", true,
                                               1, 1, 1, 1, "Changed", null, false);
        fcf.addProperty("name", "Agent");
        fcf.addProperty("title", getResourceString("LOANRET_AGENT_DO_RET_TITLE"));
        ValComboBoxFromQuery cbx = ViewFactory.createQueryComboBox(validator, fcf, true, true);
        cbx.setAsNew(true);
        cbx.setState(UIValidatable.ErrorType.Incomplete);
        return cbx;
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
     * Returns whether the dialog was canceled.
     * @return whether the dialog was canceled.
     */
    public boolean isCancelled()
    {
        return isCancelled;
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
                if (pp.getReturnedCount() > 0 || pp.getResolvedCount() > 0)
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
         * @param session
         * @param dlgParent
         * @param colObj
         * @param lpoList
         */
        public ColObjPanel(final DataProviderSessionIFace session,
                           final JDialog               dlgParent, 
                           final CollectionObject      colObj,
                           final List<LoanPreparation> lpoList)
        {
            super();
            
            this.dlgParent = dlgParent;
            this.colObj    = colObj;
            
            session.attach(colObj);
            
            setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
            //setBorder(BorderFactory.createCompoundBorder(new CurvedBorder(new Color(160,160,160)), getBorder()));
            //setBorder(new CurvedBorder(new Color(160,160,160)));
     
            PanelBuilder    pbuilder = new PanelBuilder(new FormLayout("f:p:g", "p,5px,p"), this);
            CellConstraints cc      = new CellConstraints();
     
            String taxonName = "";
            for (Determination deter : colObj.getDeterminations())
            {
                if (deter.isCurrentDet())
                {
                    if (deter.getPreferredTaxon().getFullName() == null)
                    {
                        Taxon parent = deter.getPreferredTaxon().getParent();
                        String genus = parent.getFullName() == null ? parent.getName() : parent.getFullName();
                        taxonName = genus + " " + deter.getPreferredTaxon().getName();
                        
                    } else
                    {
                        taxonName = deter.getPreferredTaxon().getFullName();
                    }

                    break;
                }
            }
            
            String descr = String.format("%s - %s", colObj.getIdentityTitle(), taxonName);
            descr = StringUtils.stripToEmpty(descr);
            
            checkBox = createCheckBox(descr);
            pbuilder.add(createLabel(descr), cc.xy(1,1));
            checkBox.setSelected(true);
            
            JPanel outerPanel = new JPanel();
            outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));
            outerPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
            
            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            outerPanel.add(contentPanel);
            
            Color[] colors = new Color[] { new Color(255,255,255), new Color(235,235,255)};
            
            System.out.println(colObj.getIdentityTitle()+"  lpoList.size: "+lpoList.size());
            int i = 0;
            for (LoanPreparation lpo : lpoList)
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
        
        public int getReturnedCount()
        {
            int count = 0;
            if (checkBox.isSelected())
            {
                for (PrepPanel pp : panels)
                {
                    count += pp.getReturnedCount();
                }
            }
            return count;
        }

        public int getResolvedCount()
        {
            int count = 0;
            if (checkBox.isSelected())
            {
                for (PrepPanel pp : panels)
                {
                    count += pp.getResolvedCount();
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
    // Return panel for each LoanPreparation.
    //------------------------------------------------------------------------------------------
    class PrepPanel extends JPanel implements ActionListener
    {
        protected Preparation prep;
        protected LoanPreparation lpo;
        protected JLabel      label       = null;
        protected JLabel      retLabel    = null;
        protected JLabel      resLabel    = null;
        protected JComponent  prepInfoBtn = null;
        protected JSpinner    returnedSpinner; 
        protected JSpinner    resolvedSpinner; 
        protected JDialog     parent;
        protected JTextField  remarks;
        
        protected int         quantityReturned;
        protected int         quantityResolved;
        protected int         quantityLoaned;
        protected int         maxValue = 0;
        protected boolean     unknownQuantity;

        /**
         * Constructs a panel representing the Preparation being returned.
         * @param parent the parent dialog
         * @param lpo the LoanPreparation being returned
         */
        public PrepPanel(final JDialog parent, 
                         final LoanPreparation lpo)
        {
            super();
            this.prep   = lpo.getPreparation();
            this.lpo    = lpo;
            this.parent = parent;
            
            Color color = new Color(192, 192, 192);
            Color bg = color.darker();
            Color fg = color.brighter();
            
            setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
            //setBorder(BorderFactory.createCompoundBorder(new CurvedBorder(new Color(160,160,160)), getBorder()));
     
            FormLayout fl = new FormLayout("max(120px;p),p," +
                                           "max(50px;p),2px,p,p," + // 3,4,5,6
                                           "max(50px;p),2px,p,p," + // 7,8,9,10
                                           "f:p:g", //"p,0px,p:g", 
                                           "f:p:g,2px,p");
            PanelBuilder    pbuilder = new PanelBuilder(fl, this);
            CellConstraints cc       = new CellConstraints();
            
            int x = 1;
            pbuilder.add(label = createLabel(prep.getPrepType().getName()), cc.xy(x,1)); x += 1;  // 1
            label.setOpaque(false);
            
            pbuilder.add(new VerticalSeparator(fg, bg, 20), cc.xy(x,1)); x += 1; // 2
            
            boolean allReturned = false;
            
            if (prep.getCountAmt() !=  null)
            {
                quantityLoaned    = lpo.getQuantity();
                quantityReturned  = lpo.getQuantityReturned();
                quantityResolved  = lpo.getQuantityResolved();
                
                int quantityResOut   = quantityLoaned - quantityResolved;
                int quantityRetOut   = quantityLoaned - quantityReturned;
                
                if ((quantityResOut > 0 || quantityRetOut > 0) && !lpo.getIsResolved())
                {
                    maxValue = quantityLoaned;
                    
                    SpinnerModel retModel = new SpinnerNumberModel(quantityReturned, //initial value
                                               quantityReturned, //min
                                               quantityLoaned,   //max
                                               1);               //step
                    returnedSpinner = new JSpinner(retModel);
                    fixBGOfJSpinner(returnedSpinner);
                    pbuilder.add(returnedSpinner, cc.xy(x, 1)); x += 2; // 3
                    setControlSize(returnedSpinner);
                    
                    String fmtStr = String.format(getResourceString("LOANRET_OF_FORMAT_RET"), quantityLoaned);
                    pbuilder.add(retLabel = createLabel(fmtStr), cc.xy(x, 1)); x += 1; // 5
                    
                    pbuilder.add(new VerticalSeparator(fg, bg, 20), cc.xy(x,1)); x += 1; // 6
                    
                    SpinnerModel resModel = new SpinnerNumberModel(quantityResolved, //initial value
                            quantityResolved, //min
                            quantityLoaned,   //max
                            1);               //step
                    resolvedSpinner = new JSpinner(resModel);
                    fixBGOfJSpinner(resolvedSpinner);
                    pbuilder.add(resolvedSpinner, cc.xy(x, 1)); x += 2; // 7
                    setControlSize(resolvedSpinner);
                    
                    fmtStr = String.format(getResourceString("LOANRET_OF_FORMAT_RES"), quantityLoaned);
                    pbuilder.add(retLabel = createLabel(fmtStr), cc.xy(x, 1)); x += 1; // 9
                    
                    ChangeListener cl = new ChangeListener()
                    {
                        @Override
                        public void stateChanged(ChangeEvent e)
                        {
                            int lrpResolvedQty = (Integer)resolvedSpinner.getValue();
                            int lrpReturnedQty = (Integer)returnedSpinner.getValue();
                            
                            if (e != null)
                            {
                                if (e.getSource() == resolvedSpinner)
                                {
                                    if (lrpResolvedQty < lrpReturnedQty)
                                    {
                                        lrpReturnedQty = lrpResolvedQty;
                                        final int qty = lrpReturnedQty;
                                        SwingUtilities.invokeLater(new Runnable() {
                                            @Override
                                            public void run()
                                            {
                                                returnedSpinner.setValue(qty);
                                            }
                                        });
                                    }
                                } else if (e.getSource() == returnedSpinner)
                                {
                                    if (lrpReturnedQty > lrpResolvedQty)
                                    {
                                        lrpResolvedQty = lrpReturnedQty;
                                        final int qty = lrpReturnedQty;
                                        SwingUtilities.invokeLater(new Runnable() {
                                            @Override
                                            public void run()
                                            {
                                                resolvedSpinner.setValue(qty);
                                            }
                                        });
                                    }
                                }
                            }
                        }
                    };
                    returnedSpinner.addChangeListener(cl);
                    resolvedSpinner.addChangeListener(cl);
                    
                } else
                {
                    Calendar lastReturnDate = null;
                    for (LoanReturnPreparation lrpo : lpo.getLoanReturnPreparations())
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
                        
                    String fmtStr = lastReturnDate == null ? getResourceString("LOANRET_ALL_RETURNED") :
                                 String.format(getResourceString("LOANRET_ALL_RETURNED_ON_FMT"), 
                                               scrDateFormat.format(lastReturnDate));
                    pbuilder.add(retLabel = createLabel(fmtStr), cc.xy(x, 1));
                    allReturned = true;
                }

                
            } else
            {
                pbuilder.add(retLabel = createLabel(" " + getResourceString("LOANRET_UNKNOWN_NUM_AVAIL")), cc.xywh(1, 1, 4, 1));
                unknownQuantity = true;
            }

            if (!allReturned)
            {
                remarks = new RemarksText();
                pbuilder.add(remarks, cc.xywh(1, 3, 11, 1));
            }
            
            /*if (returnedSpinner != null)
            {
                returnedSpinner.addChangeListener(new ChangeListener()
                {
                    public void stateChanged(ChangeEvent ae)
                    {
                        Integer val = (Integer)returnedSpinner.getValue();
                    }
                });
            }*/
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
            if (returnedSpinner != null)
            {
                returnedSpinner.setValue(quantityLoaned);
            }
            if (resolvedSpinner != null)
            {
                resolvedSpinner.setValue(quantityLoaned);
            }
        }

        /**
         * Returns the LoanPreparation for this panel.
         * @return the LoanPreparation for this panel.
         */
        public LoanPreparation getLoanPreparation()
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
            if (retLabel != null)
            {
                retLabel.setEnabled(enabled);
            }
            if (resLabel != null)
            {
                resLabel.setEnabled(enabled);
            }
            if (prepInfoBtn != null)
            {
                prepInfoBtn.setEnabled(enabled);
            }
            if (returnedSpinner != null)
            {
                returnedSpinner.setEnabled(enabled);
            }
        }
        
        /**
         * Adds a change listener.
         * @param cl the change listener
         */
        public void addChangeListener(final ChangeListener cl)
        {
            if (returnedSpinner != null)
            {
                returnedSpinner.addChangeListener(cl);
            }
            if (resolvedSpinner != null)
            {
                resolvedSpinner.addChangeListener(cl);
            }
        }
        
        /**
         * Returns the count from the spinner, the count of items being returning.
         * @return the count from the spinner, the count of items being returning.
         */
        public int getReturnedCount()
        {
            if (returnedSpinner != null)
            {
                Object valObj = returnedSpinner.getValue();
               return valObj == null ? 0 : ((Integer)valObj).intValue();
                
            }
            // else
            return 0;
        }
        
        public int getResolvedCount()
        {
            if (resolvedSpinner != null)
            {
                Object valObj = resolvedSpinner.getValue();
               return valObj == null ? 0 : ((Integer)valObj).intValue();
                
            }
            // else
            return 0;
        }
        
        /**
         * Returns the LoanReturnInfo describing the user input for the loan return.
         * @return the LoanReturnInfo describing the user input for the loan return
         */
        public LoanReturnInfo getLoanReturnInfo()
        {
            return new LoanReturnInfo(lpo, 
                                      remarks != null ? remarks.getText() : null,
                                      getReturnedCount(),
                                      getResolvedCount(),
                                      getResolvedCount() == quantityLoaned);
        }
        
        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e)
        {
            List<Loan> loans = new Vector<Loan>();
            
            DataProviderSessionIFace session = null;
            try
            {
                session = DataProviderFactory.getInstance().createSession();
                
                session.attach(prep);

                for (LoanPreparation loanPO : prep.getLoanPreparations())
                {
                    loans.add(loanPO.getLoan());
                }
            } catch (Exception ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(LoanReturnDlg.class, ex);
                // Error Dialog
                ex.printStackTrace();
                
            } finally 
            {
                if (session != null)
                {
                    session.close();
                }
            }
            
            ViewIFace view  = AppContextMgr.getInstance().getView("Loan");
            final ViewBasedDisplayDialog dlg = new ViewBasedDisplayDialog(parent,
                    view.getViewSetName(),
                    "Loan",
                    null,
                    getResourceString("LoanSelectPrepsDlg.IAT_LOAN_REVIEW"),
                    getResourceString("CLOSE"),
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
    public class LoanReturnInfo
    {
        protected LoanPreparation lpo;
        protected boolean         isResolved;
        protected String          remarks;
        protected int             returnedQty;
        protected int             resolvedQty;
        
        public LoanReturnInfo(LoanPreparation lpo, 
                              String remarks, 
                              int returnedQty, 
                              int resolvedQty,
                              boolean isResolved)
        {
            super();
            this.lpo = lpo;
            this.remarks = remarks;
            this.returnedQty = returnedQty;
            this.resolvedQty = resolvedQty;
            this.isResolved  = isResolved;
        }
        public LoanPreparation getLoanPreparation()
        {
            return lpo;
        }
        public String getRemarks()
        {
            return remarks;
        }
        public int getReturnedQty()
        {
            return returnedQty;
        }
        public int getResolvedQty()
        {
            return resolvedQty;
        }
        /**
         * @return the isResolved
         */
        public boolean isResolved()
        {
            return isResolved;
        }
        
    }
    
    class RemarksText extends JTextField
    {
        protected Insets inner;
        protected String bgStr     = getResourceString("LOANRET_REMARKS");
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
