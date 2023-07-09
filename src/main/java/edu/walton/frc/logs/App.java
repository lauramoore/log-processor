package edu.walton.frc.logs;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import edu.wpi.first.util.datalog.DataLogReader;
import edu.wpi.first.util.datalog.DataLogRecord;
import edu.wpi.first.util.datalog.DataLogRecord.StartRecordData;

public class App {
    public static void main(String[] args) {
        String logfile = args[0];
        try {
            DataLogReader  reader = new DataLogReader(logfile);
            String filestate = (reader.isValid()) ? "valid" : "invalid";
            System.out.println(String.format("File is %s", filestate));
            DataReporter report = new DataReporter(System.out);
            reader.forEach(report);
            System.out.println("Summary:");
            System.out.println("Start Controls " + report.getStarts());
    
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    static class DataReporter implements Consumer<DataLogRecord> {
        final Appendable outstream;
        private int starts = 0;

        /**
         * @return the starts
         */
        public int getStarts() {
            return starts;
        }

        /**
         * @param outstream
         */
        public DataReporter(Appendable outstream) {
            this.outstream = outstream;
        }

        @Override
        public void accept(DataLogRecord t){
            if (t.isStart()) {
                this.starts ++;
                StartRecordData startData = t.getStartData();
                writeStartRecord(t, startData);
            } else {
              writeData(t);
            }
        }

        private void writeData(DataLogRecord t) {
            write(t, String.valueOf(t.getEntry()));
        }

    private void writeStartRecord(DataLogRecord t, StartRecordData startData) {
            
            write(t, startData.name, 
            startData.type, 
            String.valueOf(startData.entry));

    }

    private void write(DataLogRecord t, CharSequence ... detail) {
        Instant ofEpochMilli = Instant.ofEpochMilli(t.getTimestamp());
        try {
            outstream.append(
            DateTimeFormatter
            .ISO_LOCAL_TIME
            .withZone(ZoneId.of("UTC"))
            .format(ofEpochMilli));
            outstream.append(",");
            outstream.append(Arrays.stream(detail).collect(Collectors.joining(",")));
            outstream.append("\n");
        }catch(IOException e) {
            e.printStackTrace(); //don't keep going if we can't write out.
        }    
      
    }
}


}