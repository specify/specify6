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

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
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
import edu.ku.brc.specify.datamodel.AutoNumberingScheme;
import edu.ku.brc.specify.datamodel.Collection;
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
    
    protected JList   numSchemeList;    // A list of all NumberSchemes
    protected JList   nsCollList;       // Number Scheme Collections List
    protected JList   availNSList;      // Available Number Schemes (with same format) 
    protected JButton mapToBtn;
    protected JButton unmapBtn;

    protected EditDeleteAddPanel edaPanel;
    
    protected Hashtable<String, Vector<AutoNumberingScheme>> byFormatHash = new Hashtable<String,  Vector<AutoNumberingScheme>>();
    protected Hashtable<String, AutoNumberingScheme> byNameHash   = new Hashtable<String, AutoNumberingScheme>();
    
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
        super(frame, "NS_NUM_SCHEME", true, OKCANCELAPPLYHELP, null);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        mapToBtn = createIconBtn("Map", "WB_ADD_MAPPING_ITEM", new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                //map();
            }
        });
        unmapBtn = createIconBtn("Unmap", "WB_REMOVE_MAPPING_ITEM", new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                //unmap();
            }
        });
        
        numSchemeList = new JList(new DefaultListModel());
        nsCollList    = new JList(new DefaultListModel());
        availNSList   = new JList(new DefaultListModel());
        
        CellConstraints cc    = new CellConstraints();
        PanelBuilder    topPB = new PanelBuilder(new FormLayout("f:p:g", "f:p:g"));
        topPB.add(UIHelper.createScrollPane(numSchemeList), cc.xy(1, 1));
        
        PanelBuilder middlePanel = new PanelBuilder(new FormLayout("c:p:g", "p, 2px, p"));
        middlePanel.add(mapToBtn, cc.xy(1, 1));
        middlePanel.add(unmapBtn, cc.xy(1, 3));
        
        JPanel btnPanel = middlePanel.getPanel();
        
        PanelBuilder outerMiddlePanel = new PanelBuilder(new FormLayout("c:p:g", "f:p:g, p, f:p:g"));
        outerMiddlePanel.add(btnPanel, cc.xy(1, 2));
        
        PanelBuilder    botPB = new PanelBuilder(new FormLayout("f:p:g,4px,p,4px,f:p:g", "f:p:g"));
        botPB.add(UIHelper.createScrollPane(nsCollList),  cc.xy(1, 1));
        botPB.add(UIHelper.createScrollPane(availNSList), cc.xy(5, 1));
        
        PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g", "f:p:g,6px,f:p:g"));
        pb.add(topPB.getPanel(), cc.xy(1,1));
        pb.add(botPB.getPanel(), cc.xy(1,3));
        
        pb.setDefaultDialogBorder();
        
        contentPanel = pb.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        loadSchemes();

        pack();
        
        numSchemeList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    ansSelected();
                }
            }
        });
        
        nsCollList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    collSelected();
                }
            }
        });
        
        availNSList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    availANSSelected();
                }
            }
        });
        
        okBtn.setEnabled(false);

    }
    
    /**
     * 
     */
    @SuppressWarnings("unchecked")
    private void loadSchemes()
    {
        DefaultListModel model = (DefaultListModel)numSchemeList.getModel();
        model.clear();
        
        DataProviderSessionIFace session   = DataProviderFactory.getInstance().createSession();
        try
        {
            List<AutoNumberingScheme> schemes = (List<AutoNumberingScheme>)session.getDataList("FROM AutoNumberingScheme");
            for (AutoNumberingScheme ans : schemes)
            {
                model.addElement(ans);
                ans.getCollections().size();
                
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
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(NumberingSchemeDlg.class, ex);
            log.error(ex);
            
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
        AutoNumberingScheme ans = (AutoNumberingScheme)numSchemeList.getSelectedValue();
        if (ans != null)
        {
           for (Collection coll : ans.getCollections())
           {
               model.addElement(coll);
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
    private void collSelected()
    {
        DefaultListModel model = (DefaultListModel)nsCollList.getModel();
        model.clear();
        AutoNumberingScheme ans = (AutoNumberingScheme)numSchemeList.getSelectedValue();
        if (ans != null)
        {
           for (Collection coll : ans.getCollections())
           {
               model.addElement(coll);
           }
        }
    }

    /**
     * 
     */
    private void availANSSelected()
    {
        
    }

}
