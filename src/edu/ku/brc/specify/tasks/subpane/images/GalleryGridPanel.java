/* Copyright (C) 2012, University of Kansas Center for Research
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Stack;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.ui.forms.ResultSetController;
import edu.ku.brc.af.ui.forms.ResultSetControllerListener;
import edu.ku.brc.ui.UIHelper;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Sep 3, 2012
 *
 */
public class GalleryGridPanel extends JPanel
{
    protected static final int CELL_SIZE = 135;
    protected static final int SEP_SIZE  = 8;
    
    protected Vector<ImageDataItem>       itemList    = new Vector<ImageDataItem>();
    
    protected Vector<ImageCellDisplay>    displayList = new Vector<ImageCellDisplay>();
    protected Stack<ImageCellDisplay>     recycleList = new Stack<ImageCellDisplay>();
    
    protected Vector<GalleryGridListener> selectionListeners = new Vector<GalleryGridListener>();
    protected Vector<ImageLoaderListener> loadListeners      = new Vector<ImageLoaderListener>();
    
    protected GalleryGridListener         infoListener;
    //protected MouseAdapter                mouseAdapter;
    protected ResultSetController         rsController;
    
    private int       gridRows;
    private int       gridCols;
    private Dimension layoutSize = null;
    
    private int numOfPages;
    private int pageNum;
    private int pageSize;
    private int currOffIndex  = 0;
    private int currCellIndex = -1;
 
    /**
     * 
     */
    public GalleryGridPanel(final ResultSetController rs)
    {
        super();
        this.rsController = rs;
        
        this.rsController.addListener(new ResultSetControllerListener()
        {
            @Override
            public void newRecordAdded() { }
            
            @Override
            public void indexChanged(int newIndex)
            {
                clearSelection();
                
                if (pageNum != newIndex)
                {
                    pageNum      = newIndex;
                    currOffIndex = (newIndex * pageSize);
                    
                    Rectangle r = getBounds();
                    reload(r.width, r.height);
                }
            }
            
            @Override
            public boolean indexAboutToChange(int oldIndex, int newIndex) { return true; }
        });
        
        setBackground(Color.WHITE);
        setOpaque(true);
        
        infoListener = new GalleryGridListener()
        {
            @Override
            public void itemSelected(final ImageCellDisplay imgCellDsp, 
                                     final int              index, 
                                     final boolean          isSelected, 
                                     final int              clickCount)
            {
                currCellIndex = -1;
                int i   = 0;
                for (ImageCellDisplay icd : displayList)
                {
                    if (icd.isSelected())
                    {
                        icd.setSelected(false);
                        icd.repaint();
                        notifyItemSelected(icd, currCellIndex, false, clickCount);
                    }
                    if (icd == imgCellDsp)
                    {
                        currCellIndex = i;
                    }
                    i++;
                }
                
                if (isSelected && currCellIndex > -1)
                {
                    imgCellDsp.setSelected(true);
                    imgCellDsp.repaint();
                    int adjustedIndex = currCellIndex + (pageNum * pageSize);
                    notifyItemSelected(imgCellDsp, adjustedIndex, true, clickCount);
                }
            }
            
            @Override
            public void infoSelected(final ImageCellDisplay imgCellDsp, final int index, final boolean isSelected, final int whichBtn)
            {
                currCellIndex = -1;
                if (imgCellDsp != null)
                {
                    int i   = 0;
                    for (ImageCellDisplay icd : displayList)
                    {
                        if (icd == imgCellDsp)
                        {
                            currCellIndex = i;
                            break;
                        }
                        i++;
                    }
    
                    notifyInfoSelected(imgCellDsp, currCellIndex, isSelected, whichBtn);
                }
            }
        };
    }
    
    /**
     * @param layoutSize the layoutSize to set
     */
    public void setLayoutSize(Dimension layoutSize)
    {
        this.layoutSize = layoutSize;
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#getPreferredSize()
     */
    @Override
    public Dimension getPreferredSize()
    {
        return this.layoutSize == null ? super.getPreferredSize() : this.layoutSize;
    }

    /**
     * 
     */
    public void clearSelection()
    {
        for (ImageCellDisplay icd : displayList)
        {
            icd.getImageDataItem().setSelected(false);
        }
        notifyInfoSelected(null, -1, false, -10);
    }

    /* (non-Javadoc)
     * @see java.awt.Component#setBounds(int, int, int, int)
     */
    @Override
    public void setBounds(int xc, int yc, int width, int height)
    {
        Rectangle r = getBounds();
        if (r.x == xc && r.y == yc && r.width == width && r.height == height)
        {
            return;
        }
        super.setBounds(xc, yc, width, height);
        
        reload(width, height);
    }
    
    /**
     * @param width
     * @param height
     */
    private void reload(int width, int height)
    {
        System.out.println(String.format("%d, %d", width, height));
        
        for (ImageCellDisplay imgDsp : displayList)
        {
            imgDsp.setImage((ImageIcon)null);
        }
        recycleList.addAll(displayList);
        displayList.clear();
        this.removeAll();

        if (itemList != null && itemList.size() > 0)
        {
            int fullCellSize = CELL_SIZE + SEP_SIZE;
            gridRows = (height - SEP_SIZE) / fullCellSize;
            gridCols = (width - SEP_SIZE) / fullCellSize;
            
            // For Testing
            //gridRows = 2;
            //gridCols = 2;
            
            //System.out.println(String.format("fullCellSize: %d   CELL_SIZE: %d   SEP_SIZE: %d", fullCellSize, CELL_SIZE, SEP_SIZE));
            //System.out.println(String.format("gridRows: %d   gridCols: %d", gridRows, gridCols));
            
            pageSize = gridCols * gridRows;
            if (pageSize == 0)
            {
                System.err.println("PageSize is ZERO!");
            }
            
            int numItems = itemList.size();
            numOfPages = (numItems / pageSize) + (numItems % pageSize > 0 ? 1 : 0);
            pageNum    = (currOffIndex / pageSize) + (currOffIndex % pageSize > 0 ? 1 : 0);
            
            //System.out.println(String.format("numOfPages: %d   pageNum: %d    pageSize: %d", numOfPages, pageNum, pageSize));
            
            int actualVSep = (height - (gridRows * fullCellSize)) / (gridRows + 1);
            int actualHSep = (width - (gridCols * fullCellSize)) /  (gridCols + 1);
            //System.out.println(String.format("actualVSep: %d   actualHSep: %d", actualVSep, actualHSep));
            
            String rowDef = UIHelper.createDuplicateJGoodiesDef("p", actualVSep+"px", gridRows) + ",8px,p";
            String colDef = UIHelper.createDuplicateJGoodiesDef("p", actualHSep+"px", gridCols);
            
            CellConstraints cc = new CellConstraints();
            PanelBuilder    pb = new PanelBuilder(new FormLayout(actualHSep + "px," + colDef, actualVSep + "px," + rowDef), this);
            int             x  = 2;
            int             y  = 2;
            for (int i=0;i<pageSize && i<numItems;i++)
            {
                ImageCellDisplay imgDsp = null;
                if (recycleList.size() > 0)
                {
                    imgDsp = recycleList.pop();
                } else
                {
                    int selSize = ImageCellDisplay.SELECTION_WIDTH * 2;
                    imgDsp = new ImageCellDisplay(CELL_SIZE-selSize, CELL_SIZE-selSize);
                    imgDsp.addListener(infoListener);
                }
                
                int dataInx = i + (pageSize * pageNum);
                if (dataInx == itemList.size())
                {
                    break;
                }
                //System.out.println("Display "+i+"   dataInx: "+dataInx);
                if (dataInx > -1 && dataInx < itemList.size())
                {
                    imgDsp.setImageDataItem(itemList.get(dataInx));
                } else
                {
                    imgDsp.setImageDataItem(null);
                }
                
                if (imgDsp.getImageDataItem() != null && imgDsp.getImageDataItem().getImgIcon() == null)
                {
                    imgDsp.startLoad();
                }
                displayList.add(imgDsp);
                
                pb.add(imgDsp, cc.xy(x, y));
                x += 2;
                if (((x-1) / 2) % gridCols == 0)
                {
                    x = 2;
                    y += 2;
                }
            }
            
            fillDisplay();
            
            rsController.setLength(numOfPages);
            rsController.setIndex(pageNum);
            
        } else
        {
            recycleList.addAll(displayList);
            displayList.clear();
            this.removeAll();
        }
        invalidate();
    }

    /* (non-Javadoc)
     * @see java.awt.Container#doLayout()
     */
    @Override
    public void doLayout()
    {
        super.doLayout();
        
        /*SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                GalleryGridPanel.this.repaint();
            }
        });*/
    }
    
    /**
     * @return the rs
     */
    public ResultSetController getResultSetController()
    {
        return rsController;
    }

    /**
     * 
     */
    private void fillDisplay()
    {
        int pageEnd = currOffIndex + pageSize; 
        for (int i=currOffIndex;i<itemList.size() && i < pageEnd;i++)
        {
           displayList.get(i-currOffIndex).setImage(itemList.get(i).getImgIcon()); 
           displayList.get(i-currOffIndex).repaint();
        }
    }

    /**
     * @return the pageNum
     */
    public int getPageNum()
    {
        return pageNum;
    }

    /**
     * @param pageNum the pageNum to set
     */
    public void setPageNum(int pageNum)
    {
        this.pageNum = pageNum;
    }

    /**
     * @param itemList the itemList to set
     */
    public void setItemList(Vector<ImageDataItem> itemList)
    {
        this.itemList.clear();
        this.itemList.addAll(itemList);
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                //GalleryGridPanel.this.doLayout();
                Rectangle r = getBounds();
                GalleryGridPanel.this.setBounds(r.x, r.y, r.width, r.height);
            }
        });
    }
    
    /**
     * @return the currIndex
     */
    public int getOffsetIndex()
    {
        return currOffIndex;
    }
    
    /**
     * @return the currIndex
     */
    public int getSelectedCellIndex()
    {
        return currCellIndex;
    }
    
    /**
     * @param lsl
     */
    public void addListSelectionListener(final GalleryGridListener lsl)
    {
        if (lsl != null)
        {
            selectionListeners.add(lsl);
        }
    }

    /**
     * @param lsl
     */
    public void removeListSelectionListener(final GalleryGridListener lsl)
    {
        if (lsl != null)
        {
            selectionListeners.remove(lsl);
        }
    }
    
    private void notifyItemSelected(ImageCellDisplay item, int index, boolean isSelected, int clickCount)
    {
        for (GalleryGridListener lsl : selectionListeners)
        {
            lsl.itemSelected(item, index, isSelected, clickCount);
        }
    }
    
    private void notifyInfoSelected(ImageCellDisplay item, int index, boolean isSelected, final int whichBtn)
    {
        for (GalleryGridListener lsl : selectionListeners)
        {
            lsl.infoSelected(item, index, isSelected, whichBtn);
        }
    }
    
    /**
     * @param lsl
     */
    public void addLoadImageListener(final ImageLoaderListener l)
    {
        if (l != null)
        {
            loadListeners.add(l);
        }
    }

    /**
     * @param lsl
     */
    public void removeLoadImageListener(final GalleryGridListener l)
    {
        if (l != null)
        {
            loadListeners.remove(l);
        }
    }
}
