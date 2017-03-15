/*
 * The MIT License
 *
 * Copyright 2016 Mackenzie G.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package lunar_lander;

import com.almasb.ents.Entity;
import com.almasb.fxgl.app.ApplicationMode;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.entity.Entities;
import com.almasb.fxgl.entity.EntityView;
import com.almasb.fxgl.entity.RenderLayer;
import com.almasb.fxgl.entity.control.ExpireCleanControl;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.PhysicsWorld;
import com.almasb.fxgl.settings.GameSettings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import lunar_lander.control.LandControl;
import lunar_lander.control.ParticleControl;
import lunar_lander.control.ParticleCrashControl;
import lunar_lander.control.PlayerControl;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import lunar_lander.control.OutOfBoundsControl;

/**
 *
 * @author Mack
 */
public class Lunar_lander extends GameApplication {

    //Entitys
    private Entity player;
    private Entity land;

    //World varibles
    private final double gravity = 0.004;

    //Window varibles
    private final int windowWidth = 1000;
    private final int windowHight = 800;

    //Land varibles 
    private double landPositionX = 0;
    private double landXOffSet;

    private Double landPoints[];

    //Lander varibles
    private double positionX = 200;
    private double positionY = 100;
    private double velocityX = Math.random();
    private double velocityY = 0;
    private double rotationalVelocity = 0;
    private double rotation = 0;

    private final double turnSpeed = .01;
    private final double maxLandingVelocity = .35;
    private final double maxThrust = .01;

    private final int delayTime = 300;

    private int delayTimerActive;
    private int hasLandedDelay = 5;

    private boolean landed = false;
    private boolean crashed = false;

    private IntegerProperty throttle;
    private IntegerProperty score = new SimpleIntegerProperty(0);
    private IntegerProperty lives = new SimpleIntegerProperty(2);
    private DoubleProperty fuel = new SimpleDoubleProperty(10000);

    //Controls 
    private PlayerControl playerC;
    private OutOfBoundsControl OutOfBoundsC;
    private LandControl landC;
    private ParticleControl particleFXC;
    private ParticleCrashControl crashFXC;
    private ArrayList<OutOfBoundsControl> outOfWorldC = new <OutOfBoundsControl>ArrayList();

    //Text varibles
    private ArrayList<Text> landingPoints = new ArrayList();
    Text screenText;
    Text boundsText;
    Text LandedText;
    Text xVelText;
    Text yVelText;

    private boolean boundsTextAdded = false;

    /**
     * Launches the game
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Bind HIDs to actions
     */
    @Override
    protected void initInput() {
        Input input = getInput(); // get input service

        input.addAction(new UserAction("Move Right") {
            @Override
            protected void onAction() {
                rotationalVelocity += turnSpeed;
            }
        }, KeyCode.D);

        input.addAction(new UserAction("Move Left") {
            @Override
            protected void onAction() {
                rotationalVelocity -= turnSpeed;
            }
        }, KeyCode.A);

        input.addAction(new UserAction("Move Up") {
            @Override
            protected void onAction() {
                if (throttle.get() < 100 && !onGround()) {
                    throttle.set(throttle.get() + 1);
                }
            }
        }, KeyCode.W);

        input.addAction(new UserAction("Move Down") {
            @Override
            protected void onAction() {
                if (throttle.get() > 0 && !onGround()) {
                    throttle.set(throttle.get() - 1);
                }
            }
        }, KeyCode.S);
    }

    /**
     * Loads in assets
     */
    @Override
    protected void initAssets() {
    }

    /**
     * Sets up the game world
     */
    @Override
    protected void initGame() {

        //create background
        Rectangle bg0 = new Rectangle(getWidth(), getHeight());
        bg0.setFill(Color.BLACK);
        Pane bg = new Pane();
        bg.getChildren().add(bg0);
        Entities.builder()
                .viewFromNode(new EntityView(bg, RenderLayer.BACKGROUND))
                .buildAndAttach(getGameWorld());

        //create player
        player = EntityFactory.newPlayer(positionX, positionY, 25);
        getGameWorld().addEntity(player);
        playerC = player.getControlUnsafe(PlayerControl.class);

        makeLand(false);
    }

    /**
     * Sets up the Physics wold
     */
    @Override
    protected void initPhysics() {
        getPhysicsWorld().setGravity(0, 0);
        PhysicsWorld physicsWorld = getPhysicsWorld();
    }

    /**
     * Set up the UI
     */
    @Override
    protected void initUI() {
        this.throttle = new SimpleIntegerProperty(0);

        boundsText = getUIFactory().newText("", Color.RED, 50);

        //create landing points with score
        createLandingPoints(landPoints);

        //create and bind the the throttle % to text on the GUI
        Text throttleText = getUIFactory().newText("", Color.WHITE, 18);
        throttleText.setTranslateX(800);
        throttleText.setTranslateY(150);
        throttleText.textProperty().bind(throttle.asString("Throttle: %d%%"));
        getGameScene().addUINodes(throttleText);

        //create and bind the the fuel left to text on the GUI
        Text fuelText = getUIFactory().newText("", Color.WHITE, 18);
        fuelText.setTranslateX(800);
        fuelText.setTranslateY(100);
        fuelText.textProperty().bind(fuel.asString("Fuel: %g"));
        getGameScene().addUINodes(fuelText);

        //create and bind the the score to text on the GUI
        Text ScoreText = getUIFactory().newText("", Color.WHITE, 18);
        ScoreText.setTranslateX(800);
        ScoreText.setTranslateY(50);
        ScoreText.textProperty().bind(score.asString("Score: %d"));
        getGameScene().addUINodes(ScoreText);

        //create and bind the the lives to text on the GUI
        Text LivesText = getUIFactory().newText("", Color.WHITE, 18);
        LivesText.setTranslateX(800);
        LivesText.setTranslateY(200);
        LivesText.textProperty().bind(lives.asString("Lives: %d"));
        getGameScene().addUINodes(LivesText);

        //create and bind the the lives to text on the GUI
        xVelText = getUIFactory().newText("X-velocity = ", Color.WHITE, 18);
        xVelText.setTranslateX(50);
        xVelText.setTranslateY(50);
        getGameScene().addUINodes(xVelText);

        //create and bind the the lives to text on the GUI
        yVelText = getUIFactory().newText("Y-velocity = ", Color.WHITE, 18);
        yVelText.setTranslateX(50);
        yVelText.setTranslateY(100);
        getGameScene().addUINodes(yVelText);
    }

    /**
     * Sets up the setting of FXGL
     *
     * @param settings Settings of the game, sets automatically
     */
    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(windowWidth);
        settings.setHeight(windowHight);
        settings.setTitle("Lunar Lander HD");
        settings.setVersion("0.2");
        settings.setIntroEnabled(false); // turn off intro
        settings.setMenuEnabled(false);  // turn off menus
        settings.setCloseConfirmation(false);
        settings.setProfilingEnabled(true);
        settings.setApplicationMode(ApplicationMode.DEVELOPER);
    }

    /**
     * The Main Game loop, attempts to run at 60HZ
     *
     * @param d Refresh rate
     */
    @Override
    protected void onUpdate(double d) {

        //Update the velosity on the GUI
        xVelText.setText("X-velocity = " + (int) (velocityX * 100)); //<editor-fold>
        yVelText.setText("Y-velocity = " + (int) (velocityY * 100));

        //set the color to green if the velocity is acceptable for landing
        if (Math.abs(velocityX) + Math.abs(velocityY) < maxLandingVelocity) {
            yVelText.fillProperty().set(Color.LIGHTGREEN);
            xVelText.fillProperty().set(Color.LIGHTGREEN);
        } else {
            yVelText.fillProperty().set(Color.WHITE);
            xVelText.fillProperty().set(Color.WHITE);
        }//</editor-fold>

        //Burn if crashed & dig in to ground to give a crashed look
        if (crashed) {  //<editor-fold>
            if (hasLandedDelay > 0) { //run 10 times on loop update
                hasLandedDelay--;
                positionX += velocityX;
                positionY += velocityY;
                playerC.SetXY(positionX, positionY);
            }
        }   //</editor-fold>

        //Resets game 
        if (onGround()) {// <editor-fold>
            if (delayTimerActive > 0) { //Run after a delay
                delayTimerActive--;
            } else {
                if (lives.get() < 1 || fuel.get() < 0.1) {  //game over if out of fuel or lives
                    JOptionPane.showMessageDialog(null, "Your final score was " + score.get(), " " + "", JOptionPane.INFORMATION_MESSAGE);
                    resetGame(true);
                } else {                //if not game over
                    resetGame(false);
                }
                getGameScene().removeUINode(LandedText);
            }
        } //</editor-fold>

        //When flying
        if (!onGround()) { //<editor-fold>

            //Calulate velocities and position
            if (fuel.get() > 0) {//if there is fuel remaining <editor-fold>
                double tempX = (float) Math.cos(Math.toRadians(rotation + 90));
                double tempY = (float) Math.sin(Math.toRadians(rotation + 90));
                velocityX += (-(double) throttle.get() / 100) * tempX * maxThrust;
                velocityY += (-(double) throttle.get() / 100) * tempY * maxThrust;
                thrustFX(throttle.get());
                useFuel(throttle.get());
            }//</editor-fold>

            //Find curernt position
            positionX += velocityX;
            positionY += velocityY;

            /*  Check if landed or crashed  
            *   landed if on flat ground and velcity is low
            *   crashed if not on flat ground or velcity is too high
             */
            if (checkHighFromLand(positionX, positionY, landPositionX, landPoints) < 25) { // if on or below land <editor-fold>
                if (findLandFlat(positionX, landPositionX, landPoints)
                        && Math.abs(velocityX) + Math.abs(velocityY) < maxLandingVelocity) {
                    //Landing was successful
                    landed = true;
                    throttle.set(0);
                    int scoreToAddTemp = getLandingScore(positionX, landPositionX, landPoints);
                    score.set(score.get() + scoreToAddTemp);
                    System.out.print("there");

                    //Creates successful Landing text
                    LandedText = getUIFactory().newText("Successful Landing " + scoreToAddTemp + " Points", Color.GREEN, 50);
                    LandedText.setTranslateX(50);
                    LandedText.setTranslateY(300);
                    getGameScene().addUINodes(LandedText);

                    //Restart game after delay
                    delayTimerActive = delayTime;

                } else {
                    //Landing was unsuccessful
                    crashed = true;
                    crashFX(100);
                    lives.set(lives.get() - 1);

                    //Creates unsuccessful Landing text
                    LandedText = getUIFactory().newText("Unsuccessful Landing", Color.RED, 50);
                    getGameScene().addUINodes(LandedText);
                    LandedText.setTranslateX(200);
                    LandedText.setTranslateY(300);

                    //Restart game after delay
                    delayTimerActive = delayTime;
                }//</editor-fold>
            }

            //Edge scrolling when in the most left and right 1/6ths of the screen
            if (positionX > (windowWidth - windowWidth / 6)) { //<editor-fold>
                //Move land and Objects attached to 
                landPositionX += ((windowWidth / 6) - (windowWidth - positionX));
                landC.Move(-velocityX, 0.0);
                updateBounds(-velocityX);
                updateLandingPoints(-velocityX);

                //Dont move player
                positionX -= (windowWidth / 6) - (windowWidth - positionX);
                playerC.MoveXY(-velocityX, 0);

            }
            if (positionX < (windowWidth / 6)) {
                //Move land and Objects attached to 
                landPositionX -= (windowWidth / 6) - positionX;
                landC.Move(-velocityX, 0.0);
                updateBounds(-velocityX);
                updateLandingPoints(-velocityX);

                //Dont move player
                positionX += (windowWidth / 6) - positionX;
                playerC.MoveXY(-velocityX, 0);
            }//</editor-fold>

            //gravity
            velocityY += gravity;

            //smooth roatation
            rotation += rotationalVelocity;

            //set positions
            playerC.SetXY(positionX, positionY);
            playerC.rotateTo(rotation);

            //check if going out of the world
            if ((landPositionX > 1300 || landPositionX < 150) && !boundsTextAdded && !onGround()) { //<editor-fold>
                boundsText = getUIFactory().newText("Turn Around", Color.RED, 50);
                boundsText.setTranslateX(300);
                boundsText.setTranslateY(400);
                getGameScene().addUINodes(boundsText);
                boundsTextAdded = true;
            } else if (boundsTextAdded) {
                getGameScene().removeUINode(boundsText);
                boundsTextAdded = false;
            }
            if (landPositionX > 1600 || landPositionX < -150) {
                lives.set(lives.get() - 1);
                if (lives.get() < 1 || fuel.get() < 0.1) {  //game over if out of fuel or lives
                    JOptionPane.showMessageDialog(null, "Your final score was " + score.get(), " " + "", JOptionPane.INFORMATION_MESSAGE);
                    resetGame(true);
                } else { //if not game over
                    resetGame(false);
                }
            }//</editor-fold>
        }//</editor-fold>
    }

    /**
     *
     * @param x The X position of the lander
     * @param lx The X position of the land
     * @param land The ground file in int[] format
     * @return Returns the Points to the left and 
     */
    private int[] findLandPoints(double x, double lx) {
        int[] points = new int[2];
        double relativeLandPos = lx + x + 12.5;
        points[0] = (int) (Math.ceil((relativeLandPos / 10)) * 2) + 3;
        points[1] = (int) (Math.floor((relativeLandPos / 10)) * 2) + 3;
        return points;
    }

    /**
     *
     * @param x The X position of the lander
     * @param lx The X position of the land
     * @param land The ground file in int[] format
     * @return
     */
    private boolean findLandFlat(double x, double lx, Double[] land) {
        boolean flat = false;
        int[] points = findLandPoints(x, lx);
        double temp1, temp2;
        temp1 = land[points[0]];
        temp2 = land[points[1]];
        if (temp1 == temp2) {
            flat = true;
        }
        return flat;
    }

    /**
     *
     * @param x The X position of the lander
     * @param y The Y position of the lander
     * @param lx The X position of the land
     * @param land The ground file in int[] format
     * @return
     */
    private double checkHighFromLand(double x, double y, double lx, Double[] land) {
        double dReturn = 0.0;
        double relativeLandPos = lx + x + 12.5;
        int point = (int) (relativeLandPos / 10) * 2 + 3;
        dReturn = windowHight - (y - land[point]);
        return dReturn;
    }

    /**
     *
     * @param x The X position of the lander
     * @param lx The X position of the land
     * @param land The ground file in int[] format
     * @return
     */
    private double checkHighFromLand(double x, double lx, Double[] land) {
        double dReturn = 0.0;
        double relativeLandPos = lx + x + 12.5;
        int point = (int) (relativeLandPos / 10) * 2 + 3;
        dReturn = windowHight - (-land[point]);
        return dReturn;
    }

    /**
     * Moves all landing points along the x axis. Used to give a scrolling
     * effect
     *
     * @param x Amount to move all landingPoints GUI Text elements
     */
    private void updateLandingPoints(double x) {
        for (int y = 0; y < landingPoints.size(); y++) {
            landingPoints.get(y).setTranslateX(
                    landingPoints.get(y).getTranslateX() + x);
        }
    }

    /**
     *
     * @param x
     */
    private void updateBounds(double x) {
        for (int y = 0; y < outOfWorldC.size(); y++) {
            outOfWorldC.get(y).MoveXY(x, 0.0);
        }
    }

    /**
     *
     * @param land The ground file in int[] format
     */
    private void createLandingPoints(Double[] land) {
        boolean wasFlat = false;
        int wasFlatCount = 0;

        for (int x = 0; x < EntityFactory.getLandWidth() / 10; x++) {
            double temp1, temp2;
            temp1 = land[(x * 2) + 3];
            temp2 = land[(x * 2) + 5];
            int maxScore = 100;
            if (temp1 == temp2) {
                wasFlat = true;
                wasFlatCount++;
            } else {
                if (wasFlatCount > 1) {
                    Text LandingScoreText = getUIFactory().newText(Integer.toString(maxScore / wasFlatCount), Color.WHITE, 15);
                    LandingScoreText.setTranslateX(((((x - 1) * 10) - 1240) + windowWidth / 2) - wasFlatCount * 5);
                    LandingScoreText.setTranslateY(checkHighFromLand(((x * 10) - 1250), 1240, land) - 5);
                    getGameScene().addUINodes(LandingScoreText);
                    landingPoints.add(LandingScoreText);
                }
                wasFlatCount = 0;
            }

        }

    }

    /**
     *
     * @param x The X position of the lander
     * @param lx The X position of the land
     * @param land The ground file in int[] format
     * @return The score for landing at a given X-coordinate
     */
    private int getLandingScore(double x, double lx, Double[] land) {
        int[] points = new int[2];
        double relativeLandPos = lx + x + 12.5;
        int FlatCountR = 0;
        int FlatCountL = 0;

        points[0] = (int) (Math.ceil((relativeLandPos / 10)) * 2) + 3;
        points[1] = (int) (Math.floor((relativeLandPos / 10)) * 2) + 3;

        double temp1, temp2;
        temp1 = land[points[0] + 2];
        temp2 = land[points[1] + 2];

        while (temp1 == temp2) {
            FlatCountR++;
            temp1 = land[points[0] + 2 + (2 * FlatCountR)];
            temp2 = land[points[1] + 2] + (2 * FlatCountR);
        }

        temp1 = land[points[0] - 2];
        temp2 = land[points[1] - 2];
        while (temp1 == temp2) {
            FlatCountL++;
            temp1 = land[points[0] - 2 - (2 * FlatCountL)];
            temp2 = land[points[1] - 2 - (2 * FlatCountL)];
        }

        return 100 / (FlatCountR + FlatCountL + 1);
    }

    /**
     * Uses fuel based on the current value of the throttle
     *
     * @param throttle The throttle value from 0 - 100
     */
    private void useFuel(int throttle) {
        double fuelTemp = Math.floor((float) fuel.get() - (float) throttle / 20);

        if (fuelTemp > 0) {
            fuel.set(fuelTemp);
        } else {
            fuel.set(0);

        }
    }

    /**
     * Adds thrust particles based on the current value of the throttle
     *
     * @param throttle The throttle value from 0 - 100
     */
    private void thrustFX(int throttle) {
        double posX = 0, posY = 0;
        if (Math.ceil((float) throttle / 20) > 0) {
            float tempX = (float) Math.cos(Math.toRadians((rotation + 90)));
            float tempY = (float) Math.sin(Math.toRadians((rotation + 90)));
            posX = (positionX + 12.5) + (tempX * 6.25);
            posY = (positionY + 12.5) + (tempY * 6.25);

        }
        for (int x = 0; x < Math.ceil((float) throttle / 20); x++) {
            Entity thrustFX = EntityFactory.particalFX(posX, posY, Color.WHITESMOKE);
            getGameWorld().addEntity(thrustFX);
            thrustFX.addControl(new ExpireCleanControl(Duration.millis(250 + Math.random() * 500)));
            particleFXC = thrustFX.getControlUnsafe(ParticleControl.class);
            particleFXC.setRotation(rotation);
        }
    }

    /**
     * Creates an explosion of particles around the lander
     */
    private void crashFX(int particles) {
        for (int x = 0; x < particles; x++) {
            Entity crashFX = EntityFactory.particalCrashFX(positionX + 12.5, positionY + 12.5, Color.rgb(255 - (int) (Math.random() * 75), 0, 0));
            getGameWorld().addEntity(crashFX);
            crashFX.addControl(new ExpireCleanControl(Duration.millis(2000 + Math.random() * 1000)));
            crashFXC = crashFX.getControlUnsafe(ParticleCrashControl.class);
        }
    }

    /**
     * Check if lander has landed or crashed
     *
     * @return Boolean is on the ground
     */
    private boolean onGround() {
        return landed || crashed;
    }

    /**
     *
     */
    private void makeLand(boolean respawining) {
        //create land
        landXOffSet = EntityFactory.getLandWidth() / 2 - windowWidth / 2;

        if (land != null) {
            land.removeFromWorld();

            landingPoints.clear();
            createLandingPoints(EntityFactory.getLand());
        }

        landPositionX = landXOffSet;
        land = EntityFactory.newLand(-landXOffSet, windowHight);
        getGameWorld().addEntity(land);
        landC = land.getControlUnsafe(LandControl.class);
        landPoints = EntityFactory.getLand();

        for (int x = 0; x < landingPoints.size(); x++) {
            getGameScene().removeUINode(landingPoints.get(x));
        }

        createLandingPoints(EntityFactory.getLand());

        //create out of bounds areas
        if (!respawining) {
            for (int x = 0; x < 4; x++) {
                Entity outOfBounds = EntityFactory.outOfBounds(-landXOffSet - 50, 0, 200, windowHight, false);
                switch (x) {
                    case 0:
                        outOfBounds = EntityFactory.outOfBounds(-landXOffSet, 0, 100, windowHight, false);
                        break;
                    case 1:
                        outOfBounds = EntityFactory.outOfBounds(-landXOffSet + 100, 0, 200, windowHight, true);
                        break;
                    case 2:
                        outOfBounds = EntityFactory.outOfBounds(+2 * landXOffSet - 50, 0, 200, windowHight, true);
                        break;
                    case 3:
                        outOfBounds = EntityFactory.outOfBounds(+2 * landXOffSet + 150, 0, 100, windowHight, false);
                        break;
                    default:
                        break;
                }

                getGameWorld().addEntity(outOfBounds);
                OutOfBoundsC = outOfBounds.getControlUnsafe(OutOfBoundsControl.class);

                outOfWorldC.add(OutOfBoundsC);
            }
        }
    }

    /**
     *
     */
    private void resetGame(boolean gameOver) {
        throttle.set(0);
        positionX = 200;
        positionY = 100;
        velocityX = Math.random();
        velocityY = 0;
        rotationalVelocity = 0;
        rotation = 0;
        landed = false;
        crashed = false;
        hasLandedDelay = 5;
        landPositionX = 0;

        if (gameOver) {
            score.set(0);
            lives.set(2);
            fuel.set(10000);
        }

        for (int x = 0; x < landingPoints.size(); x++) {
            getGameScene().removeUINode(landingPoints.get(x));
        }

        makeLand(true);

        for (int y = 0; y < outOfWorldC.size(); y++) {
            switch (y) {
                case 0:
                    outOfWorldC.get(y).SetXY(-landXOffSet, 0.0);
                    break;
                case 1:
                    outOfWorldC.get(y).SetXY(-landXOffSet + 100, 0.0);
                    break;
                case 2:
                    outOfWorldC.get(y).SetXY(+2 * landXOffSet - 50, 0.0);
                    break;
                case 3:
                    outOfWorldC.get(y).SetXY(+2 * landXOffSet + 150, 0.0);
                    break;
                default:
                    break;
            }

        }
    }
}
