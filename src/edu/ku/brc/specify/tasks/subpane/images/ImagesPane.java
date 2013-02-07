/* Copyright (C) 2011, University of Kansas Center for Research
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
package edu.ku.brc.specify.tasks.subpane.images;

import static edu.ku.brc.ui.UIRegistry.clearSimpleGlassPaneMsg;
import static edu.ku.brc.ui.UIRegistry.getGlassPane;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.getStatusBar;
import static edu.ku.brc.ui.UIRegistry.getTopWindow;
import static edu.ku.brc.ui.UIRegistry.writeSimpleGlassPaneMsg;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.RecordSetFactory;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.af.ui.SearchBox;
import edu.ku.brc.af.ui.db.PickListDBAdapterFactory;
import edu.ku.brc.af.ui.db.ViewBasedDisplayDialog;
import edu.ku.brc.af.ui.db.ViewBasedDisplayPanel;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.ResultSetController;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.dbsupport.RecordSetItemIFace;
import edu.ku.brc.specify.datamodel.Attachment;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.tasks.DataEntryTask;
import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.ui.BubbleGlassPane;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.DocumentAdaptor;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.SearchBoxComponent;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.dnd.GhostGlassPane;
import edu.ku.brc.util.Pair;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Oct 12, 2011
 *
 */
public class ImagesPane extends BaseSubPane
{
    protected static final Logger  log = Logger.getLogger(ImagesPane.class);
    protected static final String  LAST_SEARCH   = "imglastsearch"; 
    protected static final int     MENU_CLEAR    = 0;
    protected static final int     MENU_DATE     = 1;
    protected static final int     MENU_FILENAME = 2;
    protected static final int     MENU_COMBO    = 3;
    
    protected static final int GLASS_FONT_SIZE = 14;
    protected static final int MAX_IMAGE_REQUEST_COUNT = 3;
    
    protected static ImageIcon infoIcon16 = IconManager.getIcon("InfoIcon", IconManager.STD_ICON_SIZE.Std16);

    protected static GhostGlassPane oldGlassPane     = null;    

    protected ViewBasedDisplayPanel coVBP;
    protected BubbleGlassPane       bubblePane = null;
    protected boolean               showingGlassPane = false;
    
    protected boolean               isAllImages;
    protected RecordSetIFace        recordSet;
    
    protected Vector<ImageDataItem> rowsVector = new Vector<ImageDataItem>();
    protected ImageInfoPanel        infoPanel;
    protected MetaDataPanel         metaDataPanel;
    
    protected GalleryGridPanel      gridPanel;
    protected ResultSetController   rsController;

    protected JButton               infoBtn;
    protected JButton               metaDataBtn;
    protected JButton               helpBtn;
    protected boolean               isInfoShown = false;
    
    // Search
    protected SearchBoxComponent            searchBoxComp;
    protected SearchBox                     searchBox;
    protected JTextField                    searchText;
    protected JButton                       searchBtn;
    protected Color                         textBGColor      = null;
    protected Color                         badSearchColor   = new Color(255,235,235);

    
    // Loading in batches
    protected List<RecordSetItemIFace> items     = null;
    
    protected HashMap<String, String> dataMap = new HashMap<String, String>();
    
    protected CollectionDataFetcher   dataFetcher = new CollectionDataFetcher();
    
    protected int   [] colTblIds;
    protected String[] fieldNames;
    protected String[] labels;
    protected UIFieldFormatterIFace[] formatters;

    /**
     * @param name
     * @param task
     */
    public ImagesPane(String name, Taskable task, final boolean isAllImages)
    {
        super(name, task);
        this.isAllImages = isAllImages;
        this.recordSet   = null;
        initImagePane();
    }
    
    /**
     * @param name
     * @param task
     */
    public ImagesPane(String name, Taskable task, 
                      final RecordSetIFace recordSet)
    {
        super(name, task);
        this.isAllImages = false;
        this.recordSet   = recordSet;
        initImagePane();
    }
     
    /**
     * 
     */
    private void initImagePane()
    {
        Collection collection = AppContextMgr.getInstance().getClassObject(Collection.class);
        boolean isEmbeded  = collection.getIsEmbeddedCollectingEvent();
        
        colTblIds  = new int[]    {41,                1,               10,                   10,             2,             2,          2,          3,          4,     };
        fieldNames = new String[] {"OrigFilename","CatalogNumber", "StartDate",     "StationFieldNumber","LocalityName","Latitude1","Longitude1","GeoName", "TaxName", };
        labels     = new String [fieldNames.length];
        formatters = new UIFieldFormatterIFace[fieldNames.length];
        if (isEmbeded)
        {
            colTblIds[3]  = 1;
            fieldNames[3] = "FieldNumber";
        }
        for (int i=0;i<fieldNames.length-2;i++)
        {
            String fldName = fieldNames[i].equals("GeoName") || fieldNames[i].equals("TaxName") ? "FullName" : fieldNames[i];
            
            labels[i]      = DBTableIdMgr.getInstance().getTitleForField(colTblIds[i], fldName);
            DBTableInfo ti = DBTableIdMgr.getInstance().getInfoById(colTblIds[i]);
            formatters[i]  = DBTableIdMgr.getFieldFormatterFor(ti.getClassObj(), fldName);
        }
        int inx = fieldNames.length-2;
        labels[inx] = DBTableIdMgr.getInstance().getTitleForId(colTblIds[inx]);
        inx++;
        labels[inx] = DBTableIdMgr.getInstance().getTitleForId(colTblIds[inx]);
        
        rsController = new ResultSetController(null, false, false, false, "Image", 0, true);
        gridPanel    = new GalleryGridPanel(rsController);
        addGridListener();
        
        createUI();
    }
    
    /**
     * @return panel containing search ui
     */
    protected JPanel createSearchPanel()
    {
        ActionListener doQuery = new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                //doQuery();
            }
        };
        
        searchBoxComp  = new SearchBoxComponent(new SearchBoxMenuCreator(), doQuery, false,
                                                PickListDBAdapterFactory.getInstance().create("ExpressSearch", true));
        searchBoxComp.createUI();
        searchBox      = searchBoxComp.getSearchBox();
        searchText     = searchBoxComp.getSearchText();
        searchBtn      = searchBoxComp.getSearchBtn();
        textBGColor    = searchBoxComp.getTextBGColor();
        badSearchColor = searchBoxComp.getBadSearchColor();
        
        searchBtn.setToolTipText(getResourceString("ExpressSearchTT"));
        HelpMgr.setHelpID(searchBtn, "Express_Search");
        HelpMgr.registerComponent(searchText, "Express_Search");
        
        AppPreferences localPrefs = AppPreferences.getLocalPrefs();
        searchText.setText(localPrefs.get(getLastSearchKey(), ""));
        textBGColor = searchText.getBackground();

        searchText.addMouseListener(new MouseAdapter() 
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                showContextMenu(e);
            }

            @Override
            public void mouseReleased(MouseEvent e)
            {
                showContextMenu(e);
            }
        });
        return searchBoxComp;
    }

    /**
     * Creates the UI.
     */
    protected void createUI()
    {
        infoPanel = new ImageInfoPanel(dataFetcher, this);
        infoPanel.createUI();
        
        metaDataPanel = new MetaDataPanel();
        metaDataPanel.createUI();
        
        infoBtn   = UIHelper.createIconBtn("InfoIcon", IconManager.STD_ICON_SIZE, null, null);
        infoBtn.setEnabled(true);
        
        metaDataBtn = UIHelper.createIconBtn("MetaData", IconManager.STD_ICON_SIZE, null, null);
        metaDataBtn.setEnabled(true);
        
        helpBtn = UIHelper.createHelpIconButton("ImageBrowser");
        
        //filterBtn = UIHelper.createIconBtn("Filter20", IconManager.STD_ICON_SIZE, null, null);
        //filterBtn.setEnabled(true);
        
        rsController.getPanel().setOpaque(true);
        CommandBarPanel  cbp = new CommandBarPanel(rsController);
        //cbp.setLeftComps(createSearchPanel());                      // temporarily disabled (work in progress)
        cbp.setRightComps(metaDataBtn, infoBtn, helpBtn);
        cbp.createUI();
        
        JPanel botPanel = new JPanel(new BorderLayout());
        botPanel.add(cbp, BorderLayout.NORTH);
        botPanel.add(metaDataPanel, BorderLayout.CENTER);
        
        setLayout(new BorderLayout());
        if (isAllImages)
        {
            createColObjSearch();
            
        } else if (recordSet != null)
        {
            searchForRecordSetImages();
        }
        
        add(infoPanel, BorderLayout.EAST);
        add(botPanel, BorderLayout.SOUTH);
        
        infoBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                showInfoPanel(gridPanel.getSelectedCellIndex());
            }
        });
        
        infoPanel.setVisible(false);
        
        metaDataBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                showMetaDataPanel(gridPanel.getSelectedCellIndex());
            }
        });
        
        metaDataPanel.setVisible(false);
    }
    
    /* (non-Javadoc)
     * @see java.awt.Component#setBounds(int, int, int, int)
     */
    @Override
    public void setBounds(int x, int y, int width, int height)
    {
        super.setBounds(x, y, width, height);
        
        if (gridPanel != null)
        {
            gridPanel.setLayoutSize(new Dimension(width, height));
        }
    }
    
    /**
     * @return a discipline based pref name.
     */
    protected String getLastSearchKey()
    {
        Discipline discp          = AppContextMgr.getInstance().getClassObject(Discipline.class);
        String     disciplineName = discp != null ? ("_" + discp.getType()) : "";
        return LAST_SEARCH + disciplineName;
    }
    
    /**
     * Shows the Reset menu.
     * @param e the mouse event
     */
    protected void showContextMenu(final MouseEvent e)
    {
        if (e.isPopupTrigger())
        {
            JPopupMenu popup = new JPopupMenu();
            JMenuItem menuItem = new JMenuItem(UIRegistry.getResourceString("ES_TEXT_RESET"));
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ex)
                {
                    searchText.setEnabled(true);
                    searchText.setBackground(textBGColor);
                    searchText.setText("");
                }
            });
            popup.add(menuItem);
            popup.show(e.getComponent(), e.getX(), e.getY());

        }
    }    

    /**
     * @param index
     */
    private void fillInfoPanel(final int index)
    {
        ImageDataItem idi = index == -1 ? null : (ImageDataItem)rowsVector.get(index);
        infoPanel.setImgDataItem(idi);
    }
    
    /**
     * @param index
     */
    private void fillMetaDataPanel(final int index)
    {
        ImageDataItem idi = index == -1 ? null : (ImageDataItem)rowsVector.get(index);
        metaDataPanel.setAttachmentId(idi != null ? idi.getAttachmentId() : null);
    }
    
    /**
     * 
     */
    protected void showInfoPanel(final int index)
    {
        //log.debug("infoBtn showInfoPanel - isInfoShown: "+isInfoShown);
        //gridPanel.clearSelection();
        
        // Current State of the Info Panel
        if (!infoPanel.isExpanded())
        {
            fillInfoPanel(index);
        }
        isInfoShown = !isInfoShown;
        
        // The new State of the Info Panel
        //infoPanel.setVisible(isInfoShown);
        if (isInfoShown)
        {
            infoPanel.setVisible(true);
            /*int infoPanelSize = ImageInfoPanel.IMG_SIZE + 10;
            Dimension s = new Dimension(0, getPreferredSize().height);
            infoPanel.setSize(new Dimension(0, getPreferredSize().height));
            //infoPanel.setPreferredSize(s);
            infoPanel.setSpecialPrefSize(s);*/
            infoPanel.expand();
        } else
        {
            infoPanel.contract();
        }
    }
    
    /**
     * @param index
     */
    protected void showMetaDataPanel(final int index)
    {
        //gridPanel.clearSelection();
        if (!metaDataPanel.isExpanded())
        {
            fillMetaDataPanel(index);
            metaDataPanel.setVisible(true);
            metaDataPanel.expand();
        } else
        {
            metaDataPanel.contract();
        }
    }
    
    /**
     * 
     */
    private void searchForColObjImagesForTable()
    {
        String sql = "SELECT a.AttachmentID, a.Title, a.AttachmentLocation, a.MimeType FROM collectionobject co INNER JOIN collectionobjectattachment coa ON co.CollectionObjectID = coa.CollectionObjectID " +
                           "INNER JOIN attachment a ON coa.AttachmentID = a.AttachmentID %s";

        String whereStr = isAllImages ? "ORDER BY Title" : "WHERE CatalogNumber = '%s' ORDER BY Ordinal";
        sql = String.format(sql, whereStr);
        
        UIFieldFormatterIFace fmt = DBTableIdMgr.getFieldFormatterFor(CollectionObject.class, "catalogNumber");
        if (fmt != null || isAllImages)
        {
            String queryStr = sql;
            if (!isAllImages)
            {
                coVBP.getMultiView().getDataFromUI();
                //System.out.println("["+dataMap.get("CatalogNumber")+"]");
                String catNum   = (String)fmt.formatFromUI(dataMap.get("CatalogNumber"));
                queryStr        = String.format(sql, catNum);
                //System.out.println(queryStr);
            }
            Statement stmt  = null;
            try
            {
                rowsVector.clear();
                stmt = DBConnection.getInstance().getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(queryStr);
                while (rs.next())
                {
                    ImageDataItem imgDataItem = new ImageDataItem(rs.getInt(1), CollectionObject.getClassTableId(), 
                                                                  rs.getString(2), rs.getString(3), rs.getString(4));
                    rowsVector.add(imgDataItem);
                }
                rs.close();
                
                Collections.sort(rowsVector, new Comparator<ImageDataItem>() {
                    @Override
                    public int compare(ImageDataItem o1, ImageDataItem o2)
                    {
                        return o1.getShortName().compareTo(o2.getShortName());
                    }
                });
                
            } catch (Exception e)
            {
                e.printStackTrace();
            } finally
            {
                try
                {
                    if (stmt != null) stmt.close();
                } catch (Exception e) {}
            }
        }
    }
    
    /**
     * 
     */
    private void searchForAllImages()
    {
        String    sql  = "SELECT AttachmentID, TableID, Title, AttachmentLocation, MimeType FROM attachment WHERE MimeType LIKE 'image/%' ORDER BY Title";
        Statement stmt = null;
        try
        {
            rowsVector.clear();
            stmt = DBConnection.getInstance().getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next())
            {
                ImageDataItem imgDataItem = new ImageDataItem(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getString(4), rs.getString(5));
                rowsVector.add(imgDataItem);
            }
            rs.close();
            
            Collections.sort(rowsVector, new Comparator<ImageDataItem>() {
                @Override
                public int compare(ImageDataItem o1, ImageDataItem o2)
                {
                    return o1.getShortName().compareTo(o2.getShortName());
                }
            });
            
        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            try
            {
                if (stmt != null) stmt.close();
            } catch (Exception e) {}
        }
    }
    
    /**
     * 
     */
    private void searchForRecordSetImages()
    {
        final String MEGS = "MEGS";
        final String STATUSBAR_NAME = "ImageSearchStatusBar";
        
        rowsVector.clear();
        items = recordSet.getOrderedItems();
        
        final int numItems   = items.size();

        SwingWorker<Integer, Integer> backupWorker = new SwingWorker<Integer, Integer>()
        {
            @Override
            protected Integer doInBackground() throws Exception
            {
                final DBTableInfo ti = DBTableIdMgr.getInstance().getInfoById(recordSet.getDbTableId());
                
                String sql = "SELECT a.AttachmentID, a.TableID, a.Title, a.AttachmentLocation, a.MimeType FROM attachment a " +
                		     "INNER JOIN %sattachment coa ON a.AttachmentID = coa.AttachmentID "+
                             "WHERE coa.%s IN (%s) AND a.MimeType LIKE 'image/%s' ORDER BY a.Title";

                int batchSize  = 500;
                int attchIndex = 0;
                int batches    = (numItems / batchSize) + (numItems % batchSize == 0 ? 0 : 1); 
                if (numItems < batchSize)
                {
                    batchSize = numItems;
                }

                Statement stmt = null;
                try
                {
                    stmt = DBConnection.getInstance().getConnection().createStatement();

                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < batches; i++)
                    {
                        firePropertyChange(MEGS, 0, i+1);
                        sb.setLength(0);
                        for (int j = 0; j < batchSize && attchIndex < numItems; j++)
                        {
                            RecordSetItemIFace rsi = items.get(attchIndex++);
                            if (j > 0)
                                sb.append(',');
                            sb.append(rsi.getRecordId().toString());
                        }

                        String fullSQL = String.format(sql, ti.getName(), ti.getIdColumnName(), sb.toString(), "%");
                        ResultSet rs = stmt.executeQuery(fullSQL);
                        while (rs.next())
                        {
                            ImageDataItem imgDataItem = new ImageDataItem(rs.getInt(1),
                                    rs.getInt(2), rs.getString(3), rs.getString(4), rs.getString(5));
                            rowsVector.add(imgDataItem);
                        }
                        rs.close();
                    }

                } catch (Exception e)
                {
                    e.printStackTrace();
                } finally
                {
                    try
                    {
                        if (stmt != null)
                            stmt.close();
                    } catch (Exception e)
                    {
                    }
                }

                Collections.sort(rowsVector, new Comparator<ImageDataItem>()
                {
                    @Override
                    public int compare(ImageDataItem o1, ImageDataItem o2)
                    {
                        return o1.getShortName().compareTo(o2.getShortName());
                    }
                });

                return null;
            }

            @Override
            protected void done()
            {
                super.done();
                
                gridPanel.setItemList(rowsVector);
                JScrollPane sb = new JScrollPane(gridPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                add(sb, BorderLayout.CENTER);

                getStatusBar().setProgressDone(STATUSBAR_NAME);

                clearSimpleGlassPaneMsg();

            }
        };

        final JStatusBar statusBar = getStatusBar();
        statusBar.setIndeterminate(STATUSBAR_NAME, true);
        
        writeSimpleGlassPaneMsg(getResourceString("Searching for Images"), 24);
        
        backupWorker.addPropertyChangeListener(
                new PropertyChangeListener() {
                    public  void propertyChange(final PropertyChangeEvent evt) {
                        if (MEGS.equals(evt.getPropertyName())) 
                        {
                            Integer value = (Integer)evt.getNewValue();
                            int     val   = (int)(((double)value / (double)numItems) * 100.0);
                            statusBar.setText(Integer.toString(val));//getLocalizedMessage("MySQLBackupService.BACKUP_MEGS", val));
                        }
                    }
                });
        backupWorker.execute();    
    }
    
    /**
     * @param index
     */
    private void showFullImage(final int index)
    {
        //log.debug("infoBtn showFullImage - index: "+index);

        if (index > -1 && index < rowsVector.size())
        {
            ImageDataItem item = rowsVector.get(index);
            if (item != null)
            {
                FullImagePane pane = new FullImagePane(item.getTitle(), getTask(), item);
                SubPaneMgr.getInstance().addPane(pane);
            }
        }
    }
    
    /**
     * @param item
     * @return
     */
    protected HashMap<String, Object> getImageData(final ImageDataItem item)
    {
        HashMap<String, Object> map = item.getDataMap();
        if (map == null)
        {
            map = dataFetcher.getData(item.getAttachmentId(), item.getTableId());
            item.setDataMap(map);
        }
        return map;
    }
    
    /**
     * @param item
     * @return
     */
    protected Vector<Pair<String, Object>> getImageDataValueList(final ImageDataItem item)
    {
        Vector<Pair<String, Object>> list = new Vector<Pair<String, Object>>();
        
        HashMap<String, Object> map = getImageData(item);
        if (map != null)
        {
            int i = 0;
            for (String key : fieldNames)
            {
                Object val = map.get(key);
                if (val != null)
                {
                    if (key.equals("OrigFilename"))
                    {
                        val = FilenameUtils.getName(val.toString());
                    }
                    if (formatters[i] != null)
                    {
                        val = formatters[i].formatToUI(val);
                    }
                    Pair<String, Object> p = new Pair<String, Object>(labels[i]+":", val != null ? val.toString() : "");
                    list.add(p);
                }
                i++;
            }
        }
        return list;
    }
    
    /**
     * @param index
     * @param isInfoBtn
     */
    private void showBubble(final int index, final int whichBtn)
    {
        //log.debug("showBubble - index: "+index+"  whichBtn: "+whichBtn);

        final ImageDataItem item = index > -1 && index < rowsVector.size() ? rowsVector.get(index) : null;
        if (item == null) return;
        
        bubblePane = new BubbleGlassPane();
        bubblePane.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                if (showingGlassPane)
                {
                    ((JFrame)getTopWindow()).setGlassPane(oldGlassPane);
                    bubblePane.setVisible(false);
                    showingGlassPane = false;
                    
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            if (bubblePane.getBtnClicked() != -1)
                            {
                                showItemsInForm(item);
                                /*int index = rowsVector.indexOf(item);
                                if (index > -1)
                                {
                                    showFullImage(index);
                                }*/
                            }
                        }
                    });
                }
            }
        });
        
        Vector<Pair<String, Object>> values = null;
        if (whichBtn == ImageCellDisplay.INFO_BTN)
        {
            values = getImageDataValueList(item);
            if (values != null)
            {
                for (Pair<String, Object> p : values)
                {
                    bubblePane.addLine(p.first, p.second.toString());
                }
            }
        }
        
        if (values != null)
        {
            String btnTitle = String.format("Show %s", DBTableIdMgr.getInstance().getTitleForId(item.getTableId()));
            bubblePane.addBtn(btnTitle, null);
        
            oldGlassPane = getGlassPane();
            if (oldGlassPane != null)
            {
                oldGlassPane.finishedWithDragAndDrop();
            }
            
            if (bubblePane != null && getTopWindow() != null)
            {
                ((JFrame)getTopWindow()).setGlassPane(bubblePane);
                bubblePane.setVisible(true);
                showingGlassPane = true;
                
            } else
            {
                oldGlassPane     = null;
                showingGlassPane = false;
            }
        }
    }
    
    
    /**
     * @param item
     */
    private void showItemsInForm(final ImageDataItem item)
    {
        //log.debug("showItemsInForm - item: "+item != null ? item.getTitle() : "(null)");
        
        if (item != null && item.getDataMap() != null)
        {
            Integer recId = (Integer)item.getDataMap().get("Id");
            if (recId != null)
            {
                RecordSetIFace rs = RecordSetFactory.getInstance().createRecordSet("", item.getTableId(), RecordSet.GLOBAL);
                rs.addItem(recId);
                CommandDispatcher.dispatch(new CommandAction(DataEntryTask.DATA_ENTRY, DataEntryTask.EDIT_DATA, rs));
            }
        }
    }
    
    /*private void searchForColObjImages()
    {
        final int numCols = 4;
        
        final String sql = "SELECT a.AttachmentID, a.Title, a.AttachmentLocation, a.MimeType FROM collectionobject co INNER JOIN collectionobjectattachment coa ON co.CollectionObjectID = coa.CollectionObjectID " +
                           "INNER JOIN attachment a ON coa.AttachmentID = a.AttachmentID WHERE CatalogNumber = '%s' ORDER BY Ordinal";

        UIFieldFormatterIFace fmt = DBTableIdMgr.getFieldFormatterFor(CollectionObject.class, "catalogNumber");
        if (fmt != null)
        {
            coVBP.getMultiView().getDataFromUI();
            String catNum   = (String)fmt.formatFromUI(dataMap.get("CatalogNumber"));
            String queryStr = String.format(sql, catNum);
            Statement stmt  = null;
            try
            {
                stmt = DBConnection.getInstance().getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(queryStr);
                while (rs.next())
                {
                    for (int i=0;i<50;i++)
                    {
                        JLabel lbl = new JLabel();
                        
                        lbl.setVerticalTextPosition(SwingConstants.BOTTOM);
                        lbl.setHorizontalTextPosition(SwingConstants.CENTER);
                        lbl.setHorizontalAlignment(SwingConstants.CENTER);
                        ImageDataItem img = new ImageDataItem(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), null);
                        lbl.setText(img.getTitle());
                        
                        ImageIcon ii = img.getImageIcon();
                        if (ii != null)
                        {
                            lbl.setIcon(ii);
                            lbl.setPreferredSize(new Dimension(100, 100));
                        }
                        gridPanel.add(lbl);
                    }
                }
                
                rs.close();
                
                gridPanel.revalidate();
                gridPanel.invalidate();
                gridPanel.doLayout();
                gridPanel.repaint();
                
            } catch (Exception e)
            {
                e.printStackTrace();
            } finally
            {
                try
                {
                    if (stmt != null) stmt.close();
                } catch (Exception e) {}
            }
        }
    }*/
    
    /**
     * 
     */
    private void addGridListener()
    {
        gridPanel.addListSelectionListener(new GalleryGridListener()
        {
            @Override
            public void itemSelected(ImageCellDisplay icd, int index, boolean isSelected, int clickCount)
            {
                log.debug("itemSelected - index: "+index+"  isSelected:" +isSelected+"  clickCount:" +clickCount);

                if (isSelected)
                {
                    if (clickCount == 1)
                    {
                        fillInfoPanel(index);
                        fillMetaDataPanel(index);
                        
                    } else if (clickCount == 2)
                    {
                        showFullImage(index);
                    }
                }
            }
            
            @Override
            public void infoSelected(ImageCellDisplay icd, int index, boolean isSelected, final int whichBtn)
            {
                //log.debug("itemSelected - index: "+index+"  isSelected:" +isSelected+"  whichBtn:" +whichBtn);
                if (index > -1)
                {
                    showBubble(index, whichBtn);
                } else
                {
                    infoPanel.setImgDataItem(null);
                }
            }
        });
    }

    /**
     * @return
     */
    private void createColObjSearch()
    {
//        if (mainComp != null)
//        {
//            remove(mainComp);
//        }
        
        if (isAllImages)
        {
            searchForAllImages();
            gridPanel.setItemList(rowsVector);
            JScrollPane sb = new JScrollPane(gridPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            add(sb, BorderLayout.CENTER);
            return;
        }
        
        CellConstraints cc = new CellConstraints();
        
        String rowDef  = isAllImages ? "f:p:g" : "p,4px,f:p:g";
        PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g,2px,p", rowDef));
        
        int y = 1;
        coVBP = new ViewBasedDisplayPanel(null, "COImageSearch", CollectionObject.class.getName(), true, MultiView.NO_SCROLLBARS | MultiView.IS_SINGLE_OBJ | MultiView.IS_EDITTING);
        pb.add(coVBP, cc.xyw(1,y,3));
        y += 2;
        
        pb.add(gridPanel, cc.xyw(1,y,3));

        coVBP.setData(dataMap);
        
        FormViewObj fvo       = coVBP.getMultiView().getCurrentViewAsFormViewObj();
        JButton     searchBtn = fvo.getCompById("2");
        JTextField  textField = fvo.getCompById("1");
        y += 2;
        
        //fvo.setAlwaysGetDataFromUI(true);
    
        if (searchBtn != null)
        {
            final JButton    searchBtnFinal = searchBtn;
            final JTextField textFieldFinal = textField;

            searchBtn.setEnabled(false);
            searchBtn.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    searchForColObjImagesForTable();
                    gridPanel.setItemList(rowsVector);
                    //invalidate();
                    //revalidate();
                }
            });
            
            textField.getDocument().addDocumentListener(new DocumentAdaptor()
            {
                @Override
                protected void changed(DocumentEvent e)
                {
                    searchBtnFinal.setEnabled(textFieldFinal.getText().length() > 0);
                }
            });
            textField.addKeyListener(new KeyAdapter()
            {
                @Override
                public void keyPressed(KeyEvent e)
                {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER &&
                            textFieldFinal.getText().length() > 0)
                    {
                        searchForColObjImagesForTable();
                    }
                }
                
            });
        }
        JScrollPane sb = new JScrollPane(pb.getPanel(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(sb, BorderLayout.CENTER);
    }
    
    /**
     * 
     */
    private void doClear()
    {
        
    }
    
    /**
     * 
     */
    private void showDateDialog()
    {
        //PanelBuilder pb = new PanelBuilder(new FormLayout("p,2px,p", "p,4px,p"));
        //CustomDialog dlg = new CustomDialog((Frame)UIRegistry.getTopWindow(), "Search", true, pb.getPanel());
        final ViewBasedDisplayDialog dlg = new ViewBasedDisplayDialog((Frame)UIRegistry.getTopWindow(),
                "Search",
                "ImageDateSearch",
                null,
                getResourceString(getResourceString("CHG_PWD_TITLE")),
                "OK",
                null,
                null,
                true,
                MultiView.HIDE_SAVE_BTN | MultiView.DONT_ADD_ALL_ALTVIEWS | MultiView.USE_ONLY_CREATION_MODE |
                MultiView.IS_EDITTING);
        //dlg.setHelpContext("CHANGE_PWD");
        dlg.setWhichBtns(CustomDialog.OK_BTN | CustomDialog.CANCEL_BTN);
        Hashtable<String, String> valuesHash = new Hashtable<String, String>();
        dlg.setData(valuesHash);
        UIHelper.centerAndShow(dlg);
        
        if (dlg.isCancelled()) return;
        
        String startDate  = valuesHash.get("StartDate");
        String endDate = valuesHash.get("EndDate");
        //System.out.println(String.format("[%s][%s]", startDate, endDate));
        
        StringBuilder sb         = new StringBuilder();
        StringBuilder displayStr = new StringBuilder();
        
        Integer year  = null;
        Integer month = null;
        //Integer day   = null;
        if (startDate.length() == 10)
        {
            //day = Integer.parseInt(startDate.substring(8,10));
            sb.append(String.format("FileCreatedDate = '%s'", startDate));
            displayStr.append(startDate);
        } else
        {
            if (startDate.length() > 3) // Year
            {
                year = Integer.parseInt(startDate.substring(0,4));
                sb.append(String.format("YEAR(FileCreatedDate) = %d", year));
                displayStr.append(year.toString());
            }
            if (startDate.length() > 6)
            {
                month = Integer.parseInt(startDate.substring(5,7));
                sb.append(String.format(" AND MONTH(FileCreatedDate) = %d", month));
                displayStr.append(String.format("-%02d", month));
            }
        }
        
        searchText.setText(displayStr.toString());
        
        AppContextMgr acm = AppContextMgr.getInstance();
        int[] ids = {acm.getClassObject(Collection.class).getId(), Attachment.COLLECTION_SCOPE,
                     acm.getClassObject(Discipline.class).getId(), Attachment.DISCIPLINE_SCOPE,
                     acm.getClassObject(Division.class).getId(), Attachment.DIVISION_SCOPE,};
        
        
        StringBuilder whereSB = new StringBuilder();
        for (int i=0;i<ids.length;i+=2)
        {
            whereSB.append( String.format("(ScopeID = %d AND ScopeType = %d) OR ", ids[i], ids[i+1]));
        }
        whereSB.append( String.format("(ScopeID IS NULL AND ScopeType = %d)", Attachment.GLOBAL_SCOPE));     

        String sql = String.format("SELECT AttachmentID, TableID, Title, AttachmentLocation, MimeType " +
        		                   "FROM attachment WHERE (%s) AND %s AND MimeType LIKE 'image/%c' ORDER BY Title", whereSB.toString(), sb.toString(), '%');
        log.debug(sql);
        
        Statement stmt = null;
        try
        {
            rowsVector.clear();
            stmt = DBConnection.getInstance().getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next())
            {
                ImageDataItem imgDataItem = new ImageDataItem(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getString(4), rs.getString(5));
                rowsVector.add(imgDataItem);
            }
            rs.close();
            
            Collections.sort(rowsVector, new Comparator<ImageDataItem>() {
                @Override
                public int compare(ImageDataItem o1, ImageDataItem o2)
                {
                    return o1.getShortName().compareTo(o2.getShortName());
                }
            });
            
            gridPanel.setItemList(rowsVector);
            gridPanel.reloadGallery();
            
        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            try
            {
                if (stmt != null) stmt.close();
            } catch (Exception e) {}
        }
    }

    /**
     * 
     */
//    private void showComboDialog()
//    {
//        
//        PanelBuilder pb = new PanelBuilder(new FormLayout("", ""));
//        
//        CustomDialog dlg = new CustomDialog((Frame)UIRegistry.getTopWindow(), "Search", true, pb.getPanel());
//        
//    }

    //-------------------------------------------------------------------------
    //-- CustomQueryListener Interface
    //-------------------------------------------------------------------------
    public class SearchBoxMenuCreator implements SearchBox.MenuCreator
    {
        protected List<JComponent>    menus       = null;
        protected ActionListener      action;
        
        /**
         * Constructor.
         */
        public SearchBoxMenuCreator()
        {
            action = new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    int inx = -1;
                    for (int i=0;i<menus.size();i++)
                    {
                        if (e.getSource() == menus.get(i))
                        {
                            inx = i;
                            break;
                        }
                    }
                    switch (inx)
                    {
                        case MENU_CLEAR:
                            doClear();
                            break;
                            
                        case MENU_DATE:
                            showDateDialog();
                            break;
                            
                        case MENU_FILENAME:
                            break;
                            
                        case MENU_COMBO:
                            break;
                    }
                }
            };
        }
        
        /* (non-Javadoc)
         * @see edu.ku.brc.ui.SearchBox.MenuCreator#createPopupMenus()
         */
        public List<JComponent> createPopupMenus()
        {
            if (menus == null)
            {
                menus = new Vector<JComponent>();
                String[] menuNames = {"Clear",    "Date",     "File Name", "Combination"};
                String[] icon      = {"Eraser16", "Calendar", "Search",    "Search", };
                int i = 0;
                for (String nm : menuNames)
                {
                    JMenuItem menuItem = new JMenuItem(nm, IconManager.getIcon(icon[i], IconManager.IconSize.Std16));
                    menuItem.addActionListener(action);
                    menus.add(menuItem);
                    i++;
                }
                
                if (recordSet == null)
                {
                    menus.add(new JSeparator());
                    for (Class<?> cls : CollectionDataFetcher.getAttachmentClasses())
                    {
                        DBTableInfo ti = DBTableIdMgr.getInstance().getByShortClassName(cls.getSimpleName());
                        JMenuItem menuItem = new JMenuItem(ti.getTitle(), IconManager.getIcon(ti.getName(), IconManager.IconSize.Std16));
                        menuItem.addActionListener(action);
                        menus.add(menuItem);
                        if (ti.getTableId() == Taxon.getClassTableId())
                        {
                            menus.add(new JSeparator());
                        }
                    }
                }
                //configMenuItem = new JMenuItem(getResourceString("ESConfig"), IconManager.getIcon("SystemSetup", IconManager.IconSize.Std16));
                //configMenuItem.addActionListener(action);
                //menus.add(configMenuItem);
            }
            
            return menus;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.ui.SearchBox.MenuCreator#reset()
         */
        public void reset()
        {
            this.menus = null;
        }
    }

}
