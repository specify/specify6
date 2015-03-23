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
package edu.ku.brc.af.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Properties;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;

import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.UIPluginable;
import edu.ku.brc.specify.datamodel.DNASequence;
import edu.ku.brc.ui.GetSetValueIFace;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 7, 2009
 *
 */
public class IllustrativeBarCodeUI extends JPanel implements GetSetValueIFace, UIPluginable
{
    protected int[]  totals   = {0, 0, 0, 0, 0};
    protected String sequence = "";
    /**
     * 
     */
    public IllustrativeBarCodeUI()
    {
        super();
        
        setPreferredSize(new Dimension(700, 100));
    }
    
    private int getIndex(final char code)
    {
        switch (code)
        {
            case 'a' :
            case 'A' : return 0;
            
            case 'g' :
            case 'G' : return 1;
            
            case 'c' :
            case 'C' : return 2;
            
            case 't' :
            case 'T' : return 3;
        }
        return 4;
    }

    /**
     * @param code
     * @return
     */
    private Color getColor(final char code)
    {
        switch (code)
        {
            case 'a' :
            case 'A' : return Color.GREEN;
            
            case 'g' :
            case 'G' : return Color.BLACK;
            
            case 'c' :
            case 'C' : return Color.BLUE;
            
            case 't' :
            case 'T' : return Color.RED;
        }
        return Color.WHITE;
    }

    /**
     * @param sequence
     */
    public void setSequence(final String sequence)
    {
        for (int i=0;i<totals.length;i++)
        {
            totals[i] = 0;
        }
        this.sequence = sequence;
        
        if (sequence != null)
        {
            for (int i=0;i<sequence.length();i++)
            {
                totals[getIndex(sequence.charAt(i))]++;
            }
        }
        repaint();
    }
    
    /**
     * @param code
     * @return
     */
    public int getTotal(final char code)
    {
        return totals[getIndex(code)];
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#getPreferredSize()
     */
    public Dimension getPreferredSize()
    {
        return new Dimension(700, 150);
    }
    
    public Dimension getSize()
    {
        Dimension size = super.getSize();
        return size;//new Dimension(size.width, 200);
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#paint(java.awt.Graphics)
     */
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        
        RenderingHints hints = new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        
        Graphics2D g2 = (Graphics2D)g;
        
        Dimension size = getSize();
        g2.setColor(Color.WHITE);
        
        g2.fillRect(0, 0, size.width, size.height);
        
        Font font = new Font("Arial", Font.PLAIN, 10);
        g.setFont(font);
        
        FontMetrics fm        = g.getFontMetrics();
        int         txtHeight = fm.getHeight();
        
        int barHeight = 25;
        int spacing   = txtHeight + 4;
        int startX    = 10;
        int x         = startX;
        int y         = spacing;
        int barLength = (size.width/2) - (2 * startX);
        
        if (sequence != null)
        {
            g.setColor(Color.BLACK);
            g2.drawString("0", x, y-2);
            
            int endX = 0;
            int i;
            for (i=0;i<sequence.length();i++)
            {
                char code = sequence.charAt(i);
                g2.setColor(getColor(code));
                g2.drawLine(x, y, x, y+barHeight);
                x += 2;            
                if ((i+1) % barLength == 0)
                {
                    g.setColor(Color.BLACK);
                    g2.drawString(Integer.toString(i), x-fm.stringWidth(Integer.toString(i)), y-2);
                    y += barHeight + spacing;
                    x = startX;
                    
                    g2.drawString(Integer.toString(i+1), x, y-2);
                    endX = x+fm.stringWidth(Integer.toString(i+1));
                }
            }
            g.setColor(Color.BLACK);
            x = Math.max(endX+6, x-fm.stringWidth(Integer.toString(i)));
            g2.drawString(Integer.toString(i), x, y-2);
        }
    }
    
    //--------------------------------------------------------------------------------------------
    //-- UIPlugin
    //--------------------------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#addChangeListener(javax.swing.event.ChangeListener)
     */
    @Override
    public void addChangeListener(ChangeListener listener)
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#getUIComponent()
     */
    @Override
    public JComponent getUIComponent()
    {
        return this;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#initialize(java.util.Properties, boolean)
     */
    @Override
    public void initialize(Properties properties, boolean isViewMode)
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#isNotEmpty()
     */
    @Override
    public boolean isNotEmpty()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#setCellName(java.lang.String)
     */
    @Override
    public void setCellName(String cellName)
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#setParent(edu.ku.brc.af.ui.forms.FormViewObj)
     */
    @Override
    public void setParent(FormViewObj parent)
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#shutdown()
     */
    @Override
    public void shutdown()
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.GetSetValueIFace#getValue()
     */
    @Override
    public Object getValue()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.GetSetValueIFace#setValue(java.lang.Object, java.lang.String)
     */
    @Override
    public void setValue(Object value, String defaultValue)
    {
        //setSequence(value != null ? value.toString() : "");
        if (value != null)
        {
            setSequence(((DNASequence)value).getGeneSequence());
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#canCarryForward()
     */
    @Override
    public boolean canCarryForward()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#getCarryForwardFields()
     */
    @Override
    public String[] getCarryForwardFields()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#getTitle()
     */
    @Override
    public String getTitle()
    {
        return "IllustrativeBarCode";
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#getFieldNames()
     */
    @Override
    public String[] getFieldNames()
    {
        return new String[] {"geneSequence"};
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#carryForwardStateChange()
     */
    @Override
    public void carryForwardStateChange()
    {
        // no op
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#setNewObj(boolean)
     */
    @Override
    public void setNewObj(boolean isNewObj)
    {
        // no op
    }
}
