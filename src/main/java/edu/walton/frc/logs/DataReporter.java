package edu.walton.frc.logs;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import edu.wpi.first.util.datalog.DataLogRecord;
import edu.wpi.first.util.datalog.DataLogRecord.StartRecordData;

class DataReporter implements Consumer<DataLogRecord> {
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
              //writeData(t);
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

     private void writeStartRecord(StartRecordData startData) {
            write("start record", startData.name, 
            startData.type, 
            String.valueOf(startData.entry));
    }

    private void write(DataLogRecord t, CharSequence ... detail) {
        Instant ofEpochMilli = Instant.ofEpochMilli(t.getTimestamp());
        String utcDateTime = DateTimeFormatter
            .ISO_LOCAL_TIME
            .withZone(ZoneId.of("UTC"))
            .format(ofEpochMilli);  
        write(utcDateTime, detail);    
    }

    private void write(String description, CharSequence ... detail) {    
        try {
            outstream.append( description );
            outstream.append(",");
            outstream.append(Arrays.stream(detail).collect(Collectors.joining(",")));
            outstream.append("\n");
        }catch(IOException e) {
            e.printStackTrace(); //don't keep going if we can't write out.
        }    
      
    }
}
