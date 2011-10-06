/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/**
 * 
 */
package edu.ku.brc.specify.plugins.sgr;

import java.awt.Color;
import java.awt.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdesktop.swingx.decorator.AbstractHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.Highlighter;

import edu.ku.brc.sgr.datamodel.BatchMatchResultItem;
import edu.ku.brc.sgr.datamodel.BatchMatchResultSet;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.ui.tmanfe.SpreadSheet;

/**
 * @author ben
 *
 * @code_status Alpha
 *
 * Created Date: May 23, 2011
 *
 */
public class WorkbenchColorizer
{
    private final Workbench          workbench;
    private final SpreadSheet        spreadSheet;
    private Highlighter              highlighter = null;
    private final Map<String, Float> sgrScores   = new HashMap<String, Float>();
    private float                    maxScore;
    private final Color              origGridColor;
    private BatchMatchResultSet      resultSet;
       
    public WorkbenchColorizer(Workbench workbench, SpreadSheet spreadSheet)
    {
        this.workbench = workbench;
        this.spreadSheet = spreadSheet;
        origGridColor = spreadSheet.getGridColor();
    }

    public void setBatchResults(BatchMatchResultSet results)
    {
        this.resultSet = results;
        
        if (highlighter != null)
        {
            spreadSheet.removeHighlighter(highlighter);
        }
        
        maxScore = (float)results.getMax();
        sgrScores.clear();
        
        List<BatchMatchResultItem> items = results.getAllItems(); 
        for (BatchMatchResultItem item : items)
        {
            Color c = SGRColors.colorForScore(item.maxScore(), maxScore);
            sgrScores.put(item.matchedId(), item.maxScore());
        }
        
        highlighter = new AbstractHighlighter() {

            @Override
            protected Component doHighlight(Component arg0, ComponentAdapter arg1) {
                if (arg1.isSelected()) return arg0;

                WorkbenchRow row = workbench.getRow(spreadSheet.convertRowIndexToModel(arg1.row));
                Float score = getScoreForRow(row);
                if (score != null)
                {
                    Color c = SGRColors.colorForScore(score, maxScore);
                    arg0.setBackground(c);
                }
                return arg0;
            }
        };

        spreadSheet.addHighlighter(highlighter);
//        spreadSheet.setShowGrid(false);
//        spreadSheet.setShowHorizontalLines(true);
        spreadSheet.setGridColor(Color.DARK_GRAY);
//        showHideSgrCol(true);
    }
    
    public Float getScoreForRow(WorkbenchRow row)
    {
        return getScoreForRow(row, false);
    }
    
    public Float getScoreForRow(WorkbenchRow row, boolean scaled)
    {
        Float score = sgrScores.get("TOMATCH-" + row.getId());
        if (scaled && score != null && maxScore > 0.0)
            return score/maxScore;
        else
            return score;
    }
    
    public float getMaxScore()
    {
        return maxScore;
    }
    
    public BatchMatchResultSet getResultSet()
    {
        return resultSet;
    }
    
    public void stopColoring()
    {
        if (highlighter != null)
        {
            spreadSheet.removeHighlighter(highlighter);
//            spreadSheet.setShowGrid(true);
            spreadSheet.setGridColor(origGridColor);
            highlighter = null;
        }
        
//        model.showHideSgrCol(false);
    }

    public void cleanup()
    {
        stopColoring();
    }
}
