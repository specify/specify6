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
import static edu.ku.brc.ui.UIHelper.centerAndShow;
import static edu.ku.brc.ui.UIHelper.createCheckBox;
import static edu.ku.brc.ui.UIHelper.createFormLabel;
import static edu.ku.brc.ui.UIHelper.createI18NButton;
import static edu.ku.brc.ui.UIHelper.createI18NFormLabel;
import static edu.ku.brc.ui.UIHelper.createI18NLabel;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIHelper.createScrollPane;
import static edu.ku.brc.ui.UIHelper.createTextField;
import static edu.ku.brc.ui.UIRegistry.getTopWindow;

import java.awt.Color;
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
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
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
import edu.ku.brc.specify.conversion.BasicSQLUtils;
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
    
    private StateCountryContXRef stCntXRef;
    private Vector<GeoSearchResultsItem> countryInfo = new Vector<GeoSearchResultsItem>();
    
    private Integer          lookupId   = null;

    private Vector<GeoSearchResultsItem> coInfoList = null;
    private HashMap<Integer, String> i18NLabelsMap = new HashMap<Integer, String>();
    private JCheckBox        updateNameCB;
    private JCheckBox        mergeCB;
    private JCheckBox        addISOCodeCB;
    private JTextField       isoCodeTF;
    private JProgressBar     progressBar = new JProgressBar();
    
    private JList<String>    mainList;
    private DefaultListModel<String> dlm;
    private boolean          noMatchesFound;
    
    private int              selectedIndex;
    private List<GeoSearchResultsItem> luceneResults = null;


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
                         final int        geoTotal) throws HeadlessException
    {
        super((Frame)UIRegistry.getTopWindow(), "Choose", true, OKCANCELAPPLYHELP, null); // I18N
        
        this.nameStr      = nameStr;
        this.rankId       = rankId;
        this.level        = level;
        this.parentNames  = parentNames;
        this.parentRanks  = parentRanks;
        this.geonameId    = geonameId;
        this.stCntXRef    = stCntXRef;
        this.countryInfo  = countryInfo;
        this.doAllCountries = doAllCountries;
        this.doInvCountry = doInvCountry;
        this.readConn     = readConn;
        this.geoTotal     = geoTotal;
        
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
        
        calcProgress();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#buildButtonBar()
     */
    @Override
    protected JPanel buildButtonBar()
    {
        helpBtn.setText("Quit"); // I18N
        applyBtn.setText("Skip To Next Country");
        cancelBtn.setText("Skip");
        okBtn.setText("Save and Next");
        
        CellConstraints cc  = new CellConstraints();

        PanelBuilder pbLast = new PanelBuilder(new FormLayout("f:p:g,p,8px,p,8px,p,8px" + (whichBtns == OKCANCELAPPLYHELP ? ",p,8px" : ""), "p"));
        int inx = 2;
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
            SwingWorker<Boolean, Boolean> worker = new SwingWorker<Boolean, Boolean>()
            {
                @Override
                protected Boolean doInBackground() throws Exception
                {
                    int geoCount = BasicSQLUtils.getCount(QueryAdjusterForDomain.getInstance().adjustSQL("SELECT COUNT(*) FROM geography WHERE GeographyTreeDefID = GEOTREEDEFID AND GeographyCode IS NULL"));
                    progressBar.setValue(geoTotal == 0 ? 0 : (geoCount*100) / geoTotal);
                    return true;
                }
                @Override
                protected void done()
                {
                    super.done();
                    
                }
            };
            worker.execute();
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
        this.whichBtns = doStatesOrCounties ? CustomDialog.OKCANCELAPPLYHELP : CustomDialog.OKCANCELHELP;
        
        boolean isStCnty = rankId > 200;//== 300 || rankId == 400; 
        //isStCnty = true;
        
        CellConstraints cc  = new CellConstraints();
        PanelBuilder    pb  = new PanelBuilder(new FormLayout("f:p:g", "p,2px,p,12px,p,2px,f:p:g,8px,p,4px,p,10px,p"+ (isStCnty ? ",8px,p" : "")));

        this.contentPanel = pb.getPanel();
        
        super.createUI();
        
        try
        {
            dlm      = new DefaultListModel<String>();
            mainList = new JList<String>(dlm);

            String geoName;
            if (coInfoList != null && coInfoList.size() > 0)
            {
                geoName = fillFromLuceneResults();
            } else
            {
                geoName = fillFromQuery();
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
                lookupBtn = createI18NButton("Look Up"); // I18N
                lookPB.add(lookupBtn, cc.xy(2,1));
                final String geoNameFinal = geoName;
                lookupBtn.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        int index = (rankId / 100) - 2;
                        lookupId = globalRankSearch(parentNames[index], geoNameFinal);
                    }
                });
            }
            
            int i = 0;
            ArrayList<String> labels = new ArrayList<String>();
            while (i < parentNames.length && parentRanks[i] > -1)
            {
                labels.add(i18NLabelsMap.get(parentRanks[i++])); 
            }

            JScrollPane  sb     = createScrollPane(mainList, true);
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
            
            if (doAllCountries[0])
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
            
            noMatchesFound = dlm.size() == 0;
            
            // Optional Depending on States / Countries
            if (doStatesOrCounties)
            {
                if (dlm.getSize() == 0)
                {
                    dlm.addElement("No matches found.");// I18N
                }
            }

            pb.setDefaultDialogBorder();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
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
             dlm.addElement(item.name);
             if (geonameId != null && geonameId.equals(item.recId))
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
        String twoChars  = nameStr.substring(0, 2);
                
        for (GeoSearchResultsItem p : coInfoList)
        {
            String name = p.name;
            char   fc   = name.charAt(0);
            String cmp  = name.length() > 1 ? name.substring(0, 2) : null;
            dlm.addElement(name);
            
            if (inx == -1)
            {
                if (name.equals(geoName))
                {
                    inx = i;
                } else if (geonameId != null && p.recId == geonameId)
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
            GeoSearchResultsItem item = coInfoList.get(inx);
            isoCodeTF.setText(item.isoCode);
        }
    }

    /**
     * @param selIndex
     * @param model
     * @param ids
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void fillGeoList(final int selIndex, 
                             final String parentName,
                             final DefaultListModel model, 
                             final ArrayList<Integer> ids)
    {
        ids.clear();
        model.removeAllElements();
        
        StringBuilder sb = new StringBuilder("");
        if (selIndex == 0)
        {
            sb.append("SELECT geonameId, name FROM countryinfo ORDER BY name");
        } else
        {
            String countryCode = stCntXRef.countryNameToCode(parentName);
            sb.append(String.format("SELECT geonameId, asciiname FROM geoname WHERE fcode = 'ADM1' AND country = '%s' ORDER BY asciiname", countryCode));
        }
        System.err.println(sb.toString());
        for (Object[] row : query(sb.toString()))
        {
            ids.add((Integer)row[0]);
            model.addElement(row[1]);
        }
    }
    
    /**
     * @param parentId
     * @param geoName
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Integer globalRankSearch(final String parentName, final String geoName)
    {
        final DefaultListModel   model    = new DefaultListModel();
        final JComboBox          typeCBX  = UIHelper.createComboBox(new Object[] {"Country", "State"});
        final JList              list     = new JList(model);
        final ArrayList<Integer> ids      = new ArrayList<Integer>();
        
        CellConstraints cc  = new CellConstraints();
        PanelBuilder    pb  = new PanelBuilder(new FormLayout("p,2px,f:p:g","p,8px,p,2px,p"));
        JScrollPane     sb  = createScrollPane(list, true);

        pb.add(createI18NFormLabel("Choose a Cataegory"), cc.xy(1, 1));
        pb.add(typeCBX, cc.xy(3, 1));
        
        pb.add(createI18NLabel("Choose a Geography"), cc.xyw(1, 3, 3));
        pb.add(sb, cc.xyw(1, 5, 3));
              
                
        pb.setDefaultDialogBorder();
        
        typeCBX.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                fillGeoList(typeCBX.getSelectedIndex(), parentName, model, ids);
            }
        });
        
        if (lookupId != null)
        {
            dlm.removeElementAt(0); 
        }
        lookupId = null;
        CustomDialog dlg = new CustomDialog((Frame)getTopWindow(), "Choose", true, CustomDialog.OKCANCEL, pb.getPanel());
        
        typeCBX.setSelectedIndex(0);

        centerAndShow(dlg);
        
        int selInx = list.getSelectedIndex();
        if (selInx > -1)
        {
            Object selObj = model.get(selectedIndex);
            dlm.insertElementAt((String)selObj, 0);
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    mainList.setSelectedIndex(0);
                }
            });
            return ids.get(selInx);
        }
        return null;
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
    public String getSelectedListValue()
    {
        return (String)mainList.getSelectedValue();
    }
    
    /**
     * @return
     */
    public String getSelectedISOValue()
    {
        return isoCodeTF.getText();
    }
    
    /**
     * @return the lookupId
     */
    public Integer getLookupId()
    {
        return lookupId;
    }

    /**
     * @return
     */
    public Integer getSelectedId()
    {
        Integer id     = null;
        int     selInx = mainList.getSelectedIndex();
        if (selInx > -1 && coInfoList.size() > 0)
        {
            id = coInfoList.get(mainList.getSelectedIndex()).recId;
        }
        return lookupId != null ? null : id;
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
    protected void cancelButtonPressed() // Acts like the Skip Button
    {
        isCancelled = false;
        btnPressed  = CANCEL_BTN;
        setVisible(false);
    }
}
