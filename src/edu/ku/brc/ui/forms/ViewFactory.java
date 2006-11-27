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
package edu.ku.brc.ui.forms;

import static edu.ku.brc.ui.validation.UIValidator.parseValidationType;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.split;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.helpers.BrowserLauncher;
import edu.ku.brc.ui.BrowseBtnPanel;
import edu.ku.brc.ui.ColorChooser;
import edu.ku.brc.ui.ColorWrapper;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandActionWrapper;
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.ImageDisplay;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UIPluginable;
import edu.ku.brc.ui.db.PickListDBAdapterFactory;
import edu.ku.brc.ui.db.PickListDBAdapterIFace;
import edu.ku.brc.ui.db.TextFieldFromPickListTable;
import edu.ku.brc.ui.db.TextFieldWithInfo;
import edu.ku.brc.ui.forms.persist.AltView;
import edu.ku.brc.ui.forms.persist.FormCell;
import edu.ku.brc.ui.forms.persist.FormCellCommand;
import edu.ku.brc.ui.forms.persist.FormCellField;
import edu.ku.brc.ui.forms.persist.FormCellLabel;
import edu.ku.brc.ui.forms.persist.FormCellPanel;
import edu.ku.brc.ui.forms.persist.FormCellSeparator;
import edu.ku.brc.ui.forms.persist.FormCellSubView;
import edu.ku.brc.ui.forms.persist.FormRow;
import edu.ku.brc.ui.forms.persist.FormViewDef;
import edu.ku.brc.ui.forms.persist.View;
import edu.ku.brc.ui.forms.persist.ViewDef;
import edu.ku.brc.ui.validation.DataChangeNotifier;
import edu.ku.brc.ui.validation.FormValidator;
import edu.ku.brc.ui.validation.TypeSearchForQueryFactory;
import edu.ku.brc.ui.validation.UIValidator;
import edu.ku.brc.ui.validation.ValComboBox;
import edu.ku.brc.ui.validation.ValComboBoxFromQuery;
import edu.ku.brc.ui.validation.ValFormattedTextField;
import edu.ku.brc.ui.validation.ValListBox;
import edu.ku.brc.ui.validation.ValPasswordField;
import edu.ku.brc.ui.validation.ValTextArea;
import edu.ku.brc.ui.validation.ValTextField;
import edu.ku.brc.ui.validation.ValidatedJPanel;

/**
 * Creates FormViewObj object that implment the Viewable interface.
 
 * @code_status Complete
 **
 * @author rods
 *
 */
public class ViewFactory
{
    // Statics
    private static final Logger log = Logger.getLogger(ViewFactory.class);
    private static final ViewFactory  instance = new ViewFactory();

    // Data Members
    protected static ColorWrapper     viewFieldColor = null;

    protected MultiView               rootMultiView  = null; // transient - is valid only during a build process

    /**
     * Constructor.
     */
    protected ViewFactory()
    {
        // do nothing
    }

    /**
     * Returns the singleton for the ViewSetMgr.
     * @return the singleton for the ViewSetMgr
     */
    public static ViewFactory getInstance()
    {
        return instance;
    }

    /**
     * Creates a panel with the "..." icon.
     * @param comp the component to put into the panel
     */
    public JPanel createIconPanel(final JComponent comp)
    {
        JPanel  panel = new JPanel(new BorderLayout());
        JButton btn   = new JButton("...");
        panel.add(btn, BorderLayout.WEST);
        panel.add(comp, BorderLayout.EAST);
        return panel;
    }

    /**
     * Returns a ViewDef Obj with the form UI built.
     * @param view the view definition
     * @param altView which AltView to build
     * @param parentView the MultiViw that this view/form will be parented to
     * @param options the options needed for creating the form
     * @return a Viewable Obj with the form UI built
     */
    public Viewable buildViewable(final View      view, 
                                  final AltView   altView, 
                                  final MultiView parentView,
                                  final int       options)
    {
        if (viewFieldColor == null)
        {
            viewFieldColor = AppPrefsCache.getColorWrapper("ui", "formatting", "viewfieldcolor");
        }

        ViewDef viewDef = altView.getViewDef();

        if (viewDef == null) return null;

        this.rootMultiView =  parentView;

        if (viewDef.getType() == ViewDef.ViewType.form)
        {
            Viewable viewable = buildFormViewable(view, altView, parentView, options);
            this.rootMultiView =  null;
            return viewable;

        } else if (viewDef.getType() == FormViewDef.ViewType.table ||
                   viewDef.getType() == FormViewDef.ViewType.formTable)
        {
            Viewable viewable = buildTableViewable(view, altView, parentView, options);
            this.rootMultiView =  null;
            return viewable;

        } else if (viewDef.getType() == FormViewDef.ViewType.field)
        {
            this.rootMultiView =  null;
            return null;

        }
        else if (viewDef.getType() == FormViewDef.ViewType.iconview)
        {
            return new IconViewObj(view, altView, parentView, options);
        }
        else if (viewDef.getType() == FormViewDef.ViewType.rstable)
        {
            return new RecordSetTableViewObj(view, altView, parentView, options);
                
        } else
        {
            this.rootMultiView =  null;
            throw new RuntimeException("Form Type not covered by builder ["+viewDef.getType()+"]");
        }

    }

    /**
     * Creates a ValTextField.
     * @param validator a validator to hook the control up to (may be null)
     * @param cellField the definition of the cell for this control
     * @return a ValTextField
     */
    protected JTextField createTextField(final FormValidator validator,
                                         final FormCellField cellField,
                                         final PickListDBAdapterIFace adapter)
    {
        String validationRule = cellField.getValidationRule();

        JTextField txtField;
        if (validator != null && (cellField.isRequired() || isNotEmpty(validationRule) || cellField.isChangeListenerOnly()))
        {
            ValTextField textField = new ValTextField(cellField.getCols(), adapter);
            textField.setRequired(cellField.isRequired());

            validator.hookupTextField(textField,
                                      cellField.getId(),
                                      cellField.isRequired(),
                                      parseValidationType(cellField.getValidationType()),
                                      cellField.getValidationRule(),
                                      cellField.isChangeListenerOnly());

            txtField = textField;
            textField.setEditable(!cellField.isReadOnly());

        } else if (adapter != null)
        {
            ValTextField textField = new ValTextField(cellField.getCols(), adapter);

            txtField = textField;
            textField.setEditable(false);

        } else
        {
            txtField = new JTextField(cellField.getCols());
        }

        return txtField;
    }

    /**
     * Creates a ValPasswordField.
     * @param validator a validator to hook the control up to (may be null)
     * @param cellField the definition of the cell for this control
     * @return a ValPasswordField
     */
    public JTextField createPasswordField(final FormValidator validator,
                                          final FormCellField cellField)
    {
        String validationRule = cellField.getValidationRule();
        JTextField txt;

        if (validator != null && (cellField.isRequired() || isNotEmpty(validationRule) || cellField.isChangeListenerOnly()))
        {
            ValPasswordField textField = new ValPasswordField(cellField.getCols());
            textField.setRequired(cellField.isRequired());
            textField.setEncrypted(cellField.isEncrypted());

            validator.hookupTextField(textField,
                                      cellField.getId(),
                                      cellField.isRequired(),
                                      parseValidationType(cellField.getValidationType()),
                                      validationRule,
                                      cellField.isChangeListenerOnly());

           txt = textField;

        } else
        {
            txt = new ValPasswordField(cellField.getCols());
        }
        return txt;
    }


    /**
     * Creates a ValFormattedTextField.
     * @param validator a validator to hook the control up to (may be null)
     * @param cellField the definition of the cell for this control
     * @return ValFormattedTextField
     */
    protected JTextField createFormattedTextField(final FormValidator validator,
                                                  final FormCellField cellField)
    {
        log.debug(cellField.getName()+"  "+cellField.getUIFieldFormatter());
        ValFormattedTextField textField;// = new ValFormattedTextField(cellField.getUIFieldFormatter());

        // Because it is formatted we ALWAYS validate it when there is a validator
        if (validator != null)
        {
            // deliberately ignore "cellField.isChangeListenerOnly()"
            // pass in false instead

            textField = new ValFormattedTextField(cellField.getUIFieldFormatter());
            textField.setRequired(cellField.isRequired());

            validator.hookupTextField(textField,
                                      cellField.getId(),
                                      cellField.isRequired(),
                                      UIValidator.Type.Changed,  cellField.getValidationRule(), false);
                                      //UIValidator.Type.Changed,  cellField.getName()+".isInError() == false", false);


        } else
        {
            textField = new ValFormattedTextField(cellField.getUIFieldFormatter());
        }

        textField.setEditable(!cellField.isReadOnly());

        return textField;
    }

    /**
     * Creates a ValTextArea.
     * @param validator a validator to hook the control up to (may be null)
     * @param cellField the definition of the cell for this control
     * @return ValTextArea
     */
    protected JTextArea createTextArea(final FormValidator validator,
                                       final FormCellField cellField)
    {
        ValTextArea textArea = new ValTextArea("", cellField.getRows(), cellField.getCols());
        if (validator != null)
        {
            UIValidator.Type type = parseValidationType(cellField.getValidationType());
            DataChangeNotifier dcn = validator.hookupComponent(textArea, cellField.getId(), type, null, true);
            if (type == UIValidator.Type.Changed)
            {
                textArea.getDocument().addDocumentListener(dcn);

            } else if (type == UIValidator.Type.Focus)
            {
                textArea.addFocusListener(dcn);

            } else
            {
               // Do nothing for UIValidator.Type.OK
            }
        }
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        textArea.setEditable(!cellField.isReadOnly());

        return textArea;
    }


    /**
     * Creates a ValListBox.
     * @param validator a validator to hook the control up to (may be null)
     * @param cellField the definition of the cell for this control
     * @return ValListBox
     */
    protected JList createList(final FormValidator validator,
                               final FormCellField cellField)
    {
        String[] initArray = null;
        String dataStr = cellField.getProperty("data");
        if (isNotEmpty(dataStr))
        {
            initArray = split(dataStr, ",");
            for (int i=0;i<initArray.length;i++)
            {
                initArray[i] = initArray[i].trim();
            }
        }

        ValListBox valList = initArray == null ? new ValListBox() : new ValListBox(initArray);
        if (validator != null && (cellField.isRequired() || isNotEmpty(cellField.getValidationRule())))
        {
            DataChangeNotifier dcn = validator.hookupComponent(valList, cellField.getId(), parseValidationType(cellField.getValidationType()), cellField.getValidationRule(), false);
            valList.getModel().addListDataListener(dcn);
            valList.addFocusListener(dcn);
        }
        valList.setRequired(cellField.isRequired());
        
        valList.setVisibleRowCount(cellField.getPropertyAsInt("rows", 15));
        
        return valList;
    }

    /**
     * Creates a ValComboBoxFromQuery.
     * @param validator a validator to hook the control up to (may be null)
     * @param cellField the definition of the cell for this control
     * @return ValComboBoxFromQuery
     */
    protected ValComboBoxFromQuery createQueryComboBox(final FormValidator validator,
                                                       final FormCellField cellField)
    {
        //String cbxName = cellField.getInitialize();
        String cbxName = cellField.getProperty("name");
        if (isNotEmpty(cbxName))
        {
            ValComboBoxFromQuery cbx = TypeSearchForQueryFactory.createValComboBoxFromQuery(cbxName);
            cbx.setRequired(cellField.isRequired());
            if (validator != null && (cellField.isRequired() || isNotEmpty(cellField.getValidationRule())))
            {
                DataChangeNotifier dcn = validator.hookupComponent(cbx, cellField.getId(), parseValidationType(cellField.getValidationType()), cellField.getValidationRule(), false);
                cbx.getComboBox().getModel().addListDataListener(dcn);
                
                if (dcn.getValidationType() == UIValidator.Type.Focus) // returns None when no Validator
                {
                    cbx.addFocusListener(dcn);
                }
            }
            cbx.setCellName(cellField.getName());
            return cbx;

        }
        // else
        throw new RuntimeException("CBX Name for ValComboBoxFromQuery ["+cbxName+"] is empty!");
    }

    /**
     * Creates a ValComboBox.
     * @param validator a validator to hook the control up to (may be null)
     * @param cellField the definition of the cell for this control
     * @return ValComboBox
     */
    protected ValComboBox createValComboBox(final FormValidator validator,
                                            final FormCellField cellField,
                                            final PickListDBAdapterIFace adapter)
    {
        boolean                makeEditable = cellField.getPropertyAsBoolean("editable", false);
        ValComboBox            cbx          = null;
        if (adapter != null)
        {
            cbx = new ValComboBox(adapter); // false means don't auto-create picklist
                
        } else
        {
            String[] initArray = null;
            String data = cellField.getProperty("data");
            if (StringUtils.isNotEmpty(data))
            {
                initArray = split(data, ",");
                for (int i=0;i<initArray.length;i++)
                {
                    initArray[i] = initArray[i].trim();
                }
            }

            cbx = initArray == null || initArray.length == 0 ? new ValComboBox(makeEditable) : new ValComboBox(initArray, makeEditable);
        }
        cbx.setRequired(cellField.isRequired());

        if (validator != null && (cellField.isRequired() || isNotEmpty(cellField.getValidationRule())))
        {
            DataChangeNotifier dcn = validator.hookupComponent(cbx, cellField.getId(), parseValidationType(cellField.getValidationType()), cellField.getValidationRule(), false);
            //cbx.getModel().addListDataListener(dcn);
            cbx.getComboBox().addActionListener(dcn);

            if (dcn.getValidationType() == UIValidator.Type.Focus) // returns None when no Validator
            {
                cbx.addFocusListener(dcn);
            }
        }

        return cbx;
    }
    
    /**
     * Makes adjusts to the border and the colors to make it "flat" for diaply mode.
     * @param textField the text field to be flattened
     */
    protected void changeTextFieldUIForDisplay(final JTextField textField)
    {
        Insets insets = textField.getBorder().getBorderInsets(textField);
        textField.setBorder(BorderFactory.createEmptyBorder(insets.top, insets.left, insets.bottom, insets.bottom));
        textField.setForeground(Color.BLACK);
        textField.setEditable(false);
        if (viewFieldColor != null)
        {
            textField.setBackground(viewFieldColor.getColor());
        }
    }
    
    /**
     * Creates a JTextArea for display purposes only.
     * @param cellField FormCellField info
     * @return the control
     */
    protected JScrollPane createDisplayTextArea(final FormCellField cellField)
    {
        JTextArea ta = new JTextArea(cellField.getRows(), cellField.getCols());
        // ta.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
        Insets insets = ta.getBorder().getBorderInsets(ta);
        ta.setBorder(BorderFactory.createEmptyBorder(insets.top, insets.left, insets.bottom, insets.bottom));
        ta.setForeground(Color.BLACK);
        ta.setEditable(false);
        ta.setBackground(viewFieldColor.getColor());

        JScrollPane scrollPane = new JScrollPane(ta);
        insets = scrollPane.getBorder().getBorderInsets(scrollPane);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(insets.top, insets.left, insets.bottom, insets.bottom));
        
        return scrollPane;
    }
    
    /**
     * Creates a TextFieldWithInfo.
     * @param cellField FormCellField info
     * @param parent the parent mulitview needed because of pop Dialogs (from the info btn)
     * @return the control
     */
    protected TextFieldWithInfo createTextFieldWithInfo(final FormCellField cellField,
                                                        final MultiView         parent)
    {
        TextFieldWithInfo textFieldInfo;
        String            txtName = cellField.getProperty("name");
        if (isNotEmpty(txtName))
        {
            textFieldInfo = TypeSearchForQueryFactory.getTextFieldWithInfo(txtName);
            textFieldInfo.setMultiView(parent);
            textFieldInfo.setFrameTitle(cellField.getProperty("title"));
            
        } else
        {
            throw new RuntimeException("textfieldinfo Name for textFieldWithInfo ["+txtName+"] is empty!");
        }

        JTextField textField = textFieldInfo.getTextField();
        textField.setColumns(cellField.getCols());
        
        changeTextFieldUIForDisplay(textField);
        return textFieldInfo;
    }
    
    /**
     * Creates an ImageDisplay control,
     * @param cellField FormCellField info
     * @param mode indicates whether in Edit or View mode
     * @return the control
     */
    protected ImageDisplay createImageDisplay(final FormCellField cellField,
                                              final AltView.CreationMode mode,
                                              final MultiView parent)
    {
        int w = 150;
        int h = 150;
        String sizeDefStr = cellField.getProperty("size");
        if (isNotEmpty(sizeDefStr))
        {
            String[] wh = StringUtils.split(sizeDefStr, ",");
            if (wh.length == 2)
            {
                try
                {
                    w = Integer.parseInt(wh[0]);
                    h = Integer.parseInt(wh[1]);

                } catch (Exception ex)
                {
                    log.error("size prop for Image is incorrect ["+sizeDefStr+"]");
                }
            }

        }
        
        boolean imageInEdit = mode == AltView.CreationMode.Edit;
        String editModeStr = cellField.getProperty("edit");
        if (isNotEmpty(editModeStr))
        {
            imageInEdit = editModeStr.toLowerCase().equals("true");
        }

        // create a new ImageDisplay
        // override the selectNewImage method to notify the parent MultiView when a change occurs
        ImageDisplay imgDisp = new ImageDisplay(w, h, imageInEdit, cellField.getPropertyAsBoolean("border", true))
        {
            @Override
            protected void selectNewImage()
            {
                super.selectNewImage();
                parent.dataChanged(this.getName(), this, null);
            }
        };
        
        String urlStr = cellField.getProperty("url");
        if (isNotEmpty(urlStr))
        {
            imgDisp.setValue(urlStr, "");
        } else
        {
            String name = cellField.getProperty("icon");
            if (isNotEmpty(name))
            {
                boolean loadIt   = true;
                String  iconSize = cellField.getProperty("iconsize");
                if (isNotEmpty(iconSize))
                {
                    IconManager.IconSize sz = IconManager.getIconSize(Integer.parseInt(iconSize), false, false);
                    if (sz != null)
                    {
                        imgDisp.setImage(IconManager.getImage(name, sz));
                        loadIt = false;
                    }
                    
                } 
                
                if (loadIt)
                {
                    imgDisp.setImage(IconManager.getIcon(name));
                }
            }
        }
        return imgDisp;
    }
    
    /**
     * Creates an ImageDisplay control,
     * @param cellField FormCellField info
     * @param mode indicates whether in Edit or View mode
     * @return the control
     */
    protected JComponent createPlugin(final FormCellField cellField)
    {
        String classNameStr = cellField.getProperty("class");
        if (StringUtils.isEmpty(classNameStr))
        {
            throw new RuntimeException("Creating plugin and the class property was missing.");
        }
        
        try
        {
            JComponent uiObj = Class.forName(classNameStr).asSubclass(JComponent.class).newInstance();
            
            if (!(uiObj instanceof GetSetValueIFace))
            {
                throw new RuntimeException("Plugin of class["+classNameStr+"] doesn't implement the GetSetValueIFace!");
            }
            
            if (uiObj instanceof UIPluginable)
            {
                ((UIPluginable)uiObj).initialize(cellField.getProperties());
            }
            return uiObj;
            
        } catch (Exception ex)
        {
           log.error(ex);
           throw new RuntimeException(ex);
        }
    }
    


    /**
     * Processes the rows in a definition.
     * @param parent MultiView parent
     * @param formViewDef the FormViewDef (Viewdef)
     * @param validator optional validator
     * @param viewBldObj the FormViewObj this row belongs to
     * @param mode the creation mode
     * @param builder the current JGoodies builder
     * @param labelsForHash the has table for label
     * @param cc CellConstraints
     * @param currDataObj the current data object
     * @param formRows the list of rows to be processed
     */
    protected void processRows(final MultiView         parent,
                               final FormViewDef       formViewDef,
                               final FormValidator     validator,
                               final ViewBuilderIFace  viewBldObj,
                               final AltView.CreationMode mode,
                               final Hashtable<String, JLabel> labelsForHash,
                               final Object            currDataObj,
                               final List<FormRow>     formRows)
    {
        int rowInx    = 1;
        int curMaxRow = 1;

        for (FormRow row : formRows)
        {
            int colInx = 1;

            if (rowInx < curMaxRow)
            {
                //rowInx = curMaxRow;
            }
            for (FormCell cell : row.getCells())
            {
                JComponent compToAdd = null;
                JComponent compToReg = null;

                int        colspan   = cell.getColspan();
                int        rowspan   = cell.getRowspan();

                boolean    addToValidator = true;
                boolean    addControl     = true;

                if (cell.getType() == FormCell.CellType.label)
                {
                    FormCellLabel cellLabel = (FormCellLabel)cell;

                    String lblStr = cellLabel.getLabel();
                    if (cellLabel.isRecordObj())
                    {
                        JComponent riComp = viewBldObj.createRecordIndentifier(lblStr, cellLabel.getIcon());
                        compToAdd = riComp;
                        
                    } else
                    {
                        JLabel lbl = new JLabel(isNotEmpty(lblStr) ? lblStr + ":" : "  ", SwingConstants.RIGHT);
                        //lbl.setFont(boldLabelFont);
                        labelsForHash.put(cellLabel.getLabelFor(), lbl);
                        compToAdd      =  lbl;
                        viewBldObj.addLabel(cellLabel, lbl);
                    }

                    addToValidator = false;
                    addControl     = false;


                } else if (cell.getType() == FormCell.CellType.field)
                {
                    FormCellField cellField = (FormCellField)cell;

                    FormCellField.FieldType uiType = cellField.getUiType();
                    
                    // Check to see if there is a picklist and get it if there is
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

                    // The Default Display for combox is dsptextfield, except when there is a TableBased PickList
                    // At the time we set the display we don't want to go get the picklist to find out. So we do it
                    // here after we have the picklist and actually set the change into the cellField
                    // because it uses the value to determine whether to convert the value into a text string 
                    // before setting it.
                    if (mode == AltView.CreationMode.View)
                    {
                        if (uiType == FormCellField.FieldType.combobox && cellField.getDspUIType() != FormCellField.FieldType.textpl)
                        {
                            if (adapter != null && adapter.isTabledBased())
                            {
                                uiType = FormCellField.FieldType.textpl;
                                cellField.setDspUIType(uiType);
                                
                            } else
                            {
                                uiType = cellField.getDspUIType();    
                            }
                        } else
                        {
                            uiType = cellField.getDspUIType();
                        }
                    }
                    
                    // Create the UI Component
                    
                    switch (uiType)
                    {
                        case text:
                            compToAdd = createTextField(validator, cellField, adapter);
                            addToValidator = validator == null; // might already added to validator
                            break;
                        
                        case formattedtext:
                            compToAdd = createFormattedTextField(validator, cellField);
                            addToValidator = validator == null; // might already added to validator
                            break;
                            
                        case label:
                            compToAdd = new JLabel("", SwingConstants.LEFT);
                            break;
                            
                        case dsptextfield:
                            if (StringUtils.isEmpty(cellField.getPickListName()))
                            {
                                JTextField text = new JTextField(cellField.getCols());
                                changeTextFieldUIForDisplay(text);
                                compToAdd = text;
                            } else
                            {
                                compToAdd = createTextField(validator, cellField, adapter);
                                addToValidator = validator == null; // might already added to validator
                            }
                            break;

                        case textfieldinfo:
                            compToAdd = createTextFieldWithInfo(cellField, parent);
                            break;

                            
                        case image:
                            compToAdd = createImageDisplay(cellField, mode, parent);
                            addToValidator = false;
                            break;

                        
                        case url:
                            compToAdd = new BrowserLauncherBtn(cellField.getProperty("title"));
                            addToValidator = false;
    
                            break;
                        
                        case combobox:
                            compToAdd = createValComboBox(validator, cellField, adapter);
                            addToValidator = validator != null; // might already added to validator
                            break;
                        
                        case checkbox:
                        {
                            JCheckBox checkbox = new JCheckBox(cellField.getLabel());
                            if (validator != null)
                            {
                                DataChangeNotifier dcn = validator.createDataChangeNotifer(cellField.getId(), checkbox, null);
                                checkbox.addActionListener(dcn);
                            }
    
                            compToAdd = checkbox;
                            break;
                       }
                         
                        case password:
                            compToAdd      = createPasswordField(validator, cellField);
                            addToValidator = validator == null; // might already added to validator
                            break;
                        
                        case dsptextarea:
                            compToAdd = createDisplayTextArea(cellField);
                            break;
                        
                        case textarea:
                        {
                            JTextArea ta = createTextArea(validator, cellField);
                            JScrollPane scrollPane = new JScrollPane(ta);
                            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
                            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
    
                            addToValidator = validator == null; // might already added to validator
                            compToReg = ta;
                            compToAdd = scrollPane;
                            break;
                        }
                        
                        case browse:
                            BrowseBtnPanel bbp = new BrowseBtnPanel(createTextField(validator, cellField, null));
                            compToAdd = bbp;
                            break;
                            
                        case querycbx:
                        {
                            ValComboBoxFromQuery cbx = createQueryComboBox(validator, cellField);
                            cbx.setMultiView(parent);
                            cbx.setFrameTitle(cellField.getProperty("title"));
                            
                            compToAdd = cbx;
                            addToValidator = validator == null; // might already added to validator
                            break;
                        }
                        
                        case list:
                        {
                            JList list = createList(validator, cellField);
    
                            JScrollPane scrollPane = new JScrollPane(list);
                            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    
                            addToValidator = validator == null;
                            compToReg = list;
                            compToAdd = scrollPane;
                            break;
                        }
                        
                        case colorchooser:
                        {
                            ColorChooser colorChooser = new ColorChooser(Color.BLACK);
                            if (validator != null)
                            {
                                DataChangeNotifier dcn = validator.createDataChangeNotifer(cellField.getName(), colorChooser, null);
                                colorChooser.addPropertyChangeListener("setValue", dcn);
                            }
                            compToAdd = colorChooser;
    
                            break;
                        }
                        
                        case button:
                            compToAdd = new JButton(cellField.getProperty("title"));
                            break;
                            
                        case progress:
                            compToAdd = new JProgressBar(0, 100);
                            break;
                        
                        case plugin:
                            compToAdd = createPlugin(cellField);
                            break;

                        case textpl:
                            compToAdd = new TextFieldFromPickListTable(adapter);
                            break;
                        
                        default:
                            throw new RuntimeException("Don't recognize uitype=["+uiType+"]");
                        
                    } // switch

                } else if (cell.getType() == FormCell.CellType.command)
                {
                    FormCellCommand cellCmd = (FormCellCommand)cell;
                    JButton btn  = new JButton(cellCmd.getLabel());
                    if (cellCmd.getCommandType().length() > 0)
                    {
                        btn.addActionListener(new CommandActionWrapper(new CommandAction(cellCmd.getCommandType(), cellCmd.getAction(), "")));
                    }
                    addToValidator = false;
                    compToAdd = btn;

                } else if (cell.getType() == FormCell.CellType.separator)
                {
                    // still have compToAdd = null;
                    Component sep = viewBldObj.createSeparator(((FormCellSeparator)cell).getLabel(), colInx, rowInx, cell.getColspan());
                    if (cell.getName().length() > 0)
                    {
                        viewBldObj.registerControl(cell, sep);
                    }
                    curMaxRow = rowInx;
                    colInx += 2;

                }
                else if (cell.getType() == FormCell.CellType.iconview)
                {
                    FormCellSubView cellSubView = (FormCellSubView) cell;

                    String subViewName = cellSubView.getViewName();

                    View subView = AppContextMgr.getInstance().getView(cellSubView.getViewSetName(), subViewName);
                    if (subView != null)
                    {
                        if (parent != null)
                        {
                            int options = MultiView.VIEW_SWITCHER
                                    | (MultiView.isOptionOn(parent.getCreateOptions(), MultiView.IS_NEW_OBJECT) ? MultiView.IS_NEW_OBJECT
                                            : 0);

                            MultiView multiView = new MultiView(parent, cellSubView.getName(), subView, parent
                                    .getCreateWithMode(), options);
                            parent.addChild(multiView);

                            viewBldObj.addSubView(cellSubView, multiView, colInx, rowInx, cellSubView.getColspan(), 1);
                            viewBldObj.closeSubView(cellSubView);
                            curMaxRow = rowInx;

                        }
                        else
                        {
                            log.error("buildFormView - parent is NULL for subview [" + subViewName + "]");
                        }

                    }
                    else
                    {
                        log.error("buildFormView - Could find subview's with ViewSet[" + cellSubView.getViewSetName()
                                + "] ViewName[" + subViewName + "]");
                    }
                    // still have compToAdd = null;
                    colInx += 2;
                    
                } else if (cell.getType() == FormCell.CellType.subview)
                {
                    FormCellSubView cellSubView = (FormCellSubView)cell;

                    String subViewName = cellSubView.getViewName();

                    View subView = AppContextMgr.getInstance().getView(cellSubView.getViewSetName(), subViewName);
                    if (subView != null)
                    {
                        if (!viewBldObj.shouldFlatten())
                        {
                            if (parent != null)
                            {
                                int options = (cellSubView.isSingleValueFromSet() ? 0 : MultiView.RESULTSET_CONTROLLER) | MultiView.VIEW_SWITCHER |
                                              (MultiView.isOptionOn(parent.getCreateOptions(), MultiView.IS_NEW_OBJECT) ? MultiView.IS_NEW_OBJECT : 0);
                                    
                                MultiView multiView = new MultiView(parent, 
                                                                    cellSubView.getName(),
                                                                    subView,
                                                                    parent.getCreateWithMode(), 
                                                                    options);
                                parent.addChild(multiView);
    
                                viewBldObj.addSubView(cellSubView, multiView, colInx, rowInx, cellSubView.getColspan(), 1);                               
                                viewBldObj.closeSubView(cellSubView);
                                curMaxRow = rowInx;
                                
                            } else
                            {
                                log.error("buildFormView - parent is NULL for subview ["+subViewName+"]");
                            }
                        } else
                        {
                            viewBldObj.addSubView(cellSubView, parent, colInx, rowInx, cellSubView.getColspan(), 1); 
                            
                            AltView  altView;
                            //if (createWithMode != null)
                            //{
                            //    altView = subView.getDefaultAltViewWithMode(createWithMode);
                            //} else
                            //{
                                altView = subView.getDefaultAltView();
                            //}
                            FormViewDef subFormViewDef = (FormViewDef)altView.getViewDef();
                            processRows(parent, formViewDef, validator, viewBldObj, altView.getMode(), labelsForHash, currDataObj, subFormViewDef.getRows());
                            viewBldObj.closeSubView(cellSubView);
                        }

                    } else
                    {
                        log.error("buildFormView - Could find subview's with ViewSet["+cellSubView.getViewSetName()+"] ViewName["+subViewName+"]");
                    }
                    // still have compToAdd = null;
                    colInx += 2;

                } else if (cell.getType() == FormCell.CellType.statusbar)
                {
                    compToAdd      = new JStatusBar();
                    addControl     = true;
                    addToValidator = false;
                    
                } else if (cell.getType() == FormCell.CellType.rstable)
                {
                    FormCellSubView cellSubView = (FormCellSubView)cell;
                    
                    /*
                    String subViewName = cellSubView.getViewName();
                    
                    View subView = AppContextMgr.getInstance().getView(cellSubView.getViewSetName(), subViewName);
                    if (subView != null)
                    {
                        AltView altView = null;
                        for (AltView av : subView.getAltViews())
                        {
                            if (av.getViewDef().getType() == ViewDef.ViewType.formTable)
                            {
                                altView = av;
                                break;
                            }
                        }
                        int options = MultiView.VIEW_SWITCHER | (MultiView.isOptionOn(parent.getCreateOptions(), MultiView.IS_NEW_OBJECT) ? MultiView.IS_NEW_OBJECT : 0);
                        Viewable viewable = ViewFactory.createFormView(parent, subView, altView.getName(), null, options);
                        viewBldObj.addSubView(cellSubView, multiView, colInx, rowInx, cellSubView.getColspan(), 1);
                        viewBldObj.closeSubView(cellSubView);
                        curMaxRow = rowInx;

                    }*/
                    
                    

                    String subViewName = cellSubView.getViewName();

                    View subView = AppContextMgr.getInstance().getView(cellSubView.getViewSetName(), subViewName);
                    if (subView != null)
                    {
                        if (parent != null)
                        {
                            int options = MultiView.VIEW_SWITCHER
                                    | (MultiView.isOptionOn(parent.getCreateOptions(), MultiView.IS_NEW_OBJECT) ? MultiView.IS_NEW_OBJECT : 0);

                            
                            AltView altView = null;
                            for (AltView av : subView.getAltViews())
                            {
                                if (av.getViewDef().getType() == ViewDef.ViewType.formTable)
                                {
                                    altView = av;
                                    break;
                                }
                            }
                            MultiView multiView = new MultiView(parent, cellSubView.getName(), subView, altView, parent.getCreateWithMode(), options);
                            parent.addChild(multiView);

                            viewBldObj.addSubView(cellSubView, multiView, colInx, rowInx, cellSubView.getColspan(), 1);
                            viewBldObj.closeSubView(cellSubView);
                            curMaxRow = rowInx;

                        }
                        else
                        {
                            log.error("buildFormView - parent is NULL for subview [" + subViewName + "]");
                        }

                    }
                    else
                    {
                        log.error("buildFormView - Could find subview's with ViewSet[" + cellSubView.getViewSetName()
                                + "] ViewName[" + subViewName + "]");
                    }
                    
                    // still have compToAdd = null;
                    colInx += 2;
                    
                } else if (cell.getType() == FormCell.CellType.panel)
                {
                    FormCellPanel           cellPanel = (FormCellPanel)cell;
                    PanelViewable.PanelType panelType = PanelViewable.getType(cellPanel.getPanelType());
                    
                    if (panelType == PanelViewable.PanelType.Panel)
                    {
                        PanelViewable panelViewable = new PanelViewable(cellPanel);

                        processRows(parent, formViewDef, validator, panelViewable, mode, labelsForHash, currDataObj, cellPanel.getRows());

                        compToAdd = panelViewable;

                    } else if (panelType == PanelViewable.PanelType.ButtonBar)
                    {
                        compToAdd = PanelViewable.buildButtonBar(processRows(viewBldObj, cellPanel.getRows()));
                        
                    } else
                    {
                        throw new RuntimeException("Panel Type is not implemented.");
                    }

                    addControl     = false;
                    addToValidator = false;

                }

                if (compToAdd != null)
                {
                    //System.out.println(colInx+"  "+rowInx+"  "+colspan+"  "+rowspan+"  "+compToAdd.getClass().toString());
                    viewBldObj.addControlToUI(compToAdd, colInx, rowInx, colspan, rowspan);

                    if (addControl)
                    {
                        viewBldObj.registerControl(cell, compToReg == null ? compToAdd : compToReg);
                    }
                    
                    curMaxRow = Math.max(curMaxRow, rowspan+rowInx);


                    if (validator != null && addToValidator)
                    {

                        validator.addUIComp(cell.getId(), compToReg == null ? compToAdd : compToReg);
                    }
                    colInx += colspan + 1;
                 }

            }
            rowInx += 2;
        }


    }

    /**
     * Processes the rows for a button bar.
     * @param viewBldObj formViewObj
     * @param formRows formRows
     * @return the array of buttons
     */
    protected JButton[] processRows(final ViewBuilderIFace viewBldObj,
                                    final List<FormRow>    formRows)
    {
        List<JButton> btns = new ArrayList<JButton>();

        for (FormRow row : formRows)
        {
            for (FormCell cell : row.getCells())
            {
                if (cell.getType() == FormCell.CellType.command)
                {
                    FormCellCommand cellCmd = (FormCellCommand)cell;
                    JButton btn  = new JButton(cellCmd.getLabel());
                    if (cellCmd.getCommandType().length() > 0)
                    {
                        btn.addActionListener(new CommandActionWrapper(new CommandAction(cellCmd.getCommandType(), cellCmd.getAction(), "")));
                    }
                    viewBldObj.registerControl(cell, btn);
                    btns.add(btn);
                }
            }
        }

        JButton[] btnsArray = new JButton[btns.size()];
        int i = 0;
        for (JButton b : btns)
        {
            btnsArray[i++] = b;
        }
        btns.clear();
        return btnsArray;

    }



    /**
     * Creates a Form.
     * @param view view the view definition
     * @param altView the altView to use (if null, then it uses the default ViewDef)
     * @param parentView the MultiView parent (this may be null)
     * @param options the options needed for creating the form
     * @return the form
     */
    public FormViewObj buildFormViewable(final View        view,
                                         final AltView     altView,
                                         final MultiView   parentView,
                                         final int         options)
    {
        try
        {
            FormViewDef formViewDef = (FormViewDef)altView.getViewDef();

            Hashtable<String, JLabel> labelsForHash = new Hashtable<String, JLabel>();
            
            ValidatedJPanel validatedPanel = null;
            FormValidator   validator      = null;
            if (altView.isValidated())
            {
                validatedPanel = new ValidatedJPanel();
                validator      = validatedPanel.getFormValidator();
                validator.setDataChangeNotification(true);
            }

            FormViewObj formViewObj = new FormViewObj(view, altView, parentView, validator, options);

            Object currDataObj = formViewObj.getCurrentDataObj();

            processRows(parentView, formViewDef, validator, formViewObj, altView.getMode(), labelsForHash, currDataObj, formViewDef.getRows());


            if (validatedPanel != null)
            {
                validatedPanel.addPanel(formViewObj.getPanel());

                // Here we add all the components whether they are used or not
                // XXX possible optimization is to only load the ones being used (although I am not sure how we will know that)
                Map<String, Component> mapping = formViewObj.getControlMapping();
                for (String id : mapping.keySet())
                {
                    validatedPanel.addValidationComp(id, mapping.get(id));
                }
                Map<String, String> enableRules = formViewDef.getEnableRules();

                // Load up validation Rules
                FormValidator fv = validatedPanel.getFormValidator();
                formViewObj.setValidator(fv);
                
                fv.setName(formViewDef.getName()); // For Debugging

                for (String id : enableRules.keySet())
                {
                    fv.addEnableRule(id, enableRules.get(id));
                }

                // Load up labels and associate them with their component
                for (String idFor : labelsForHash.keySet())
                {
                    fv.addUILabel(idFor, labelsForHash.get(idFor));
                }

                formViewObj.setFormComp(validatedPanel);
                
            } else
            {
                formViewObj.setFormComp(formViewObj.getPanel());
            }

            return formViewObj;

        } catch (Exception e)
        {
            log.error("buildPanel - Outer Name["+altView.getName()+"]");
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Creates a Table View.
     * @param view view the view definition
     * @param altView the altView to use (if null, then it uses the default ViewDef)
     * @param parentView the MultiView parent (this may be null)
     * @param options the options needed for creating the form
     * @return the form
     */
    public TableViewObj buildTableViewable(final View        view,
                                           final AltView     altView,
                                           final MultiView   parentView,
                                           final int         options)
    {
        try
        {
            ViewDef viewDef = altView.getViewDef();
            
            // Special situation where we create a table from a Form Definition
            if (viewDef instanceof FormViewDef)
            {
                FormViewDef               formViewDef   = (FormViewDef)viewDef;  
                Hashtable<String, JLabel> labelsForHash = new Hashtable<String, JLabel>();
                TableViewObj              tableViewObj  = new TableViewObj(view, altView, parentView, null, options);

                processRows(parentView, formViewDef, null, tableViewObj, altView.getMode(), labelsForHash, null, formViewDef.getRows());
                return tableViewObj;
                
            } else
            {
                FormViewDef formViewDef = (FormViewDef)altView.getViewDef();
                
                Hashtable<String, JLabel> labelsForHash = new Hashtable<String, JLabel>();
                
                /*
                ValidatedJPanel validatedPanel = null;
                FormValidator   validator      = null;
                if (altView.isValidated())
                {
                    validatedPanel = new ValidatedJPanel();
                    validator      = validatedPanel.getFormValidator();
                    validator.setDataChangeNotification(true);
                }
                */
                
                TableViewObj tableViewObj = new TableViewObj(view, altView, parentView, null, options);
    
                //Object currDataObj = tableViewObj.getCurrentDataObj();
    
                processRows(parentView, formViewDef, null, tableViewObj, altView.getMode(), labelsForHash, null, formViewDef.getRows());

            /*
            if (validatedPanel != null)
            {
                validatedPanel.addPanel(formViewObj.getPanel());

                // Here we add all the components whether they are used or not
                // XXX possible optimization is to only load the ones being used (although I am not sure how we will know that)
                Map<String, Component> mapping = formViewObj.getControlMapping();
                for (String id : mapping.keySet())
                {
                    validatedPanel.addValidationComp(id, mapping.get(id));
                }
                Map<String, String> enableRules = formViewDef.getEnableRules();

                // Load up validation Rules
                FormValidator fv = validatedPanel.getFormValidator();
                formViewObj.setValidator(fv);
                
                fv.setName(formViewDef.getName()); // For Debugging

                for (String id : enableRules.keySet())
                {
                    fv.addEnableRule(id, enableRules.get(id));
                }

                // Load up labels and associate them with there component
                for (String idFor : labelsForHash.keySet())
                {
                    fv.addUILabel(idFor, labelsForHash.get(idFor));
                }

                formViewObj.setFormComp(validatedPanel);
                
            } else
            {
                formViewObj.setFormComp(formViewObj.getPanel());
            }
*/
            return tableViewObj;

            }

        } catch (Exception e)
        {
            log.error("buildPanel - Outer Name["+altView.getName()+"]");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Creates a FormViewObj.
     * 
     * @param multiView
     *            the parent multiView
     * @param view
     *            the definition of the form view to be created
     * @param altName
     *            the name of the altView to be used (can be null - then it defaults to the default
     *            AltView)
     * @param data
     *            the data to be set into the form
     * @param options
     *            the options needed for creating the form
     * @return a new FormViewObj
     */
    public static Viewable createFormView(final MultiView multiView, 
                                          final View      view, 
                                          final String    altName, 
                                          final Object    data,
                                          final int       options)
    {
        if (viewFieldColor == null)
        {
            viewFieldColor = AppPrefsCache.getColorWrapper("ui", "formatting", "viewfieldcolor");
        }

        AltView altView = view.getAltView(altName);

        if (altView != null)
        {
            Viewable viewable = instance.buildViewable(view, altView, multiView, options);
            if (viewable != null)
            {
                if (data != null)
                {
                    viewable.setDataObj(data);
                    viewable.setDataIntoUI();
                } else
                {
                    throw new RuntimeException("Form could be created! ["+view.getName()+"]["+altView.getName()+"]");
                }
                return viewable;
            }
        }
        return null;
    }

    //-----------------------------------------------------------
    // Inner Class
    //-----------------------------------------------------------
    
    class BrowserLauncherBtn extends JButton implements GetSetValueIFace
    {
        protected String url     = null;
        protected Object dataObj = null;
        protected BrowserLauncherAction action = null;
        
        public BrowserLauncherBtn(final String text)
        {
            super(text);
            setEnabled(false);
        }
        
        /* (non-Javadoc)
         * @see edu.ku.brc.af.ui.GetSetValueIFace#setValue(java.lang.Object, java.lang.String)
         */
        public void setValue(Object value, String defaultValue)
        {
            if (value != null)
            {
                url = value.toString();
                dataObj = value;
                
                if (action != null)
                {
                    this.removeActionListener(action);
                }
                
                if (StringUtils.isNotEmpty(url) && url.startsWith("http"))
                {
                    action = new BrowserLauncherAction(url);
                    addActionListener(action);
                    setEnabled(true);
                } else
                {
                    setEnabled(false);
                }
            } else
            {
                setEnabled(false);
            }
        }
        
        /* (non-Javadoc)
         * @see edu.ku.brc.af.ui.GetSetValueIFace#getValue()
         */
        public Object getValue()
        {
            return dataObj;
        }
    }
    
    class BrowserLauncherAction implements ActionListener
    {
        protected String url;
        
        public BrowserLauncherAction(final String url)
        {
            this.url = url;
        }
        
        public void actionPerformed(ActionEvent ae)
        {
            BrowserLauncher.openURL(url);
        }
    }
}
