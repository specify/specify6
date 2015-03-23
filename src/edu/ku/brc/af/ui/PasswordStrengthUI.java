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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.beans.PropertyChangeListener;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.UIPluginable;
import edu.ku.brc.ui.DocumentAdaptor;
import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * Adapted from the a web example at http://justwild.us/examples/password/ (open source example)
 * 
 * @author rod
 *
 * @code_status Alpha
 *
 * Dec 16, 2008
 *
 */
public class PasswordStrengthUI extends JPanel implements UIPluginable, GetSetValueIFace
{
    // Rules variables
    private static final int PWD_MIXED_CASE = 1;
    private static final int PWD_NUMERIC    = 1;
    private static final int PWD_SPECIAL    = 1;
    
    private String[] SCORE_KEYS = {"VERY_WEAK",      "WEAK",           "MEDIOCRE",    "STRONG",         "VERY_STRONG"};
    private String[] ERR_KEYS   = {"PWD_MIN_LENGTH", "PWD_NUMERIC", "PWD_MIXED_CASE", "PWD_SPECIAL"};
    private JCheckBox[] cbxs    = new JCheckBox[ERR_KEYS.length];
    private String[] scoreStrings;
    private String[] errorStrings;
    
    //private static final int PWD_STRENGTH   = 30;
    
    protected ColorPanel   colorPanel;
    
    protected JProgressBar progress; 
    protected int          score     = 0;
    protected String       errReason = null;
    protected String       scoreDesc = null;
    protected int          minPwdLen;
    protected boolean      doPainting = false;
    protected boolean      initialSet = true;

    /**
     * Constructor.
     */
    public PasswordStrengthUI()
    {
        this(8);
    }
    
    /**
     * Constructor.
     */
    public PasswordStrengthUI(final int minPwdLen)
    {
        super(new BorderLayout());
        
        this.minPwdLen = minPwdLen;

        progress = new JProgressBar(0, 100);
        //add(progress, BorderLayout.CENTER);
        
        UIRegistry.loadAndPushResourceBundle("specify_plugins");
        scoreStrings = new String[SCORE_KEYS.length];
        for (int i=0;i<SCORE_KEYS.length;i++)
        {
            scoreStrings[i] = UIRegistry.getResourceString(getKey(SCORE_KEYS[i]));
        }
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pbi = new PanelBuilder(new FormLayout(UIHelper.createDuplicateJGoodiesDef("p", "4px", cbxs.length), "p"));
        errorStrings = new String[ERR_KEYS.length];
        int x = 1;
        for (int i=0;i<ERR_KEYS.length;i++)
        {
            errorStrings[i] = UIRegistry.getResourceString(getKey(ERR_KEYS[i]));
            cbxs[i] = UIHelper.createCheckBox(errorStrings[i]);
            pbi.add(cbxs[i], cc.xy(x, 1));
            cbxs[i].setEnabled(false);
            x += 2;
        }
        UIRegistry.popResourceBundle();
        
        colorPanel = new ColorPanel();
        colorPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        
        PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g", "p,4px,p"), this);
        pb.add(colorPanel, cc.xy(1,1));
        pb.add(pbi.getPanel(), cc.xy(1,3));
    }
    
    /**
     * @param minPwdLen the minPwdLen to set
     */
    public void setMinPwdLen(int minPwdLen)
    {
        this.minPwdLen = minPwdLen;
    }

    /**
     * @param doPainting the doPainting to set
     */
    public void setDoPainting(boolean doPainting)
    {
        this.doPainting = doPainting;
    }

    /**
     * Hooks up a Password field to the Strength UI
     * @param pwdTF the password text field to be be hooked up to this
     * @param btn optional button that will be enabled when the PasswordText is not empty
     */
    public void setPasswordField(final JTextField pwdTF, final JButton btn)
    {
        DocumentListener listener = new DocumentAdaptor()
        {
            @Override
            protected void changed(DocumentEvent e)
            {
                String txt = ((JTextField)pwdTF).getText();
                
                if (StringUtils.isEmpty(txt))
                {
                    initialSet = true;
                    doPainting = false;
                    for (JCheckBox cbx : cbxs)
                    {
                        cbx.setSelected(false);
                    }
                    
                } else if (initialSet)
                {
                    initialSet = false;
                } else
                {
                    doPainting = true;
                }
                
                if (btn != null)
                {
                    btn.setEnabled(!txt.isEmpty());
                }
                
                if (doPainting)
                {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run()
                        {
                            checkStrength(pwdTF.getText()); // ignore return boolean
                            colorPanel.repaint();
                        }
                    });
                }
            }
        };
        
        pwdTF.getDocument().addDocumentListener(listener);
    }

    /**
     * @return
     */
    public String getScoreDesc()
    {
        int inx;
        
        if (score < 16)
        {
            inx = 0; 
            
        } else if (score < 25)
        {
            inx = 1; 
            
        } else if (score < 35)
        {
            inx = 2; 
            
        } else if (score < 45)
        {
            inx = 3; 
            
        } else
        {
            inx = 4; 
        }
        return scoreStrings[inx];
    }
    
    /**
     * @return the score 0-100
     */
    public int getScore()
    {
        return Math.min((int)(score / 50.0 * 100.0), 100);
    }

    /**
     * @param pwd
     * @return
     */
    public boolean checkStrength(final String pwd) 
    {
        boolean isOK = checkStrengthInternal(pwd);
        if (isOK)
        {
            progress.setValue(getScore());
            progress.setString(getScoreDesc());
        } else
        {
            progress.setValue(0);
            progress.setString(errReason);
        }
        return isOK;
    }
    
    /**
     * @param key
     * @return
     */
    private String getKey(final String key)
    {
        return "PasswordStrengthUI." + key;
    }

    /**
     * @param pwd
     * @return
     */
    private boolean checkStrengthInternal(final String pwd) 
    {
        int upper    = 0;
        int lower    = 0;
        int numbers  = 0;
        int special  = 0;
        int length   = 0;
        
        score = 0;

        Pattern p;
        Matcher m;
        if (StringUtils.isEmpty(pwd))
        {
            return false;
        }

        // Password Length
        length = pwd.length();
        if (length < 5) // length 4 or less
        {
            score += 3;
            
        } else if (length > 4 && length < 8) // length between 5 and 7
        {
            score += 6;
            
        } else if (length > 7 && length < 16) // length between 8 and 15
        {
            score += 12;
            
        } else if (length > 15) // length 16 or more
        {
            score += 18;
        }
        // 18 sub-total
        
        // Letters
        p = Pattern.compile(".??[a-z]");
        m = p.matcher(pwd);
        while (m.find()) // at least one lower case letter
        {
            lower += 1;
        }
        
        if (lower > 0)
        {
           score += 1; // 19
        }
        
        // Uppercase
        p = Pattern.compile(".??[A-Z]");
        m = p.matcher(pwd);
        while (m.find()) // at least one upper case letter
        {
            upper += 1; // 20
        }
        
        if (upper > 0)
        {
            score += 5; // 21
        }
        
        // Includes Numbers
        p = Pattern.compile(".??[0-9]");
        m = p.matcher(pwd);
        while (m.find()) // at least one number
        {
            numbers += 1; // 22
        }
        if (numbers > 0)
        {
            score += 5; // 27
            
            if (numbers > 1)
            {
                score += 2; // 29
                
                if (numbers > 2)
                {
                    score += 3;  // 31
                }
            }
        }
        
        // Special Characters
        p = Pattern.compile(".??[:,!,@,#,$,%,^,&,*,?,_,~]");
        m = p.matcher(pwd);
        while (m.find()) // at least one special character
        {
            special += 1; // 32
        }
        
        if (special > 0)
        {
            score += 5; // 37
            
            if (special > 1)
            {
                score += 5; // 42
            }
        }
        
        // Combinations
        if (upper > 0 && lower > 0) // both upper and lower case
        {
            score += 2; // 44
        }
        if ((upper > 0 || lower > 0) && numbers > 0) // both letters and numbers
        {
            score += 2; // 46
        }
        if ((upper > 0 || lower > 0) && numbers > 0 && special > 0) // letters, numbers, and special characters
        {
            score += 2; // 48
        }
        if (upper > 0 && lower > 0 && numbers > 0 && special > 0) // upper, lower, numbers, and special characters
        {
            score += 2; // 50
        }
        
        for (JCheckBox cbx : cbxs)
        {
            cbx.setSelected(false);
        }
        errReason = null;
        if (length < minPwdLen)
        { 
            errReason = errorStrings[0];
        } else
        {
            cbxs[0].setSelected(true);
        }
        
        if (numbers < PWD_NUMERIC)
        {
            errReason = errorStrings[1];
        } else
        {
            cbxs[1].setSelected(true);
        }
        
        // Does it meet the password policy?
        if (upper < PWD_MIXED_CASE || lower < PWD_MIXED_CASE)
        {
            errReason = errorStrings[2];
        } else
        {
            cbxs[2].setSelected(true);
        }
        
        if (special < PWD_SPECIAL)
        {
            errReason = errorStrings[3];
        } else
        {
            cbxs[3].setSelected(true);
        }

        return true;
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
        return "Password Strength";
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#addChangeListener(javax.swing.event.ChangeListener)
     */
    @Override
    public void addChangeListener(ChangeListener listener)
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#addPropertyChangeListener(java.beans.PropertyChangeListener)
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener l)
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
    public void initialize(final Properties properties, final boolean isViewMode)
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
    public void setCellName(final String cellName)
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#setParent(edu.ku.brc.af.ui.forms.FormViewObj)
     */
    @Override
    public void setParent(final FormViewObj parent)
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
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#getFieldNames()
     */
    @Override
    public String[] getFieldNames()
    {
        return new String[] {"password"};
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
    
    //-----------------------------------------------------------------------
    //-- 
    //-----------------------------------------------------------------------
    class ColorPanel extends JComponent
    {
        /**
         * 
         */
        public ColorPanel()
        {
            super();
        }

        /* (non-Javadoc)
         * @see javax.swing.JComponent#getPreferredSize()
         */
        @Override
        public Dimension getPreferredSize()
        {
            Insets ins = getInsets();
            return new Dimension(200, (new JLabel("X")).getPreferredSize().height+ins.top+ins.bottom+4);
        }
        
        /* (non-Javadoc)
         * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
         */
        @Override
        protected void paintComponent(final Graphics g)
        {
            super.paintComponent(g);
            
            if (isEnabled() && doPainting)
            {
                Graphics2D g2 = (Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(this.getBackground());
                
                //FontMetrics fm = g2.getFontMetrics();
                //String    text      = errReason != null ? errReason : getScoreDesc();
                //int       textWidth = fm.stringWidth(text);
                Dimension size      = getSize();
                
                Insets ins   = getInsets();
                int barWidth = size.width - ins.left - ins.right;
                int w        = (int)(barWidth * (getScore() / 100.0));
                int h        = size.height-ins.top-ins.bottom;
                
                int halfBW = barWidth / 2;
                GradientPaint bg = new GradientPaint(new Point(0, 0), Color.RED,
                                                     new Point(halfBW,0), Color.YELLOW);
                g2.setPaint(bg);
                
                Shape clipShape = g.getClip();
                
                g2.setClip(ins.left, ins.top, ins.left+w, h);
                
                g.fillRect(ins.left, ins.top, halfBW, h);
                
                // Second Half
                bg = new GradientPaint(new Point(ins.left+halfBW,0), Color.YELLOW,
                                       new Point(ins.left+barWidth,0), Color.GREEN);
                g2.setPaint(bg);
                g.fillRect(ins.left+halfBW, ins.top, halfBW*2, h);
                g.setClip(clipShape);
                
                //g.setColor(Color.BLACK);
                //System.out.println(score+"  "+getScore()+"  w: "+w+"  BW: "+barWidth);
                //g.drawString(text, (size.width-textWidth)/2, size.height - ((size.height-fm.getAscent())/2) - ins.bottom);
            }
        }

    }
}
