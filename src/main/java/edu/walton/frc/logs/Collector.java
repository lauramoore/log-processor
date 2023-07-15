package edu.walton.frc.logs;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Supplier;

import edu.wpi.first.util.datalog.DataLogRecord;

public interface Collector<T> extends Consumer<DataLogRecord>, Supplier<Collection<Reading<T>>>{
    default int size() {
        return 0;
    }

    @Override
    default void accept(DataLogRecord d) {

    }

}
