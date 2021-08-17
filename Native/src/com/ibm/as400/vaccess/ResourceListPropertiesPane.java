///////////////////////////////////////////////////////////////////////////////
//                                                                             
// JTOpen (IBM Toolbox for Java - OSS version)                              
//                                                                             
// Filename: ResourceListPropertiesPane.java
//                                                                             
// The source code contained herein is licensed under the IBM Public License   
// Version 1.0, which has been approved by the Open Source Initiative.         
// Copyright (C) 1997-2000 International Business Machines Corporation and     
// others. All rights reserved.                                                
//                                                                             
///////////////////////////////////////////////////////////////////////////////

package com.ibm.as400.vaccess;

import com.ibm.as400.resource.ResourceList;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
The ResourceListPropertiesPane class represents a dialog for
updating the properties of a resource list.
**/
class ResourceListPropertiesPane
extends JDialog
{
  private static final String copyright = "Copyright (C) 1997-2000 International Business Machines Corporation and others.";




    // MRI.
    private static final String DLG_APPLY_              = ResourceLoader.getText("DLG_APPLY");
    private static final String DLG_CANCEL_             = ResourceLoader.getText("DLG_CANCEL");
    private static final String DLG_OK_                 = ResourceLoader.getText("DLG_OK");
    private static final String DLG_PROPERTIES_TITLE_   = ResourceLoader.getText("DLG_PROPERTIES_TITLE");
    private static final String RESOURCE_SORT_TAB_      = ResourceLoader.getText("RESOURCE_SORT_TAB");



    // Private data.
    private JButton             applyButton_;
    private JButton             cancelButton_;
    private JButton             okButton_;

    private ResourceListPropertiesTabbedPane tabbedPane_;

    private ErrorEventSupport   errorEventSupport_;



/**
Constructs a ResourceListPropertiesPane object.

@param resourceList         The resource list.
@param errorEventSupport    The error event support.
**/
    public ResourceListPropertiesPane(ResourceList resourceList, ErrorEventSupport errorEventSupport)
    {
        super();

        errorEventSupport_      = errorEventSupport;

        // Set up the buttons.
        okButton_ = new JButton(DLG_OK_);
        cancelButton_ = new JButton(DLG_CANCEL_);
        applyButton_ = new JButton(DLG_APPLY_);

        applyButton_.setEnabled(false);

        ActionListener actionListener = new ActionListener_();
        okButton_.addActionListener(actionListener);
        cancelButton_.addActionListener(actionListener);
        applyButton_.addActionListener(actionListener);

        JPanel buttons = new JPanel();
        buttons.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(okButton_);
        buttons.add(cancelButton_);
        buttons.add(applyButton_);

        // Set up the tabbed pane.
        tabbedPane_ = new ResourceListPropertiesTabbedPane(resourceList);
        tabbedPane_.addErrorListener(errorEventSupport_);
        tabbedPane_.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent event) {
                    applyButton_.setEnabled(true);
                }
            });
        
        // Arrange everything on this dialog.
        setTitle(ResourceLoader.substitute(DLG_PROPERTIES_TITLE_, resourceList.getPresentation().getName()));
        setResizable(false);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add("Center", tabbedPane_);
        getContentPane().add("South", buttons);
        pack();
    }



/**
Applies any changes made by the user.
**/
    public void applyChanges()
    {
        tabbedPane_.applyChanges();
        applyButton_.setEnabled(false);
    }



/**
The ActionListener_ class processes clicks of the OK, Cancel, and
Apply buttons.
**/
    private class ActionListener_ implements ActionListener
    {
        public void actionPerformed(ActionEvent event)
        {
            Object source = event.getSource();
        
            if (source == okButton_) {
                applyChanges();
                dispose();
            }
        
            else if (source == cancelButton_)
                dispose();
        
            else if (source == applyButton_) {
                applyChanges();
            }
        }
    }



}

            
