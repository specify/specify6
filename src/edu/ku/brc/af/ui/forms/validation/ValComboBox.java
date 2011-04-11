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
package edu.ku.brc.af.ui.forms.validation;

import static edu.ku.brc.ui.UIHelper.setControlSize;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.text.JTextComponent;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.af.prefs.AppPrefsChangeEvent;
import edu.ku.brc.af.prefs.AppPrefsChangeListener;
import edu.ku.brc.af.ui.db.PickListDBAdapterIFace;
import edu.ku.brc.af.ui.db.PickListIFace;
import edu.ku.brc.af.ui.db.PickListItemIFace;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.persist.FormDevHelper;
import edu.ku.brc.specify.datamodel.PickListItem;
import edu.ku.brc.ui.ColorWrapper;
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.Java2sAutoComboBox;
import edu.ku.brc.ui.Java2sAutoTextField;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;


/**
 * A JComboBox that implements UIValidatable for participating in validation

 * @code_status Beta
 **
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class ValComboBox extends JPanel implements UIValidatable, 
                                                   ListDataListener, 
                                                   GetSetValueIFace, 
                                                   AppPrefsChangeListener,
                                                   FormControlSaveable
{
    // Static Members
    private static final Logger log  = Logger.getLogger(ValComboBox.class);
            
    protected static Color        defaultTextBGColor = null;
    protected static ColorWrapper valTextColor       = null;
    protected static ColorWrapper requiredFieldColor = null;

    // Data Members
    protected UIValidatable.ErrorType valState       = UIValidatable.ErrorType.Valid;
    protected boolean                 isRequired     = false;
    protected boolean                 isChanged      = false;
    protected boolean                 isNew          = false;
    protected Integer                 nullIndex      = null;

    protected JComboBox               comboBox       = null;
    protected Java2sAutoTextField     textEditor     = null;
    protected String                  defaultValue   = null;
    protected String                  currTypedValue = null;
    protected PickListDBAdapterIFace  adapter        = null;
    
    protected MultiView               multiView      = null;
    protected boolean                 isFormObjNew   = false;
    
    // Change Notification
    protected Vector<ChangeListener>  listeners      = null;
    
    /**
     * Constructor.
     */
    public ValComboBox(final boolean editable)
    {
        if (editable)
        {
            comboBox = new Java2sAutoComboBox(new ArrayList<Object>());
        } else
        {
            comboBox = new ClearableComboBox();
            setControlSize(comboBox);
        }
        init(editable);
    }

    /**
     * Constructor.
     * @param array object array of items
     */
    public ValComboBox(final Object[] array, final boolean editable)
    {
        if (editable)
        {
            ArrayList<Object> items = new ArrayList<Object>();
            Collections.addAll(items, array);
            comboBox = new Java2sAutoComboBox(items);
        } else
        {
            comboBox = new ClearableComboBox(array);
            setControlSize(comboBox);
        }
        init(editable);
    }

    /**
     * Constructor.
     * @param vector vector of items
     */
    public ValComboBox(final Vector<?> vector, final boolean editable)
    {
        if (editable)
        {
            comboBox = new Java2sAutoComboBox(vector);
        } else
        {
            comboBox = new ClearableComboBox(vector);
            setControlSize(comboBox);
        }
        init(editable);
    }
    
    /**
     * Constructor with dbAdapter.
     * @param dbAdapter the adaptor for enabling auto complete
     */
    public ValComboBox(final PickListDBAdapterIFace adapter)
    {
        if (!adapter.isReadOnly())
        {
            Java2sAutoComboBox cbx = new Java2sAutoComboBox(adapter.getList());
            comboBox = cbx;
            setControlSize(comboBox);
            cbx.setStrict(false);
            
        } else if (adapter instanceof ComboBoxModel)
        {
            comboBox = new ClearableComboBox((ComboBoxModel)adapter);
            setControlSize(comboBox);
        } else
        {
            String msg = "PickListDBAdapterIFace is not an instanceof ComboBoxModel and MUST BE!";
            FormDevHelper.appendFormDevError(msg);
        }
            
        this.adapter = adapter;
        adapter.setAutoSaveOnAdd(false);
        
        init(!adapter.isReadOnly());
    }

    /**
     * @param makeEditable
     */
    public void init(final boolean makeEditable)
    {
        if (makeEditable)
        {
            Java2sAutoComboBox cbx = (Java2sAutoComboBox)comboBox;
            textEditor = cbx.getAutoTextFieldEditor().getAutoTextFieldEditor();
            textEditor.addKeyListener(getTextKeyAdapter());
            addPopupMenu(textEditor);
            
            comboBox.addKeyListener(new KeyAdapter() {

                @Override
                public void keyPressed(KeyEvent e)
                {
                    super.keyPressed(e);
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE || 
                               e.getKeyCode() == KeyEvent.VK_DELETE || 
                               e.getKeyCode() == KeyEvent.VK_BACK_SPACE)
                    {
                        comboBox.setSelectedIndex(-1);
                    }       
                    notifyChangeListeners(new ChangeEvent(ValComboBox.this));
                }

                @Override
                public void keyReleased(KeyEvent e)
                {
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE || 
                            e.getKeyCode() == KeyEvent.VK_DELETE || 
                            e.getKeyCode() == KeyEvent.VK_BACK_SPACE)
                    {
                         comboBox.setSelectedIndex(-1);
                    }
                    super.keyReleased(e);
                }

                /* (non-Javadoc)
                 * @see java.awt.event.KeyAdapter#keyTyped(java.awt.event.KeyEvent)
                 */
                @Override
                public void keyTyped(KeyEvent e)
                {
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE || 
                            e.getKeyCode() == KeyEvent.VK_DELETE || 
                            e.getKeyCode() == KeyEvent.VK_BACK_SPACE)
                    {
                         comboBox.setSelectedIndex(-1);
                    }
                    super.keyTyped(e);
                }
                
            });
        }

        setOpaque(false);
        
        if (defaultTextBGColor == null)
        {
            defaultTextBGColor = (new JTextField()).getBackground();
        }
        
        setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        PanelBuilder    builder    = new PanelBuilder(new FormLayout("f:p:g", "p:g:f"), this);
        CellConstraints cc         = new CellConstraints();
        builder.add(comboBox, cc.xy(1,1));

        comboBox.getModel().addListDataListener(this);

        if (valTextColor == null || requiredFieldColor == null)
        {
            valTextColor = AppPrefsCache.getColorWrapper("ui", "formatting", "valtextcolor");
            requiredFieldColor = AppPrefsCache.getColorWrapper("ui", "formatting", "requiredfieldcolor");
        }
        if (valTextColor != null)
        {
            AppPrefsCache.addChangeListener("ui.formatting.valtextcolor", this);
            AppPrefsCache.addChangeListener("ui.formatting.requiredfieldcolor", this);
        }
    }
    
    /**
     * @param txtComp
     */
    private void addPopupMenu(final JTextComponent txtComp)
    {
        final JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem mi = new JMenuItem(UIRegistry.getResourceString("DELETE"));
        popupMenu.add(mi);
        mi.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                PickListItemIFace pli = (PickListItemIFace)comboBox.getSelectedItem();
                if (pli != null)
                {
                    PickListIFace pl = adapter.getPickList();
                    pl.removeItem(pli);
                }
            }
        });
    }
    
    /**
     * @param listener
     */
    public void addChangeListener(final ChangeListener listener)
    {
        if (this.listeners == null)
        {
            this.listeners = new Vector<ChangeListener>();
        }
        this.listeners.add(listener);
    }
    
    public JTextField getTextField()
    {
        return textEditor;
    }

    /**
     * @return
     */
    private KeyAdapter getTextKeyAdapter()
    {
        return new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e)
            {
                super.keyPressed(e);
                notifyChangeListeners(new ChangeEvent(ValComboBox.this));
            }

            @Override
            public void keyReleased(KeyEvent e)
            {
                super.keyReleased(e);
            }
        };
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#requestFocus()
     */
    @Override
    public void requestFocus()
    {
        comboBox.requestFocus();
    }

    /* (non-Javadoc)
     * @see java.awt.Component#setEnabled(boolean)
     */
    @Override
    public void setEnabled(final boolean enabled)
    {
        super.setEnabled(enabled);
        comboBox.setEnabled(enabled);
        
        // Cheap and easy way to set the BG Color depending on Enabled state
        setRequired(isRequired);
    }

    /**
     * Returns the model for the combobox.
     * @return the model for the combobox.
     */
    public ComboBoxModel getModel()
    {
        return comboBox.getModel();
    }

    /**
     * @param model
     */
    public void setModel(final ComboBoxModel model)
    {
        comboBox.setModel(model);
        model.addListDataListener(this);
    }

    /* (non-Javadoc)
     * @see java.awt.Component#paint(java.awt.Graphics)
     */
    @Override
    public void paint(final Graphics g)
    {
        super.paint(g);

        if (!isNew && valState == UIValidatable.ErrorType.Error && isEnabled())
        {
            UIHelper.drawRoundedRect((Graphics2D)g, valTextColor.getColor(), getSize(), 1);
        }
    }

    /**
     * Returns the combobox
     * @return the combobox
     */
    public JComboBox getComboBox()
    {
        return comboBox;
    }

    /**
     * Helper function that indicates whether the control has text or not
     * @return whether the control has text or not
     */
    public boolean hasText()
    {
        return textEditor != null ? textEditor.getText().length() > 0 : comboBox.getSelectedIndex() > -1;
    }

    /**
     * Helper function that returns true if an item is selected or the text field is not empty
     * @return true if an item is selected or the text field is not empty
     */
    @Override
    public boolean isNotEmpty()
    {
        //log.debug((comboBox.getSelectedIndex() > -1) +"  "+ hasText()+"  "+comboBox.getSelectedItem());
        return comboBox.getSelectedIndex() > -1 || hasText();
    }

    /* (non-Javadoc)
     * @see java.awt.Component#addFocusListener(java.awt.event.FocusListener)
     */
    @Override
    public void addFocusListener(final FocusListener l)
    {
        comboBox.addFocusListener(l);
        if (textEditor != null)
        {
            textEditor.addFocusListener(l);
        }
    }

    /* (non-Javadoc)
     * @see java.awt.Component#removeFocusListener(java.awt.event.FocusListener)
     */
    @Override
    public void removeFocusListener(final FocusListener l)
    {
        comboBox.removeFocusListener(l);
        if (textEditor != null)
        {
            textEditor.removeFocusListener(l);
        }
    }

    //--------------------------------------------------
    //-- FormControlSaveable Interface
    //--------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.FormControlSaveable#saveControlData()
     */
    @Override
    public boolean saveControlData()
    {
        if (adapter != null && comboBox.getSelectedIndex() == -1 && textEditor != null)
        {
            String newValue = textEditor.getText();
            if (StringUtils.isNotEmpty(newValue))
            {
                adapter.addItem(newValue, newValue);
                adapter.save();
                Java2sAutoComboBox cbx = (Java2sAutoComboBox)comboBox;
                cbx.setDataList(adapter.getList());
            }
        }
        return true;
    }
    
    
    //--------------------------------------------------
    //-- UIValidatable Interface
    //--------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#isInError()
     */
    @Override
    public boolean isInError()
    {
        return valState != UIValidatable.ErrorType.Valid;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#getState()
     */
    @Override
    public ErrorType getState()
    {
        return valState;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#setState(edu.ku.brc.ui.forms.validation.UIValidatable.ErrorType)
     */
    @Override
    public void setState(ErrorType state)
    {
        this.valState = state;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#isRequired()
     */
    @Override
    public boolean isRequired()
    {
        return isRequired;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#setRequired(boolean)
     */
    @Override
    public void setRequired(final boolean isRequired)
    {
        if (textEditor != null)
        {
            textEditor.setBackground(isRequired && isEnabled() ? requiredFieldColor.getColor() : defaultTextBGColor);
        }
        this.isRequired = isRequired;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#isChanged()
     */
    @Override
    public boolean isChanged()
    {
        return isChanged;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#setChanged(boolean)
     */
    @Override
    public void setChanged(final boolean isChanged)
    {
        this.isChanged = isChanged;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#setAsNew(boolean)
     */
    @Override
    public void setAsNew(final boolean isNew)
    {
        this.isNew = isRequired ? isNew : false;
    }

    /* (non-Javadoc)
     * @see java.awt.Component#validate()
     */
    @Override
   public UIValidatable.ErrorType validateState()
    {
        //log.debug(isRequired+"  "+ comboBox.getSelectedIndex());
        valState = isRequired && comboBox.getSelectedIndex() == -1 ? UIValidatable.ErrorType.Incomplete : UIValidatable.ErrorType.Valid;
        return valState;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#reset()
     */
    @Override
    public void reset()
    {
        comboBox.setSelectedIndex(-1);
        if (textEditor != null)
        {
            textEditor.setText(StringUtils.isNotEmpty(defaultValue) ? defaultValue : "");
        }
        valState = isRequired ? UIValidatable.ErrorType.Incomplete : UIValidatable.ErrorType.Valid;
        repaint();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#getValidatableUIComp()
     */
    @Override
    public Component getValidatableUIComp()
    {
        return this;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#cleanUp()
     */
    @Override
    public void cleanUp()
    {
        if (textEditor != null)
        {
            UIHelper.removeFocusListeners(textEditor);
            UIHelper.removeKeyListeners(textEditor);
        }
        comboBox.getModel().removeListDataListener(this);
        UIHelper.removeFocusListeners(comboBox);
        UIHelper.removeKeyListeners(this);
        comboBox = null;
        AppPrefsCache.removeChangeListener("ui.formatting.requiredfieldcolor", this);
        
        if (listeners != null)
        {
            listeners.clear();
            listeners  = null;
        }
    }
    
    /**
     * Notify all the change listeners.
     * @param e the change event or null
     */
    protected void notifyChangeListeners(final ChangeEvent e)
    {
        if (listeners != null)
        {
            for (ChangeListener l : listeners)
            {
                l.stateChanged(e);
            }
        }
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.validation.UIValidatable#getReason()
     */
    @Override
    public String getReason()
    {
        return null;
    }

    //--------------------------------------------------------
    // ListDataListener (JComboxBox)
    //--------------------------------------------------------

    /* (non-Javadoc)
     * @see javax.swing.event.ListDataListener#contentsChanged(javax.swing.event.ListDataEvent)
     */
    @Override
    public void contentsChanged(final ListDataEvent e)
    {
        isChanged = true;
        validateState();
        repaint();
    }

    /* (non-Javadoc)
     * @see javax.swing.event.ListDataListener#intervalAdded(javax.swing.event.ListDataEvent)
     */
    @Override
    public void intervalAdded(final ListDataEvent e)
    {
        // do nothing
    }

    /* (non-Javadoc)
     * @see javax.swing.event.ListDataListener#intervalRemoved(javax.swing.event.ListDataEvent)
     */
    @Override
    public void intervalRemoved(final ListDataEvent e)
    {
        // do nothing
    }

    /**
     * @return
     */
    private MultiView getMultiView()
    {
        Component comp = getParent();
        do
        {
            if (comp instanceof MultiView)
            {
                return (MultiView)comp;
            }
            if (comp != null)
            {
                comp = comp.getParent();
            } else
            {
                break;
            }
        } while (true);
        
        return null;
    }
    
    //--------------------------------------------------------
    // GetSetValueIFace
    //--------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.GetSetValueIFace#setValue(java.lang.Object, java.lang.String)
     */
    @Override
    public void setValue(final Object value, final String defaultValue)
    {
        if (multiView == null)
        {
            multiView = getMultiView();
        }
        
        if (multiView != null)
        {
            isNew = isFormObjNew = multiView.isNewForm();
        }
        
        Integer fndInx = -1;
        
        if (value != null)
        {
            ComboBoxModel  model = comboBox.getModel();
            boolean isFormObjIFace = value instanceof FormDataObjIFace;

            if (comboBox.getModel() instanceof PickListDBAdapterIFace)
            {
                for (int i=0;i<comboBox.getItemCount();i++)
                {
                    PickListItemIFace pli    = (PickListItemIFace)model.getElementAt(i);
                    Object            valObj = pli.getValueObject();
                    
                    if (valObj != null)
                    {
                        if (isFormObjIFace && valObj instanceof FormDataObjIFace)
                        {
                            //log.debug(((FormDataObjIFace)value).getId().longValue()+"  "+(((FormDataObjIFace)valObj).getId().longValue()));
                            if (((FormDataObjIFace)value).getId().intValue() == (((FormDataObjIFace)valObj).getId().intValue()))
                            {
                                fndInx = i;
                                break;                                
                            }
                        } else if (pli.getValue().equals(value.toString()))
                        {
                            fndInx = i;
                            break;                            
                        }
                    } else
                    {
                        Object pliObj = pli.getValue();
                        if (pliObj != null && pliObj.equals(value.toString())) // really should never be null!
                        {
                            fndInx = i;
                            break;
                        }
                    }
                }
                
                // Decided to just let non-existent vales pass on by
                if (fndInx == -1 && comboBox.getModel() instanceof PickListDBAdapterIFace)
                {
                    PickListDBAdapterIFace pla      = (PickListDBAdapterIFace)comboBox.getModel();
                    PickListIFace          pickList = pla.getPickList();
                    if (!pickList.getReadOnly())
                    {
                        textEditor.setText(value.toString());
                        
                    } else
                    {
                        UIRegistry.showLocalizedError("ValComboBox.PL_ITEM_NOTFND", value.toString());//$NON-NLS-1$
                    }
                }

            } else
            {
                for (int i=0;i<comboBox.getItemCount();i++)
                {
                    Object item = model.getElementAt(i);
                    if (item instanceof String)
                    {
                        String val = value != null && StringUtils.isEmpty(value.toString()) && StringUtils.isNotEmpty(defaultValue) ? defaultValue : (value != null ? value.toString() : "");
                        if (((String)item).equals(val))
                        {
                            fndInx = i;
                            break;
                        }
                    } else if ((isFormObjIFace && item == value) || item.equals(value))
                    {
                        fndInx = i;
                        break;
                    }
                }
            }
            
            if (fndInx != -1)
            {
                this.valState = UIValidatable.ErrorType.Valid;
                
            } else
            {
                this.valState = (comboBox.getModel() instanceof PickListDBAdapterIFace && isRequired) || isRequired ? UIValidatable.ErrorType.Incomplete : UIValidatable.ErrorType.Valid;
            }
        } else
        {
            if (nullIndex == null)
            {
                if (adapter != null)
                {
                    int inx = 0;
                    for (PickListItemIFace item : adapter.getList())
                    {
                        if (item != null && item.getValue() != null)
                        {
                            if (item.getValue().equals("|null|"))
                            {
                                nullIndex = inx;
                                comboBox.setSelectedIndex(nullIndex);
                                break;
                            }
                        } else if (item != null && item.getValueObject() == null)
                        {
                            log.error("PickList item's value was null and it can't be. Title["+item.getTitle()+"]");
                        }
                        inx++;
                    }
                    if (nullIndex == null)
                    {
                        nullIndex = -1;
                    }
                }
            } else if (nullIndex > -1)
            {
                comboBox.setSelectedIndex(nullIndex);
                return;
            }
            
            if (isRequired && comboBox.getSelectedIndex() == -1 && adapter != null && adapter.getList() != null && adapter.getList().size() == 1)
            {
                fndInx = -1;
                valState = isRequired ? UIValidatable.ErrorType.Incomplete : UIValidatable.ErrorType.Valid;
                SwingUtilities.invokeLater(new Runnable() 
                {
                    @Override
                    public void run()
                    {
                        if (isNew)
                        {
                            valState = UIValidatable.ErrorType.Valid;
                            comboBox.setSelectedIndex(0);
                        }
                    }
                });
                
            } else
            {
                valState = isRequired && comboBox.getSelectedIndex() == -1 ? UIValidatable.ErrorType.Incomplete : UIValidatable.ErrorType.Valid;
                if (textEditor != null)
                {
                    textEditor.setText("");
                }
            }
        }
        
        comboBox.setSelectedIndex(fndInx);
        
        if (textEditor != null && fndInx == -1 && (value != null || defaultValue != null))
        {
            textEditor.setText(value != null ? value.toString() : defaultValue);
        }
        
        repaint();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.GetSetValueIFace#getValue()
     */
    @Override
    public Object getValue()
    {
        if (textEditor != null && StringUtils.isEmpty(textEditor.getText().trim()))
        {
            return null;
        }
        
        int inx = comboBox.getSelectedIndex();
        if (nullIndex != null && inx > -1 && inx == nullIndex)
        {
            return null;
        }
        
        Object selectedObj = comboBox.getSelectedItem();
        if (selectedObj != null)
        {
            if (comboBox.getModel() instanceof PickListDBAdapterIFace)
            {
                return selectedObj instanceof PickListItemIFace ? ((PickListItem)selectedObj).getValueObject() : selectedObj;
            }
        }
        return selectedObj;
    }

    //-------------------------------------------------
    // AppPrefsChangeListener
    //-------------------------------------------------

    @Override
    public void preferenceChange(final AppPrefsChangeEvent evt)
    {
        if (evt.getKey().equals("ui.formatting.requiredfieldcolor"))
        {
            if (textEditor != null)
            {
                textEditor.setBackground(isRequired && isEnabled() ? requiredFieldColor.getColor() : defaultTextBGColor);
            }
        }
    }
    

    //-------------------------------------------------
    // ClearableComboBox
    //-------------------------------------------------
    class ClearableComboBox extends JComboBox
    {
        public ClearableComboBox(ComboBoxModel model)
        {
            super(model);
            addKL();
        }
        
        /**
         * 
         */
        public ClearableComboBox()
        {
            super();
            addKL();
        }

        /**
         * @param items
         */
        public ClearableComboBox(Object[] items)
        {
            super(items);
            addKL();
        }

        /**
         * @param items
         */
        public ClearableComboBox(Vector<?> items)
        {
            super(items);
            addKL();
        }

        private void addKL()
        {
            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e)
                {
                    super.keyPressed(e);
                    checkKeyCode(e);
                }
            });
        }
        
        private void checkKeyCode(KeyEvent e)
        {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE || 
                e.getKeyCode() == KeyEvent.VK_DELETE || 
                e.getKeyCode() == KeyEvent.VK_BACK_SPACE)
            {
                 setSelectedIndex(-1);
            }
        }
    }
}
