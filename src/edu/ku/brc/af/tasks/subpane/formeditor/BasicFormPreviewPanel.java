/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.af.tasks.subpane.formeditor;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Rectangle;
import java.util.Stack;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import com.jgoodies.forms.layout.CellConstraints;

import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.persist.AltViewIFace;
import edu.ku.brc.af.ui.forms.persist.FormCell;
import edu.ku.brc.af.ui.forms.persist.View;
import edu.ku.brc.af.ui.forms.persist.ViewDef;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 26, 2007
 *
 */
public class BasicFormPreviewPanel extends JPanel
{
    private static final Logger log = Logger.getLogger(BasicFormPreviewPanel.class);
    
    protected Vector<Pair<Component, FormCell>> controls     = new Vector<Pair<Component, FormCell>>();
    protected Stack<Pair<Component, FormCell>>  recycleCache = new Stack<Pair<Component, FormCell>>();
    
    protected MultiView   multiView = null;
    protected View        view      = null;
    protected ViewDef     viewDef   = null;
    
    protected CellConstraints cc           = new CellConstraints();
    
    /**
     * 
     */
    public BasicFormPreviewPanel()
    {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 10));

        rebuild(false);
    }
    
    /**
     * @param formViewDef the formViewDef to set
     */
    public void set(final View view)
    {
        this.view = view;
    }

    public void setFormViewDef(final ViewDef viewDef)
    {
        this.viewDef = viewDef;
    }

    /**
     * @param formCell
     * @return
     */
    protected Pair<Component, FormCell> createPair(final FormCell formCell)
    {
        Pair<Component, FormCell> pair = null;
        /*if (recycleCache.size() > 0)
        {
            pair = recycleCache.pop();
        } else
        {
            //pair = new Pair<Component, FormCell>(, formCell);
        }
        Component lbl = pair.first;
        if (formCell instanceof FormCellField)
        {
            
        } else
        {
            
        }
        pair.first.setText(formCell.toString());*/
        return pair;
    }
    
    /*protected Component createComponent(final FormCell cell)
    {
        if (cell instanceof FormCellField)
        {
            FormCellField          cellField = (FormCellField)cell;
            String                 pickListName = cellField.getPickListName();
            PickListDBAdapterIFace adapter      = null;
            if (isNotEmpty(pickListName))
            {
                adapter = PickListDBAdapterFactory.getInstance().create(pickListName, false);
                
                if (adapter == null || adapter.getPickList() == null)
                {
                    throw new RuntimeException("PickList Adapter ["+pickListName+"] cannot be null!");
                }
            }
            
            FormValidator validator = null;
            MultiView     parent    = null;
            FormCellField.FieldType uiType = cellField.getUiType();
            
            AltViewIFace.CreationMode mode = AltViewIFace.CreationMode.VIEW;
            
            Component compToAdd = null;
            switch (cellField.getDspUIType())
            {
                case text:
                    compToAdd = ViewFactory.createTextField(validator, cellField, adapter);
                    break;
                
                case formattedtext:
                    compToAdd = ViewFactory.createFormattedTextField(validator, cellField, mode == AltViewIFace.CreationMode.VIEW, cellField.getPropertyAsBoolean("alledit", false));
                    break;
                    
                case label:
                    JLabel label = new JLabel("", SwingConstants.LEFT);
                    compToAdd = label;
                    break;
                    
                case dsptextfield:
                    if (StringUtils.isEmpty(cellField.getPickListName()))
                    {
                        JTextField text = new JTextField(cellField.getTxtCols());
                        ViewFactory.changeTextFieldUIForDisplay(text, cellField.getPropertyAsBoolean("transparent", false));
                        compToAdd = text;
                    } else
                    {
                        compToAdd = ViewFactory.createTextField(validator, cellField, adapter);
                    }
                    break;

                case textfieldinfo:
                    compToAdd = ViewFactory.createTextFieldWithInfo(cellField, parent);
                    break;

                    
                case image:
                    compToAdd = ViewFactory.createImageDisplay(cellField, mode, validator);
                    break;

                
                case url:
                    compToAdd = new BrowserLauncherBtn(cellField.getProperty("title"));

                    break;
                
                case combobox:
                    compToAdd = ViewFactory.createValComboBox(validator, cellField, adapter);
                    break;
                    
                case checkbox:
                {
                    ValCheckBox checkbox = new ValCheckBox(cellField.getLabel(), 
                                                           cellField.isRequired(), 
                                                           cellField.isReadOnly() || mode == AltViewIFace.CreationMode.VIEW);
                    compToAdd = checkbox;
                    break;
                }
                
                case spinner:
                {
                    String minStr = cellField.getProperty("min");
                    int    min    = StringUtils.isNotEmpty(minStr) ? Integer.parseInt(minStr) : 0;
                    
                    String maxStr = cellField.getProperty("max");
                    int    max    = StringUtils.isNotEmpty(maxStr) ? Integer.parseInt(maxStr) : 0; 
                    
                    ValSpinner spinner = new ValSpinner(min, max, cellField.isRequired(), 
                                                           cellField.isReadOnly() || mode == AltViewIFace.CreationMode.VIEW);
                    compToAdd = spinner;
                    break;
                }                            
                 
                case password:
                    compToAdd      = ViewFactory.createPasswordField(validator, cellField);
                    break;
                
                case dsptextarea:
                    compToAdd = ViewFactory.createDisplayTextArea(cellField);
                    break;
                
                case textarea:
                {
                    JTextArea ta = ViewFactory.createTextArea(validator, cellField);
                    JScrollPane scrollPane = new JScrollPane(ta);
                    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
                    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
                    compToAdd = scrollPane;
                    break;
                }
                
                case browse:
                    BrowseBtnPanel bbp = new BrowseBtnPanel(ViewFactory.createTextField(validator, cellField, null), 
                                                            cellField.getPropertyAsBoolean("dirsonly", false), 
                                                            cellField.getPropertyAsBoolean("forinput", true));
                    compToAdd = bbp;
                    break;
                    
                case querycbx:
                {
                    ValComboBoxFromQuery cbx = ViewFactory.createQueryComboBox(validator, cellField);
                    cbx.setMultiView(parent);
                    cbx.setFrameTitle(cellField.getProperty("title"));
                    
                    compToAdd = cbx;
                    break;
                }
                
                case list:
                {
                    JList list = ViewFactory.createList(validator, cellField);
                    
                    JScrollPane scrollPane = new JScrollPane(list);
                    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

                    compToAdd = scrollPane;
                    break;
                }
                
                case colorchooser:
                {
                    ColorChooser colorChooser = new ColorChooser(Color.BLACK);
                    compToAdd = colorChooser;

                    break;
                }
                
                case button:
                    JButton btn = createButton(cellField.getProperty("title"));
                    
                    compToAdd = btn;
                    break;
                    
                case progress:
                    compToAdd = new JProgressBar(0, 100);
                    break;
                
                case plugin:
                    compToAdd = ViewFactory.createPlugin(validator, cellField, mode == AltViewIFace.CreationMode.VIEW);
                    break;

                case textpl:
                    JTextField txt = new TextFieldFromPickListTable(adapter);
                    ViewFactory.changeTextFieldUIForDisplay(txt, cellField.getPropertyAsBoolean("transparent", false));
                    compToAdd = txt;
                    break;
                
                default:
                    throw new RuntimeException("Don't recognize uitype=["+uiType+"]");
            }
            return compToAdd;
        }
        
        return createFormCell(cell);
    }*/
    
    /**
     * @param formCell
     * @return
     */
    /*protected Component createFormCell(final FormCell cell)
    {
        Component compToAdd = null;
        
        if (cell.getType() == FormCellIFace.CellType.label)
        {
            FormCellLabel cellLabel = (FormCellLabel)cell;

            String lblStr = cellLabel.getLabel();
            if (cellLabel.isRecordObj())
            {
                JComponent riComp = viewBldObj.createRecordIndentifier(lblStr, cellLabel.getIcon());
                compToAdd = riComp;
                
            } else
            {
                boolean useColon = StringUtils.isNotEmpty(cellLabel.getLabelFor());
                int align;
                String alignProp = cellLabel.getProperty("align");
                System.out.println(lblStr);
                if (StringUtils.isNotEmpty(alignProp))
                {
                    if (alignProp.equals("left"))
                    {
                        align = SwingConstants.LEFT;
                        
                    } else if (alignProp.equals("center"))
                    {
                        align = SwingConstants.CENTER;
                        
                    } else
                    {
                        align = SwingConstants.RIGHT;
                    }
                } else if (useColon)
                {
                    align = SwingConstants.RIGHT;
                } else
                {
                    align = SwingConstants.LEFT;
                }
                
                String lStr;
                if (isNotEmpty(lblStr))
                {
                    if (useColon)
                    {
                        lStr = lblStr + ":";
                    } else
                    {
                        lStr = lblStr;
                    }
                } else
                {
                    lStr = "  ";
                }
                JLabel lbl = new JLabel(lStr, align);
                compToAdd      =  lbl;
                viewBldObj.addLabel(cellLabel, lbl);
            }


        } else if (cell.getType() == FormCellIFace.CellType.separator)
        {
            
            // still have compToAdd = null;
            FormCellSeparatorIFace fcs             = (FormCellSeparatorIFace)cell;
            String            collapsableName = fcs.getCollapseCompName();
            Component         sep             = viewBldObj.createSeparator(fcs.getLabel());
            if (isNotEmpty(collapsableName))
            {
                CollapsableSeparator collapseSep = new CollapsableSeparator(sep);
                if (collapseSepHash == null)
                {
                    collapseSepHash = new Hashtable<CollapsableSeparator, String>();
                }
                collapseSepHash.put(collapseSep, collapsableName);
                sep = collapseSep;
                
            }
            compToAdd      = (JComponent)sep;

        } else if (cell.getType() == FormCellIFace.CellType.command)
        {
            FormCellCommand cellCmd = (FormCellCommand)cell;
            JButton btn  = createButton(cellCmd.getLabel());
            if (cellCmd.getCommandType().length() > 0)
            {
                btn.addActionListener(new CommandActionWrapper(new CommandAction(cellCmd.getCommandType(), cellCmd.getAction(), "")));
            }
            compToAdd = btn;

        } 
        else if (cell.getType() == FormCellIFace.CellType.iconview)
        {
            FormCellSubView cellSubView = (FormCellSubView) cell;

            String subViewName = cellSubView.getViewName();

            
        } else if (cell.getType() == FormCellIFace.CellType.subview)
        {
            FormCellSubView cellSubView = (FormCellSubView)cell;

            String subViewName = cellSubView.getViewName();
        }
    }*/
    
    /**
     * @param pair
     */
    protected void recycle(final Pair<Component, FormCell> pair)
    {
        pair.second = null;
        recycleCache.push(pair);
    }
    
    /**
     * 
     */
    public void rebuild(final boolean isEdit)
    {
        if (multiView != null)
        {
            //multiView.shutdown();
        }
        
        removeAll();
        
        if (viewDef == null)
        {
            add(new JLabel(getResourceString("BasicFormPreviewPanel.PREVIEW_UNAVAILABLE")), BorderLayout.CENTER); //$NON-NLS-1$
            return;
        }
        
        if (view != null)
        {
            multiView = new MultiView(null,
                                      null, 
                                      view, 
                                      isEdit ? AltViewIFace.CreationMode.EDIT : AltViewIFace.CreationMode.VIEW,
                                              MultiView.IS_NEW_OBJECT, null);


            if (multiView != null)
            {
                add(multiView, BorderLayout.CENTER);
                
            } else
            {
                log.error("Couldn't load Multiview from View["+view+"]"); //$NON-NLS-1$ //$NON-NLS-2$
                return;
            }
            
        } else
        {
            log.error("Couldn't load Multiview the view was null"); //$NON-NLS-1$
            return;

        }
        
        Component parent = getParent();
        while (!(parent instanceof Frame))
        {
            parent = parent.getParent();
        }
        Frame frame = (Frame)parent;
        frame.pack();
        
        Rectangle r = frame.getBounds();
        r.width += 20;
        r.height += 20;
        frame.setBounds(r);
        
        /*
        removeAll();
        
        if (formViewDef == null)
        {
            setLayout(new BorderLayout());
            add(new JLabel("Preview is Not Available"), BorderLayout.CENTER);
            return;
        }
        
        setLayout(null);
        
        for (Pair<Component, FormCell> pair : controls)
        {
            recycle(pair);
        }
        controls.clear();
        
        int maxRows = formViewDef.getRows().size();
        int maxCols = 0;
        for (FormRowIFace row : formViewDef.getRows())
        {
            int cols = 0;
            for (FormCellIFace cell : row.getCells())
            {
                cols += cell.getColspan();
            }
            maxCols = Math.max(maxCols, cols);
        }
        
        String colDef = UIHelper.createDuplicateJGoodiesDef("p", "2px", maxCols);
        String rowDef = UIHelper.createDuplicateJGoodiesDef("p", "2px", maxRows);
        PanelBuilder pb = new PanelBuilder(new FormLayout(colDef, rowDef), this);
        
        int rowInx = 1;
        for (FormRowIFace row : formViewDef.getRows())
        {
            int colInx = 1; 
            for (FormCellIFace cell : row.getCells())
            {
                int rowSpan = cell.getRowspan();
                int colSpan = cell.getColspan();
                
                Pair<Component, FormCell> pair = createPair((FormCell)cell);
                pb.add(pair.first, cc.xywh(colInx, rowInx, colSpan, rowSpan));
                colInx += colSpan + 1;
            }
            rowInx += 2;
        }
        */
        validate();
        repaint();
    }
    
}
