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
package edu.ku.brc.af.ui.forms.persist;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Mar 26, 2010
 *
 */
public class FormDevHelper
{
    private static Boolean       isFormDevMode = null;
    private static CustomDialog  frame         = null;
    private static JTextArea     msgArea;
    private static StringBuilder buffer        = new StringBuilder();
    
    
    /**
     * @return
     */
    public static JDialog getLogFrame()
    {
        if (frame == null)
        {
            msgArea = UIHelper.createTextArea(10, 60);
            msgArea.setLineWrap(true);
            msgArea.setWrapStyleWord(true);
            
            JPanel p = new JPanel(new BorderLayout());
            p.add(UIHelper.createScrollPane(msgArea, true), BorderLayout.CENTER);
            
            frame = new CustomDialog((Frame)UIRegistry.getTopWindow(), "Form Development Errors", false, CustomDialog.OKCANCEL, p)
            {
                @Override
                protected void cancelButtonPressed()
                {
                    buffer.setLength(0);
                    msgArea.setText("");
                }
            };
            frame.setOkLabel("Close");
            frame.setCancelLabel("Clear");
            
            frame.setSize(800, 600);
            frame.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        }
        return frame;
    }
    
    /**
     * @return the isFormDevMode
     */
    public static boolean isFormDevMode()
    {
        if (isFormDevMode == null)
        {
            isFormDevMode = AppPreferences.getLocalPrefs().getBoolean("form.devmode", false);
        }
        return isFormDevMode;
    }

    /**
     * @param isFormDevMode the isFormDevMode to set
     */
    public static void setIsFormDevMode(Boolean isFormDevMode)
    {
        FormDevHelper.isFormDevMode = isFormDevMode;
    }
    
    /**
     * @param msg
     * @param throwable
     */
    public static void appendFormDevError(final String msg, final Throwable throwable)
    {
        appendFormDevError(msg + " Exception "+throwable.getMessage());
    }
    
    /**
     * @param msg
     * @param throwable
     */
    public static void appendFormDevError(final Throwable throwable)
    {
        appendFormDevError(" Exception "+throwable.getMessage());
    }
    
    /**
     * @param msg
     */
    public static void showFormDevError(final String msg)
    {
        appendFormDevError(msg);
        UIRegistry.showError(msg);
    }
    
    /**
     * @param msg
     */
    public static void showFormDevError(final Throwable throwable)
    {
        showFormDevError(throwable);
    }
    
    /**
     * @param msg
     */
    public static void appendFormDevError(final String msg)
    {
        if (isFormDevMode)
        {
            if (msgArea != null)
            {
                boolean showFirstTime = frame == null;
                
                getLogFrame();
                
                buffer.append(msg);
                buffer.append("\n");
                if (buffer.length() > 4096)
                {
                    int overage = buffer.length() - 4096;
                    int inx     = buffer.indexOf("\n", overage);
                    buffer.replace(0, inx, "");
                }
                msgArea.setText(buffer.toString());
                msgArea.setCaretPosition(msgArea.getDocument().getLength());
                
                if (showFirstTime)
                {
                    frame.setSize(800, 600);
                    UIHelper.centerAndShow(frame);
                    
                } else
                {
                    frame.setVisible(false);
                    
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run()
                        {
                            /*frame.setFocusable(true);
                            frame.setFocusableWindowState(true);
                            frame.requestFocus();
                            frame.requestFocusInWindow();
                            frame.setAlwaysOnTop(true);
                            frame.toFront();
                            frame.setAlwaysOnTop(false);*/
                            frame.setVisible(true);
                        }
                    });
                }
            } else
            {
                System.err.println(msg);
            }
        } else
        {
            //UIRegistry.showError(msg);
            System.err.println(msg);
        }
    }
    
    /**
     * @param key
     * @param args
     */
    public static void appendLocalizedFormDevError(final String key, final Object ... args)
    {
        appendFormDevError(String.format(UIRegistry.getResourceString(key), args));
    }
    
}
