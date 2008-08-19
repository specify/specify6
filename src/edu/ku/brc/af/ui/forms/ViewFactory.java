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
package edu.ku.brc.af.ui.forms;

import static edu.ku.brc.af.ui.forms.validation.UIValidator.parseValidationType;
import static edu.ku.brc.ui.UIHelper.createButton;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.createProgressBar;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.split;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Insets;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.auth.PermissionSettings;
import edu.ku.brc.af.auth.SecurityMgr;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.TaskMgr;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableChildIFace;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.af.ui.BrowseBtnPanel;
import edu.ku.brc.af.ui.db.PickListDBAdapterFactory;
import edu.ku.brc.af.ui.db.PickListDBAdapterIFace;
import edu.ku.brc.af.ui.db.TextFieldFromPickListTable;
import edu.ku.brc.af.ui.db.TextFieldWithInfo;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.af.ui.forms.persist.AltViewIFace;
import edu.ku.brc.af.ui.forms.persist.FormCellCommand;
import edu.ku.brc.af.ui.forms.persist.FormCellField;
import edu.ku.brc.af.ui.forms.persist.FormCellFieldIFace;
import edu.ku.brc.af.ui.forms.persist.FormCellIFace;
import edu.ku.brc.af.ui.forms.persist.FormCellLabel;
import edu.ku.brc.af.ui.forms.persist.FormCellPanel;
import edu.ku.brc.af.ui.forms.persist.FormCellSeparatorIFace;
import edu.ku.brc.af.ui.forms.persist.FormCellSubView;
import edu.ku.brc.af.ui.forms.persist.FormRowIFace;
import edu.ku.brc.af.ui.forms.persist.FormViewDef;
import edu.ku.brc.af.ui.forms.persist.FormViewDefIFace;
import edu.ku.brc.af.ui.forms.persist.ViewDefIFace;
import edu.ku.brc.af.ui.forms.persist.ViewIFace;
import edu.ku.brc.af.ui.forms.validation.DataChangeNotifier;
import edu.ku.brc.af.ui.forms.validation.FormValidator;
import edu.ku.brc.af.ui.forms.validation.TypeSearchForQueryFactory;
import edu.ku.brc.af.ui.forms.validation.UIValidatable;
import edu.ku.brc.af.ui.forms.validation.UIValidator;
import edu.ku.brc.af.ui.forms.validation.ValBrowseBtnPanel;
import edu.ku.brc.af.ui.forms.validation.ValCheckBox;
import edu.ku.brc.af.ui.forms.validation.ValComboBox;
import edu.ku.brc.af.ui.forms.validation.ValComboBoxFromQuery;
import edu.ku.brc.af.ui.forms.validation.ValFormattedTextField;
import edu.ku.brc.af.ui.forms.validation.ValFormattedTextFieldSingle;
import edu.ku.brc.af.ui.forms.validation.ValListBox;
import edu.ku.brc.af.ui.forms.validation.ValPasswordField;
import edu.ku.brc.af.ui.forms.validation.ValSpinner;
import edu.ku.brc.af.ui.forms.validation.ValTextArea;
import edu.ku.brc.af.ui.forms.validation.ValTextField;
import edu.ku.brc.af.ui.forms.validation.ValidatedJPanel;
import edu.ku.brc.af.ui.weblink.WebLinkButton;
import edu.ku.brc.exceptions.ConfigurationException;
import edu.ku.brc.ui.ColorChooser;
import edu.ku.brc.ui.ColorWrapper;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandActionWrapper;
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.ImageDisplay;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIPluginable;
import edu.ku.brc.ui.UIRegistry;

/**
 * Creates FormViewObj object that implement the Viewable interface.
 *
 * @code_status Complete
 *
 * @author rods
 *
 */
public class ViewFactory
{
    // Statics
    private static final Logger log = Logger.getLogger(ViewFactory.class);
    private static final ViewFactory  instance = new ViewFactory();
    private static final String LF = "\\" + "n";
    
    // Data Members
    protected static ColorWrapper     viewFieldColor = null;
    protected static boolean          doFixLabels    = true;

    // transient - is valid only during a build process
    protected MultiView               rootMultiView    = null;
    
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
     * @param doFixLabels the doFixLabels to set
     */
    public static void setDoFixLabels(boolean doFixLabels)
    {
        ViewFactory.doFixLabels = doFixLabels;
    }

    /**
     * Creates a panel with the "..." icon.
     * @param comp the component to put into the panel
     */
    public JPanel createIconPanel(final JComponent comp)
    {
        JPanel  panel = new JPanel(new BorderLayout());
        JButton btn   = createButton("...");
        panel.add(btn, BorderLayout.WEST);
        panel.add(comp, BorderLayout.EAST);
        return panel;
    }

    /**
     * Returns a ViewDef Obj with the form UI built.
     * @param view the view definition
     * @param altView which AltViewIFace to build
     * @param parentView the MultiViw that this view/form will be parented to
     * @param options the options needed for creating the form
     * @return a Viewable Obj with the form UI built
     */
    public Viewable buildViewable(final ViewIFace    view, 
                                  final AltViewIFace altView, 
                                  final MultiView    parentView,
                                  final int          options,
                                  final Color        bgColor)
    {
        if (viewFieldColor == null)
        {
            viewFieldColor = AppPrefsCache.getColorWrapper("ui", "formatting", "viewfieldcolor");
        }

        ViewDefIFace viewDef = altView.getViewDef();

        if (viewDef == null) return null;

        this.rootMultiView =  parentView;

        if (viewDef.getType() == ViewDefIFace.ViewType.form)
        {
            Viewable viewable = buildFormViewable(view, altView, parentView, options, bgColor);
            this.rootMultiView =  null;
            return viewable;

        } else if (viewDef.getType() == ViewDefIFace.ViewType.table ||
                   viewDef.getType() == ViewDefIFace.ViewType.formtable)
        {
            Viewable viewable = buildTableViewable(view, altView, parentView, options, bgColor);
            this.rootMultiView =  null;
            return viewable;

        } else if (viewDef.getType() == ViewDefIFace.ViewType.field)
        {
            this.rootMultiView =  null;
            return null;

        }
        else if (viewDef.getType() == ViewDefIFace.ViewType.iconview)
        {
            return new IconViewObj(view, altView, parentView, options);
        }
        else if (viewDef.getType() == ViewDefIFace.ViewType.rstable)
        {
            return buildRecordSetTableViewable(view, altView, parentView, options, bgColor);
                
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
    public static JTextField createTextField(final FormValidator validator,
                                             final FormCellField cellField,
                                             @SuppressWarnings("unused")
                                             final DBFieldInfo   fieldInfo,
                                             final boolean       isRequired,
                                             final PickListDBAdapterIFace adapter)
    {
        String validationRule = cellField.getValidationRule();
       
        JTextField txtField;
        if (validator != null && (isRequired || isNotEmpty(validationRule) || cellField.isChangeListenerOnly()))
        {
            ValTextField textField = new ValTextField(cellField.getTxtCols(), adapter);
            textField.setRequired(isRequired);

            validator.hookupTextField(textField,
                                      cellField.getIdent(),//+"_text",
                                      isRequired,
                                      parseValidationType(cellField.getValidationType()),
                                      cellField.getValidationRule(),
                                      cellField.isChangeListenerOnly());

            txtField = textField;
            textField.setEditable(!cellField.isReadOnly());

        } else if (adapter != null)
        {
            ValTextField textField = new ValTextField(cellField.getTxtCols(), adapter);

            txtField = textField;
            textField.setEditable(false);

        } else
        {
            txtField = new ValTextField(cellField.getTxtCols());
        }
        return txtField;
    }

    /**
     * Creates a ValPasswordField.
     * @param validator a validator to hook the control up to (may be null)
     * @param cellField the definition of the cell for this control
     * @param isRequired whether the field is required or not
     * @return a ValPasswordField
     */
    public static JTextField createPasswordField(final FormValidator validator,
                                                 final FormCellField cellField,
                                                 final boolean isRequired)
    {
        String validationRule = cellField.getValidationRule();
        JTextField txt;

        if (validator != null && (isRequired || isNotEmpty(validationRule) || cellField.isChangeListenerOnly()))
        {
            ValPasswordField textField = new ValPasswordField(cellField.getTxtCols());
            textField.setRequired(isRequired);
            textField.setEncrypted(cellField.isEncrypted());

            validator.hookupTextField(textField,
                                      cellField.getIdent(),
                                      isRequired,
                                      parseValidationType(cellField.getValidationType()),
                                      validationRule,
                                      cellField.isChangeListenerOnly());

           txt = textField;

        } else
        {
            txt = new ValPasswordField(cellField.getTxtCols());
        }

        return txt;
    }


    /**
     * Creates a ValFormattedTextField.
     * @param validator a validator to hook the control up to (may be null)
     * @param cellField the definition of the cell for this control
     * @param isViewOnly whether it is in view mode
     * @param isRequired whether the field is required or not
     * @param allEditOK indicates that all the fields should be editable (event the auto-numbered field)
     * @return ValFormattedTextField
     */
    public static JComponent createFormattedTextField(final FormValidator validator,
                                                      final FormCellField cellField,
                                                      final String        uiFormatterName,
                                                      final boolean       isViewOnly,
                                                      final boolean       isRequired,
                                                      final boolean       allEditOK)
    {
        //log.debug(cellField.getName()+"  "+cellField.getUIFieldFormatter());

        boolean isPartialOK         = cellField.getPropertyAsBoolean("ispartial", false);
        boolean isFromUIFmtOverride = cellField.getPropertyAsBoolean("fromuifmt", false);
        Integer suggestedNumCols    = cellField.getPropertyAsInteger("cols", null);
        
        // Because it is formatted we ALWAYS validate it when there is a validator
        if (validator != null)
        {
            // deliberately ignore "cellField.isChangeListenerOnly()"
            // pass in false instead
            // THis is the OLD way before the UIFieldFormatter was moved into the DBFieldInfo and also the CellField
            UIFieldFormatterIFace formatter = UIFieldFormatterMgr.getInstance().getFormatter(uiFormatterName);
            if (formatter == null)
            {
                String msg = "Field["+cellField.getName()+ "] is missing formatter by name ["+uiFormatterName+"]";
                UIRegistry.showError(msg);
                throw new RuntimeException(msg);
            }
            
            if (formatter.isDate() || formatter.isNumeric())
            {
                ValFormattedTextFieldSingle textField = new ValFormattedTextFieldSingle(uiFormatterName, 
                                                                                       isViewOnly, 
                                                                                       isPartialOK, 
                                                                                       suggestedNumCols);
                textField.setRequired(isRequired);
                
                validator.hookupTextField(textField,
                                          cellField.getIdent(),
                                          isRequired,
                                          UIValidator.Type.Changed,  
                                          cellField.getValidationRule(), 
                                          false);
                
                if (isViewOnly)
                {
                    changeTextFieldUIForDisplay(textField, cellField.getPropertyAsBoolean("transparent", false));
                } else
                {
                    textField.setEditable(!cellField.isReadOnly());
                }
                
                textField.setFromUIFmtOverride(isFromUIFmtOverride);
                return textField;
            }
            
            ValFormattedTextField textField = new ValFormattedTextField(formatter, isViewOnly, allEditOK);
            textField.setPartialOK(isPartialOK);
            textField.setRequired(isRequired);
            textField.setFromUIFmtOverride(isFromUIFmtOverride);

            DataChangeNotifier dcn = validator.hookupComponent(textField,
                                                               cellField.getIdent(),
                                                               UIValidator.Type.Changed,  
                                                               cellField.getValidationRule(), 
                                                               false);
            if (cellField.isRequired())
            {
                textField.addDocumentListener(dcn);
            }

            textField.setChangeListener(dcn);
            return textField;
 
        }
        
        if (isViewOnly)
        {
            ValFormattedTextFieldSingle vtfs = new ValFormattedTextFieldSingle(uiFormatterName, 
                                                                               isViewOnly, 
                                                                               false, 
                                                                               suggestedNumCols);
            changeTextFieldUIForDisplay(vtfs, cellField.getPropertyAsBoolean("transparent", false));
            return vtfs;
        }
        // else
        ValFormattedTextField vtf = new ValFormattedTextField(uiFormatterName, isViewOnly, allEditOK);
        vtf.setEnabled(!cellField.isReadOnly());
        return vtf;
    }

    /**
     * Creates a ValTextArea.
     * @param validator a validator to hook the control up to (may be null)
     * @param cellField the definition of the cell for this control
     * @return ValTextArea
     */
    public static JTextArea createTextArea(final FormValidator validator,
                                           final FormCellField cellField)
    {
        ValTextArea textArea = new ValTextArea("", cellField.getTxtRows(), cellField.getTxtCols());
        if (validator != null)
        {
            UIValidator.Type type = parseValidationType(cellField.getValidationType());
            DataChangeNotifier dcn = validator.hookupComponent(textArea, cellField.getIdent(), type, null, true);
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
     * @param isRequired whether the field is required or not
     * @return ValListBox
     */
    public static JList createList(final FormValidator validator,
                                   final FormCellField cellField,
                                   final boolean isRequired)
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
        if (validator != null && (isRequired || isNotEmpty(cellField.getValidationRule())))
        {
            DataChangeNotifier dcn = validator.hookupComponent(valList, cellField.getIdent(), parseValidationType(cellField.getValidationType()), cellField.getValidationRule(), false);
            valList.getModel().addListDataListener(dcn);
            valList.addFocusListener(dcn);
        }
        valList.setRequired(isRequired);
        
        valList.setVisibleRowCount(cellField.getPropertyAsInt("rows", 15));
        
        return valList;
    }


    /**
     * Creates a ValComboBoxFromQuery.
     * @param validator a validator to hook the control up to (may be null)
     * @param cellField the definition of the cell for this control
     * @param isRequired whether the field is required or not
     * @return ValComboBoxFromQuery
     */
    public static ValComboBoxFromQuery createQueryComboBox(final FormValidator validator,
                                                           final FormCellField cellField,
                                                           final boolean       isRequired)
    {
        //String cbxName = cellField.getInitialize();
        String cbxName = cellField.getProperty("name");
        if (isNotEmpty(cbxName))
        {
            int btnOpts = 0;
            btnOpts |= cellField.getPropertyAsBoolean("editbtn", true) ? ValComboBoxFromQuery.CREATE_EDIT_BTN : 0;
            btnOpts |= cellField.getPropertyAsBoolean("newbtn", true) ? ValComboBoxFromQuery.CREATE_NEW_BTN : 0;
            btnOpts |= cellField.getPropertyAsBoolean("searchbtn", true) ? ValComboBoxFromQuery.CREATE_SEARCH_BTN : 0;
            
            ValComboBoxFromQuery cbx = TypeSearchForQueryFactory.createValComboBoxFromQuery(cbxName, btnOpts, cellField.getFormatName());
            cbx.setRequired(isRequired);
            cbx.setSearchDlgName(cellField.getProperty("searchdlg"));
            cbx.setDisplayDlgName(cellField.getProperty("displaydlg"));
            
            if (validator != null)// && (cellField.isRequired() || isNotEmpty(cellField.getValidationRule())))
            {
                DataChangeNotifier dcn = validator.hookupComponent(cbx, cellField.getIdent(), parseValidationType(cellField.getValidationType()), cellField.getValidationRule(), false);
                cbx.addListSelectionListener(dcn);

                //if (dcn.getValidationType() == UIValidator.Type.Focus) // returns None when no Validator
                //{
                    cbx.addFocusListener(dcn);
                //}
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
     * @param isRequired whether the field is required or not
     * @return ValComboBox
     */
    public static ValComboBox createValComboBox(final FormValidator validator,
                                                final FormCellField cellField,
                                                final PickListDBAdapterIFace adapter,
                                                final boolean isRequired)
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
            if (isNotEmpty(data))
            {
                initArray = split(data, ",");
                for (int i=0;i<initArray.length;i++)
                {
                    initArray[i] = initArray[i].trim();
                }
            }

            cbx = initArray == null || initArray.length == 0 ? new ValComboBox(makeEditable) : new ValComboBox(initArray, makeEditable);
        }
        cbx.setRequired(isRequired);
        
        if (validator != null && (isRequired || cellField.isChangeListenerOnly() || isNotEmpty(cellField.getValidationRule())))
        {
            DataChangeNotifier dcn = validator.hookupComponent(cbx, 
                                                               cellField.getIdent(), 
                                                               parseValidationType(cellField.getValidationType()), 
                                                               cellField.getValidationRule(), 
                                                               cellField.isChangeListenerOnly());
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
     * Makes adjusts to the border and the colors to make it "flat" for display mode.
     * @param textField the text field to be flattened
     * @param isTransparent make the background transparent instead of using the viewFieldColor
     */
    public static void changeTextFieldUIForDisplay(final JTextField textField, final boolean isTransparent)
    {
        changeTextFieldUIForDisplay(textField, null, isTransparent);
    }
    
    /**
     * Makes adjusts to the border and the colors to make it "flat" for display mode.
     * @param textField the text field to be flattened
     * @param isTransparent make the background transparent instead of using the viewFieldColor
     */
    public static void changeTextFieldUIForDisplay(final JTextField textField, final Color borderColor, final boolean isTransparent)
    {
        Insets insets = textField.getBorder().getBorderInsets(textField);
        if (borderColor != null)
        {
            textField.setBorder(BorderFactory.createMatteBorder(Math.min(insets.top, 3), Math.min(insets.left, 3), 
                                                                Math.min(insets.bottom, 3), Math.min(insets.right, 3), borderColor));
        } else
        {
            textField.setBorder(BorderFactory.createEmptyBorder(Math.min(insets.top, 3), Math.min(insets.left, 3), 
                                                                Math.min(insets.bottom, 3), Math.min(insets.right, 3)));
        }
        textField.setForeground(Color.BLACK);
        textField.setEditable(false);
        textField.setFocusable(false);
        
        if (isTransparent)
        {
            textField.setOpaque(false);
            textField.setBackground(null);
            
        } else if (viewFieldColor != null)
        {
            textField.setBackground(viewFieldColor.getColor());
        }
    }
    
    /**
     * @param textField
     * @param border
     * @param fgColor
     * @param bgColor
     * @param isOpaque
     */
    public static void changeTextFieldUIForEdit(final JTextField textField, 
                                                final Border     border, 
                                                final Color      fgColor,
                                                final Color      bgColor,
                                                final boolean    isOpaque)
    {
        textField.setBorder(border);
        textField.setForeground(fgColor);
        textField.setEditable(true);
        textField.setFocusable(true);
        textField.setOpaque(isOpaque);
        textField.setBackground(bgColor);
    }
    
    /**
     * Creates a JTextArea for display purposes only.
     * @param cellField FormCellField info
     * @return the control
     */
    public static JScrollPane createDisplayTextArea(final FormCellFieldIFace cellField)
    {
        JTextArea ta = new JTextArea(cellField.getTxtRows(), cellField.getTxtCols());
        // ta.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
        Insets insets = ta.getBorder().getBorderInsets(ta);
        ta.setBorder(BorderFactory.createEmptyBorder(insets.top, insets.left, insets.bottom, insets.bottom));
        ta.setForeground(Color.BLACK);
        ta.setEditable(false);
        ta.setBackground(viewFieldColor.getColor());
        
        JScrollPane scrollPane = new JScrollPane(ta);
        insets = scrollPane.getBorder().getBorderInsets(scrollPane);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(insets.top, insets.left, insets.bottom, insets.bottom));
        
        return scrollPane;
    }
    
    /**
     * Creates a TextFieldWithInfo.
     * @param cellField FormCellField info
     * @param parent the parent mulitview needed because of pop Dialogs (from the info btn)
     * @return the control
     */
    public static TextFieldWithInfo createTextFieldWithInfo(final FormCellField cellField,
                                                            final MultiView     parent)
    {
        TextFieldWithInfo textFieldInfo = null;
        String            txtName = cellField.getProperty("name");
        if (isNotEmpty(txtName))
        {
            textFieldInfo = TypeSearchForQueryFactory.getTextFieldWithInfo(txtName, cellField.getFormatName());
            if (textFieldInfo != null)
            {
                textFieldInfo.setMultiView(parent);
                textFieldInfo.setFrameTitle(cellField.getProperty("title"));
                JTextField textField = textFieldInfo.getTextField();
                textField.setColumns(cellField.getTxtCols());
                
                // Overrides the defined in the TableInfo
                String displayInfoDialogName = cellField.getProperty("displaydlg");
                if (StringUtils.isNotEmpty(displayInfoDialogName))
                {
                    textFieldInfo.setDisplayInfoDialogName(displayInfoDialogName);
                }
                
                changeTextFieldUIForDisplay(textField, false);
                
            } else
            {
                log.error("Could TypeSearchForQueryFactory.getTextFieldWithInfo("+txtName+")");
            }
        } else
        {
            throw new RuntimeException("textfieldinfo Name for textFieldWithInfo ["+txtName+"] is empty!");
        }
        return textFieldInfo;
    }
    
    /**
     * Creates an ImageDisplay control.
     * @param cellField FormCellField info
     * @param mode indicates whether in Edit or View mode
     * @param validator the validator
     * @return the control
     */
    public static ImageDisplay createImageDisplay(final FormCellField cellField,
                                                  final AltViewIFace.CreationMode mode,
                                                  final FormValidator validator)
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
        
        boolean imageInEdit = mode == AltViewIFace.CreationMode.EDIT;
        String editModeStr = cellField.getProperty("edit");
        if (isNotEmpty(editModeStr))
        {
            imageInEdit = editModeStr.toLowerCase().equals("true");
        }

        // create a new ImageDisplay
        // override the selectNewImage method to notify the parent MultiView when a change occurs
        ImageDisplay imgDisp = new ImageDisplay(w, h, imageInEdit, cellField.getPropertyAsBoolean("border", true));
        
        if (validator != null)
        {
            DataChangeNotifier dcn = validator.hookupComponent(imgDisp, cellField.getIdent(), parseValidationType(cellField.getValidationType()), cellField.getValidationRule(), false);
            imgDisp.addPropertyChangeListener("imageURL", dcn);

            if (dcn.getValidationType() == UIValidator.Type.Focus) // returns None when no Validator
            {
                imgDisp.addFocusListener(dcn);
            }
        }
        
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
     * @param isViewMode indicates whether in Edit or View mode
     * @param isRequired whether the field is required or not
     * @return the control
     */
    protected static UIPluginable createPlugin(final MultiView     parent,
                                               final FormValidator validator, 
                                               final FormCellField cellField,
                                               final boolean       isViewMode,
                                               final boolean       isRequired)
    {
        String pluginName = cellField.getProperty("name");
        if (StringUtils.isEmpty(pluginName))
        {
            throw new RuntimeException("Creating plugin and the name property was missing. ["+cellField.getName()+"]");
        }
        
        // We should refactor the plugin manager.
        Class<?> pluginClass = TaskMgr.getUIPluginClassForName(pluginName);
        if (pluginClass != null && UIPluginable.class.isAssignableFrom(pluginClass))
        {
            try
            {
                // instantiate the plugin object
                UIPluginable uiPlugin = pluginClass.asSubclass(UIPluginable.class).newInstance();
                
                Properties props = (Properties)cellField.getProperties().clone();
                
                System.out.println(cellField.getName());
                if (uiPlugin instanceof WebLinkButton)
                {
                    //if (StringUtils.isNotEmpty((String)props.get("weblink")))
                    {
                        DBTableInfo tInfo = DBTableIdMgr.getInstance().getByClassName(parent.getView().getClassName());
                        if (tInfo != null)
                        {
                            System.out.println(cellField.getName());
                            DBFieldInfo fInfo = tInfo.getFieldByName(cellField.getName());
                            if (fInfo != null)
                            {
                                System.out.println(fInfo.getFormatStr() + " weblink: "+fInfo.getWebLinkName());
                                if (StringUtils.isEmpty((String)props.get("weblink")) && StringUtils.isNotEmpty(fInfo.getWebLinkName()))
                                {
                                    props.put("weblink", fInfo.getWebLinkName());
                                }
                            }
                        }                        
                    }
                }

                // This needs to be done before the initialize.
                if (uiPlugin instanceof UIValidatable)
                {
                    ((UIValidatable)uiPlugin).setRequired(isRequired);
                }

                // initialize the plugin object
               
                props.put("parent", parent);
                uiPlugin.initialize(props, isViewMode);
                
                // get the UI component provided by the plugin object
                JComponent pluginUI = uiPlugin.getUIComponent();
                
                // check for another required interface (GetSetValueIFace)
                if (!(uiPlugin instanceof GetSetValueIFace))
                {
                    throw new RuntimeException("Plugin of class ["+pluginClass.getName()+"] doesn't implement the GetSetValueIFace!");
                }
                
                if (validator != null)
                {
                    DataChangeNotifier dcn = validator.hookupComponent(pluginUI, 
                                                                       cellField.getIdent(),
                                                                       parseValidationType(cellField.getValidationType()), 
                                                                       cellField.getValidationRule(), 
                                                                       false);
                    uiPlugin.setChangeListener(dcn);
                }

                return uiPlugin;
                
            } catch (Exception ex)
            {
               log.error(ex);
               ex.printStackTrace();
               throw new RuntimeException(ex);
            }
        }
        log.error("Couldn't find plugin by name["+pluginName+"]");
        return null;
    }
    
    //----------------------------------------------------------------------------------------------------------------
    //-- 
    //----------------------------------------------------------------------------------------------------------------
    class BuildInfoStruct
    {
        public Hashtable<CollapsableSeparator, String> collapseSepHash = null;
        
        public int        curMaxRow        = 0;
        public JComponent compToAdd        = null;
        public JComponent compToReg        = null;
        public boolean    doAddToValidator = true;
        public boolean    doRegControl     = true;
        public int        colInx           = 0;   
        public boolean    isRequired       = false;
        public boolean    isDerivedLabel   = false;
        
        public void clear()
        {
            curMaxRow       = 0;
            compToAdd       = null;
            compToReg       = null;
            doAddToValidator  = true;
            doRegControl      = true;
            colInx          = 0; 
            isRequired      = false;
            isDerivedLabel = false;
            
            if (collapseSepHash != null)
            {
                collapseSepHash.clear();
            }
        }
    }


    protected void createItem(final DBTableChildIFace           childInfo,
                              final MultiView                   parent,
                              final FormViewDefIFace            formViewDef,
                              final FormValidator               validator,
                              final ViewBuilderIFace            viewBldObj,
                              final AltViewIFace.CreationMode   mode,
                              final Hashtable<String, JLabel>   labelsForHash,
                              final Object                      currDataObj,
                              final FormCellIFace               cell,
                              final boolean                     isEditOnCreateOnly,
                              final int                         rowInx,
                              final BuildInfoStruct             bi)
    {
        bi.compToAdd      = null;
        bi.compToReg      = null;
        bi.doAddToValidator = true;
        bi.doRegControl     = true;
        
        if (isEditOnCreateOnly)
        {
            EditViewCompSwitcherPanel evcsp = new EditViewCompSwitcherPanel(cell);
            bi.compToAdd =  evcsp;
            bi.compToReg =  evcsp;
            
            if (validator != null)
            {
                //DataChangeNotifier dcn = validator.createDataChangeNotifer(cell.getIdent(), evcsp, null);
                DataChangeNotifier dcn = validator.hookupComponent(evcsp, cell.getIdent(), UIValidator.Type.Changed,  null, false);
                evcsp.setDataChangeNotifier(dcn);
            }
            
        } else if (cell.getType() == FormCellIFace.CellType.label)
        {
            FormCellLabel cellLabel = (FormCellLabel)cell;

            String lblStr = cellLabel.getLabel();
            if (cellLabel.isRecordObj())
            {
                JComponent riComp = viewBldObj.createRecordIndentifier(lblStr, cellLabel.getIcon());
                bi.compToAdd = riComp;
                
            } else
            {
                String  lStr     = "  ";
                int     align    = SwingConstants.RIGHT;
                boolean useColon = StringUtils.isNotEmpty(cellLabel.getLabelFor());
                
                if (lblStr.equals("##"))
                {
                    //lStr = "  ";
                    bi.isDerivedLabel = true;
                    cellLabel.setDerived(true);
                    
                } else
                {
                    String alignProp = cellLabel.getProperty("align");
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
                }
                
                if (lStr.indexOf(LF) > -1)
                {
                    lStr = "<html>" + StringUtils.replace(lStr, LF, "<br>") + "</html>";
                }
                JLabel lbl = createLabel(lStr, align);
                labelsForHash.put(cellLabel.getLabelFor(), lbl);
                bi.compToAdd =  lbl;
                viewBldObj.addLabel(cellLabel, lbl);
            }

            bi.doAddToValidator = false;
            bi.doRegControl     = false;


        } else if (cell.getType() == FormCellIFace.CellType.field)
        {
            FormCellField cellField = (FormCellField)cell;
            
            String fieldName = cellField.getName();
            if (childInfo == null && 
                fieldName != null && 
                fieldName.indexOf('.') == -1 &&
                !fieldName.equals("this")
                )
            {
               log.debug("No DBChild ["+cellField.getName()+"]"); 
            }
            
            bi.isRequired = cellField.isRequired() || (childInfo != null && childInfo.isRequired());
            
            DBFieldInfo fieldInfo = childInfo instanceof DBFieldInfo ? (DBFieldInfo)childInfo : null;
            if (fieldInfo != null && fieldInfo.isHidden())
            {
                UIRegistry.showLocalizedError("ViewFactory.FORM_FIELD_HIDDEN", cellField.getIdent(), cellField.getName(), formViewDef.getName());
            }
            
            FormCellField.FieldType uiType = cellField.getUiType();
            
            // Check to see if there is a PickList and get it if there is
            PickListDBAdapterIFace adapter = null;
            
            String pickListName = cellField.getPickListName();
            if (childInfo != null && StringUtils.isEmpty(pickListName) && fieldInfo != null)
            {
                pickListName = fieldInfo.getPickListName();
            }
            
            if (isNotEmpty(pickListName))
            {
                adapter = PickListDBAdapterFactory.getInstance().create(pickListName, false);
                
                if (adapter == null || adapter.getPickList() == null)
                {
                    throw new RuntimeException("PickList Adapter ["+pickListName+"] cannot be null!");
                }
            }
            
            /*if (uiType == FormCellFieldIFace.FieldType.text)
            {
                String weblink = cellField.getProperty("weblink");
                if (StringUtils.isNotEmpty(weblink))
                {
                    String name = cellField.getProperty("name");
                    if (StringUtils.isNotEmpty(name) && name.equals("WebLink"))
                    {
                        uiType
                    }
                }
            }*/

            // The Default Display for combox is dsptextfield, except when there is a TableBased PickList
            // At the time we set the display we don't want to go get the picklist to find out. So we do it
            // here after we have the picklist and actually set the change into the cellField
            // because it uses the value to determine whether to convert the value into a text string 
            // before setting it.
            if (mode == AltViewIFace.CreationMode.VIEW)
            {
                if (uiType == FormCellFieldIFace.FieldType.combobox && cellField.getDspUIType() != FormCellFieldIFace.FieldType.textpl)
                {
                    if (adapter != null)// && adapter.isTabledBased())
                    {
                        uiType = FormCellFieldIFace.FieldType.textpl;
                        cellField.setDspUIType(uiType);
                        
                    } else
                    {
                        uiType = cellField.getDspUIType();    
                    }
                } else
                {
                    uiType = cellField.getDspUIType();
                }
            } else if (uiType == FormCellField.FieldType.querycbx)
            {
                if (AppContextMgr.isSecurityOn())
                {
                    DBTableInfo tblInfo = DBTableIdMgr.getInstance().getByShortClassName(childInfo.getDataClass().getSimpleName());
                    if (tblInfo != null)
                    {
                        PermissionSettings perm = tblInfo.getPermissions();
                        if (perm != null)
                        {
                            PermissionSettings.dumpPermissions("QCBX: "+tblInfo.getShortClassName(), perm.getOptions());
                            if (perm.isViewOnly() || !perm.canView())
                            {
                                uiType = FormCellField.FieldType.textfieldinfo;
                            }
                        }
                    }
                }
            }
            
            String uiFormatName = cellField.getUIFieldFormatterName();
            if (mode == AltViewIFace.CreationMode.EDIT && 
                uiType == FormCellField.FieldType.text && 
                childInfo != null)
            {
                Class<?> cls = childInfo.getDataClass();
                if (cls == String.class)
                {
                	// check whether there's a formatter defined for this field in the schema
                    if (fieldInfo.getFormatter() != null)
                    {
                    	uiFormatName = fieldInfo.getFormatter().getName();
                        uiType =  FormCellField.FieldType.formattedtext;
                    }
                }
                else if (cls == Integer.class || 
                		 cls == Long.class || 
                		 cls == Byte.class || 
                		 cls == Double.class || 
                		 cls == Float.class || 
                		 cls == BigDecimal.class)
                {
                    //log.debug(cellField.getName()+"  is being changed to NUMERIC");
                    uiType =  FormCellField.FieldType.formattedtext;
                    uiFormatName = "Numeric" + cls.getSimpleName();
                }
            }

            // Create the UI Component
            
            switch (uiType)
            {
                case text:
                    
                    bi.compToAdd = createTextField(validator, cellField, fieldInfo, bi.isRequired, adapter);
                    bi.doAddToValidator = validator == null; // might already added to validator
                    break;
                
                case formattedtext:
                    bi.compToAdd = createFormattedTextField(validator, cellField, uiFormatName, mode == AltViewIFace.CreationMode.VIEW, bi.isRequired, cellField.getPropertyAsBoolean("alledit", false));
                    bi.doAddToValidator = validator == null; // might already added to validator
                    break;
                    
                case label:
                    JLabel label = createLabel("", SwingConstants.LEFT);
                    bi.compToAdd = label;
                    break;
                    
                case dsptextfield:
                    if (StringUtils.isEmpty(cellField.getPickListName()))
                    {
                        JTextField text = UIHelper.createTextField(cellField.getTxtCols());
                        changeTextFieldUIForDisplay(text, cellField.getPropertyAsBoolean("transparent", false));
                        bi.compToAdd = text;
                    } else
                    {
                        bi.compToAdd = createTextField(validator, cellField, fieldInfo, bi.isRequired, adapter);
                        bi.doAddToValidator = validator == null; // might already added to validator
                    }
                    break;

                case textfieldinfo:
                    bi.compToAdd = createTextFieldWithInfo(cellField, parent);
                    break;

                    
                case image:
                    bi.compToAdd = createImageDisplay(cellField, mode, validator);
                    bi.doAddToValidator = (validator != null);
                    break;

                
                case url:
                    BrowserLauncherBtn blb = new BrowserLauncherBtn(cellField.getProperty("title"));
                    bi.compToAdd        = blb;
                    bi.doAddToValidator = false;

                    break;
                
                case combobox:
                    bi.compToAdd = createValComboBox(validator, cellField, adapter, bi.isRequired);
                    bi.doAddToValidator = validator != null; // might already added to validator
                    break;
                    
                case checkbox:
                {
                    String lblStr = cellField.getLabel();
                    if (lblStr.equals("##"))
                    {
                        bi.isDerivedLabel = true;
                        cellField.setDerived(true);
                    }
                    ValCheckBox checkbox = new ValCheckBox(lblStr, 
                                                           bi.isRequired, 
                                                           cellField.isReadOnly() || mode == AltViewIFace.CreationMode.VIEW);
                    if (validator != null)
                    {
                        DataChangeNotifier dcn = validator.createDataChangeNotifer(cellField.getIdent(), checkbox, null);
                        checkbox.addChangeListener(dcn);
                    }
                    bi.compToAdd = checkbox;
                    break;
                }
                
                case spinner:
                {
                    String minStr = cellField.getProperty("min");
                    int    min    = StringUtils.isNotEmpty(minStr) ? Integer.parseInt(minStr) : 0;
                    
                    String maxStr = cellField.getProperty("max");
                    int    max    = StringUtils.isNotEmpty(maxStr) ? Integer.parseInt(maxStr) : 0; 
                    
                    ValSpinner spinner = new ValSpinner(min, max, bi.isRequired, 
                                                           cellField.isReadOnly() || mode == AltViewIFace.CreationMode.VIEW);
                    if (validator != null)
                    {
                        DataChangeNotifier dcn = validator.createDataChangeNotifer(cellField.getIdent(), spinner, null);
                        spinner.addChangeListener(dcn);
                    }
                    bi.compToAdd = spinner;
                    break;
                }                            
                 
                case password:
                    bi.compToAdd        = createPasswordField(validator, cellField, bi.isRequired);
                    bi.doAddToValidator = validator == null; // might already added to validator
                    break;
                
                case dsptextarea:
                    bi.compToAdd = createDisplayTextArea(cellField);
                    break;
                
                case textarea:
                {
                    JTextArea ta = createTextArea(validator, cellField);
                    JScrollPane scrollPane = new JScrollPane(ta);
                    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                    scrollPane.setVerticalScrollBarPolicy(UIHelper.isMacOS() ? ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS : ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
                    
                    bi.doAddToValidator = validator == null; // might already added to validator
                    bi.compToReg = ta;
                    bi.compToAdd = scrollPane;
                    break;
                }
                
                case browse:
                {
                    JTextField textField = createTextField(validator, cellField, null, bi.isRequired, null);
                    if (textField instanceof ValTextField)
                    {
                        ValBrowseBtnPanel bbp = new ValBrowseBtnPanel((ValTextField)textField, 
                                                                      cellField.getPropertyAsBoolean("dirsonly", false), 
                                                                      cellField.getPropertyAsBoolean("forinput", true));
                        bi.compToAdd = bbp;
                        
                    } else
                    {
                        BrowseBtnPanel bbp = new BrowseBtnPanel(textField, 
                                cellField.getPropertyAsBoolean("dirsonly", false), 
                                cellField.getPropertyAsBoolean("forinput", true));
                        bi.compToAdd = bbp;
                    }
                    break;
                }
                    
                case querycbx:
                {
                    ValComboBoxFromQuery cbx = createQueryComboBox(validator, cellField, bi.isRequired);
                    cbx.setMultiView(parent);
                    cbx.setFrameTitle(cellField.getProperty("title"));
                    
                    bi.compToAdd = cbx;
                    bi.doAddToValidator = validator == null; // might already added to validator
                    break;
                }
                
                case list:
                {
                    JList list = createList(validator, cellField, bi.isRequired);
                    
                    JScrollPane scrollPane = new JScrollPane(list);
                    scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                    scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

                    bi.doAddToValidator = validator == null;
                    bi.compToReg = list;
                    bi.compToAdd = scrollPane;
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
                    //setControlSize(colorChooser);
                    bi.compToAdd = colorChooser;

                    break;
                }
                
                case button:
                    JButton btn = createButton(cellField.getProperty("title"));
                    bi.compToAdd = btn;
                    break;
                    
                case progress:
                    bi.compToAdd = createProgressBar(0, 100);
                    break;
                
                case plugin:
                    UIPluginable uip = createPlugin(parent,
                                                validator, 
                                                cellField, 
                                                mode == AltViewIFace.CreationMode.VIEW, 
                                                bi.isRequired);
                    if (uip != null)
                    {
                        bi.compToAdd = uip.getUIComponent();
                        viewBldObj.registerPlugin(cell, uip);
                    } else
                    {
                        bi.compToAdd = new JPanel();
                        log.error("Couldn't create UIPlugin ["+cellField.getName()+"]");
                    }
                    bi.doRegControl = false;                        

                    break;

                case textpl:
                    JTextField txt = new TextFieldFromPickListTable(adapter);
                    changeTextFieldUIForDisplay(txt, cellField.getPropertyAsBoolean("transparent", false));
                    bi.compToAdd = txt;
                    break;
                
                default:
                    throw new RuntimeException("Don't recognize uitype=["+uiType+"]");
                
            } // switch

        } else if (cell.getType() == FormCellIFace.CellType.separator)
        {
            // still have compToAdd = null;
            FormCellSeparatorIFace fcs             = (FormCellSeparatorIFace)cell;
            String                 collapsableName = fcs.getCollapseCompName();
            
            String label = fcs.getLabel();
            if (StringUtils.isEmpty(label) || label.equals("##"))
            {
                String className = fcs.getProperty("forclass");
                if (StringUtils.isNotEmpty(className))
                {
                    DBTableInfo ti = DBTableIdMgr.getInstance().getByShortClassName(className);
                    if (ti != null)
                    {
                        label = ti.getTitle();
                    }
                }
            }
            Component sep = viewBldObj.createSeparator(label);
            if (isNotEmpty(collapsableName))
            {
                CollapsableSeparator collapseSep = new CollapsableSeparator(sep);
                if (bi.collapseSepHash == null)
                {
                    bi.collapseSepHash = new Hashtable<CollapsableSeparator, String>();
                }
                bi.collapseSepHash.put(collapseSep, collapsableName);
                sep = collapseSep;
                
            }
            bi.doRegControl     = cell.getName().length() > 0;
            bi.compToAdd        = (JComponent)sep;
            bi.doRegControl     = StringUtils.isNotEmpty(cell.getIdent());
            bi.doAddToValidator = false;
            
        } else if (cell.getType() == FormCellIFace.CellType.command)
        {
            FormCellCommand cellCmd = (FormCellCommand)cell;
            JButton btn  = createButton(cellCmd.getLabel());
            if (cellCmd.getCommandType().length() > 0)
            {
                btn.addActionListener(new CommandActionWrapper(new CommandAction(cellCmd.getCommandType(), cellCmd.getAction(), "")));
            }
            bi.doAddToValidator = false;
            bi.compToAdd = btn;

        } 
        else if (cell.getType() == FormCellIFace.CellType.iconview)
        {
            FormCellSubView cellSubView = (FormCellSubView)cell;

            String subViewName = cellSubView.getViewName();

            ViewIFace subView = AppContextMgr.getInstance().getView(cellSubView.getViewSetName(), subViewName);
            if (subView != null)
            {
                if (parent != null)
                {
                    int options = MultiView.VIEW_SWITCHER
                            | (MultiView.isOptionOn(parent.getCreateOptions(), MultiView.IS_NEW_OBJECT) ? MultiView.IS_NEW_OBJECT
                                    : MultiView.NO_OPTIONS);

                    MultiView multiView = new MultiView(parent, 
                                                        cellSubView.getName(), 
                                                        subView, 
                                                        parent.getCreateWithMode(), 
                                                        options, null);
                    parent.addChildMV(multiView);
                    multiView.setClassToCreate(getClassToCreate(parent, cell));

                    log.debug("["+cell.getType()+"] ["+cell.getName()+"] col: "+bi.colInx+" row: "+rowInx+" colspan: "+cell.getColspan()+" rowspan: "+cell.getRowspan());
                    viewBldObj.addSubView(cellSubView, multiView, bi.colInx, rowInx, cellSubView.getColspan(), 1);
                    viewBldObj.closeSubView(cellSubView);
                    bi.curMaxRow = rowInx;
                    bi.colInx += cell.getColspan() + 1;
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
            bi.colInx += 2;
            
        } else if (cell.getType() == FormCellIFace.CellType.subview)
        {
            FormCellSubView cellSubView = (FormCellSubView)cell;
            String          subViewName = cellSubView.getViewName();
            ViewIFace       subView     = AppContextMgr.getInstance().getView(cellSubView.getViewSetName(), subViewName);

            if (subView != null)
            {
                // Check to see this view should be "flatten" meaning we are creating a grid from a form
                if (!viewBldObj.shouldFlatten())
                {
                    if (parent != null)
                    {
                        ViewIFace  parentView = parent.getView();
                        Properties props      = cellSubView.getProperties();
                        
                        boolean isSingle      = cellSubView.isSingleValueFromSet();
                        boolean isACollection = false;

                        try
                        {
                            Class<?> cls = Class.forName(parentView.getClassName());
                            Field    fld = getFieldFromDotNotation(cellSubView, cls);
                            if (fld != null)
                            {
                                isACollection = Collection.class.isAssignableFrom(fld.getType());
                            } else
                            {
                                log.error("Couldn't find field ["+cellSubView.getName()+"] in class ["+parentView.getClassName()+"]");
                            }
                        } catch (Exception ex)
                        {
                            log.error("Couldn't find field ["+cellSubView.getName()+"] in class ["+parentView.getClassName()+"]");
                        }
                        
                        boolean useNoScrollbars = UIHelper.getProperty(props, "noscrollbars", false);
                        
                        int options = (isACollection && !isSingle ? MultiView.RESULTSET_CONTROLLER : MultiView.IS_SINGLE_OBJ) | MultiView.VIEW_SWITCHER |
                        (MultiView.isOptionOn(parent.getCreateOptions(), MultiView.IS_NEW_OBJECT) ? MultiView.IS_NEW_OBJECT : MultiView.NO_OPTIONS) |
                        (mode == AltViewIFace.CreationMode.EDIT ? MultiView.IS_EDITTING : MultiView.NO_OPTIONS) |
                        (useNoScrollbars ? MultiView.NO_SCROLLBARS : MultiView.NO_OPTIONS);
                        
                        if (!(isACollection && !isSingle))
                        {
                            options &= ~MultiView.ADD_SEARCH_BTN;
                        }
          
                        //MultiView.printCreateOptions("_______________________________", parent.getCreateOptions());
                        //MultiView.printCreateOptions("_______________________________", options);
                        boolean useBtn = UIHelper.getProperty(props, "btn", false);
                        if (useBtn)
                        {
                            SubViewBtn.DATA_TYPE dataType;
                            if (isSingle)
                            {
                                dataType = SubViewBtn.DATA_TYPE.IS_SINGLESET_ITEM;
                                
                            } else if (isACollection)
                            {
                                dataType = SubViewBtn.DATA_TYPE.IS_SET;
                            } else
                            {
                                dataType = cellSubView.getName().equals("this") ? SubViewBtn.DATA_TYPE.IS_THIS : SubViewBtn.DATA_TYPE.IS_SET;
                            }
                            
                            SubViewBtn subViewBtn = new SubViewBtn(parent, cellSubView, subView, dataType, options, props, getClassToCreate(parent, cell), mode);
                            subViewBtn.setHelpContext(props.getProperty("hc", null));
                            
                            bi.doAddToValidator   = false;
                            bi.compToAdd          = subViewBtn;
                            
                            addControl(validator, viewBldObj, rowInx, cell, bi);
                            
                            bi.doRegControl     = false;
                            bi.compToAdd        = null;
                            
                        } else
                        {
                            
                            Color bgColor = getBackgroundColor(props, parent.getBackground());
                            
                            if (UIHelper.getProperty(props, "addsearch", false))
                            {
                                options |= MultiView.ADD_SEARCH_BTN;
                            }
                            
                            //MultiView.printCreateOptions("SUBVIEW", options);
                            MultiView multiView = new MultiView(parent, 
                                                                cellSubView.getName(),
                                                                subView,
                                                                parent.getCreateWithMode(), 
                                                                cellSubView.getDefaultAltViewType(),
                                                                options, 
                                                                bgColor);
                            multiView.setClassToCreate(getClassToCreate(parent, cell));
                            setBorder(multiView, cellSubView.getProperties());
                            
                            parent.addChildMV(multiView);
                            
                            //log.debug("["+cell.getType()+"] ["+cell.getName()+"] col: "+bi.colInx+" row: "+rowInx+" colspan: "+cell.getColspan()+" rowspan: "+cell.getRowspan());
                            viewBldObj.addSubView(cellSubView, multiView, bi.colInx, rowInx, cellSubView.getColspan(), 1);
                            viewBldObj.closeSubView(cellSubView);
                            
                            Viewable viewable = multiView.getCurrentView();
                            if (viewable instanceof TableViewObj)
                            {
                                ((TableViewObj)viewable).setVisibleRowCount(cellSubView.getTableRows());
                            }
                            bi.colInx += cell.getColspan() + 1;
                        }
                        bi.curMaxRow = rowInx;
                        
                        //if (hasColor)
                        //{
                        //    setMVBackground(multiView, multiView.getBackground());
                        //}
                        
                    } else
                    {
                        log.error("buildFormView - parent is NULL for subview ["+subViewName+"]");
                        bi.colInx += 2;
                    }
                } else
                {
                    //log.debug("["+cell.getType()+"] ["+cell.getName()+"] col: "+bi.colInx+" row: "+rowInx+" colspan: "+cell.getColspan()+" rowspan: "+cell.getRowspan());
                    viewBldObj.addSubView(cellSubView, parent, bi.colInx, rowInx, cellSubView.getColspan(), 1); 
                    
                    AltViewIFace     altView        = subView.getDefaultAltView();
                    FormViewDefIFace subFormViewDef = (FormViewDefIFace)altView.getViewDef();
                    DBTableInfo      sbTableInfo    = DBTableIdMgr.getInstance().getByClassName(subView.getClassName());  

                    processRows(sbTableInfo, parent, formViewDef, validator, viewBldObj, altView.getMode(), labelsForHash, currDataObj, subFormViewDef.getRows());
                    viewBldObj.closeSubView(cellSubView);
                    bi.colInx += cell.getColspan() + 1;
                }

            } else
            {
                log.error("buildFormView - Could find subview's with ViewSet["+cellSubView.getViewSetName()+"] ViewName["+subViewName+"]");
            }
            // still have compToAdd = null;
            

        } else if (cell.getType() == FormCellIFace.CellType.statusbar)
        {
            bi.compToAdd        = new JStatusBar();
            bi.doRegControl     = true;
            bi.doAddToValidator = false;
            
        } else if (cell.getType() == FormCellIFace.CellType.panel)
        {
            FormCellPanel           cellPanel = (FormCellPanel)cell;
            PanelViewable.PanelType panelType = PanelViewable.getType(cellPanel.getPanelType());
            
            if (panelType == PanelViewable.PanelType.Panel)
            {
                PanelViewable panelViewable = new PanelViewable(viewBldObj, cellPanel);

                processRows(null, parent, formViewDef, validator, panelViewable, mode, labelsForHash, currDataObj, cellPanel.getRows());

                bi.compToAdd = panelViewable;

            } else if (panelType == PanelViewable.PanelType.ButtonBar)
            {
                bi.compToAdd = PanelViewable.buildButtonBar(processRows(viewBldObj, cellPanel.getRows()));
                
            } else
            {
                throw new RuntimeException("Panel Type is not implemented.");
            }

            bi.doRegControl     = false;
            bi.doAddToValidator = false;

        }

    }
    
    /**
     * Processes the rows in a definition.
     * @param tableInfo table info for current form (may be null)
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
    protected void processRows(final DBTableInfo               tableInfo,
                               final MultiView                 parent,
                               final FormViewDefIFace          formViewDef,
                               final FormValidator             validator,
                               final ViewBuilderIFace          viewBldObj,
                               final AltViewIFace.CreationMode mode,
                               final Hashtable<String, JLabel> labelsForHash,
                               final Object                    currDataObj,
                               final List<FormRowIFace>        formRows)
    {
        BuildInfoStruct bi = new BuildInfoStruct();
        bi.curMaxRow  = 1;
        
        int     rowInx = 1;
        boolean hasRequiredOrDerivedField = false;
        
        for (FormRowIFace row : formRows)
        {
            bi.colInx = 1;

            for (FormCellIFace cell : row.getCells())
            {
                DBTableChildIFace childInfo = null;
                String            cellName  = cell.getName();
                if (tableInfo != null && StringUtils.isNotEmpty(cellName))
                {
                    childInfo = tableInfo.getItemByName(cellName);
                }
                
                boolean isEditOnCreateOnly = false;
                if (mode == AltViewIFace.CreationMode.EDIT && cell.getType() == FormCellIFace.CellType.field)
                {
                    isEditOnCreateOnly = ((FormCellField)cell).getPropertyAsBoolean("editoncreate", false);
                    ((FormCellField)cell).setEditOnCreate(true);
                }
                
                createItem(childInfo, parent, formViewDef, validator, viewBldObj, mode, labelsForHash, currDataObj, cell, isEditOnCreateOnly, rowInx, bi);
                
                //log.debug(cell.getType()+" "+cell.getName()+" col: "+bi.colInx);
                if (bi.compToAdd != null)
                {
                    addControl(validator, viewBldObj, rowInx, cell, bi);
                }
                
                if (isEditOnCreateOnly)
                {
                    EditViewCompSwitcherPanel evcsp = (EditViewCompSwitcherPanel)bi.compToReg;
                    evcsp.setParentValidator(validator);
                    
                    BuildInfoStruct bi2 = new BuildInfoStruct();
                    bi2.curMaxRow  = 1;
                    bi2.colInx     = 1;
                    
                    createItem(childInfo, parent, formViewDef, evcsp.getValidator(), viewBldObj, AltViewIFace.CreationMode.EDIT, 
                               labelsForHash, currDataObj, cell, false, rowInx, bi2);
                    Component editCompReg = bi2.compToReg;
                    Component editCompAdd = bi2.compToAdd;
                    
                    createItem(childInfo, parent, formViewDef, null, viewBldObj, AltViewIFace.CreationMode.VIEW, 
                               labelsForHash, currDataObj, cell, false, rowInx, bi2);
                    Component viewCompReg = bi2.compToReg;
                    Component viewCompAdd = bi2.compToAdd;
                    
                    
                    evcsp.set(editCompReg, editCompAdd, viewCompReg, viewCompAdd);
                }

            }
            
            if  (bi.isRequired || bi.isDerivedLabel)
            {
                hasRequiredOrDerivedField = true;
            }
            rowInx += 2;
        }

        if (bi.collapseSepHash != null && bi.collapseSepHash.size() > 0)
        {
            for (Enumeration<CollapsableSeparator> e=bi.collapseSepHash.keys();e.hasMoreElements();)
            {
                CollapsableSeparator collapseSep = e.nextElement();
                Component            comp        = viewBldObj.getControlByName(bi.collapseSepHash.get(collapseSep));
                if (comp != null)
                {
                    collapseSep.setInnerComp(comp);
                }
            }
        }
        
        // Check to see if there is at least one required field
        if (doFixLabels && hasRequiredOrDerivedField)
        {
            viewBldObj.fixUpRequiredDerivedLabels();
        }
    }
    
    /**
     * @param validator
     * @param viewBldObj
     * @param rowInx
     * @param cell
     * @param bi
     */
    protected void addControl(final FormValidator    validator,
                              final ViewBuilderIFace viewBldObj,
                              final int              rowInx,
                              final FormCellIFace    cell,
                              final BuildInfoStruct  bi)
    {
        int colspan = cell.getColspan();
        int rowspan = cell.getRowspan();
        
        //log.debug("["+cell.getType()+"] ["+cell.getName()+"] col: "+bi.colInx+" row: "+rowInx+" colspan: "+colspan+" rowspan: "+rowspan);
        viewBldObj.addControlToUI(bi.compToAdd, bi.colInx, rowInx, colspan, rowspan);

        if (bi.doRegControl)
        {
            viewBldObj.registerControl(cell, bi.compToReg == null ? bi.compToAdd : bi.compToReg);
        }
        
        bi.curMaxRow = Math.max(bi.curMaxRow, rowspan+rowInx);

        if (validator != null && bi.doAddToValidator)
        {
            validator.addUIComp(cell.getIdent(), bi.compToReg == null ? bi.compToAdd : bi.compToReg);
        }
        bi.colInx += colspan + 1;
    }
    
    /**
     * @param cellSubView
     * @param dataClass
     * @return
     */
    protected Field getFieldFromDotNotation(final FormCellSubView cellSubView, final Class<?> dataClass)
    {
        Class<?> parentCls = dataClass;
        
        String[] fieldNames = StringUtils.split(cellSubView.getName(), ".");
        for (int i=0;i<fieldNames.length;i++)
        {
            try
            {
                //System.out.println("["+fieldNames[i]+"]");
                if (fieldNames[i].equals("this"))
                {
                    continue;
                }
                
                Field fld =null;
                try
                {
                    fld = parentCls.getDeclaredField(fieldNames[i]);
                    
                } catch (java.lang.NoSuchFieldException ex)
                {
                    String parentTitle = parentCls.getSimpleName();
                    DBTableInfo ti = DBTableIdMgr.getInstance().getByClassName(parentCls.getName());
                    if (ti != null)
                    {
                        parentTitle = ti.getTitle();
                    }
                    
                    UIRegistry.showError(String.format(UIRegistry.getResourceString("INVALID_FIELD_NAME"), fieldNames[i], parentTitle));
                }
                if (fld != null)
                {
                    if (i == fieldNames.length-1)
                    {
                        return fld;
                    }
                    parentCls = fld.getType();
                    
                } else
                {
                    log.error("Couldn't find field ["+cellSubView.getName()+"] in class ["+parentCls.getSimpleName()+"]");
                }
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
        return null;
    }
    
    /**
     * Determines the class that is to be created.
     * @param multiView the parent
     * @param cell the definitions
     * @return the class
     */
    protected Class<?> getClassToCreate(final MultiView multiView, final FormCellIFace cell)
    {
        DBTableInfo tableInfo = DBTableIdMgr.getInstance().getByClassName(multiView.getView().getClassName());
        if (tableInfo != null)
        {
            DBTableChildIFace tblChild = tableInfo.getItemByName(cell.getName());
            if (tblChild != null)
            {
                return tblChild.getDataClass();
            }
            // Sometime, like in Taxon, the QCBX is not a field in the schema.
            // see 'acceptedParent'
            //log.debug("How did we get here? ["+cell.getName()+"]");
        }
        return null;
    }
    
    /**
     * Sets a border on the component as defined in the properties.
     * @param comp the component
     * @param props the list of properties
     */
    protected void setBorder(final JComponent comp, final Properties props)
    {
        if (props != null)
        {
            String borderType = props.getProperty("border");
            if (StringUtils.isNotEmpty(borderType))
            {
                if (borderType.equals("etched"))
                {
                    comp.setBorder(BorderFactory.createEtchedBorder());
                    
                } else if (borderType.equals("lowered"))
                {
                    comp.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
                    
                } else if (borderType.equals("raised"))
                {
                    comp.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
                    
                } else if (borderType.equals("empty"))
                {
                    comp.setBorder(BorderFactory.createEmptyBorder());
                    
                } else if (borderType.equals("line"))
                {
                    Color  color       = Color.LIGHT_GRAY;
                    String borderColor = props.getProperty("bordercolor");
                    if (StringUtils.isNotEmpty(borderColor))
                    {
                        try
                        {
                            color = UIHelper.parseRGB(borderColor);
                            
                        } catch(ConfigurationException ex)
                        {
                            log.error(ex);
                        }
                    }
                    comp.setBorder(BorderFactory.createLineBorder(color));
                }
            }
        }
    }

    /**
     * @param props
     * @param bgColor
     * @return
     */
    protected Color getBackgroundColor(final Properties props, final Color bgColor)
    {
        if (props != null)
        {
            String colorStr = props.getProperty("bgcolor");
            if (StringUtils.isNotEmpty(colorStr))
            {
                if (colorStr.endsWith("%"))
                {
                    try
                    {
                        int percent = Integer.parseInt(colorStr.substring(0, colorStr.length()-1));
                        double per = (percent / 100.0);
                        int r = Math.min((int)(bgColor.getRed() * per), 255);
                        int g = Math.min((int)(bgColor.getGreen() * per), 255);
                        int b = Math.min((int)(bgColor.getBlue() * per), 255);
                        return new Color(r,g,b);

                    } catch (Exception ex)
                    {
                        log.error(ex);
                    }
                } else
                {
                    try
                    {
                        return UIHelper.parseRGB(colorStr);
                        
                    } catch(ConfigurationException ex)
                    {
                        log.error(ex);
                    }
                }
            }
        }
        return bgColor;
    }
    
    public void setMVBackground(final MultiView parent, final Color bgColor)
    {
        for (MultiView mv : parent.getKids())
        {
            setMVBackground(mv, bgColor);
        }
        for (Viewable viewable : parent.getViewables())
        {
            viewable.getUIComponent().setBackground(bgColor);
        }
    }

    /**
     * Processes the rows for a button bar.
     * @param viewBldObj formViewObj
     * @param formRows formRows
     * @return the array of buttons
     */
    protected JButton[] processRows(final ViewBuilderIFace   viewBldObj,
                                    final List<FormRowIFace> formRows)
    {
        List<JButton> btns = new ArrayList<JButton>();

        for (FormRowIFace row : formRows)
        {
            for (FormCellIFace cell : row.getCells())
            {
                if (cell.getType() == FormCellIFace.CellType.command)
                {
                    FormCellCommand cellCmd = (FormCellCommand)cell;
                    JButton btn  = createButton(cellCmd.getLabel());
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
    public FormViewObj buildFormViewable(final ViewIFace    view,
                                         final AltViewIFace altView,
                                         final MultiView    parentView,
                                         final int          options,
                                         final Color        bgColor)
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

            FormViewObj formViewObj = new FormViewObj(view, altView, parentView, validator, options, bgColor);

            Object currDataObj = formViewObj.getCurrentDataObj();
            
            DBTableInfo tableInfo  = DBTableIdMgr.getInstance().getByClassName(view.getClassName());  

            processRows(tableInfo, parentView, formViewDef, validator, formViewObj, altView.getMode(), labelsForHash, currDataObj, formViewDef.getRows());

            formViewObj.addUsageNotes();
            
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

                if (enableRules != null)
                {
                    for (String id : enableRules.keySet())
                    {
                        fv.addEnableRule(id, enableRules.get(id));
                    }
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
    public TableViewObj buildTableViewable(final ViewIFace    view,
                                           final AltViewIFace altView,
                                           final MultiView    parentView,
                                           final int          options,
                                           final Color        bgColor)
    {
        try
        {
            ViewDefIFace viewDef = altView.getViewDef();
            
            FormValidator validator = null;
            if (altView.isValidated())
            {
                ValidatedJPanel validatedPanel = new ValidatedJPanel();
                validator      = validatedPanel.getFormValidator();
                validator.setDataChangeNotification(true);
            }
            
            DBTableInfo tableInfo  = DBTableIdMgr.getInstance().getByClassName(view.getClassName());
            
            // Special situation where we create a table from a Form Definition
            if (viewDef instanceof FormViewDef)
            {
                FormViewDefIFace          formViewDef   = (FormViewDefIFace)viewDef;  
                Hashtable<String, JLabel> labelsForHash = new Hashtable<String, JLabel>();
                TableViewObj              tableViewObj  = new TableViewObj(view, altView, parentView, validator, options, bgColor);


                processRows(tableInfo, parentView, formViewDef, null, tableViewObj, altView.getMode(), labelsForHash, validator, formViewDef.getRows());
                return tableViewObj;
                
            }
            // else
            FormViewDefIFace formViewDef = (FormViewDefIFace)altView.getViewDef();
            
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
            
            TableViewObj tableViewObj = new TableViewObj(view, altView, parentView, null, options, bgColor);
            
            //Object currDataObj = tableViewObj.getCurrentDataObj();

            processRows(tableInfo, parentView, formViewDef, null, tableViewObj, altView.getMode(), labelsForHash, validator, formViewDef.getRows());

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
    public TableViewObj buildRecordSetTableViewable(final ViewIFace    view,
                                                    final AltViewIFace altView,
                                                    final MultiView    parentView,
                                                    @SuppressWarnings("unused")
                                                    final int          options,
                                                    final Color        bgColor)
    {
        RecordSetTableViewObj rsTableViewObj = null;
        try
        {
            ViewDefIFace viewDef = altView.getViewDef();
            
            // Special situation where we create a table from a Form Definition
            if (viewDef instanceof FormViewDef)
            {
                FormViewDefIFace               formViewDef   = (FormViewDefIFace)viewDef;  
                Hashtable<String, JLabel> labelsForHash = new Hashtable<String, JLabel>();
                
                rsTableViewObj  = new RecordSetTableViewObj(view, altView, parentView, null, 0, bgColor);
                
                DBTableInfo tableInfo  = DBTableIdMgr.getInstance().getByClassName(view.getClassName());  
                
                processRows(tableInfo, parentView, formViewDef, null, rsTableViewObj, altView.getMode(), labelsForHash, null, formViewDef.getRows());
                return rsTableViewObj;
            }

        } catch (Exception e)
        {
            log.error("buildPanel - Outer Name["+altView.getName()+"]");
            e.printStackTrace();
        }
        return rsTableViewObj;
    }


    /**
     * Creates a FormViewObj.
     * 
     * @param multiView the parent multiView
     * @param view the definition of the form view to be created
     * @param altName the name of the altView to be used (can be null - then it defaults to the default AltViewIFace)
     * @param data the data to be set into the form
     * @param options the options needed for creating the form
     * @return a new FormViewObj
     */
    public static Viewable createFormView(final MultiView multiView, 
                                          final ViewIFace      view, 
                                          final String    altName, 
                                          final Object    data,
                                          final int       options)
    {
        return createFormView(multiView, view, altName, data, options, null);
    }


    /**
     * Creates a FormViewObj.
     * 
     * @param multiView the parent multiView
     * @param view the definition of the form view to be created
     * @param altName the name of the altView to be used (can be null - then it defaults to the default AltViewIFace)
     * @param data the data to be set into the form
     * @param options the options needed for creating the form
     * @param bgColor the background color
     * @return a new FormViewObj
     */
    public static Viewable createFormView(final MultiView multiView, 
                                          final ViewIFace view, 
                                          final String    altName, 
                                          final Object    data,
                                          final int       options,
                                          final Color     bgColor)
    {
        if (viewFieldColor == null)
        {
            viewFieldColor = AppPrefsCache.getColorWrapper("ui", "formatting", "viewfieldcolor");
        }

        AltViewIFace altView = view.getAltView(altName);

        if (altView != null)
        {
            Viewable viewable = instance.buildViewable(view, altView, multiView, options, bgColor);
            if (viewable != null)
            {
                if (data != null)
                {
                    viewable.setDataObj(data);
                    //viewable.setDataIntoUI();
                } else
                {
                    // This is bad to have when you don't have any items yet. - rods
                    //throw new RuntimeException("Form could be created because the data was null! ["+view.getName()+"]["+altView.getName()+"]");
                }
                return viewable;
            }
        }
        return null;
    }
}
