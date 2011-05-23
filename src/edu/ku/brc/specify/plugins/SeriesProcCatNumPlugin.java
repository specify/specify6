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
package edu.ku.brc.specify.plugins;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.ui.forms.CarryForwardInfo;
import edu.ku.brc.af.ui.forms.validation.ValFormattedTextFieldIFace;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.util.Pair;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Oct 27, 2010
 *
 */
public class SeriesProcCatNumPlugin extends UIPluginBase implements ValFormattedTextFieldIFace
{
    private ValFormattedTextFieldIFace textFieldStart;
    private ValFormattedTextFieldIFace textFieldEnd;
    private JButton                    expandBtn;
    
    private boolean                    isAutoNumOn = true;
    private boolean                    isExpanded  = false;
    private PanelBuilder               pb;
    private CellConstraints            cc          = new CellConstraints();
    private JPanel                     panel;
    
    /**
     * @param textFieldStart
     * @param textFieldEnd
     */
    public SeriesProcCatNumPlugin(final ValFormattedTextFieldIFace textFieldStart,
                                  final ValFormattedTextFieldIFace textFieldEnd)
    {
        super();
        this.textFieldStart = textFieldStart;
        this.textFieldEnd   = textFieldEnd;
        this.expandBtn      = UIHelper.createIconBtn("move_right", "", null);
        
        pb = new PanelBuilder(new FormLayout("p,f:p:g", "c:p"), this);
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(textFieldStart.getValidatableUIComp());
        panel.add(textFieldEnd.getValidatableUIComp());
        panel.add(expandBtn);
        expandBtn.setEnabled(true);
        
        pb.add(panel, cc.xy(1,1));
        
        expandBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                doToggleContract();
            }
        });
        
        textFieldEnd.setAutoNumberEnabled(false);
        textFieldEnd.getValidatableUIComp().setEnabled(true);
        
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                isExpanded = true;
                expandBtn.doClick();
                expandBtn.setVisible(false);
            }
        });
    }
    
    /**
     * @return a pair representing the start and end catalogNumbers for the Batch.
     */
    public Pair<String, String> getStartAndEndCatNumbers()
    {
        String start = textFieldStart.getValue() != null ? textFieldStart.getValue().toString() : null;
        String end = !isExpanded() ? null : (textFieldEnd.getValue() != null ? textFieldEnd.getValue().toString() : null);
        if (end == null && start != null)
        {
        	end = start;
        }
    	return new Pair<String, String>(start, end);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.UIPluginBase#setNewObj(boolean)
     */
    public void setNewObj(boolean isNewObj)
    {
        super.setNewObj(isNewObj);
        carryForwardStateChange();
    }

    /**
     * @return the isExpanded
     */
    public boolean isExpanded()
    {
        return isExpanded;
    }
    
    /**
     * 
     */
    public void clearEndTextField()
    {
        textFieldEnd.setValue("", "");
    }
    
    /**
     * 
     */
    private void checkToggleContract()
    {
        boolean          isCarryForwardOK = false;
        CarryForwardInfo cfInfo           = fvo.getCarryFwdInfo();
        if (cfInfo != null)
        {
            isCarryForwardOK = cfInfo.getFieldList().size() > 0;
        }
        
        boolean isOKToExpand = isNewObj && !isAutoNumOn && isCarryForwardOK;
        textFieldStart.setAutoNumberEnabled(!isOKToExpand);
        textFieldEnd.setAutoNumberEnabled(!isOKToExpand);
        expandBtn.setVisible(isOKToExpand);
        
        //System.err.println("isOKToExpand "+isOKToExpand+"  isExpanded "+isExpanded+"  isCarryForwardOK "+isCarryForwardOK+"  isNewObj "+isNewObj+"  isAutoNumOn "+isAutoNumOn);
        if (!isOKToExpand)
        {
            isExpanded = true;
            doToggleContract();
        }
    }
    
    /**
     * 
     */
    private void doToggleContract()
    {
        isExpanded = !isExpanded;
        
        if (isExpanded)
        {
            panel.removeAll();
            panel.add(textFieldStart.getValidatableUIComp());
            panel.add(textFieldEnd.getValidatableUIComp());
            panel.add(expandBtn);
        } else
        {
            panel.removeAll();
            panel.add(textFieldStart.getValidatableUIComp());
            panel.add(expandBtn);
        }
        expandBtn.setIcon(IconManager.getIcon(!isExpanded ? "move_right" : "move_left"));
        invalidate();
        revalidate();
        doLayout();
        if (getParent() != null)
        {
        	getParent().doLayout();
        }
        repaint();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.UIPluginBase#isNotEmpty()
     */
    @Override
    public boolean isNotEmpty()
    {
        return textFieldStart.isNotEmpty() && (!isExpanded || textFieldEnd.isNotEmpty());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#getFieldNames()
     */
    @Override
    public String[] getFieldNames()
    {
        return new String[] {"catalogNumber"};
    }

    //----------------------------------------------------------------------------------
    // AutoNumberable Interface
    //----------------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.AutoNumberableIFace#isFormatterAutoNumber()
     */
    @Override
    public boolean isFormatterAutoNumber()
    {
        return textFieldStart.isFormatterAutoNumber();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.AutoNumberableIFace#setAutoNumberEnabled(boolean)
     */
    @Override
    public void setAutoNumberEnabled(boolean turnOn)
    {
        isAutoNumOn = turnOn;
        carryForwardStateChange();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.UIPluginBase#carryForwardStateChange()
     */
    @Override
    public void carryForwardStateChange()
    {
        checkToggleContract();
        
        repaint();
        invalidate();
        revalidate();
        doLayout();
        getParent().doLayout();
        repaint();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.AutoNumberableIFace#updateAutoNumbers()
     */
    @Override
    public void updateAutoNumbers()
    {
        if (!isExpanded)
        {
            textFieldStart.updateAutoNumbers();
        }
    }

    //----------------------------------------------------------------------------------
    // UIValidatable Interface
    //----------------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.UIPluginBase#canCarryForward()
     */
    @Override
    public boolean canCarryForward()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.UIPluginBase#getCarryForwardFields()
     */
    @Override
    public String[] getCarryForwardFields()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.UIPluginBase#getValue()
     */
    @Override
    public Object getValue()
    {
        return textFieldStart.getValue();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.UIPluginBase#setValue(java.lang.Object, java.lang.String)
     */
    @Override
    public void setValue(Object value, String defaultValue)
    {
        textFieldStart.setValue(value, defaultValue);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#cleanUp()
     */
    @Override
    public void cleanUp()
    {
        textFieldStart.cleanUp();
        textFieldEnd.cleanUp();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#getReason()
     */
    @Override
    public String getReason()
    {
        if (isExpanded)
        {
            StringBuilder sb = new StringBuilder();
            if (StringUtils.isNotEmpty(textFieldStart.getReason()))
            {
                sb.append(textFieldStart.getReason());
            }
            
            if (StringUtils.isNotEmpty(textFieldEnd.getReason()))
            {
                if (sb.length() > 0) sb.append("\n");
                sb.append(textFieldEnd.getReason());
            }
            return sb.toString();
        }
        return textFieldStart.getReason();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#getState()
     */
    @Override
    public ErrorType getState()
    {
        if (isExpanded)
        {
            ErrorType state1 =  textFieldStart.validateState();
            ErrorType state2 =  textFieldEnd.validateState();
            return state1.ordinal() > state2.ordinal() ? state1 : state2;
        }
        return textFieldStart.validateState();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#getValidatableUIComp()
     */
    @Override
    public Component getValidatableUIComp()
    {
        return this;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#isChanged()
     */
    @Override
    public boolean isChanged()
    {
        return textFieldStart.isChanged() && (!isExpanded || textFieldEnd.isChanged());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#isInError()
     */
    @Override
    public boolean isInError()
    {
        return textFieldStart.isInError() && (!isExpanded || textFieldEnd.isInError());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#isRequired()
     */
    @Override
    public boolean isRequired()
    {
        return textFieldStart.isRequired();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#reset()
     */
    @Override
    public void reset()
    {
        textFieldStart.reset();
        textFieldEnd.reset();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#setAsNew(boolean)
     */
    @Override
    public void setAsNew(boolean isNew)
    {
        textFieldStart.setAsNew(isNew);
        textFieldEnd.setAsNew(isNew);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#setChanged(boolean)
     */
    @Override
    public void setChanged(boolean isChanged)
    {
        textFieldStart.setChanged(isChanged);
        textFieldEnd.setChanged(isChanged);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#setRequired(boolean)
     */
    @Override
    public void setRequired(boolean isRequired)
    {
        textFieldStart.setRequired(isRequired);
        textFieldEnd.setRequired(isRequired);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#setState(edu.ku.brc.af.ui.forms.validation.UIValidatable.ErrorType)
     */
    @Override
    public void setState(ErrorType state)
    {
        textFieldStart.setState(state);
        textFieldEnd.setState(state);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#validateState()
     */
    @Override
    public ErrorType validateState()
    {
        if (isExpanded)
        {
            ErrorType state1 =  textFieldStart.validateState();
            ErrorType state2 =  textFieldEnd.validateState();
            return state1.ordinal() > state2.ordinal() ? state1 : state2;
        }
        return textFieldStart.validateState();
    }
}
