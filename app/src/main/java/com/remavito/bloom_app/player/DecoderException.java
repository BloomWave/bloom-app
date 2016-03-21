package com.remavito.bloom_app.player;

/**
 * Created by tom on 01/08/15.
 */
public class DecoderException extends Throwable {
    private String msg;
    public DecoderException(String _msg){
        this.msg = _msg;

    }
}
