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
package edu.ku.brc.af.tasks.subpane;

import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.createScrollPane;

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.persist.AltViewIFace;
import edu.ku.brc.af.ui.forms.persist.ViewDefIFace;
import edu.ku.brc.af.ui.forms.persist.ViewIFace;
import edu.ku.brc.af.ui.forms.persist.ViewSetIFace;
import edu.ku.brc.ui.UIHelper;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Sep 29, 2007
 *
 */
public class ViewViewSetEditorPanel extends JPanel
{
    protected ViewSetIFace         viewSet;
    protected Vector<ViewIFace>    views;
    protected Vector<ViewDefIFace> viewDefs;
    
    protected ViewIFace            selectedView    = null;
    protected AltViewIFace         selectedAltView = null;
    
    // UI
    protected JList                viewList;
    protected JList                altViewList;
    protected JList                viewDefList;
    protected DefaultListModel     altViewModel = new DefaultListModel();
    
    
    protected MultiView            viewsMultiView;
    protected ViewIFace            viewsFormView;
    protected MultiView            altViewsMultiView;
    protected ViewIFace            altViewsFormView;
    protected MultiView            viewDefMultiView;
    protected ViewIFace            viewDefFormView;
    
    protected CardLayout           cardLayout = new CardLayout();
    protected JPanel               cardPanel;
    
    /**
     * 
     */
    public ViewViewSetEditorPanel(final ViewSetIFace viewSet)
    {
        this.viewSet = viewSet;
        
        PanelBuilder    pb = new PanelBuilder(new FormLayout("f:p:g,max(200;p),10px,p,f:p:g",  //$NON-NLS-1$
                                                             "p,2px,p,2px,p,2px,p,2px,p,2px,p,10px, p,5px,p,2px,p,2px,p,p:g"), this); //$NON-NLS-1$
        CellConstraints cc = new CellConstraints();
        
        viewsFormView = AppContextMgr.getInstance().getView("SystemSetup", "ViewProps"); //$NON-NLS-1$ //$NON-NLS-2$
        
        viewsMultiView = new MultiView(null,
                                  null, 
                                  viewsFormView, 
                                  AltViewIFace.CreationMode.EDIT,
                                  MultiView.IS_EDITTING, null);

        altViewsFormView = AppContextMgr.getInstance().getView("SystemSetup", "AltViewProps"); //$NON-NLS-1$ //$NON-NLS-2$
        
        altViewsMultiView = new MultiView(null,
                                  null, 
                                  altViewsFormView, 
                                  AltViewIFace.CreationMode.EDIT,
                                  MultiView.IS_EDITTING, null);

        viewDefFormView = AppContextMgr.getInstance().getView("SystemSetup", "ViewDefProps"); //$NON-NLS-1$ //$NON-NLS-2$
        
        viewDefMultiView = new MultiView(null,
                                  null, 
                                  viewDefFormView, 
                                  AltViewIFace.CreationMode.EDIT,
                                  MultiView.IS_EDITTING, null);

        cardPanel = new JPanel(cardLayout);
        cardPanel.add("View",    viewsMultiView); //$NON-NLS-1$
        cardPanel.add("AltView", altViewsMultiView); //$NON-NLS-1$
        
        //setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 10));
        
        views = new Vector<ViewIFace>(viewSet.getViews().values());
        Collections.sort(views);
        
        viewDefs = new Vector<ViewDefIFace>(viewSet.getViewDefs().values());
        Collections.sort(views);
        
        viewList    = new JList(views);
        altViewList = new JList(altViewModel);
        viewDefList = new JList(viewDefs);
        
        altViewList.setVisibleRowCount(4);
        
        JPanel viewBtnBar    = createViewListBtnBar();
        JPanel altViewBtnBar = createAltViewListBtnBar();
        JPanel viewDefBtnBar = createAltViewListBtnBar();
        
        JScrollPane vsp  = createScrollPane(viewList);
        JScrollPane asp  = createScrollPane(altViewList);
        JScrollPane vdsp = createScrollPane(viewDefList);
        
        pb.add(createLabel("Views"),     cc.xy(2, 1));
        pb.add(vsp,                     cc.xy(2, 3));
        pb.add(viewBtnBar,              cc.xy(2, 5));
        

        pb.add(createLabel("Alt Views"), cc.xy(2, 7));
        pb.add(asp,                     cc.xy(2, 9));
        pb.add(altViewBtnBar,           cc.xy(2, 11));

        pb.add(cardPanel,               cc.xywh(4, 1, 1, 11));
        
        pb.addSeparator("",             cc.xywh(1, 13, 5, 1)); //$NON-NLS-1$
        
        pb.add(createLabel("View Defs"), cc.xy(2, 15)); //$NON-NLS-1$
        pb.add(vdsp,                    cc.xy(2, 17));
        pb.add(viewDefBtnBar,           cc.xy(2, 19));
        
        pb.add(viewDefMultiView,        cc.xywh(4, 15, 1, 6));

        // Hooking upListeners
        viewList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    viewSelected();
                }
            }
        });
        
        altViewList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    altViewSelected();
                }
            }
        });
        viewDefList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    viewDefSelected();
                }
            }
        });
    }
    
    /**
     * 
     */
    protected void viewSelected()
    {
        ViewIFace view = (ViewIFace)viewList.getSelectedValue();
        if (view != selectedView)
        {
            // fill
            selectedView = view;
            viewsMultiView.setData(view);
            
            altViewModel.clear();
            for (AltViewIFace av : selectedView.getAltViews())
            {
                altViewModel.addElement(av);
            }
        }
        cardLayout.show(cardPanel, "View"); //$NON-NLS-1$
    }
    
    /**
     * 
     */
    protected void altViewSelected()
    {
        AltViewIFace altView = (AltViewIFace)altViewList.getSelectedValue();
        if (altView != selectedAltView)
        {
            selectedAltView = altView;
            altViewsMultiView.setData(selectedAltView);
        }
        cardLayout.show(cardPanel, "AltView"); //$NON-NLS-1$
    }
    
    /**
     * 
     */
    protected void viewDefSelected()
    {
        ViewDefIFace viewDef = (ViewDefIFace)viewDefList.getSelectedValue();
        viewDefMultiView.setData(viewDef);
    }
    
    /**
     * @return
     */
    protected JPanel createViewListBtnBar()
    {
        ActionListener addAL = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
            }
        };
        ActionListener removeAL = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
            }
        };
        return UIHelper.createAddRemoveEditBtnBar(addAL, "", removeAL, "", null, null); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    /**
     * @return
     */
    protected JPanel createAltViewListBtnBar()
    {
        ActionListener addAL = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
            }
        };
        ActionListener removeAL = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
            }
        };
        return UIHelper.createAddRemoveEditBtnBar(addAL, "", removeAL, "", null, null); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
