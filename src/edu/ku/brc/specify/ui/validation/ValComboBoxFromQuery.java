/* Filename:    $RCSfile: ValComboBoxFromQuery.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2006/01/16 19:59:54 $
 *
 * This library is free software; you can redistribute it and/or
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

package edu.ku.brc.specify.ui.validation;

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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.dbsupport.HibernateUtil;
import edu.ku.brc.specify.helpers.UIHelper;
import edu.ku.brc.specify.prefs.PrefsCache;
import edu.ku.brc.specify.ui.ColorWrapper;
import edu.ku.brc.specify.ui.GetSetValueIFace;
import edu.ku.brc.specify.ui.IconManager;
import edu.ku.brc.specify.ui.UICacheManager;
import edu.ku.brc.specify.ui.db.DialogFactory;
import edu.ku.brc.specify.ui.db.GenericDisplayFrame;
import edu.ku.brc.specify.ui.db.GenericSearchDialog;
import edu.ku.brc.specify.ui.db.JComboBoxFromQuery;
import edu.ku.brc.specify.ui.forms.DataGetterForObj;
import edu.ku.brc.specify.ui.forms.DataObjFieldFormatMgr;
import edu.ku.brc.specify.ui.forms.MultiView;


/**
 * This is a Validated Auto Complete combobox that is filled from a database table. It implements GetSetValueFace
 * and the set and get methods expect (and return) the Hibernate Object for the the table. When the user types
 * into the editable combobox it performs a case insensitive search against a single field. The display can be
 * constructed from multiple columns in the database. It is highly recommended that the first column be the same column
 * that is being searched. It is is unclear whether showing more columns than they can search on is a problem, this may
 * need to be addressed laster.<br><br>
 * The search looks like this:<br>
 * select distinct lastName,firstName,AgentID from agent where lower(lastName) like 's%' order by lastName asc
 *
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class ValComboBoxFromQuery extends JPanel implements UIValidatable, 
                                                            ListDataListener, 
                                                            GetSetValueIFace, 
                                                            PreferenceChangeListener, 
                                                            PropertyChangeListener
{
    protected static Log log = LogFactory.getLog(ValComboBoxFromQuery.class);
    
    protected static ColorWrapper valtextcolor       = null;
    protected static ColorWrapper requiredfieldcolor = null;

    protected UIValidatable.ErrorType valState  = UIValidatable.ErrorType.Valid;
    
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
    protected Class              classObj = null;
    protected DataGetterForObj   getter   = null;
    protected String             searchDialogName;
    protected String[]           fieldNames;
    protected Object             dataObj  = null;
    
    protected String             displayInfoDialogName;
    protected String             frameTitle = null;
    
    protected GenericDisplayFrame frame      = null;
    protected MultiView           multiView  = null;
    
    protected List<FocusListener> focusListeners = new ArrayList<FocusListener>();


    /**
     * @param sql the fully specified SQL statement with a "%s" in the string for the substitution for what the user entered
     * @param className the Class name of the java object that represents the table
     * @param idName the POJO field name of the ID column
     * @param keyName the POJO field name of the key column
     * @param format the format specification (null is OK if displayNames is null)
     * @param searchDialogName the name to look up to display the search dialog (from the search dialog factory)
     */
    public ValComboBoxFromQuery(final String sql,
                                final String className,
                                final String idName,
                                final String keyName,
                                final String format,
                                final String formatName,
                                final String searchDialogName,
                                final String displayInfoDialogName)
    {
        this.className        = className;
        this.idName           = idName;
        this.keyName          = keyName;
        this.format           = format;
        this.formatName = formatName;
        
        this.searchDialogName = searchDialogName;
        this.displayInfoDialogName = displayInfoDialogName;

        comboBox = new JComboBoxFromQuery(sql, format);
        
        init(false);
    }
    
    /**
     *  Constructor
     * @param tableName name of the table to be searched
     * @param idColumn the column name that contains the record ID
     * @param keyColumn the column that is searched
     * @param displayColumn a comma separated list of columns to be displayed and formatted by the format clause (null is OK)
     * @param className the Class name of the java object that represents the table
     * @param idName the POJO field name of the ID column
     * @param keyName the POJO field name of the key column
     * @param format the format specification (null is OK if displayNames is null)
     * @param searchDialogName the name to look up to display the search dialog (from the search dialog factory)
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
                                final String displayInfoDialogName)
    {
        if (displayColumn != null && format == null && formatName == null)
        {
            throw new RuntimeException("For ValComboBoxFromQuery table["+tableName+"] display is not null and the format is null.");
        }
        this.className  = className;
        this.idName     = idName;
        this.keyName    = keyName;
        this.format     = format;
        this.formatName = formatName;
        this.searchDialogName = searchDialogName;
        this.displayInfoDialogName = displayInfoDialogName;

        comboBox = new JComboBoxFromQuery(tableName, idColumn, keyColumn, displayColumn, format);

        init(false);
    }
    
    /* (non-Javadoc)
     * @see java.awt.Component#requestFocus()
     */
    public void requestFocus()
    {
        comboBox.requestFocus();
    }
    
    /* (non-Javadoc)
     * @see java.awt.Component#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        comboBox.setEnabled(enabled);
        if (searchBtn != null)
        {
            searchBtn.setEnabled(enabled);
        }
        editBtn.setEnabled(enabled);
        createBtn.setEnabled(enabled);

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.db.JAutoCompComboBox#init(boolean)
     */
    public void init(final boolean makeEditable)
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
        
        PanelBuilder    builder    = new PanelBuilder(new FormLayout("p,1px,p,1px,p"+(hasSearchBtn ? ",1px,p" : ""), "c:p"), this);
        CellConstraints cc         = new CellConstraints();

        builder.add(comboBox, cc.xy(1,1));

        int x = 3;
        editBtn = new JButton(IconManager.getIcon("EditForm", IconManager.IconSize.Std16));
        editBtn.setFocusable(false);
        editBtn.setMargin(new Insets(1,1,1,1));
        editBtn.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
        builder.add(editBtn, cc.xy(x,1));
        x += 2;
        
        createBtn = new JButton(IconManager.getIcon("CreateObj", IconManager.IconSize.Std16));
        createBtn.setFocusable(false);
        createBtn.setMargin(new Insets(1,1,1,1));
        createBtn.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
        builder.add(createBtn, cc.xy(x,1));
        x += 2;

        if (hasSearchBtn)
        {
            searchBtn = new JButton(IconManager.getIcon("Search", IconManager.IconSize.Std16));
            searchBtn.setFocusable(false);
            searchBtn.setMargin(new Insets(1,1,1,1));
            searchBtn.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
            builder.add(searchBtn, cc.xy(x,1));
            x += 2;
        }
        
        setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        comboBox.getModel().addListDataListener(this);

        bgColor = comboBox.getTextField().getBackground();
        if (valtextcolor == null || requiredfieldcolor == null)
        {
            valtextcolor = PrefsCache.getColorWrapper("ui", "formatting", "valtextcolor");
            requiredfieldcolor = PrefsCache.getColorWrapper("ui", "formatting", "requiredfieldcolor");
        }
        UICacheManager.getAppPrefs().node("ui/formatting").addPreferenceChangeListener(this);


        comboBox.getTextField().addFocusListener(new FocusAdapter()
                {
                    public void focusGained(FocusEvent e)
                    {
                        for (FocusListener l : focusListeners)
                        {
                            l.focusGained(e);
                        }  
                    }

                    public void focusLost(FocusEvent e)
                    {
                        isNew = false;
                        validateState();
                        repaint();
                        for (FocusListener l : focusListeners)
                        {
                            l.focusLost(e);
                        }
                    }
                });

        if (hasSearchBtn)
        {
            searchBtn.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    GenericSearchDialog dlg = DialogFactory.createSearchDialog(searchDialogName);
                    dlg.setVisible(true);
                    if (!dlg.isCancelled())
                    {
                        setValue(dlg.getSelectedObject());
                    }
    
                }
            });
        }
        
        editBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                createEditFrame(false);
            }});
        
        
        createBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                createEditFrame(true);
            }});

    }
    
    /**
     * Creates a Dialog (non-modl) that will display detail information 
     * for the object in the text field. 
     */
    protected void createEditFrame(final boolean isNewObject)
    {
        frame = DialogFactory.createDisplayDialog(displayInfoDialogName, frameTitle, isNewObject) ;
        
        if (isNewObject)
        {
            Object object = UIHelper.createAndNewDataObj(classObj);
            UIHelper.initAndAddToParent(multiView.getParentDataObj(), object);
            frame.setData(object);
            
        } else
        {
            frame.setData(dataObj);
        }
        frame.setCloseListener(this);
        
        frame.setVisible(true);
        
        if (multiView != null)
        {
            multiView.registerDisplayFrame(frame);
        }
    }
    
    /**
     * Sets the string that is preappended to the title
     * @param frameTitle the string arg
     */
    public void setFrameTitle(final String frameTitle)
    {
        this.frameTitle = frameTitle;
    }
    
    /**
     * Sets the MultiView parent into the control
     * @param multiView parent multiview
     */
    public void setMultiView(final MultiView multiView)
    {
        this.multiView = multiView; 
    }
    
    /**
     * Return the JComboBox for this control
     * @return the JComboBox for this control
     */
    public JComboBox getComboBox()
    {
        return comboBox;
    }

    /**
     * Returns the model for the combo box
     * @return the model for the combo box
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
    public void addFocusListener(FocusListener l)
    {
        focusListeners.add(l);
    }

    /* (non-Javadoc)
     * @see java.awt.Component#removeFocusListener(java.awt.event.FocusListener)
     */
    public void removeFocusListener(FocusListener l)
    {
        focusListeners.remove(l);
    }

    //--------------------------------------------------
    //-- UIValidatable Interface
    //--------------------------------------------------

    /* (non-Javadoc)
     * @see edu.kui.brc.specify.validation.UIValidatable#isInError()
     */
    public boolean isInError()
    {
        return valState != UIValidatable.ErrorType.Valid;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.validation.UIValidatable#getState()
     */
    public ErrorType getState()
    {
        return valState;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.validation.UIValidatable#setState(edu.ku.brc.specify.ui.validation.UIValidatable.ErrorType)
     */
    public void setState(ErrorType state)
    {
        this.valState = state;
    }
    /* (non-Javadoc)
     * @see edu.kui.brc.specify.validation.UIValidatable#isRequired()
     */
    public boolean isRequired()
    {
        return isRequired;
    }

    /* (non-Javadoc)
     * @see edu.kui.brc.specify.validation.UIValidatable#setRequired(boolean)
     */
    public void setRequired(boolean isRequired)
    {
        comboBox.getTextField().setBackground(isRequired && isEnabled() ? requiredfieldcolor.getColor() : bgColor);
        this.isRequired = isRequired;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.validation.UIValidatable#isChanged()
     */
    public boolean isChanged()
    {
        return isChanged;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.validation.UIValidatable#setChanged(boolean)
     */
    public void setChanged(boolean isChanged)
    {
        this.isChanged = isChanged;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.validation.UIValidatable#setAsNew(boolean)
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
     * @see edu.ku.brc.specify.ui.validation.UIValidatable#reset()
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
     * @see edu.ku.brc.specify.ui.validation.UIValidatable#getValidatableUIComp()
     */
    public Component getValidatableUIComp()
    {
        return this;
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
    }

    /* (non-Javadoc)
     * @see javax.swing.event.ListDataListener#intervalRemoved(javax.swing.event.ListDataEvent)
     */
    public void intervalRemoved(ListDataEvent e)
    {
    }


    //--------------------------------------------------------
    // GetSetValueIFace
    //--------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.GetSetValueIFace#setValue(java.lang.Object)
     */
    public void setValue(Object value)
    {
        dataObj = value;
        List<String> list = comboBox.getList();
        list.clear();

        if (value != null)
        {
            
            if (getter == null)
            {
                getter = new DataGetterForObj();
            }

            // NOTE: If there was a formatName defined for this then the value coming 
            // in will already be correctly formatted.
            // So just set the cvalue if there is a format name.
            Object newVal = value;
            if (isEmpty(formatName))
            {
                Object[] val = UIHelper.getFieldValues(fieldNames, value, getter);
                if (isNotEmpty(format))
                {
                    newVal = UIHelper.getFormattedValue(val, format);
                } else
                {
                    newVal = value;
                }
            } else
            {
                newVal = DataObjFieldFormatMgr.format(value, formatName);
            }
            
            if (newVal != null)
            {
                comboBox.getTextField().setCaretPosition(0);
                list.add(newVal.toString());
                comboBox.setSelectedIndex(0);
                valState = UIValidatable.ErrorType.Valid;
            } else
            {
                comboBox.setSelectedIndex(-1);
                valState = UIValidatable.ErrorType.Incomplete;
            }
        } else
        {
            comboBox.setSelectedIndex(-1);
            valState = UIValidatable.ErrorType.Incomplete;
        }
        repaint();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.GetSetValueIFace#getValue()
     */
    public Object getValue()
    {
        Object value = null;
        Integer id = comboBox.getSelectedId();
        if (id != null)
        {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Criteria criteria = session.createCriteria(classObj);
            criteria.add(Expression.eq(idName, id));
            List list = criteria.list();

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
    // PreferenceChangeListener
    //-------------------------------------------------

    public void preferenceChange(PreferenceChangeEvent evt)
    {
        if (evt.getKey().equals("requiredfieldcolor"))
        {
            comboBox.setBackground(isRequired && isEnabled() ? requiredfieldcolor.getColor() : bgColor);
        }
    }

    //--------------------------------------------------------
    // PropertyChangeListener
    //--------------------------------------------------------

    public void propertyChange(PropertyChangeEvent evt)
    {
        if (multiView != null)
        {
            multiView.unregisterDisplayFrame(frame);
        }
        frame = null;
    }
}
