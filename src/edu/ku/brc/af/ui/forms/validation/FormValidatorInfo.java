/* Copyright (C) 2013, University of Kansas Center for Research
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

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.util.Pair;

/**
 * This JPanel contains a Table display for all the controls that are Incomplete or in Error on a form or subform.
 * It is a list of the FormValidator and updates on each change, so it is best to put this in a modal dialog
 *
 * @code_status Beta
 * 
 * @author rods
 *
 */
public class FormValidatorInfo extends JPanel
{

    protected JTable                 table;
    protected FormValidatorInfoModel model;
    protected FormValidator          formValidator;

    /**
     * CReate a FormValidator.
     * @param formViewObj the FormViewObj that will be validated
     */
    public FormValidatorInfo(final FormValidator formValidator)
    {
        model = new FormValidatorInfoModel(formValidator);
        table = new JTable(model);
        
        this.formValidator = formValidator;
        
        setLayout(new BorderLayout());
        add(UIHelper.createScrollPane(table), BorderLayout.CENTER);
    }
    
    /**
     * Unregisters the panel from the validator .
     */
    public void cleanUp()
    {
        formValidator = null;
    }
    
    //-----------------------------------------------------------
    //-- ValidationListener
    //-----------------------------------------------------------
    
    class FormValidatorInfoModel extends AbstractTableModel
    {
        protected Vector<Pair<String, String>> rows   = new Vector<Pair<String, String>>();
        protected String[]                     header = {getResourceString("VAL_CONTROLSUBFORM_LABEL"), getResourceString("VAL_STATUS_LABEL")};
        
        /**
         * @param viewable
         */
        public FormValidatorInfoModel(final FormValidator validator) 
        {
            for (FormValidator kidValidator : validator.getKids())
            {
                if (kidValidator.isRequired() && !kidValidator.isFormValid())
                {
                    rows.add(new Pair<String, String>(kidValidator.getName(), kidValidator.getState().toString()));
                }
            }
                
            for (DataChangeNotifier dcn : validator.getDCNs().values())
            {
                if (dcn.getUIV() != null)
                {
                    UIValidatable uval =  dcn.getUIV().getUIV();
                    if (uval != null && uval.getValidatableUIComp().isEnabled())
                    {
                        // Items that aren't required can be incomplete.
                        if ((uval.isRequired() && uval.getState() != UIValidatable.ErrorType.Valid) ||
                            (!uval.isRequired() && uval.getState() == UIValidatable.ErrorType.Error))
                        {
                            String titleStr = validator.getLabelTextForId(dcn.getId());
                            if (titleStr == null || titleStr.trim().length() == 0)
                            {
                                titleStr = uval.getValidatableUIComp().getClass().getSimpleName();
                            }
                            String reason = uval.getReason();
                            if (StringUtils.isEmpty(reason))
                            {
                                reason = uval.getState().toString();
                            }
                            rows.add(new Pair<String, String>(titleStr, reason));
                        }
                    }
                }
            }

        }

        public int getColumnCount()
        {
            return 2;
        }

        @Override
        public String getColumnName(int column)
        {
            return header[column];
        }

        public int getRowCount()
        {
            return rows.size();
        }

        public Object getValueAt(int row, int column)
        {
            Pair<String, String> item = rows.get(row);
            return column == 0 ? item.first : getResourceString(item.second.toString());
        }

        @Override
        public boolean isCellEditable(int row, int column)
        {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            return String.class;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {
            return;
        }

    }

}
