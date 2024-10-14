package edu.walton.frc.logs;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;
import edu.wpi.first.util.datalog.DataLogRecord;
import edu.wpi.first.util.datalog.DataLogRecord.StartRecordData;

public class TheData implements Consumer<DataLogRecord> {
    final Collector<?> m_collector;
    final StartRecordData m_start;

    public TheData(StartRecordData start){
        m_start = start;
        switch(m_start.type){
            case "double": m_collector = new DoubleCollector(); break;
            default: m_collector = new Collector<Void>() {

                @Override
                public Collection<Reading<Void>> get() {
                   return Collections.emptyList();
                }
                
            };
        }
    }

    public void accept(DataLogRecord d){
        if (d.getEntry() == m_start.entry) {
            m_collector.accept(d);
        }
    }

    @Override
    public String toString() {
        return String.format(
            "Data: %s  Total Records %d  ", //values:\n %s",
            m_start.name, 
            m_collector.size()
            //m_collector
        );            
    }

    public boolean hasData() {
        return m_collector.size() > 0;
    }
}