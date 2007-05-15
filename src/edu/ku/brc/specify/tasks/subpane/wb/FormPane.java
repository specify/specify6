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
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.AWTKeyStroke;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.specify.tasks.WorkbenchTask;
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.dnd.GhostActionable;
import edu.ku.brc.ui.dnd.GhostGlassPane;
import edu.ku.brc.ui.dnd.GhostMouseInputAdapter;
import edu.ku.brc.ui.forms.ResultSetControllerListener;
import edu.ku.brc.ui.validation.ValCheckBox;
import edu.ku.brc.ui.validation.ValTextArea;
import edu.ku.brc.ui.validation.ValTextField;

/**
 * This Panel holds the form elements for Template definition of a Workbench. NOTE: This assumes that the IUNputPanel is a container and the label
 * and UI input control are direct children. This could makes calls to getParent that assume it is only nested one deep.
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
    private static final Logger log = Logger.getLogger(FormPane.class);
    
    protected static final short TEXTFIELD_DATA_LEN_MAX = 64;
    protected static final short MAX_COLS               = 64;
    protected static final short DEFAULT_TEXTFIELD_COLS = 15;
    protected static final short DEFAULT_TEXTAREA_COLS  = 45;
    protected static final short DEFAULT_TEXTFIELD_ROWS = 1;
    protected static final short DEFAULT_TEXTAREA_ROWS  = 5;
       
    protected WorkbenchPaneSS    workbenchPane;
    protected Workbench          workbench;
    protected boolean            hasChanged        = false;
    protected Vector<InputPanel> uiComps           = new Vector<InputPanel>();
    protected boolean            isInImageMode     = false;
    protected JButton            controlPropsBtn   = null;
    protected Component          firstComp         = null;
    
    protected boolean            ignoreChanges     = false;
    protected boolean            changesInForm     = false;
    protected int                currentIndex      = -1;
    
    protected InputPanel         selectedInputPanel = null;   

    protected Vector<WorkbenchTemplateMappingItem> headers = new Vector<WorkbenchTemplateMappingItem>();
    
    protected List<DataFlavor>   dropFlavors       = new ArrayList<DataFlavor>();
    
    protected EditFormControlDlg controlPropsDlg   = null;
    protected boolean            wasShowing        = false;
    protected JScrollPane        scrollPane;
    
    // This is necessary because CardLayout doe4sn't set visibility
    protected Dimension          initialSize       = null; 
    
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
        
        buildUI();
        
        scrollPane = new JScrollPane(this, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }
    
    /**
     * Creates all the UI form the Form. 
     */
    protected void buildUI()
    {
        headers.addAll(workbench.getWorkbenchTemplate().getWorkbenchTemplateMappingItems());
        Collections.sort(headers);
        
        MouseAdapter clickable = new MouseAdapter()
        {
            
            @Override
            public void mousePressed(MouseEvent e)
            {
                //if (e.getClickCount() == 2 && (controlPropsDlg == null || !controlPropsDlg.isVisible()))
                //{
                //    showControlProps();
                //}

                selectControl(e.getSource());
                
            }
            
            @Override
            public void mouseClicked(MouseEvent e)
            {
                selectedInputPanel = getInputPanel(e.getSource());
                controlPropsBtn.setEnabled(true);
                
                if (controlPropsDlg != null)
                {
                    controlPropsDlg.setControl(selectedInputPanel);
                }
                
                if (e.getClickCount() == 2 && (controlPropsDlg == null || !controlPropsDlg.isVisible()))
                {
                    UsageTracker.incrUsageCount("WB.FormPropsTool");
                    showControlProps();
                }
                
            }
        };
        
        addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                // Not a bug, but is leaving the Edit Btn enabled such a bad thing?
                //selectedInputPanel = null;
                //controlPropsBtn.setEnabled(false);
            }
        });
        
        Point topLeftPnt = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);

        final int LAYOUT_SPACING = 4;
        short maxY = 0;
        Vector<InputPanel> delayedLayout = new Vector<InputPanel>();
        int maxWidthOffset = 0;
        for (WorkbenchTemplateMappingItem wbtmi : headers)
        {
            // Create the InputPanel and make it draggable
            InputPanel panel = new InputPanel(wbtmi, wbtmi.getCaption(), createUIComp(wbtmi));
            panel.createMouseInputAdapter(); // this makes it draggable
            panel.getMouseInputAdapter().setDropCanvas(this);
            
            // Add the listener for double clicking for properties
            panel.getLabel().addMouseListener(clickable);

            Dimension size = panel.getPreferredSize();
            panel.setSize(size);
            panel.setPreferredSize(size);
            
            // Finds the largest label
            maxWidthOffset = Math.max(panel.getTextFieldOffset(), maxWidthOffset);
            
            int x = wbtmi.getXCoord();
            int y = wbtmi.getYCoord();
            if (y < topLeftPnt.y || (y == topLeftPnt.y && x < topLeftPnt.x))
            {
                firstComp = panel.getComp();
                topLeftPnt.setLocation(x, y);
            }
            
            // Add it to the Form (drag canvas)
            uiComps.add(panel);
            add(panel);
            
            // NOTE: that the constructor sets the x,y location from WorkbenchTemplateMappingItem object
            // so the ones with XCoord and YCoord set do not need to be positioned
            if (wbtmi.getXCoord() == null || wbtmi.getYCoord() == null || wbtmi.getXCoord() == -1 || wbtmi.getYCoord() == -1)
            {
                delayedLayout.add(panel); // remember this for later once we know the Max Y
                
            } else
            {
                maxY = (short)Math.max(wbtmi.getYCoord() + size.height, maxY);
            }
        }
        
        // Now align the control by their text fields and skips the ones that have actual positions defined.
        // NOTE: We set the X,Y into the Mapping so that each item knows where it is, then if the user
        // drags and drop anything or save the template everyone knows where they are suppose to be
        // 
        int   inx        = 0;
        int   maxX       = 0;
        int   y          = maxY + LAYOUT_SPACING;
        for (WorkbenchTemplateMappingItem wbtmi : headers)
        {
            InputPanel panel = uiComps.get(inx);
            if (delayedLayout.contains(panel))
            {
                int x = maxWidthOffset - panel.getTextFieldOffset();
                panel.setLocation(x, y);
                wbtmi.setXCoord((short)x);
                wbtmi.setYCoord((short)y);
                
                Dimension size = panel.getPreferredSize();
                y += size.height + LAYOUT_SPACING;
                
            }
            Rectangle r = panel.getBounds();
            maxX = Math.max(maxX, r.x + r.width);
            inx++;
        }
        
        initialSize = new Dimension(maxX, y);
        
        controlPropsBtn = createIconBtn("ControlEdit", IconManager.IconSize.NonStd, "WB_EDIT_CONTROL", false, new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                UsageTracker.getUsageCount("WBFormPropsTool");
                showControlProps();
            }
        });
        
        addArrowTraversalKeys();
    }
    
    /**
     * Adds Up/Down Focus Traversal Keys to the form.
     */
    protected void addArrowTraversalKeys()
    {
        Set<AWTKeyStroke> set = getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
        KeyStroke forward = KeyStroke.getKeyStroke("DOWN");
        set = new HashSet<AWTKeyStroke>(set);
        set.add(forward);
        setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, set);
        
        set = getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
        KeyStroke backward = KeyStroke.getKeyStroke("UP");
        set = new HashSet<AWTKeyStroke>(set);
        set.add(backward);
        setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, set);
    }
    
    /**
     * For a given Object (usually from a MouseEvent) it determinaes if a selection of a control or label happened.
     * It also requests focus for the COntrol of the label.
     * @param obj the UI Object
     * @return the parent InputPanel
     */
    protected InputPanel getInputPanel(final Object obj)
    {
        Component comp = ((Component)obj).getParent();
        while (comp != null && !(comp instanceof InputPanel))
        {
            comp = comp.getParent();
        }
        
        InputPanel inputPanel = comp instanceof InputPanel ? (InputPanel)comp : null;
        if (inputPanel != null)
        {
            if (obj instanceof JLabel)
            {
                inputPanel.getComp().requestFocus();
            }
        }
        return inputPanel;
    }
    
    /**
     * Determinaes if a child of the InputPanel was clicked on and selects the InputPanel and enables the Edit Control button.
     * @param uiObj the UI Object usually from an event
     */
    protected void selectControl(final Object uiObj)
    {
        selectedInputPanel = getInputPanel(uiObj); // NOTE: This also requests the focus if it finds one
        controlPropsBtn.setEnabled(true);
        
        if (controlPropsDlg != null)
        {
            controlPropsDlg.setControl(selectedInputPanel);
        }
    }
    
    /**
     * Sets in a new Workbench.
     * @param workbench the new wb
     */
    public void setWorkbench(final Workbench workbench)
    {
        this.workbench = workbench;
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#getPreferredSize()
     */
    @Override
    public Dimension getPreferredSize()
    {
        return initialSize;
    }

    public JScrollPane getScrollPane()
    {
        return scrollPane;
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
        // no op
    }

    /**
     * Creates a JTextArea in a ScrollPane.
     * @return the scollpane
     */
    protected JScrollPane createTextArea(final short len, final short rows)
    {
        ValTextArea textArea = new ValTextArea("", rows, len);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.getDocument().addDocumentListener(this);
        JScrollPane taScrollPane = new JScrollPane(textArea);
        taScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        taScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        return taScrollPane;
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
                            wbtmi.getFieldName(), 
                            wbtmi.getFieldType(),
                            wbtmi.getDataFieldLength(), 
                            getColumns(wbtmi),
                            getRows(wbtmi));
    }
    
    /**
     * Determinaes whether it is a TextField or whether it SHOULD be a TextField
     * @param fieldName the name of the field
     * @param fieldType the type of field
     * @param fieldLen the length of the data 
      * @return true to use TextField, false to use TextArea
     */
    protected static boolean useTextField(final String fieldName, final Short fieldType, final Short fieldLen)
    {
        // Check to see if the length of the data field is past threshold TEXTFIELD_DATA_LEN_MAX
        // if so, then only create a TextArea if the field name contains the word "remarks"
        // the user can switch other field fro TextField to TextArea if the wish.
        if (fieldType != null)
        {
            return fieldType == WorkbenchTemplateMappingItem.TEXTFIELD;
        }
        
        if (fieldLen > TEXTFIELD_DATA_LEN_MAX)
        {
            return fieldName.toLowerCase().indexOf("remarks") == -1;
        }
        
        return true;
    }
    
    protected static boolean useTextField(final WorkbenchTemplateMappingItem wbtmi)
    {
        return useTextField(wbtmi.getFieldName(), wbtmi.getFieldType(), wbtmi.getDataFieldLength());
    }
    
    /**
     * Returns the number of columns it should use for the TextComponent.
     * @param wbtmi the mapping item
     * @return the number of columns for the UI component
     */
    public static short getColumns(final WorkbenchTemplateMappingItem wbtmi)
    {
        // Check to see if the length of the data field is past threshold TEXTFIELD_DATA_LEN_MAX
        // if so, then only create a TextArea if the field name contains the word "remarks"
        // the user can switch other field fro TextField to TextArea if the wish.
        short columns = useTextField(wbtmi) ? DEFAULT_TEXTFIELD_COLS : DEFAULT_TEXTAREA_COLS;
        
        String metaData = wbtmi.getMetaData();
        if (StringUtils.isNotEmpty(metaData))
        {
            Properties props = UIHelper.parseProperties(metaData);
            if (props != null)
            {
                columns = (short)UIHelper.getProperty(props, "columns", useTextField(wbtmi) ? DEFAULT_TEXTFIELD_COLS : DEFAULT_TEXTAREA_COLS);
            }
        }
        return columns;
    }
    
    public static short getRows(final WorkbenchTemplateMappingItem wbtmi)
    {
        // Check to see if the length of the data field is past threshold TEXTFIELD_DATA_LEN_MAX
        // if so, then only create a TextArea if the field name contains the word "remarks"
        // the user can switch other field fro TextField to TextArea if the wish.
        short rows = useTextField(wbtmi) ? DEFAULT_TEXTFIELD_ROWS : DEFAULT_TEXTAREA_ROWS;
        
        String metaData = wbtmi.getMetaData();
        if (StringUtils.isNotEmpty(metaData))
        {
            Properties props = UIHelper.parseProperties(metaData);
            if (props != null)
            {
                rows = (short)UIHelper.getProperty(props, "rows", useTextField(wbtmi) ? DEFAULT_TEXTFIELD_ROWS : DEFAULT_TEXTAREA_ROWS);
            }
        }
        return rows;
    }
    
    /**
     * Creates the proper UI component for the Mapping item.
     * @param dbFieldType the field type
     * @param caption the caption
     * @param fieldName the name of the field
     * @param dataFieldLength the length of the definition for that field
     * @param fieldType the field type
     * @return a UI component for editing
     */
    protected JComponent createUIComp(final Class<?> dbFieldTypeArg,
                                      final String   caption,
                                      final String   fieldName,
                                      final Short    fieldType,
                                      final Short    fieldLength, 
                                      final short    columns,
                                      final short    rows)
    {
        //System.out.println(wbtmi.getCaption()+" "+wbtmi.getDataType()+" "+wbtmi.getFieldLength());
        Class<?> dbFieldType = dbFieldTypeArg;
        if (dbFieldType == null)
        {
            // if we can't find a class for the field (i.e. Genus Species, or other 'fake' fields), we say it's a string
            dbFieldType = String.class;
        }
        
        JComponent comp;
        Component focusComp;
        
        // handle dates
        if (dbFieldType.equals(Calendar.class) || dbFieldType.equals(Date.class))
        {
            //ValFormattedTextField txt = new ValFormattedTextField("Date"); 
            //txt.setColumns(columns == -1 ? DEFAULT_TEXTFIELD_COLS : columns);
            ValTextField txt = new ValTextField(columns);
            txt.getDocument().addDocumentListener(this);
            comp      = txt;
            focusComp = comp;
            
        }
        else if (dbFieldType.equals(Boolean.class)) // strings
        {
            ValCheckBox checkBox = new ValCheckBox(caption, false, false);
            checkBox.addChangeListener(this);
            comp      = checkBox;
            focusComp = comp;
        }
        else if (useTextField(fieldName, fieldType, fieldLength))
        {
            ValTextField txt = new ValTextField(columns);
            txt.getDocument().addDocumentListener(this);
            txt.setInputVerifier(new LengthVerifier(caption, fieldLength));
            comp      = txt;
            focusComp = comp;
        }
        else
        {
            JScrollPane taScrollPane = createTextArea(columns, rows);
            ((JTextArea)taScrollPane.getViewport().getView()).setInputVerifier(new LengthVerifier(caption, fieldLength));
            comp = taScrollPane;
            focusComp = taScrollPane.getViewport().getView();
        }
        
        focusComp.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e)
            {
                selectControl(e.getSource());
            }
            public void focusLost(FocusEvent e) {}
        });
        
        
        comp.addKeyListener(new KeyAdapter() {

            /* (non-Javadoc)
             * @see java.awt.event.KeyAdapter#keyTyped(java.awt.event.KeyEvent)
             */
            @Override
            public void keyPressed(KeyEvent e)
            {
                if ((e.isControlDown() || e.isMetaDown()) && e.getKeyCode() == KeyEvent.VK_N)
                {
                    workbenchPane.addRowAfter();
                }
                super.keyTyped(e);
            }
            
        });
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
        short          fieldType;
        short          rows;
        if (inputPanel.getComp() instanceof JTextField)
        {
            JTextField tf = (JTextField)inputPanel.getComp();
            tf.getDocument().removeDocumentListener(this);
            fieldType = WorkbenchTemplateMappingItem.TEXTAREA;
            oldComp   = tf;
            rows      = DEFAULT_TEXTAREA_ROWS; 
        } else
        {
            JTextArea ta = (JTextArea)inputPanel.getComp();
            ta.getDocument().removeDocumentListener(this);
            fieldType = WorkbenchTemplateMappingItem.TEXTFIELD;
            oldComp   = ta;
            rows      = DEFAULT_TEXTFIELD_ROWS; 
        }
        
        WorkbenchTemplateMappingItem wbtmi = inputPanel.getWbtmi();
        inputPanel.setComp(createUIComp(WorkbenchTask.getDataType(wbtmi), 
                                        wbtmi.getCaption(), 
                                        wbtmi.getFieldName(), 
                                        fieldType, 
                                        wbtmi.getDataFieldLength(), 
                                        fieldLen,
                                        rows));
        
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
        if (controlPropsDlg == null)
        {
            controlPropsDlg = new EditFormControlDlg((Frame)UIRegistry.get(UIRegistry.FRAME), "", selectedInputPanel, this); // I18N
        }
        
        if (selectedInputPanel != null) // this shouldn't happen
        {
            controlPropsDlg.setTitle(String.format(getResourceString("WB_EDIT_DLG_TITLE"), new Object[] {selectedInputPanel.getControlTitle()}));
            
            controlPropsDlg.setVisible(true);
            if (!controlPropsDlg.isCancelled())
            {
                workbenchPane.gridColumnsUpdated();
                
                adjustPanelSize();
            }
            controlPropsDlg.dispose();
            controlPropsDlg = null;
        }
    }
    
    /**
     * Adjust the size of the FormPane to encompass the the controls. 
     */
    protected void adjustPanelSize()
    {
        int maxX = 0;
        int maxY = 0;
        
        for (InputPanel panel : uiComps)
        {
            Rectangle r = panel.getBounds();
            maxX = Math.max(r.x+r.width, maxX);
            maxY = Math.max(r.y+r.height, maxY);
        }
        
        Dimension size = getSize();
        if (maxX != size.width || maxY != size.height)
        {
            initialSize.width  = maxX;
            initialSize.height = maxY;
            scrollPane.validate();
            scrollPane.repaint();
        }
    }

    /**
     * Tells the form it is being hidden.
     * @param show true - show, false hide
     */
    public void aboutToShowHide(final boolean show)
    {
        if (!show)
        {
            if (currentIndex > -1 && changesInForm)
            {
                copyDataFromForm(currentIndex);
            }
        } else 
        {  
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    if (firstComp != null)
                    {
                        firstComp.requestFocus();
                    }
                }
            });
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
               controlPropsDlg.setVisible(true);
           }
        } else if (controlPropsDlg != null)
        {
            controlPropsDlg.setVisible(false);
            wasShowing = true;
            
        } else
        {
            wasShowing = false;
        }
    }
    
    /**
     * Copies the data from the form into the row.
     */
    public void copyDataFromForm()
    {
        copyDataFromForm(currentIndex);
    }
    
    /**
     * Copies the data from the form into the row.
     * @param index the index of the row
     */
    protected void copyDataFromForm(final int index)
    {
        if (!changesInForm)
        {
            return;
        }
        
        ignoreChanges = true;
        
        changesInForm = false;
        
        WorkbenchRow wbRow = workbench.getWorkbenchRowsAsList().get(index);
        for (InputPanel p : uiComps)
        {
            short col = p.getWbtmi().getViewOrder();
            
            if (p.getComp() instanceof JTextComponent)
            {
                String data     = ((JTextComponent)p.getComp()).getText();
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
               log.error("Can't get data from control["+p.getLabelText()+"]");
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
        if (!aFlag && controlPropsDlg != null)
        {
            controlPropsDlg.setVisible(false);
        }
        super.setVisible(aFlag);
    }
    
    /**
     * Sets the data into the form for a given index.
     * @param index the index of the record to be set
     */
    protected void setDataIntoForm(final int index)
    {
        if (index < 0)
        {
            ignoreChanges = true;
            for (InputPanel panel : uiComps)
            {
                panel.getLabel().setEnabled(false);
                Component comp = panel.getComp();
                comp.setEnabled(false);
                if (comp instanceof JTextComponent)
                {
                    ((JTextComponent)comp).setText("");
                } else if (comp instanceof JCheckBox)
                {
                    ((JCheckBox)comp).setSelected(false);
                }
            } 
            ignoreChanges = false;
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run()
                {
                    workbenchPane.getResultSetController().getNewRecBtn().requestFocus();
                }
            });
           
            return;
            
        } else if (index == 0 && workbench.getWorkbenchRowsAsList().size() == 1)
        {
            for (InputPanel panel : uiComps)
            {
                panel.getLabel().setEnabled(true);
                panel.getComp().setEnabled(true);
            } 
        }
        
        ignoreChanges = true; // turn off change notification
        
        WorkbenchRow wbRow = workbench.getWorkbenchRowsAsList().get(index);
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

    //-----------------------------------------------
    // ResultSetControllerListener Interface
    //-----------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ResultSetControllerListener#indexAboutToChange(int, int)
     */
    public boolean indexAboutToChange(int oldIndex, int newIndex)
    {
        if (newIndex != currentIndex && currentIndex > -1 && scrollPane.isVisible())
        {
            copyDataFromForm(currentIndex);
            currentIndex = newIndex;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ResultSetControllerListener#indexChanged(int)
     */
    public void indexChanged(int newIndex)
    {
        if (scrollPane.isVisible())
        {
            currentIndex  = newIndex;
            setDataIntoForm(currentIndex);
            
            if (firstComp != null)
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        firstComp.requestFocus();
                    }
                });
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.ResultSetControllerListener#newRecordAdded()
     */
    public void newRecordAdded()
    {
        if (currentIndex > -1)
        {
            copyDataFromForm(currentIndex);
        }
        workbenchPane.addRowAfter();
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
            Point offset   = ((GhostGlassPane)UIRegistry.get(UIRegistry.GLASSPANE)).getOffset();
            location.translate(offSet.x - offset.x, offSet.y - offset.y);
            location.x = Math.max(0, location.x);
            location.y = Math.max(0, location.y);
            //System.out.println("***************************************");
            inputPanel.setLocation(location);
            remove(inputPanel);
            add(inputPanel);
            repaint();
            validate();
            doLayout();
            
            adjustPanelSize();
            
            // Update the template
            WorkbenchTemplateMappingItem wbtmi = inputPanel.getWbtmi();
            wbtmi.setXCoord((short)location.x);
            wbtmi.setYCoord((short)location.y);
            workbenchPane.setChanged(true);
            
            UsageTracker.getUsageCount("WBLayoutFormDrop");
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
    public Object getDataForClass(Class<?> classObj)
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
    
    //-----------------------------------------------
    // Inner Class
    //-----------------------------------------------
    class LengthVerifier extends InputVerifier
    {
        protected String caption;
        protected int    maxLength;
        
        public LengthVerifier(final String caption, final int maxLength)
        {
            this.caption   = caption;
            this.maxLength = maxLength;
        }
        
        @Override
        public boolean verify(JComponent comp)
        {
            boolean isOK = ((JTextComponent)comp).getText().length() <= maxLength;
            if (!isOK)
            {
                String msg = String.format(getResourceString("WB_NEWDATA_TOO_LONG"), new Object[] { caption, maxLength } );
                UIRegistry.getStatusBar().setErrorMessage(msg);
                Toolkit.getDefaultToolkit().beep();
                
            } else
            {
                UIRegistry.getStatusBar().setText("");
            }
            return isOK;
        }
    }
    
}
