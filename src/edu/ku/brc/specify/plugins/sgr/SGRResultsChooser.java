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
package edu.ku.brc.specify.plugins.sgr;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JDialog;
import javax.swing.WindowConstants;

import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.HighlightPredicate;

import com.google.common.base.Function;

import edu.ku.brc.sgr.MatchResults;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.ui.tmanfe.SpreadSheet;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Sep 27, 2010
 *
 */
public class SGRResultsChooser extends JDialog implements WindowListener
{
    private SGRResultsDisplay          resultsDisplayPanel;
    private final Function<Void, Void> finished;

    private final SpreadSheet          spreadSheet;
    private final ColorHighlighter     highlighter;

    /**
     * @param parent
     * @param queue
     * @param spreadSheet 
     * @param finished 
     */
    public SGRResultsChooser(final Frame parent, final WorkbenchRow row, MatchResults results, 
                             SpreadSheet spreadSheet, Function<Void, Void> finished)
    {
        super(parent);//parent, "", false, CustomDialog.NONE_BTN, null);
        
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        
        addWindowListener(this);
        
        this.finished = finished;
        this.spreadSheet = spreadSheet;
        
        Rectangle r = spreadSheet.getCellRect(row.getRowNumber(), 0, false);
        Point p = spreadSheet.getLocationOnScreen();
        Container ssParent = spreadSheet.getParent();
        p.translate(r.x, r.y + r.height + 10);
        setLocation(p);
        
        resultsDisplayPanel = new SGRResultsDisplay(ssParent.getWidth(), results);
        setContentPane(resultsDisplayPanel);
        pack();
        
        highlighter = new ColorHighlighter(
                new HighlightPredicate()
                {
                    @Override
                    public boolean isHighlighted(Component arg0, ComponentAdapter arg1)
                    {
                        return row.getRowNumber() == arg1.row;
                    }
                }, Color.YELLOW, Color.BLACK);
        
        spreadSheet.addHighlighter(highlighter); 
        spreadSheet.clearSelection();
        //setUndecorated(true);
    }
    

//  WindowListener Implementation    
    
    @Override
    public void windowActivated(WindowEvent e)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void windowClosed(WindowEvent e)
    {
        // TODO Auto-generated method stub
        spreadSheet.removeHighlighter(highlighter);
        spreadSheet.repaint();
        finished.apply(null);
    }

    @Override
    public void windowClosing(WindowEvent e)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void windowDeactivated(WindowEvent e)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void windowDeiconified(WindowEvent e)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void windowIconified(WindowEvent e)
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void windowOpened(WindowEvent e)
    {
        // TODO Auto-generated method stub
    }
}
