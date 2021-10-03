package com.taylor.access;

import com.ibm.as400.access.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;

public class TestAS400 {
    @Test
    public void testConnection() throws AS400SecurityException, IOException, ObjectDoesNotExistException, InterruptedException, ErrorCompletingRequestException {
        AS400 power = new AS400("pub400.com", "JADAN", "HDgtDVi5");
        power.connectService(AS400.FILE);
        power.connectService(AS400.COMMAND);
        power.connectService(AS400.DATABASE);
        Assertions.assertTrue(power.isConnected());

        IFSFile file = new IFSFile(power, "/archivo.csv");
        BufferedReader reader = new BufferedReader(new IFSFileReader(file));
        // Read the first line of the file, converting characters.
        String line1 = reader.readLine();
        // Display the String that was read.
        System.out.println(line1);
        // Close the reader.
        reader.close();

        power.disconnectAllServices();
    }
}
