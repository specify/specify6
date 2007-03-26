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
package edu.ku.brc.specify.tasks.subpane.wb;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Point;
import java.util.Hashtable;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.validation.ValSpinner;

/**
 * Creates a Dialog used to edit form control attributes.
 * 
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Mar 22, 2007
 *
 */
public class EditFormControl extends CustomDialog implements ChangeListener, DocumentListener
{
    protected InputPanel inputPanel;
    protected FormPane   formPane;
    
    protected ValSpinner xCoord      = null;
    protected ValSpinner yCoord;
    protected ValSpinner fieldWidth;
    protected JTextField labelTF;
    
    protected String     origLabel;
    protected Point      origLocation;
    protected int        origFieldLen;
    
    protected Hashtable<Object, Boolean> changeTracker = new Hashtable<Object, Boolean>();
    
    /**
     * Constructor.
     * 
     * @param frame parent frame
     * @param title the title of the dialog
     * @param isModal whether or not it is model
     * @param contentPanel the contentpane
     * @throws HeadlessException
     */
    public EditFormControl(final Frame      frame, 
                           final String     title, 
                           final InputPanel inputPanel,
                           final FormPane   canvasPanel) throws HeadlessException
    {
        super(frame, title, true, OKCANCELAPPLYHELP, null);
        
        this.inputPanel = inputPanel;
        this.formPane   = canvasPanel;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    protected void createUI()
    {
        if (xCoord == null)
        {
            super.createUI();
            
            int y    = 1;
            CellConstraints cc         = new CellConstraints();
            PanelBuilder    panelBlder = new PanelBuilder(new FormLayout("p,2px,p,p:g", UIHelper.createDuplicateJGoodiesDef("p", "2px", 10)));
            JPanel          panel      = panelBlder.getPanel();
            
            Dimension canvasSize  = formPane.getSize();
            Dimension controlSize = inputPanel.getSize();
            
            panelBlder.add(new JLabel("X:", JLabel.RIGHT), cc.xy(1, y));
            panelBlder.add(xCoord = new ValSpinner(0, canvasSize.width-controlSize.width, false, false), cc.xy(3, y));
            y += 2;
            
            panelBlder.add(new JLabel("Y:", JLabel.RIGHT), cc.xy(1, y));
            panelBlder.add(yCoord = new ValSpinner(0, canvasSize.height-controlSize.height, false, false), cc.xy(3, y));
            y += 2;
            
            panelBlder.add(new JLabel("Label:", JLabel.RIGHT), cc.xy(1, y));
            panelBlder.add(labelTF = new JTextField(25), cc.xywh(3, y, 2, 1));
            y += 2;
            
            if (inputPanel.getComp() instanceof JTextField)
            {
                panelBlder.add(new JLabel("Field Columns:", JLabel.RIGHT), cc.xy(1, y));
                panelBlder.add(fieldWidth = new ValSpinner(0, 100, false, false), cc.xy(3, y));
                y += 2;
            }
            
            fill();
             
            xCoord.addChangeListener(this);
            yCoord.addChangeListener(this);
            fieldWidth.addChangeListener(this);
            labelTF.getDocument().addDocumentListener(this);
    
            mainPanel.add(panel, BorderLayout.CENTER);
            
            pack();
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#applyButtonPressed()
     */
    @Override
    protected void applyButtonPressed()
    {
        adjustControl();
        super.applyButtonPressed();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#cancelButtonPressed()
     */
    @Override
    protected void cancelButtonPressed()
    {
        reset();
        super.cancelButtonPressed();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#helpButtonPressed()
     */
    @Override
    protected void helpButtonPressed()
    {
        super.helpButtonPressed();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#okButtonPressed()
     */
    @Override
    protected void okButtonPressed()
    {
        adjustControl();
        
        inputPanel.getWbtmi().setXCoord((short)((Integer)xCoord.getValue()).intValue());
        inputPanel.getWbtmi().setYCoord((short)((Integer)yCoord.getValue()).intValue());
        inputPanel.getWbtmi().setCaption(labelTF.getText());
        inputPanel.getWbtmi().setMetaData("columns="+((Integer)xCoord.getValue()).intValue());

        formPane.getWorkbenchPane().setChanged(true);
        
        super.okButtonPressed();
    }
    
    /**
     * Sets in a new component to edit.
     * @param inputPanel the input panel component.
     */
    public void setControl(final InputPanel inputPanel)
    {
        if (changeTracker.size() != 0)
        {
            // ask to save changes
        }
        this.inputPanel = inputPanel;
        fill();
    }

    /**
     * Fills in the initial values.
     */
    protected void fill()
    {
        if (xCoord == null)
        {
            createUI();
        }
        
        Point location = inputPanel.getLocation();
        xCoord.setValue(((Double)location.getX()).intValue());
        yCoord.setValue(((Double)location.getY()).intValue());
        labelTF.setText(inputPanel.getLabel().getText());
        
        origLabel    = inputPanel.getLabel().getText();
        origLocation = new Point(location.x, location.y);
        
        if (inputPanel.getComp() instanceof JTextField)
        {
            int cols = ((JTextField)inputPanel.getComp()).getColumns();
            String metaData = inputPanel.getWbtmi().getMetaData();
            if (StringUtils.isNotEmpty(metaData))
            {
                Properties props = UIHelper.parseProperties(metaData);
                if (props != null)
                {
                    String columnsStr = props.getProperty("columns");
                    if (StringUtils.isNotEmpty(columnsStr))
                    {
                        int val = Integer.parseInt(columnsStr);
                        if (val > 0 && val < 65)
                        {
                            cols = val;
                        }
                    }
                }
            }
            
            fieldWidth.setValue(cols);
            origFieldLen = cols;
        }
        changeTracker.clear();
    }
    
    /**
     * Resets all values back to original. 
     */
    protected void reset()
    {
        xCoord.setValue(origLocation.x);
        yCoord.setValue(origLocation.y);
        labelTF.setText(origLabel);

        if (inputPanel.getComp() instanceof JTextField)
        {
            fieldWidth.setValue(origFieldLen);
        }
        adjustControl();
    }
    
    /**
     * Adjust the controls per the changes. 
     */
    protected void adjustControl()
    {

        if (changeTracker.get(xCoord) != null || changeTracker.get(yCoord) != null)
        {
            inputPanel.setLocation(((Integer)xCoord.getValue()).intValue(), ((Integer)yCoord.getValue()).intValue());
        }
        
        boolean doResize = false;
        
        if (changeTracker.get(labelTF.getDocument()) != null)
        {
            inputPanel.getLabel().setText(labelTF.getText());
            doResize = true;
        }
        
        if (changeTracker.get(fieldWidth) != null)
        {
            if (inputPanel.getComp() instanceof JTextField)
            {
                ((JTextField)inputPanel.getComp()).setColumns(((Integer)fieldWidth.getValue()).intValue());
            }
            doResize = true;
        }
        
        if (doResize)
        {
            inputPanel.doLayout();
            inputPanel.repaint();
        }
        changeTracker.clear();
    }
    
    public void stateChanged(ChangeEvent e)
    {
        changeTracker.put(e.getSource(), true);
    }
    
    public void insertUpdate(DocumentEvent e)
    {
        changeTracker.put(e.getDocument(), true);
    }
    
    public void removeUpdate(DocumentEvent e)
    {
        changeTracker.put(e.getDocument(), true);
    }
    
    public void changedUpdate(DocumentEvent e)
    {
        changeTracker.put(e.getDocument(), true);
    }
}
