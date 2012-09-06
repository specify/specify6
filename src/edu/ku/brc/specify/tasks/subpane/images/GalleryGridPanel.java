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
import java.awt.event.MouseAdapter;
import java.util.Stack;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.ui.forms.ResultSetController;
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
    protected MouseAdapter                mouseAdapter;
    protected ResultSetController         rs;
    
    private int gridRows;
    private int gridCols;
    
    private int numOfPages;
    private int pageNum;
    private int pageSize;
    private int currIndex = 0;
 
    /**
     * 
     */
    public GalleryGridPanel(final ResultSetController rs)
    {
        super();
        this.rs = rs;
        
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
                int inx = -1;
                int i   = 0;
                for (ImageCellDisplay icd : displayList)
                {
                    if (icd.isSelected())
                    {
                        icd.setSelected(false);
                        icd.repaint();
                        notifyItemSelected(icd, inx, false, clickCount);
                    }
                    if (icd == imgCellDsp)
                    {
                        inx = i;
                    }
                    i++;
                }
                
                if (isSelected && inx > -1)
                {
                    imgCellDsp.setSelected(true);
                    imgCellDsp.repaint();
                    notifyItemSelected(imgCellDsp, inx, true, clickCount);
                }
            }
            
            @Override
            public void infoSelected(final ImageCellDisplay imgCellDsp, final int index, final boolean isSelected)
            {
                int inx = -1;
                int i   = 0;
                for (ImageCellDisplay icd : displayList)
                {
                    if (icd == imgCellDsp)
                    {
                        inx = i;
                        break;
                    }
                    i++;
                }

                notifyInfoSelected(imgCellDsp, inx, isSelected);
            }
        };
    }

    /* (non-Javadoc)
     * @see java.awt.Component#setBounds(int, int, int, int)
     */
    @Override
    public void setBounds(int xc, int yc, int width, int height)
    {
        super.setBounds(xc, yc, width, height);
        
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
            
            pageSize = gridCols * gridRows;
            if (pageSize == 0)
            {
                System.err.println("PageSize is ZERO!");
            }
            
            int numItems = itemList.size();
            numOfPages = (numItems / pageSize) + (numItems % pageSize > 0 ? 1 : 0);
            pageNum    = (currIndex / pageSize) + (currIndex % pageSize > 0 ? 1 : 0);
            
            int actualVSep = (height - (gridRows * fullCellSize)) / (gridRows + 1);
            int actualHSep = (width - (gridCols * fullCellSize)) /  (gridCols + 1);
            
            String rowDef = UIHelper.createDuplicateJGoodiesDef("p", actualVSep+"px", gridRows) + ",8px,p";
            String colDef = UIHelper.createDuplicateJGoodiesDef("p", actualHSep+"px", gridCols);
            
            CellConstraints cc = new CellConstraints();
            PanelBuilder    pb = new PanelBuilder(new FormLayout(colDef, rowDef), this);
            int             x  = 1;
            int             y  = 1;
            for (int i=0;i<pageSize && i<numItems;i++)
            {
                ImageCellDisplay imgDsp = null;
                if (recycleList.size() > 0)
                {
                    imgDsp = recycleList.pop();
                } else
                {
                    imgDsp = new ImageCellDisplay(CELL_SIZE, CELL_SIZE);
                    imgDsp.addListener(infoListener);
                }
                
                System.out.println("Display "+i);
                imgDsp.setImageDataItem(itemList.get(i));
                imgDsp.startLoad();
                displayList.add(imgDsp);
                
                
                pb.add(imgDsp, cc.xy(x, y));
                x += 2;
                if ((x / 2) % gridCols == 0)
                {
                    x = 1;
                    y += 2;
                }
            }
            
            fillDisplay();
            
            rs.setLength(numOfPages);
            rs.setIndex(pageNum);
            
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
        return rs;
    }

    /**
     * 
     */
    private void fillDisplay()
    {
        int pageEnd = currIndex + pageSize; 
        for (int i=currIndex;i<itemList.size() && i < pageEnd;i++)
        {
           displayList.get(i).setImage(itemList.get(i).getImgIcon()); 
           displayList.get(i).repaint();
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
    }
    
    /**
     * @return the currIndex
     */
    public int getSelectedIndex()
    {
        return currIndex;
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
    
    private void notifyInfoSelected(ImageCellDisplay item, int index, boolean isSelected)
    {
        for (GalleryGridListener lsl : selectionListeners)
        {
            lsl.infoSelected(item, index, isSelected);
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
