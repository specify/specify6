/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.tasks.subpane.wb;

import static edu.ku.brc.ui.UIHelper.createIconBtn;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.specify.tasks.WorkbenchTask;
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.dnd.GhostActionable;
import edu.ku.brc.ui.dnd.GhostGlassPane;
import edu.ku.brc.ui.dnd.GhostMouseInputAdapter;
import edu.ku.brc.ui.forms.ResultSetControllerListener;
import edu.ku.brc.ui.validation.ValCheckBox;
import edu.ku.brc.ui.validation.ValFormattedTextField;
import edu.ku.brc.ui.validation.ValTextField;

/**
 * This Panel holds the form elements for Template definition of a Workbench.
 * 
 * @author rod
 *
 * @code_status Beta
 *
 * Mar 8, 2007
 *
 */
public class FormPane extends JPanel implements ResultSetControllerListener, 
                                                GhostActionable,
                                                DocumentListener,
                                                ChangeListener
{
    protected static final short TEXTFIELD_DATA_LEN_MAX = 64;
    protected static final short MAX_COLS               = 64;
    protected static final short DEFAULT_TEXTFIELD_COLS = 15;
    protected static final short DEFAULT_TEXTAREA_COLS  = 45;
    
    protected WorkbenchPaneSS    workbenchPane;
    protected Workbench          workbench;
    protected boolean            hasChanged        = false;
    protected Vector<InputPanel> uiComps           = new Vector<InputPanel>();
    protected boolean            isInImageMode     = false;
    protected JButton            controlPropsBtn   = null;
    
    protected boolean            ignoreChanges     = false;
    protected boolean            changesInForm     = false;
    protected int                currentIndex      = -1;
    
    protected InputPanel         selectedInputPanel = null;   

    protected Vector<WorkbenchTemplateMappingItem> headers = new Vector<WorkbenchTemplateMappingItem>();
    
    protected List<DataFlavor>   dropFlavors  = new ArrayList<DataFlavor>();
    
    protected EditFormControl    controlProperties = null;
    protected boolean            wasShowing        = false;
    
    /**
     * Creates a Pane for editing a Workbench as a form.
     * @param workbenchPane the workbench pane to be parented into
     * @param workbench the workbench
     */
    public FormPane(final WorkbenchPaneSS workbenchPane, 
                    final Workbench workbench)
    {
        setLayout(null);
        
        this.workbenchPane = workbenchPane;
        this.workbench     = workbench;
        
        dropFlavors.add(InputPanel.INPUTPANEL_FLAVOR);
        
        headers.addAll(workbench.getWorkbenchTemplate().getWorkbenchTemplateMappingItems());
        Collections.sort(headers);
        
        MouseAdapter clickable = new MouseAdapter()
        {
            protected InputPanel getInputPanel(final Object obj)
            {
                Component comp = ((Component)obj).getParent();
                while (comp != null && !(comp instanceof InputPanel))
                {
                    comp = comp.getParent();
                }
                return comp instanceof InputPanel ? (InputPanel)comp : null;
            }
            
            public void mousePressed(MouseEvent e)
            {
                if (e.getClickCount() == 2 && (controlProperties == null || !controlProperties.isVisible()))
                {
                    showControlProps();
                }

                selectedInputPanel = getInputPanel(e.getSource());
                controlPropsBtn.setEnabled(true);
                
                if (controlProperties != null)
                {
                    controlProperties.setControl(selectedInputPanel);
                }
            }
            
            public void mouseClicked(MouseEvent e)
            {
                selectedInputPanel = getInputPanel(e.getSource());
                controlPropsBtn.setEnabled(true);
                
                if (controlProperties != null)
                {
                    controlProperties.setControl(selectedInputPanel);
                }
                
                if (e.getClickCount() == 2 && (controlProperties == null || !controlProperties.isVisible()))
                {
                    showControlProps();
                }
                
            }
        };
        
        addMouseListener(new MouseAdapter()
        {
            public void mouseClicked(MouseEvent e)
            {
                selectedInputPanel = null;
                controlPropsBtn.setEnabled(false);
            }
        });
        
        Vector<InputPanel> itemsToSkip = new Vector<InputPanel>();
        int maxWidthOffset = 0;
        int x = 5;
        int y = 5;
        for (WorkbenchTemplateMappingItem wbtmi : headers)
        {
            InputPanel p = new InputPanel(wbtmi, wbtmi.getCaption(), createUIComp(wbtmi));
            p.createMouseInputAdapter(); // this makes it draggable
            p.getMouseInputAdapter().setDropCanvas(this);
            
            p.getLabel().addMouseListener(clickable);
            p.getComp().addMouseListener(clickable);

            Dimension size = p.getPreferredSize();
            p.setSize(size);
            p.setPreferredSize(size);
            
            maxWidthOffset = Math.max(p.getTextFieldOffset(), maxWidthOffset);
            uiComps.add(p);
            add(p);
            
            // NOTE: that the constructor sets the location from WorkbenchTemplateMappingItem object
            if (wbtmi.getXCoord() == null || wbtmi.getYCoord() == null || wbtmi.getXCoord() == -1 || wbtmi.getYCoord() == -1)
            {
                p.setLocation(x, y);
                y += size.height + 4;
                
            } else
            {
                itemsToSkip.add(p);
            }
            
        }
        
        // Now align the control by their text fields and skips the ones that have actual positions defined.
        int inx = 0;
        for (WorkbenchTemplateMappingItem wbtmi : headers)
        {
            InputPanel panel = uiComps.get(inx);
            if (!itemsToSkip.contains(panel))
            {
                Point pLoc = panel.getLocation();
                x = maxWidthOffset - panel.getTextFieldOffset();
                panel.setLocation(x, pLoc.y);
                wbtmi.setXCoord((short)x);
                wbtmi.setYCoord((short)pLoc.y);
            }
            inx++;
        }
        
        
        controlPropsBtn = createIconBtn("ControlEdit", IconManager.IconSize.Std16, "WB_EDIT_CONTROL", true, new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                showControlProps();
            }
        });
    }
    
    /**
     * @return the WorkbenchPaneSS
     */
    public WorkbenchPaneSS getWorkbenchPane()
    {
        return workbenchPane;
    }

    /**
     * @return the controlPropsBtn
     */
    public JButton getControlPropsBtn()
    {
        return controlPropsBtn;
    }


    /**
     * Clean up and listeners etc.
     */
    public void cleanup()
    {
        if (controlProperties != null)
        {
            controlProperties.setVisible(false);
            controlProperties.dispose();
            controlProperties = null;
        }
    }

    /**
     * Creates a JTextArea in a ScrollPane.
     * @return the scollpane
     */
    protected JComponent createTextArea(final short len)
    {
        JTextArea textArea = new JTextArea("", 5, len);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.getDocument().addDocumentListener(this);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        return scrollPane;
    }
    
    
    /**
     * Returns a UI component for editing.
     * @param wbtmi the mapping item
     * @return a UI component for editing
     */
    protected JComponent createUIComp(final WorkbenchTemplateMappingItem wbtmi)
    {
        return createUIComp(WorkbenchTask.getDataType(wbtmi), 
                            wbtmi.getCaption(), 
                            wbtmi.getDataFieldLength(), 
                            wbtmi.getFieldType(),
                            getColumns(wbtmi));
    }
    
    /**
     * Returns 
     * @param wbtmi
     * @return
     */
    public static short getColumns(final WorkbenchTemplateMappingItem wbtmi)
    {
        short columns;
        if (wbtmi.getFieldType() != null)
        {
            columns =  wbtmi.getFieldType() == WorkbenchTemplateMappingItem.TEXTAREA ? DEFAULT_TEXTAREA_COLS : DEFAULT_TEXTFIELD_COLS;
        } else
        {
            columns =  wbtmi.getDataFieldLength() > TEXTFIELD_DATA_LEN_MAX ? DEFAULT_TEXTAREA_COLS : DEFAULT_TEXTFIELD_COLS;
        }
        
        String metaData = wbtmi.getMetaData();
        if (StringUtils.isNotEmpty(metaData))
        {
            Properties props = UIHelper.parseProperties(metaData);
            if (props != null)
            {
                String columnsStr = props.getProperty("columns");
                if (StringUtils.isNotEmpty(columnsStr))
                {
                    short val = Short.parseShort(columnsStr);
                    if (val > 0 && val < 65)
                    {
                        columns = val;
                    }
                }
            }
        }
        return columns;
    }
    
    /**
     * @param dbFieldType
     * @param caption
     * @param dataFieldLength
     * @param fieldType
     * @return a UI component for editing
     */
    protected JComponent createUIComp(final Class<?> dbFieldTypeArg,
                                      final String   caption,
                                      final Short    fieldLength, 
                                      final Short    fieldType,
                                      final short    columns)
    {
        //System.out.println(wbtmi.getCaption()+" "+wbtmi.getDataType()+" "+wbtmi.getFieldLength());
        Class<?> dbFieldType = dbFieldTypeArg;
        if (dbFieldType == null)
        {
            // if we can't find a class for the field (i.e. Genus Species, or other 'fake' fields), we say it's a string
            dbFieldType = String.class;
        }
        
        JComponent comp;
        
        // handle dates
        if (dbFieldType.equals(Calendar.class) || dbFieldType.equals(Date.class))
        {
            ValFormattedTextField txt = new ValFormattedTextField("Date"); 
            txt.getDocument().addDocumentListener(this);
            comp = txt;
            
        }
        else if (dbFieldType.equals(Boolean.class)) // strings
        {
            ValCheckBox checkBox = new ValCheckBox(caption, false, false);
            checkBox.addChangeListener(this);
            comp = checkBox;
        }
        else
        {
            if ((fieldLength > 64 && fieldType == null) || fieldType != null && fieldType == WorkbenchTemplateMappingItem.TEXTAREA)
            {
                comp = createTextArea(columns);
            }
            else
            {
                ValTextField txt = new ValTextField(columns);
                txt.getDocument().addDocumentListener(this);
                comp = txt;
            }
        }
        return comp;
    }
    
    /**
     * Swaps out a TextField for a TextArea and vs.
     * @param inputPanel the InputPanel that hold the text component
     * @param fieldLen the length of the text field component (columns)
     */
    public void swapTextFieldType(final InputPanel inputPanel, final short fieldLen)
    {
        JTextComponent oldComp;
        short fieldType;
        if (inputPanel.getComp() instanceof JTextField)
        {
            JTextField tf = (JTextField)inputPanel.getComp();
            tf.getDocument().removeDocumentListener(this);
            fieldType = WorkbenchTemplateMappingItem.TEXTAREA;
            oldComp = tf;
        } else
        {
            JTextArea ta = (JTextArea)inputPanel.getComp();
            ta.getDocument().removeDocumentListener(this);
            fieldType = WorkbenchTemplateMappingItem.TEXTFIELD;
            oldComp = ta;
        }
        
        WorkbenchTemplateMappingItem wbtmi = inputPanel.getWbtmi();
        inputPanel.setComp(createUIComp(WorkbenchTask.getDataType(wbtmi), wbtmi.getCaption(), fieldLen, fieldType, fieldLen));
        
        ignoreChanges = true;
        ((JTextComponent)inputPanel.getComp()).setText(oldComp.getText());
        ignoreChanges = false;
        
        hasChanged = true;
        workbenchPane.setChanged(true);
    }
    
    /**
     *  Show a properties dialog for the control.
     */
    protected void showControlProps()
    {
        if (controlProperties == null)
        {
            controlProperties = new EditFormControl((Frame)UICacheManager.get(UICacheManager.FRAME), "Properties", selectedInputPanel, this); // I18N
        }
        controlProperties.setVisible(true);
        if (!controlProperties.isCancelled())
        {
            workbenchPane.gridColumnsUpdated();
        }
        controlProperties.dispose();
        controlProperties = null;
    }
    
    /**
     * Tells the form it is being hidden.
     * @param show true - show, false hide
     */
    public void switching(final boolean show)
    {
        if (!show)
        {
            if (currentIndex > -1 && changesInForm)
            {
                copyDataFromForm(currentIndex);
            }
        }
    }
    
    /**
     * Tells the pane whether it is about to show or not when the parent pane is being shown or not.
     * @param show true show, false hide
     */
    public void showingPane(final boolean show)
    {
        if (show)
        {
           if (wasShowing)
           {
               controlProperties.setVisible(true);
           }
        } else if (controlProperties != null)
        {
            controlProperties.setVisible(false);
            wasShowing = true;
            
        } else
        {
            wasShowing = false;
        }
    }
    
    /**
     * Copies the data from the form into the row.
     * @param index the index of the row
     */
    protected void copyDataFromForm(final int index)
    {
        ignoreChanges = true;
        
        changesInForm = false;
        
        WorkbenchRow wbRow = workbench.getWorkbenchRowsAsList().get(index);
        for (InputPanel p : uiComps)
        {
            short col = p.getWbtmi().getViewOrder();
            
            if (p.getComp() instanceof JTextField)
            {
                String data     = ((JTextField)p.getComp()).getText();
                String cellData = wbRow.getData(col);
                if (StringUtils.isNotEmpty(cellData) || data != null)
                {
                    wbRow.setData(data == null ? "" : data, col);
                }
                
            } else if (p.getComp() instanceof GetSetValueIFace)
            {
                Object data     = ((GetSetValueIFace)p.getComp()).getValue();
                String cellData = wbRow.getData(col);
                if (StringUtils.isNotEmpty(cellData) || data != null)
                {
                    wbRow.setData(data == null ? "" : data.toString(), col);
                }
                
            } else if (p.getComp() instanceof JScrollPane)
            {
                JScrollPane sc   = (JScrollPane)p.getComp();
                Component   comp = sc.getViewport().getView();
                if (comp instanceof JTextArea)
                {
                    wbRow.setData(((JTextArea)comp).getText(), col);
                }
            } else
            {
                ((JTextComponent)p.getComp()).setText(wbRow.getData(col));
                wbRow.setData(((JTextComponent)p.getComp()).getText(), col);
            }
        }
        ignoreChanges = false;
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean aFlag)
    {
        if (!aFlag && controlProperties != null)
        {
            controlProperties.setVisible(false);
        }
        super.setVisible(aFlag);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ResultSetControllerListener#indexAboutToChange(int, int)
     */
    public boolean indexAboutToChange(int oldIndex, int newIndex)
    {
        if (oldIndex != newIndex && isVisible())
        {
            copyDataFromForm(oldIndex);
            currentIndex = newIndex;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ResultSetControllerListener#indexChanged(int)
     */
    public void indexChanged(int newIndex)
    {
        if (isVisible())
        {
            ignoreChanges = true; 
            currentIndex  = newIndex;
            WorkbenchRow wbRow = workbench.getWorkbenchRowsAsList().get(newIndex);
            for (InputPanel p : uiComps)
            {
                int col = p.getWbtmi().getViewOrder();
                
                if (p.getComp() instanceof GetSetValueIFace)
                {
                    ((GetSetValueIFace)p.getComp()).setValue(wbRow.getData(col), wbRow.getData(col));
                    
                } else if (p.getComp() instanceof JScrollPane)
                {
                    JScrollPane sc = (JScrollPane)p.getComp();
                    Component comp = sc.getViewport().getView();
                    if (comp instanceof JTextArea)
                    {
                        ((JTextArea)comp).setText(wbRow.getData(col));
                    }
                } else
                {
                    ((JTextComponent)p.getComp()).setText(wbRow.getData(col));
                }
            }
            ignoreChanges = false;
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ResultSetControllerListener#newRecordAdded()
     */
    public void newRecordAdded()
    {
        // TODO Auto-generated method stub
    }
    
    //-----------------------------------------------
    // GhostActionable Interface
    //-----------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#doAction(edu.ku.brc.ui.dnd.GhostActionable)
     */
    public void doAction(GhostActionable src)
    {
        if (src instanceof InputPanel)
        {
            InputPanel inputPanel = (InputPanel)src;
            Point offSet   = src.getMouseInputAdapter().getOffsetFromStartPnt();
            Point location = inputPanel.getLocation();
            Point offset   = ((GhostGlassPane)UICacheManager.get(UICacheManager.GLASSPANE)).getOffset();
            location.translate(offSet.x - offset.x, offSet.y - offset.y);
            inputPanel.setLocation(location);
            remove(inputPanel);
            add(inputPanel);
            repaint();
            validate();
            doLayout();
            
            // Update the template
            WorkbenchTemplateMappingItem wbtmi = inputPanel.getWbtmi();
            wbtmi.setXCoord((short)location.x);
            wbtmi.setYCoord((short)location.y);
            workbenchPane.setChanged(true);
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#setData(java.lang.Object)
     */
    public void setData(final Object data)
    {
        //this.data = data;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getData()
     */
    public Object getData()
    {
        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getDataForClass(java.lang.Class)
     */
    public Object getDataForClass(Class classObj)
    {
        return UIHelper.getDataForClass(this, classObj);
    }
   
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#createMouseInputAdapter()
     */
    public void createMouseInputAdapter()
    {
        // nothing
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#setActive(boolean)
     */
    public void setActive(boolean isActive)
    {
        // Auto-generated method stub
    }
    
    /**
     * Returns the adaptor for tracking mouse drop gestures
     * @return Returns the adaptor for tracking mouse drop gestures
     */
    public GhostMouseInputAdapter getMouseInputAdapter()
    {
        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getBufferedImage()
     */
    public BufferedImage getBufferedImage() 
    {
        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getDataFlavor()
     */
    public List<DataFlavor> getDropDataFlavors()
    {
        return dropFlavors;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostActionable#getDragDataFlavors()
     */
    public List<DataFlavor> getDragDataFlavors()
    {
        return null; // this is not draggable
    }
    
    //-----------------------------------------------
    // DocumentListener Interface
    //-----------------------------------------------
    public void insertUpdate(DocumentEvent e) 
    {
        if (!ignoreChanges)
        {
            changesInForm = true;
            workbenchPane.setChanged(true);
        }
    }
    
    public void removeUpdate(DocumentEvent e) 
    {
        if (!ignoreChanges)
        {
            changesInForm = true;
            workbenchPane.setChanged(true);
        }
    }
    
    public void changedUpdate(DocumentEvent e) 
    {
        if (!ignoreChanges)
        {
            changesInForm = true;
            workbenchPane.setChanged(true);
        }
    }

    //-----------------------------------------------
    // DocumentListener Interface
    //-----------------------------------------------
    public void stateChanged(ChangeEvent e)
    {
        if (!ignoreChanges)
        {
            changesInForm = true;
            workbenchPane.setChanged(true);
        }
    }
    
}
