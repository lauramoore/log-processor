package edu.walton.frc.logs;
import java.io.IOException;

import edu.wpi.first.util.datalog.DataLogReader;

public class App {
    public static void main(String[] args) {
        String logfile = args[0];
        try {
            DataLogReader  reader = new DataLogReader(logfile);
            String filestate = (reader.isValid()) ? "valid" : "invalid";
            System.out.println(String.format("File is %s", filestate));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        


    }
}