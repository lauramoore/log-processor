package edu.walton.frc.logs;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
public class DSLogParser implements AutoCloseable{
    private static final long MAX_INT64 = (1L << 63) - 1;
    private static final double DSLOG_TIMESTEP = 0.020;

    private RandomAccessFile strm;
    private long currTime;
    private double recordTimeOffset;

    public DSLogParser(String inputFilePath) throws IOException {
        strm = new RandomAccessFile(inputFilePath, "r");
        currTime = 0;
        recordTimeOffset = DSLOG_TIMESTEP;

        readHeader();
    }
    @Override
    public void close() throws IOException {
        strm.close();
    }

    public Map<String, Object> readRecord() throws IOException {
        if (currTime < 0) {
            return null;
        }

        byte[] dataBytes = new byte[10];
        if (strm.read(dataBytes) < 10) {
            return null;
        }

        byte[] pdpBytes = new byte[25];
        if (strm.read(pdpBytes) < 25) {
            throw new EOFException("No data for PDP. Unexpected end of file.");
        }

        Map<String, Object> res = new HashMap<>();
        res.put("time", parseTimestamp(currTime));
        res.putAll(parseData(dataBytes));
        res.putAll(parsePDP(pdpBytes));
        currTime += recordTimeOffset;

        return res;
    }

    private void readHeader() throws IOException {
        byte[] versionBytes = new byte[4];
        if (strm.read(versionBytes) < 4) {
            throw new IOException("Error reading version bytes");
        }
        int version = ByteBuffer.wrap(versionBytes).order(ByteOrder.BIG_ENDIAN).getInt();
        if (version != 3) {
            throw new IOException("Unknown file version number " + version);
        }

        byte[] timeBytes = new byte[16];
        if (strm.read(timeBytes) < 16) {
            throw new IOException("Error reading timestamp bytes");
        }
        currTime = readTimestamp(timeBytes);
    }

    private Map<String, Object> parseData(byte[] dataBytes) {
        ByteBuffer buffer = ByteBuffer.wrap(dataBytes).order(ByteOrder.BIG_ENDIAN);
        byte roundTripTime = buffer.get();
        byte packetLoss = buffer.get();
        short voltage = buffer.getShort();
        byte rioCpu = buffer.get();
        byte statusBits = buffer.get();
        byte canUsage = buffer.get();
        byte wifiDb = buffer.get();
        short bandwidth = buffer.getShort();

        Map<String, Object> res = new HashMap<>();
        res.put("round_trip_time", shiftedFloat(roundTripTime, 1));
        res.put("packet_loss", 0.04 * packetLoss);
        res.put("voltage", shiftedFloat(voltage, 8));
        res.put("rio_cpu", 0.01 * shiftedFloat(rioCpu, 1));
        res.put("can_usage", 0.01 * shiftedFloat(canUsage, 1));
        res.put("wifi_db", shiftedFloat(wifiDb, 1));
        res.put("bandwidth", shiftedFloat(bandwidth, 8));

        res.put("robot_disabled", (statusBits & (1 << 7)) != 0);
        res.put("robot_auto", (statusBits & (1 << 6)) != 0);
        res.put("robot_tele", (statusBits & (1 << 5)) != 0);
        res.put("ds_disabled", (statusBits & (1 << 4)) != 0);
        res.put("ds_auto", (statusBits & (1 << 3)) != 0);
        res.put("ds_tele", (statusBits & (1 << 2)) != 0);
        res.put("watchdog", (statusBits & (1 << 1)) != 0);
        res.put("brownout", (statusBits & 1) != 0);

        return res;
    }

    private Map<String, Object> parsePDP(byte[] pdpBytes) {
        int[] pdpOffsets = new int[]{8, 18, 28, 38, 48, 58, 72, 82, 92, 102, 112, 122, 136, 146, 156, 166};

        Map<String, Object> res = new HashMap<>();
        for (int i = 0; i < pdpOffsets.length; i++) {
            double val = shiftedFloat(uintFromBytes(pdpBytes, pdpOffsets[i], 10), 3);
            res.put("pdp_" + i, val);
        }

        double totalI = 0.0;
        for (int i = 0; i < 16; i++) {
            totalI += (double) res.get("pdp_" + i);
        }
        res.put("pdp_total_current", totalI);

        return res;
    }

    private double shiftedFloat(int rawValue, int shiftRight) {
        return rawValue / Math.pow(2.0, shiftRight);
    }

    private int uintFromBytes(byte[] bytes, int offset, int sizeInBits) {
        int firstByte = offset / 8;
        int numBytes = (int) Math.ceil(sizeInBits / 8.0);

        int value;
        if (numBytes == 1) {
            value = bytes[firstByte] & 0xFF;
        } else if (numBytes == 2) {
            value = ByteBuffer.wrap(bytes, firstByte, numBytes).order(ByteOrder.BIG_ENDIAN).getShort();
        } else {
            throw new UnsupportedOperationException("Not supported");
        }

        int leftBitShift = offset - firstByte * 8;
        int rightBitShift = numBytes * 8 - sizeInBits - leftBitShift;
        value = (value & (0xFFFF >> leftBitShift)) >> rightBitShift;

        return value;
    }

    private long readTimestamp(byte[] bytes) {
        long sec = ByteBuffer.wrap(bytes, 0, 8).order(ByteOrder.BIG_ENDIAN).getLong();
        long millisec = ByteBuffer.wrap(bytes, 8, 8).order(ByteOrder.BIG_ENDIAN).getLong();

        Instant instant = Instant.ofEpochSecond(sec, millisec * 1_000_000);
        return instant.toEpochMilli();
    }

    private String parseTimestamp(long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        LocalDateTime localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        return localDateTime.format(formatter);
    }
}