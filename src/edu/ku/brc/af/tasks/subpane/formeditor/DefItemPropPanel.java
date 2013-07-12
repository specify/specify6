/* Copyright (C) 2013, University of Kansas Center for Research
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
package edu.ku.brc.af.tasks.subpane.formeditor;

import static edu.ku.brc.ui.UIHelper.createCheckBox;
import static edu.ku.brc.ui.UIHelper.createComboBox;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.setControlSize;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.tasks.subpane.formeditor.JGoodiesDefItem.ALIGN_TYPE;
import edu.ku.brc.af.tasks.subpane.formeditor.JGoodiesDefItem.MINMAX_TYPE;
import edu.ku.brc.af.tasks.subpane.formeditor.JGoodiesDefItem.SIZE_TYPE;

/**
 * @author rod
 *
 * @code_status Beta
 *
 * Oct 27, 2007
 *
 */
public class DefItemPropPanel extends JPanel
{
    protected JGoodiesDefItem.MINMAX_TYPE[] minMax   = {MINMAX_TYPE.None, MINMAX_TYPE.Min, MINMAX_TYPE.Max};
    protected JGoodiesDefItem.ALIGN_TYPE[]  rowAlign = {ALIGN_TYPE.None, ALIGN_TYPE.Top, ALIGN_TYPE.Bottom};
    protected JGoodiesDefItem.ALIGN_TYPE[]  colAlign = {ALIGN_TYPE.None, ALIGN_TYPE.Left, ALIGN_TYPE.Right};
    protected JGoodiesDefItem.SIZE_TYPE[]   sizes    = {SIZE_TYPE.Pixels, SIZE_TYPE.Dlus};
    
    protected boolean isRow;
    
    protected JComboBox minMaxCBX;
    protected JComboBox alignCBX;
    protected JComboBox unitCBX;
    protected JSpinner  sizeSpinner;
    protected JCheckBox growCB;
    protected JCheckBox preferredSizeCB;
    
    protected JGoodiesDefItem currentItem = null;
    
    protected JPanel          propsPanel;
    
    /**
     * @param defStr
     * @param numInUse
     * @param isRow
     */
    public DefItemPropPanel(final int     numInUse,
                            final boolean isRow)
    {
        super(new BorderLayout());
        
        this.isRow = isRow;
        
        createUI(isRow);
    }
    
    /**
     * @param isRow
     */
    protected void createUI(@SuppressWarnings("hiding")
    final boolean isRow)
    {
        propsPanel = createPropertyPanel(isRow);
        
        enablePropPanel(true);
        
        add(propsPanel, BorderLayout.CENTER);
    }

    
    /**
     * @param isRow
     * @return
     */
    protected JPanel createPropertyPanel(@SuppressWarnings("hiding")
    final boolean isRow)
    {
        alignCBX  = createComboBox(isRow ? rowAlign : colAlign);
        minMaxCBX = createComboBox(minMax);
        unitCBX   = createComboBox(sizes);
        
        SpinnerModel model = new SpinnerNumberModel(0, //initial value
                0, //min
                1024,   //max
                10);               //step
        sizeSpinner = new JSpinner(model);

        setControlSize(sizeSpinner);
        
        growCB          = createCheckBox(getResourceString("DefItemPropPanel.SHOULD_GROW")); //$NON-NLS-1$
        preferredSizeCB = createCheckBox(getResourceString("DefItemPropPanel.USE_PREF_SIZE")); //$NON-NLS-1$
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("p,2px,p:g", "p,2px,p,2px,p,2px,p,2px,p,2px,p,5px")); //$NON-NLS-1$ //$NON-NLS-2$
        
        int y = 1;
        pb.add(createLabel(getResourceString("DefItemPropPanel.MIN_MAX"), SwingConstants.RIGHT), cc.xy(1, y)); //$NON-NLS-1$
        pb.add(minMaxCBX, cc.xy(3,y));
        y += 2;
        
        pb.add(createLabel(getResourceString("DefItemPropPanel.ALIGNMENT"), SwingConstants.RIGHT), cc.xy(1, y)); //$NON-NLS-1$
        pb.add(alignCBX, cc.xy(3,y));
        y += 2;
        
        pb.add(createLabel(" ", SwingConstants.RIGHT), cc.xy(1, y)); //$NON-NLS-1$
        pb.add(preferredSizeCB, cc.xy(3,y));
        y += 2;
        
        pb.add(createLabel(getResourceString("DefItemPropPanel.SIZE"), SwingConstants.RIGHT), cc.xy(1, y)); //$NON-NLS-1$
        pb.add(sizeSpinner, cc.xy(3,y));
        y += 2;
        
        pb.add(createLabel(getResourceString("DefItemPropPanel.UNIT"), SwingConstants.RIGHT), cc.xy(1, y)); //$NON-NLS-1$
        pb.add(unitCBX, cc.xy(3,y));
        y += 2;
        
        pb.add(createLabel("", SwingConstants.RIGHT), cc.xy(1, y)); //$NON-NLS-1$
        pb.add(growCB, cc.xy(3,y));
        y += 2;

        minMaxCBX.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                updateUIControls();
            }
        });
        
        preferredSizeCB.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e)
            {
                updateUIControls();
            }
            
        });

        return pb.getPanel();
    }
    
    /**
     * 
     */
    protected void updateUIControls()
    {
        boolean isNone = minMaxCBX.getSelectedIndex() == 0;
        boolean enable = true;
        if (isNone)
        {
            enable = !preferredSizeCB.isSelected();
        }
        
        preferredSizeCB.setEnabled(isNone);
        
        sizeSpinner.setEnabled(enable);
        unitCBX.setEnabled(enable);
        if (!enable)
        {
            sizeSpinner.setValue(0);
        }
    }
    
    /**
     * @param enable
     */
    protected void enablePropPanel(final boolean enable)
    {
        for (int i=0;i<propsPanel.getComponentCount();i++)
        {
            propsPanel.getComponent(i).setEnabled(enable);
        }
    }
    
    /**
     * 
     */
    protected void setDataIntoUI()
    {
        if (currentItem != null)
        {
            minMaxCBX.setSelectedItem(currentItem.getMinMax());
            alignCBX.setSelectedItem(currentItem.getAlign());
            if (currentItem.getLen() > -1)
            {
                sizeSpinner.setValue(currentItem.getLen());
            }
            growCB.setSelected(currentItem.isGrow());
            preferredSizeCB.setSelected(currentItem.isPreferredSize());

            updateUIControls();
            
        } else
        {
            enablePropPanel(false);
        }

    }

    /**
     * 
     */
    protected void getDataFromUI()
    {
        if (currentItem != null)
        {
            currentItem.setMinMax((JGoodiesDefItem.MINMAX_TYPE)minMaxCBX.getSelectedItem());
            currentItem.setAlign((JGoodiesDefItem.ALIGN_TYPE)alignCBX.getSelectedItem());
            currentItem.setLen(preferredSizeCB.isSelected() ? -1 : (Integer)sizeSpinner.getValue());
            currentItem.setPreferredSize(preferredSizeCB.isSelected());
            currentItem.setGrow(growCB.isSelected());
        }
    }

    /**
     * @return
     */
    public JGoodiesDefItem getCurrentItem()
    {
        return currentItem;
    }

    /**
     * @param currentItem
     */
    public void setCurrentItem(JGoodiesDefItem currentItem)
    {
        enablePropPanel(true);
        
        this.currentItem = currentItem;
        
        setDataIntoUI();
    }


}
