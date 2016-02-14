package com.remavito.resonance.player;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by tom on 01/08/15.
 */
public class WavInfo {
    private static final String RIFF_HEADER = "RIFF";
    private static final String WAVE_HEADER = "WAVE";
    private static final String FMT_HEADER = "fmt ";
    private static final String DATA_HEADER = "data";

    private static final int HEADER_SIZE = 44;

    private static final String CHARSET = "ASCII";
    private static final String TAG = "WavInfo";

    int rate;
    boolean channel;
    int dataSize;

        /* ... */
    public WavInfo(int _rate, boolean _channel, int _dataSize){
        this.rate = _rate;
        this.channel = _channel;
        this.dataSize = _dataSize;
    }

    public  int getDataSize(){
        return this.dataSize;
    }
    public  int getRate(){
        return this.rate;
    }
    public  boolean getChannel(){
        return this.channel;
    }

    public static WavInfo readHeader(InputStream wavStream)
            throws IOException, DecoderException {

        ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        wavStream.read(buffer.array(), buffer.arrayOffset(), buffer.capacity());

        buffer.rewind();
        buffer.position(buffer.position() + 20);
        int format = buffer.getShort();
        checkFormat(format == 1, "Unsupported encoding: " + format); // 1 means Linear PCM
        int channels = buffer.getShort();
        checkFormat(channels == 1 || channels == 2, "Unsupported channels: " + channels);
        int rate = buffer.getInt();
        checkFormat(rate <= 48000 && rate >= 11025, "Unsupported rate: " + rate);
        buffer.position(buffer.position() + 6);
        int bits = buffer.getShort();
        checkFormat(bits == 16, "Unsupported bits: " + bits);
        int dataSize = 0;
        while (buffer.getInt() != 0x61746164) { // "data" marker
            Log.d(TAG, "Skipping non-data chunk");
            int size = buffer.getInt();
            wavStream.skip(size);

            buffer.rewind();
            wavStream.read(buffer.array(), buffer.arrayOffset(), 8);
            buffer.rewind();
        }
        dataSize = buffer.getInt();
        checkFormat(dataSize > 0, "wrong datasize: " + dataSize);

        return new WavInfo(rate, channels == 2, dataSize);
    }

    public static byte[] readWavPcm(WavInfo info, InputStream stream) throws IOException {
        byte[] data = new byte[info.getDataSize()];
        stream.read(data, 0, data.length);
        return data;
    }

    private static void checkFormat(boolean b, String s) throws DecoderException{
        if(!b) {
            Log.i(TAG, s);
            throw new DecoderException(s);
        }
    }
}
