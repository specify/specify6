/* Copyright (C) 2020, Specify Collections Consortium
 *
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.sql.Connection;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.*;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.PrefsPanelIFace;
import edu.ku.brc.dbsupport.*;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.specify.prefs.LoansPrefsPanel;
import edu.ku.brc.ui.CustomDialog;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.TaskMgr;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.tasks.BaseTask.ASK_TYPE;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.dbsupport.RecordSetItemIFace;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.InfoRequest;
import edu.ku.brc.specify.datamodel.OneToManyProviderIFace;
import edu.ku.brc.specify.ui.ColObjInfo;
import edu.ku.brc.specify.ui.PrepInfo;
import edu.ku.brc.specify.ui.SelectPrepsDlg;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

import static edu.ku.brc.ui.UIRegistry.*;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Oct 9, 2008
 *
 */
public class InteractionsProcessor<T extends OneToManyProviderIFace>
{
    private static final Logger log = Logger.getLogger(InteractionsProcessor.class);
    private static final String LOAN_LOADR = "LoanLoader";
    public static final String DEFAULT_SRC_TBL_ID = "Interactions.DefaultSrcTableId";

    protected static final int forLoan = 0;
    protected static final int forGift = 1;
    protected static final int forAcc = 2;
    protected static final int forExchange = 3;
    protected static final int forDisposal = 4;
    protected static final int forLegalDeacc = 5;

    protected InteractionsTask task;
    protected int              isFor;
    protected int              tableId;
    protected Viewable         viewable = null;

    /**
     *
     */
    public InteractionsProcessor(final InteractionsTask task,
                                 final int          isFor,
                                 final int              tableId)
    {
        this.task    = task;
        this.isFor  = isFor;
        this.tableId = tableId;
    }


    /**
     * Asks where the source of the Loan Preps should come from.
     * @return the source enum
     */
    protected ASK_TYPE askSourceOfPreps(final boolean hasInfoReqs, final boolean hasColObjRS, final T currPrepProvider)
    {
        if (isFor == forLegalDeacc) {
            return ASK_TYPE.None;
        }

        String label;
        if (hasInfoReqs && hasColObjRS)
        {
            label = getResourceString("NEW_INTER_USE_RS_IR");

        } else if (hasInfoReqs)
        {
            label = getResourceString("NEW_INTER_USE_IR");
        } else
        {
            label = getResourceString("NEW_INTER_USE_RS");
        }

        boolean isForAcc = isFor == forAcc;
        Object[] options = new Object[!isForAcc || (isForAcc && ((!hasInfoReqs && !hasColObjRS) || currPrepProvider != null)) ? 2 : 3];
        Integer dosOpt = null;
        Integer rsOpt = null;
        Integer noneOpt = null;
        if (!isForAcc || currPrepProvider != null) {
            options[0] = label;
            options[1] = getResourceString("NEW_INTER_ENTER_CATNUM");
            rsOpt = JOptionPane.YES_OPTION;
            dosOpt = JOptionPane.NO_OPTION;
        } else {
            if (options.length == 2) {
                options[0] = getResourceString("NEW_INTER_ENTER_CATNUM");
                options[1] = getResourceString("NEW_INTER_EMPTY");
                dosOpt = JOptionPane.YES_OPTION;
                noneOpt = JOptionPane.NO_OPTION;
            } else {
                options[0] = label;
                options[1] = getResourceString("NEW_INTER_ENTER_CATNUM");
                options[2] = getResourceString("NEW_INTER_EMPTY");
                rsOpt = JOptionPane.YES_OPTION;
                dosOpt = JOptionPane.NO_OPTION;
                noneOpt = JOptionPane.CANCEL_OPTION;
            }
        }

        int userChoice = JOptionPane.showOptionDialog(UIRegistry.getTopWindow(),
                getResourceString("NEW_INTER_CHOOSE_RSOPT"),
                getResourceString("NEW_INTER_CHOOSE_RSOPT_TITLE"),
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (userChoice == dosOpt) {
            return ASK_TYPE.EnterDataObjs;
        } else if (rsOpt != null && userChoice == rsOpt) {
            return ASK_TYPE.ChooseRS;
        } else if (noneOpt != null && userChoice == noneOpt) {
            return ASK_TYPE.None;
        }

        return ASK_TYPE.Cancel;
    }

    /**
     * Creates a new loan/gift.
     */
    public void createOrAdd()
    {
        this.viewable = null;
        createOrAdd(null, null, null);
    }

    /**
     * Creates a new loan/gift and will set the new data object back into the Viewable.
     * @param viewableArg
     */
    public void createOrAdd(final Viewable viewableArg)
    {
        this.viewable = viewableArg;

        createOrAdd(null, null, null);
    }

    /**
     * @param recordSetArg
     */
    public void createOrAdd(final RecordSetIFace recordSetArg)
    {
        this.viewable = null;
        createOrAdd(null, null, recordSetArg);
    }

    /**
     * @param currPrepProvider
     */
    public void createOrAdd(final T currPrepProvider)
    {
        this.viewable = null;
        createOrAdd(currPrepProvider, null, null);
    }

    public static int promptForItemIdTableId() {
        JPanel mainPane = new JPanel(new BorderLayout());
        mainPane.add(new Label(getResourceString("InteractionsProcessor.ItemIDForTblMsg")), BorderLayout.NORTH);
        ButtonGroup buttGrp = new ButtonGroup();
        JRadioButton coBtn = new JRadioButton(DBTableIdMgr.getInstance().getTitleForId(CollectionObject.getClassTableId()));
        coBtn.setSelected(true);
        JRadioButton prepBtn = new JRadioButton(DBTableIdMgr.getInstance().getTitleForId(Preparation.getClassTableId()));
        buttGrp.add(coBtn);
        buttGrp.add(prepBtn);
        JPanel buttGrpPane = new JPanel();
        buttGrpPane.setLayout(new BoxLayout(buttGrpPane, BoxLayout.Y_AXIS));
        buttGrpPane.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 4));
        buttGrpPane.add(coBtn);
        buttGrpPane.add(prepBtn);
        mainPane.add(buttGrpPane, BorderLayout.CENTER);
        mainPane.setBorder(BorderFactory.createEmptyBorder(12, 5, 12, 5));
        //TaskMgr.getTask("")
        JCheckBox remember = null;
        boolean canSetPref = !AppContextMgr.isSecurityOn();
        if (!canSetPref) {
            PrefsPanelIFace loanPrefs = new LoansPrefsPanel(false);
            canSetPref = loanPrefs.getPermissions().canModify();
        }
        if (canSetPref) {
            remember = new JCheckBox(getResourceString("InteractionsProcessor.DoNotAskAgain"));
            mainPane.add(remember, BorderLayout.SOUTH);
        }
        CustomDialog cwin = new CustomDialog((Frame)getTopWindow(), getResourceString("InteractionsProcessor.ItemIDForTblTitle"), true,
                mainPane); // i18n
        cwin.setWhichBtns(CustomDialog.OKCANCEL);
        cwin.setModal(true);
        cwin.setVisible(true);
        int result = cwin.isCancelled() ? 0 :
                (coBtn.isSelected() ? CollectionObject.getClassTableId() : Preparation.getClassTableId());
        if (canSetPref && remember.isSelected()) {
            AppPreferences.getRemote().putInt(DEFAULT_SRC_TBL_ID, result);
        }
        cwin.dispose();
        return result;
    }

    public static String getDefaultInteractionLookupField(int tableId) {
        if (tableId == CollectionObject.getClassTableId()) {
            return "CatalogNumber";
        } else if (tableId == Preparation.getClassTableId()) {
            return "BarCode";
        } else {
            DBTableInfo tbl = DBTableIdMgr.getInstance().getInfoById(tableId);
            String tblName = tbl != null ? tbl.getName() : "table zero";
            log.error("No default lookup field for " + tblName);
            return null;
        }
    }

    public static String getInteractionItemLookupFieldPref(int tableId) {
        return "InteractionItemLookupField." + tableId;
    }

    public static String getInteractionItemLookupField(int tableId) {
        return AppPreferences.getRemote().get(getInteractionItemLookupFieldPref(tableId), getDefaultInteractionLookupField(tableId));
    }

    /**
     * Creates a new loan from a RecordSet.
     * @param currPrepProvider an existing loan that needs additional Preps
     * @param infoRequest a info request
     * @param recordSetArg the recordset to use to create the loan
     */
    public void createOrAdd(final T              currPrepProvider,
                            final InfoRequest    infoRequest,
                            final RecordSetIFace recordSetArg)
    {
        RecordSetIFace recordSet = recordSetArg;
        boolean isEmptyAcc = false;
        if (infoRequest == null && recordSet == null)
        {
            String catNumField = "catalogNumber";

            // Get a List of InfoRequest RecordSets
            Vector<RecordSetIFace> rsList       = task.getInfoReqRecordSetsFromSideBar();
            RecordSetTask          rsTask       = (RecordSetTask)TaskMgr.getTask(RecordSetTask.RECORD_SET);
            List<RecordSetIFace>   colObjRSList = rsTask.getRecordSets(CollectionObject.getClassTableId());

            // If the List is empty then
            if (rsList.size() == 0 && colObjRSList.size() == 0 && (isFor != forAcc || currPrepProvider != null)) {
                recordSet = task.askForDataObjRecordSet(CollectionObject.class, catNumField, isFor == forAcc);

            } else {
                ASK_TYPE rv = askSourceOfPreps(rsList.size() > 0, colObjRSList.size() > 0, currPrepProvider);
                if (rv == ASK_TYPE.ChooseRS) {
                    Vector<Integer> tblIds = new Vector<>();
                    tblIds.add(CollectionObject.getClassTableId());
                    if (isFor != forAcc) {
                        tblIds.add(Preparation.getClassTableId());
                    }
                    recordSet = RecordSetTask.askForRecordSet(tblIds, rsList, true);
                } else if (rv == ASK_TYPE.EnterDataObjs) {
                    Integer defSrcTblId = AppPreferences.getRemote().getInt(DEFAULT_SRC_TBL_ID, null);
                    if (isFor == forAcc) {
                        defSrcTblId = 1;
                    }
                    if (defSrcTblId == null || defSrcTblId == 0) {
                        defSrcTblId = promptForItemIdTableId();
                    }
                    if (defSrcTblId != null && defSrcTblId != 0) {
                        recordSet = task.askForDataObjRecordSet(defSrcTblId == CollectionObject.getClassTableId() ? CollectionObject.class : Preparation.class,
                                getInteractionItemLookupField(defSrcTblId), isFor == forAcc);
                    }
                } else if (rv == ASK_TYPE.None) {
                    recordSet = null;
                    isEmptyAcc = true;
                } else if (rv == ASK_TYPE.Cancel) {
                    if (viewable != null) {
                        viewable.setNewObject(null);
                    }
                    return;
                }
            }
        }

        if (recordSet == null && !isEmptyAcc) {
            return;
        }

        if (isEmptyAcc) {
            PrepLoaderSQL prepLoaderSQL = new PrepLoaderSQL(null, recordSet, infoRequest, isFor);
            prepLoaderSQL.execute();
        } else {
            if (recordSet.getNumItems() == 0 && isFor != forAcc) {
                if (isFor == forLoan) {
                    task.addPrepsToLoan(currPrepProvider, infoRequest, new Hashtable<Integer, Integer>(), viewable);
                } else if (isFor == forGift) {
                    task.addPrepsToGift(currPrepProvider, infoRequest, new Hashtable<Integer, Integer>(), viewable);
                }  else if (isFor == forDisposal) {
                    task.addPrepsToDisposal(currPrepProvider, infoRequest, new Hashtable<>(), viewable);
                } 
            } else {
                DBTableIdMgr.getInstance().getInClause(recordSet);

                DBTableInfo tableInfo = DBTableIdMgr.getInstance().getInfoById(recordSet.getDbTableId());

                DataProviderFactory.getInstance().evict(tableInfo.getClassObj()); // XXX Not sure if this is really needed

                DataProviderSessionIFace session = null;
                try {
                    session = DataProviderFactory.getInstance().createSession();

                    // OK, it COULD be a RecordSet contain one or more InfoRequest,
                    // we will only accept an RS with one InfoRequest
                    if (infoRequest == null && recordSet.getDbTableId() == InfoRequest.getClassTableId()) {
                        if (recordSet.getNumItems() == 1) {
                            RecordSetItemIFace item = recordSet.getOnlyItem();
                            if (item != null) {
                                InfoRequest infoReq = session.get(InfoRequest.class, item.getRecordId().intValue());
                                if (infoReq != null) {
                                    createOrAdd(null, infoReq, infoReq.getRecordSets().iterator().next());

                                } else {
                                    // error about missing info request
                                    // Error Dialog
                                }
                            } else {
                                // error about item being null for some unbelievable reason
                                // Error Dialog
                            }
                        } else {
                            // error about item having more than one or none
                            // Error Dialog
                        }
                        return;
                    }

                    // OK, here we have a recordset of CollectionObjects
                    // First we process all the CollectionObjects in the RecordSet
                    // and create a list of Preparations that can be loaned
                    String sqlStr = DBTableIdMgr.getInstance().getQueryForTable(recordSet);
                    if (StringUtils.isNotBlank(sqlStr)) {

                        //CACA
                        final JStatusBar statusBar = UIRegistry.getStatusBar();
                        statusBar.setIndeterminate(LOAN_LOADR, true);

                        if (recordSet.getNumItems() > 2) {
                            UIRegistry.writeSimpleGlassPaneMsg(getResourceString("NEW_INTER_LOADING_PREP"), 24);
                        }

                        PrepLoaderSQL prepLoaderSQL = new PrepLoaderSQL(currPrepProvider, recordSet, infoRequest, isFor);
                        prepLoaderSQL.addPropertyChangeListener(
                                new PropertyChangeListener() {
                                    public void propertyChange(PropertyChangeEvent evt) {
                                        log.debug(evt.getNewValue());
                                        if ("progress".equals(evt.getPropertyName())) {
                                            statusBar.setValue(LOAN_LOADR, (Integer) evt.getNewValue());
                                        }
                                    }
                                });
                        prepLoaderSQL.execute();

                    } else {
                        log.error("Query String empty for RecordSet tableId[" + recordSet.getDbTableId() + "]");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(InteractionsProcessor.class, ex);
                }
            }
        }
    }

    /**
     * @param rs
     * @param prepProvider
     */
    protected void cosLoaded(final RecordSetIFace rs, final T prepProvider) {
        //System.out.println("Adding cos to accession...");
        if (isFor == forAcc) {
            task.addCosToAcc(prepProvider, rs, viewable);
        } else {
            task.addToLegalDeacc(prepProvider, rs, viewable);
        }
    }
    /**
     * @param coToPrepHash
     * @param prepTypeHash
     * @param prepProvider
     * @param infoRequest
     * @param session
     */
    protected void prepsLoaded(final Hashtable<Integer, ColObjInfo> coToPrepHash,
                               final Hashtable<Integer, String>     prepTypeHash,
                               final T                              prepProvider,
                               final InfoRequest                    infoRequest,
                               final int objTblId) {
        if (coToPrepHash.size() == 0 || prepTypeHash.size() == 0) {
            UIRegistry.showLocalizedMsg("NEW_INTER_NO_PREPS_TITLE", "NEW_INTER_NO_PREPS");
            return;
        }

        final DBTableInfo ti = DBTableIdMgr.getInstance().getInfoById(tableId);

        final SelectPrepsDlg loanSelectPrepsDlg = new SelectPrepsDlg(coToPrepHash, prepTypeHash, ti.getTitle(), objTblId);
        loanSelectPrepsDlg.createUI();
        loanSelectPrepsDlg.setModal(true);

        UIHelper.centerAndShow(loanSelectPrepsDlg);

        if (loanSelectPrepsDlg.isCancelled()) {
            if (viewable != null) {
                viewable.setNewObject(null);
            }
            return;
        }

        final Hashtable<Integer, Integer> prepsHash = loanSelectPrepsDlg.getSelection();
        if (prepsHash.size() > 0) {
            final SwingWorker worker = new SwingWorker() {
                @Override
                public Object construct() {
                    JStatusBar statusBar = UIRegistry.getStatusBar();
                    statusBar.setIndeterminate("INTERACTIONS", true);
                    statusBar.setText(getLocalizedMessage("CREATING_INTERACTION", ti.getTitle()));

                    if (isFor == forLoan) {
                        task.addPrepsToLoan(prepProvider, infoRequest, prepsHash, viewable);
                    } else if (isFor == forGift) {
                        task.addPrepsToGift(prepProvider, infoRequest, prepsHash, viewable);
                    } else if (isFor == forExchange) {
                        task.addPrepsToExchangeOut(prepProvider, infoRequest, prepsHash, viewable);
                    } else if (isFor == forDisposal) {
                        task.addPrepsToDisposal(prepProvider, infoRequest, prepsHash, viewable);
                    }
                    return null;
                }

                //Runs on the event-dispatching thread.
                @Override
                public void finished() {
                    JStatusBar statusBar = UIRegistry.getStatusBar();
                    statusBar.setProgressDone("INTERACTIONS");
                    statusBar.setText("");
                }
            };
            worker.start();
        }
    }

    /**
     * Creates a new loan from a InfoRequest.
     * @param infoRequest the infoRequest to use to create the loan
     */
    public void createFromInfoRequest(final InfoRequest infoRequest)
    {
        RecordSetIFace rs = null;
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            session.attach(infoRequest);
            rs = infoRequest.getRecordSets().iterator().next();

        } catch (Exception ex)
        {
            ex.printStackTrace();
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(InteractionsProcessor.class, ex);
            // Error Dialog

        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }

        if (rs != null)
        {
            createOrAdd(null, infoRequest, rs);
        }
    }

    public static int LOAN_ADJUST_IDX = 0;
    public static int GIFT_ADJUST_IDX = 1;
    public static int EXCHANGEOUT_ADJUST_IDX = 2;
    public static int DISPOSAL_ADJUST_IDX = 3;

    public static String getAdjustedCountForPrepSQL(String where, boolean[] settings) {
    String sql = "select p.preparationid, coalesce(p.countamt, 0)";
    String adjusters = "";
    String joiners = "";
    if (settings[LOAN_ADJUST_IDX]) {
        adjusters += "coalesce(sum(lp.unavailable), 0)";
        joiners += " left join (select preparationid, sum(coalesce(quantity, 0) - coalesce(quantityresolved, 0)) unavailable from loanpreparation group by 1) lp on lp.preparationid = p.preparationid";
    }
    if (settings[GIFT_ADJUST_IDX]) {
        adjusters += (adjusters.length() > 0 ? "+" : "") + "coalesce(sum(gp.unavailable), 0)";
        joiners += " left join (select preparationid, sum(coalesce(quantity, 0)) unavailable from giftpreparation group by 1) gp on gp.preparationid = p.preparationid";
    }
    if (settings[EXCHANGEOUT_ADJUST_IDX]) {
        adjusters += (adjusters.length() > 0 ? "+" : "") + "coalesce(sum(ep.unavailable), 0)";
        joiners += " left join (select preparationid, sum(coalesce(quantity, 0)) unavailable from exchangeoutprep group by 1) ep on ep.preparationid = p.preparationid";
    }
    if (settings[DISPOSAL_ADJUST_IDX]) {
        adjusters += (adjusters.length() > 0 ? "+" : "") + "coalesce(sum(dp.unavailable), 0)";
        joiners += " left join (select preparationid, sum(coalesce(quantity, 0)) unavailable from disposalpreparation group by 1) dp on dp.preparationid = p.preparationid";
    }
    if (adjusters.length() > 0) {
        sql += " - (" + adjusters +") available from preparation p " + joiners;
    }
    if (where != null) {
        sql += " where " + where;
    }
    sql += " group by 1";
    return sql;
}

    //--------------------------------------------------------------
    // Background loader class for loading a large number of loan preparations
    //--------------------------------------------------------------
    class PrepLoaderSQL extends javax.swing.SwingWorker<Integer, Integer>
    {
        private final String PROGRESS = "progress";


        private RecordSetIFace recordSet;
        private T              prepsProvider;
        private InfoRequest    infoRequest;
        private int         isFor;

        private Hashtable<Integer, String>     prepTypeHash = new Hashtable<Integer, String>();
        private Hashtable<Integer, ColObjInfo> coToPrepHash = new Hashtable<Integer, ColObjInfo>();


        /**
         * @param prepsProvider
         * @param recordSet
         * @param infoRequest
         */
        public PrepLoaderSQL(final T              prepsProvider,
                             final RecordSetIFace recordSet,
                             final InfoRequest    infoRequest,
                             final int        isFor)
        {
            this.recordSet     = recordSet;
            this.prepsProvider = prepsProvider;
            this.infoRequest   = infoRequest;
            this.isFor     = isFor;
        }

        /**
         * @param val
         * @return
         */
        private Integer getInt(final Object val)
        {
            return val == null ? 0 : (Integer)val;
        }

        private String getVisibleIdFldForSql() {
            String def;
            if (recordSet.getDbTableId() == 1) {
                def = "catalognumber";
            } else {
                def = "barcode";
            }
            return "o." + AppPreferences.getRemote().get(InteractionsProcessor.getInteractionItemLookupFieldPref(recordSet.getDbTableId()),def);
        }

        private String getIdSelectForSql() {
            return (recordSet.getDbTableId() == 1 ? "o.CollectionObjectID" : "o.PreparationID") + ", " + getVisibleIdFldForSql();
        }

        private String getRecordSetFilter() {
            return (recordSet.getDbTableId() == 1 ? "o.CollectionObjectID" : "o.PreparationID") + DBTableIdMgr.getInstance().getInClause(recordSet);
        }

        private String getFrom() {
            return (recordSet.getDbTableId() == 1 ? "collectionobject" : "preparation" ) +
                    " o left join determination dt ON dt.CollectionObjectID = o.CollectionObjectID " +
                    "left JOIN taxon tx ON dt.TaxonID = tx.TaxonID";
        }
        /**
         * @return a List of rows that have the CollectionObject info from the rcordset
         */
        protected Vector<Object[]> getColObjsFromRecordSet() {
            String sql = "SELECT " + getIdSelectForSql() + ", tx.FullName FROM " + getFrom() + " WHERE (isCurrent is null or isCurrent <> 0) " +
                    "AND " + getRecordSetFilter();
            log.debug("-------------- " + sql);

            Vector<Object[]> fullItems = BasicSQLUtils.query(sql);
            //log.debug("-------------- " + "fullItems: " + fullItems.size());
            return fullItems;
        }

        //see git issue #730
        protected String getAvailableCountForPrepSQL(String where) {
            boolean[] settings = {true, true, true, true};
            return getAdjustedCountForPrepSQL(where, settings);
//            String sql = "select p.preparationid, coalesce(p.countamt, 0) - (coalesce(sum(lp.unavailable), 0) + coalesce(sum(gp.unavailable), 0) + coalesce(sum(ep.unavailable), 0) + coalesce(sum(dp.unavailable), 0)) available "
//                + "from preparation p left join "
//                + "(select preparationid, sum(coalesce(quantity, 0) - coalesce(quantityresolved, 0)) unavailable from loanpreparation group by 1) lp on lp.preparationid = p.preparationid left join "
//                + "(select preparationid, sum(coalesce(quantity, 0)) unavailable from giftpreparation group by 1) gp on gp.preparationid = p.preparationid left join "
//                + "(select preparationid, sum(coalesce(quantity, 0)) unavailable from exchangeoutprep group by 1) ep on ep.preparationid = p.preparationid left join "
//                + "(select preparationid, sum(coalesce(quantity, 0)) unavailable from disposalessionpreparation group by 1) dp on dp.preparationid = p.preparationid ";
//            if (where != null) {
//                sql += "where " + where;
//            }
//            sql += " group by 1";
//            return sql;
        }
        /**
         *
         * @return
         */
        protected java.sql.Connection getConnForAvailableCounts() {
            String sqlMode = BasicSQLUtils.querySingleObj("select @@sql_mode");
            log.debug("-------------- " + sqlMode);
            Connection result = DBConnection.getInstance().createConnection();
            int updated = BasicSQLUtils.update(result, "set sql_mode = 'STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION'");
            if (updated < 0) {
                log.warn("error setting sql_mode");
            }
            sqlMode = BasicSQLUtils.querySingleObj(result,"select @@sql_mode");
            log.debug("-------------- " + sqlMode);
            return result;
        }

        /**
         *
         * @param objectIds
         * @return
         */
        protected List<Object[]> getAvailableCounts(List<String> objectIds) {
            String sql = "select " + (recordSet.getDbTableId() == 1 ? "p.collectionobjectid" : "p.preparationid")
                    + ", p.preparationid, coalesce(p.CountAmt,0), pt.Name, pt.PrepTypeID, avail.available from preparation p";
            //assuming there aren't 10s of 1000s of items in collectionObjectIds
            String idStr = objectIds.toString();
            idStr = idStr.substring(1, idStr.length()-1);
            String where = recordSet.getDbTableId() == 1 ? "p.collectionobjectid" : "p.preparationid" + " in(" + idStr + ")";
            String subSql = getAvailableCountForPrepSQL(where);
            sql = sql + " inner join (" + subSql + ") avail on avail.preparationid = p.preparationid"
                    + " inner join preptype pt on pt.preptypeid = p.preptypeid";
            if (isFor == forLoan) {
                sql += " where pt.isloanable";
            }
            Connection conn = getConnForAvailableCounts();
            log.debug("-------------- " + sql);
            List<Object[]> rows = BasicSQLUtils.query(conn, sql);
            try {
                conn.close();
            } catch (Exception x) {
                log.error(x);
            }
            log.debug("--------------rows: " + rows.size());
            return rows;
        }

        protected int collect() {
            if (isFor != forAcc && isFor != forLegalDeacc) {
                coToPrepHash = new Hashtable<>();
                List<String> objds = new ArrayList<>();
                processRecordSetForCollect(objds);
                List<Object[]> rows = getAvailableCounts(objds);
                if (rows.size() > 0) {
                    for (Object[] row : rows) {
                        int prepId = (Integer) row[1];
                        int prepQty = Integer.valueOf(row[2].toString());
                        String prepType = (String) row[3];
                        int prepTypeId = (Integer) row[4];
                        int coId = (Integer) row[0];
                        int available = Integer.valueOf(row[5].toString());
                        prepTypeHash.put(prepTypeId, prepType);
                        ColObjInfo colObjInfo = coToPrepHash.get(coId);
                        if (colObjInfo != null) {
                            colObjInfo.add(new PrepInfo(prepId, prepTypeId, prepQty, available));
                        } else {
                            //what went wrong?
                        }
                    }
                }
            }
            return 0;
        }


        protected void processRecordSetForCollect(List<String> objIds) {
            List<Object[]> objIdRows = getColObjsFromRecordSet();
            for (Object[] row : objIdRows) {
                Integer objId = (Integer)row[0];
                objIds.add(objId.toString());
                if (row[1] != null) {
                    coToPrepHash.put(objId, new ColObjInfo(objId, row[1].toString(), (String)row[2]));
                }
            }
        }

        /* (non-Javadoc)
         * @see javax.swing.SwingWorker#doInBackground()
         */
        @Override
        protected Integer doInBackground() throws Exception {
             return collect();
        }

        /* (non-Javadoc)
         * @see javax.swing.SwingWorker#done()
         */
        @Override
        protected void done()
        {
            super.done();

            UIRegistry.getStatusBar().setProgressDone(LOAN_LOADR);

            if (recordSet != null && recordSet.getNumItems() > 2)
            {
                UIRegistry.clearSimpleGlassPaneMsg();
            }

            if (isFor == forAcc || isFor == forLegalDeacc) {
                cosLoaded(recordSet, prepsProvider);
            } else {
                prepsLoaded(coToPrepHash, prepTypeHash, prepsProvider, infoRequest, recordSet.getDbTableId());
            }
        }

    }


}
