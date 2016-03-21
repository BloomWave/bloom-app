package com.remavito.bloom_app.audio_processing;

import java.nio.ByteBuffer;

/**
 * Created by tomMoral on 20/03/16.
 */
public class FFT_Processing {


    int signal_size, m;

    // Lookup tables. Only need to recompute when size of FFT changes.
    double[] cos;
    double[] sin;

    public FFT_Processing(int signal_size) {
        this.signal_size = signal_size;
        this.m = (int) (Math.log(signal_size) / Math.log(2));

        // Make sure n is a power of 2
        if (signal_size != (1 << m))
            throw new RuntimeException("FFT length must be power of 2");

        // precompute tables
        cos = new double[signal_size / 2];
        sin = new double[signal_size / 2];

        for (int i = 0; i < signal_size / 2; i++) {
            cos[i] = Math.cos(-2 * Math.PI * i / signal_size);
            sin[i] = Math.sin(-2 * Math.PI * i / signal_size);
        }

    }

    public void rfft(ByteBuffer buff, double[] res){
        int n = res.length;
        int n_fft = 1 << (int) Math.ceil(Math.log(n) / Math.log(2));
        double[] x = new double[n_fft];
        double[] y = new double[n_fft];
        for(int i=0; i < n_fft; i++) {
            y[i] = 0;
            if(i < n)
                x[i] = (double) buff.getShort(i);
            else
                x[i] = 0;
        }
        this.fft(x, y);
        for(int i=0; i < n_fft; i ++)
            res[i] = x[i]*x[i] + y[i]*y[i];

    }

    public void fft(double[] x, double[] y) {
        int i, j, k, n1, n2, a;
        double ct, st, t1, t2;

        // Bit-reverse
        j = 0;
        n2 = signal_size / 2;
        for (i = 1; i < signal_size - 1; i++) {
            n1 = n2;
            while (j >= n1) {
                j = j - n1;
                n1 = n1 / 2;
            }
            j = j + n1;

            if (i < j) {
                t1 = x[i];
                x[i] = x[j];
                x[j] = t1;
                t1 = y[i];
                y[i] = y[j];
                y[j] = t1;
            }
        }

        // FFT
        n1 = 0;
        n2 = 1;

        for (i = 0; i < m; i++) {
            n1 = n2;
            n2 *= 2;
            a = 0;

            for (j = 0; j < n1; j++) {
                ct = cos[a];
                st = sin[a];
                a += 1 << (m - i - 1);

                for (k = j; k < signal_size; k += n2) {
                    t1 = ct * x[k + n1] - st * y[k + n1];
                    t2 = st * x[k + n1] + ct * y[k + n1];
                    x[k + n1] = x[k] - t1;
                    y[k + n1] = y[k] - t2;
                    x[k] += t1;
                    y[k] += t2;
                }
            }
        }
    }
}
