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
package edu.ku.brc.specify.config.init;

import javax.swing.JButton;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Aug 12, 2010
 *
 */
public class DivisionPanel extends GenericFormPanel
{

    /**
     * @param name
     * @param title
     * @param helpContext
     * @param labels
     * @param fields
     * @param lengths
     * @param nextBtn
     * @param prevBtnBtn
     * @param makeStretchy
     */
    public DivisionPanel(String name, String title, String helpContext, String[] labels,
            String[] fields, Integer[] lengths, JButton nextBtn, JButton prevBtnBtn,
            boolean makeStretchy)
    {
        super(name, title, helpContext, labels, fields, lengths, nextBtn, prevBtnBtn, makeStretchy);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.config.init.GenericFormPanel#isUIValid()
     */
    @Override
    public boolean isUIValid()
    {
        JTextField tf = (JTextField)comps.get("divName");
        if (tf != null)
        {
            String name = tf.getText();
            if (DBConnection.getInstance().getConnection() != null && StringUtils.isNotEmpty(name))
            {
                int cnt = BasicSQLUtils.getCountAsInt(String.format("SELECT COUNT(*) FROM division WHERE Name = '%s'", name));
                if (cnt > 0)
                {
                    UIRegistry.showLocalizedError("DIVNAME_DUP", name);
                    return false;
                }
                return true;
            } 
        }
        return false;
    }
}
