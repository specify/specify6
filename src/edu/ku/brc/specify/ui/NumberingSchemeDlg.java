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

import static edu.ku.brc.ui.UIHelper.createIconBtn;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.createList;
import static edu.ku.brc.ui.UIHelper.createScrollPane;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.AutoNumberingScheme;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.EditDeleteAddPanel;
import edu.ku.brc.ui.UIHelper;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Apr 17, 2009
 *
 */
public class NumberingSchemeDlg extends CustomDialog
{
    private static final Logger log = Logger.getLogger(NumberingSchemeDlg.class);
    
    protected JList              numSchemeList;    // A list of all NumberSchemes
    protected JList              nsCollList;       // Number Scheme Collections List
    protected JList              availNSList;      // Available Number Schemes (with same format) 
    protected JButton            mapToBtn;
    protected JButton            unmapBtn;
    protected JComboBox          typeCBX;
    
    protected boolean            blockUpdate = false;

    protected EditDeleteAddPanel edaPanel;
    
    protected Hashtable<String, Vector<AutoNumberingScheme>> byFormatHash   = new Hashtable<String,  Vector<AutoNumberingScheme>>();
    protected Hashtable<String, AutoNumberingScheme>         byNameHash     = new Hashtable<String, AutoNumberingScheme>();
    
    protected Hashtable<Integer, Division>                   allDivisions   = new Hashtable<Integer, Division>();
    protected Hashtable<Integer, Collection>                 allCollections = new Hashtable<Integer, Collection>();
    
    protected Vector<AutoNumberingScheme>                    ansToBeDeleted = new Vector<AutoNumberingScheme>();
    
    /**
     * @param dialog
     * @param title
     * @param isModal
     * @param whichBtns
     * @param contentPanel
     * @throws HeadlessException
     */
    public NumberingSchemeDlg(Frame frame) throws HeadlessException
    {
        super(frame, "NS_NUM_SCHEME", true, OKCANCELHELP, null);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @SuppressWarnings("unchecked")
    @Override
    public void createUI()
    {
        super.createUI();
        
        mapToBtn = createIconBtn("Map", "WB_ADD_MAPPING_ITEM", new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                removeFromCurrentANS();
            }
        });
        unmapBtn = createIconBtn("Unmap", "WB_REMOVE_MAPPING_ITEM", new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                addToCurrentANS();
            }
        });
        
        typeCBX = UIHelper.createComboBox(new Object[] {"Catalog Numbers", "Acessions", "Other"});
        
        numSchemeList = createList(new DefaultListModel());
        nsCollList    = createList(new DefaultListModel());
        availNSList   = createList(new DefaultListModel());
        
        CellConstraints cc    = new CellConstraints();
        PanelBuilder    topPB = new PanelBuilder(new FormLayout("f:p:g", "f:p:g"));
        topPB.add(createScrollPane(numSchemeList), cc.xy(1, 1));
        
        PanelBuilder middlePanel = new PanelBuilder(new FormLayout("c:p:g", "p, 2px, p"));
        middlePanel.add(mapToBtn, cc.xy(1, 1));
        middlePanel.add(unmapBtn, cc.xy(1, 3));
        
        JPanel btnPanel = middlePanel.getPanel();
        
        PanelBuilder outerMiddlePanel = new PanelBuilder(new FormLayout("c:p:g", "f:p:g, p, f:p:g"));
        outerMiddlePanel.add(btnPanel, cc.xy(1, 2));
        
        PanelBuilder botPB = new PanelBuilder(new FormLayout("f:p:g,4px,p,4px,f:p:g", "p,2px,f:p:g"));
        botPB.add(createLabel("Included in Auto Number Scheme"), cc.xy(1, 1)); 
        botPB.add(createLabel("Available to be Added"),          cc.xy(5, 1));

        botPB.add(outerMiddlePanel.getPanel(),                   cc.xywh(3, 3, 1, 1)); 

        botPB.add(createScrollPane(nsCollList),                  cc.xy(1, 3)); 
        botPB.add(createScrollPane(availNSList),                 cc.xy(5, 3));
        
        PanelBuilder pb = new PanelBuilder(new FormLayout("p,f:p:g", "p,6px,p,2px,f:p:g,16px,f:p:g"));
        
        int y = 1;
        pb.add(typeCBX,                  cc.xy(1 ,y));y += 2;
        pb.add(createLabel("Available Auto Numbering Schemes"), cc.xywh(1, y, 1, 1)); y += 2;
        pb.add(topPB.getPanel(),         cc.xywh(1, y, 2, 1)); y += 2;
        pb.add(botPB.getPanel(),         cc.xywh(1, y, 2, 1)); y += 2;
        
        pb.setDefaultDialogBorder();
        
        contentPanel = pb.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        //loadSchemes();

        pack();
        
        numSchemeList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    ansSelected();
                    fillWithAvail();
                }
            }
        });
        
        nsCollList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    updateMoveBtns(false);
                }
            }
        });
        
        availNSList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    updateMoveBtns(true);
                }
            }
        });
        
        
        typeCBX.setSelectedIndex(0);
        fillCatalogNumANS();
        
        typeCBX.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e)
            {
                JComboBox cb = (JComboBox)e.getSource();
                if (e.getStateChange() == ItemEvent.SELECTED) 
                {
                    switch (cb.getSelectedIndex())
                    {
                        case -1 :
                            break;
                            
                        case 0 :
                            fillCatalogNumANS();
                            break;
                            
                        case 1 :
                            fillAccNumANS();
                            break;
                            
                        case 2 :
                            fillOtherNumANS();
                            break;
                    }
                }
            }
        });
        
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            
            for (Division div : (List<Division>)session.getDataList("FROM Division"))
            {
                div.forceLoad();
                allDivisions.put(div.getId(), div);
            }
            
            for (Collection coll : (List<Collection>)session.getDataList("FROM Collection"))
            {
                coll.forceLoad();
                allCollections.put(coll.getId(), coll);
            }
            
        } catch (Exception ex)
        {
            log.error(ex);
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(NumberingSchemeDlg.class, ex);
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
        
        okBtn.setEnabled(false);
        
    }
    
    private void updateMoveBtns(final Boolean availSel)
    {
        if (!blockUpdate && availSel != null)
        {
            blockUpdate = true;
            if (availSel)
            {
                if (availNSList.getSelectedIndex() > -1)
                {
                    unmapBtn.setEnabled(true);
                    nsCollList.clearSelection();
                } else
                {
                    unmapBtn.setEnabled(false);
                }
                mapToBtn.setEnabled(false);
 
            } else
            {
                if (nsCollList.getSelectedIndex() > -1)
                {
                    mapToBtn.setEnabled(true);
                    availNSList.clearSelection();
                } else
                {
                    mapToBtn.setEnabled(false);
                }
                unmapBtn.setEnabled(false);
            }
            blockUpdate = false;
         }
    }
    
    /**
     * 
     */
    private void fillCatalogNumANS()
    {
        loadSchemes(CollectionObject.getClassTableId());
    }
    
    /**
     * 
     */
    private void fillAccNumANS()
    {
        loadSchemes(Accession.getClassTableId());
    }
    
    /**
     * 
     */
    private void fillOtherNumANS()
    {
        loadSchemes(0); // means anything NOT Cat No or Acc No
    }
    
    /**
     * 
     */
    @SuppressWarnings("unchecked")
    private void loadSchemes(final int tableId)
    {
        DefaultListModel model = (DefaultListModel)numSchemeList.getModel();
        model.clear();
        
        byFormatHash.clear();
        byNameHash.clear();
        
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            String whereStr;
            if (tableId != 0)
            {
                whereStr = "tableNumber = " + tableId;
            } else
            {
                whereStr = "tableNumber <> " + Accession.getClassTableId() + " AND tableNumber <> " + CollectionObject.getClassTableId();
            }
            
            List<AutoNumberingScheme> schemes = (List<AutoNumberingScheme>)session.getDataList("FROM AutoNumberingScheme WHERE " + whereStr);
            for (AutoNumberingScheme ans : schemes)
            {
                model.addElement(ans);
                ans.getCollections().size();
                ans.getDivisions().size();
                
                Vector<AutoNumberingScheme> list = byFormatHash.get(ans.getFormatName());
                if (list == null)
                {
                    list = new Vector<AutoNumberingScheme>();
                    byFormatHash.put(ans.getFormatName(), list);
                }
                list.add(ans);
                byNameHash.put(ans.getSchemeName(), ans);
            }
            
        } catch (Exception ex)
        {
            log.error(ex);
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(NumberingSchemeDlg.class, ex);
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
    }
    
    /**
     * 
     */
    private void ansSelected()
    {
        DefaultListModel model = (DefaultListModel)nsCollList.getModel();
        model.clear();
        
        int typeIndex = typeCBX.getSelectedIndex();
        
        AutoNumberingScheme ans = (AutoNumberingScheme)numSchemeList.getSelectedValue();
        if (ans != null)
        {
            if (typeIndex == 0)
            {
                for (Collection coll : ans.getCollections())
                {
                    model.addElement(coll);
                }
            } else  if (typeIndex == 1)
            {
                for (Division div : ans.getDivisions())
                {
                    model.addElement(div);
                }
            }
        }
  
        model = (DefaultListModel)availNSList.getModel();
        model.clear();
        
        if (ans != null)
        {
            Vector<AutoNumberingScheme> list = byFormatHash.get(ans.getFormatName());
            if (list != null)
            {
                for (AutoNumberingScheme availNS : list)
                {
                    if (availNS != ans)
                    {
                        model.addElement(availNS);
                    }
                }
            }
        }
    }

    /**
     * 
     */
    private void fillWithAvail()
    {
        int typeIndex = typeCBX.getSelectedIndex();
        
        DefaultListModel model = (DefaultListModel)availNSList.getModel();
        model.clear();
        
        AutoNumberingScheme ans = (AutoNumberingScheme)numSchemeList.getSelectedValue();
        if (ans != null)
        {
            String ansFieldFormat = ans.getFormatName();
            HashSet<Integer> idSet = new HashSet<Integer>();
            if (typeIndex == 0)
            {
                
                for (Collection coll : ans.getCollections())
                {
                   idSet.add(coll.getId().intValue());
                }
                
                for (Collection coll : allCollections.values())
                {
                    if (!idSet.contains(coll.getId().intValue()))
                    {
                        if (coll.getNumberingSchemes().size() == 1)
                        {
                            AutoNumberingScheme othANS = coll.getNumberingSchemes().iterator().next();
                            if (othANS.getFormatName().equals(ansFieldFormat))
                            {
                                model.addElement(coll);
                            }
                        }
                    }
                }
                
            } else  if (typeIndex == 1)
            {
                for (Division div : ans.getDivisions())
                {
                    idSet.add(div.getId().intValue());
                }
                
                for (Division div : allDivisions.values())
                {
                    if (!idSet.contains(div.getId().intValue()))
                    {
                        if (div.getNumberingSchemes().size() == 1)
                        {
                            AutoNumberingScheme othANS = div.getNumberingSchemes().iterator().next();
                            if (othANS.getFormatName().equals(ansFieldFormat))
                            {
                                model.addElement(div);
                            }
                        }
                    }
                }
            }
            idSet.clear();
        }
    }

    protected void removeFromCurrentANS()
    {
    }
    
    protected void addToCurrentANS()
    {
        
    }
}
