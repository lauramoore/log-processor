package edu.walton.frc.logs;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import edu.wpi.first.util.datalog.DataLogReader;
import edu.wpi.first.util.datalog.DataLogRecord;

public class App {
    private static Set<TheData> allData = new HashSet<>();

    public static void main(String[] args) {
        String logfile = args[0];

        if(logfile.contains(".dslog")) {
            logfile =  renameFile(logfile);   
        }    
        doRobotLogs(logfile);

    }
    static String renameFile(String logfile) {
        File orig = new File(logfile);
        File renamed = new File(logfile.replace(".dslog", ".wpilog"));
        orig.renameTo(renamed);
        return renamed.toString();
    }
    static void doRobotLogs(String logfile) {
        try{
            DataLogReader  reader = new DataLogReader(logfile);
            String filestate = (reader.isValid()) ? "valid" : "invalid";
            System.out.println(String.format("File is %s", filestate));
            DataReporter report = new DataReporter(System.out);
            Consumer<DataLogRecord> tableBuilder = (rec) -> {
                if(rec.isStart()) {
                    TheData d = new TheData(rec.getStartData());
                    allData.add(d);
                    reader.forEach(d);
                }
            };
            reader.forEach(tableBuilder);
            allData.stream()
            .filter(TheData::hasData)
            .map(TheData::toString)
            .forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}

