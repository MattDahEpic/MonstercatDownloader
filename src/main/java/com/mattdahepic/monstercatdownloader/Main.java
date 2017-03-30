package com.mattdahepic.monstercatdownloader;

import com.google.gson.Gson;
import jdk.nashorn.internal.scripts.JO;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import java.io.*;

public class Main {
    private static APIResponse apiResponse;
    private static File basePath;
    public static void main(String[] args) {
        String connectSID = JOptionPane.showInputDialog("connect.sid?");
        if (connectSID.isEmpty()) {
            JOptionPane.showMessageDialog(null,"Try again.");
            System.exit(1);
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            basePath = chooser.getSelectedFile();
        } else {
            JOptionPane.showMessageDialog(null,"Try again.");
            System.exit(2);
        }
        
        //setup connect handler
        HttpClient client = HttpClients.createDefault();
        HttpContext context = new BasicHttpContext();
        CookieStore store = new BasicCookieStore();
        BasicClientCookie SIDcookie = new BasicClientCookie("connect.sid",connectSID);
        SIDcookie.setDomain("connect.monstercat.com");
        SIDcookie.setPath("/");
        store.addCookie(SIDcookie);
        context.setAttribute(ClientContext.COOKIE_STORE,store);
        //get releases
        HttpGet apiGet = new HttpGet("https://connect.monstercat.com/api/catalog/release?fields=_id");
        try {
            System.out.println("Getting API Response...");
            apiResponse = new Gson().fromJson(new InputStreamReader(client.execute(apiGet, context).getEntity().getContent()), APIResponse.class);
            System.out.println("API Response Gotten!");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,ex.getMessage());
            System.exit(3);
        }
        ProgressBar prog = new ProgressBar(apiResponse.total);
        JFrame frame = new JFrame("Monstercat Catalog Download Progress");
        frame.setSize(800,100);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(prog);
        frame.pack();
        frame.setVisible(true);
        for (Release r : apiResponse.results) {
            int releaseNum = apiResponse.results.indexOf(r);
            prog.updateTotalBar(releaseNum);
            byte[] buffer = new byte[1024];
            if (r.downloadable) {
                System.out.println(releaseNum+": DOWNLOADING");
                try {
                    //prep
                    HttpGet link = new HttpGet("https://connect.monstercat.com/api/release/" + r._id + "/download?method=download&type=mp3_v0");
                    //get
                    HttpResponse response = client.execute(link,context);
                    InputStream input = response.getEntity().getContent();
                    String pathname = basePath.getPath();
                    if (response.containsHeader("Content-Disposition")) {
                        StringBuilder sb = new StringBuilder(response.getFirstHeader("Content-Disposition").getValue().split("filename=")[1].trim());
                        sb.deleteCharAt(0);
                        sb.deleteCharAt(sb.length()-1);
                       pathname += "\\"+sb.toString();
                    } else {
                        pathname += "\\unknown.zip";
                    }
                    //save
                    CountingInputStream counting = new CountingInputStream(input);
                    OutputStream output = new FileOutputStream(pathname);
                    for (int length; (length = counting.read(buffer)) > 0;) {
                        output.write(buffer,0,length);
                        prog.updateFileBar((int)counting.getCount(),(int)response.getEntity().getContentLength());
                    }
                    System.out.println(releaseNum+": SUCCESS");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.out.println(releaseNum+": ---FAIL---");
                }
            }
        }
    }
}
