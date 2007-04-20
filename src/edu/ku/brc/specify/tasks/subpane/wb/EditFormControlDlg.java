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

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.validation.ValCheckBox;
import edu.ku.brc.ui.validation.ValFormattedTextField;
import edu.ku.brc.ui.validation.ValSpinner;

/**
 * Creates a Dialog used to edit form control attributes.
 * 
 * @author rods
 *
 * @code_status Complete
 *
 * Created Date: Mar 22, 2007
 *
 */
public class EditFormControlDlg extends CustomDialog implements ChangeListener, DocumentListener
{
    protected InputPanel inputPanel;
    protected FormPane   formPane;
    
    protected ValSpinner xCoord      = null;
    protected ValSpinner yCoord;
    protected ValSpinner fieldWidth;
    protected ValSpinner numRows;
    protected JTextField labelTF;
    protected JComboBox  textFieldType;
    protected boolean    isTextField;
    protected boolean    isFormattedText;
    protected int        fieldTypeIndex   = -1; 
    protected boolean    fieldTypeChanged = false; // not using changeTracker for Field Type Combobox
    
    protected JLabel     rowsLabels;
    protected JLabel     typeLabel;
    
    
    protected String     origLabel;
    protected Point      origLocation;
    protected int        origFieldLen;
    protected Short      origFieldType;
    protected int        origFieldTypeIndex;
    protected int        origRows;
    
    protected Hashtable<Object, Boolean> changeTracker = new Hashtable<Object, Boolean>();
    
    protected boolean    parentHasChanged;
    /**
     * Constructor.
     * 
     * @param frame parent frame
     * @param title the title of the dialog
     * @param isModal whether or not it is model
     * @param contentPanel the contentpane
     * @throws HeadlessException
     */
    public EditFormControlDlg(final Frame      frame, 
                              final String     title, 
                              final InputPanel inputPanel,
                              final FormPane   canvasPanel) throws HeadlessException
    {
        super(frame, title, true, OKCANCELAPPLYHELP, null);
        
        this.inputPanel = inputPanel;
        this.formPane   = canvasPanel;
        
        parentHasChanged = formPane.getWorkbenchPane().isChanged();
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
            
            isTextField     = inputPanel.getComp() instanceof JTextComponent;
            isFormattedText = inputPanel.getComp() instanceof ValFormattedTextField;
                       
            int y    = 1;
            CellConstraints cc         = new CellConstraints();
            PanelBuilder    panelBlder = new PanelBuilder(new FormLayout("p,2px,p,p:g", UIHelper.createDuplicateJGoodiesDef("p", "2px", 8 + 
                    (isTextField && !isFormattedText ? 4 : 0))));
            JPanel          panel      = panelBlder.getPanel();
            
            Dimension canvasSize  = formPane.getSize();
            Dimension controlSize = inputPanel.getSize();
            
            panelBlder.add(new JLabel("X:", SwingConstants.RIGHT), cc.xy(1, y));
            panelBlder.add(xCoord = new ValSpinner(0, canvasSize.width-controlSize.width, false, false), cc.xy(3, y));
            y += 2;
            
            panelBlder.add(new JLabel("Y:", SwingConstants.RIGHT), cc.xy(1, y));
            panelBlder.add(yCoord = new ValSpinner(0, canvasSize.height-controlSize.height, false, false), cc.xy(3, y));
            y += 2;
            
            panelBlder.add(new JLabel("Label:", SwingConstants.RIGHT), cc.xy(1, y));
            panelBlder.add(labelTF = new JTextField(25), cc.xywh(3, y, 2, 1));
            y += 2;
            
            if (isTextField)
            {
                panelBlder.add(new JLabel("Field Columns:", SwingConstants.RIGHT), cc.xy(1, y));
                panelBlder.add(fieldWidth = new ValSpinner(0, 100, false, false), cc.xy(3, y));
                y += 2;
 
                if (!isFormattedText)
                {
                    panelBlder.add(rowsLabels = new JLabel("Number of Rows:", SwingConstants.RIGHT), cc.xy(1, y));
                    panelBlder.add(numRows = new ValSpinner(1, 25, false, false), cc.xy(3, y));
                    y += 2;
     
                    panelBlder.add(typeLabel = new JLabel("Field Type:", SwingConstants.RIGHT), cc.xy(1, y));
                    panelBlder.add(textFieldType = new JComboBox(new Object[] { getResourceString("WB_TEXTFIELD"), getResourceString("WB_TEXTAREA")}), cc.xy(3, y));
                    y += 2;
                }
            }
            
            fill();
            
            if (fieldWidth != null)
            {
                origFieldLen  = ((Integer)fieldWidth.getValue()).shortValue();
            }
            
            if (textFieldType != null)
            {
                origFieldTypeIndex = textFieldType.getSelectedIndex();
            }
            
            if (numRows != null)
            {
                origRows      = ((Integer)numRows.getValue()).shortValue();
                origFieldType = inputPanel.getWbtmi().getFieldType();
                numRows.addChangeListener(this);
            }
            
             
            xCoord.addChangeListener(this);
            yCoord.addChangeListener(this);
            
            if (isTextField)
            {
                fieldWidth.addChangeListener(this);
            }
            
            if (textFieldType != null)
            {
                textFieldType.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e)
                    {
                        fieldTypeChanged = textFieldType.getSelectedIndex() != fieldTypeIndex; 
                        
                        if (numRows != null)
                        {
                            adjustTextRowsUI();
                            numRows.setValue(textFieldType.getSelectedIndex() == 0 ? FormPane.DEFAULT_TEXTFIELD_ROWS : (origRows == 1 ? FormPane.DEFAULT_TEXTAREA_ROWS : origRows));
                        }
                    }
                });
            }
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
        if (!parentHasChanged)
        {
            formPane.getWorkbenchPane().setChanged(false);
        }
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
        if (textFieldType != null && origFieldTypeIndex != textFieldType.getSelectedIndex())
        {
            if (fieldTypeChanged)
            {
                adjustControl();
            }
            inputPanel.getWbtmi().setFieldType(inputPanel.getComp() instanceof JTextField ? WorkbenchTemplateMappingItem.TEXTFIELD : WorkbenchTemplateMappingItem.TEXTAREA);
        }
        
        inputPanel.getWbtmi().setXCoord((short)((Integer)xCoord.getValue()).intValue());
        inputPanel.getWbtmi().setYCoord((short)((Integer)yCoord.getValue()).intValue());
        inputPanel.getWbtmi().setCaption(labelTF.getText());
        inputPanel.setLabelText(labelTF.getText()+":");
        
        if (fieldWidth != null)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("columns=");
            sb.append(((Integer)fieldWidth.getValue()).intValue());
            if (numRows != null)
            {
                sb.append(";rows=");
                sb.append(((Integer)numRows.getValue()).intValue());
            }
            inputPanel.getWbtmi().setMetaData(sb.toString());
        }
        
        adjustControl();

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
        
        if (textFieldType != null)
        {
            origFieldTypeIndex = textFieldType.getSelectedIndex();
        }
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
        labelTF.setText(StringUtils.strip(inputPanel.getLabelText(), ":"));
        
        origLabel    = inputPanel.getLabelText();
        origLocation = new Point(location.x, location.y);
        
        if (isTextField)
        {
            if (textFieldType != null)
            {
                fieldTypeIndex     = inputPanel.getComp() instanceof JTextField ? 0 : 1;
                textFieldType.setSelectedIndex(fieldTypeIndex);
            }
            
            if (fieldWidth != null)
            {
                int cols = inputPanel.getComp() instanceof JTextField ? ((JTextField)inputPanel.getComp()).getColumns() : ((JTextArea)inputPanel.getComp()).getColumns();
                fieldWidth.setValue(cols);
            }
            
            if (numRows != null)
            {
                adjustTextRowsUI();
                int rows = inputPanel.getComp() instanceof JTextArea ? ((JTextArea)inputPanel.getComp()).getRows() : 1;
                numRows.setValue(rows);
            }
        }
        changeTracker.clear();
    }
    
    protected void adjustTextRowsUI()
    {
        if (numRows != null)
        {
            boolean isTexArea = textFieldType.getSelectedIndex() == 1;
            rowsLabels.setEnabled(isTexArea);
            numRows.setEnabled(isTexArea);
        }
    }
    
    /**
     * Resets all values back to original. 
     */
    protected void reset()
    {
        xCoord.setValue(origLocation.x);
        yCoord.setValue(origLocation.y);
        labelTF.setText(origLabel);

        if (isTextField)
        {
            if (textFieldType != null)
            {
                if ((origFieldTypeIndex == 0 && inputPanel.getComp() instanceof JTextArea) ||
                    (origFieldTypeIndex == 1 && inputPanel.getComp() instanceof JTextField))
                {
                    //textFieldType.setSelectedIndex(origFieldTypeIndex);
                    fieldTypeChanged = true;
                } else
                {
                    fieldTypeChanged = false;
                }
            }
            
            if (fieldWidth != null)
            {
                fieldWidth.setValue(origFieldLen);
            }
            if (numRows != null)
            {
                numRows.setValue(origRows);
            }
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
            if (inputPanel.getComp() instanceof ValCheckBox)
            {
                ((ValCheckBox)inputPanel.getComp()).setText(labelTF.getText());
            } else
            {
                inputPanel.getLabel().setText(StringUtils.strip(labelTF.getText(), ":")+":");
            }
            doResize = true;
        }
        
        if (isTextField)
        {
            if (fieldTypeChanged)
            {
                formPane.swapTextFieldType(inputPanel, ((Integer)fieldWidth.getValue()).shortValue());
                    
                fieldTypeChanged = false;
                fieldTypeIndex   = textFieldType.getSelectedIndex();
                adjustTextRowsUI();
            }
            
            if (changeTracker.get(fieldWidth) != null || (numRows != null && changeTracker.get(numRows) != null))
            {
                if (inputPanel.getComp() instanceof JTextField)
                {
                    ((JTextField)inputPanel.getComp()).setColumns(((Integer)fieldWidth.getValue()).intValue());
                } else
                {
                    ((JTextArea)inputPanel.getComp()).setColumns(((Integer)fieldWidth.getValue()).intValue());
                    if (numRows != null) // shouldn't be null - defensive
                    {
                        ((JTextArea)inputPanel.getComp()).setRows(((Integer)numRows.getValue()).intValue());
                    }
                    inputPanel.validate();
                    inputPanel.repaint();
                }
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
    
    /* (non-Javadoc)
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    public void stateChanged(ChangeEvent e)
    {
        changeTracker.put(e.getSource(), true);
    }
    
    /* (non-Javadoc)
     * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
     */
    public void insertUpdate(DocumentEvent e)
    {
        changeTracker.put(e.getDocument(), true);
    }
    
    /* (non-Javadoc)
     * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
     */
    public void removeUpdate(DocumentEvent e)
    {
        changeTracker.put(e.getDocument(), true);
    }
    
    /* (non-Javadoc)
     * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
     */
    public void changedUpdate(DocumentEvent e)
    {
        changeTracker.put(e.getDocument(), true);
    }
}
