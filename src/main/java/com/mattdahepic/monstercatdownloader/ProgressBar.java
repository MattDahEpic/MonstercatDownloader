package com.mattdahepic.monstercatdownloader;

import javax.swing.*;

public class ProgressBar extends JPanel {
    JProgressBar filebar;
    JLabel filelabel;
    JProgressBar totalbar;
    JLabel totallabel;
    
    public ProgressBar (int max) {
        filebar = new JProgressBar();
        filelabel = new JLabel("???????????/????????????");
        totalbar = new JProgressBar(0,max);
        totallabel = new JLabel("???/???");
        
        add(filebar);
        add(filelabel);
        add(totalbar);
        add(totallabel);
    }
    public void updateFileBar (int current, int max) {
        filebar.setMaximum(max);
        filebar.setValue(current);
        filelabel.setText(current+"/"+max);
    }
    public void updateTotalBar (int value) {
        totallabel.setText(value+"/"+ totalbar.getMaximum());
        totalbar.setValue(value);
    }
}
