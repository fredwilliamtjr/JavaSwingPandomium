/*
    Maven-JavaFX-Package-Example
    Copyright (C) 2017-2018 Luca Bognolo

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see <http://www.gnu.org/licenses/>.

 */
package br.com.fwtj.javafx.main;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Vector;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.commons.lang3.StringUtils;
import org.cef.CefSettings;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.*;
import org.cef.handler.CefDownloadHandler;
import org.cef.handler.CefLoadHandler;
import org.cef.handler.CefRequestHandlerAdapter;
import org.cef.handler.CefResourceHandler;
import org.cef.misc.BoolRef;
import org.cef.misc.StringRef;
import org.cef.network.*;
import org.panda_lang.pandomium.Pandomium;
import org.panda_lang.pandomium.settings.PandomiumSettings;
import org.panda_lang.pandomium.wrapper.PandomiumBrowser;
import org.panda_lang.pandomium.wrapper.PandomiumClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * Main application class.
 */
public class MainApplication {

    private static String pathArquivoRecebido = null;

    private static String humanReadableByteCount(long bytes) {
        int unit = 1024;
        if (bytes < unit)
            return bytes + " B";

        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = "" + ("kMGTPE").charAt(exp - 1);
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static void main(String[] args) {

        PandomiumSettings settings = PandomiumSettings.getDefaultSettingsBuilder().build();
        settings.getCefSettings().ignore_certificate_errors = true;
        settings.getCefSettings().cache_path = "cacheChrome";
        settings.getCefSettings().persist_session_cookies = true;
        settings.getCefSettings().command_line_args_disabled = false;
        settings.getCefSettings().log_file = "Chrome.log";
        settings.getCefSettings().log_severity = CefSettings.LogSeverity.LOGSEVERITY_DEFAULT;

        Pandomium pandomium = new Pandomium(settings);
        pandomium.initialize();

        PandomiumClient client = pandomium.createClient();
//        client.getCefClient().addDownloadHandler(new CefDownloadHandler() {
//            @Override
//            public void onBeforeDownload(CefBrowser cefBrowser, CefDownloadItem cefDownloadItem, String s, CefBeforeDownloadCallback cefBeforeDownloadCallback) {
//                File file = new File("");
//                String absolutePath = file.getAbsolutePath();
//                pathArquivoRecebido = absolutePath.concat(File.separator).concat(s);
//                cefBeforeDownloadCallback.Continue(pathArquivoRecebido, false);
//            }
//
//            @Override
//            public void onDownloadUpdated(CefBrowser cefBrowser, CefDownloadItem cefDownloadItem, CefDownloadItemCallback cefDownloadItemCallback) {
//                String rcvBytes = humanReadableByteCount(cefDownloadItem.getReceivedBytes());
//                System.out.println(rcvBytes);
//                String totalBytes = humanReadableByteCount(cefDownloadItem.getTotalBytes());
//                System.out.println(totalBytes);
//                String speed = humanReadableByteCount(cefDownloadItem.getCurrentSpeed()) + "it/s";
//                System.out.println(speed);
//
//                boolean complete = cefDownloadItem.isComplete();
//                System.out.println("Terminou : " + complete);
//
//            }
//        });

        client.getCefClient().addRequestHandler(new CefRequestHandlerAdapter() {
            @Override
            public boolean onBeforeBrowse(CefBrowser cefBrowser, CefFrame cefFrame, CefRequest cefRequest, boolean b, boolean b1) {
                CefPostData postData = cefRequest.getPostData();
                if (postData != null) {
                    Vector<CefPostDataElement> elements = new Vector<CefPostDataElement>();
                    postData.getElements(elements);
                    for (CefPostDataElement el : elements) {
                        int numBytes = el.getBytesCount();
                        if (numBytes <= 0)
                            continue;

                        byte[] readBytes = new byte[numBytes];
                        if (el.getBytes(numBytes, readBytes) <= 0)
                            continue;

                        String readString = new String(readBytes);
                        if (readString.indexOf("ignore") > -1) {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    //JOptionPane.showMessageDialog(owner_, "The request was rejected because you've entered \"ignore\" into the form.");
                                }
                            });
                            return true;
                        }
                    }
                }
                return false;
            }

            @Override
            public boolean onBeforeResourceLoad(CefBrowser cefBrowser, CefFrame cefFrame, CefRequest request) {
                // If you send a HTTP-POST request to http://www.google.com/
                // google rejects your request because they don't allow HTTP-POST.
                //
                // This test extracts the value of the test form.
                // (see "Show Form" entry within BrowserMenuBar)
                // and sends its value as HTTP-GET request to Google.
                if (request.getMethod().equalsIgnoreCase("POST") && request.getURL().equals("http://www.google.com/")) {
                    String forwardTo = "http://www.google.com/#q=";
                    CefPostData postData = request.getPostData();
                    boolean sendAsGet = false;
                    if (postData != null) {
                        Vector<CefPostDataElement> elements = new Vector<CefPostDataElement>();
                        postData.getElements(elements);
                        for (CefPostDataElement el : elements) {
                            int numBytes = el.getBytesCount();
                            if (numBytes <= 0)
                                continue;

                            byte[] readBytes = new byte[numBytes];
                            if (el.getBytes(numBytes, readBytes) <= 0)
                                continue;

                            String readString = new String(readBytes).trim();
                            String[] stringPairs = readString.split("&");
                            for (String s : stringPairs) {
                                int startPos = s.indexOf('=');
                                if (s.startsWith("searchFor"))
                                    forwardTo += s.substring(startPos + 1);
                                else if (s.startsWith("sendAsGet")) {
                                    sendAsGet = true;
                                }
                            }
                        }
                        if (sendAsGet)
                            postData.removeElements();
                    }
                    if (sendAsGet) {
                        request.setFlags(0);
                        request.setMethod("GET");
                        request.setURL(forwardTo);
                        request.setFirstPartyForCookies(forwardTo);
                        HashMap<String, String> headerMap = new HashMap<>();
                        request.getHeaderMap(headerMap);
                        headerMap.remove("Content-Type");
                        headerMap.remove("Origin");
                        request.setHeaderMap(headerMap);
                    }
                }
                return false;
            }

            @Override
            public CefResourceHandler getResourceHandler(CefBrowser cefBrowser, CefFrame cefFrame, CefRequest cefRequest) {
                // the non existing domain "foo.bar" is handled by the ResourceHandler implementation
                // E.g. if you try to load the URL http://www.foo.bar, you'll be forwarded
                // to the ResourceHandler class.
                if (cefRequest.getURL().endsWith("foo.bar/")) {
                    return new ResourceHandler();
                }
                return null;
            }

            @Override
            public void onResourceRedirect(CefBrowser cefBrowser, CefFrame cefFrame, CefRequest cefRequest, CefResponse cefResponse, StringRef stringRef) {
                super.onResourceRedirect(cefBrowser, cefFrame, cefRequest, cefResponse, stringRef);
            }

            @Override
            public boolean onResourceResponse(CefBrowser cefBrowser, CefFrame cefFrame, CefRequest cefRequest, CefResponse cefResponse) {
                return super.onResourceResponse(cefBrowser, cefFrame, cefRequest, cefResponse);
            }

            @Override
            public void onResourceLoadComplete(CefBrowser cefBrowser, CefFrame cefFrame, CefRequest cefRequest, CefResponse cefResponse, CefURLRequest.Status status, long l) {
                super.onResourceLoadComplete(cefBrowser, cefFrame, cefRequest, cefResponse, status, l);
            }

            @Override
            public boolean getAuthCredentials(CefBrowser cefBrowser, CefFrame cefFrame, boolean b, String s, int i, String s1, String s2, CefAuthCallback cefAuthCallback) {
                return super.getAuthCredentials(cefBrowser, cefFrame, b, s, i, s1, s2, cefAuthCallback);
            }

            @Override
            public boolean onQuotaRequest(CefBrowser cefBrowser, String s, long l, CefRequestCallback cefRequestCallback) {
                return super.onQuotaRequest(cefBrowser, s, l, cefRequestCallback);
            }

            @Override
            public void onProtocolExecution(CefBrowser cefBrowser, String s, BoolRef boolRef) {
                super.onProtocolExecution(cefBrowser, s, boolRef);
            }

            @Override
            public boolean onCertificateError(CefBrowser cefBrowser, CefLoadHandler.ErrorCode errorCode, String s, CefRequestCallback cefRequestCallback) {
                return super.onCertificateError(cefBrowser, errorCode, s, cefRequestCallback);
            }

            @Override
            public void onPluginCrashed(CefBrowser cefBrowser, String s) {
                super.onPluginCrashed(cefBrowser, s);
            }

            @Override
            public void onRenderProcessTerminated(CefBrowser cefBrowser, TerminationStatus terminationStatus) {
                super.onRenderProcessTerminated(cefBrowser, terminationStatus);
            }

            @Override
            public int hashCode() {
                return super.hashCode();
            }

            @Override
            public boolean equals(Object obj) {
                return super.equals(obj);
            }

            @Override
            protected Object clone() throws CloneNotSupportedException {
                return super.clone();
            }

            @Override
            public String toString() {
                return super.toString();
            }

            @Override
            protected void finalize() throws Throwable {
                super.finalize();
            }
        });

        PandomiumBrowser browser = client.loadURL("http://foo.bar/");
        //PandomiumBrowser browser = client.loadURL("https://mozilla.github.io/pdf.js/web/viewer.html");
        final JFrame frame = new JFrame();
        frame.getContentPane().add(browser.toAWTComponent(), BorderLayout.CENTER);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                frame.dispose();
                System.exit(0);
            }
        });
        frame.setTitle("JavaSwingPandomium");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        Dimension dimension = redimensionarFrameTotal();
        frame.setSize(dimension);
        frame.setVisible(true);

    }

    public static Dimension redimensionarFrameTotal() {
        return (new Dimension((int) Toolkit.getDefaultToolkit().getScreenSize().getWidth(), (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight()));
    }


}
