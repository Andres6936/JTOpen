package com.taylor.access;

import com.ibm.as400.access.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class TestFileChooser {
    @Test
    public void testConnection() throws AS400SecurityException, IOException, ObjectDoesNotExistException, InterruptedException, ErrorCompletingRequestException {
        AS400 power = new AS400("pub400.com", "JADAN", "HDgtDVi5");
        power.connectService(AS400.FILE);
        power.connectService(AS400.COMMAND);
        power.connectService(AS400.DATABASE);
        Assertions.assertTrue(power.isConnected());

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
