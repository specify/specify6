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
package edu.ku.brc.ui;

import static edu.ku.brc.ui.UICacheManager.getResourceString;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.helpers.MenuItemPropertyChangeListener;
import edu.ku.brc.ui.db.DatabaseLoginDlg;
import edu.ku.brc.ui.db.DatabaseLoginListener;
import edu.ku.brc.ui.db.DatabaseLoginPanel;
import edu.ku.brc.ui.dnd.GhostDataAggregatable;
import edu.ku.brc.ui.forms.DataObjectGettable;
import edu.ku.brc.ui.forms.persist.FormCell;

/**
 * A Helper class that has a very wide array of misc methods for helping out. (Is that meaningless or what?)
 *
 * @code_status Code Freeze
 *
 * @author rods
 *
 */
public final class UIHelper
{
    public enum OSTYPE {Unknown, Windows, MacOSX, Linux}

    // Static Data Members
    protected static final Logger   log      = Logger.getLogger(UIHelper.class);
    protected static Calendar calendar = new GregorianCalendar();
    protected static OSTYPE   oSType;

    protected static Object[]          values   = new Object[2];
    protected static SimpleDateFormat  scrDateFormat = null;

    static {

        String osStr = System.getProperty("os.name");
        if (osStr.startsWith("Mac OS X"))
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
        StringBuilder strBuf = new StringBuilder(64);
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
                log.error("getFloat - Class type is "+valObj.getClass().getName());
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
                log.error("getDouble - Class type is "+valObj.getClass().getName());
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
                log.error("getInt - Class type is "+valObj.getClass().getName());
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
                log.error("getString - Class type is "+valObj.getClass().getName()+" should be String");
            }
        } else
        {
            log.error("getString - Result Object is null for["+valObj+"] in getString");
        }
        return "";
   }
    
    /**
     * Converts a String to the class that is passed in.
     * @param dataStr the data string to be converted
     * @param cls the class that the string is to be converted t
     * @return the data object
     */
    public static Object convertDataFromString(final String dataStr, final Class cls)
    {
        if (cls == Integer.class)
        {
            return Integer.parseInt(dataStr);
            
        } else if (cls == Float.class)
        {
            return Float.parseFloat(dataStr);
            
        } else if (cls == Double.class)
        {
            return Double.parseDouble(dataStr);
            
        } else if (cls == Long.class)
        {
            return Long.parseLong(dataStr);
            
        } else if (cls == Short.class)
        {
            return Short.parseShort(dataStr);
            
        } else if (cls == Byte.class)
        {
            return Byte.parseByte(dataStr);
            
        } else
        {
            throw new RuntimeException("Unsupported type for conversion["+cls.getSimpleName()+"]");
        }
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
                    log.error("getBoolean - value is not 'true' or 'false'");
                }
            } else
            {
                log.error("getBoolean - Class type is "+valObj.getClass().getName()+" should be String");
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
     * Returns the type of the current OS.
     * @return the type of the current OS
     */
    public static OSTYPE getOSType()
    {
        return oSType;
    }

    //-----------------------------------------------------------------------------------------
    // Menu Helpers
    //-----------------------------------------------------------------------------------------

    /**
     * @param resKey
     * @param virtualKeyCode
     * @param mneu
     * @return
     */
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
     * @return the menubar
     */
    public static JMenuBar getBasicMenuBar(final Object app, final boolean isPlatformSpecific, final boolean includeCutCopyPaste)
    {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = createMenu(menuBar, getResourceString("FileMenu"), getResourceString("FileMneu"));

        if (oSType != OSTYPE.MacOSX)
        {
            fileMenu.addSeparator();
            fileMenu.add(createMenu(getResourceString("ExitMenu"), getResourceString("ExitAccl").charAt(0), getResourceString("ExitMneu")));
        } else
        {
            //new MacOSAppHandler((AppIFace)app);
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
            log.error("Couldn't create menu for " + labelKey + "  " + mneuKey);
        }
        return menu;
    }

    //-----------------------------------------------------------------------------------------
    // DataObject and Data Access Helpers
    //-----------------------------------------------------------------------------------------


    /**
     * Returna an array of values given a FormCell definition. Note: The returned array is owned by the utility and
     * may be longer than the number of fields defined in the CellForm object. Any additional "slots" in the array that are used
     * are set to null;
     * @param fieldNames the array of field name to be filled ( the array is really the path to the object)
     * @param dataObj the dataObj from which to get the data from
     * @param getter the DataObjectGettable to use to get the data
     * @return an array of values at least as long as the fielName list, but may be longer
     */
    public static Object[] getFieldValues(final String[] fieldNames,
                                          final Object dataObj,
                                          final DataObjectGettable getter)
    {
        if (scrDateFormat == null)
        {
            scrDateFormat = AppPrefsCache.getSimpleDateFormat("ui", "formatting", "scrdateformat");
        }

        if (fieldNames.length > values.length)
        {
            values = new Object[fieldNames.length];
        } else
        {
            for (int i=fieldNames.length;i<values.length;i++)
            {
                values[i] = null;
            }
        }

        boolean  allFieldsNull = true;

        int cnt = 0;
        for (String fldName : fieldNames)
        {
            Object dataValue;
            if (getter.usesDotNotation())
            {
                int inx = fldName.indexOf(".");
                if (inx > -1)
                {
                    StringTokenizer st = new StringTokenizer(fldName, ".");
                    Object data = dataObj;
                    while (data != null && st.hasMoreTokens())
                    {
                        data = getter.getFieldValue(data, st.nextToken());
                    }
                    dataValue = data == null ? "" : data;
                } else
                {
                    dataValue = getter.getFieldValue(dataObj, fldName);
                }
            } else
            {
                dataValue = getter.getFieldValue(dataObj, fldName);
            }

            if (dataValue instanceof java.util.Date)
            {
                dataValue = scrDateFormat.format((java.util.Date)dataValue);

            } else if (dataValue instanceof java.util.Calendar)
            {
                dataValue = scrDateFormat.format(((java.util.Calendar)dataValue).getTime());
            }

            if (allFieldsNull && dataValue != null)
            {
                allFieldsNull = false;
            }
            values[cnt++] = dataValue;
        }

         return allFieldsNull ? null : values;
    }

    /**
     * Return an array of values given a FormCell definition. Note: The returned array is owned by the utility and
     * may be longer than the number of fields defined in the CellForm object. Any additional "slots" in the array that are used
     * are set to null;
     * @param formCell the defition of the field to get
     * @param dataObj the dataObj from which to get the data from
     * @param getter the DataObjectGettable to use to get the data
     * @return an array of values at least as long as the fielName list, but may be longer
     */
    public static Object[] getFieldValues(final FormCell formCell, final Object dataObj, final DataObjectGettable getter)
    {
        String[] fieldNames = formCell.getFieldNames();
        if( fieldNames != null && fieldNames.length != 0 )
        {
            return getFieldValues(fieldNames, dataObj, getter);
        }
        else
        {
        	return null;
        }
    }


    /**
     * Returns a single String value that formats all the value in the array per the format mask
     * @param valuesArg the array of values
     * @param format the format mask
     * @return a string with the formatted values
     */
    public static Object getFormattedValue(final Object[] valuesArg,
                                           final String format)
    {
        if (valuesArg == null)
        {
            return "";
        }

        try
        {
            Formatter formatter = new Formatter();
            formatter.format(format, valuesArg);
            return formatter.toString();

        } catch (java.util.IllegalFormatConversionException ex)
        {
            return valuesArg[0] != null ? valuesArg[0].toString() : "";
        }

    }

    //-------------------------------------------------------
    //-- Helpers for creating Lists and Maps
    //-------------------------------------------------------
    /**
     * @return a string map
     */
    public static Map<String, String> createMap()
    {
        return new Hashtable<String, String>();
    }

    /**
     * @return string list
     */
    public static List<String> createList()
    {
        return new ArrayList<String>();
    }

    //-------------------------------------------------------
    //-- Helpers for logging into the database
    //-------------------------------------------------------

    /**
     * Constructs the full connection string for JDBC
     * @param dbProtocol the protocol
     * @param dbServer the server name machine or IP address
     * @param dbName the name of the database
     * @return the full JDBC connection string
     */
    public static String constructJDBCConnectionString(final String dbProtocol,
                                                       final String dbServer,
                                                       final String dbName)
    {
        StringBuilder strBuf = new StringBuilder(64);
        strBuf.append("jdbc:");
        strBuf.append(dbProtocol);

        if (isNotEmpty(dbServer))
        {
            strBuf.append("://");
            strBuf.append(dbServer);
            strBuf.append("/");
            strBuf.append(dbName);

        } else
        {
            strBuf.append(":");
            strBuf.append(dbName);
        }
        return strBuf.toString();
    }

    /**
     * Tries to login using the supplied params
     * @param dbDriver the driver (a class name)
     * @param dbDialect the Hibernate Dialect class name
     * @param connectionStr the full JDBC connection string that includes the database name
     * @param dbName the name of the database
     * @param dbUsername the user name
     * @param dbPassword the password
     * @return true if logged in, false if not
     */
    public static boolean tryLogin(final String dbDriver,
                                   final String dbDialect,
                                   final String dbName,
                                   final String connectionStr,
                                   final String dbUsername,
                                   final String dbPassword)
    {
        HibernateUtil.setCurrentUserEditStr("");

        DBConnection dbConn = DBConnection.getInstance();

        dbConn.setDriver(dbDriver);
        dbConn.setDialect(dbDialect);
        dbConn.setDatabaseName(dbName);
        dbConn.setConnectionStr(connectionStr);
        dbConn.setUsernamePassword(dbUsername, dbPassword);

        Connection connection = dbConn.createConnection();
        if (connection != null)
        {
            try
            {
                connection.close();
                
                // This is used to fill who editted the object
                HibernateUtil.setCurrentUserEditStr(dbUsername);
                
            } catch (SQLException ex)
            {

            }
            return true;

        } else
        {
            return false;
        }
    }

    /**
     * Tries to do the login, if doAutoLogin is set to true it will try without displaying a dialog
     * and if the login fails then it will display the dialog
     * @param doAutoLogin whether to try to utomatically log the user in
     * @param doAutoClose hwther it should automatically close the window when it is logged in successfully
     * @param useDialog use a Dialog or a Frame
     * @param listener a listener for when it is logged in or fails
     */
    public static DatabaseLoginPanel doLogin(final boolean doAutoLogin,
                                             final boolean doAutoClose,
                                             final boolean useDialog,
                                             final DatabaseLoginListener listener)
    {
        boolean doAutoLoginNow = doAutoLogin && AppPreferences.getLocalPrefs().getBoolean("login.autologin", false);

        if (useDialog)
        {
            DatabaseLoginDlg dlg = new DatabaseLoginDlg(listener);
            dlg.setDoAutoLogin(doAutoLoginNow);
            dlg.setDoAutoClose(doAutoClose);
            UIHelper.centerAndShow(dlg);

            return dlg.getDatabaseLoginPanel();

        } else
        {
            class DBListener implements DatabaseLoginListener
            {
                protected JFrame                frame;
                protected DatabaseLoginListener frameDBListener;
                protected boolean               doAutoCloseOfListener;

                public DBListener(JFrame frame, DatabaseLoginListener frameDBListener, boolean doAutoCloseOfListener)
                {
                    this.frame                 = frame;
                    this.frameDBListener       = frameDBListener;
                    this.doAutoCloseOfListener = doAutoCloseOfListener;
                }
                
                public void loggedIn(final String databaseName, final String userName)
                {
                    if (doAutoCloseOfListener)
                    {
                        frame.setVisible(false);
                    }
                    frameDBListener.loggedIn(databaseName, userName);
                }

                public void cancelled()
                {
                    frame.setVisible(false);
                    frameDBListener.cancelled();
                }
            }
            JFrame.setDefaultLookAndFeelDecorated(false);

            JFrame frame = new JFrame(getResourceString("logintitle"));
            DatabaseLoginPanel panel = new DatabaseLoginPanel(new DBListener(frame, listener, doAutoClose), false);
            panel.setAutoClose(doAutoClose);
            panel.setWindow(frame);
            frame.setContentPane(panel);
            frame.setIconImage(IconManager.getIcon("AppIcon", IconManager.IconSize.Std16).getImage());
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

            frame.pack();

            if (doAutoLoginNow)
            {
                panel.doLogin();
            }
            UIHelper.centerAndShow(frame);

            return panel;
        }

    }

}
