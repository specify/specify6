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

package edu.ku.brc.ui.validation;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import edu.ku.brc.ui.forms.FormViewObj;
import edu.ku.brc.ui.forms.Viewable;

/**
 * This JPanel contains a Table display for all the controls that are Incomplete or in Error on a form or subform.
 * It is a list of the FormValidator and updates on each change, so it is best to put this in a modal dialog
 *
 * @code_status Unknown (auto-generated)
 * 
 * @author rods
 *
 */
public class FormValidatorInfo extends JPanel implements ValidationListener
{

    protected JTable                 table;
    protected FormValidatorInfoModel model;
    protected FormValidator          formValidator;

    /**
     * CReate a FormValidator.
     * @param name the name of the validator (mostly for debug purposes)
     * @param formViewObj the FormViewObj that will be validated
     */
    public FormValidatorInfo(final String name, final FormViewObj formViewObj)
    {
        
        model = new FormValidatorInfoModel(formViewObj);
        table = new JTable(model);
        
        formValidator = formViewObj.getValidator();
        formValidator.addValidationListener(this);
        
        setLayout(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);

    }
    
    /**
     * Unregisters the panel from the validator .
     */
    public void cleanUp()
    {
        formValidator.removeValidationListenerListener(this);
        formValidator = null;
    }
    
    //-----------------------------------------------------------
    //-- ValidationListener
    //-----------------------------------------------------------
    /* (non-Javadoc)
     * @see ValidationListener#wasValidated(UIValidator)
     */
    public void wasValidated(final UIValidator validator)
    {
        model.updateModel(validator);
        model.fireTableDataChanged();
        table.repaint();
    }
    
    //-----------------------------------------------------------
    //--
    //-----------------------------------------------------------
    class ControlInfo 
    {
        protected String label;
        protected UIValidatable uiv;
        
        public ControlInfo(String label, UIValidatable uiv)
        {
            this.label = label;
            this.uiv = uiv;
        }
        public String getLabel()
        {
            return label;
        }
        public String getStateStr()
        {
            return uiv.getState().toString();
        }
        public UIValidatable.ErrorType getState()
        {
            return uiv.getState();
        }
    }



    class FormValidatorInfoModel extends AbstractTableModel
    {
        protected List<ControlInfo> rows   = new ArrayList<ControlInfo>();
        protected String[]          header = {"Control", "Status"}; // XXX I18N
        
        protected int[]             rowInxMap;
        protected int               rowCnt = 0;
        
        public FormValidatorInfoModel(final Viewable viewable) 
        {
            FormValidator validator = viewable.getValidator();

            Hashtable<String, String> labelsMap = new Hashtable<String, String>();
            List<String>              ids       = new ArrayList<String>();
            viewable.getFieldIds(ids);
            rowInxMap = new int[ids.size()];
            
            for (String id : ids)
            {
                labelsMap.put(id, validator.getLabelTextForId(id));
                //System.out.println("["+id+"]["+validator.getLabelTextForId(id)+"]");
            }
                
            int inx = 0;
            for (DataChangeNotifier dcn : validator.getDCNs().values())
            {
                if (dcn.getUIV() != null)
                {
                    //System.out.println("["+dcn.getId()+"]");
                    ControlInfo ci = new ControlInfo(validator.getLabelTextForId(dcn.getId()), dcn.getUIV().getUIV());
                    if (ci.getState() != UIValidatable.ErrorType.Valid)
                    {
                        rowInxMap[rowCnt++] = inx;
                    }
                    rows.add(ci);
                    inx++;
                }
            }

        }
        
        public void updateModel(final UIValidator validator)
        {
            // Not optimzied
            int inx = 0;
            rowCnt  = 0;
            for (ControlInfo ci : rows)
            {
                if (ci.getState() != UIValidatable.ErrorType.Valid)
                {
                    rowInxMap[rowCnt++] = inx;
                }
                inx++;
            }
        }

        public int getColumnCount()
        {
            return 2;
        }

        public String getColumnName(int column)
        {
            return header[column];
        }

        public int getRowCount()
        {
            //return rows.size();
            return rowCnt;
        }

        public Object getValueAt(int row, int column)
        {
            ControlInfo ci = rows.get(rowInxMap[row]);
            return column == 0 ? ci.getLabel() : ci.getState();
        }

        public boolean isCellEditable(int row, int column)
        {
            return false;
        }

        public Class<?> getColumnClass(int columnIndex)
        {
            return String.class;
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex)
        {
            return;
        }

    }

}
