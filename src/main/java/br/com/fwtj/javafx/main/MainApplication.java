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
import org.cef.callback.CefBeforeDownloadCallback;
import org.cef.callback.CefDownloadItem;
import org.cef.callback.CefDownloadItemCallback;
import org.cef.handler.CefDownloadHandler;
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

        PandomiumBrowser browser = client.loadURL("https://sistema.smartnfe.net/");
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
