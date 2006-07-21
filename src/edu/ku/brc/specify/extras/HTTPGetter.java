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

import java.io.IOException;
import java.net.ConnectException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

/**
 *
 * @author rods
 *
 */
public class HTTPGetter implements Runnable
{
    private static final Logger log = Logger.getLogger(HTTPGetter.class);

    public enum ErrorCode {
        NoError, Error, HttpError, NotDoneError, IOError, URLError
    }

    protected ErrorCode status = ErrorCode.NoError;
    protected Thread    thread = null;
    protected String    urlStr;


    public HTTPGetter()
    {
        this.urlStr = null;
    }

    public HTTPGetter(final String urlStr, final boolean cacheFile)
    {
        this.urlStr = urlStr;
    }

    public String getUrlStr()
    {
        return urlStr;
    }

    public void setUrlStr(String urlStr)
    {
        this.urlStr = urlStr;
    }

    public ErrorCode getStatus()
    {
        return status;
    }

    /**
     * Performs a "generic" HTTP request and fill member variable with results
     * use "getDigirResultsetStr" to get the results as a String
     *
     * @param url URL to be executaed
     * @return returns an error code
     */
    public byte[] doHTTPRequest(final String url)
    {
        byte[] bytes = null;
        status = ErrorCode.NoError;

        // Create an HttpClient with the MultiThreadedHttpConnectionManager.
        // This connection manager must be used if more than one thread will
        // be using the HttpClient.
        HttpClient httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());

        GetMethod method = new GetMethod(url);
        try
        {

            log.debug("getting " + method.getURI());
            // execute the method
            httpClient.executeMethod(method);

            System.out.println("Get executed");
            // get the response body as an array of bytes
            bytes = method.getResponseBody();

            log.debug(bytes.length + " bytes read");

        } catch (ConnectException ce)
        {
            log.error("Could not make HTTP connection.  ("
                    + ce.toString() + ")");
            status = ErrorCode.HttpError;

        } catch (HttpException he)
        {
            log.error("Http problem making request.  ("
                    + he.toString() + ")");
            status = ErrorCode.HttpError;

        } catch (IOException ioe)
        {
            log.error("IO problem making request.  (" + ioe.toString()
                    + ")");
            status = ErrorCode.IOError;

        } catch (Exception e)
        {
            log.error("Error: " + e);
            status = ErrorCode.Error;

        } finally
        {
            // always release the connection after we're done
            method.releaseConnection();
            log.debug("Connection released");
        }
        return bytes;
    }

    // ----------------------------------------------------------------
    // -- Runnable Interface
    // ----------------------------------------------------------------
    public void start()
    {
        if (thread == null)
        {
            thread = new Thread(this);
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
        }
    }

    /**
     *
     *
     */
    public synchronized void stop()
    {
        if (thread != null)
        {
            thread.interrupt();
        }
        thread = null;
        notifyAll();
    }

    /**
     *
     */
    public void run()
    {
        //Thread me = Thread.currentThread();

        doHTTPRequest(urlStr);

        stop();
    }
}
