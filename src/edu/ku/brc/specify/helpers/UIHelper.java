package edu.ku.brc.specify.helpers;

import static edu.ku.brc.specify.ui.UICacheManager.getResourceString;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.awt.event.KeyEvent;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.ku.brc.specify.ui.dnd.GhostDataAggregatable;
import edu.ku.brc.specify.ui.*;

public final class UIHelper
{
    public enum OSTYPE {Unknown, Windows, MacOSX, Linux};

    // Static Data Members
    protected static Log      log      = LogFactory.getLog(UIHelper.class);
    protected static Calendar calendar = new GregorianCalendar();
    protected static OSTYPE   oSType;

    static {

        String osStr = System.getProperty("os.name");
        if (osStr.equals("Mac OS X"))
        {
            oSType   = OSTYPE.MacOSX;

        } else if (osStr.indexOf("Windows") != -1)
        {
            oSType   = OSTYPE.Windows;

        } else if (osStr.indexOf("Linux") != -1)
        {
            oSType   = OSTYPE.Linux;

        } else
        {
            oSType   = OSTYPE.Unknown;
        }
    }

    /**
     * Center and make the window visible
     * @param window the window to center
     */
    public static void centerAndShow(java.awt.Window window)
    {
        Rectangle screenRect = window.getGraphicsConfiguration().getBounds();
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(window.getGraphicsConfiguration());

        // Make sure we don't place the demo off the screen.
        int centerWidth = screenRect.width < window.getSize().width ? screenRect.x : screenRect.x
            + screenRect.width / 2 - window.getSize().width / 2;
        int centerHeight = screenRect.height < window.getSize().height ? screenRect.y : screenRect.y
            + screenRect.height / 2 - window.getSize().height / 2;

        centerHeight = centerHeight < screenInsets.top ? screenInsets.top : centerHeight;

        window.setLocation(centerWidth, centerHeight);

        window.setVisible(true);
    }

    /**
     * Center and make the window visible
     * @param window the window to center
     */
    public static void positionAndShow(java.awt.Window window)
    {
        if (System.getProperty("os.name").equals("Mac OS X"))
        {
            window.setLocation(0, 0);
            window.setVisible(true);

        } else
        {
            centerAndShow(window);
        }

    }

    /**
     * Returns a JGoodies column or row definition string that has 'length' number of duplicated formats
     * @param def the col/row def to be duplicated
     * @param length the number of duplications
     * @return Returns a JGoodies column or row definition string that has 'length' number of duplicated formats
     */
    public static String createDuplicateJGoodiesDef(final String def, final String separator, final int length)
    {
        StringBuffer strBuf = new StringBuffer();
        for (int i=0;i<length;i++)
        {
            if (strBuf.length() > 0)
            {
                strBuf.append(',');
            }
            strBuf.append(def);

            if (i < (length-1))
            {
                strBuf.append(',');
                strBuf.append(separator);
            }
        }
        return strBuf.toString();
    }

    /**
     * Converts an integer time in the form of YYYYMMDD to the proper Date
     * @param iDate the int to be converted
     * @return the date object
     */
    public static Date convertIntToDate(final int iDate)
    {
        calendar.clear();

        int year  = iDate / 10000;
        if (year > 1800)
        {
            int tmp   = (iDate - (year * 10000));
            int month = tmp / 100;
            int day   = (tmp - (month * 100));

            calendar.set(year, month-1, day);
        } else
        {
            calendar.setTimeInMillis(0);
        }

        return calendar.getTime();
    }

    /**
     * Converts a Date to an Integer formated as YYYYMMDD
     * @param date the date to be converted
     * @return the date object
     */
    public static int convertDateToInt(final Date date)
    {
        calendar.setTimeInMillis(date.getTime());

        return (calendar.get(Calendar.YEAR) * 10000) + ((calendar.get(Calendar.MONTH)+1) * 100) + calendar.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * Converts Object to a Float
     * @param valObj the object in question
     * @return a float
     */
    public static float getFloat(Object valObj)
    {
        float value = 0.0f;
        if (valObj != null)
        {
            if (valObj instanceof Integer)
            {
                value = ((Integer)valObj).floatValue();
            } else if (valObj instanceof Long)
            {
                value = ((Long)valObj).floatValue();
            } else if (valObj instanceof Float)
            {
                value = ((Float)valObj).floatValue();
            } else if (valObj instanceof Double)
            {
                value = ((Double)valObj).floatValue();
            } else
            {
                System.out.println("getFloat - Class type is "+valObj.getClass().getName());
            }
        } else
        {
            log.error("getFloat - Result Object is null for["+valObj+"]");
        }
        return value;
    }

    public static double getDouble(Object valObj)
    {
        double value = 0.0;
        if (valObj != null)
        {
            if (valObj instanceof Integer)
            {
                value = ((Integer)valObj).doubleValue();
            } else if (valObj instanceof Long)
            {
                value = ((Long)valObj).doubleValue();
            } else if (valObj instanceof Float)
            {
                value = ((Float)valObj).doubleValue();
            } else if (valObj instanceof Double)
            {
                value = ((Double)valObj).doubleValue();
            } else
            {
                System.out.println("getDouble - Class type is "+valObj.getClass().getName());
            }
        } else
        {
            log.error("getDouble - Result Object is null for["+valObj+"]");
        }
        return value;
    }

    public static int getInt(Object valObj)
    {
        int value = 0;
        if (valObj != null)
        {
            if (valObj instanceof Integer)
            {
                value = ((Integer)valObj).intValue();
            } else if (valObj instanceof Long)
            {
                value = ((Long)valObj).intValue();
            } else if (valObj instanceof Float)
            {
                value = ((Float)valObj).intValue();
            } else if (valObj instanceof Double)
            {
                value = ((Double)valObj).intValue();
            } else
            {
                System.out.println("getInt - Class type is "+valObj.getClass().getName());
            }
        } else
        {
            log.error("getInt - Result Object is null for["+valObj+"]");
        }
        return value;
    }

    public static String getString(Object valObj)
    {
        if (valObj != null)
        {
            if (valObj instanceof String)
            {
                return (String)valObj;
            } else
            {
                System.out.println("getString - Class type is "+valObj.getClass().getName()+" should be String");
            }
        } else
        {
            log.error("getString - Result Object is null for["+valObj+"] in getString");
        }
        return "";
   }

    public static boolean getBoolean(Object valObj)
    {
        if (valObj != null)
        {
            if (valObj instanceof String)
            {
                String valStr = ((String)valObj).toLowerCase();
                if (valStr.equals("true"))
                {
                    return true;
                } else if (valStr.equals("false"))
                {
                    return false;
                } else
                {
                    System.out.println("getBoolean - value is not 'true' or 'false'");
                }
            } else
            {
                System.out.println("getBoolean - Class type is "+valObj.getClass().getName()+" should be String");
            }
        } else
        {
            log.error("getBoolean - Result Object is null for["+valObj+"]");
        }
        return false;
   }


     /**
      * Helper method for returning data if it is of a particular Class.
      * Meaning is this data implementing an interface or is it derived from some other class.
     * @param data the generic data
     * @param classObj the class in questions
     * @return the data if it is derived from or implements, can it be cast to
     */
    public static Object getDataForClass(final Object data, Class classObj)
    {
        // Short circut if all they are interested in is the generic "Object"
        if (classObj == Object.class)
        {
            return data;
        }

        // Check to see if it supports the aggrgation interface
        if (data instanceof GhostDataAggregatable)
        {
            Object newData = ((GhostDataAggregatable)data).getDataForClass(classObj);
            if (newData != null)
            {
                return newData;
            }
        }

        Vector<Class> classes = new Vector<Class>();

        // First Check interfaces
        Class[] theInterfaces = data.getClass().getInterfaces();
        for (Class co : theInterfaces)
        {
            classes.add(co);
        }

        if (classes.contains(classObj))
        {
            return data;
        }
        classes.clear();

        // Now Check super classes
        Class superclass = data.getClass().getSuperclass();
        while (superclass != null)
        {
            classes.addElement(superclass);
            superclass = superclass.getSuperclass();
        }

        // Wow, it doesn't support anything
        return null;
    }

    /**
     * Returns the type of the current OS
     * @return the type of the current OS
     */
    public static OSTYPE getOSType()
    {
        return oSType;
    }

    //-----------------------------------------------------------------------------------------
    // Menu Helpers
    //-----------------------------------------------------------------------------------------

    protected static JMenuItem createMenu(final String resKey, int virtualKeyCode, String mneu)
    {
        JMenuItem jmi = new JMenuItem(getResourceString(resKey));
        if (oSType != OSTYPE.MacOSX)
        {
            jmi.setMnemonic(mneu.charAt(0));
        }
        jmi.setAccelerator(KeyStroke.getKeyStroke(virtualKeyCode, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        return jmi;
    }

    /**
     * @param app
     * @param isPlatformSpecific
     * @param includeCutCopyPaste
     * @return
     */
    public static JMenuBar getBasicMenuBar(final AppIFace app, final boolean isPlatformSpecific, final boolean includeCutCopyPaste)
    {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = createMenu(menuBar, getResourceString("FileMenu"), getResourceString("FileMneu"));

        if (oSType != OSTYPE.MacOSX)
        {
            fileMenu.addSeparator();
            fileMenu.add(createMenu(getResourceString("ExitMenu"), getResourceString("ExitAccl").charAt(0), getResourceString("ExitMneu")));
        } else
        {
            new MacOSAppHandler(app);
        }

        if (includeCutCopyPaste)
        {
            JMenu editMenu = createMenu(menuBar, getResourceString("EditMenu"), getResourceString("EditMneu"));
            editMenu.add(createMenu(getResourceString("CutMenu"), getResourceString("CutAccl").charAt(0), getResourceString("CutMneu")));
            editMenu.add(createMenu(getResourceString("CopyMenu"), KeyEvent.VK_C, getResourceString("CopyMneu")));
            editMenu.add(createMenu(getResourceString("PasteMenu"), KeyEvent.VK_V, getResourceString("PasteMneu")));
        }



        return menuBar;
    }

    /**
     * @param menu xxxxx
     * @param label xxxxx
     * @param mnemonic xxxxx
     * @param accessibleDescription xxxxx
     * @param enabled xxxxx
     * @param action xxxxx
     * @return xxxxx
     */
    public static JMenuItem createMenuItem(final JMenu          menu,
                                           final String         label,
                                           final String         mnemonic,
                                           final String         accessibleDescription,
                                           final boolean        enabled,
                                           final AbstractAction action)
    {
        JMenuItem mi = new JMenuItem(label);
        if (menu != null)
        {
            menu.add(mi);
        }
        if (isNotEmpty(mnemonic))
        {
            mi.setMnemonic(mnemonic.charAt(0));
        }
        mi.getAccessibleContext().setAccessibleDescription(accessibleDescription);
        mi.addActionListener(action);
        if (action != null)
        {
            action.addPropertyChangeListener(new MenuItemPropertyChangeListener(mi));
            action.setEnabled(enabled);
        }

        return mi;
    }

    /**
     * Create a menu
     * @param menuBar the menubar
     * @param labelKey the label key to be localized
     * @param mneuKey the mneu key to be localized
     * @return returns a menu
     */
    public static JMenu createMenu(final JMenuBar menuBar, final String labelKey, final String mneuKey)
    {
        JMenu menu = null;
        try
        {
            menu = menuBar.add(new JMenu(getResourceString(labelKey)));
            if (oSType != OSTYPE.MacOSX)
            {
                menu.setMnemonic(getResourceString(mneuKey).charAt(0));
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
            log.info("Couldn't create menu for " + labelKey + "  " + mneuKey);
        }
        return menu;
    }

}
