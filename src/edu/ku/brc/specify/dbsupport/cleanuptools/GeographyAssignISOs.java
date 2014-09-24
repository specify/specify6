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

import static edu.ku.brc.specify.conversion.BasicSQLUtils.getCountAsInt;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.query;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.queryForRow;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.querySingleObj;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.update;
import static edu.ku.brc.ui.UIHelper.centerAndShow;
import static edu.ku.brc.ui.UIHelper.createCheckBox;
import static edu.ku.brc.ui.UIHelper.createComboBox;
import static edu.ku.brc.ui.UIHelper.createDuplicateJGoodiesDef;
import static edu.ku.brc.ui.UIHelper.createFormLabel;
import static edu.ku.brc.ui.UIRegistry.getAppDataDir;
import static edu.ku.brc.ui.UIRegistry.getTopWindow;
import static edu.ku.brc.ui.UIRegistry.showError;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.BooleanClause.Occur;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.conversion.TableWriter;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.GeographyTreeDef;
import edu.ku.brc.specify.dbsupport.BuildFromGeonames;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.ProgressFrame;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.AttachmentUtils;

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
public class GeographyAssignISOs
{
    private static final Logger  log = Logger.getLogger(GeographyAssignISOs.class);

    private Agent                      createdByAgent;
    
    private Connection                 readConn = null;
    private Connection                 updateConn;
    
    private int                        totalUpdated;
    
    //-------------------------------------------------
    // UI
    //-------------------------------------------------
    private JCheckBox                   continentsCBX;
    private JCheckBox                   countriesCBX;
    
    private JLabel                      spCountriesLbl;
    private JComboBox<?>                spCountriesCmbx;

    private JRadioButton               allCountriesRB;
    private JRadioButton               singleCountryRB;
    private ButtonGroup                btnGroup;
    
    private boolean      doStopProcessing = false;
    private boolean      doSkipCountry    = false;
    
    // Fix Geo UI
    private boolean doUpdateName = false;
    
    private boolean[] doAllCountries;
    private boolean[] doInvCountry;
    private Integer   doIndvCountryId = null;
    
    private Vector<Integer> countryIds = new Vector<Integer>();
    private Vector<GeoSearchResultsItem> countryInfo = new Vector<GeoSearchResultsItem>();
    private Vector<GeoSearchResultsItem> luceneResults = new Vector<GeoSearchResultsItem>();
    
    /**
     * Constructor.
     * @param geoDef
     * @param nowStr
     * @param createdByAgent
     * @param itUsername
     * @param itPassword
     * @param frame
     */
    public GeographyAssignISOs(final GeographyTreeDef   geoDef, 
                               final Agent              createdByAgent,
                               final ProgressFrame      frame)
    {
        super();
        //this.geoDef            = geoDef;
        this.createdByAgent    = createdByAgent;
        //this.frame             = frame;
        
    }
    
    /**
     * 
     */
    private void connectToDB()
    {
        DBConnection currDBConn = DBConnection.getInstance();
        if (updateConn == null) updateConn = currDBConn.createConnection();
        if (readConn == null) readConn = currDBConn.createConnection();
        
    }
    
    /**
     * @return
     */
    @SuppressWarnings("rawtypes")
    public boolean buildAsync(final int earthId)
    {
        continentsCBX = createCheckBox("All Continents"); // I18N

        CellConstraints cc  = new CellConstraints();
        PanelBuilder    pb1 = new PanelBuilder(new FormLayout("f:p:g", "p,4px,p,4px,p,8px"));
        countriesCBX  = createCheckBox("All Countries");
        pb1.add(countriesCBX,  cc.xy(1, 1));
        
        allCountriesRB  = new JRadioButton("Choose the Geography level to be processed");
        singleCountryRB = new JRadioButton("Choose an individual Country");
        btnGroup = new ButtonGroup();
        btnGroup.add(this.allCountriesRB);
        btnGroup.add(this.singleCountryRB);
        
        continentsCBX.setEnabled(false);
        continentsCBX.setSelected(true);
        
        countriesCBX.setEnabled(true);
        
        countryIds.clear();
        String sql = "SELECT g.GeographyID, g.Name, g2.Name FROM geography g LEFT JOIN geography g2 ON g.ParentID = g2.GeographyID " +
        	         "WHERE g.Name IS NOT NULL && LENGTH(g.Name) > 0 AND g.RankID = 200 AND g.GeographyTreeDefID = GEOTREEDEFID ORDER BY g.Name";
        sql = adjustSQL(sql);
        
        Vector<Object[]> rows   = query(sql);
        Object[]         titles = new Object[rows.size()+1];
        int i = 0;
        titles[i++] = "None"; // I18N
        countryIds.add(-1);
        for (Object[] r : rows)
        {
            countryIds.add((Integer)r[0]);
            String countryStr = (String)r[1];
            String contStr    = (String)r[2];
            titles[i++] = countryStr != null ? (countryStr + " (" + contStr + ")") : countryStr;
        }
        
        
        PanelBuilder    pb2 = new PanelBuilder(new FormLayout("8px,p,2px,f:p:g", "p,4px,p,4px,p,8px"));
        spCountriesLbl  = createFormLabel("Country");          // I18N
        spCountriesCmbx = createComboBox(titles);
        
        pb2.add(spCountriesLbl, cc.xy(2, 1));
        pb2.add(spCountriesCmbx, cc.xy(4, 1));
        
        spCountriesCmbx.setSelectedIndex(0);
        
        String          rowDef = createDuplicateJGoodiesDef("p", "4px", 7);
        PanelBuilder    pb     = new PanelBuilder(new FormLayout("16px,f:p:g", rowDef));
        
        pb.addSeparator("Continents to be processed", cc.xyw(1, 1, 2));
        pb.add(continentsCBX, cc.xyw(1, 3, 2));
        
        pb.addSeparator("Countries to be processed", cc.xyw(1, 5, 2));
        pb.add(allCountriesRB, cc.xyw(1, 7, 2));
        pb.add(pb1.getPanel(), cc.xyw(2, 9, 1));
        
        pb.add(singleCountryRB, cc.xyw(1, 11, 2));
        pb.add(pb2.getPanel(),  cc.xyw(2, 13, 1));
        
        pb.setDefaultDialogBorder();
        final CustomDialog dlg = new CustomDialog((Frame)getTopWindow(), "ISO Code Processing", true, pb.getPanel()); // I18N
       
        // Setup actions
        ChangeListener rbChangeListener = new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                radioSelected(dlg);
            }
        };
        allCountriesRB.addChangeListener(rbChangeListener);
        singleCountryRB.addChangeListener(rbChangeListener);
        
        countriesCBX.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                boolean isSel = countriesCBX.isSelected();
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
                dlg.getOkBtn().setEnabled(isSel || countriesCBX.isSelected());

            }
        });
        
        allCountriesRB.setSelected(true);
        
        dlg.createUI();
        dlg.getOkBtn().setEnabled(false);
        
        centerAndShow(dlg);
        if (dlg.isCancelled())
        {
            return false;
        }
        
        connectToDB();
        
//        doAllCountries  = new boolean[] {countriesCBX.isSelected(), stateCBX.isSelected(), countiesCBX.isSelected(), false};
//        doInvCountry    = new boolean[] {spCountriesCmbx.getSelectedIndex() > 0, spStatesCBX.isSelected(), spCountiesCBX.isSelected(), false};
//        doIndvCountryId = doInvCountry[0] ? countryIds.get(spCountriesCmbx.getSelectedIndex()) : null;
        return true;
    }
    
    private String adjustSQL(final String sql)
    {
        return QueryAdjusterForDomain.getInstance().adjustSQL(sql);
    }
    
    /**
     * @param dlg
     */
    private void radioSelected(final CustomDialog dlg)
    {
        boolean isAllCountries = this.allCountriesRB.isSelected();
       
        countriesCBX.setEnabled(isAllCountries);
        
        countriesCBX.setSelected(false);
        
        spCountriesLbl.setEnabled(!isAllCountries);
        spCountriesCmbx.setEnabled(!isAllCountries);
        
        if (dlg.getOkBtn() != null)
        {
            dlg.getOkBtn().setEnabled(false);
        }
    }
    
    /**
     * @param theRankId
     * @param parentNames
     * @param parentRanks
     * @return
     */
    private String getParentNameWithRank(final int theRankId, final String[] parentNames, final int[] parentRanks)
    {
        int i = 0;
        while (parentRanks[i] > 0)
        {
            if (parentRanks[i] == theRankId)
            {
                return parentNames[i];
            }
            i++;
        }
        return null;
    }
    

    
    /**
     * @param geoId
     * @param geonameId
     * @param rankId
     */
    private void updateGeography(final int      geoId, 
                                 final int      geonameId,
                                 final String[] parentNames,
                                 final String   countryCode)
    {
        boolean autoCommit = true;
            PreparedStatement pStmt   = null;
            try
            {
                autoCommit = updateConn.getAutoCommit();
                String sql = "UPDATE geography SET GeographyCode=?, ModifiedByAgentID=?, TimestampModified=? WHERE GeographyID=?";
                int inx     = 2; 
                
                if (pStmt != null)
                {
                    pStmt.setInt(inx,  createdByAgent.getId());
                    pStmt.setTimestamp(inx+1, new Timestamp(Calendar.getInstance().getTime().getTime()));
                    pStmt.setInt(inx+2,  geoId);
        
                    boolean isOK = true;
                    int rv = pStmt.executeUpdate();
                    if (rv != 1)
                    {
                        log.error("Error updating ");
                        isOK = false;
                    } else
                    {
                        //areNodesChanged = true; // Global indication that at least one node was updated.
                        totalUpdated++;
                    }
                    
                    
        
                    if (!autoCommit)
                    {
                        updateConn.commit();
                    }
                }
                
            } catch (SQLException e)
            {
                e.printStackTrace();
                
                if (!autoCommit)
                {
                    try
                    {
                        updateConn.rollback();
                    } catch (SQLException e1)
                    {
                        e1.printStackTrace();
                    }
                }
    
            } finally
            {
                // These need to be reset here, 
                // because this method is called sometimes automatically 
                doUpdateName = false;
                try
                {
                    if (pStmt != null) pStmt.close();
                } catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
    }
    

}
