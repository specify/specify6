/* Filename:    $RCSfile: ResultSetController.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2006/01/23 16:52:27 $
 *
 * This library is free software; you can redistribute it and/or
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
 */package edu.ku.brc.specify.ui.forms;

import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.ui.IconManager;

/**
 * @author rods
 *
 */
public class ResultSetController
{
    protected Vector<ResultSetControllerListener> listeners = new Vector<ResultSetControllerListener>();
    
    protected JPanel  panel    = null;
    protected JButton firstBtn = null;
    protected JButton prevBtn  = null;
    protected JLabel  recDisp  = null;
    protected JButton nextBtn  = null;
    protected JButton lastBtn  = null;  
    
    protected int     currentInx = 0;
    protected int     lastInx    = 0;
    protected int     numRecords = 0;
    
    /**
     * Constructor
     */
    public ResultSetController()
    {
       buildRecordNavBar();
       updateUI();
    }
    
    /**
     * Constructor
     * @param len
     */
    public ResultSetController(int len)
    {
       setLength(len);
       
       buildRecordNavBar();
       updateUI();
    }
    
    /**
     * 
     */
    protected void buildRecordNavBar()
    {
        Insets insets = new Insets(1,1,1,1);
        DefaultFormBuilder rowBuilder = new DefaultFormBuilder(new FormLayout("p,2dlu,p,2dlu,max(50dlu;p):grow,2dlu,p,2dlu,p", "p"));
        firstBtn = new JButton(IconManager.getImage("FirstRec"));
        prevBtn  = new JButton(IconManager.getImage("PrevRec"));
        
        recDisp  = new JLabel("  ");
        recDisp.setHorizontalAlignment(JLabel.CENTER);
        recDisp.setOpaque(true);
        recDisp.setBackground(Color.WHITE);
        recDisp.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        
        nextBtn  = new JButton(IconManager.getImage("NextRec"));
        lastBtn  = new JButton(IconManager.getImage("LastRec"));
        
        firstBtn.setMargin(insets);
        prevBtn.setMargin(insets);
        nextBtn.setMargin(insets);
        lastBtn.setMargin(insets);
        
        CellConstraints cc = new CellConstraints();
        rowBuilder.add(firstBtn, cc.xy(1,1));
        rowBuilder.add(prevBtn, cc.xy(3,1));
        rowBuilder.add(recDisp, cc.xy(5,1));
        rowBuilder.add(nextBtn, cc.xy(7,1));
        rowBuilder.add(lastBtn, cc.xy(9,1));
        
        firstBtn.addActionListener(new ActionListener()
                {
            public void actionPerformed(ActionEvent ae)
            {
                currentInx = 0;
                updateUI();
                notifyListeners();
            }
        });
        prevBtn.addActionListener(new ActionListener()
                {
            public void actionPerformed(ActionEvent ae)
            {
                currentInx--;
                updateUI();
                notifyListeners();
            }
        });
        nextBtn.addActionListener(new ActionListener()
                {
            public void actionPerformed(ActionEvent ae)
            {
                currentInx++;
                updateUI();
                notifyListeners();
            }
        });
        lastBtn.addActionListener(new ActionListener()
                {
            public void actionPerformed(ActionEvent ae)
            {
                currentInx = lastInx;
                updateUI();
                notifyListeners();
            }
        });
        panel = rowBuilder.getPanel();    
    }

    /**
     * 
     * @param len
     */
    public void setLength(int len)
    {
        currentInx = 0;
        numRecords = len;
        lastInx    = numRecords - 1;
        updateUI(); 
    }
    
    /**
     * 
     * @return the length (the number of records)
     */
    public int getLength()
    {
        return numRecords;
    }
    
    /**
     * @return the current index
     */
    public int getCurrentIndex()
    {
        return currentInx;
    }
    

    /**
     * @return the panel
     */
    public JPanel getPanel()
    {
        return panel;
    }
    
    
    /**
     * Enables/Disables the UI according to where we are in the resultset
     */
    protected void updateUI()
    {
        if (panel == null) return;
        
        firstBtn.setEnabled(currentInx > 0);
        prevBtn.setEnabled(currentInx > 0);
        nextBtn.setEnabled(currentInx < lastInx);
        lastBtn.setEnabled(currentInx < lastInx);
        
        recDisp.setText((currentInx+1) + " of " + numRecords);
        panel.validate();
    }
    
    /**
     * Adds a listener
     * @param l thelistener
     */
    public void addListener(ResultSetControllerListener l)
    {
        listeners.addElement(l);
    }
    
    /**
     * Remoave a listener
     * @param l the listener
     */
    public void removeListener(ResultSetControllerListener l)
    {
        listeners.remove(l);
    }
    
    /**
     * Remove all the listeners
     */
    public void removeAllListeners()
    {
        listeners.clear();
    }
    
    /**
     * Notifies all the listeners that the index has changed
     */
    protected void notifyListeners()
    {
        for (ResultSetControllerListener rscl : listeners)
        {
            rscl.indexChanged(currentInx);
        }
    }
}
