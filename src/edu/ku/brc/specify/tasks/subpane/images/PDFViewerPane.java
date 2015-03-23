/* Copyright (C) 2015, University of Kansas Center for Research
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

import java.io.File;
import java.io.FileInputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import ch.randelshofer.pdf.JPDFFilePanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.sun.pdfview.PDFFile;

import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.tasks.subpane.BaseSubPane;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Aug 30, 2012
 *
 */
public class PDFViewerPane extends BaseSubPane
{
    /**
     * @param name
     * @param task
     * @param idi
     */
    public PDFViewerPane(final String name, final Taskable task, final ImageDataItem idi)
    {
        super(name, task);
        
        createUI();
    }
    
    /**
     * Creates the UI.
     */
    protected void createUI()
    {
        PDFFile file = null;
        try
        {
            File            f = new File("example2.pdf");
            FileInputStream fis = new FileInputStream(f);
            FileChannel     fc  = fis.getChannel();
    
            // Get the file's size and then map it into memory
            int sz = (int)fc.size();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, sz);
    
            file = new PDFFile(bb);
            
        } catch (Exception ex)
        {
            
        }
           
        if (file != null)
        {
            JPDFFilePanel panel = new JPDFFilePanel();
            panel.setPDF(file);
            
            CellConstraints cc = new CellConstraints();
            
            PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g", "f:p:g"), this);
            pb.add(pb.getPanel(), cc.xy(1, 1));
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.subpane.BaseSubPane#aboutToShutdown()
     */
    @Override
    public boolean aboutToShutdown()
    {
        return super.aboutToShutdown();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.subpane.BaseSubPane#shutdown()
     */
    @Override
    public void shutdown()
    {
        super.shutdown();
    }
}
