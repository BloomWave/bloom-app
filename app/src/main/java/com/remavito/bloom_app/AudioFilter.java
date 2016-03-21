package com.remavito.bloom_app;

import android.media.audiofx.AudioEffect;
import android.util.Log;

/**
 * Created by tomMoral on 07/11/15.
 */
public class AudioFilter {

    final private static int MIN_FREQ = 100;
    final private static int MAX_FREQ = 4000;
    final private static int BASE_FREQ = 700;
    final private static int SLIDER_PREC = 1000;
    final private static int LEN_BUFF = 16;

    final public static int FILTER_LOWPASS = 0;
    final public static int FILTER_LOWSHELF = 2;
    final public static int FILTER_HIGHPASS = 1;
    final public static int FILTER_HIGHSHELF = 3;
    final public static int FILTER_PEAK = 4;
    final public static int FILTER_CHANNEL_LOW = 0;
    final public static int FILTER_CHANNEL_HIGH = 2;

    volatile private double[] A = {1.045750809331701, -1.991609855149324, 0.954249190668299};
    volatile private double[] B = {0.002097536212669, 0.004195072425338, 0.002097536212669};
    private int Fs, Fc, channel, L, delayPos;

    private short[] delayBuffer;

    public AudioFilter(){
        Fs = 44100;
        Fc = 700;
        channel = FILTER_CHANNEL_LOW;
        delayPos = 0;
        delayBuffer = new short[LEN_BUFF];
    }

    public int getFs(){ return Fs; }
    public void setFs(int _Fs){ Fs = _Fs; }

    public String getParam(){
        String res = String.format("Freq coupure: %6d", Fc);

        return res;
    }


    public void set_params(int _Fc, double Q, double peakGain, int chan, int type) {


        channel = chan;
        Fc = _Fc;


        double K = Math.tan(Math.PI * Fc / Fs);

        A[0] = 1.0;
        A[0] = 1.0;
        if(type == FILTER_LOWPASS) {
            double norm = 1 / (1 + K / Q + K * K);
            // Low pass filter adaptation
            B[0] = K * K * norm;
            B[1] = 2 * B[0];
            B[2] = B[0];
            A[1] = 2 * (K * K - 1) * norm;
            A[2] = (1 - K / Q + K * K) * norm;
        }else if(type == FILTER_LOWSHELF){
            double SQRT2 = Math.sqrt(2.0);
            double norm = 1 / (1 + SQRT2 * K + K * K);
            double V = Math.pow(10, Math.abs(peakGain) / 20);
            // Low pass filter
            B[0] = (1 + Math.sqrt(2*V) * K + V * K * K) * norm;
            B[1] = 2 * (V * K * K - 1) * norm;
            B[2] = (1 - Math.sqrt(2*V) * K + V * K * K) * norm;
            A[1] = 2 * (K * K - 1) * norm;
            A[2] = (1 - SQRT2 * K + K * K) * norm;
        }else if(type == FILTER_HIGHSHELF){
            double SQRT2 = Math.sqrt(2.0);
            double norm = 1 / (1 + SQRT2 * K + K * K);
            double V = Math.pow(10, Math.abs(peakGain) / 20);
            // Low pass filter
            norm = 1 / (1 + SQRT2 * K + K * K);
            B[0] = (V + Math.sqrt(2 * V) * K + K * K) * norm;
            B[1] = 2 * (K * K - V) * norm;
            B[2] = (V - Math.sqrt(2 * V) * K + K * K) * norm;
            A[1] = 2 * (K * K - 1) * norm;
            A[2] = (1 - SQRT2 * K + K * K) * norm;
        }else if(type == FILTER_HIGHPASS){
            double norm = 1 / (1 + K / Q + K * K);
            // Low pass filter
            B[0] = 1 * norm;
            B[1] = -2 * B[0];
            B[2] = B[0];
            A[1] = 2 * (K * K - 1) * norm;
            A[2] = (1 - K / Q + K * K) * norm;
        }else if(type == FILTER_PEAK){
            double SQRT2 = Math.sqrt(2.0);
            double norm = 1 / (1 + 1/Q * K + K * K);
            double V = Math.pow(10, Math.abs(peakGain) / 20);
            // Peak filter
            B[0] = (1 + V/Q * K + K * K) * norm;
            B[1] = 2 * (K * K - 1) * norm;
            B[2] = (1 - V/Q * K + K * K) * norm;
            A[1] = B[1];
            A[2] = (1 - 1/Q * K + K * K) * norm;
        }
    }

    public void apply(byte[] input, int i_seg, int l_seg){
        for(int j = i_seg*l_seg; j < (i_seg+1)*l_seg; j+=4) {
            short xn = getSample(input, j + channel);
            short y = (short) (delayBuffer[delayPos] + B[0] * xn / A[0]);
            delayBuffer[delayPos] = y;
            delayBuffer[(delayPos + 1) % LEN_BUFF] += (B[1] * xn - A[1] * y) / A[0];
            delayBuffer[(delayPos + 2) % LEN_BUFF] += (B[2] * xn - A[2] * y) / A[0];
            setSample(input, j + channel, delayBuffer[delayPos]);
            delayBuffer[delayPos] = 0;
            delayPos = (delayPos + 1) % LEN_BUFF;
        }
    }


    private void reset(){
        delayPos = 0;
        for(int j = 0; j < L; j ++){
            delayBuffer[j] = 0;
        }
    }

    private static short getSample(byte[] buffer, int position) {
        return (short) (((buffer[position + 1] & 0xff) << 8) | (buffer[position] & 0xff));
    }

    public static void setSample(byte[] buffer, int position, short sample) {
        buffer[position] = (byte) (sample & 0xff);
        buffer[position + 1] = (byte) ((sample >> 8) & 0xff);
    }
}
