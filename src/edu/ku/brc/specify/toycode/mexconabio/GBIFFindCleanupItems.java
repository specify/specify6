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
package edu.ku.brc.specify.toycode.mexconabio;

import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Vector;

import javax.swing.DefaultListCellRenderer;

import edu.ku.brc.specify.dbsupport.cleanuptools.BaseFindCleanupItems;
import edu.ku.brc.specify.dbsupport.cleanuptools.FindItemInfo;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Aug 3, 2010
 *
 */
public class GBIFFindCleanupItems extends BaseFindCleanupItems
{
    protected AnalysisWithGBIF awg; 
    
    /**
     * @param tblInfo
     * @throws HeadlessException
     */
    public GBIFFindCleanupItems(final AnalysisWithGBIF awg) throws HeadlessException
    {
        super("GBIF", "Catalog Number");
        this.awg = awg;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.cleanuptools.BaseFindCleanupItems#doCleanupOfItem()
     */
    @Override
    protected void doCleanupOfItem()
    {
        FindItemInfo itemInfo = (FindItemInfo)itemsList.getSelectedValue();
        if (itemInfo != null)
        {
            GBIFCleanupResults gbifResults = new GBIFCleanupResults("TITLE", itemInfo, awg);
            
            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize(); 
            gbifResults.createUI();
            gbifResults.setBounds(0, 0, dim.width, dim.height);
            gbifResults.setVisible(true);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.cleanuptools.BaseFindCleanupItems#getListCellRenderer()
     */
    @Override
    protected DefaultListCellRenderer getListCellRenderer()
    {
        return null; // then it will use toString()
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.cleanuptools.BaseFindCleanupItems#doWork()
     */
    @Override
    protected Vector<FindItemInfo> doWork()
    {
        Connection dstDBConn = awg.getDstDBConn();
        
        Vector<FindItemInfo> items = new Vector<FindItemInfo>();
        Statement        stmt  = null;
        
        try
        {
            stmt  = dstDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            
            ResultSet rs = stmt.executeQuery("SELECT id, other_collnum, COUNT(other_collnum) FROM raw_cache GROUP BY other_collnum");
            while (rs.next())
            {
                int     id        = rs.getInt(1);
                String  catNum    = rs.getString(2);
                int     grpCnt    = rs.getInt(3);
                
                String titleStr = String.format("%s - %d", catNum, grpCnt);
                items.add(new FindItemInfo(id, catNum, titleStr));
            }
            rs.close();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally 
        {
            try
            {
                if (stmt != null)
                {
                    stmt.close();
                }
            } catch (Exception ex) {}
        }
        return items;
    }

}
