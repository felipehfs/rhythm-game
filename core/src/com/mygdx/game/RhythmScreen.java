package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;

import java.util.ArrayList;
import java.util.Collections;

public class RhythmScreen extends BaseScreen{

    private ArrayList<String> keyList;
    private ArrayList<Color> colorList;
    private ArrayList<TargetBox> targetList;
    private  ArrayList<ArrayList<FailingBox>> failingList;
    private Music gameMusic;
    private SongData songData;
    private final float leadTime = 3;
    private float advanceTimer;
    private float spawnHeight;
    private float noteSpeed;

    private Message message;
    private Label scoreLabel;
    private int score;
    private int maxScore;
    private Label timeLabel;
    private float songDuration;

    @Override
    public void initialize() {
        BaseActor background = new BaseActor(0, 0, mainStage);
        background.loadTexture("assets/space.png");
        background.setSize(800, 600);
        BaseActor.setWorldBounds(background);

        keyList = new ArrayList<>();
        String[] keyArray = {"F", "G", "H", "J"};
        Collections.addAll(keyList, keyArray);

        colorList = new ArrayList<>();
        Color[] colorsArray = { Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE};
        Collections.addAll(colorList, colorsArray);

        Table targetTable = new Table();
        targetTable.setFillParent(true);
        targetTable.add().colspan(4).expandY();
        targetTable.row();
        mainStage.addActor(targetTable);

        targetList = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            TargetBox tb = new TargetBox(0, 0, mainStage, keyList.get(i), colorList.get(i));
            targetList.add(tb);
            targetTable.add(tb).pad(32);
        }

        failingList = new ArrayList<>();
        for (int i = 0; i < 4;i++) {
            failingList.add(new ArrayList<FailingBox>());
        }

        advanceTimer = 0;
        spawnHeight = 650;
        noteSpeed = (spawnHeight - targetList.get(0).getY()) / leadTime;

        TextButton startButton = new TextButton("Start", BaseGame.textButtonStyle);
        startButton.addListener((Event e) -> {
            if (!isTouchDownEvent(e))
                return false;

            FileHandle dataFileHandle = Gdx.files.internal("assets/FunkyJunky.key");
            songData = new SongData();
            songData.readFromFile(dataFileHandle);
            songData.resetIndex();

            songDuration = songData.getSongDuration();
            score = 0;
            maxScore = 100 * songData.keyTimeCount();

            FileHandle songFileHandle = Gdx.files.internal("assets/" + songData.getSongName());
            gameMusic = Gdx.audio.newMusic(songFileHandle);
            startButton.setVisible(false);
            message.displayCountdown();

            return true;
        });

        scoreLabel = new Label("Score: 0" + "\n" + "Max: 0", BaseGame.labelStyle);
        scoreLabel.setAlignment(Align.right);

        timeLabel = new Label("Time: 0" + "\n" + "End: 0", BaseGame.labelStyle);
        timeLabel.setAlignment(Align.right);

        message = new Message(0, 0, uiStage);
        message.setOpacity(0);


        scoreLabel.setText("Score: " + score + "\n" + "Max: " + maxScore);
        timeLabel.setText("Time: " + 0 + "\n" + "End: " + (int)songDuration);

        uiTable.pad(10);
        uiTable.add(startButton).width(200).left();
        uiTable.add(timeLabel).width(150);
        uiTable.add(scoreLabel).width(200).right();
        uiTable.row();
        uiTable.add(message).colspan(3).expandX().expandY();

    }

    @Override
    public void update(float dt) {
        if (songData == null) return;

        if (advanceTimer < leadTime && advanceTimer + dt > leadTime) {
            gameMusic.play();
        }

        if (advanceTimer < leadTime)
            advanceTimer += dt;
        else
            advanceTimer = leadTime + gameMusic.getPosition();

        while (!songData.isFinished() && advanceTimer >= songData.getCurrentKeyTime().getTime()) {
            String key = songData.getCurrentKeyTime().getKey();
            int i = keyList.indexOf(key);

            FailingBox fb = new FailingBox(targetList.get(i).getX(), spawnHeight, mainStage);
            fb.setSpeed(noteSpeed);
            fb.setMotionAngle(270);
            fb.setColor(colorList.get(i));

            failingList.get(i).add(fb);
            songData.advanceIndex();
        }

        if (gameMusic.isPlaying()) {
            timeLabel.setText("Time: " + (int)gameMusic.getPosition() + "\n" +"End: " + (int) songDuration);
        }


        for (int i = 0; i < 4;i++) {
            String key = keyList.get(i);
            ArrayList<FailingBox> failingBoxes = failingList.get(i);
            if (failingBoxes.size() > 0) {
                FailingBox fb = failingBoxes.get(0);
                TargetBox tb = targetList.get(i);
                if (fb.getY() < tb.getY() && !fb.overlaps(tb)) {
                    message.setAnimation(message.miss);
                    message.pulseFade();
                    failingBoxes.remove(fb);
                    fb.setSpeed(0);
                    fb.flashOut();
                }
            }
        }

        if (songData.isFinished() && !gameMusic.isPlaying()) {
            message.displayCountdown();
            songData = null;
        }
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (songData == null) return false;

        String keyString = Input.Keys.toString(keycode);
        if (keyList.contains(keyString)) {
            int i = keyList.indexOf(keyString);
            TargetBox tb = targetList.get(i);
            ArrayList<FailingBox> failingItem = failingList.get(i);

            tb.pulse();
            if (failingItem.size() == 0) {
                message.setAnimation(message.miss);
                message.pulseFade();
            } else {
                FailingBox fb = failingItem.get(0);
                float distance = Math.abs(fb.getY() - tb.getY());

                if (distance < 8) {
                    message.setAnimation(message.perfect);
                    score += 100;
                } else if (distance < 16) {
                    message.setAnimation(message.great);
                    score += 80;
                } else if (distance < 24) {
                    message.setAnimation(message.good);
                    score += 50;
                } else if (distance < 32) {
                    message.setAnimation(message.almost);
                    score += 20;
                } else {
                    message.setAnimation(message.miss);
                }

                message.pulseFade();
                scoreLabel.setText("Score: " + score + "\n" + "Max: " + maxScore);
                failingItem.remove(fb);
                fb.setSpeed(0);
                fb.flashOut();
            }
        }

        return false;
    }
}
