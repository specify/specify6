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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;

import org.apache.commons.io.FilenameUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;
import edu.ku.brc.af.ui.db.ViewBasedDisplayPanel;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.ResultSetController;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.ui.BubbleGlassPane;
import edu.ku.brc.ui.DocumentAdaptor;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.dnd.GhostGlassPane;

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
    protected static final int GLASS_FONT_SIZE = 14;
    protected static final int MAX_IMAGE_REQUEST_COUNT = 3;
    
    protected static ImageIcon infoIcon16 = IconManager.getIcon("InfoIcon", IconManager.STD_ICON_SIZE.Std16);

    protected static GhostGlassPane oldGlassPane     = null;    

    protected ViewBasedDisplayPanel coVBP;
    protected BubbleGlassPane       bubblePane = null;
    protected boolean               showingGlassPane = false;
    
    protected boolean               isAllImages;
    
    protected Vector<ImageDataItem> rowsVector = new Vector<ImageDataItem>();
    protected ImageInfoPanel        infoPanel;
    
    protected GalleryGridPanel      gridPanel;
    protected ResultSetController   rs;

    protected JButton               infoBtn;
    protected boolean               isInfoShown = false;
    
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
        
        rs        = new ResultSetController(null, false, false, false, "Image", 0, true);
        gridPanel = new GalleryGridPanel(rs);
        addGridListener();
        
        createUI();
    }
    
    /**
     * Creates the UI.
     */
    protected void createUI()
    {
        infoPanel = new ImageInfoPanel(dataFetcher);
        
        infoBtn   = UIHelper.createIconBtn("InfoIcon", IconManager.STD_ICON_SIZE, null, null);
        infoBtn.setEnabled(true);
        
        rs.getPanel().setOpaque(true);
        CommandBarPanel  cbp = new CommandBarPanel(rs, infoBtn);
        
        setLayout(new BorderLayout());
        add(createColObjSearch(), BorderLayout.CENTER);
        add(infoPanel, BorderLayout.EAST);
        add(cbp, BorderLayout.SOUTH);
        
        infoBtn.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                showInfoPanel(gridPanel.getSelectedIndex());
            }
        });
        
        infoPanel.setVisible(false);
        
        createColObjSearch();
    }
    
    /**
     * 
     */
    private void fillInfoPanel(final int index)
    {
        infoPanel.setImgDataItem(index == -1 ? null : (ImageDataItem)rowsVector.get(index));
    }
    
    /**
     * 
     */
    protected void showInfoPanel(final int index)
    {
        if (isInfoShown)
        {
            infoPanel.setImgDataItem(null);
        } else
        {
            fillInfoPanel(index);
        }
        isInfoShown = !isInfoShown;
        
        infoPanel.setVisible(isInfoShown);
        if (isInfoShown)
        {
            int infoPanelSize = ImageInfoPanel.IMG_SIZE + 10;
            Dimension s = new Dimension(infoPanelSize, infoPanelSize);
            infoPanel.setSize(s);
            infoPanel.setPreferredSize(s);
        }
        
        /*SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                invalidate();
                validate();
                doLayout();
            }
        });*/
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
        
        ChangeListener cl = new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                ImagesPane.this.repaint();
            }
        };
        
        UIFieldFormatterIFace fmt = DBTableIdMgr.getFieldFormatterFor(CollectionObject.class, "catalogNumber");
        if (fmt != null || isAllImages)
        {
            String queryStr = sql;
            if (!isAllImages)
            {
                coVBP.getMultiView().getDataFromUI();
                String catNum   = (String)fmt.formatFromUI(dataMap.get("CatalogNumber"));
                queryStr        = String.format(sql, catNum);
            }
            Statement stmt  = null;
            try
            {
                rowsVector.clear();
                stmt = DBConnection.getInstance().getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(queryStr);
                while (rs.next())
                {
                    ImageDataItem imgDataItem = new ImageDataItem(CollectionObject.getClassTableId(), 
                            rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), cl);
                    rowsVector.add(imgDataItem);
                }
                rs.close();
                
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
        String sql = "SELECT AttachmentID, TableID, Title, AttachmentLocation, MimeType FROM attachment WHERE MimeType LIKE 'image/%' ORDER BY Title";
        ChangeListener cl = new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                ImagesPane.this.repaint();
            }
        };
        
        Statement stmt  = null;
        try
        {
            rowsVector.clear();
            stmt = DBConnection.getInstance().getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next())
            {
                ImageDataItem imgDataItem = new ImageDataItem(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getString(4), rs.getString(5), cl);
                rowsVector.add(imgDataItem);
            }
            rs.close();
            
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
     * @param index
     */
    private void showFullImage(final int index)
    {
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
     * 
     */
    private void showBubble(final int index)
    {
        ImageDataItem item = index > -1 && index < rowsVector.size() ? rowsVector.get(index) : null;
        if (item == null) return;
        
        bubblePane = new BubbleGlassPane();
        bubblePane.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                if (showingGlassPane)
                {
                    ((JFrame)UIRegistry.getTopWindow()).setGlassPane(oldGlassPane);
                    bubblePane.setVisible(false);
                    showingGlassPane = false;
                }
            }
        });
        
        HashMap<String, Object> map = dataFetcher.getData(item.getAttachmentId(), item.getTableId());
        if (map != null)
        {
            int i = 0;
            for (String key : fieldNames)
            {
                Object val = map.get(key);
                if (val != null)
                {
                    if (formatters[i] != null)
                    {
                        val = formatters[i].formatToUI(val);
                    }
                    bubblePane.addLine(labels[i]+":", val != null ? val.toString() : "");
                }
                i++;
            }
            
            String btnTitle = String.format("Show %s", DBTableIdMgr.getInstance().getTitleForId(item.getTableId()));
            bubblePane.addBtn(btnTitle, null);
        
            oldGlassPane = UIRegistry.getGlassPane();
            if (oldGlassPane != null)
            {
                oldGlassPane.finishedWithDragAndDrop();
            }
            
            if (bubblePane != null && UIRegistry.getTopWindow() != null)
            {
                ((JFrame)UIRegistry.getTopWindow()).setGlassPane(bubblePane);
                bubblePane.setVisible(true);
                showingGlassPane = true;
                
            } else
            {
                oldGlassPane     = null;
                showingGlassPane = false;
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
                if (isSelected)
                {
                    if (clickCount == 1)
                    {
                        fillInfoPanel(index);
                    } else if (clickCount == 2)
                    {
                        showFullImage(index);
                    }
                }
            }
            
            @Override
            public void infoSelected(ImageCellDisplay icd, int index, boolean isSelected)
            {
                showBubble(index);
            }
        });
    }

    /**
     * @return
     */
    private JComponent createColObjSearch()
    {
       if (isAllImages)
        {
           searchForAllImages();
            gridPanel.setItemList(rowsVector);
            return gridPanel;
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
        return pb.getPanel();
    }

    /**
     * @return
     */
    /*private JPanel createTaxonSearch()
    {
        CellConstraints cc = new CellConstraints();
        
        Hashtable<String, Object>        dataMap = new Hashtable<String, Object>();
        
        Viewable form = null;
        PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g,2px,p", "p,4px,p"));
        ViewIFace formView = AppContextMgr.getInstance().getView("Search", "AgentNameSearch");
        if (formView != null)
        {
            form = ViewFactory.createFormView(null, formView, null, dataMap, MultiView.NO_OPTIONS, null);

        } else
        {
            //log.error("Couldn't load form with name ["+viewSetName+"] Id ["+viewName+"]");
        }
        
        if (form != null)
        {
            pb.add(form.getUIComponent(), cc.xyw(1,1,3));
            
            JButton searchBtn = UIHelper.createI18NButton("Search");
            pb.add(searchBtn, cc.xy(3,3));
            
        } else
        {
            //log.error("ViewSet ["+viewSetName + "] View["+viewName + "] could not be created.");
        }
        return pb.getPanel();
    }*/
}
