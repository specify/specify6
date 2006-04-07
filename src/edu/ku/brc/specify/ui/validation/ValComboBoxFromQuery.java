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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.criterion.Expression;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.specify.dbsupport.HibernateUtil;
import edu.ku.brc.specify.prefs.PrefsCache;
import edu.ku.brc.specify.ui.ColorWrapper;
import edu.ku.brc.specify.ui.GetSetValueIFace;
import edu.ku.brc.specify.ui.IconManager;
import edu.ku.brc.specify.ui.UICacheManager;
import edu.ku.brc.specify.ui.db.GenericSearchDialog;
import edu.ku.brc.specify.ui.db.JComboBoxFromQuery;
import edu.ku.brc.specify.ui.db.SearchDialogFactory;
import edu.ku.brc.specify.ui.forms.DataGetterForObj;


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
public class ValComboBoxFromQuery extends JPanel implements UIValidatable, ListDataListener, GetSetValueIFace, PreferenceChangeListener
{
    protected static Log log = LogFactory.getLog(ValComboBoxFromQuery.class);
            
    protected boolean            isInError  = false;
    protected boolean            isRequired = false;
    protected boolean            isChanged  = false;
    protected Color              bgColor    = null;
    
    protected JComboBoxFromQuery comboBox;
    protected JButton            searchBtn  = null;
    protected String             className;
    protected String             idName;
    protected String             keyName;
    protected String             format;
    protected Class              classObj = null;
    protected DataGetterForObj   getter   = null;
    protected String             searchDialogName;
    
    protected static ColorWrapper valtextcolor       = null;
    protected static ColorWrapper requiredfieldcolor = null;

    /**
     *  Constructor
     * @param tableName name of the table to be searched
     * @param idColumn the column name that contains the record ID 
     * @param keyColumn the column that is searched
     * @param displayColumn a comma separated list of columns to be displayed and formatted by the format clause (null is OK)
     * @param className the Class name of the java object that represents the table
     * @param idName the POJO field name of the ID column 
     * @param keyName the POJO field name of the ke column
     * @param format the format specification (null is OK if displayNames is null)
     */
    public ValComboBoxFromQuery(final String tableName, 
                                final String idColumn, 
                                final String keyColumn,
                                final String displayColumn,
                                final String className,
                                final String idName,
                                final String keyName,
                                final String format,
                                final String searchDialogName)
    {
        if (displayColumn != null && format == null)
        {
            throw new RuntimeException("For ValComboBoxFromQuery table["+tableName+"] display is not null and the format is null.");
        }
        this.className = className;
        this.idName    = idName;
        this.keyName   = keyName;
        this.format   = format;
        this.searchDialogName = searchDialogName;
        
        comboBox = new JComboBoxFromQuery(tableName, idColumn, keyColumn, displayColumn, format);
        try
        {
            classObj = Class.forName(className);
            
        } catch (ClassNotFoundException ex)
        {
           log.error(ex);
           throw new RuntimeException(ex);
        }
        init(false);
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.db.JAutoCompComboBox#init(boolean)
     */
    public void init(final boolean makeEditable)
    {
        //setLayout(new BorderLayout());
        //add(comboBox, BorderLayout.CENTER);
        
        PanelBuilder    builder    = new PanelBuilder(new FormLayout("p,1px,p", "b:p"), this);
        CellConstraints cc         = new CellConstraints();

        builder.add(comboBox, cc.xy(1,1));
        
        if (StringUtils.isNotEmpty(searchDialogName))
        {
            searchBtn = new JButton(IconManager.getIcon("Search", IconManager.IconSize.Std8));
            searchBtn.setMargin(new Insets(1,1,1,1));
            builder.add(searchBtn, cc.xy(3,1));
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

        
        comboBox.getTextField().addFocusListener(new FocusListener() 
                {
                    public void focusGained(FocusEvent e) {}
                    public void focusLost(FocusEvent e) 
                    {
                        isInError = comboBox.getSelectedIndex() == -1;
                        repaint();
                    }
                    
                });
        
        searchBtn.addActionListener(new ActionListener() 
        {
            public void actionPerformed(ActionEvent e)
            {
                GenericSearchDialog dlg = SearchDialogFactory.createDialog(searchDialogName);
                dlg.setVisible(true);
                if (!dlg.isCancelled())
                {
                    setValue(dlg.getSelectedObject());    
                }
                
            }
        });
        
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

        if (isInError() && isEnabled())
        {
            Dimension dim = getSize();
            g.setColor(valtextcolor.getColor());
            g.drawRect(0, 0, dim.width-1, dim.height-1);
        }
    }

    //--------------------------------------------------
    //-- UIValidatable Interface
    //--------------------------------------------------

    /* (non-Javadoc)
     * @see edu.kui.brc.specify.validation.UIValidatable#isInError()
     */
    public boolean isInError()
    {
        return isInError;
    }

    /* (non-Javadoc)
     * @see edu.kui.brc.specify.validation.UIValidatable#setInError(boolean)
     */
    public void setInError(boolean isInError)
    {
        this.isInError = isInError;
        repaint();

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

    //--------------------------------------------------------
    // ListDataListener (JComboxBox)
    //--------------------------------------------------------

    /* (non-Javadoc)
     * @see javax.swing.event.ListDataListener#contentsChanged(javax.swing.event.ListDataEvent)
     */
    public void contentsChanged(ListDataEvent e)
    {
        isChanged = true;
        isInError = isRequired && comboBox.getSelectedIndex() != -1;
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
        List<String> list= comboBox.getList();
        list.clear();        
        if (value != null)
        {
    
            if (getter == null)
            {
                getter = new DataGetterForObj();
            }
            
            Object val = getter.getFieldValue(value, keyName, null, format);
          
            if (val != null)
            {
                list.add(val.toString());
                comboBox.setSelectedIndex(0);
                isInError = false;
            } else
            {
                comboBox.setSelectedIndex(-1);
                isInError = true;
            }
        } else
        {
            comboBox.setSelectedIndex(-1);
            isInError = true;
        }
        repaint();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.GetSetValueIFace#getValue()
     */
    public Object getValue()
    {
        Integer id = comboBox.getSelectedId();
        if (id != null)
        {
            Criteria criteria = HibernateUtil.getCurrentSession().createCriteria(classObj);
            criteria.add(Expression.eq(idName, id));
            List list = criteria.list();
            
            if (list.size() != 0)
            {
                return list.get(0);
            } else
            {
                log.error("**** Can't find the Object "+classObj+" with ID: "+id);
            }
        }

        return null;
    }
    
    //-------------------------------------------------
    // PreferenceChangeListener
    //-------------------------------------------------

    public void preferenceChange(PreferenceChangeEvent evt)
    {
        if (evt.getKey().equals("requiredfieldcolor"))
        {
            setBackground(isRequired && isEnabled() ? requiredfieldcolor.getColor() : bgColor);
        }
    }

}
