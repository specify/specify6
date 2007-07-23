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

package edu.ku.brc.ui.forms.validation;

import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.split;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.af.prefs.AppPrefsChangeEvent;
import edu.ku.brc.af.prefs.AppPrefsChangeListener;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.ui.ColorWrapper;
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.ViewBasedDialogFactoryIFace;
import edu.ku.brc.ui.db.JComboBoxFromQuery;
import edu.ku.brc.ui.db.ViewBasedDisplayIFace;
import edu.ku.brc.ui.db.ViewBasedSearchDialogIFace;
import edu.ku.brc.ui.forms.DataGetterForObj;
import edu.ku.brc.ui.forms.DataObjectSettable;
import edu.ku.brc.ui.forms.DataObjectSettableFactory;
import edu.ku.brc.ui.forms.FormDataObjIFace;
import edu.ku.brc.ui.forms.FormHelper;
import edu.ku.brc.ui.forms.MultiView;
import edu.ku.brc.ui.forms.formatters.DataObjFieldFormatMgr;


/**
 * This is a Validated Auto Complete combobox that is filled from a database table. It implements GetSetValueFace
 * and the set and get methods expect (and return) the Hibernate Object for the the table. When the user types
 * into the editable combobox it performs a case insensitive search against a single field. The display can be
 * constructed from multiple columns in the database. It is highly recommended that the first column be the same column
 * that is being searched. It is is unclear whether showing more columns than they can search on is a problem, this may
 * need to be addressed laster.<br><br>
 * The search looks like this:<br>
 * select distinct lastName,firstName,AgentID from agent where lower(lastName) like 's%' order by lastName asc

 * @code_status Complete
 **
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class ValComboBoxFromQuery extends JPanel implements UIValidatable,
                                                            ListDataListener,
                                                            GetSetValueIFace,
                                                            AppPrefsChangeListener
{
    protected static final Logger log                = Logger.getLogger(ValComboBoxFromQuery.class);

    public static final int CREATE_EDIT_BTN   = 1;
    public static final int CREATE_NEW_BTN    = 2;
    public static final int CREATE_SEARCH_BTN = 4;
    public static final int CREATE_ALL        = 7;
    
    protected enum MODE {Unknown, Editting, NewAndEmpty, NewAndNotEmpty}

    protected static ColorWrapper valtextcolor       = null;
    protected static ColorWrapper requiredfieldcolor = null;

    protected UIValidatable.ErrorType valState  = UIValidatable.ErrorType.Valid;

    protected String             cellName   = null;
    protected boolean            isRequired = false;
    protected boolean            isChanged  = false;
    protected boolean            isNew      = false;
    protected Color              bgColor    = null;

    protected JComboBoxFromQuery comboBox;
    protected JButton            searchBtn  = null;
    protected JButton            createBtn  = null;
    protected JButton            editBtn    = null;
    protected String             className;
    protected String             idName;
    protected String             keyName;
    protected String             format;
    protected String             formatName;
    protected Class<?>           classObj = null;
    protected DataGetterForObj   getter   = null;
    protected String             searchDialogName;
    protected String[]           fieldNames;

    protected FormDataObjIFace   dataObj     = null;
    protected FormDataObjIFace   newDataObj  = null;
    protected MODE               currentMode = MODE.Unknown;

    protected String             displayInfoDialogName;
    protected String             frameTitle = null;

    protected ViewBasedDisplayIFace frame      = null;
    protected MultiView             multiView  = null;

    protected List<FocusListener> focusListeners = new ArrayList<FocusListener>();
    
    protected ActionListener defaultSearchAction;
    protected ActionListener defaultEditAction;
    protected ActionListener defaultNewAction;

    /**
     *  Constructor.
     * @param tableName name of the table to be searched
     * @param idColumn the column name that contains the record ID
     * @param keyColumn the column that is searched
     * @param displayColumn a comma separated list of columns to be displayed and formatted by the format clause (null is OK)
     * @param className the Class name of the java object that represents the table
     * @param idName the POJO field name of the ID column
     * @param keyName the POJO field name of the key column
     * @param format the format specification (null is OK if displayNames is null)
     * @param formatName the name of the pre-defined (user-defined) format
     * @param searchDialogName the name to look up to display the search dialog (from the dialog factory)
     * @param displayInfoDialogName the name to look up to display the info dialog (from the dialog factory)
     * @param objTitle the title of a single object
     */
    public ValComboBoxFromQuery(final String tableName,
                                final String idColumn,
                                final String keyColumn,
                                final String displayColumn,
                                final String className,
                                final String idName,
                                final String keyName,
                                final String format,
                                final String formatName,
                                final String searchDialogName,
                                final String displayInfoDialogName,
                                final String objTitle,
                                final int    btns)
    
    {
        if (StringUtils.isEmpty(displayColumn))
        {
            throw new RuntimeException("For ValComboBoxFromQuery table["+tableName+"] displayColumn null.");
        }
        if (StringUtils.isEmpty(format) && StringUtils.isEmpty(formatName))
        {
            throw new RuntimeException("For ValComboBoxFromQuery table["+tableName+"] both format and formatName are null.");
        }
        if (StringUtils.isEmpty(displayInfoDialogName))
        {
            throw new RuntimeException("For ValComboBoxFromQuery table["+tableName+"] displayInfoDialogName is null.");
        }
        
        this.className  = className;
        this.idName     = idName;
        this.keyName    = keyName;
        this.format     = format;
        this.formatName = formatName;
        this.searchDialogName = searchDialogName;
        this.displayInfoDialogName = displayInfoDialogName;

        comboBox = new JComboBoxFromQuery(tableName, idColumn, keyColumn, displayColumn, format);
        comboBox.setAllowNewValues(true);

        init(objTitle, btns);
    }

    /**
     * Sets the "cell" name of this control, this is the name of this control in the form.
     * @param cellName the cell name
     */
    public void setCellName(String cellName)
    {
        this.cellName = cellName;
    }

    /* (non-Javadoc)
     * @see java.awt.Component#requestFocus()
     */
    @Override
    public void requestFocus()
    {
        comboBox.requestFocus();
    }

    /* (non-Javadoc)ValComboBoxFromQuery
     * @see java.awt.Component#setEnabled(boolean)
     */
    @Override
    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        
        comboBox.setEnabled(enabled);
        if (searchBtn != null)
        {
            searchBtn.setEnabled(enabled);
        }
        if (editBtn != null)
        {
            editBtn.setEnabled(enabled && dataObj != null);
        }
        if (createBtn != null)
        {
            createBtn.setEnabled(enabled);
        }
        
        // Cheap easy way of setting the Combobox's Text Field to the proper BG Color
        setRequired(isRequired);

    }
    
    /**
     * Helper to create a button.
     * @param iconName the name of the icon (not localized)
     * @param tooltipKey the name of the tooltip (not localized)
     * @param objTitle the title of one object needed for the Info Button
     * @return the new button
     */
    protected JButton createBtn(final String iconName, final String tooltipKey, final String objTitle)
    {
        JButton btn = new JButton(IconManager.getIcon(iconName, IconManager.IconSize.Std16));
        btn.setToolTipText(String.format(getResourceString(tooltipKey), new Object[] {objTitle}));
        btn.setFocusable(false);
        btn.setMargin(new Insets(1,1,1,1));
        btn.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
        return btn;
    }

    /**
     * Creates the UI for the ComboBox.
     * @param objTitle the title of one object needed for the Info Button
     */
    public void init(final String objTitle,
                     final int    btnMask)
    {
        fieldNames = split(StringUtils.deleteWhitespace(keyName), ",");

        try
        {
            classObj = Class.forName(className);

        } catch (ClassNotFoundException ex)
        {
           log.error(ex);
           throw new RuntimeException(ex);
        }

        boolean hasSearchBtn = StringUtils.isNotEmpty(searchDialogName);

        PanelBuilder    builder    = new PanelBuilder(new FormLayout("p:g,1px,p,1px,p"+(hasSearchBtn ? ",1px,p" : ""), "c:p"), this);
        CellConstraints cc         = new CellConstraints();

        builder.add(comboBox, cc.xy(1,1));

        int x = 3;
        if ((btnMask & CREATE_EDIT_BTN) != 0)
        {
            editBtn = createBtn("EditIcon", "EditRecordTT", objTitle);
            builder.add(editBtn, cc.xy(x,1));
            x += 2;
        }

        if ((btnMask & CREATE_NEW_BTN) != 0)
        {
            createBtn = createBtn("CreateObj", "NewRecordTT", objTitle); 
            builder.add(createBtn, cc.xy(x,1));
            x += 2;
        }


        if (hasSearchBtn && (btnMask & CREATE_SEARCH_BTN) != 0)
        {
            searchBtn = createBtn("Search", "SearchForRecordTT", objTitle); 
            builder.add(searchBtn, cc.xy(x,1));
            x += 2;
        }

        setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        comboBox.getModel().addListDataListener(this);

        bgColor = comboBox.getTextField().getBackground();
        if (valtextcolor == null || requiredfieldcolor == null)
        {
            valtextcolor = AppPrefsCache.getColorWrapper("ui", "formatting", "valtextcolor");
            requiredfieldcolor = AppPrefsCache.getColorWrapper("ui", "formatting", "requiredfieldcolor");
        }
        AppPreferences.getRemote().addChangeListener("ui.formatting.requiredfieldcolor", this);


        comboBox.getTextField().addFocusListener(new FocusAdapter()
                {
                    @Override
                    public void focusGained(FocusEvent e)
                    {
                        for (FocusListener l : focusListeners)
                        {
                            l.focusGained(e);
                        }
                    }

                    @Override
                    public void focusLost(FocusEvent e)
                    {
                        isNew = false;
                        validateState();
                        repaint();
                        
                        /*
                         * CODE FOR SETTING THE CURRENT OBJECT INTO the COMBOXBOX
                         */
                        //Object data = comboBox.getSelectedItem();
                        //if (data != null && data != dataObj)
                        //{
                        //    setValue(data, null);
                        //}
                        
                        if (comboBox.getTextField() != null)
                        {
                            String str = comboBox.getTextField().getText().trim();
                            if (StringUtils.isNotEmpty(str))
                            {
                                Object selObj = comboBox.getSelectedItem();
                                if (selObj != null && !selObj.toString().equals(str))
                                {
                                    comboBox.getTextField().setText(selObj.toString());
                                }                        
                            } else
                            {
                                comboBox.setSelectedIndex(-1);
                            }
                        }
                        
                        for (FocusListener l : focusListeners)
                        {
                            l.focusLost(e);
                        }
                    }
                });

        if (searchBtn != null)
        {
            defaultSearchAction = new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    ViewBasedSearchDialogIFace dlg = UIRegistry.getViewbasedFactory()
                            .createSearchDialog(UIHelper.getFrame(searchBtn), searchDialogName);
                    dlg.getDialog().setVisible(true);
                    if (!dlg.isCancelled())
                    {
                        setValue(dlg.getSelectedObject(), null);
                        valueHasChanged();
                    }
                }
            };
            searchBtn.addActionListener(defaultSearchAction);
        }

        if (editBtn != null)
        {
            defaultEditAction = new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    currentMode = MODE.Editting;
                    createEditFrame(false);
                }
            };
            editBtn.addActionListener(defaultEditAction);
        }

        if (createBtn != null)
        {
            defaultNewAction = new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    currentMode = dataObj != null ? MODE.NewAndNotEmpty : MODE.NewAndEmpty;
                    createEditFrame(true);
                }
            };
            createBtn.addActionListener(defaultNewAction);
        }
    }
    
    public void setEditAction(ActionListener al)
    {
        if (editBtn != null)
        {
            removeAllActionListeners(editBtn);
            if (al != null)
            {
                editBtn.addActionListener(al);
            }
        }
    }
    
    public void setSearchAction(ActionListener al)
    {
        if (searchBtn != null)
        {
            removeAllActionListeners(searchBtn);
            if (al != null)
            {
                searchBtn.addActionListener(al);
            }
        }
    }
    
    public void setNewAction(final ActionListener al)
    {
        if (createBtn != null)
        {
            removeAllActionListeners(createBtn);
            if (al != null)
            {
                createBtn.addActionListener(al);
            }
        }
    }

    protected void removeAllActionListeners(final JButton button)
    {
        for (ActionListener al: button.getActionListeners())
        {
            button.removeActionListener(al);
        }
    }
    
    public void setEditEnabled(boolean enabled)
    {
        if (editBtn != null)
        { 
            editBtn.setEnabled(enabled);
        }
    }
    
    protected void valueHasChanged()
    {
        if (frame != null)
        {
            MultiView mv = frame.getMultiView();
            if (mv != null)
            {           
               if (mv.hasChanged())
               {
                   this.setChanged(true);
                   // TODO: Change this, this is SOOOO lame
                   // I set and reset it to make the change data listener get activated.
                   // There has to be a better way!
                   int inx = comboBox.getSelectedIndex();
                   comboBox.setSelectedIndex(-1);
                   comboBox.setSelectedIndex(inx);
               }
            }
            
            frame.getMultiView().getDataFromUI();
            refreshUIFromData();
        }
    }

    /**
     * Creates a Dialog (non-modal) that will display detail information
     * for the object in the text field.
     */
    protected void createEditFrame(final boolean isNewObject)
    {
        String closeBtnTitle = getResourceString(isNewObject ? "Accept" : "Save");
        frame = UIRegistry.getViewbasedFactory().createDisplay(UIHelper.getFrame(this),
                                                                   displayInfoDialogName,
                                                                   frameTitle,
                                                                   closeBtnTitle,
                                                                   true,   // false means View Mode
                                                                   (isNewObject ? MultiView.IS_NEW_OBJECT : 0) | MultiView.HIDE_SAVE_BTN,
                                                                   ViewBasedDialogFactoryIFace.FRAME_TYPE.DIALOG);
        if (isNewObject)
        {
            newDataObj = FormHelper.createAndNewDataObj(classObj);
            newDataObj.initialize();
            
            //frame.setData(newDataObj);

            // Now get the setter for an object and set the value they typed into the combobox and place it in
            // the first field name
            DataObjectSettable ds = DataObjectSettableFactory.get(classObj.getName(), "edu.ku.brc.ui.forms.DataSetterForObj");
            if (ds != null)
            {
                ds.setFieldValue(newDataObj, fieldNames[0], comboBox.getTextField().getText());
            }
            frame.setData(newDataObj);

        } else
        {
            frame.setData(dataObj);
        }
        
        //if (multiView != null)
        //{
        //    multiView.registerDisplayFrame(frame);
        //}
        
        frame.showDisplay(true);
        if (frame.getBtnPressed() == ViewBasedDisplayIFace.OK_BTN)
        {
            if (frame.isEditMode())
            {
                if (currentMode == MODE.NewAndEmpty)
                {
                    if (multiView != null)
                    {
                        Object parentDataObj = multiView.getData();
                        if (parentDataObj instanceof FormDataObjIFace)
                        {
                            ((FormDataObjIFace) parentDataObj).addReference(newDataObj, cellName);
                        }
                        else
                        {
                            FormHelper.addToParent(multiView != null ? multiView.getData() : null, newDataObj);
                        }
                    }
                    setValue(newDataObj, null);
                    newDataObj = null;
                }
                valueHasChanged();
            }

            currentMode = MODE.Unknown;

            //if (multiView != null)
            //{
            //    multiView.unregisterDisplayFrame(frame);
            //}
        }
        frame.dispose();
        frame = null;
    }

    /**
     * Sets the string that is pre-appended to the title.
     * @param frameTitle the string arg
     */
    public void setFrameTitle(final String frameTitle)
    {
        this.frameTitle = frameTitle;
    }

    /**
     * Sets the MultiView parent into the control.
     * @param multiView parent multiview
     */
    public void setMultiView(final MultiView multiView)
    {
        this.multiView = multiView;
    }

    /**
     * Return the JComboBox for this control.
     * @return the JComboBox for this control
     */
    public JComboBox getComboBox()
    {
        return comboBox;
    }

    /**
     * Returns the model for the combobox.
     * @return the model for the combobox
     */
    public ComboBoxModel getModel()
    {
        return comboBox.getModel();
    }


    /* (non-Javadoc)
     * @see javax.swing.JComboBox#setModel(javax.swing.ComboBoxModel)
     */
    public void setModel(ComboBoxModel model)
    {
        comboBox.setModel(model);
        model.addListDataListener(this);
    }

    /* (non-Javadoc)
     * @see java.awt.Component#paint(java.awt.Graphics)
     */
    @Override
    public void paint(Graphics g)
    {
        super.paint(g);

        if (!isNew && valState == UIValidatable.ErrorType.Error && comboBox.isEnabled())
        {
            Graphics2D g2d = (Graphics2D)g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Dimension dim = getSize();
            g.setColor(valtextcolor.getColor());
            g.drawRect(0, 0, dim.width-1, dim.height-1);
        }
    }

    // Overriding the add/remove of FocusListeners is so we can make sure they get
    // called AFTER the Combobox has had a change to process it's focus listener

    /* (non-Javadoc)
     * @see java.awt.Component#addFocusListener(java.awt.event.FocusListener)
     */
    @Override
    public void addFocusListener(FocusListener l)
    {
        focusListeners.add(l);
    }

    /* (non-Javadoc)
     * @see java.awt.Component#removeFocusListener(java.awt.event.FocusListener)
     */
    @Override
    public void removeFocusListener(FocusListener l)
    {
        focusListeners.remove(l);
    }

    /**
     * Updates the UI from the data value (assume the data has changed but OK if it hasn't).
     */
    public void refreshUIFromData()
    {
        List<String> list = comboBox.getList();
        list.clear();

        if (this.dataObj != null)
        {
            if (getter == null)
            {
                getter = new DataGetterForObj();
            }

            // NOTE: If there was a formatName defined for this then the value coming
            // in will already be correctly formatted.
            // So just set the cvalue if there is a format name.
            Object newVal = this.dataObj;
            if (isEmpty(formatName))
            {
                Object[] val = UIHelper.getFieldValues(fieldNames, this.dataObj, getter);
                if (isNotEmpty(format))
                {
                    newVal = UIHelper.getFormattedValue(val, format);
                } else
                {
                    newVal = this.dataObj;
                }
            } else
            {
                newVal = DataObjFieldFormatMgr.format(this.dataObj, formatName);
            }

            if (newVal != null)
            {
                comboBox.getTextField().setCaretPosition(0);
                list.add(newVal.toString());
                comboBox.setSelectedIndex(0);
                valState = UIValidatable.ErrorType.Valid;
                if (editBtn != null)
                {
                    editBtn.setEnabled(true);
                }
                
            } else
            {
                comboBox.setSelectedIndex(-1);
                valState = UIValidatable.ErrorType.Incomplete;
            }

        } else
        {
            comboBox.setSelectedIndex(-1);
            valState = UIValidatable.ErrorType.Incomplete;
            if (editBtn != null)
            {
                editBtn.setEnabled(false);
            }
        }
        repaint();
    }


    //--------------------------------------------------
    //-- UIValidatable Interface
    //--------------------------------------------------

    /* (non-Javadoc)
     * @see edu.kui.brc.ui.validation.UIValidatable#isInError()
     */
    public boolean isInError()
    {
        return valState != UIValidatable.ErrorType.Valid;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#getState()
     */
    public ErrorType getState()
    {
        return valState;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#setState(edu.ku.brc.ui.forms.validation.UIValidatable.ErrorType)
     */
    public void setState(ErrorType state)
    {
        this.valState = state;
    }
    /* (non-Javadoc)
     * @see edu.kui.brc.ui.validation.UIValidatable#isRequired()
     */
    public boolean isRequired()
    {
        return isRequired;
    }

    /* (non-Javadoc)
     * @see edu.kui.brc.ui.validation.UIValidatable#setRequired(boolean)
     */
    public void setRequired(boolean isRequired)
    {
        comboBox.getTextField().setBackground(isRequired && isEnabled() ? requiredfieldcolor.getColor() : bgColor);
        this.isRequired = isRequired;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#isChanged()
     */
    public boolean isChanged()
    {
        return isChanged;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#setChanged(boolean)
     */
    public void setChanged(boolean isChanged)
    {
        this.isChanged = isChanged;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#setAsNew(boolean)
     */
    public void setAsNew(boolean isNew)
    {
        this.isNew = isRequired ? isNew : false;
    }

    /* (non-Javadoc)
     * @see java.awt.Component#validate()
     */
    public UIValidatable.ErrorType validateState()
    {
        valState = isRequired && comboBox.getSelectedIndex() == -1 ? UIValidatable.ErrorType.Incomplete : UIValidatable.ErrorType.Valid;
        return valState;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#reset()
     */
    public void reset()
    {
        comboBox.setSelectedIndex(-1);
        if (comboBox.getTextField() != null)
        {
            comboBox.getTextField().setText("");
        }
        valState = isRequired ? UIValidatable.ErrorType.Incomplete : UIValidatable.ErrorType.Valid;
        repaint();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#getValidatableUIComp()
     */
    public Component getValidatableUIComp()
    {
        return this;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#cleanUp()
     */
    public void cleanUp()
    {
        classObj   = null;
        getter     = null;
        dataObj    = null;
        frame      = null;
        multiView  = null;

        focusListeners.clear();

        comboBox           = null;
        AppPreferences.getRemote().removeChangeListener("ui.formatting.requiredfieldcolor", this);
    }

    //--------------------------------------------------------
    // ListDataListener (JComboxBox)
    //--------------------------------------------------------

    /* (non-Javadoc)
     * @see javax.swing.event.ListDataListener#contentsChanged(javax.swing.event.ListDataEvent)
     */
    public void contentsChanged(ListDataEvent e)
    {
        isChanged = true;
        validateState();
        repaint();
    }

    /* (non-Javadoc)
     * @see javax.swing.event.ListDataListener#intervalAdded(javax.swing.event.ListDataEvent)
     */
    public void intervalAdded(ListDataEvent e)
    {
        // do nothing
    }

    /* (non-Javadoc)
     * @see javax.swing.event.ListDataListener#intervalRemoved(javax.swing.event.ListDataEvent)
     */
    public void intervalRemoved(ListDataEvent e)
    {
        // do nothing
    }


    //--------------------------------------------------------
    // GetSetValueIFace
    //--------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.GetSetValueIFace#setValue(java.lang.Object, java.lang.String)
     */
    public void setValue(Object value, String defaultValue)
    {
        
        if (value == null || value instanceof FormDataObjIFace)
        {
            dataObj = (FormDataObjIFace)value;
            refreshUIFromData();
            
        } else
        {
            throw new RuntimeException("Data is does not extend FormDataObjIFace "+ value);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.GetSetValueIFace#getValue()
     */
    public Object getValue()
    {
        if (comboBox.getTextField() != null && StringUtils.isEmpty(comboBox.getTextField().getText().trim()))
        {
            return null;
        }
        
        Object value = null;
        Long  id     = comboBox.getSelectedId();
        if (id != null)
        {
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();

            List<?> list = session.getDataList(classObj, idName, id, DataProviderSessionIFace.CompareType.Restriction);
            if (list.size() != 0)
            {
                value = list.get(0);
            } else
            {
                log.error("**** Can't find the Object "+classObj+" with ID: "+id);
            }
            session.close();

        } else
        {
            return dataObj;
        }

        return value;
    }

    //-------------------------------------------------
    // AppPrefsChangeListener
    //-------------------------------------------------

    public void preferenceChange(AppPrefsChangeEvent evt)
    {
        if (evt.getKey().equals("ui.formatting.requiredfieldcolor"))
        {
            comboBox.setBackground(isRequired && isEnabled() ? requiredfieldcolor.getColor() : bgColor);
        }
    }

}
