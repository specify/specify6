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

package edu.ku.brc.specify.extras;


import java.awt.Image;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import javax.imageio.stream.FileImageOutputStream;
import javax.swing.ImageIcon;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import edu.ku.brc.helpers.HTTPGetter;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.ui.UICacheManager;

/**
 * @author rods
 *
 */
public class FishBaseInfoGetter extends HTTPGetter
{
    public enum InfoType {Summary, Image, Thumbnail, PictureList}

    protected FishBaseInfoGetterListener consumer;

    protected InfoType  type;
    protected String    genus;
    protected String    species;
    protected Image     image    = null;
    protected String    imageURL = null;

    protected String    tmpDir   = null;
    protected Element   dom      = null;
    protected String    data     = null;


  public FishBaseInfoGetter(final FishBaseInfoGetterListener consumer,
                            final InfoType type,
                            final String   genus,
                            final String   species)
    {
        this.consumer = consumer;
        this.type     = type;
        this.genus    = genus;
        this.species  = species;

        //tmpDir = System.getProperty("java.io.tmpdir");
        // XXX Will use long term cache in future
        tmpDir = UICacheManager.getInstance().getDefaultWorkingPath() + File.separator + "cache";
        File path = new File(tmpDir);
        if (!path.exists())
        {
            if (!path.mkdirs())
            {
                String msg = "unable to create directory [" + path.getAbsolutePath() + "]";
                System.err.println(msg);
                throw new RuntimeException(msg);
            }
        }
    }

    public Element getDom()
    {
        return dom;
    }

    public Image getImage()
    {
        return image;
    }

    public void setConsumer(FishBaseInfoGetterListener consumer)
    {
		this.consumer = consumer;
	}

	public String getImageURL()
    {
        return imageURL;
    }

    /**
     * Performs a "generic" HTTP request and fill member variable with results use
     * "getDigirResultsetStr" to get the results as a String
     *
     */
    public Image getImage(final String fileName, final String url)
    {
        imageURL = url;

        String fullPath = tmpDir + File.separator + fileName;
        System.out.println(fullPath);
        File file = new File(fullPath);
        if (file.exists())
        {
            try
            {
                //imageURL = file.toURI().toString();

                ImageIcon image = new ImageIcon(fullPath);
                return image.getImage();

            } catch (Exception ex)
            {
                ex.printStackTrace();
                status = ErrorCode.Error;
            }

        } else
        {
            byte[] bytes = super.doHTTPRequest(url);

            try
            {
                FileImageOutputStream fos = new FileImageOutputStream(file);
                fos.write(bytes);
                fos.flush();
                fos.close();

                ImageIcon image = new ImageIcon(bytes);
                return image.getImage();

            } catch (Exception ex)
            {
                ex.printStackTrace();
                status = ErrorCode.Error;
            }
        }
        return null;
    }

    /**
     * Performs a "generic" HTTP request and fill member variable with results use
     * "getDigirResultsetStr" to get the results as a String
     *
     */
    public void getDOMDoc(final String url, final InfoType infoType)
    {
        dom = null;
        String fileName = tmpDir + File.separator + genus + "_" + species + "_" + infoType.toString() + ".xml";
        System.out.println(fileName);
        File file = new File(fileName);
        if (file.exists())
        {
            try
            {
                dom = XMLHelper.readFileToDOM4J(file);

            } catch (Exception ex)
            {
                ex.printStackTrace();
                status = ErrorCode.Error;
            }

        } else
        {
            //System.out.println("http://www.fishbase.org.ph/webservice/Species/SpeciesSummary.asp?Genus=Etheostoma&Species=ramseyi");
            System.out.println(url);
            byte[] bytes = super.doHTTPRequest(url);

            data = new String(bytes);
            int inx = data.indexOf("<?");
            if (inx > -1)
            {
                data = data.substring(inx, data.length());

                System.out.println(data);
                try
                {
                    Writer output = new BufferedWriter(new FileWriter(file));
                    output.write(data);
                    output.flush();
                    output.close();

                    // Is is cheating and slow, but I will do it for now
                    // XXX FIXME!
                    dom = XMLHelper.readFileToDOM4J(file);

                } catch (Exception ex)
                {
                    ex.printStackTrace();
                    status = ErrorCode.Error;
                }

            } else
            {
                status = ErrorCode.Error;
                data = null;
            }

        }
    }

    public void run()
    {
        if (type == InfoType.Summary)
        {
            urlStr = "http://www.fishbase.org.ph/webservice/Species/SpeciesSummary.asp?Genus="+genus+"&Species="+species;
            getDOMDoc(urlStr, type);

        } else if (type == InfoType.Thumbnail || type == InfoType.Image)
        {
            urlStr = "http://www.fishbase.org/webservice/Photos/FishPicsList.php?Genus=" + genus + "&Species=" + species;
            getDOMDoc(urlStr, InfoType.PictureList);

            if (status == ErrorCode.NoError)
            {
                Element species = (Element)dom.selectSingleNode("/fishbase/species");
                if (species != null)
                {
                    int numPictures = XMLHelper.getAttr(species, "total_adult", 0);
                    if (numPictures > 0)
                    {
                        List adults = dom.selectNodes("/fishbase/pictures[@type='adult']");
                        for ( Iterator iter = adults.iterator(); iter.hasNext(); )
                        {
                            Element pictureElement = (Element) iter.next();
                            Element nameElement = (Element)pictureElement.selectSingleNode(type == InfoType.Thumbnail ? "thumbnail" : "actual");
                            if (nameElement != null)
                            {
                                String fileURL = nameElement.getTextTrim();
                                System.out.println(fileURL);
                                if (StringUtils.isNotEmpty(fileURL))
                                {
                                    String shortName = "";
                                    int inx = fileURL.lastIndexOf("/");
                                    if (inx > -1)
                                    {
                                        shortName = fileURL.substring(inx+1, fileURL.length());
                                    }
                                    System.out.println("*["+shortName+"]["+fileURL+"]");
                                    image = getImage(shortName, fileURL);
                                    if (image != null)
                                    {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }

        if (consumer != null)
        {
            if (status == ErrorCode.NoError)
            {
                consumer.infoArrived(this);

            } else
            {
                consumer.infoGetWasInError(this);
            }
        }
        stop();
    }

}
