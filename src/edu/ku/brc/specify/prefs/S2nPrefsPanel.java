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
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.*;
import edu.ku.brc.specify.datamodel.busrules.LoanGiftShipmentBusRules;
import edu.ku.brc.specify.tools.export.ExportPanel;
import edu.ku.brc.specify.tools.gbifregistration.GbifSandbox;
import edu.ku.brc.ui.UIRegistry;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import org.dom4j.Element;

import static edu.ku.brc.specify.datamodel.busrules.LoanBusRules.DUEINMONTHS;
import static java.util.Calendar.*;


public class S2nPrefsPanel  extends GenericPrefsPanel implements PrefsSavable, PrefsPanelIFace {
    JButton sendBtn;
    ValCheckBox onChk;

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
        prefs.putBoolean("S2n.S2nOn", onChk.isSelected());
    }
    /**
     * Create the UI for the panel
     */
    protected void createUI() {
        createForm("Preferences", "S2nPrefs");
        AppPreferences prefs = AppPreferences.getRemote();

        Boolean isOn = prefs.getBoolean("S2n.S2nOn", false);

        FormViewObj fvo = (FormViewObj) form;

        sendBtn      = form.getCompById("SendData"); //$NON-NLS-1$
        sendBtn.addActionListener(e -> {UIRegistry.showLocalizedMsg("SENDING_SYFTY_DATA"); buildStinkyDwCArchive(); });
        sendBtn.setEnabled(isOn);
        onChk = fvo.getCompById("S2nOn");
        if (onChk != null) {
            onChk.setSelected(isOn);
            onChk.addActionListener(e -> inOrOut());

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
        if (result) {
            result = setUpStynthyRec();
        }
        return onChk.isSelected();
    }

    protected void inOrOut() {
        boolean optedIn = optInOrOut();
        //maybe need to disable event-handling...but so far no
        onChk.setSelected(optedIn);
        sendBtn.setEnabled(optedIn);
    }



    protected Object getStynthyRegInfoForCollection() {
        return new String("reginfo");
    }

    protected Object getSynthyUpdateInfo(Object regInfo) {
        //until server is up
        if ("reginfo".equals(regInfo)){
            //Object[] dat = BasicSQLUtils.queryForRow("select datediff(now(), lastexported) - UpdatePeriodDays from spstynthy where collectionid = " + getCurrentCollectionId());
            //Long days = dat[0] != null ? Long.valueOf(dat[0].toString()) : null;
            //return days == null || days >= 0;
            String dateStrFromUser = UIRegistry.askForString("ENTERADATE","TESTINGFAKEOUT", "YYYY-MM-DD", false);
            if (dateStrFromUser.equalsIgnoreCase("rebuild")) dateStrFromUser = null;
            return dateStrFromUser;
        } else {
            return null;
        }
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
                String update = lastUpdateInfo.toString();
                date.setYear(Integer.valueOf(update.substring(0, 4)) - 1900);
                date.setMonth(Integer.valueOf(update.substring(5, 7)) - 1);
                date.setDate(Integer.valueOf(update.substring(8)));
                result.setTimestampExported(new Timestamp(date.getTime()));
            } else {
                result.setTimestampExported(null);
            }
            Calendar now = Calendar.getInstance();
            result.setMappingName(AppContextMgr.getInstance().getClassObject(Collection.class).getCollectionName().replaceAll(" ", "_")
                    + "_S2n_" + now.get(YEAR) + "_" + (now.get(MONTH) + 1) + "_" + now.get(DAY_OF_MONTH) + "_"
                    + now.get(HOUR_OF_DAY) + "_" + now.get(MINUTE) + "_" + now.get(SECOND));
        }
        return result;
    }

    public void buildStinkyDwCArchive() {
        Object regInfo = getStynthyRegInfoForCollection();
        if (regInfo == null) {
            //shouldn't happen
            return;
        }
        Object lastUpdateInfo = getSynthyUpdateInfo(regInfo);
//        if (lastUpdateInfo == null) {
//            //shouldn't happen
//            return;
//        }
//        if (needToDoIt(lastUpdateInfo)) {
            SpExportSchemaMapping mapping = loadMapping(lastUpdateInfo);
            if (mapping != null) {
                ExportPanel.buildStinkyDwCArchive(mapping);
            }
//        }
    }

}
