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
package edu.ku.brc.specify.plugins.ipadexporter;

import javax.swing.SwingUtilities;

import edu.ku.brc.ui.ProgressDialog;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.dnd.SimpleGlassPane;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * May 4, 2012
 *
 */
public class ProgressDelegate
{
    private ProgressDialog  progressDlg = null;
    private SimpleGlassPane glassPane   = null;
    
    //private int             val         = 0;
    //private int             min         = 0;
    private int             max         = 0;
    
    /**
     * @param doDlg
     */
    public ProgressDelegate(final boolean doDlg)
    {
        super();
        
        if (doDlg)
        {
            progressDlg = new ProgressDialog("iPad Exporter", true, false);
        } else
        {
            //glassPane = UIRegistry.writeSimpleGlassPaneMsg(getResourceString("NEW_INTER_LOADING_PREP"), 24);
            glassPane = UIRegistry.writeSimpleGlassPaneMsg("iPad Exporter", 24);
        }
    }

    /**
     * 
     */
    public void incOverall()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                if (progressDlg != null)
                {
                    progressDlg.processDone();
                } else
                {
                    //int v = (int)(((double)val / (double)max) * 100.0);
                    //glassPane.setProgress(v);
                }
            }
        });
    }
    
    /**
     * @param min
     * @param max
     */
    public void setOverall(final int min, final int max)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                if (progressDlg != null)
                {
                    progressDlg.setOverall(min, max);
                } else
                {
                    //this.min = min;
                    //this.max = max;
                    //glassPane.setProgress(0);
                }
            }
        });
    }
    
    /**
     * @param value
     */
    public void setOverall(final int value)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                if (progressDlg != null)
                {
                    progressDlg.setOverall(value);
                } else
                {
                    //int v = (int)(((double)value / (double)max) * 100.0);
                    //glassPane.setProgress(v);
                }
            }
        });
    }
    
    /**
     * @param min
     * @param max
     */
    public void setProcess(final int min, final int max)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                if (progressDlg != null)
                {
                    progressDlg.processDone();
                } else
                {
                    ProgressDelegate.this.max = max;
                    setProcess(0);
                }
                
            }
        });
    }
    
    /**
     * @param value
     */
    public void setProcess(final int value)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                if (progressDlg != null)
                {
                    progressDlg.processDone();
                } else
                {
                    int v = (int)(((double)value / (double)max) * 100.0);
                    glassPane.setProgress(v);
                }
                
            }
        });
    }
    
    /**
     * @param text
     */
    public void setDesc(final String text)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                if (progressDlg != null)
                {
                    progressDlg.setDesc(text);
                } else
                {
                    UIRegistry.writeSimpleGlassPaneMsg(text, 24);
                }
            }
        });
    }
    
    /**
     * 
     */
    public void processDone()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                if (progressDlg != null)
                {
                    progressDlg.processDone();
                } else
                {
                    setProcess(100);
                }

            }
        });
    }
    
    /**
     * 
     */
    public void shutdown()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                if (progressDlg != null)
                {
                    progressDlg.setVisible(false);
                    progressDlg.dispose();
                    progressDlg = null;
                } else
                {
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            UIRegistry.clearSimpleGlassPaneMsg();
                        }
                    });
                }
            }
        });
    }
    
    /**
     * 
     */
    public void showAndFront()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                if (progressDlg != null)
                {
                    progressDlg.setVisible(true);
                    progressDlg.toFront();
                }
            }
        });
    }
}
