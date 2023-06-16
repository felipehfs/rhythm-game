package com.mygdx.game;

import com.badlogic.gdx.files.FileHandle;

import java.util.ArrayList;

public class SongData {
    private String songName;
    private float songDuration;
    private ArrayList<KeyTimePair> keyTimePairsList;
    private int keyTimeIndex;
    public class KeyTimePair {
        private String key;
        private Float time;

        public KeyTimePair(String k, Float t) {
            key = k;
            time = t;
        }

        public String getKey() {
            return key;
        }

        public Float getTime() {
            return time;
        }
    }

    public SongData() {
        keyTimePairsList = new ArrayList<KeyTimePair>();
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public float getSongDuration() {
        return songDuration;
    }

    public void setSongDuration(float songDuration) {
        this.songDuration = songDuration;
    }

    public ArrayList<KeyTimePair> getKeyTimePairsList() {
        return keyTimePairsList;
    }

    public void setKeyTimePairsList(ArrayList<KeyTimePair> keyTimePairsList) {
        this.keyTimePairsList = keyTimePairsList;
    }

    public int getKeyTimeIndex() {
        return keyTimeIndex;
    }

    public void setKeyTimeIndex(int keyTimeIndex) {
        this.keyTimeIndex = keyTimeIndex;
    }

    public void addKeyTime(String k, Float t) {
        keyTimePairsList.add(new KeyTimePair(k, t));
    }

    public void resetIndex() {
        keyTimeIndex = 0;
    }

    public void advanceIndex() {
        keyTimeIndex++;
    }

    public KeyTimePair getCurrentKeyTime() {
        return keyTimePairsList.get(keyTimeIndex);
    }

    public int keyTimeCount() {
        return keyTimePairsList.size();
    }

    public boolean isFinished() {
        return keyTimeIndex >= keyTimeCount();
    }

    public void writeToFile(FileHandle file) {
        file.writeString(getSongName() + "\n", false);
        file.writeString(getSongDuration() + "\n", true);
        for (KeyTimePair ktp: keyTimePairsList) {
            String data = ktp.getKey() + "," + ktp.getTime() + "\n";
            file.writeString(data, true);
        }
    }

   public void readFromFile(FileHandle file) {
        String rawData = file.readString();
        String[] dataArray = rawData.split("\n");
        setSongName(dataArray[0]);
        setSongDuration(Float.parseFloat(dataArray[1]));
        keyTimePairsList.clear();

        for (int i = 2; i < dataArray.length; i++) {
            String[] keyTimeData = dataArray[i].split(",");
            String key = keyTimeData[0];
            Float time = Float.parseFloat(keyTimeData[1]);
            keyTimePairsList.add(new KeyTimePair(key, time));
        }
   }
}
