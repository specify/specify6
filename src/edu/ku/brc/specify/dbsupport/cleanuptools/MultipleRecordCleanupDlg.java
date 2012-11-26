/* Copyright (C) 2012, University of Kansas Center for Research
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

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Jul 27, 2012
 *
 */
public class MultipleRecordCleanupDlg extends CustomDialog
{
    private MultipleRecordPanel         mrcPanel;
    private Vector<MultipleRecordPanel> kidPanels = new Vector<MultipleRecordPanel>();
    
    private MultipleRecordComparer mrc;
    
    /**
     * @param fii
     * @param hasLocDetail
     * @param hasGeoDetail
     * @throws HeadlessException
     */
    public MultipleRecordCleanupDlg(final MultipleRecordComparer mrc, 
                                    final String title) throws HeadlessException
    {
        super((Frame)UIRegistry.getTopWindow(), title, true, CustomDialog.OKCANCELAPPLY, null);
        this.mrc = mrc;
        setApplyLabel(UIRegistry.getResourceString("SKIP"));
        setCloseOnApplyClk(true);
    }
    
    /**
     * @return
     */
    public boolean isSingle()
    {
        return false;//itemsList.size() == 1;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        setCancelLabel(UIRegistry.getResourceString("QUIT"));
        
        super.createUI();
        
        JLabel      lbl  = UIHelper.createLabel(mrc.getTitle(), SwingConstants.CENTER);
        JTabbedPane pane = new JTabbedPane();
        
        JPanel localPanel = new JPanel(new BorderLayout());
        localPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));
        localPanel.add(lbl, BorderLayout.NORTH);
        localPanel.add(pane, BorderLayout.CENTER);
        
        System.out.println(mrc.getNumColsWithData());
        boolean hasMasterPanel = mrc.getNumColsWithData() > 0;
        if (hasMasterPanel)
        {
            mrcPanel = new MultipleRecordPanel(mrc);
            mrcPanel.createUI();
            pane.addTab(mrc.getTblInfo().getTitle(), mrcPanel);
            mrcPanel.setChangeListener(new ChangeListener()
            {
                @Override
                public void stateChanged(ChangeEvent arg0)
                {
                    okBtn.setEnabled(mrcPanel.isDataValid());
                }
            });
        }
        
        for (MultipleRecordComparer mrcKid : mrc.getKids())
        {
            if (mrcKid.hasColmnsOfDataThatsDiff())
            {
                MultipleRecordPanel kidPanel = new MultipleRecordPanel(mrcKid);
                kidPanel.setSingleRowIncluded(mrcKid.isSingleRowIncluded());
                kidPanel.createUI();
                kidPanels.add(kidPanel);
                
                if (!hasMasterPanel)
                {
                    kidPanel.setChangeListener(new ChangeListener()
                    {
                        @Override
                        public void stateChanged(ChangeEvent arg0)
                        {
                            okBtn.setEnabled(true); // XXX this needs to check to make sure at least one item is checked
                        }
                    });
                }
                pane.addTab(mrcKid.getTblInfo().getTitle(), kidPanel);
            }
        }
        
        okBtn.setEnabled(false);
        
        contentPanel = localPanel;
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        pack();
    }
    
    /**
     * @return
     */
    public MergeInfo getMainMergedInfo()
    {
        if (mrcPanel != null)
        {
            return new MergeInfo(true, true, mrcPanel.tblInfo, mrcPanel.getMergeInfo());
        } 
        return null;
    }
    
    /**
     * @return
     */
    public List<MergeInfo> getKidsMergedInfo()
    {
        Vector<MergeInfo> items = new Vector<MergeInfo>();
        
        for (MultipleRecordPanel kidPanel : kidPanels)
        {

            MergeInfo mi = new MergeInfo(false, kidPanel.isSingleRowIncluded(), kidPanel.tblInfo, kidPanel.getMergeInfo());
            items.add(mi);
        }
        return items;
    }
    
}
