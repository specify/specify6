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
package edu.ku.brc.specify.ui;

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.LoanPhysicalObject;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.ui.UIHelper;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Nov 21, 2006
 *
 */
public class LoanSelectPrepsDlg extends JDialog
{
    protected List<CollectionObject> colObjs;
    protected List<ColObjPanel>      colObjPanels = new Vector<ColObjPanel>();
    protected JButton                okBtn;
    protected JLabel                 summaryLabel;
    
    /**
     * @param colObjs
     */
    public LoanSelectPrepsDlg(List<CollectionObject> colObjs)
    {
        this.colObjs = colObjs;
        
        //List<Object> dataObjs = BuildSampleDatabase.createSingleDiscipline("fish", "fish");       
        //List<CollectionObject> colObjs = (List<CollectionObject>)BuildSampleDatabase.getObjectsByClass(dataObjs, CollectionObject.class);
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        
        JPanel mainPanel = new JPanel();
        
        String rowDef = UIHelper.createDuplicateJGoodiesDef("p", "1px,p,4px,", (colObjs.size()*2)-1) + ",10px,p";
        PanelBuilder    pbuilder = new PanelBuilder(new FormLayout("f:p:g", rowDef), mainPanel);
        CellConstraints cc      = new CellConstraints();
        
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
        for (CollectionObject co : colObjs)
        {
            if (i > 0)
            {
                pbuilder.addSeparator("", cc.xy(1,y));
            }
            y += 2;
            
            ColObjPanel panel = new ColObjPanel(co);
            colObjPanels.add(panel);
            panel.addActionListener(al, cl);
            pbuilder.add(panel, cc.xy(1,y));
            y += 2;
            i++;
        }
        okBtn = new JButton(getResourceString("OK"));
        JButton cancel = new JButton(getResourceString("Cancel"));
        y += 2;
        
        summaryLabel = new JLabel("");
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(5, 1, 5, 1));
        p.add(summaryLabel, BorderLayout.CENTER);
        
        contentPanel.add(p, BorderLayout.NORTH);
        contentPanel.add(new JScrollPane(mainPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
        
        p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(5, 0, 2, 0));
        p.add(ButtonBarFactory.buildOKCancelBar(okBtn, cancel), BorderLayout.CENTER);
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
            count += pp.getNewLoanCount();
        }
        okBtn.setEnabled(count > 0);
        //if (count > 0)
        //{
            summaryLabel.setText(String.format("%d Preparation(s) selected", new Object[] {count}));
        //}
    }
    
    public Hashtable<Preparation, Integer> getPreparationCounts()
    {
        Hashtable<Preparation, Integer> hash = new Hashtable<Preparation, Integer>();
        
        for (ColObjPanel colObjPanel : colObjPanels)
        {
            for (PrepPanel pp : colObjPanel.getPanels())
            {
                if (pp.getCount() > 0)
                {
                    hash.put(pp.getPreparation(), pp.getCount());
                }
            }
        }
        return hash;
    }
    
    
    class ColObjPanel extends JPanel
    {
        protected CollectionObject colObj;
        protected JCheckBox checkBox;
        protected Vector<PrepPanel> panels = new Vector<PrepPanel>();       
        
        /**
         * @param colObj
         */
        public ColObjPanel(CollectionObject colObj)
        {
            super();
            
            this.colObj = colObj;
            
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
            
            pbuilder.add(checkBox = new JCheckBox(descr), cc.xy(1,1));
            //builder.add(new JLabel(String.format("%6.0f", new Object[]{colObj.getCatalogNumber()})), cc.xy(1,1));
            checkBox.setSelected(true);
            
            JPanel outerPanel = new JPanel();
            outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));
            outerPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
            
            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            outerPanel.add(contentPanel);
            
            Color[] colors = new Color[] { new Color(255,255,255), new Color(235,235,255)};
            
            int i = 0;
            for (Preparation prep : colObj.getPreparations())
            {
                PrepPanel pp = new PrepPanel(prep);
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
                    System.out.println(getNewLoanCount());
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
    
    class PrepPanel extends JPanel
    {
        protected Preparation prep;
        protected JLabel      label    = null;
        protected JLabel      label2    = null;
        protected JSpinner    spinner; 

        /**
         * @param prep
         */
        public PrepPanel(final Preparation prep)
        {
            super();
            this.prep = prep;
            
            setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
            //setBorder(BorderFactory.createCompoundBorder(new CurvedBorder(new Color(160,160,160)), getBorder()));
     
            PanelBuilder    pbuilder = new PanelBuilder(new FormLayout("max(120px;p),2px,max(50px;p),2px,p:g", "c:p"), this);
            CellConstraints cc      = new CellConstraints();
            
            
            pbuilder.add(label = new JLabel(prep.getPrepType().getName()), cc.xy(1,1));
            label.setOpaque(false);
            
            //JPanel contentPanel = new JPanel();
            //contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            if (prep.getCount() !=  null)
            {
                int count       = prep.getCount() == null ? 0 : prep.getCount();
                int quantityOut = 0;
                
                if (prep.getLoanPhysicalObjects().size() > 0)
                {
                    for (LoanPhysicalObject lpo : prep.getLoanPhysicalObjects())
                    {
                        int quantityLoaned   = lpo.getQuantity() != null ? lpo.getQuantity() : 0;
                        int quantityReturned = lpo.getQuantityReturned() != null ? lpo.getQuantityReturned() : 0;
                        
                        quantityOut = quantityLoaned - quantityReturned;
                    }
                }
                
                int quantityAvailable = count - quantityOut;
                if (quantityAvailable > 0)
                {
                    SpinnerModel model = new SpinnerNumberModel(0, //initial value
                                               0, //min
                                               quantityAvailable, //max
                                               1);                //step
                    spinner = new JSpinner(model);
                    pbuilder.add(spinner, cc.xy(3, 1));
                    pbuilder.add(label2 = new JLabel(" of " + Integer.toString(quantityAvailable)), cc.xy(5, 1));
                    
                    
                } else
                {
                    pbuilder.add(label2 = new JLabel("(None Available)"), cc.xywh(3, 1, 3, 1));
                }
            } else
            {
                SpinnerModel model = new SpinnerNumberModel(0, //initial value
                        0,    //min
                        10000, //max
                        1);   //step
                spinner = new JSpinner(model);
                pbuilder.add(spinner, cc.xy(3, 1));
                pbuilder.add(label2 = new JLabel(" (Unknown Number Available)"), cc.xy(5, 1));
            }
            //pbuilder.add(contentPanel, cc.xy(1,3));
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
                
            } else
            {
                return 0;
            }
        }
    }
    



}
