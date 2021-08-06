package edu.ku.brc.specify.prefs;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.GenericPrefsPanel;
import edu.ku.brc.af.prefs.PrefsPanelIFace;
import edu.ku.brc.af.prefs.PrefsSavable;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.validation.ValCheckBox;
import edu.ku.brc.af.ui.forms.validation.ValComboBox;
import edu.ku.brc.af.ui.forms.validation.ValComboBoxFromQuery;
import edu.ku.brc.af.ui.forms.validation.ValSpinner;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.*;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.busrules.LoanGiftShipmentBusRules;
import edu.ku.brc.specify.dbsupport.SpecifySchemaUpdateService;
import edu.ku.brc.specify.tasks.subpane.qb.QBDataSourceListenerIFace;
import edu.ku.brc.specify.tools.export.ExportPanel;
import edu.ku.brc.specify.tools.gbifregistration.GbifSandbox;
import edu.ku.brc.specify.treeutils.TreeRebuilder;
import edu.ku.brc.ui.ProgressDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

import edu.ku.brc.util.Pair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import static edu.ku.brc.specify.conversion.BasicSQLUtils.update;
import static edu.ku.brc.specify.datamodel.busrules.LoanBusRules.DUEINMONTHS;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static java.util.Calendar.*;


public class S2nPrefsPanel  extends GenericPrefsPanel implements PrefsSavable, PrefsPanelIFace, QBDataSourceListenerIFace {
    protected static final Logger log            = Logger.getLogger(S2nPrefsPanel.class);
    JButton aboutBtn;
    JButton sendBtn;
    ValCheckBox onChk;
    JTextField statDsp;

    /**
     * Constructor of the S2nPrefsPanel setting panel.
     */
    public S2nPrefsPanel()      {
        super();
        createUI();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.prefs.GenericPrefsPanel#savePrefs()
     */
    @Override
    public void savePrefs()
    {
        AppPreferences prefs = AppPreferences.getRemote();
        prefs.putBoolean("S2n.S2nOn." + AppContextMgr.getInstance().getClassObject(Collection.class).getId() , onChk.isSelected());
    }

    protected String getStynthyStatus(boolean isOn) {
        if (isOn) {
            Timestamp exported = BasicSQLUtils.querySingleObj("select lastexported from spstynthy where collectionid =" + getCurrentCollectionId());
            if (exported != null) {
                return String.format(getResourceString("S2nPrefsPanel.ExportStatus"), exported.toLocalDateTime().toString());
            } else {
                return getResourceString("S2nPrefsPanel.StatusNeverExported");
            }
        }
        return getResourceString("S2nPrefsPanel.Disabled");
    }

    /**
     * Create the UI for the panel
     */
    protected void createUI() {
        createForm("Preferences", "S2nPrefs");
        AppPreferences prefs = AppPreferences.getRemote();

        Boolean isOn = prefs.getBoolean("S2n.S2nOn." + AppContextMgr.getInstance().getClassObject(Collection.class).getId(), false);

        FormViewObj fvo = (FormViewObj) form;


        aboutBtn = form.getCompById("S2nAbout");
        if (aboutBtn != null) {
            aboutBtn.addActionListener(e -> openSyftoriumAboutInBrowser());
        }
        statDsp = fvo.getCompById("S2nStatus");
        if (statDsp != null) {
            statDsp.setText(getStynthyStatus(isOn));
            statDsp.setFont(statDsp.getFont().deriveFont(Font.ITALIC));
        }
        sendBtn      = form.getCompById("SendData"); //$NON-NLS-1$
        sendBtn.addActionListener(e -> buildStinkyDwCArchive() );
        sendBtn.setEnabled(isOn);
        onChk = fvo.getCompById("S2nOn");
        if (onChk != null) {
            onChk.setSelected(isOn);
            if (isOn) {
                UIRegistry.loadAndPushResourceBundle("preferences");
                onChk.setText(getResourceString("S2n.OptedIn"));
                UIRegistry.popResourceBundle();
            }
            onChk.addActionListener(e -> inOrOut());

        }
    }

    protected void openSyftoriumAboutInBrowser() {
        String url = "https://syftorium.org";
        try {
            URI uri = new URL(url).toURI();
            Desktop.getDesktop().browse(uri);
        } catch (Exception x) {
            log.error(x);
            x.printStackTrace();
        }
    }

    protected Integer getCurrentCollectionId() {
        return AppContextMgr.getInstance().getClassObject(Collection.class).getId();
    }

    protected boolean setUpStynthyRec() {
        Vector<Object[]> rec = BasicSQLUtils.query("select * from spstynthy where collectionid =" + getCurrentCollectionId());
        if (rec == null || rec.size() == 0) {
            int r = BasicSQLUtils.update("insert into spstynthy(TimestampCreated, CollectionID) values(now(), " + getCurrentCollectionId() + ")");
            if (r != 1) {
                return false;
            }
            rec = BasicSQLUtils.query("select * from spstynthy where collectionid =" + getCurrentCollectionId());

        }
        Element dwcMeta = XMLHelper.readDOMFromConfigDir("S2nDwcMeta.xml");
        Element mapping = XMLHelper.readDOMFromConfigDir("S2nSchemaMapping.xml");
        String dwcMetaStr = dwcMeta.asXML();
        String mappingStr = mapping.asXML();
        int r = BasicSQLUtils.update("update spstynthy set MetaXML = '" + dwcMetaStr.replaceAll("'", "''") + "', MappingXML = '" + mappingStr.replaceAll("'", "''") + "' where collectionid=" + getCurrentCollectionId());        return r == 1;
    }


    protected boolean optInOrOut() {
        boolean result = onChk.isSelected();
        if (!result) {
            return true; //registration is forever, for now
        } else {
            result = setUpStynthyRec();
            if (result) {
                Pair<Integer, String> url = getS2NUrl();
                if (url.getFirst() == 200) {
                    String s2nUrl = url.getSecond().replaceAll("\"","");
                    String instName = AppContextMgr.getInstance().getClassObject(Institution.class).getName();
                    String collName = AppContextMgr.getInstance().getClassObject(Collection.class).getCollectionName();
                    String collGUID = AppContextMgr.getInstance().getClassObject(Collection.class).getGuid();
                    String regJSON = "{\"collection_name\": \"" + collName + "\", \"institution_name\": \"" + instName
                            + "\",\"collection_id\": \"" + collGUID + "\",\"collection_location\": \"\",\"contact_email\": \"\",\"contact_name\": \"\",\"public_key\": \"COLLECTION PUBLIC KEY\" }";
                    Pair<Integer, String> regResponse = regS2NCol(regJSON, s2nUrl);
                    if (regResponse.getFirst() != 200) {
                       result = false;
                        UIRegistry.displayErrorDlg( UIRegistry.getResourceString("S2nPrefsPanel.CollectionCouldNotBeRegistered"));
                    } else {
                        UIRegistry.displayInfoMsgDlg(UIRegistry.getResourceString("S2nPrefsPanel.CollectionRegistered"));
                    }
                }
            }
            return result;
        }
    }



    protected void inOrOut() {
        boolean optedIn = optInOrOut();
        //maybe need to disable event-handling...but so far no
        onChk.setSelected(optedIn);
        sendBtn.setEnabled(optedIn);
        if (statDsp != null) {
            statDsp.setText(getStynthyStatus(optedIn));
        }
    }



    protected Object getStynthyRegInfoForCollection() {
        return new String("reginfo");
    }

    protected Object getSynthyUpdateInfo() {

        /* for automatic updates...
        Object[] dat = BasicSQLUtils.queryForRow("select datediff(now(), lastexported) - UpdatePeriodDays from spstynthy where collectionid = " + getCurrentCollectionId());
        Long days = dat[0] != null ? Long.valueOf(dat[0].toString()) : null;
        return days == null || days >= 0;
        ...*/
        /* for testing...
        String dateStrFromUser = UIRegistry.askForString("ENTERADATE", "TESTINGFAKEOUT", "YYYY-MM-DD", false);
        if (dateStrFromUser.equalsIgnoreCase("rebuild")) dateStrFromUser = null;
        return dateStrFromUser;
        ...*/
        Timestamp result = BasicSQLUtils.querySingleObj("select lastexported from spstynthy where collectionid = " + getCurrentCollectionId());
        return result;
    }

    protected boolean needToDoIt(Object lastUpdateInfo) {
        //for now...
        if (lastUpdateInfo instanceof Boolean) {
            return ((Boolean) lastUpdateInfo).booleanValue();
        } else if (lastUpdateInfo instanceof String) {
            return true; //testing fakery
        }
        return false;
    }

    protected SpExportSchemaMapping loadMapping(Object lastUpdateInfo) {
        Element mapEl = GbifSandbox.getXmlElementFromFld("select MappingXML from spstynthy where collectionid =" + getCurrentCollectionId());
        SpExportSchemaMapping result = null;
        SpQuery mapQ = new SpQuery();
        mapQ.initialize();
        mapQ.fromXML(mapEl.element("query"));
        for (SpQueryField f : mapQ.getFields()) {
            if (f.getMapping() != null) {
                result =  f.getMapping().getExportSchemaMapping();
                break;
            }
        }
        if (result != null) {
            Date date = Calendar.getInstance().getTime();
            if (lastUpdateInfo != null) {
                /** testing...
                String update = lastUpdateInfo.toString();
                date.setYear(Integer.valueOf(update.substring(0, 4)) - 1900);
                date.setMonth(Integer.valueOf(update.substring(5, 7)) - 1);
                date.setDate(Integer.valueOf(update.substring(8)));
                 result.setTimestampExported(new Timestamp(date.getTime()));
                 ...*/
                if (lastUpdateInfo instanceof Timestamp) {
                    result.setTimestampExported((Timestamp)lastUpdateInfo);
                }
            } else {
                result.setTimestampExported(null);
            }
            Calendar now = Calendar.getInstance();
            result.setMappingName(AppContextMgr.getInstance().getClassObject(Collection.class).getCollectionName().replaceAll(" ", "_")
                    + "_S2n_" + getCalendarTimestamp(now, true));
        }
        return result;
    }


    static public String getSynthyTblCreateSQL() {
        return " CREATE TABLE `spstynthy` ( " +
                "`SpStynthyID` int(11) NOT NULL AUTO_INCREMENT, " +
                "`TimestampCreated` datetime NOT NULL, " +
                "`TimestampModified` datetime DEFAULT NULL, " +
                "`MetaXML` mediumblob DEFAULT NULL, " +
                "`UpdatePeriodDays` int(11) NOT NULL DEFAULT 30, " +
                "`LastExported` datetime DEFAULT NULL, " +
                "`CollectionID` int(11) NOT NULL, " +
                "`MappingXML` mediumblob DEFAULT NULL, " +
                "`Key1` varchar(256) default null, " +
                "`Key2` varchar(256) default null, " +
                "PRIMARY KEY (`SpStynthyID`) " +
                ") ENGINE=InnoDB AUTO_INCREMENT=277 DEFAULT CHARSET=utf8;";
    }
    static private Pair<Integer, String> getS2NUrl() {
        final CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet getMethod = new HttpGet("http://broker.spcoco.org/api/v1/address");
        int status = -1;
        try {
            CloseableHttpResponse response = httpClient.execute(getMethod);
            status = response.getStatusLine().getStatusCode();
            if (status == 200) {
                return new Pair<>(status, EntityUtils.toString(response.getEntity()));
            }
        } catch(Exception x) {
            x.printStackTrace();
        }
        return new Pair<>(status, null);
    }

    static private Pair<Integer, String> regS2NCol(String regJson, String url) {
        final CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost postMethod  = new HttpPost(url + "/collection");
        int status = -1;
        try {
            StringEntity se = new StringEntity(regJson, "application/json", StandardCharsets.UTF_8.toString());
            postMethod.setEntity(se);
            CloseableHttpResponse response = httpClient.execute(postMethod);
            status = response.getStatusLine().getStatusCode();
            if (status == 200) {
                return new Pair<>(status, EntityUtils.toString(response.getEntity()));
            }
        } catch(Exception x) {
            x.printStackTrace();
        }
        return new Pair<>(status, null);
    }

    static private void setLastExported(Calendar lastExported) {
        String timestamp = getCalendarTimestamp(lastExported, false);
        BasicSQLUtils.update("update spstynthy set lastexported = '" + timestamp + "' where collectionid ="
            + AppContextMgr.getInstance().getClassObject(Collection.class).getId());
    }

    static private String getCalendarTimestamp(Calendar c, boolean isForFileName) {
        String separator = isForFileName ? "_" : ":";
        String result = c.get(YEAR) + separator + (c.get(MONTH) + 1) + separator + c.get(DAY_OF_MONTH);
        separator = isForFileName ? "_" : " ";
        result += separator;
        separator = isForFileName ? "_" : ":";
        result += c.get(HOUR_OF_DAY) + separator + c.get(MINUTE) + separator + c.get(SECOND);
        return result;
    }

    static public Pair<Integer, String> postFile(String filePath, Calendar startTime) {
        final CloseableHttpClient httpClient = HttpClients.createDefault();
        Pair<Integer, String> res = getS2NUrl();
        if (res.getFirst() == 200) {
            String url = res.getSecond().replaceAll("\"", "");
            String collGUID = AppContextMgr.getInstance().getClassObject(Collection.class).getGuid();
            HttpPost postMethod = new HttpPost(url + "/collection/" + collGUID + "/occurrences/");
            res.setFirst(-1);
            try {
                File file = new File(filePath);
                if (!file.exists()) {
                    res.setSecond("file not found");
                } else {
                    postMethod.setEntity(new FileEntity(file));
                    CloseableHttpResponse response = httpClient.execute(postMethod);
                    if (204 == response.getStatusLine().getStatusCode()) {
                        res.setFirst(204);
                        res.setSecond("OK");
                        setLastExported(startTime);
                    }
                }
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
        return res;
    }

    public static void nothingToUpdate() {
        UIRegistry.displayInfoMsgDlg(getResourceString("S2nPrefsPanel.NoChangesSinceLastUpdate"));
    }

    ProgressDialog progDlg;
    javax.swing.SwingWorker<Pair<Integer, String>, Object> worker;

    public void buildStinkyDwCArchive() {
        Object regInfo = getStynthyRegInfoForCollection();
        if (regInfo == null) {
            //shouldn't happen
            return;
        }
        Object lastUpdateInfo = getSynthyUpdateInfo();
//        if (lastUpdateInfo == null) {
//            //shouldn't happen
//            return;
//        }
//        if (needToDoIt(lastUpdateInfo)) {
            SpExportSchemaMapping mapping = loadMapping(lastUpdateInfo);
            if (mapping != null) {
                progDlg = new ProgressDialog(getResourceString("S2nPrefsPanel.ExportingToSyftorium"), false, true);
                progDlg.setResizable(false);
                progDlg.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                progDlg.setModal(true);
                progDlg.setAlwaysOnTop(true);
                progDlg.getProcessProgress().setIndeterminate(true);
                JButton closer = progDlg.getCloseBtn();
                ActionListener[] listeners = closer.getActionListeners();
                for (ActionListener listener : listeners) {
                    closer.removeActionListener(listener);
                }
                closer.addActionListener(e -> {
                    //UIRegistry.displayInfoMsgDlg("you pressed the close button");
                    SwingUtilities.invokeLater(() -> {
                        if (UIRegistry.displayConfirmLocalized("S2nPrefsPanel.ConfirmCancelTitle", "S2nPrefsPanel.ConfirmCancelMsg", "Yes", "No", JOptionPane.QUESTION_MESSAGE)) {
                            worker.cancel(true);
                            progDlg.setVisible(false);
                        }
                    });
                });
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        UIHelper.centerAndShow(progDlg);
                    }
                });

                worker = ExportPanel.buildStinkyDwCArchive(mapping, this);
            }
//        }
    }

    public static String getOutputFileName(SpExportSchemaMapping schemaMapping) {
        return  UIRegistry.getAppDataDir() + File.separator  + schemaMapping.getMappingName();
    }

    Long currentExportRow;
    Long rowsToExport;

    @Override
    public void currentRow(long currentRow) {
        //System.out.println("exported " + currentRow);
    }

    @Override
    public void rowCount(long rowCount) {
        //System.out.println(rowCount + " rows to export.");
        rowsToExport = rowCount;
        currentExportRow = Long.valueOf(0L);
        progDlg.getProcessProgress().setIndeterminate(false);
        progDlg.setProcess(0, rowsToExport.intValue());
        progDlg.setDesc(getResourceString("S2nPrefsPanel.BuildingArchive"));
    }

    @Override
    public void anotherRow() {
        currentExportRow++;
        progDlg.setProcess(currentExportRow.intValue());
        //System.out.println(currentExportRow + " of " + rowsToExport);
    }

    @Override
    public void fyi(String info) {
        //System.out.println(info);
        progDlg.setDesc(info);
    }

    @Override
    public void done(long rows) {
        //System.out.println("all done");
        progDlg.processDone();
        progDlg.setVisible(false);
        Pair<Integer, String> result = new Pair<>(-1, "Unspecified error");
        if (!worker.isCancelled()) {
            try {
                result = worker.get();
            } catch (InterruptedException | ExecutionException x) {
                log.error(x);
            }
            if (result.getFirst() == 204) {
                UIRegistry.displayInfoMsgDlg(getResourceString("S2nPrefsPanel.ExportSuccessful"));
                if (statDsp != null) {
                    statDsp.setText(getStynthyStatus(true));
                }
            } else if (result.getFirst() != 0) {
                UIRegistry.displayInfoMsgDlg(String.format(getResourceString("S2nPrefsPanel.ExportFail"), result.getSecond()));
            }
        }
    }

    @Override
    public void loading() {
        //System.out.println("loading");
        progDlg.setDesc(getResourceString("S2nPrefsPanel.Loading"));
    }

    @Override
    public void loaded() {
        //System.out.println("loaded");
        progDlg.setDesc(getResourceString("S2nPrefsPanel.Loaded"));
    }

    @Override
    public void filling() {
        System.out.println("filling");
    }

    @Override
    public boolean isListeningClosely() {
        return false;
    }

    @Override
    public boolean doTellAll() {
        return false;
    }

    @Override
    public void deletedRecs(List<Integer> keysDeleted) {
        System.out.println("deletedRecs");
    }

    @Override
    public void updatedRec(Integer key) {
        System.out.println("updatedRec");
    }

    @Override
    public void addedRec(Integer key) {
        System.out.println("addedRec");
    }

    public static void main(String[] args) {
        try {
            Pair<Integer, String> url = getS2NUrl();
            System.out.println(url.getFirst() + " - " + url.getSecond());
            if (url.getFirst() == 200) {
                String s2nUrl = url.getSecond().replaceAll("\"","");
                String regJson = "{\"collection_name\": \"Fish Tissue Collection\", \"institution_name\": \"University of Kansas\",\"collection_id\": \"ku_fish_tissue_test_1j\",\"collection_location\": \"Lawrence, Kansas USA\",\"contact_email\": \"fish_collection@example.com\",\"contact_name\": \"Fish Contact\",\"public_key\": \"COLLECTION PUBLIC KEY\" }";
                Pair<Integer, String> regResponse = regS2NCol(regJson, s2nUrl);
                if (regResponse.getFirst() == 200) {
                    //Pair<Integer, String> filePostResp = postFile("/home/timo/Specify/KU_Fish_Tissue_Collection_S2n_2021_6_3.zip", s2nUrl, "ku_fish_tissue_test_1j");
                }
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
    }
}