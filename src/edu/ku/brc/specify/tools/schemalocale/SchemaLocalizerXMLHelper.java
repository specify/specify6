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
package edu.ku.brc.specify.tools.schemalocale;

import com.thoughtworks.xstream.XStream;
import edu.ku.brc.af.core.SchemaI18NService;
import edu.ku.brc.af.core.db.*;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.config.DisciplineType;
import edu.ku.brc.specify.config.init.BldrPickList;
import edu.ku.brc.specify.config.init.BldrPickListItem;
import edu.ku.brc.specify.config.init.DataBuilder;
import edu.ku.brc.specify.datamodel.*;
import edu.ku.brc.ui.ToggleButtonChooserDlg;
import edu.ku.brc.ui.ToggleButtonChooserPanel;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.*;
import java.util.List;

import static edu.ku.brc.helpers.XMLHelper.getAttr;
import static edu.ku.brc.specify.config.init.DataBuilder.createPickList;

/**
 * THis is a helper class for reading and writing the Schema Description XML to a file.
 * 
 * @author rod
 *
 * @code_status Beta
 *
 * Sep 26, 2007
 *
 */
public class SchemaLocalizerXMLHelper implements LocalizableIOIFace
{
    private static final Logger log = Logger.getLogger(SchemaLocalizerXMLHelper.class);
    
    protected static String    fileName[]   = {"schema_localization.xml", "wbschema_localization.xml"};
    protected static boolean   doFixNames = true;
    
    protected static LocalizableStrFactory                 localizableStrFactory;
    protected Byte                                         schemaType;
    
    protected DBTableIdMgr                                 tableMgr;
    
    protected List<Locale>                                 availLocales = new ArrayList<Locale>();
    protected Vector<DisciplineBasedContainer>             tables     = new Vector<DisciplineBasedContainer>();
    protected Hashtable<String, LocalizableContainerIFace> tableHash  = new Hashtable<String, LocalizableContainerIFace>();
    
    protected Vector<LocalizableJListItem>                 tableDisplayItems;
    protected Hashtable<String, LocalizableJListItem>      tableDisplayItemsHash = new Hashtable<String, LocalizableJListItem>();
    
    protected Hashtable<LocalizableJListItem, Vector<LocalizableJListItem>> itemJListItemsHash = new Hashtable<LocalizableJListItem, Vector<LocalizableJListItem>>();
    
    protected boolean                                      hasTableInfoChanged      = false;
    protected boolean                                      changesMadeDuringStartup = false;
    protected File                                         inputFile                = null;
    
    
    // Used for Caching the lists
    protected Vector<LocalizableStrIFace> namesList = new Vector<LocalizableStrIFace>();
    protected Vector<LocalizableStrIFace> descsList = new Vector<LocalizableStrIFace>();

    protected StringBuilder changesBuffer = new StringBuilder();
    

    /**
     * 
     */
    public SchemaLocalizerXMLHelper(final Byte         schemaType, 
                                    final DBTableIdMgr tableMgr)
    {
        this.schemaType = schemaType;
        this.tableMgr   = tableMgr;
    }
    
    /**
     * @return the localizableStrFactory
     */
    public static LocalizableStrFactory getLocalizableStrFactory()
    {
        return localizableStrFactory;
    }
    
    /**
     * @param localizableStrFactory the localizableStrFactory to set
     */
    public static void setLocalizableStrFactory(LocalizableStrFactory localizableStrFactory)
    {
        SchemaLocalizerXMLHelper.localizableStrFactory = localizableStrFactory;
    }

    /**
     * @return
     */
    public Vector<DisciplineBasedContainer> getSpLocaleContainers()
    {
        return tables;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#load(boolean)
     */
    @Override
    public boolean load(final boolean useCurrentLocaleOnly)
    {
        return loadWithExternalFile(null, useCurrentLocaleOnly);
    }
    
    /**
     * @param externalFile
     * @param useCurrentLocaleOnly
     * @return
     */
    public boolean loadWithExternalFile(final File externalFile, final boolean useCurrentLocaleOnly)
    {

        tables = load(null, externalFile, useCurrentLocaleOnly);
        
        boolean loadedOk = tables != null;
        if (loadedOk && externalFile == null)
        {
            for (DisciplineType disciplineType : DisciplineType.getDisciplineList())
            {
                Vector<DisciplineBasedContainer> dispContainers = load(disciplineType.getName(), null, useCurrentLocaleOnly);
                //check for non-english texts, which are unsupported and break localization (as of 1/20)
                //technically just checking for more than one text, assuming existing single text is english
                if (dispContainers != null) {
                    for (DisciplineBasedContainer c : dispContainers) {
                        for (SpLocaleContainerItem i : c.getItems()) {
                            if (i.getNames().size() > 1 || i.getDescs().size() > 1) {
                                log.warn(c.getName() + "." + i.getName() + " (" + disciplineType + "): Multiple langs are not supported for discpline-specific l10n schemas.");
                            }
                        }
                    }
                }
                addDisplineBasedContainers(disciplineType.getName(), dispContainers);
            }
        }
        
        return loadedOk;
    }

    protected String processText(Element t, String margin, SpLocaleItemStr itemStr) {
        Element text = (Element)t.selectSingleNode("text");
        String textStr = itemStr != null ? itemStr.getText() : (text == null ? null : text.getText());
        //System.out.println(margin + textStr);
        return textStr;
    }

    protected void incrementCnt(String cntKey, Map<String, Integer> cnts) {
        Integer cnt = cnts.get(cntKey);
        cnts.put(cntKey, cnt == null ? 1 : cnt + 1);
    }

    protected SpLocaleItemStr spLocaleItemStrFromXml(Element e) {
        SpLocaleItemStr result = new SpLocaleItemStr();
        result.initialize();
        String lang = getAttr(e, "language", null);
        String country = getAttr(e, "country", null);
        String variant = getAttr(e, "variant", null);
        result.setLanguage("".equals(lang) ? null : lang);
        result.setCountry("".equals(country) ? null : country);
        result.setVariant("".equals(variant) ? null : variant);
        return result;
    }

    protected SpLocaleItemStr spLocaleItemStrCopy(SpLocaleItemStr s) {
        SpLocaleItemStr result = new SpLocaleItemStr();
        result.initialize();
        String lang = s.getLanguage();
        String country = s.getCountry();
        String variant = s.getVariant();
        result.setLanguage("".equals(lang) ? null : lang);
        result.setCountry("".equals(country) ? null : country);
        result.setVariant("".equals(variant) ? null : variant);
        return result;
    }


    protected SpLocaleItemStr processTextElement(String textType, Element e, String margin, Map<String, Integer> cnts, SpLocaleItemStr name) {
        String reference = textType.equals("descs") ? getAttr(e, "reference", null) : null;
        boolean useName = false;
        if (reference != null) {
            if (reference.startsWith("../../names/str") && name != null) {
                useName = true;
            } else {
                System.out.println("ignoring reference: " + reference);
            }
        }
        String lang = useName ? name.getLanguage() : getAttr(e, "language", "");
        String country = useName ? name.getCountry() : getAttr(e, "country", "");
        String variant = useName ? name.getVariant() : getAttr(e, "variant", "");
        String cntKey = textType + "/" + lang + "/" + country + "/" + variant + "/";
        incrementCnt(cntKey, cnts);
        String text = processText(e, margin, useName ? name : null);
        SpLocaleItemStr result = useName ? spLocaleItemStrCopy(name) : spLocaleItemStrFromXml(e);
        result.setText(text);
        if (text == null) {
            incrementCnt("null/" + cntKey, cnts);
        }
        return result;
    }

    protected Pair<List<SpLocaleItemStr>, List<SpLocaleItemStr>> processTexts(Element e, String margin, Map<String, Integer> cnts) {
        List<SpLocaleItemStr> names = new ArrayList<>();
        List<SpLocaleItemStr> descs = new ArrayList<>();
        for (Object nameObj : e.selectNodes("names/str")) {
            names.add(processTextElement("names", (Element)nameObj, margin, cnts, null));
        }
        int i = 0;
        for (Object descObj : e.selectNodes("descs/str")) {
            SpLocaleItemStr name = i < names.size() ? names.get(i++) : null;
            descs.add(processTextElement("descs", (Element)descObj, margin, cnts, name));
        }
        return new Pair<>(names, descs);
    }

    protected void spLocaleBaseFromXml(SpLocaleBase lb, Element e) {
        lb.setName(getAttr(e, "name", null));
        lb.setIsHidden(getAttr(e, "isHidden", false));
        lb.setType(getAttr(e, "type", null));
        lb.setFormat(getAttr(e, "format", null));
        lb.setIsUIFormatter(Boolean.valueOf(getAttr(e, "isUIFormatter", null)));
        lb.setPickListName(getAttr(e, "pickListName", null));
    }

    protected DisciplineBasedContainer containerFromXml(Element e) {
        //SpLocaleContainer c = new SpLocaleContainer();
        DisciplineBasedContainer c = new DisciplineBasedContainer();
        c.initialize();
        spLocaleBaseFromXml(c, e);
        c.setSchemaType(this.schemaType);
        Element agg = (Element)e.selectSingleNode("aggregator");
        if (agg != null) {
            c.setAggregator(e.getText());
        }
        return c;
    }

    protected SpLocaleContainerItem containerItemFromXml(Element e) {
        SpLocaleContainerItem ci = new SpLocaleContainerItem();
        ci.initialize();
        spLocaleBaseFromXml(ci, e);
        ci.setIsRequired(getAttr(e, "isRequired", false));
        ci.setWebLinkName(getAttr(e, "webLinkName", null));
        return ci;
    }

    protected void addTextsToContainer(SpLocaleContainer c, Pair<List<SpLocaleItemStr>,List<SpLocaleItemStr>> texts) {
        for (SpLocaleItemStr i : texts.getFirst()) {
            c.getNames().add(i);
            i.setContainerName(c);
        }
        for (SpLocaleItemStr i : texts.getSecond()) {
            c.getDescs().add(i);
            i.setContainerDesc(c);
        }
    }

    protected void addTextsToContainerItem(SpLocaleContainerItem ci, Pair<List<SpLocaleItemStr>,List<SpLocaleItemStr>> texts) {
        for (SpLocaleItemStr i : texts.getFirst()) {
            ci.getNames().add(i);
            i.setItemName(ci);
        }
        for (SpLocaleItemStr i : texts.getSecond()) {
            ci.getDescs().add(i);
            i.setItemDesc(ci);
        }
    }

    public Vector<DisciplineBasedContainer> oldSchoolStepByStep(File file) throws Exception {
//        String fName = fileName[this.schemaType];
//        if (prefix != null) {
//            fName = prefix + File.separator + fName;
//        }
//        File file = XMLHelper.getConfigDir(fName);
        Element e = XMLHelper.readFileToDOM4J(file);
        int containerCnt = 0;
        int itemCnt = 0;
        Map<String, Integer> textCnts = new HashMap<>();
        Vector<DisciplineBasedContainer> result = new Vector<>();
        for (Object obj : e.selectNodes("container")) {
            Element container = (Element)obj;
            DisciplineBasedContainer c = containerFromXml(container);
            containerCnt++;
            //System.out.println(getAttr(container, "name", null));
            String margin = "  ";
            Pair<List<SpLocaleItemStr>,List<SpLocaleItemStr>> texts = processTexts(container, margin, textCnts);
            addTextsToContainer(c, texts);
            //System.out.println();
            for (Object itemObj : container.selectNodes("items/desc")) {
                Element item = (Element)itemObj;
                SpLocaleContainerItem ci = containerItemFromXml(item);
                ci.setContainer(c);
                c.getItems().add(ci);
                itemCnt++;
                //System.out.println(margin + getAttr(item, "name", null));
                margin += "  ";
                texts = processTexts(item, margin, textCnts);
                addTextsToContainerItem(ci, texts);
                margin = margin.substring(2);
            }
            result.add(c);
        }
        //System.out.println("Done. Containers: " + containerCnt + ", Items: " + itemCnt);
//        List<String> cnts = new ArrayList<>();
//        for (Map.Entry<String, Integer> me : textCnts.entrySet()) {
//            cnts.add("  " + me.getKey() + ": " + me.getValue());
//        }
//        Collections.sort(cnts);
//        for (String cnt : cnts) {
//            System.out.println(cnt);
//        }
        return result;
    }
    /**
     * Merges a disciplineType set of containers into the baseline.
     * @param disciplineType the disciplineType to be added
     * @param dispContainers  the containers for that disciplineType
     */
    protected void addDisplineBasedContainers(final String discipline, 
                                              final Vector<DisciplineBasedContainer> dispContainers)
    {
        if (dispContainers != null && dispContainers.size() > 0)
        {
            for (DisciplineBasedContainer dspContainer : dispContainers)
            {
                DisciplineBasedContainer container = (DisciplineBasedContainer)tableHash.get(dspContainer.getName());
                if (container != null)
                {
                    for (SpLocaleContainerItem item : dspContainer.getItems())
                    {
                        container.add(discipline, item);
                    }
                } else
                {
                    log.info("Couldn't find continer ["+dspContainer.getName()+"]");
                }
            }
        }
    }
    
    /**
     * 
     */
    private void discoverLocalesFromData(final Vector<DisciplineBasedContainer> containers)
    {
        HashSet<String> hash = new HashSet<String>();
        
        for (DisciplineBasedContainer container : containers)
        {
            for (SpLocaleItemStr str : container.getNames())
            {
                String language = str.getLanguage();
                String country  = str.getCountry();
                String variant  = str.getVariant();
                
                String key = String.format("%s_%s_%s", language, country != null ? country : "", variant != null ? variant : "");
                if (!hash.contains(key))
                {
                    Locale locale = null;
                    if (StringUtils.isNotBlank(language) && StringUtils.isNotBlank(country) && StringUtils.isNotBlank(variant))
                    {
                        locale = new Locale(language, country, variant);
                        
                    } else if (StringUtils.isNotBlank(language) && StringUtils.isNotBlank(country))
                    {
                        locale = new Locale(language, country);
                        
                    } else if (StringUtils.isNotBlank(language))
                    {
                        locale = new Locale(language);
                    }
                    if (locale != null)
                    {
                        System.err.println("["+key+"] "+locale);
                        availLocales.add(locale);
                        hash.add(key);
                    }
                }
            }
        }
    }
    
    /**
     * @return the availLocales
     */
    public List<Locale> getAvailLocales()
    {
        return availLocales;
    }

    /**
     * @param language
     * @param containers
     */
    public void addMissingTranslations(final String language, final String countryArg,
                                       final Vector<DisciplineBasedContainer> containers)
    {
        int containerCnt = 0;
        int itemCnt      = 0;

        String country = "".equals(countryArg.trim()) ? null : countryArg;
        for (DisciplineBasedContainer container : containers)
        {
            if (container.getNamesSet().size() == 0)
            {
                log.debug("Container: "+container.getName()+" nameSet is empty.");
                SpLocaleItemStr str = new SpLocaleItemStr();
                str.initialize();
                str.setLanguage(language);
                str.setCountry(country);
                str.setText(container.getName());
                str.setContainerDesc(container);
                container.getNamesSet().add(str);
                containerCnt++;
            } else
            {
                for (SpLocaleItemStr str : container.getNamesSet())
                {
                    if (StringUtils.isEmpty(str.getText()))
                    {
                        str.setText(container.getName());
                        containerCnt++;
                    }
                }
            }
            if (container.getDescsSet().size() == 0)
            {
                log.debug("Container: "+container.getName()+" descSet is empty.");
                SpLocaleItemStr str = new SpLocaleItemStr();
                str.initialize();
                str.setLanguage(language);
                str.setCountry(country);
                str.setText(container.getName());
                str.setContainerDesc(container);
                container.getDescsSet().add(str);
                containerCnt++;
            } else
            {
                for (SpLocaleItemStr str : container.getDescsSet())
                {
                    if (StringUtils.isEmpty(str.getText()))
                    {
                        str.setText(container.getName());
                        containerCnt++;
                    }
                }
            }
            
            for (SpLocaleContainerItem sci : container.getItems())
            {
                if (sci.getNamesSet().size() == 0)
                {
                    log.debug(container.getName()+" Item: "+sci.getName()+" nameSet is empty.");
                    SpLocaleItemStr str = new SpLocaleItemStr();
                    str.initialize();
                    str.setLanguage(language);
                    str.setCountry(country);
                    str.setText(sci.getName());
                    str.setItemDesc(sci);
                    sci.getNamesSet().add(str);
                    itemCnt++;
                } else
                {
                    for (SpLocaleItemStr str : sci.getNamesSet())
                    {
                        if (StringUtils.isEmpty(str.getText()))
                        {
                            str.setText(sci.getName());
                            itemCnt++;
                        }
                    }
                }
                if (sci.getDescsSet().size() == 0)
                {
                    //log.debug(container.getName()+" Item: "+sci.getName()+" descSet is empty.");
                    SpLocaleItemStr str = new SpLocaleItemStr();
                    str.initialize();
                    str.setLanguage(language);
                    str.setCountry(country);
                    str.setText(sci.getName());
                    str.setItemDesc(sci);
                    sci.getDescsSet().add(str);
                    itemCnt++;
                } else
                {
                    for (SpLocaleItemStr str : sci.getDescsSet())
                    {
                        if (StringUtils.isEmpty(str.getText()))
                        {
                            str.setText(sci.getName());
                            itemCnt++;
                        }
                    }
                }
            }
        }
        
        log.debug(String.format("Added %d container strings, %d item strings.", containerCnt, itemCnt));
        if (containerCnt > 0 || itemCnt > 0)
        {
            changesMadeDuringStartup = true;
        }
    }
    
    /**
     * @param language
     * @param itemsSet
     */
    private void stripMergeLocale(final String language, final String countryArg, final Set<SpLocaleItemStr> itemsSet)
    {
        String country = countryArg == null ? "" : countryArg;
        SpLocaleItemStr en = null;
        SpLocaleItemStr loc = null;
        for (SpLocaleItemStr item : itemsSet) {
            String itemCountry = item.getCountry() == null ? "" : item.getCountry();
            if (item.getLanguage().equals(language) && itemCountry.equals(country)) {
                loc = item;
                break;
            } else if (item.getLanguage().equals("en") && itemCountry.equals("")) {
                en = item;
            }
        }
        if (loc == null && itemsSet.size() > 0) {
            if (en == null) {
                en = itemsSet.iterator().next();
                log.warn("No English Default Found For Locale Merge.");
            }
            en.setLanguage(language);
            en.setCountry("".equals(countryArg.trim()) ? null : countryArg);
            loc = en;
        }
        if (loc != null) {
            List<SpLocaleItemStr> items = new ArrayList<>(itemsSet);
            for (SpLocaleItemStr item : items) {
                if (item != loc) {
                    itemsSet.remove(item);
                }
            }
        }
    }
    
    /**
     * @param language
     * @param containers
     */
    private void stripToSingleLocale(final String language, final String country,
                                     final Vector<DisciplineBasedContainer> containers)
    {
        for (DisciplineBasedContainer container : containers)
        {
            stripMergeLocale(language, country, container.getNamesSet());
            stripMergeLocale(language, country, container.getDescsSet());
            
            for (SpLocaleContainerItem sci : container.getItems())
            {
                stripMergeLocale(language, country, sci.getNamesSet());
                stripMergeLocale(language, country, sci.getDescsSet());
            }
            /*for (LocalizableItemIFace sci : container.getContainerItems())
            {
                System.out.println(sci.getName());
                //stripMergeLocale(language, sci.get);
                //stripMergeLocale(language, sci.getDescsSet());
            }*/
        }
    }


    /**
     * a debugging aid
     */
//    private void seekSomethingBad() {
//        for (DisciplineBasedContainer container : containers) {
//            for (SpLocaleItemStr s : container.getNames()) {
//                if (!"RU".equals(s.getCountry())) {
//                    System.out.println("badness");
//                }
//            }
//            for (SpLocaleItemStr s : container.getDescs()) {
//                if (!"RU".equals(s.getCountry())) {
//                    System.out.println("badness");
//                }
//            }
//            for (SpLocaleContainerItem i : container.getItems()) {
//                for (SpLocaleItemStr s : i.getNames()) {
//                    if (!"RU".equals(s.getCountry())) {
//                        System.out.println("badness");
//                    }
//                }
//                for (SpLocaleItemStr s : i.getDescs()) {
//                    if (!"RU".equals(s.getCountry())) {
//                        System.out.println("badness");
//                    }
//                }
//            }
//        }
//
//    }
    /**
     * @param discipline
     * @param extFile
     * @param useCurrentLocaleOnly
     * @return
     */
    @SuppressWarnings("unchecked")
    protected Vector<DisciplineBasedContainer> load(final String  discipline, 
                                                    final File    extFile, 
                                                    final boolean useCurrentLocaleOnly)
    {
        Vector<DisciplineBasedContainer> containers = null;

        XStream xstream = new XStream();
        configXStream(xstream);
        
        try
        {
            String fullPath = (discipline != null ? (discipline + File.separator) : "") + fileName[schemaType];
            File   file     = extFile != null ? extFile : XMLHelper.getConfigDir(fullPath);
            if (file.exists())
            {
                if (discipline == null)
                {
                    inputFile = file;
                }

                InputStreamReader inpStrmReader = new InputStreamReader(new FileInputStream(file), "UTF8"); 
                containers = (Vector<DisciplineBasedContainer>)xstream.fromXML(inpStrmReader);
//                try {
//                    containers = oldSchoolStepByStep(discipline);
//                } catch (Exception x) {
//                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
//                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SchemaLocalizerXMLHelper.class, x);
//                    log.error(x);
//                }

            }
            
            if (useCurrentLocaleOnly && containers != null && containers.size() > 0)
            {
                String language = Locale.getDefault().getLanguage();
                String country = Locale.getDefault().getCountry();
                if ("en".equals(language)) {
                    country = "";
                }
                if (discipline == null) {
                    addMissingTranslations(language, country, containers);
                }
                stripToSingleLocale(language, country, containers);
            }

            if (discipline != null)
            {
                return containers;
            }

            if (containers != null)
            {
                for (SpLocaleContainer ct : containers)
                {
                    Hashtable<String, Boolean> hash = new Hashtable<String, Boolean>();
                    for (SpLocaleContainerItem item : new Vector<SpLocaleContainerItem>(ct.getItems()))
                    {
                        if (hash.get(item.getName()) == null)
                        {
                            hash.put(item.getName(), true);
                        } else
                        {
                            log.debug("Removing Duplicate["+item.getName()+"]");
                            ct.getItems().remove(item);
                        }
                    }
                }
            
                tableDisplayItems = new Vector<LocalizableJListItem>();
                for (SpLocaleContainer cont : containers)
                {
                    LocalizableJListItem item = new LocalizableJListItem(cont.getName(), cont.getId(), null);
                    tableDisplayItems.add(item);
                    //System.out.println("["+cont.getName()+"]");
                    tableDisplayItemsHash.put(cont.getName(), item);
                    
                    tableHash.put(cont.getName(), cont);
                }
                
                Collections.sort(tableDisplayItems);
                
                log.info("Syncing with Datamodel... (ignore errors)");
                changesBuffer.append("<Center><table border=\"1\">");
                
                String lang = SchemaI18NService.getCurrentLocale().getLanguage();
                String country = SchemaI18NService.getCurrentLocale().getCountry();
                if ("".equals(country)) {
                    country = null;
                }
                
                discoverLocalesFromData(containers);
                if (availLocales.size() == 1)
                {
                    lang = availLocales.get(0).getLanguage();
                    country = availLocales.get(0).getCountry();
                    if ("".equals(country)) {
                        country = null;
                    }
                } else
                {
                    Vector<DisplayLocale> list = new Vector<DisplayLocale>();
                    for (Locale locale : availLocales)
                    {
                        list.add(new DisplayLocale(locale));
                    }
                    Collections.sort(list);

                    boolean cont = true; //This messes up the stand-alone schemalocalizer app.
                    while (cont) {
                        ToggleButtonChooserDlg<DisplayLocale> dlg = new ToggleButtonChooserDlg<>((Dialog)UIRegistry.getMostRecentWindow(),
                                "CHOOSE_LOCALE", list, ToggleButtonChooserPanel.Type.RadioButton);
                        dlg.setUseScrollPane(true);
                        dlg.setVisible(true);

                        cont = dlg.isCancelled();
                        if (!cont) {
                            lang = dlg.getSelectedObject().getLocale().getLanguage();
                            country = dlg.getSelectedObject().getLocale().getCountry();
                            stripToSingleLocale(lang, country, containers);
                        } else {
                            return null;
                        }
                    }
                }
                
                log.info("Adding New Tables and fields...");
                for (DBTableInfo ti : tableMgr.getTables())
                {
                    DisciplineBasedContainer container = (DisciplineBasedContainer)tableHash.get(ti.getName());
                    if (container == null)
                    {
                        // OK, table has been Localized, so add it.
                        container = new DisciplineBasedContainer();
                        container.initialize();
                        container.setName(ti.getName());
                        SpLocaleItemStr nameStr = new SpLocaleItemStr();
                        nameStr.initialize();
                        nameStr.setText(UIHelper.makeNamePretty(ti.getShortClassName()));
                        nameStr.setLanguage(lang);
                        nameStr.setCountry(country);
                        container.addName(nameStr);
                        log.info("Adding Table ["+ti.getName()+"]");
                        changesMadeDuringStartup = true;
                        
                        changesBuffer.append("<tr><td align=\"center\">Added</td>");
                        changesBuffer.append("<td align=\"center\">");
                        changesBuffer.append(ti.getName());
                        changesBuffer.append("</td><td>&nbsp;</td></tr>");
                        
                        tableHash.put(container.getName(), container);
                        containers.add(container);
                        
                        LocalizableJListItem jItem = new LocalizableJListItem(container.getName(), container.getId(), null);
                        tableDisplayItems.add(jItem);
                        tableDisplayItemsHash.put(container.getName(), jItem);
                                           
                        for (DBFieldInfo fi : ti.getFields())
                        {
                            SpLocaleContainerItem item = new SpLocaleContainerItem();
                            item.initialize();
                            item.setName(fi.getName());
                            item.setWebLinkName(fi.getWebLinkName());
                            item.setIsRequired(fi.isRequired());
                            item.setIsHidden(fi.isHidden());
                            
                            nameStr = new SpLocaleItemStr();
                            nameStr.initialize();
                            //nameStr.setText(UIHelper.makeNamePretty(fi.getDataClass().getSimpleName()));
                            nameStr.setText(UIHelper.makeNamePretty(fi.getName()));
                            nameStr.setLanguage(lang);
                            nameStr.setCountry(country);
                            item.addName(nameStr);
                            log.info("  Adding Field ["+fi.getName()+"]");
                            changesBuffer.append("<tr><td align=\"center\">Added</td>");
                            changesBuffer.append("<td align=\"center\">&nbsp;</td><td align=\"center\">");
                            changesBuffer.append(fi.getName());
                            changesBuffer.append("</td></tr>");
                            
                            item.setIsRequired(fi.isRequired());
                            
                            container.addItem(item);
                        }
                        
                        for (DBRelationshipInfo ri : ti.getRelationships())
                        {
                            SpLocaleContainerItem item = new SpLocaleContainerItem();
                            item.initialize();
                            item.setName(ri.getName());
                            item.setIsRequired(false);
                            
                            log.info("  Adding Field ["+ri.getName()+"]");
                            changesBuffer.append("<tr><td align=\"center\">Added</td>");
                            changesBuffer.append("<td align=\"center\">&nbsp;</td><td align=\"center\">");
                            changesBuffer.append(ri.getName());
                            changesBuffer.append("</td></tr>");
                            container.addItem(item);
                        }
                        
                    } else
                    {
                        // Look for existing Field
                        for (DBFieldInfo fi : ti.getFields())
                        {
                            SpLocaleContainerItem item = (SpLocaleContainerItem)container.getItemByName(fi.getName());
                            if (item == null)
                            {
                                item = new SpLocaleContainerItem();
                                item.initialize();
                                item.setName(fi.getName());
                                item.setIsRequired(fi.isRequired());
                                item.setIsHidden(fi.isHidden());
                                
                                SpLocaleItemStr nameStr = new SpLocaleItemStr();
                                nameStr.initialize();
                                nameStr.setText(UIHelper.makeNamePretty(fi.getName()));
                                nameStr.setLanguage(lang);
                                nameStr.setCountry(country);
                                item.addName(nameStr);
                                container.addItem(item);
                                log.info("For Table["+ti.getName()+"] Adding Field ["+fi.getName()+"]");
                                changesMadeDuringStartup = true;
                                changesBuffer.append("<tr><td align=\"center\">Added</td>");
                                changesBuffer.append("<td align=\"center\">"+ti.getName()+"</td><td align=\"center\">");
                                changesBuffer.append(fi.getName());
                                changesBuffer.append("</td></tr>");
                                
                            } else if (doFixNames)
                            {
                                Class<?> cls = fi.getDataClass();
                                if (cls != null)
                                {
                                    String name = UIHelper.makeNamePretty(fi.getDataClass().getSimpleName());
                                    for (SpLocaleItemStr str : item.getNames())
                                    {
                                        if (name.equals(str.getText()))
                                        {
                                            str.setText(UIHelper.makeNamePretty(fi.getName()));
                                            
                                            changesMadeDuringStartup = true;
                                            changesBuffer.append("<tr><td align=\"center\">Fixed Name</td>");
                                            changesBuffer.append("<td align=\"center\">"+ti.getName()+"</td><td align=\"center\">");
                                            changesBuffer.append(fi.getName());
                                            changesBuffer.append("</td></tr>");
                                        }
                                    }
                                } else
                                {
                                    log.error("Data Class is null for field["+fi.getColumn()+"]");
                                }
                            } else
                            {
                                //item.setIsRequired(fi.isRequired());
                            }
                        }
                        
                        for (DBRelationshipInfo ri : ti.getRelationships())
                        {
                            SpLocaleContainerItem item = (SpLocaleContainerItem)container.getItemByName(ri.getName());
                            if (item == null)
                            {
                                item = new SpLocaleContainerItem();
                                item.initialize();
                                item.setName(ri.getName());
                                container.addItem(item);
                                SpLocaleItemStr nameStr = new SpLocaleItemStr();
                                nameStr.initialize();
                                nameStr.setText(UIHelper.makeNamePretty(ri.getName()));
                                nameStr.setLanguage(lang);
                                nameStr.setCountry(country);
                                item.addName(nameStr);
                                
                                log.info("For Table["+ti.getName()+"] Adding Rel ["+ri.getName()+"]");
                                changesMadeDuringStartup = true;
                                changesBuffer.append("<tr><td align=\"center\">Added</td>");
                                changesBuffer.append("<td align=\"center\">"+ti.getName()+"</td><td align=\"center\">");
                                changesBuffer.append(ri.getName());
                                changesBuffer.append("</td></tr>");
                            } else
                            {
                                if (item.getNames().size() == 0)
                                {
                                    SpLocaleItemStr nameStr = new SpLocaleItemStr();
                                    nameStr.initialize();
                                    nameStr.setText(UIHelper.makeNamePretty(ri.getName()));
                                    nameStr.setLanguage(lang);
                                    nameStr.setCountry(country);
                                    item.addName(nameStr);
                                    
                                    changesMadeDuringStartup = true;
                                    changesBuffer.append("<tr><td align=\"center\">Added</td>");
                                    changesBuffer.append("<td align=\"center\">"+ti.getName()+"</td><td align=\"center\">");
                                    changesBuffer.append(ri.getName());
                                    changesBuffer.append("</td></tr>");
                                }
                            }
                        } 
                    }
                }
                
                log.info("Removing Old Tables and fields...");
                for (SpLocaleContainer container : new Vector<DisciplineBasedContainer>(containers))
                {
                    DBTableInfo ti = tableMgr.getInfoByTableName(container.getName());
                    if (ti == null)
                    {
                        log.info("Removing Table ["+container.getName()+"] from Schema");
                        containers.remove(container);
                        tableHash.remove(container.getName());
                        changesMadeDuringStartup = true;
                        changesBuffer.append("<tr><td align=\"center\">Removed</td>");
                        changesBuffer.append("<td align=\"center\">");
                        changesBuffer.append(container.getName());
                        changesBuffer.append("</td><td>&nbsp;</td></tr>");
                        
                    } else
                    {
                        for (LocalizableItemIFace itemIF : new Vector<LocalizableItemIFace>(container.getContainerItems()))
                        {
                            SpLocaleContainerItem item     = (SpLocaleContainerItem)itemIF;
                            DBTableChildIFace     tblChild = ti.getItemByName(item.getName());
                            if (tblChild == null)
                            {
                                container.removeItem(item);
                                //log.info("For Table["+ti.getName()+"] Removing Rel ["+item.getName()+"]");
                                changesMadeDuringStartup = true;
                                changesBuffer.append("<tr><td align=\"center\" color=\"red\">Removed</td>");
                                changesBuffer.append("<td align=\"center\">"+ti.getName()+"</td><td align=\"center\">");
                                changesBuffer.append(item.getName());
                                changesBuffer.append("</td></tr>");
                            }
                        }
                    }
                }
                changesBuffer.append("</table>");
            } else
            {
                log.info("There were no containers for ["+file.getAbsolutePath()+"]");
            }
            
            // Force the hidden of special fields
            /*if (false)
            {
                String[] fieldsToHide = {"timestampCreated","timestampModified",
                                        "createdByAgent","modifiedByAgent","version",
                                        "collectionMemberId"};
                Hashtable<String, Boolean> hash = new Hashtable<String, Boolean>();
                for (String fName : fieldsToHide)
                {
                    hash.put(fName, Boolean.TRUE);
                }
                for (DBTableInfo ti : tableMgr.getTables())
                {
                    DisciplineBasedContainer container = (DisciplineBasedContainer)tableHash.get(ti.getName());
                    if (container != null)
                    {
                        for (SpLocaleContainerItem item : container.getItems())
                        {
                            if (hash.get(item.getName()) != null)
                            {
                                item.setIsHidden(Boolean.TRUE);
                            }
                        }
                    }
                }
            }*/
        } catch (IOException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SchemaLocalizerXMLHelper.class, ex);
            ex.printStackTrace();
            
        } catch (Exception ex)
        {
           UIRegistry.showError("There was a problem reading the XML in the file."); // I18N
        }
           
        return containers;
    }
    
    /**
     * @return returns a HTML document of what happened during the load.
     */
    public String getChangesBuffer()
    {
        return changesBuffer.toString();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#didModelChangeDuringLoad()
     */
    @Override
    public boolean didModelChangeDuringLoad()
    {
        return changesMadeDuringStartup;
    }

    /**
     * @param name
     * @return
     */
    public LocalizableContainerIFace getContainer(final String name)
    {
        return tableHash.get(name);
    }
    
    /**
     * 
     */
    protected void escapeForXML()
    {
        //Vector<LocalizableContainerIFace> containers = new Vector<LocalizableContainerIFace>();
        
        for (SpLocaleContainer ctr : tables)
        {
            ctr.setName(StringEscapeUtils.escapeXml(ctr.getName()));
            for (SpLocaleItemStr str : ctr.getNames())
            {
                str.setText(StringEscapeUtils.escapeXml(str.getText()));
            }
            
            for (SpLocaleItemStr str : ctr.getDescs())
            {
                str.setText(StringEscapeUtils.escapeXml(str.getText()));
            }

            for (SpLocaleContainerItem item : ctr.getItems())
            {
                item.setName(StringEscapeUtils.escapeXml(item.getName()));
                for (SpLocaleItemStr str : item.getNames())
                {
                    str.setText(StringEscapeUtils.escapeXml(str.getText()));
                }
                
                for (SpLocaleItemStr str : item.getDescs())
                {
                    str.setText(StringEscapeUtils.escapeXml(str.getText()));
                }

            }
        }
    }

    /*
    protected void dumpAsNew(Vector<LocalizableContainerIFace> contrs)
    {
        Vector<LocalizableContainerIFace> containers = new Vector<LocalizableContainerIFace>();
        
        for (LocalizableContainerIFace ctr : contrs)
        {
            LocalizableContainerIFace container = new LocalizableContainerIFace();
            container.initialize();
            container.setName(ctr.getName());
            container.setType(ctr.getType());
            containers.add(container);
            
            for (Name nm : ctr.getNames())
            {
                SpLocaleItemStr cDesc = new SpLocaleItemStr();
                cDesc.setText(nm.getText());
                cDesc.setCountry(nm.getCountry());
                cDesc.setLanguage(nm.getLang());
                cDesc.setVariant(nm.getVariant());
                container.getNames().add(cDesc);
            }
            for (Desc nm : ctr.getDescs())
            {
                SpLocaleItemStr cDesc = new SpLocaleItemStr();
                cDesc.setText(nm.getText());
                cDesc.setCountry(nm.getCountry());
                cDesc.setLanguage(nm.getLang());
                cDesc.setVariant(nm.getVariant());
                container.getDescs().add(cDesc);
            }

            for (LocalizableItemIFace lndi:  ctr.getItems())
            {
                LocalizableItemIFace item = new LocalizableItemIFace();
                item.initialize();
                item.setName(lndi.getName());
                item.setType(lndi.getType());
                container.getItems().add(item);
                
                for (Name nm : lndi.getNames())
                {
                    SpLocaleItemStr cDesc = new SpLocaleItemStr();
                    cDesc.setText(nm.getText());
                    cDesc.setCountry(nm.getCountry());
                    cDesc.setLanguage(nm.getLang());
                    cDesc.setVariant(nm.getVariant());
                    item.getNames().add(cDesc);
                }
                for (Desc nm : lndi.getDescs())
                {
                    SpLocaleItemStr cDesc = new SpLocaleItemStr();
                    cDesc.setText(nm.getText());
                    cDesc.setCountry(nm.getCountry());
                    cDesc.setLanguage(nm.getLang());
                    cDesc.setVariant(nm.getVariant());
                    item.getDescs().add(cDesc);
                }
            }
        }

        XStream xstream = new XStream();
        
        xstream.alias("container", LocalizableContainerIFace.class);
        xstream.alias("item",      LocalizableItemIFace.class);
        xstream.alias("str",       SpLocaleItemStr.class);
        
        xstream.useAttributeFor(SpLocaleBase.class, "name");
        xstream.useAttributeFor(SpLocaleBase.class, "type");
        
        xstream.useAttributeFor(SpLocaleItemStr.class, "country");
        xstream.useAttributeFor(SpLocaleItemStr.class, "language");
        xstream.useAttributeFor(SpLocaleItemStr.class, "variant");
        
        xstream.omitField(LocalizableContainerIFace.class,  "spLocaleContainerId");
        xstream.omitField(LocalizableItemIFace.class,  "spLocaleContainerItemId");
        xstream.omitField(SpLocaleItemStr.class,  "spLocaleItemStrId");
        
        try
        {
            FileUtils.writeStringToFile(new File("schema_localization.xml"), xstream.toXML(containers));
        } catch (IOException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SchemaLocalizerXMLHelper.class, ex);
            ex.printStackTrace();
        }
    }
    */
    
    /**
     * @param xstream
     */
    protected void configXStream(final XStream xstream)
    {
        xstream.alias("container", DisciplineBasedContainer.class);
        xstream.alias("item",      SpLocaleContainerItem.class);
        xstream.alias("names",      SpLocaleContainerItem.class);
        xstream.alias("desc",      SpLocaleContainerItem.class);
        xstream.alias("str",       SpLocaleItemStr.class);
        
        xstream.useAttributeFor(SpLocaleBase.class, "name");
        xstream.useAttributeFor(SpLocaleBase.class, "type");
        xstream.useAttributeFor(SpLocaleBase.class, "format");
        xstream.useAttributeFor(SpLocaleBase.class, "isUIFormatter");
        xstream.useAttributeFor(SpLocaleBase.class, "pickListName");
        xstream.useAttributeFor(SpLocaleBase.class, "isHidden");
        
        xstream.useAttributeFor(SpLocaleItemStr.class, "country");
        xstream.useAttributeFor(SpLocaleItemStr.class, "language");
        xstream.useAttributeFor(SpLocaleItemStr.class, "variant");
        
        xstream.useAttributeFor(SpLocaleContainerItem.class, "isRequired");
        
        xstream.omitField(DisciplineBasedContainer.class,      "disciplineHashItems");
        
        xstream.omitField(SpLocaleContainer.class,      "spLocaleContainerId");
        xstream.omitField(SpLocaleContainer.class,      "containerItems");
        
        xstream.omitField(SpLocaleContainerItem.class,  "spLocaleContainerItemId");
        xstream.omitField(SpLocaleContainerItem.class,  "container");

        xstream.omitField(SpLocaleItemStr.class,        "spLocaleItemStrId");
        
        xstream.omitField(DataModelObjBase.class,  "timestampCreated");
        xstream.omitField(DataModelObjBase.class,  "timestampModified");
        xstream.omitField(DataModelObjBase.class,  "lastEditedBy");
        
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#save()
     */
    @Override
    public boolean save()
    {
        return save(XMLHelper.getConfigDirPath(null));
    }
    
    
    /**
     * Saves the base file and all the disciplines to a directory.
     * @param basePath the base path to the directory (must end with '/')
     * @return whether everything was saved.
     */
    public boolean save(final String basePath)
    {
//        fixDescriptions(new File("/home/timo/datas/schemadescfix/schema_localization_with_descs.xml"),
//                new File("/home/timo/datas/schemadescfix/schema_localization_master.xml"),
//                new File("/home/timo/datas/fixedschema/"));
//        fixDescriptions(new File("/home/timo/datas/schemadescfix/schema_localization.xml"),
//                new File("/home/timo/sp6locale/Good/schema_localization_en.xml"),
//                new File("/home/timo/sp6locale/Good/schema_localization_en_desced2/"));
        boolean savedOk = save(basePath, null, tables);

        if (savedOk)
        {
            changesMadeDuringStartup = false;
            hasTableInfoChanged      = false;
            
            for (DisciplineType disciplineType : DisciplineType.getDisciplineList())
            {
                save(basePath, disciplineType.getName(), null);
            }
        }
        return savedOk;
    }
    
    /**
     * Saves the base set of containers OR 'filters out all the disciplineType-based
     * containers and saves them.
     * @param baseDir the base directory for the base set of containers (the baseline)
     * @param disciplineType the disciplineType to be saved, null if it is suppose to save the baseline. 
     * @param containers the list of containers
     * @return true on success
     */
    protected boolean save(final String baseDir, 
                           final String discipline, 
                           final Vector<DisciplineBasedContainer> containers)
    {
        Vector<DisciplineBasedContainer> localeContainers = containers;
        
        String fullPath = baseDir + (discipline != null ? (discipline + File.separator) : "") + fileName[schemaType];
        File file = new File(fullPath);
        
        // Filter oput just the containers for this disciplineType
        if (discipline != null)
        {
            localeContainers = filterForDisplineContainers(discipline);
        }
        
        return saveContainers(file, localeContainers);
    }
    
    
    /**
     * Saves a list of containers to a directory.
     * @param outFile the file it is to be saved to
     * @param containers the list of containers
     * @return on success
     */
    protected boolean saveContainers(final File outFile, 
                                     final Vector<DisciplineBasedContainer> containers)
    {
        try
        {
            if (containers == null)
            {
                log.error("Datamodel information is null - datamodel file will not be written!!");
                return false;
            }
            
            //escapeForXML();
            if (containers.size() > 0)
            {
       
                //addMissingTranslations(Locale.getDefault().getLanguage(), containers);
                
                log.info("Writing descriptions to file: " + outFile.getAbsolutePath());
                
                XStream xstream = new XStream();
                
                configXStream(xstream);
                
                OutputStreamWriter outputStrmWriter = new OutputStreamWriter(new FileOutputStream(outFile), "UTF8"); 
                xstream.toXML(containers, outputStrmWriter);
                //FileUtils.writeStringToFile(outFile, xstream.toXML(containers));
                
            } /*else
            {
                log.info("There were no items to write to ["+outFile.getAbsolutePath()+"]");
            }*/
            
            return true;
            
        } catch (IOException ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SchemaLocalizerXMLHelper.class, ex);
            log.error("error writing writeTree", ex);
        }
        return false;

    }
    
    /**
     * @param discipline
     * @return
     */
    protected Vector<DisciplineBasedContainer> filterForDisplineContainers(final String discipline)
    {
        Vector<DisciplineBasedContainer> disciplineContainers = new Vector<DisciplineBasedContainer>();
        for (DisciplineBasedContainer container : tables)
        {
            if (container.hasDiscipline(discipline))
            {
                DisciplineBasedContainer dbc = (DisciplineBasedContainer)container.clone(); // Shallow Clone
                dbc.getItems().addAll(container.getDisciplineItems(discipline));
                disciplineContainers.add(dbc);
            }
        }
        return disciplineContainers;
    }

    /**
     * @param pw
     * @param parent
     * @param lndi
     * @param lang
     * @param country
     */
    protected void printLocales(final PrintWriter pw,
                                final LocalizableItemIFace parent, 
                                final LocalizableItemIFace lndi, 
                                final String lang, 
                                final String country)
    {
        lndi.fillNames(namesList);
        lndi.fillNames(descsList);
        
        for (LocalizableStrIFace nm : namesList)
        {
            if (nm.getLanguage().equals(lang) && nm.getCountry().equals(country))
            {
                if (parent != null)
                {
                    pw.write(parent.getName() + "_");
                }
                pw.write(lndi.getName());
                pw.write("=");
                pw.write(nm.getText());
                pw.write("\n");
            }
        }
        for (LocalizableStrIFace d : descsList)
        {
            if (parent != null)
            {
                pw.write(parent.getName() + "_");
            }
            pw.write(lndi.getName());
            pw.write("_desc");
            pw.write("=");
            pw.write(d.getText());
            pw.write("\n");
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#createResourceFiles()
     */
    @Override
    public boolean createResourceFiles()
    {
        Hashtable<String, Boolean> localeHash = new Hashtable<String, Boolean>();
        for (LocalizableContainerIFace table : tables)
        {
            checkForLocales(table, localeHash);
            for (LocalizableItemIFace f : table.getContainerItems())
            {
                checkForLocales(f, localeHash);
            }
        }
        
        for (String key : localeHash.keySet())
        {
            String[] toks = StringUtils.split(key, '_');
            
            String lang    = toks[0];
            String country = toks.length > 1 && StringUtils.isNotEmpty(toks[1]) ? toks[1] : "";
            
            //System.out.println("["+key+"] "+lang+" "+country);
            
            File resFile = new File("db_resources" +
                    (StringUtils.isNotEmpty(lang) ? ("_"+lang)  : "") +
                    (StringUtils.isNotEmpty(country) ? ("_"+country)  : "") + 
                    ".properties");
            
            try
            {
                PrintWriter pw = new PrintWriter(resFile);
                for (LocalizableContainerIFace table : tables)
                {
                    printLocales(pw, null, table, lang, country);
                    for (LocalizableItemIFace f : table.getContainerItems())
                    {
                        printLocales(pw, table, f, lang, country);
                    }
                }
                pw.close();
                
                return true;
                
            } catch (IOException ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SchemaLocalizerXMLHelper.class, ex);
                ex.printStackTrace();
            }
        }
        return false;
    }



    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#getContainer(edu.ku.brc.specify.tools.schemalocale.LocalizableJListItem)
     */
    @Override
    public LocalizableContainerIFace getContainer(final LocalizableJListItem item, final LocalizableIOIFaceListener l)
    {
        if (l != null)
        {
            l.containterRetrieved(tableHash.get(item.getName()));
            return tableHash.get(item.getName());
        }
        return null;
    }



    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#getContainerDisplayItems()
     */
    @Override
    public Vector<LocalizableJListItem> getContainerDisplayItems()
    {
        return tableDisplayItems;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#getItem(edu.ku.brc.specify.tools.schemalocale.LocalizableContainerIFace, edu.ku.brc.specify.tools.schemalocale.LocalizableJListItem)
     */
    @Override
    public LocalizableItemIFace getItem(final LocalizableContainerIFace container,
                                        final LocalizableJListItem      item)
    {
        for (LocalizableItemIFace cItem : container.getContainerItems())
        {
            if (cItem.getName().equals(item.getName()))
            {
                return cItem;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#getDisplayItems(edu.ku.brc.specify.tools.schemalocale.LocalizableJListItem)
     */
    @Override
    public Vector<LocalizableJListItem> getDisplayItems(final LocalizableJListItem container)
    {
        Vector<LocalizableJListItem> items = itemJListItemsHash.get(container);
        if (items == null)
        {
            LocalizableContainerIFace cont = tableHash.get(container.getName());
            if (cont != null)
            {
                items = new Vector<LocalizableJListItem>();
                for (LocalizableItemIFace item : cont.getContainerItems())
                {
                    SpLocaleContainerItem cItem = (SpLocaleContainerItem)item;
                    items.add(new LocalizableJListItem(cItem.getName(), cItem.getId(), null));
                    //System.out.println(cItem.getName());
                }
                itemJListItemsHash.put(container, items);
                Collections.sort(items);
                
            } else
            {
                log.error("Couldn't find container ["+container.getName()+"]");
            }
        }
        return items;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#containerChanged(edu.ku.brc.specify.tools.schemalocale.LocalizableContainerIFace)
     */
    @Override
    public void containerChanged(LocalizableContainerIFace container)
    {
        hasTableInfoChanged = true;
    }

    /**
     * @param lang
     * @param country
     * @param variant
     * @return
     */
    public static String makeLocaleKey(final String lang, final String country, final String variant)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(lang);
        sb.append(StringUtils.isNotEmpty(country) ? ("_" + country) : "");
        sb.append(StringUtils.isNotEmpty(variant) ? ("_" + variant) : "");
        //System.out.println(sb.toString());
        return sb.toString();
    }
    
    /**
     * @param locale
     * @return
     */
    protected static String makeLocaleKey(final Locale locale)
    {
        return makeLocaleKey(locale.getLanguage(), locale.getCountry(), locale.getVariant());
    }
    
    /**
     * @param lndi
     * @param localeHash
     */
    /**
     * @param lndi
     * @param localeHash
     */
    public static void checkForLocales(final LocalizableItemIFace lndi, final Hashtable<String, Boolean> localeHash)
    {
        Vector<LocalizableStrIFace> namesList = new Vector<LocalizableStrIFace>();
        Vector<LocalizableStrIFace> descsList = new Vector<LocalizableStrIFace>();
        
        lndi.fillDescs(descsList);
        lndi.fillNames(namesList);
        
        for (LocalizableStrIFace nm : namesList)
        {
            localeHash.put(makeLocaleKey(nm.getLanguage(), nm.getCountry(), nm.getVariant()), true);
        }
        for (LocalizableStrIFace d : descsList)
        {
            localeHash.put(makeLocaleKey(d.getLanguage(), d.getCountry(), d.getVariant()), true);
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#isLocaleInUse(java.util.Locale)
     */
    @Override
    public boolean isLocaleInUse(final Locale locale)
    {
        Hashtable<String, Boolean> localeHash = new Hashtable<String, Boolean>();
        for (SpLocaleContainer container : tables)
        {
            checkForLocales(container, localeHash);
            for (LocalizableItemIFace f : container.getContainerItems())
            {
                checkForLocales(f, localeHash);
            }
        }
        return localeHash.get(makeLocaleKey(locale)) != null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#getLocalesInUse()
     */
    @Override
    public Vector<Locale> getLocalesInUse()
    {
        Hashtable<String, Boolean> localeHash = new Hashtable<String, Boolean>();
        for (SpLocaleContainer container : tables)
        {
            checkForLocales(container, localeHash);
            for (LocalizableItemIFace f : container.getContainerItems())
            {
                checkForLocales(f, localeHash);
            }
        }
        Vector<Locale> inUseLocales = new Vector<Locale>(localeHash.keySet().size()+10);
        for (String key : localeHash.keySet())
        {
            String[] toks = StringUtils.split(key, "_");
            inUseLocales.add(new Locale(toks[0], "", ""));
        }
        return inUseLocales;
    }
    
    
    /**
     * @param item
     * @param srcLocale
     * @param dstLocale
     */
    public void copyLocale(final LocalizableItemIFace item, final Locale srcLocale, final Locale dstLocale)
    {
        item.fillDescs(descsList);
        item.fillNames(namesList);
        
        LocalizableStrIFace srcName = null;
        for (LocalizableStrIFace nm : namesList)
        {
            if (nm.isLocale(srcLocale))
            {
                srcName = nm;
                break;
            }
        }
        
        if (srcName != null)
        {
            LocalizableStrIFace name = localizableStrFactory.create(srcName.getText(), dstLocale);
            item.addName(name);
        }

        LocalizableStrIFace srcDesc = null;
        for (LocalizableStrIFace d : descsList)
        {
            if (d.isLocale(srcLocale))
            {
                srcDesc = d;
                break;
            }
        }
        
        if (srcDesc != null)
        {
            LocalizableStrIFace desc = localizableStrFactory.create(srcDesc.getText(), dstLocale);
            item.addDesc(desc);
        }                 
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#copyLocale(java.util.Locale, java.util.Locale, java.beans.PropertyChangeListener)
     */
    @Override
    public void copyLocale(final LocalizableIOIFaceListener lclIOListener, final Locale srcLocale, final Locale dstLocale, final PropertyChangeListener pcl)
    {
        double cnt = 0.0;
        Vector<LocalizableJListItem> items = getContainerDisplayItems();
        
        double inc = 100.0 / items.size();
        for (LocalizableJListItem listItem : items)
        {
            LocalizableContainerIFace table = getContainer(listItem, lclIOListener);
            
            copyLocale(table, srcLocale, dstLocale);
            
            for (LocalizableItemIFace field : table.getContainerItems())
            {
                copyLocale(field, srcLocale, dstLocale);
            }
            if (pcl != null)
            {
                pcl.propertyChange(new PropertyChangeEvent(listItem, "count", -1, (int)cnt));
                //System.out.println(cnt);
            }
            cnt += inc;
        }
        
        if (pcl != null)
        {
            pcl.propertyChange(new PropertyChangeEvent(this, "count", -1, 100));
        }
    }

    /**
     *
     * @param name
     * @param descs
     * @return
     */
    private DisciplineBasedContainer getContainerByName(String name, Vector<DisciplineBasedContainer> containers) {
        for (DisciplineBasedContainer c : containers) {
            if (c.getName().equals(name)) {
                return c;
            }
        }
        return null;
    }

    private SpLocaleContainerItem getContainerItemByName(String name, Set<SpLocaleContainerItem> spLocaleContainerItems) {
        for (SpLocaleContainerItem c : spLocaleContainerItems) {
            if (c.getName().equals(name)) {
                return c;
            }
        }
        return null;
    }

    private SpLocaleItemStr getStrForLocale(String localeStr, Set<SpLocaleItemStr> strs) {
        for (SpLocaleItemStr d : strs) {
            if (getLocaleStr(d).equals(localeStr)) {
                return d;
            }
        }
        return null;
    }

    /**
     * ignores variant
     * @param item
     * @return
     */
    private String getLocaleStr(SpLocaleItemStr item) {
        String result = item.getLanguage();
        String ctry = item.getCountry();
        if (ctry != null && !"".equals(ctry)) {
            result += "_" + ctry;
        }
        return result;
    }

    private String correctDesc(SpLocaleItemStr rightDesc, SpLocaleItemStr wrongDesc, SpLocaleItemStr wrongNameItem) {
        String wrong = wrongDesc.getText();
        String right = rightDesc.getText();
        String wrongName = wrongNameItem.getText();
        if ((wrong == null || "".equals(wrong) || wrong.equals(wrongName)) && right != null && !"".equals(right)) {
            if (!wrong.equals(right)) {
                wrongDesc.setText(rightDesc.getText());
                return rightDesc.getText();
            }
        }
        return null;
    }


    private boolean matchNameInOtherLanguage(SpLocaleItemStr descItemStr, Set<SpLocaleItemStr> itemStrs) {
        for (SpLocaleItemStr i : itemStrs) {
            String iLang = i.getLanguage();
            String dLang = descItemStr.getLanguage();
            if (descItemStr.getText().replaceAll(" ", "").equalsIgnoreCase(i.getText()) && !dLang.equals(iLang)) {
                if (!((iLang.equals("ru") && dLang.equals("uk")) || (iLang.equals("uk") && dLang.equals("ru")))) {
                    return true;
                }
            }
        }
        return false;
    }

    private void fixDescsThatAreNames(String itemName, Set<SpLocaleItemStr> descs, Set<SpLocaleItemStr> names,
                                      List<String> fixes, List<String> problems) {
        boolean doFix = false;
        for (SpLocaleItemStr d : descs) {
            if (matchNameInOtherLanguage(d, names)) {
                doFix = true;
                break;
            }
        }
        if (doFix) {
            for (SpLocaleItemStr d : descs) {
                SpLocaleItemStr newText = getStrForLocale(getLocaleStr(d), names);
                if (newText == null){
                    String msg = "Can't replace text for " + itemName + " " + getLocaleStr(d) + " - " + d.getText();
                    System.out.println(msg);
                    problems.add(msg);
                } else if (!d.getText().replaceAll(" ", "").equalsIgnoreCase(newText.getText())) {
                    fixes.add("[" + d.getText() + " <= " + newText.getText() + "]");
                    d.setText(newText.getText());
                }
            }
        }
    }

    private void addMissingNames(DisciplineBasedContainer item, List<String> newNames) {
        List<String> locales = new ArrayList<>();
        locales.add("en");
        //locales.add("pt");
        //locales.add("pt_BR");
        //locales.add("ru_RU");
        //locales.add("uk_UA");
        for (String locale : locales) {
            if (getStrForLocale(locale, item.getNames()) == null) {
                SpLocaleItemStr newItem = new SpLocaleItemStr();
                newItem.initialize();
                newItem.setText(UIHelper.makeNamePretty(item.getName()));
                newItem.setContainerName(item);
                String[] ls = locale.split("_");
                newItem.setLanguage(ls[0]);
                if (ls.length > 1) {
                    newItem.setCountry(ls[1]);
                }
                item.getNames().add(newItem);
                newNames.add(item.getName() + " for " + locale);
            }
            for (SpLocaleContainerItem cItem : item.getItems()) {
                if (getStrForLocale(locale, cItem.getNames()) == null) {
                    SpLocaleItemStr newItem = new SpLocaleItemStr();
                    newItem.initialize();
                    newItem.setText(UIHelper.makeNamePretty(cItem.getName()));
                    newItem.setItemName(cItem);
                    String[] ls = locale.split("_");
                    newItem.setLanguage(ls[0]);
                    if (ls.length > 1) {
                        newItem.setCountry(ls[1]);
                    }
                    cItem.getNames().add(newItem);
                    newNames.add(item.getName()  + "." + cItem.getName() + " for " + locale);
                }

            }
        }
    }

    private void addAllMissingNames(Vector<DisciplineBasedContainer> all) {
        List<String> newNames = new ArrayList<>();
        for (DisciplineBasedContainer c : all) {
            addMissingNames(c, newNames);
        }
        try {
            FileUtils.writeLines(new File("/home/timo/datas/schemadescfix/newNames.txt"), newNames);
        } catch (IOException x) {
            System.out.println("error writing new names file.");
        }
    }

    private void findDescsWithoutNames(DisciplineBasedContainer item, List<String> descsWoNames) {
           for (SpLocaleItemStr desc : item.getDescs()) {
            if (getStrForLocale(getLocaleStr(desc), item.getNames()) == null) {
                descsWoNames.add(item.getName() + " for " + getLocaleStr(desc));
            }
            for (SpLocaleContainerItem cItem : item.getItems()) {
                for (SpLocaleItemStr cdesc : cItem.getDescs()) {
                    if (getStrForLocale(getLocaleStr(cdesc), cItem.getNames()) == null) {
                        descsWoNames.add(item.getName() + "." + cItem.getName() + " for " + getLocaleStr(cdesc));
                    }
                }
            }
        }
    }
    private void findAllDescsWithoutNames(Vector<DisciplineBasedContainer> all) {
        List<String> newNames = new ArrayList<>();
        for (DisciplineBasedContainer c : all) {
            findDescsWithoutNames(c, newNames);
        }
        try {
            FileUtils.writeLines(new File("/home/timo/datas/schemadescfix/descsWoNames.txt"), newNames);
        } catch (IOException x) {
            System.out.println("error writing descs w/o names file.");
        }
    }

    private void cleanUpParentPointers(Vector<DisciplineBasedContainer> jump) {
        for (DisciplineBasedContainer j : jump) {
            cleanUpParentPointers(j);
        }
    }

    private void cleanUpParentPointers(DisciplineBasedContainer jump) {
        cleanUpParentPointers(jump, null);
        for (SpLocaleContainerItem i : jump.getItems()) {
            cleanUpParentPointers(null, i);
        }
    }

    private void cleanUpParentPointers(DisciplineBasedContainer c, SpLocaleContainerItem i) {
        if (c != null) {
            for (SpLocaleItemStr s : c.getNames()) {
                s.setContainerName(c);
                s.setContainerDesc(null);
                c.getDescs().remove(s);
                s.setItemDesc(null);
                s.setItemName(null);
            }
            for (SpLocaleItemStr s : c.getDescs()) {
                s.setContainerDesc(c);
                s.setContainerName(null);
                c.getNames().remove(s);
                s.setItemDesc(null);
                s.setItemName(null);
            }
        } else if (i != null) {
            for (SpLocaleItemStr s : i.getNames()) {
                s.setContainerName(null);
                s.setContainerDesc(null);
                i.getDescs().remove(s);
                s.setItemDesc(null);
                s.setItemName(i);
            }
            for (SpLocaleItemStr s : i.getDescs()) {
                s.setContainerDesc(null);
                s.setContainerName(null);
                i.getNames().remove(s);
                s.setItemDesc(i);
                s.setItemName(null);
            }

        }
    }

    private void fixDescriptions(File schemaWithDescs, File schemaWithoutDescs, File exportDirectory) {
        Vector<DisciplineBasedContainer> withDescs = null;
        Vector<DisciplineBasedContainer> withoutDescs = null;

        try {
            withDescs = oldSchoolStepByStep(schemaWithDescs);
            withoutDescs = oldSchoolStepByStep(schemaWithoutDescs);
        } catch (Exception x) {
            x.printStackTrace();
            return;
        }
        List<String> fixes = new ArrayList<>();
        List<String> problems = new ArrayList<>();
        for (DisciplineBasedContainer desced : withDescs) {
            DisciplineBasedContainer unDesced = getContainerByName(desced.getName(), withoutDescs);
            if (unDesced != null) {
                for (SpLocaleItemStr desc : desced.getDescs()) {
                    String localeStr = getLocaleStr(desc);
                    SpLocaleItemStr woDesc = getStrForLocale(localeStr, unDesced.getDescs());
                    SpLocaleItemStr woName = getStrForLocale(localeStr, unDesced.getNames());
                    if (woDesc != null) {
                        String oldDesc = woDesc.getText();
                        String correction = correctDesc(desc, woDesc, woName);
                        if (correction != null) {
                            System.out.println(oldDesc + " <= " + desc.getText());
                            fixes.add(oldDesc + " <= " + desc.getText());
                        }
                    }
                }
                fixDescsThatAreNames(unDesced.getName(), unDesced.getDescs(), unDesced.getNames(), fixes, problems);
                for (SpLocaleContainerItem descedItem : desced.getItems()) {
                    SpLocaleContainerItem unDescedItem = getContainerItemByName(descedItem.getName(), unDesced.getItems());
                    if (unDescedItem != null) {
                        for (SpLocaleItemStr desc : descedItem.getDescs()) {
                            String localeStr = getLocaleStr(desc);
                            SpLocaleItemStr woDesc = getStrForLocale(localeStr, unDescedItem.getDescs());
                            SpLocaleItemStr woName = getStrForLocale(localeStr, unDescedItem.getNames());
                            if (woDesc != null) {
                                String oldDesc = woDesc.getText();
                                String correction = correctDesc(desc, woDesc, woName);
                                if (correction != null) {
                                    System.out.println(oldDesc + " <= " + desc.getText());
                                    fixes.add(oldDesc + " <= " + desc.getText());
                                }
                            }
                        }
                        fixDescsThatAreNames(unDesced.getName() + "." + unDescedItem.getName(),
                                unDescedItem.getDescs(), unDescedItem.getNames(), fixes, problems);
                    }
                }
            }
        }
        try {
            FileUtils.writeLines(new File("/home/timo/datas/schemadescfix/problems.txt"), problems);
            FileUtils.writeLines(new File("/home/timo/datas/schemadescfix/fixes.txt"), fixes);
        } catch (IOException x) {
            System.out.println("error writing log files.");
        }
        //findAllDescsWithoutNames(withoutDescs);
        saveContainers(exportDirectory, withoutDescs);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#export(java.io.File)
     */
    @Override
    public boolean exportToDirectory(final File exportDirectory)
    {
        return save(exportDirectory.getAbsolutePath() + File.separator);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#exportSingleLanguageToDirectory(java.io.File, java.util.Locale)
     */
    @Override
    public boolean exportSingleLanguageToDirectory(final File exportDirectory, final Locale locale)
    {
        boolean isOK = false;
        if (inputFile != null)
        {
            Locale cachedLocale = Locale.getDefault();
            try
            {
                Locale.setDefault(locale);
                Vector<DisciplineBasedContainer> tmpTables = load(null, inputFile, true);
                isOK = saveContainers(exportDirectory, tmpTables);
                
            } finally
            {
                Locale.setDefault(cachedLocale);
            }
        }
        return isOK;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#getPickLists()
     */
    @Override
    public List<PickList> getPickLists(final String disciplineName)
    {
        List<PickList>     pickLists     = new Vector<PickList>();
        List<BldrPickList> bdlrPickLists;
        
        if (disciplineName == null)
        {
            bdlrPickLists = DataBuilder.getBldrPickLists("common");
            
            Hashtable<String, Boolean> nameHash = new Hashtable<String, Boolean>();
            for (DisciplineType dt : DisciplineType.getDisciplineList())
            {
                List<BldrPickList> list = DataBuilder.getBldrPickLists(dt.getName());
                if (list != null)
                {
                    for (BldrPickList bpl : list)
                    {
                       if (nameHash.get(bpl.getName()) == null)
                       {
                           nameHash.put(bpl.getName(), true);
                           bdlrPickLists.add(bpl);
                       }
                    }
                }
            }
            
        } else
        {
            bdlrPickLists = DataBuilder.getBldrPickLists(disciplineName != null ? disciplineName : "common");
        }
            
        
        for (BldrPickList pl : bdlrPickLists)
        {
            PickList pickList = createPickList(pl.getName(), pl.getType(), pl.getTableName(),
                                               pl.getFieldName(), pl.getFormatter(), pl.getReadOnly(), 
                                               pl.getSizeLimit(), pl.getIsSystem(), pl.getSortType(), null);
            for (BldrPickListItem item : pl.getItems())
            {
                pickList.addItem(item.getTitle(), item.getValue());
            }
            pickLists.add(pickList);
        }
        return pickLists;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#hasUpdatablePickLists()
     */
    @Override
    public boolean hasUpdatablePickLists()
    {
        return false;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#shouldIncludeAppTables()
     */
    @Override
    public boolean shouldIncludeAppTables()
    {
        return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tools.schemalocale.LocalizableIOIFace#hasChanged()
     */
    @Override
    public boolean hasChanged()
    {
        return changesMadeDuringStartup || hasTableInfoChanged;
    }

    /**
     * @param itemStrs
     * @param locale
     * @return
     */
    protected SpLocaleItemStr getNameForLocale(final Set<SpLocaleItemStr> itemStrs, final Locale locale)
    {
        for (SpLocaleItemStr itemStr : itemStrs)
        {
            if (itemStr.isLocale(locale))
            {
                return itemStr;
            }
        }
        return null;
    }
    
    /**
     * 
     */
    public void setTitlesIntoSchema()
    {
        Locale locale = Locale.getDefault();
        
        for (DBTableInfo ti : tableMgr.getTables())
        {
            DisciplineBasedContainer container = (DisciplineBasedContainer)tableHash.get(ti.getName());
            SpLocaleItemStr itemStr = getNameForLocale(container.getNames(), locale);
            if (itemStr != null)
            {
                ti.setTitle(itemStr.getText());
            }
            
            Set<SpLocaleContainerItem> fieldContainers = container.getItems();
            for (DBFieldInfo fi : ti.getFields())
            {
                for (SpLocaleContainerItem item : fieldContainers)
                {
                    if (item.getName().equals(fi.getName()))
                    {
                        itemStr = getNameForLocale(item.getNames(), locale);
                        if (itemStr != null)
                        {
                            fi.setTitle(itemStr.getText());
                        }
                        itemStr = getNameForLocale(item.getDescs(), locale);
                        if (itemStr != null)
                        {
                            fi.setDescription(itemStr.getText());
                        }
                    }
                }
            }
        }
    }
}
