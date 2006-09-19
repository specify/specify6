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
 */package edu.ku.brc.ui.forms;

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.validation.FormValidator;
import edu.ku.brc.ui.validation.UIValidator;
import edu.ku.brc.ui.validation.ValidationListener;

/*
 * @code_status Unknown (auto-generated)
 **
 * @author rods
 *
 */
public class ResultSetController implements ValidationListener
{
    protected static Border enabledBorder  = BorderFactory.createLineBorder(Color.BLACK);
    protected static Border disabledBorder = BorderFactory.createLineBorder(Color.GRAY.brighter());
    
    protected static Color  enabledTxtBG   = Color.WHITE;
    protected static Color  disabledTxtBG  = new Color(225,225,225);//Color.WHITE.darker();
    
    protected List<ResultSetControllerListener> listeners = new ArrayList<ResultSetControllerListener>();
    
    protected FormValidator formValidator = null;
    
    
    protected JPanel  panel    = null;
    protected JButton firstBtn = null;
    protected JButton prevBtn  = null;
    protected JLabel  recDisp  = null;
    protected JButton nextBtn  = null;
    protected JButton lastBtn  = null;  
    protected JButton newRecBtn = null;  
    protected JButton delRecBtn = null;
    
    protected int     currentInx = 0;
    protected int     lastInx    = 0;
    protected int     numRecords = 0;
    
    /**
     * Constructor
     * @param len
     */
    public ResultSetController(final FormValidator formValidator, final boolean addNewBtn,  final boolean addDelBtn, final int len)
    {
        this.formValidator = formValidator;
        
       
        if (formValidator != null)
        {
            formValidator.addValidationListener(this);
        }
       
        buildRecordNavBar(addNewBtn, addDelBtn);
        
        setLength(len);
    }
    
    /**
     * 
     */
    protected void buildRecordNavBar(final boolean addNewBtn, final boolean addDelBtn)
    {
        String colDef = "p,2dlu,p,2dlu,max(50dlu;p):grow,2dlu,p,2dlu,p" + (addNewBtn ? ",2dlu,p" : "") + (addDelBtn ? ",2dlu,p" : "");
        Insets insets = new Insets(1,1,1,1);
        DefaultFormBuilder rowBuilder = new DefaultFormBuilder(new FormLayout(colDef, "p"));
        
        firstBtn = new JButton(IconManager.getImage("FirstRec"));
        prevBtn  = new JButton(IconManager.getImage("PrevRec"));
        
        recDisp  = new JLabel("  ");
        recDisp.setHorizontalAlignment(JLabel.CENTER);
        recDisp.setOpaque(true);
        recDisp.setBackground(Color.WHITE);
        recDisp.setBorder(enabledBorder);
        
        nextBtn  = new JButton(IconManager.getImage("NextRec"));
        lastBtn  = new JButton(IconManager.getImage("LastRec"));
        
        firstBtn.setMargin(insets);
        prevBtn.setMargin(insets);
        nextBtn.setMargin(insets);
        lastBtn.setMargin(insets);
        
        firstBtn.setToolTipText(getResourceString("GotoFirstRecordTT"));
        prevBtn.setToolTipText(getResourceString("GotoPreviousRecordTT"));
        nextBtn.setToolTipText(getResourceString("GotoNextRecordTT"));
        lastBtn.setToolTipText(getResourceString("GotoLastRecordTT"));
        
        CellConstraints cc = new CellConstraints();
        rowBuilder.add(firstBtn, cc.xy(1,1));
        rowBuilder.add(prevBtn, cc.xy(3,1));
        rowBuilder.add(recDisp, cc.xy(5,1));
        rowBuilder.add(nextBtn, cc.xy(7,1));
        rowBuilder.add(lastBtn, cc.xy(9,1));
        int row = 11;
        
        if (addNewBtn)
        {
            newRecBtn = new JButton(IconManager.getImage("NewRec"));
            newRecBtn.setToolTipText(getResourceString("NewRecordTT"));
            newRecBtn.setMargin(insets);
            rowBuilder.add(newRecBtn, cc.xy(row,1));
            row += 2;
            
            newRecBtn.addActionListener(new ActionListener() {
               public void actionPerformed(ActionEvent ae)
               {
                   for (ResultSetControllerListener rscl : listeners)
                   {
                       rscl.newRecordAdded();
                   }
               }
            });
        }
        
        if (addDelBtn)
        {
            delRecBtn = new JButton(IconManager.getImage("SmallTrash"));
            delRecBtn.setToolTipText(getResourceString("RemoveRecordTT"));
            delRecBtn.setMargin(insets);
            rowBuilder.add(delRecBtn, cc.xy(row++,1));
            row += 2;
        }
        
 
        firstBtn.addActionListener(new ActionListener()
                {
            public void actionPerformed(ActionEvent ae)
            {
                notifyListenersABoutToChangeIndex(currentInx, 0);
                currentInx = 0;
                updateUI();
                notifyListeners();
            }
        });
        prevBtn.addActionListener(new ActionListener()
                {
            public void actionPerformed(ActionEvent ae)
            {
                notifyListenersABoutToChangeIndex(currentInx, currentInx-1);
                currentInx--;
                updateUI();
                notifyListeners();
            }
        });
        nextBtn.addActionListener(new ActionListener()
                {
            public void actionPerformed(ActionEvent ae)
            {
                notifyListenersABoutToChangeIndex(currentInx, currentInx+1);
                currentInx++;
                updateUI();
                notifyListeners();
            }
        });
        lastBtn.addActionListener(new ActionListener()
                {
            public void actionPerformed(ActionEvent ae)
            {
                notifyListenersABoutToChangeIndex(currentInx, lastInx);
                currentInx = lastInx;
                updateUI();
                notifyListeners();
            }
        });
        panel = rowBuilder.getPanel();    
    }
    
    /**
     * Sets whether the record controller should be enabled
     * @param enabled true enabled, false not enabled
     */
    public void setEnabled(boolean enabled)
    {
        currentInx = enabled ? 0 : -1;
        updateUI();
    }

    /**
     * 
     * @param len
     */
    public void setLength(int len)
    {
        currentInx = len > 0 ? 0 : -1;
        numRecords = len;
        lastInx    = numRecords - 1;
        updateUI(); 
    }
    
    /**
     * Sets the controller to a new index
     * @param index the new index
     */
    public void setIndex(int index)
    {
        currentInx = index;
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
        
        boolean enabled = numRecords > 0;
        
        recDisp.setEnabled(enabled);
        recDisp.setBorder(enabled ? enabledBorder : disabledBorder);
        recDisp.setBackground(enabled ? enabledTxtBG : disabledTxtBG);
        
        
        
        if (delRecBtn != null)
        {
            delRecBtn.setEnabled(enabled);
        }
        
        recDisp.setText(enabled ? ((currentInx+1) + " of " + numRecords) : " "); // XXX Move to I18N properties file formatted
        panel.validate();
    }
    
    /**
     * Returns the JButton that is used to create new records.
     * @return the JButton that is used to create new records
     */
    public JButton getNewRecBtn()
    {
        return newRecBtn;
    }

    /**
     * Returns the JBUtton that is used to create new records.
     * @return the JBUtton that is used to create new records
     */
    public JButton getDelRecBtn()
    {
        return delRecBtn;
    }

    /**
     * Adds a listener
     * @param l thelistener
     */
    public void addListener(ResultSetControllerListener l)
    {
        listeners.add(l);
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
    
    /**
     * Notifies all the listeners that the index has changed
     */
    protected void notifyListenersABoutToChangeIndex(final int oldIndex, final int newIndex)
    {
        for (ResultSetControllerListener rscl : listeners)
        {
            rscl.indexAboutToChange(oldIndex, newIndex);
        }
    }
    
    /**
     * Sets all the UI Enabled/Disabled
     * @param enabled true/false
     */
    protected void setUIEnabled(final boolean enabled)
    {
        if (!enabled)
        {
            firstBtn.setEnabled(enabled);
            prevBtn.setEnabled(enabled);
            nextBtn.setEnabled(enabled);
            lastBtn.setEnabled(enabled);
            recDisp.setEnabled(enabled);
            
            if (newRecBtn != null)
            {
                newRecBtn.setEnabled(enabled);
            }
            if (delRecBtn != null)
            {
                delRecBtn.setEnabled(enabled);
            }
        } else
        {
           updateUI(); 
        }
        
    }
    
    //-----------------------------------------------------
    // ValidationListener
    //-----------------------------------------------------

   /* (non-Javadoc)
     * @see ValidationListener#wasValidated(UIValidator)
     */
    public void wasValidated(final UIValidator validator)
    {
        if (formValidator != null && firstBtn.isEnabled() != formValidator.isFormValid())
        {
            setUIEnabled(formValidator.isFormValid());
        }

    }
}
