package com.taylor.access;

import com.ibm.as400.access.*;
import org.junit.Assert;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class TestFileChooser {
    @Test
    public void testConnection() throws AS400SecurityException, IOException, ObjectDoesNotExistException, InterruptedException, ErrorCompletingRequestException {
        AS400 power = new AS400("192.168.1.3", "JBURIT", "HDGTDVI5");
        power.connectService(AS400.FILE);
        power.connectService(AS400.COMMAND);
        power.connectService(AS400.DATABASE);
        Assert.assertTrue(power.isConnected());

        IFSJavaFile dir = new IFSJavaFile(power, "/");
        JFileChooser chooser = new JFileChooser(dir, new IFSSystemView(power));
        Frame parent = new Frame();
        int returnVal = chooser.showOpenDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            IFSJavaFile chosenFile = (IFSJavaFile) (chooser.getSelectedFile());
            System.out.println("You selected the file named " +
                    chosenFile.getName());
        }

        power.disconnectAllServices();
    }
}
