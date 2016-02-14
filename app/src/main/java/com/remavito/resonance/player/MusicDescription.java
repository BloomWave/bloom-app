package com.remavito.resonance.player;

/**
 * Created by tomMoral on 07/11/15.
 */

public class MusicDescription{
    private String name;
    private String artist;
    private boolean playable;

    MusicDescription(String _name, String _artiste){
        name = _name;
        artist = _artiste;
        playable = name.contains(".wav");

    }

    public String getName(){
        return  name;
    }
    public  String getArtist(){
        return artist;
    }

    public boolean isPlayable(){
        return playable;
    }
}
