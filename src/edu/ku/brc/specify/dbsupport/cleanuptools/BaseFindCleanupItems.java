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
package edu.ku.brc.specify.dbsupport.cleanuptools;

import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIHelper.createScrollPane;
import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.getTopWindow;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Jun 29, 2010
 *
 */
public class BaseFindCleanupItems extends CustomDialog
{
    protected enum ItemStatusType {eOK, eProcessed, eRelated}
    
    protected JList       itemsList;
    protected JButton     cleanupBtn;
    protected DBTableInfo tblInfo;
    
    
    /**
     * @param frame
     * @param title
     * @throws HeadlessException
     */
    public BaseFindCleanupItems(final DBTableInfo tblInfo) throws HeadlessException
    {
        super((Frame)getTopWindow(), getLocalizedMessage("CLNUP.FNDTITLE", tblInfo.getTitle()), true, OK_BTN, null);
        this.tblInfo = tblInfo;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        setOkLabel(getResourceString("CLOSE"));
        
        super.createUI();
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("p,2px,f:p:g", "p,2px,p"));
        
        DefaultListModel model = new DefaultListModel();
        model.addElement(new ItemInfo(0, getResourceString("CLNUP.LOAD")));
        itemsList  = new JList(model);
        itemsList.setEnabled(false);
        
        cleanupBtn = createButton(getLocalizedMessage("CLNUP.CHOOSE", tblInfo.getTitle()));
        cleanupBtn.setEnabled(false);
        
        pb.add(createScrollPane(itemsList), cc.xyw(1,1,3));
        pb.add(cleanupBtn, cc.xy(1,3));
        
        itemsList.setCellRenderer(getListCellRenderer());
        
        pb.setDefaultDialogBorder();
        contentPanel = pb.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        pack();
        
        cleanupBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                doCleanupOfItem();
            }
        });
        
        itemsList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    if (cleanupBtn != null)
                    {
                        cleanupBtn.setEnabled(itemsList.getSelectedIndex() != -1);
                    }
                }
            }
        });
        itemsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                super.mouseClicked(e);
                
                if (e.getClickCount() == 2)
                {
                    cleanupBtn.setEnabled(true);
                    cleanupBtn.doClick();
                }
            }
        });
    }
    
    /**
     * 
     */
    protected void doCleanupOfItem()
    {
        
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible)
    {
        if (visible)
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    //startProcessing();
                }
            });
            startProcessing();
        }
        
        super.setVisible(visible);
    }

    /**
     * @param model
     */
    protected void fillModel(final DefaultListModel model)
    {
        itemsList.setModel(model);
        pack();
        setSize(Math.max(getSize().width, 350), getSize().height);
        UIHelper.centerWindow(this);
    }
    
    /**
     * @return
     */
    protected Vector<ItemInfo> doWork()
    {
        return new Vector<ItemInfo>();
    }
    
    /**
     * 
     */
    protected void startProcessing()
    {
        SwingWorker<DefaultListModel, Object> worker = new SwingWorker<DefaultListModel, Object>()
        {
            DefaultListModel wrkModel = null;
            
            /* (non-Javadoc)
             * @see javax.swing.SwingWorker#doInBackground()
             */
            @Override
            protected DefaultListModel doInBackground() throws Exception
            {
                wrkModel = new DefaultListModel();
                for (ItemInfo ii :  doWork())
                {
                    wrkModel.addElement(ii);
                }
                return wrkModel;
            }

            /* (non-Javadoc)
             * @see javax.swing.SwingWorker#done()
             */
            @Override
            protected void done()
            {
                fillModel(this.wrkModel);
                itemsList.setEnabled(true);
                super.done();
            }
        };
        
        worker.execute();
    }
    
    protected DefaultListCellRenderer getListCellRenderer()
    {
        //final Color sameColor = new Color(0,192,0);
        
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list,
                                                          Object value,
                                                          int index,
                                                          boolean isSelected,
                                                          boolean cellHasFocus)
            {
                ItemInfo itemInfo = (ItemInfo)value;
                JLabel lbl = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                //lbl.setForeground(model.isSame(column) ? sameColor : Color.BLACK);
                lbl.setText(itemInfo.value.toString());
                return lbl;
            }
        };
    }
    
    class ItemInfo
    {
        ItemStatusType status;
        int            id;
        Object         value;
        /**
         * @param id
         * @param value
         */
        public ItemInfo(int id, Object value)
        {
            super();
            this.id    = id;
            this.value  = value;
            this.status = ItemStatusType.eOK;
        }
    }
}
