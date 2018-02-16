package edu.ku.brc.ui.tmanfe;

import javax.swing.*;
import java.awt.*;

public class RowHeaderLabel extends JComponent
{
    protected String rowNumStr;
    protected int    rowNum;
    protected Font font;

    protected int    labelWidth  = Integer.MAX_VALUE;
    protected int    labelheight = Integer.MAX_VALUE;

    public RowHeaderLabel(int rowNum, final Font font)
    {
        this.rowNum    = rowNum;
        this.rowNumStr = Integer.toString(rowNum);
        this.font      = font;
    }

    public int getRowNum()
    {
        return rowNum;
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
     */
    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        g.setFont(font);

        if (labelWidth == Integer.MAX_VALUE)
        {
            FontMetrics fm = getFontMetrics(font);
            labelheight = fm.getAscent();
            labelWidth  = fm.stringWidth(rowNumStr);
        }

        Insets    ins  = getInsets();
        Dimension size = this.getSize();
        int y = size.height - ((size.height - labelheight) / 2) - ins.bottom;

        g.drawString(rowNumStr, (size.width - labelWidth) / 2, y);
    }
}
