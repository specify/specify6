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

import java.awt.BorderLayout;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.AbstractTableModel;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.ui.forms.Viewable;
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
    public FormValidatorInfo(final Viewable formViewObj)
    {
        model = new FormValidatorInfoModel(formViewObj);
        table = new JTable(model);
        
        formValidator = formViewObj.getValidator();
        
        setLayout(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
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
        protected String[]            header = {getResourceString("VAL_CONTROLSUBFORM_LABEL"), getResourceString("VAL_STATUS_LABEL")};
        
        /**
         * @param viewable
         */
        public FormValidatorInfoModel(final Viewable viewable) 
        {
            FormValidator validator = viewable.getValidator();
            
            for (FormValidator kidValidator : validator.getKids())
            {
                if (!kidValidator.isFormValid())
                {
                    rows.add(new Pair<String, String>(kidValidator.getName(), kidValidator.getState().toString()));
                }
            }
                
            for (DataChangeNotifier dcn : validator.getDCNs().values())
            {
                if (dcn.getUIV() != null)
                {
                    UIValidatable uval =  dcn.getUIV().getUIV();
                    if (uval != null && uval.getValidatableUIComp().isEnabled() && uval.getState() != UIValidatable.ErrorType.Valid)
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
