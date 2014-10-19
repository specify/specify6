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
package edu.ku.brc.specify.dbsupport.cleanuptools;

import static edu.ku.brc.specify.conversion.BasicSQLUtils.query;
import static edu.ku.brc.ui.UIHelper.centerAndShow;
import static edu.ku.brc.ui.UIHelper.createCheckBox;
import static edu.ku.brc.ui.UIHelper.createComboBox;
import static edu.ku.brc.ui.UIHelper.createFormLabel;
import static edu.ku.brc.ui.UIHelper.createI18NLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.getTopWindow;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIRegistry;

/**
 * Can't use PrepareStatment because of MySQL boolean bit issue.
 * 
 * @author rods
 *
 * @code_status Beta
 *
 * Aug 16, 2009
 *
 */
public class CountryClearProcesser
{
    //private static final Logger  log = Logger.getLogger(ChooseGeography.class);

    @SuppressWarnings("unused")
    private Agent                      createdByAgent;
    
    //-------------------------------------------------
    // UI
    //-------------------------------------------------
    private JCheckBox                   countriesCBX;
    
    private JLabel                      spCountriesLbl;
    private JComboBox<?>                spCountriesCmbx;
    
    // Fix Geo UI
    private Vector<Integer> countryIds = new Vector<Integer>();
    
    /**
     * Constructor.
     * @param geoDef
     * @param nowStr
     * @param createdByAgent
     * @param itUsername
     * @param itPassword
     * @param frame
     */
    public CountryClearProcesser(final GeographyTreeDef geoDef, 
                           final Agent            createdByAgent)
    {
        super();
    }
    
    /**
     * @return
     */
    @SuppressWarnings("rawtypes")
    public boolean start()
    {
        CellConstraints cc  = new CellConstraints();
        countriesCBX  = createCheckBox(getResourceString("CLNUP_GEO_ALL_COUNTRIES"));
        countriesCBX.setEnabled(true);
        
        countryIds.clear();
        String sql = "SELECT g.GeographyID, g.Name, g2.Name FROM geography g LEFT JOIN geography g2 ON g.ParentID = g2.GeographyID " +
        	         "WHERE g.Name IS NOT NULL && LENGTH(g.Name) > 0 AND g.RankID = 200 AND g.GeographyTreeDefID = GEOTREEDEFID ORDER BY g.Name";
        sql = adjustSQL(sql);
        
        Vector<Object[]> rows   = query(sql);
        Object[]         titles = new Object[rows.size()+1];
        int i = 0;
        titles[i++] = getResourceString("CLNUP_GEO_NONE");
        countryIds.add(-1);
        for (Object[] r : rows)
        {
            countryIds.add((Integer)r[0]);
            String countryStr = (String)r[1];
            String contStr    = (String)r[2];
            titles[i++] = countryStr != null ? (countryStr + " (" + contStr + ")") : countryStr;
        }
        
        sql = String.format("SELECT Name FROM geographytreedefitem WHERE GeographyTreeDefID = GEOTREEDEFID AND RankID = %d", 200);
        String countryName = BasicSQLUtils.querySingleObj(adjustSQL(sql));
        
        spCountriesLbl  = createFormLabel(countryName);
        spCountriesCmbx = createComboBox(titles);
        
        spCountriesCmbx.setSelectedIndex(0);
        
        PanelBuilder    pb     = new PanelBuilder(new FormLayout("p,2px,f:p:g", "p,4px,p,4px,p,4px,p"));
        
        JLabel label = createI18NLabel("CLNUP_GEO_CLEAR_DESC");
        pb.add(label,           cc.xyw(1, 1, 3)); 
        pb.add(countriesCBX,    cc.xyw(1, 3, 3));
        pb.add(spCountriesLbl,  cc.xy(1, 5));
        pb.add(spCountriesCmbx, cc.xy(3, 5));  
        
        pb.setDefaultDialogBorder();
        final CustomDialog dlg = new CustomDialog((Frame)getTopWindow(), getResourceString("CLNUP_GEO_CLEAR_TITLE"), true, pb.getPanel()); // I18N
       
        countriesCBX.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                boolean isSel = countriesCBX.isSelected();
                spCountriesCmbx.setEnabled(!isSel);
                spCountriesLbl.setEnabled(!isSel);
                dlg.getOkBtn().setEnabled(isSel || spCountriesCmbx.getSelectedIndex() > 0);
            }
        });
        
        
        // Special
        spCountriesCmbx.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                boolean isSel = spCountriesCmbx.getSelectedIndex() > 0;
                countriesCBX.setEnabled(!isSel);
                
                dlg.getOkBtn().setEnabled(isSel || countriesCBX.isSelected());
            }
        });
        
        dlg.setOkLabel(getResourceString("Clear"));
        dlg.createUI();
        dlg.getOkBtn().setEnabled(false);
        
        centerAndShow(dlg);
        if (!dlg.isCancelled())
        {
            clearISOCodes();
        }
        return true;
    }
    
    private String adjustSQL(final String sql)
    {
        return QueryAdjusterForDomain.getInstance().adjustSQL(sql);
    }
    
    private void clearISOCodes()
    {
        @SuppressWarnings("unused")
        int total = 0;
        if (countriesCBX.isSelected())
        {
            String updateSQL = adjustSQL("UPDATE geography SET GeographyCode=NULL WHERE GeographyTreeDefID = GEOTREEDEFID AND RankID > 100");
            total = BasicSQLUtils.update(updateSQL);
        } else
        {
            Integer  selectedGeoID = countryIds.get(spCountriesCmbx.getSelectedIndex());
            String   sql       = adjustSQL("SELECT HighestChildNodeNumber, NodeNumber FROM geography WHERE GeographyID = " + selectedGeoID);
            Object[] row       = BasicSQLUtils.queryForRow(sql);
            String   updateSQL = adjustSQL(String.format("UPDATE geography SET GeographyCode=NULL WHERE NodeNumber >= %d AND NodeNumber <= %d", (Integer)row[1],(Integer)row[0]));
            total = BasicSQLUtils.update(updateSQL);
        }
        UIRegistry.writeTimedSimpleGlassPaneMsg(getResourceString("CLNUP_GEO_CLEAR_RESULTS"), 4000, true);
    }
}
