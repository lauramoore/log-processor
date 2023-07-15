package edu.walton.frc.logs;

import java.time.Instant;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import edu.wpi.first.util.datalog.DataLogRecord;

public class DoubleCollector implements Collector<Double>{
    List<Reading<Double>> values = new LinkedList<>();
    @Override
    public void accept(DataLogRecord t) {
        this.add(t.getTimestamp(), t.getDouble());
    }
    public int size() {
        return values.size();
    }

    private void add(long timestamp, double theValue) {
       values.add( 
        new Reading<Double>(timestamp, Double.valueOf(theValue))
       );
    }

    @Override
    public String toString() {
        return values.toString();
    }
    @Override
    public Collection<Reading<Double>> get() {
       return values;
    }

    
    
}
