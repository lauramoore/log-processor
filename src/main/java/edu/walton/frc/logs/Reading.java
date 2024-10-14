package edu.walton.frc.logs;

import java.time.Instant;

public class Reading<T> {
    Instant m_timestamp;
    T m_value;
    public Reading(long timestamp, T value) {
        this(Instant.ofEpochMilli(timestamp), value);
    }
    public Reading(Instant timestamp, T value){
        m_timestamp = timestamp;
        m_value = value;
    }
    public Instant getTimestamp() {
        return m_timestamp;
    }
    public T getValue(){
        return m_value;
    }
    @Override
    public String toString() {
        return "Reading [m_timestamp=" + m_timestamp + ", m_value=" + m_value + "]";
    }

}