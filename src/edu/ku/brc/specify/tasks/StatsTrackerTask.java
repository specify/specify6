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
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.Institution;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.util.Pair;

/**
 * This class sends usage stats.
 * 
 * @author rods
 * 
 * @code_status Complete
 */
public class StatsTrackerTask extends edu.ku.brc.af.tasks.StatsTrackerTask
{
    private static final Logger log = Logger.getLogger(StatsTrackerTask.class);
    
    private final static String DATABASE     = "Database";
    private final static String resourceName = "CollStats";

    private boolean                      hasChanged          = false;
    private JProgressBar                 progress;
    private Hashtable<Class<?>, Boolean> tablesHash          = new Hashtable<Class<?>, Boolean>();
    private Vector<Pair<String, String>> queries             = new Vector<Pair<String,String>>();
    private Collection                   collection          = null;  
    private Discipline                   discipline          = null;  
    private Division                     division            = null;  
    private Institution                  institution          = null;  
    
    /**
     * Constructor.
     */
    public StatsTrackerTask()
    {
        super();
        
        CommandDispatcher.register(DATABASE, this);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.StatsTrackerTask#initialize()
     */
    @Override
    public void initialize()
    {
        super.initialize();
        
        Element rootElement = AppContextMgr.getInstance().getResourceAsDOM(resourceName);
        if (rootElement != null)
        {
            List<?> rows = rootElement.selectNodes("/statistics/tables/table"); //$NON-NLS-1$
            for (Object obj : rows)
            {
                Element statElement     = (Element)obj;
                String  tableClassName  = XMLHelper.getAttr(statElement, "class", null);
                if (StringUtils.isNotEmpty(tableClassName))
                {
                    Class<?> cls = null;
                    try
                    {
                        cls = Class.forName(tableClassName);
                        tablesHash.put(cls, true);
                        
                    } catch (Exception ex)
                    {
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(StatsTrackerTask.class, ex);
                        ex.printStackTrace();
                    }
                }
            }
        } else
        {
            log.error("Couldn't find resource ["+resourceName+"]"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.StatsTrackerTask#createClosingFrame()
     */
    @Override
    protected void showClosingFrame()
    {
        if (hasChanged)
        {
            ImageIcon img = IconManager.getIcon("SpecifySplash");
            
            CellConstraints    cc = new CellConstraints();
            PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g,150px", "f:p:g,2px,p"));
            pb.setDefaultDialogBorder();
            
            JLabel lbl = new JLabel(img);
            pb.add(lbl, cc.xyw(1, 1, 2));
            lbl = UIHelper.createI18NLabel("SPECIFY_SHUTDOWN", SwingConstants.CENTER);
            lbl.setFont(lbl.getFont().deriveFont(18.0f));
            pb.add(lbl, cc.xy(1, 3));
            
            progress = new JProgressBar(0, 100);
            pb.add(progress, cc.xy(2, 3));
            
            JFrame frame = new JFrame();
            frame.setUndecorated(true);
            frame.setContentPane(pb.getPanel());
            frame.pack();
            UIHelper.centerAndShow(frame);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.StatsTrackerTask#completed()
     */
    @Override
    protected void completed()
    {
        collection  = null;
        discipline  = null;
        division    = null;
        institution = null;
        queries.clear();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.StatsTrackerTask#starting()
     */
    @Override
    protected boolean starting()
    {
        if (collection == null)
        {
            collection = AppContextMgr.getInstance().getClassObject(Collection.class);
            discipline  = AppContextMgr.getInstance().getClassObject(Discipline.class);
            division    = AppContextMgr.getInstance().getClassObject(Division.class);
            institution = AppContextMgr.getInstance().getClassObject(Institution.class);
            
            queries.clear();
            
            // Need to do this now before all the Cached Objects change
            // that the QueryAdjusterForDomain uses.
            Element rootElement = AppContextMgr.getInstance().getResourceAsDOM(resourceName);
            if (rootElement != null)
            {
                List<?> rows = rootElement.selectNodes("/statistics/stats/stat"); //$NON-NLS-1$
                for (Object obj : rows)
                {
                    Element statElement = (Element)obj;
                    String  statsName   = XMLHelper.getAttr(statElement, "name", null);
                    if (StringUtils.isNotEmpty(statsName))
                    {
                        String sqlStr = QueryAdjusterForDomain.getInstance().adjustSQL(statElement.getText());
                        queries.add(new Pair<String, String>(statsName, sqlStr));
                    }
                }
            }
            return true;
        }
        
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.StatsTrackerTask#getPCLForWorker()
     */
    @Override
    protected PropertyChangeListener getPCLForWorker()
    {
        return new PropertyChangeListener() {
            public  void propertyChange(final PropertyChangeEvent evt) {
                if ("progress".equals(evt.getPropertyName())) 
                {
                    if (progress != null) progress.setValue((Integer)evt.getNewValue());
                }
            }
        };
    }
    
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.StatsTrackerTask#sendStats()
     */
    @Override
    protected void sendStats() throws Exception
    {
        super.sendStats();
        sendCollectionStats();
    }

    /**
     * Collection Statistics about the Collection (synchronously).
     */
    @Override
    protected Vector<NameValuePair> collectSecondaryStats(final boolean doSendSecondaryStats)
    {
        boolean isAnon = false;
        if (institution != null)
        {
            isAnon = institution.isSendStatsAnonymous();
        }
        
        if (doSendSecondaryStats || !isAnon)
        {
            Vector<NameValuePair> stats = new Vector<NameValuePair>();
            if (hasChanged)
            {
                if (progress != null) progress.setIndeterminate(true);
                if (queries.size() > 0)
                {
                    int    count = 0;
                    double total = queries.size();
                    for (Pair<String, String> p : queries)
                    {
                        String  statsName   = p.first;
                        if (StringUtils.isNotEmpty(statsName))
                        {
                            count++;
                            addStat(statsName, stats, p.second);
                            if (progress != null) progress.setIndeterminate(false);
                            worker.setProgressValue((int)(100.0 * (count / total)));
                        }
                    }
                    worker.setProgressValue(100);
                    
                } else
                {
                    log.error("Couldn't find resource ["+resourceName+"]"); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
            
            SpecifyUser su = AppContextMgr.getInstance().getClassObject(SpecifyUser.class);
            if (su != null)
            {
                stats.add(new NameValuePair("specifyuser",  fixParam(su.getName()))); //$NON-NLS-1$
            }
    
            // Gather Collection Counts;
            if (collection != null)
            {
                Integer estSize = collection.getEstimatedSize();
                String  estSizeStr = estSize != null ? Integer.toString(estSize) : "";
                
                stats.add(new NameValuePair("Collection_estsize",  estSizeStr)); //$NON-NLS-1$
                stats.add(new NameValuePair("Collection_number",  fixParam(collection.getRegNumber()))); //$NON-NLS-1$
                stats.add(new NameValuePair("Collection_website", fixParam(collection.getWebSiteURI()))); //$NON-NLS-1$
                stats.add(new NameValuePair("Collection_portal",  fixParam(collection.getWebPortalURI()))); //$NON-NLS-1$
                stats.add(new NameValuePair("Collection_name",    fixParam(collection.getCollectionName()))); //$NON-NLS-1$
            }
    
            if (discipline != null)
            {
                stats.add(new NameValuePair("Discipline_number",  fixParam(discipline.getRegNumber()))); //$NON-NLS-1$
                stats.add(new NameValuePair("Discipline_name",    fixParam(discipline.getName()))); //$NON-NLS-1$
            }
    
            if (division != null)
            {
                stats.add(new NameValuePair("Division_number",  fixParam(division.getRegNumber()))); //$NON-NLS-1$
                stats.add(new NameValuePair("Division_name",    fixParam(division.getName()))); //$NON-NLS-1$
            }
    
            if (institution != null)
            {
                stats.add(new NameValuePair("Institution_number",  fixParam(institution.getRegNumber()))); //$NON-NLS-1$
                stats.add(new NameValuePair("Institution_name",    fixParam(institution.getName()))); //$NON-NLS-1$
            }
            
            return stats;
        }
        return null;
    }
    
    /**
     * @param colId
     * @param colNm
     * @param regNum
     * @param sql
     * @param sb
     */
    private void collectStatsData(final String  colNm, 
                                  final String  regNum, 
                                  final String  sql,
                                  final StringBuilder sb)
    {
        if (sb.length() > 0) sb.append(';');
        sb.append(colNm);
        sb.append(String.format("{%s, %s [", colNm, regNum));

        int     c   = 0;
        for (Object[] row : BasicSQLUtils.query(sql))
        {
            Integer yr  = (Integer)row[0];
            Long    cnt = (Long)row[1];
            if (yr != null)
            {
                if (c > 0) sb.append(',');
                sb.append(String.format("%d=%d", yr, cnt));
            }
            c++;
        }
        sb.append("]}");
    }
    
    /**
     * @param stats
     * @param valName
     * @param value
     */
    private void addEncodedPair(final Vector<NameValuePair> stats, final String valName, final String value)
    {
        String val = "";
        try
        {
            val = URLEncoder.encode(value, "UTF-8");
        } catch (Exception ex) {}
        //System.out.println(String.format("[%s][%s]", valName, val));
        stats.add(new NameValuePair(valName, val));
    }
    
    /**
     * @param stats
     */
    private void getCollectingStats(final Vector<NameValuePair> stats)
    {
        final String ALL_YEAR_CATS      = "ALL_YEAR_CATS_STAT"; 
        final String LAST_COL_YEAR_STAT = "LAST_COL_YEAR_STAT"; 
        //final String LAST_30_DAYS  = "LAST_30DAYS_STAT"; 
        
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        
        AppPreferences  remotePrefs  = AppPreferences.getRemote();
        int             lastColYear  = remotePrefs.getInt(LAST_COL_YEAR_STAT, -1);
        boolean         doAllYears   = remotePrefs.getBoolean(ALL_YEAR_CATS, true);
        
        if (lastColYear != currentYear) // override when it is a new year
        {
            doAllYears = true; 
        }
        doAllYears = true;
        
        StringBuilder  allYearsSB   = new StringBuilder();
        StringBuilder  last30DaysSB = new StringBuilder();
        
        for (Object[] colRow : BasicSQLUtils.query("SELECT CollectionID,CollectionName,RegNumber FROM collection"))
        {
            Integer colId  = (Integer)colRow[0];
            String  colNm  = (String)colRow[1];
            String  regNum = (String)colRow[2];
            
            if (doAllYears)
            {
                String  sql = String.format("SELECT YR,COUNT(YR) FROM (SELECT YEAR(if (CatalogedDate IS NULL, TimestampCreated, CatalogedDate)) AS YR FROM collectionobject WHERE CollectionMemberID = %d) T1 GROUP BY YR ORDER BY YR", colId);
                collectStatsData(colNm, regNum, sql, allYearsSB);
            }

            // Cataloged by Month for current year
            String tmp = "SELECT MN, COUNT(MN) FROM (SELECT MONTH(DT) MN, YEAR(DT) YR FROM (SELECT if (CatalogedDate IS NULL, TimestampCreated, CatalogedDate) AS DT FROM collectionobject WHERE CollectionMemberID = %d) T1 WHERE YEAR(DT) = %d) T2 GROUP BY MN";
            String sql = String.format(tmp, colId, currentYear);
            collectStatsData(colNm, regNum, sql, last30DaysSB);
        }
        
        if (doAllYears)
        {
            addEncodedPair(stats, "catbyyr", allYearsSB.toString());
            remotePrefs.putBoolean(ALL_YEAR_CATS, false);
            remotePrefs.putInt(LAST_COL_YEAR_STAT, currentYear);
        }
        
        addEncodedPair(stats, "catbymn", last30DaysSB.toString());
        
        // Audit Information 
        Vector<Object[]> instRresults = BasicSQLUtils.query("SELECT Name, RegNumber FROM institution");
        if (instRresults.size() > 0)
        {
            Object[] instRow = instRresults.get(0);
            String   instName = (String)instRow[0];
            String   regNum   = (String)instRow[1];

            String[] actionStr = {"ins", "upd", "rmv"};
            for (int action=0;action<3;action++)
            {
                StringBuilder auditSB = new StringBuilder();
                String tmp  = "SELECT * FROM (SELECT a.TableNum, Count(a.TableNum) as Cnt FROM spauditlog AS a WHERE a.Action = %d GROUP BY a.TableNum) T1 ORDER BY Cnt DESC";
                String sql = String.format(tmp, action);
                collectStatsData(instName, regNum, sql, auditSB);
                addEncodedPair(stats, "audit_"+actionStr[action], auditSB.toString());
            }
        }
    }
    
    /**
     * @param cmdActionArg
     */
    private void checkTableType(final CommandAction cmdActionArg)
    {
        if (cmdActionArg.getData() instanceof FormDataObjIFace)
        {
            FormDataObjIFace data = (FormDataObjIFace)cmdActionArg.getData();
            if (tablesHash != null && tablesHash.get(data.getClass()) != null)
            {
                hasChanged = true;
            }
        }
    }
    
    /**
     * @return
     */
    protected NameValuePair[] createPostParametersForCollections()
    {
        Vector<NameValuePair> postParams = new Vector<NameValuePair>();
        Collections.addAll(postParams, createPostParameters(false));
        getCollectingStats(postParams);
            
        // create an array from the params
        NameValuePair[] paramArray = new NameValuePair[postParams.size()];
        for (int i = 0; i < paramArray.length; ++i)
        {
            paramArray[i] = postParams.get(i);
        }
        
        return paramArray;
    }

    
    /**
     * @throws Exception
     */
    private void sendCollectionStats() throws Exception
    {
        // check the website for the info about the latest version
        HttpClient httpClient = new HttpClient();
        httpClient.getParams().setParameter("http.useragent", getClass().getName()); //$NON-NLS-1$
        
        // get the URL of the website to check, with usage info appended, if allowed
        String collStatsCheckURL =  getResourceString("StatsTrackerTask.COLLSTATSURL"); //$NON-NLS-1$
        if (StringUtils.isNotEmpty(collStatsCheckURL))
        {
            PostMethod postMethod = new PostMethod(collStatsCheckURL);
            
            // get the POST parameters (which includes usage stats, if we're allowed to send them)
            NameValuePair[] postParams = createPostParametersForCollections();
            postMethod.setRequestBody(postParams);
            
            // connect to the server
            try
            {
                httpClient.executeMethod(postMethod);
                
                // get the server response
                @SuppressWarnings("unused")
                String responseString = postMethod.getResponseBodyAsString();
                
                /*if (StringUtils.isNotEmpty(responseString))
                {
                    System.err.println(responseString);
                }*/
    
            } catch (java.net.UnknownHostException ex)
            {
                log.debug("Couldn't reach host.");
                
            } catch (Exception e)
            {
                //e.printStackTrace();
                //UsageTracker.incrHandledUsageCount();
                //edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(StatsTrackerTask.class, e);
                throw new ConnectionException(e);
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @Override
    public void doCommand(final CommandAction cmdActionArg)
    {
        super.doCommand(cmdActionArg);
        
        if (cmdActionArg.isType(DATABASE))
        {
            checkTableType(cmdActionArg);
        }
    }
}
