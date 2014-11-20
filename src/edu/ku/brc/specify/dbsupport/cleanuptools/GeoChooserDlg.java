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
import static edu.ku.brc.specify.conversion.BasicSQLUtils.querySingleObj;
import static edu.ku.brc.ui.UIHelper.createCheckBox;
import static edu.ku.brc.ui.UIHelper.createFormLabel;
import static edu.ku.brc.ui.UIHelper.createI18NButton;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.createScrollPane;
import static edu.ku.brc.ui.UIHelper.createTextField;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Aug 26, 2012
 *
 */
public class GeoChooserDlg extends CustomDialog
{
    private static final Logger  log = Logger.getLogger(GeoChooserDlg.class);
    private static boolean isUpdateNamesChecked = false;
    
    private static final String kGeoLookUp = "SELECT GeographyCode FROM geography WHERE RankiD = %d AND LOWER(Name) = '%s'";
    
    private boolean[] doAllCountries;
    private boolean[] doInvCountry;
    
    private int        rankId;
    private int        level;
    private Connection readConn;
    private String[]   parentNames;
    private int[]      parentRanks;
    private Integer    geonameId;
    private String     nameStr;
    private int        geoTotal;
    private int        processedCount;
    
    private StateCountryContXRef stCntXRef;
    private Vector<GeoSearchResultsItem> countryInfo = new Vector<GeoSearchResultsItem>();
    
    private Vector<GeoSearchResultsItem> coInfoList = null;
    private HashMap<Integer, String> i18NLabelsMap = new HashMap<Integer, String>();
    private JCheckBox        updateNameCB;
    private JCheckBox        mergeCB;
    private JCheckBox        addISOCodeCB;
    private JTextField       isoCodeTF;
    private JProgressBar     progressBar = new JProgressBar();
    
    private JList<GeoSearchResultsItem>            mainList;
    private DefaultListModel<GeoSearchResultsItem> dataListModel;
    private boolean          noMatchesFound;
    
    private int              selectedIndex;

    /**
     * @param nameStr
     * @param rankId
     * @param level
     * @param parentNames
     * @param parentRanks
     * @param geonameId
     * @param stCntXRef
     * @param countryInfo
     * @param doAllCountries
     * @param doInvCountry
     * @param readConn
     * @param processedCount
     * @param geoTotal
     * @throws HeadlessException
     */
    public GeoChooserDlg(final String     nameStr,
                         final int        rankId,
                         final int        level,
                         final String[]   parentNames,
                         final int[]      parentRanks,
                         final Integer    geonameId,
                         final StateCountryContXRef stCntXRef,
                         final Vector<GeoSearchResultsItem> countryInfo,
                         boolean[]        doAllCountries,
                         boolean[]        doInvCountry,
                         final Connection readConn,
                         final int        processedCount,
                         final int        geoTotal) throws HeadlessException
    {
        super((Frame)UIRegistry.getTopWindow(), "Choose", true, OKCANCELHELP, null); // I18N
        
        this.nameStr        = nameStr;
        this.rankId         = rankId;
        this.level          = level;
        this.parentNames    = parentNames;
        this.parentRanks    = parentRanks;
        this.geonameId      = geonameId;
        this.stCntXRef      = stCntXRef;
        this.countryInfo    = countryInfo;
        this.doAllCountries = doAllCountries;
        this.doInvCountry   = doInvCountry;
        this.readConn       = readConn;
        this.geoTotal       = geoTotal;
        this.processedCount = processedCount;
        
        String sql = "SELECT Name, RankID FROM geographytreedefitem WHERE RankID IN (100,200,300,400) AND GeographyTreeDefID = GEOTREEDEFID ORDER BY RankID ASC";
        for (Object[] row : query(QueryAdjusterForDomain.getInstance().adjustSQL(sql)))
        {
            String  name   = (String)row[0];
            Integer rankID = (Integer)row[1];
            i18NLabelsMap.put(rankID, name);
        }
        if (i18NLabelsMap.containsKey(rankId))
        {
            setTitle("Choose "+i18NLabelsMap.get(rankId)); // I18N
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#buildButtonBar()
     */
    @Override
    protected JPanel buildButtonBar()
    {
        helpBtn.setText("Quit"); // I18N
        cancelBtn.setText("Skip");
        okBtn.setText("Save and Next");

        if (applyBtn != null)
        {
            applyBtn.setText("Skip To Next Country");
        }
        
        boolean isContinent = this.rankId == 100;
        if (isContinent)
        {
            whichBtns = OKCANCELHELP;
        }
        
        CellConstraints cc  = new CellConstraints();

        PanelBuilder pbLast = new PanelBuilder(new FormLayout("4px,p,8px,f:p:g,p,8px,p,8px,p,4px" + (whichBtns == OKCANCELAPPLYHELP ? ",p,8px" : ""), "p"));
        
        JButton theRealHelpBtn = UIHelper.createHelpIconButton("GeoCleanUpISOChooser");
        pbLast.add(theRealHelpBtn, cc.xy(2, 1));
        
        int inx = 5;
        pbLast.add(helpBtn,   cc.xy(inx, 1)); inx += 2;
        if (whichBtns == OKCANCELAPPLYHELP)
        {
            pbLast.add(applyBtn,  cc.xy(inx, 1)); inx += 2;
            setCloseOnApplyClk(true);
        }
        pbLast.add(cancelBtn, cc.xy(inx, 1)); inx += 2;
        pbLast.add(okBtn,     cc.xy(inx, 1)); inx += 2;
        
        setCloseOnHelpClk(true);
        
        return pbLast.getPanel();
    }
    
    /**
     * 
     */
    private void calcProgress()
    {
        if (progressBar != null)
        {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    int percent = geoTotal == 0 ? 0 : (int)(((double)processedCount*100.0) / (double)geoTotal);
                    progressBar.setValue(percent);
                    progressBar.repaint();
                }
            });
        }
    }
    
    /**
     * @param countryName
     * @param rankId
     * @return
     */
    private String getCountryISOCode(final String countryName, final int rankId)
    {
        String isoCode = stCntXRef.countryNameToCode(countryName);
        if (isoCode == null)
        {
            isoCode = querySingleObj(String.format(kGeoLookUp, rankId, countryName.toLowerCase()));
        }
        return isoCode;
    }

    /**
     * @param countryName
     * @param rankId
     * @return
     */
    private String getStateISOCode(final String stateName, final int rankId)
    {
        String isoCode = stCntXRef.stateNameToCode(stateName);
        if (isoCode == null)
        {
            isoCode = querySingleObj(String.format(kGeoLookUp, rankId, stateName));
        }
        return isoCode;
    }

    /**
     * @param theRankId
     * @return
     */
    private String getParentNameWithRank(final int theRankId)
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void createUI()
    {
        boolean doStatesOrCounties = doAllCountries[1] || doAllCountries[2] || doInvCountry[1] || doInvCountry[2];
        //this.whichBtns = doStatesOrCounties && !doInvCountry[1] && rankId > 200 ? CustomDialog.OKCANCELAPPLYHELP : CustomDialog.OKCANCELHELP;
        
        boolean isStCnty = true;//rankId > 200; 
        
        dataListModel      = new DefaultListModel<GeoSearchResultsItem>();
        mainList = new JList<GeoSearchResultsItem>(dataListModel);
        JScrollPane sb = createScrollPane(mainList, true);
        
        String listDim;
        if (UIHelper.isWindows())
        {
            listDim = "250px";
            Dimension sz = new Dimension(250, 250);
            mainList.setPreferredSize(sz);
            sb.setPreferredSize(sz);
        } else
        {
            listDim = "f:p:g";
        }
        
        CellConstraints cc  = new CellConstraints();
        PanelBuilder    pb  = new PanelBuilder(new FormLayout("f:p:g", "p,2px,p,12px,p,2px," + listDim + ",8px,p,4px,p,10px,p"+ (isStCnty ? ",8px,p" : "")));

        this.contentPanel = pb.getPanel();
        
        super.createUI();
        
        calcProgress();
        
        try
        {
            if (coInfoList != null && coInfoList.size() > 0)
            {
                fillFromLuceneResults();
            } else
            {
                fillFromQuery();
            }
            
            mainList.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseClicked(MouseEvent e) 
                {
                    if (e.getClickCount() == 2)
                    {
                        getOkBtn().doClick();
                        
                    } else if (e.getClickCount() == 1 && !noMatchesFound && !mainList.isSelectionEmpty())
                    {
                        getOkBtn().setEnabled(true);
                    }
                }
            });
            mainList.getSelectionModel().addListSelectionListener(new ListSelectionListener()
            {
                @Override
                public void valueChanged(ListSelectionEvent e)
                {
                    if (!e.getValueIsAdjusting()) 
                    {
                        listItemSelected();
                    }
                }
            });
            
            updateNameCB = createCheckBox("Update the Name in the Geography tree.");    // I18N
            //mergeCB      = createCheckBox("Merge all the geographies with the same name.");
            addISOCodeCB = createCheckBox("Add the ISO Code to the record");
            isoCodeTF    = createTextField(8);
            isoCodeTF.setVisible(rankId < 400);
    
            updateNameCB.setSelected(isUpdateNamesChecked);
            //mergeCB.setSelected(true);
            addISOCodeCB.setSelected(true);
            
            updateNameCB.addChangeListener(new ChangeListener()
            {
                @Override
                public void stateChanged(final ChangeEvent e)
                {
                    isUpdateNamesChecked = updateNameCB.isSelected();
                }
            });
            
            //labels.add(nameStr);// + "  (Unknown)");
            
            PanelBuilder lookPB    = null;
            JButton      lookupBtn = null;
            if (isStCnty)
            {
                lookPB    = new PanelBuilder(new FormLayout("f:p:g,p", "p"));
                lookupBtn = createI18NButton("CLNUP_GEO_LOOK_UP_ISO");
                lookPB.add(lookupBtn, cc.xy(2,1));
                lookupBtn.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        globalRankSearch();
                    }
                });
            }
            
            int i = 0;
            ArrayList<String> labels = new ArrayList<String>();
            while (i < parentNames.length && parentRanks[i] > -1)
            {
                labels.add(i18NLabelsMap.get(parentRanks[i++])); 
            }

            PanelBuilder pbTop  = new PanelBuilder(new FormLayout("p,2px,f:p:g", UIHelper.createDuplicateJGoodiesDef("p", "2px", labels.size())));
            int y = 1;
            for (i=0;i<labels.size();i++)
            {
                JLabel lbl = createLabel(parentNames[i]);
                pbTop.add(createFormLabel(labels.get(i)), cc.xy(1, y));
                pbTop.add(lbl,     cc.xy(3, y));
                lbl.setBackground(Color.WHITE);
                lbl.setOpaque(true);
                y += 2;
            }
            
            pb.add(pbTop.getPanel(), cc.xy(1, 3));
            pb.addSeparator("Possible standard Geography choices", cc.xy(1, 5)); // I18N
            pb.add(sb,               cc.xy(1, 7));
            pb.add(updateNameCB,     cc.xy(1, 9));
            
            PanelBuilder pbc  = new PanelBuilder(new FormLayout("p,10px,p,f:p:g", "p"));
            pbc.add(addISOCodeCB,     cc.xy(1, 1));
            pbc.add(isoCodeTF,        cc.xy(3, 1));
            
            pb.add(pbc.getPanel(),   cc.xy(1, 11));
            
            i = 13;
            if (isStCnty) 
            {
                pb.add(lookPB.getPanel(), cc.xy(1, i));
                i += 2;
            }
            
            //if (doAllCountries[0])
            if (false) // hidding it for now
            {
                progressBar = new JProgressBar(0, 100);
                progressBar.setStringPainted(true);
                PanelBuilder prgPB = new PanelBuilder(new FormLayout("p,2px,f:p:g","p"));
                prgPB.add(createFormLabel("Progress"), cc.xy(1, 1));
                prgPB.add(progressBar, cc.xy(3, 1));
                pb.add(prgPB.getPanel(), cc.xy(1, i));
                i += 2;
            }
            
            mainList.setSelectedIndex(selectedIndex);
            mainList.ensureIndexIsVisible(selectedIndex);
            
            noMatchesFound = dataListModel.size() == 0;
            
            // Optional Depending on States / Countries
            if (doStatesOrCounties)
            {
                if (dataListModel.getSize() == 0)
                {
                    dataListModel.addElement(new GeoSearchResultsItem("No matches found."));// I18N
                }
            }

            pb.setDefaultDialogBorder();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        
        if (UIHelper.isWindows())
        {
            setResizable(false);
        }
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE); // Must be called at the end 'createUI'
    }
    
    /**
     * @param coInfoList the coInfoList to set
     */
    public void setCoInfoList(final Vector<GeoSearchResultsItem> coInfoList)
    {
        this.coInfoList = coInfoList;
    }

    /**
     * @return
     * @throws Exception
     */
    private String fillFromLuceneResults() throws Exception
    {
        Collections.sort(coInfoList, new Comparator<GeoSearchResultsItem>()
        {
            @Override
            public int compare(GeoSearchResultsItem p1, GeoSearchResultsItem p2)
            {
                return p1.name.compareTo(p2.name);
            }
        });
        
        int inx = -1;
        int i = 0;
        for (GeoSearchResultsItem item : coInfoList)
        {
             dataListModel.addElement(item);
             if (geonameId != null && geonameId.equals(item.geonameId))
             {
                 inx = i; 
             }
             i++;
        }
        if (inx > -1)
        {
            selectedIndex = inx;
        }

        return null;
    }
    
    /**
     * @return
     * @throws Exception
     */
    private String fillFromQuery() throws Exception
    {
        // Geography     Level
        // Continent       0
        // Country         1
        // State           2
        // County          3
        
        String geoName   = null;
        String selectStr = "SELECT geonameId, asciiname, ISOCode FROM geoname ";
        String whereStr  = "";
        String orderStr  = " ORDER BY asciiname";
        
        switch (rankId)
        {
            case 100:
                whereStr  = String.format("WHERE fcode = 'CONT'");
                break;
                
            case 200:
                selectStr = "SELECT geonameId, name, iso_alpha2 FROM countryinfo ";
                orderStr  = "ORDER BY name";
                geoName   = parentNames[level];
                break;
                
            case 300: {
                String countryName = getParentNameWithRank(200);
                if (countryName != null)
                {
                    String countryCode = getCountryISOCode(countryName, 200);
                    whereStr = String.format("WHERE fcode = 'ADM1' AND country = '%s'", countryCode);
                    geoName = parentNames[level];
                }
            } break;
                
            case 400: {// County
                String stname = getParentNameWithRank(300);
                if (stname != null)
                {
                    String stateCode = getStateISOCode(stname, 300);
                    if (stateCode != null)
                    {
                        whereStr = String.format("WHERE fcode = 'ADM2' AND admin1 = '%s'", stateCode.length() == 4 ? stateCode.substring(2) : stateCode);
                    } else
                    {
                        System.err.println("Missing state code["+stname+"]");
                    }
                    geoName = parentNames[level];
                }
            } break;
        }
        
        whereStr += orderStr;
        
        int  inx  = -1; // geonameId
        int  inx1 = -1; // first letter
        int  inx2 = -1; // first two letters
        int  i    = 0;
        
        if (rankId == 100 || rankId > 200)
        {
            Statement stmt = readConn.createStatement();
            coInfoList = new Vector<GeoSearchResultsItem>();
            
            String sql = selectStr + whereStr;
            log.debug(sql);
            ResultSet rs   = stmt.executeQuery(sql);
            while (rs.next())
            {
                String name = rs.getString(2);
                if (StringUtils.isNotEmpty(name))
                {
                    coInfoList.add(new GeoSearchResultsItem(name, rs.getInt(1), rs.getString(3)));
                }
            }
            rs.close();
            stmt.close();
        } else
        {
            coInfoList = countryInfo;
        }
        
        Collections.sort(coInfoList, new Comparator<GeoSearchResultsItem>()
        {
            @Override
            public int compare(GeoSearchResultsItem p1, GeoSearchResultsItem p2)
            {
                return p1.name.compareTo(p2.name);
            }
        });
        
        selectedIndex = -1;
        String currentName = parentNames[this.level];
        i = 0;
        for (GeoSearchResultsItem item : coInfoList)
        {
            if (selectedIndex == -1 && item.name.contains(currentName))
            {
                selectedIndex = i;
            }
            i++;
        }
        
        char   firstChar = nameStr.charAt(0);
        String twoChars  = nameStr.length() > 1 ? nameStr.substring(0, 2) : nameStr;
                
        for (GeoSearchResultsItem p : coInfoList)
        {
            String name = p.name;
            char   fc   = StringUtils.isNotEmpty(name) ? name.charAt(0) : ' ';
            String cmp  = name.length() > 1 ? name.substring(0, 2) : null;
            dataListModel.addElement(p);
            
            if (inx == -1)
            {
                if (name.equals(geoName))
                {
                    inx = i;
                } else if (geonameId != null && p.geonameId == geonameId)
                {
                    inx = i;
                }
            } 
            
            if (inx1 == -1 && fc == firstChar)
            {
                inx1 = i;
            } else if (inx1 == -1 && fc == firstChar)
            {
                inx1 = i;
            }
            
            if (inx2 == -1 && cmp != null && cmp.equals(twoChars))
            {
                inx2 = i;
            }
            i++;
        }
        if (selectedIndex == -1)
        {
            selectedIndex = inx > -1 ? inx : (inx2 > -1 ? inx2 : inx1);
        }
        return geoName;
    }
    
    /**
     * 
     */
    private void listItemSelected()
    {
        int inx = mainList.getSelectedIndex();
        if (inx > -1)
        {
            GeoSearchResultsItem item = mainList.getSelectedValue();
            isoCodeTF.setText(item.isoCode);
        }
    }
    
    /**
     * @param parentId
     * @param geoName
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void globalRankSearch()
    {
        ISOCodeListDlg dlg = new ISOCodeListDlg(null, OKCANCELHELP);
        dlg.setAlwaysOnTop(true);
        UIHelper.centerAndShow(dlg);
        if (!dlg.isCancelled())
        {
            GeoSearchResultsItem selectedItem = dlg.getSelectedItem();
            if (selectedItem != null)
            {
                dataListModel.insertElementAt(selectedItem, 0);
                mainList.setSelectedIndex(0);
            }
        }
    }
    
    /**
     * @return the updateNameCB
     */
    public JCheckBox getUpdateNameCB()
    {
        return updateNameCB;
    }

    /**
     * @return the mergeCB
     */
    public JCheckBox getMergeCB()
    {
        return mergeCB;
    }

    /**
     * @return the addISOCodeCB
     */
    public JCheckBox getAddISOCodeCB()
    {
        return addISOCodeCB;
    }
    
    /**
     * @return
     */
    public GeoSearchResultsItem getSelectedGeoSearchItem()
    {
        return mainList.getSelectedValue();
    }
    
    /**
     * @return
     */
    public String getISOCodeFromTextField()
    {
        return isoCodeTF.getText();
    }

    /**
     * @return
     */
    public Integer getSelectedGeonameId()
    {
        int     selInx    = mainList.getSelectedIndex();
        if (selInx > -1 && coInfoList.size() > 0)
        {
            return coInfoList.get(mainList.getSelectedIndex()).geonameId;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#helpButtonPressed()
     */
    @Override
    protected void helpButtonPressed() // Acts Like Cancel Button which Means Quit
    {
        isCancelled = true;
        btnPressed  = HELP_BTN;
        setVisible(false);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#cancelButtonPressed()
     */
    @Override
    protected void cancelButtonPressed() // Acts like the Skip Button or Skip Country Button
    {
        isCancelled = false;
        btnPressed  = whichBtns == OKCANCELAPPLYHELP ? APPLY_BTN : CANCEL_BTN;
        setVisible(false);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#cancelButtonPressed()
     */
    @Override
    protected void applyButtonPressed() // Acts like the normal Skip Button when there
    {
        isCancelled = false;
        btnPressed  = CANCEL_BTN;
        setVisible(false);
    }
}
