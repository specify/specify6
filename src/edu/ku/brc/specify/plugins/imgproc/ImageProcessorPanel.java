package edu.ku.brc.specify.plugins.imgproc;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.Trayable;
import edu.ku.brc.util.Pair;


/* Copyright (C) 2013, University of Kansas Center for Research
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

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Sep 10, 2011
 *
 */
public class ImageProcessorPanel extends JPanel
{
    protected static final int PROC_ICON_SIZE = 120;
    protected static final int PROC_IMG_SIZE  = 93;
    protected static BasicStroke lineStroke   = new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    protected static Font        font         = null;

    protected BarCodeDecoder        barDecoder = new BarCodeDecoder();
    protected IconTray<Trayable>    iconTray;
    
    protected Timer                 timer = null;
    protected String[]              imgNames = {"png", "tif", "bmp", "gif", "jpg"};
    protected int                   imgIndex = 0;
    protected ImageIcon             circle;
    protected ImageProcListener     listener;
    protected Vector<TrayImageIcon> trayItems = new Vector<TrayImageIcon>();
    protected String                srcDir = "/Users/rods/Pictures/Eye-Fi/Pics";
    
    protected int                   step          = 0;
    protected int                   numPics       = 3;
    protected int                   barcodePicInx = 0;
    
    protected String[]    procImgNames = {"camera", "barcode", "metadata", "disk"};
    
    protected ArrayList<ProcessObject> procObjs = new ArrayList<ImageProcessorPanel.ProcessObject>();

    protected static RenderingHints hints;
    
    
    protected ArrayList<File> filesToProcess = new ArrayList<File>();
    
    static
    {
        hints = new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }

    /**
     * @param iconTray
     * @param listener
     */
    public ImageProcessorPanel(final IconTray<Trayable> iconTray, 
                               final ImageProcListener listener)
    {
        super(null);
        this.iconTray = iconTray;
        this.listener = listener;
    }
    
    /**
     * 
     */
    public void createUI()
    {
        Dimension s = new Dimension(350, 350);
        setSize(s);
        setPreferredSize(s);
        
        setBackground(Color.WHITE);
        for (String imgNm : procImgNames)
        {
            procObjs.add(new ProcessObject(imgNm));
        }

        circle = IconManager.getIcon("ip_circle");
        
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                doProcLayout();
                
                step = -1;
                
                doNextStep();
            }
        });
    }
    
    /**
     * 
     */
    protected void doProcLayout()
    {
        Dimension size = getSize();
        int margin = 20;
        
        Point p = new Point(margin, margin);
        for (int i=0;i<4;i++)
        {
            switch (i)
            {
                case 0:
                    p.setLocation(margin, margin*2);
                    break;
                case 1:
                    p.setLocation(size.width-PROC_ICON_SIZE-margin, margin*2);
                    break;
                case 2:
                    p.setLocation(size.width-PROC_ICON_SIZE-margin, size.height-PROC_ICON_SIZE-margin);
                    break;
                case 3:
                    p.setLocation(margin, size.height-PROC_ICON_SIZE-margin);
                    break;
            }
            add(procObjs.get(i));
            procObjs.get(i).setLocation(p);
            procObjs.get(i).setVisible(true);
        }
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#paint(java.awt.Graphics)
     */
    @Override
    public void paint(Graphics g)
    {
        ((Graphics2D)g).addRenderingHints(hints);
        
        super.paint(g);
       
        if (font == null)
        {
            font = (new JLabel()).getFont();
            font = font.deriveFont(18.0f);
            font = font.deriveFont(Font.BOLD);
        }
        String title = "Specify Image Processor";
        g.setColor(new Color(32, 131, 155));
        g.setFont(font);
        int w = g.getFontMetrics().stringWidth(title);
        int y = g.getFontMetrics().getHeight()-5;
        int x = (getSize().width - w) / 2;
        
        g.drawString(title, x, y);
    }
    
    /**
     * 
     */
    protected void startWatching()
    {
        timer = new Timer(1500, new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                acquireImages();
            }
        });
        timer.start();
    }
    
    /**
     * 
     */
    public void clearFiles()
    {
        File dir = new File(srcDir);
        
        if (dir != null && dir.exists())
        {
            for (File f : dir.listFiles())
            {
                String ext = FilenameUtils.getExtension(f.getName());
                if (!f.getName().startsWith(".") && ext != null && 
                    (ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("png")))
                {
                    f.delete();
                }
            }
        }
    }
    
    /**
     * 
     */
    protected void doNextStep()
    {
        if (step > -1)
        {
            procObjs.get(step).setOn(false);
        }
        
        step++;
        if (step >= procObjs.size())
        {
            step = 0;
        }
        
        System.out.println("Step: "+step);
        
        procObjs.get(step).setOn(true);
        
        SwingWorker<Integer, Integer> worker = new SwingWorker<Integer, Integer>()
        {
            /* (non-Javadoc)
             * @see javax.swing.SwingWorker#doInBackground()
             */
            @Override
            protected Integer doInBackground() throws Exception
            {
                switch (step)
                {
                    case 0:
                        acquireImages();
                        break;
                        
                    case 1:
                        processBarCode();
                        break;
                        
                    case 2:
                        processMetaData();
                        break;
                        
                    case 3:
                        processSaveToDisk();
                        break;
                        
                }
                return null;
            }
        };
        worker.execute();
    }
    
    /**
     * 
     */
    protected void advance()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                ImageProcessorPanel.this.doNextStep();
            }
        });

    }
    
    /**
     * 
     */
    protected void acquireImages()
    {
        if (timer != null)
        {
            timer.stop();
        }
        
        File dir = new File(srcDir);
        
        String nms[] = dir.list();
        int cnt = 0;
        if (nms.length > 0)
        {
            for (String s : nms)
            {
                if (!s.startsWith("."))
                {
                    cnt++;
                }
            }
        }
        //System.out.println(dir.getAbsolutePath()+":  "+cnt);
        
        if (cnt == numPics)
        {
            filesToProcess.clear();
            trayItems.clear();
            iconTray.removeAllItems();
            
            File[] files = dir.listFiles();
            for (File f : files) 
            {
                if (!f.getName().startsWith("."))
                {
                    filesToProcess.add(f);
                    System.out.println("Add: "+f.getName());
                }
            }
            
            Comparator<File> fileComp = new Comparator<File>()
            {
                @Override
                public int compare(File o1, File o2)
                {
                    return ((Long)o1.lastModified()).compareTo(o2.lastModified());
                }
                
            };
            Collections.sort(filesToProcess, fileComp);
            
            iconTray.removeAllItems();
            
            for (File f : filesToProcess) 
            {
                System.out.println(f.getName());
                TrayImageIcon trayItem = new TrayImageIcon(f);
                iconTray.addItem(trayItem);
                trayItems.add(trayItem);
            }
            iconTray.repaint();
            
            advance();
            
        } else
        {
            startWatching();
        }
    }
    
    /**
     * 
     */
    public void readSetupPrefs()
    {
        AppPreferences locPrefs = AppPreferences.getLocalPrefs();
        numPics       = locPrefs.getInt("IMGWRKFLW.PIC_CNT", 3);
        barcodePicInx = locPrefs.getInt("IMGWRKFLW.PIC_INX", 1);
        srcDir        = locPrefs.get("IMGWRKFLW.PIC_DEST", "/Users/rods/Pictures/Eye-Fi/Pics");
    }

    
    /**
     * 
     */
    protected void processBarCode()
    {
        listener.statusMsg("Reading BarCode from image...");
        
        File file = filesToProcess.get(barcodePicInx);
        System.out.println("BarCode: "+file.getName());
        try
        {
            barDecoder.decode(file);
            String number = barDecoder.getNumber();
            if (StringUtils.isNotEmpty(number))
            {
                listener.statusMsg("Barcode: "+number);
                
                ArrayList<File> imgFiles = new ArrayList<File>();
                
                System.out.println(trayItems.size());
                int cnt = 1;
                for (TrayImageIcon trayItem : trayItems)
                {
                    String ext     = FilenameUtils.getExtension(trayItem.getFile().getName());
                    String nm      = String.format("processedimages/%s_%d.%s", number, cnt, ext);
                    File   newFile = new File(nm);
                    
                    try
                    {
                        FileUtils.copyFile(trayItem.getFile(), newFile);
                        imgFiles.add(newFile);
                        
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    
                    trayItem.setFile(newFile);
                    cnt++;
                }
                
                try
                {
                    listener.complete(ImageProcListener.ActionType.eBarDecoding, new Pair<String, ArrayList<File>>(number, imgFiles));
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }

            } else
            {
                listener.statusMsg("Error reading BarCode from image...");
            }
            iconTray.repaint();

        } catch (IOException e)
        {
            listener.statusMsg("Barcode Error!");
            e.printStackTrace();
        }
        
        advance();
    }

    /**
     * 
     */
    protected void processMetaData()
    {
        listener.statusMsg("Setting metadata...");

        //for (File f : filesToProcess) System.out.println("MetaData: "+f.getName());
        try
        {
            Thread.sleep(500);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        advance();
    }

    /**
     * 
     */
    protected void processSaveToDisk()
    {
        listener.statusMsg("Saving to disk...");
        //for (File f : filesToProcess) System.out.println("Save: "+f.getName());
        try
        {
            Thread.sleep(500);
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        listener.statusMsg(" ");
        for (File f : filesToProcess) f.delete();
        filesToProcess.clear();
        iconTray.removeAllItems();
        iconTray.repaint();
        
        advance();
    }

    
    //-------------------------------------------------------------------------------
    class ProcessObject extends JPanel
    {
        protected Point     imgPnt;
        protected ImageIcon img;
        protected boolean   isOn;
        
        /**
         * @param nm
         * @param x
         * @param y
         */
        public ProcessObject(final String nm)
        {
            setSize(500,500);
            setBorder(BorderFactory.createLineBorder(Color.BLACK));
            
            img = IconManager.getIcon(String.format("ip_%s", nm));
            
            setSize(PROC_ICON_SIZE, PROC_ICON_SIZE);
            setOpaque(false);
            setVisible(false);
            
            int offsetX = (PROC_ICON_SIZE - img.getIconWidth()) / 2;
            int offsetY = (PROC_ICON_SIZE - img.getIconHeight()) / 2;
            imgPnt = new Point(offsetX, offsetY);
        }
        
        /* (non-Javadoc)
         * @see javax.swing.JComponent#paint(java.awt.Graphics)
         */
        public void paint(final Graphics g)
        {
            Graphics2D g2d = (Graphics2D)g;
            
            //g2d.setColor(Color.BLACK);
            //g.drawRect(pnt.x, pnt.y, img.getIconWidth(), img.getIconHeight());
            
            
            g.drawImage(img.getImage(), imgPnt.x, imgPnt.y, null);
            
            if (isOn)
            {
                //int circleOffset = (circle.getIconWidth() - img.getIconWidth()) / 2;
                g.drawImage(circle.getImage(), 0, 0, null);
            }
            //int offset = img.getIconHeight() / 2;
            //g2d.setStroke(lineStroke);
            //g2d.setColor(Color.BLUE);
            //g2d.drawRect(0, 0, circle.getIconWidth()-1, circle.getIconHeight()-1);
            //GraphicsUtils.drawCircle(g, pnt.x+offset, pnt.y+offset, offset*2);
        }

        /**
         * @return the isOn
         */
        public boolean isOn()
        {
            return isOn;
        }

        /**
         * @param isOn the isOn to set
         */
        public void setOn(boolean isOn)
        {
            this.isOn = isOn;
            repaint();
        }
        
    }

}
