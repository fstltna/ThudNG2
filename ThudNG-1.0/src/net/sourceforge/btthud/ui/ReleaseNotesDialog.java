//
//  ReleaseNotesDialog.java
//  Thud
//
//  Created by asp on October 18, 2002, 12:39 AM
//  Copyright (c) 2001-2006 Anthony Parker & the THUD team. 
//  All rights reserved. See LICENSE.TXT for more information.
//
package net.sourceforge.btthud.ui;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.rtf.*;
import java.awt.*;

public class ReleaseNotesDialog extends javax.swing.JDialog {

    JButton 				okButton;
    JTextPane				notesPane;

    Document				doc;
    RTFEditorKit			rtf;
    
    public ReleaseNotesDialog(java.awt.Frame parent, boolean modal)
    {
        super(parent, modal);
        initComponents();
    }

    private void initComponents()
    {
        okButton = new javax.swing.JButton();

        rtf = new RTFEditorKit();
        doc = rtf.createDefaultDocument();
        
        // Get current classloader, then get the CHANGES.RTF file
        try
        {
            ClassLoader 	cl = this.getClass().getClassLoader();
            rtf.read(cl.getResourceAsStream("CHANGES.RTF"), doc, 0);
        }
        catch (Exception e)
        {
            try {
                doc.insertString(doc.getLength(), "Please see: http://bt-thud.sourceforge.net for release notes.", null);
            } catch (Exception e2) {
                // I give up
                System.out.println("Error: releaseNotesDialog: " + e + e2);
            }
        }
        
        notesPane = new JTextPane((StyledDocument) doc);
        notesPane.setDocument(doc);
        notesPane.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(notesPane,
                                                 JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                                 JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(600,300));

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });
        
        okButton.setText("OK");
        getContentPane().add(okButton, java.awt.BorderLayout.SOUTH);
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
               okButtonActionPerformed(evt);
            }
        });
        
        getContentPane().add(scrollPane, java.awt.BorderLayout.CENTER);
        
        pack();
    }

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt)
    {
        closeDialog(null);
    }
    
    private void closeDialog(java.awt.event.WindowEvent evt)
    {
        setVisible(false);
        dispose();
    }

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        new ReleaseNotesDialog(new javax.swing.JFrame(), true).setVisible(true);
    }
}
