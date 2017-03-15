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
import com.almasb.fxgl.entity.GameEntity;
import com.almasb.fxgl.entity.component.CollidableComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import lunar_lander.control.LandControl;
import lunar_lander.control.OutOfBoundsControl;
import lunar_lander.control.ParticleControl;
import lunar_lander.control.ParticleCrashControl;
import lunar_lander.control.PlayerControl;

/**
 *
 * @author Mack
 */
public class EntityFactory {

    private static final int landWidth = 500;

    private static Double[] generatedLand = new Double[landWidth];

    /**
     *
     * @param x
     * @param y
     * @param s
     * @return
     */
    public static Entity newPlayer(double x, double y, int s) {
        ImageView scaledLander = new ImageView("Lander.png");
        scaledLander.setPreserveRatio(false);
        scaledLander.setFitHeight(25);
        scaledLander.setFitWidth(25);

        GameEntity Player = new GameEntity();
        Player.getTypeComponent().setValue(EntityType.PLAYER);
        Player.getPositionComponent().setValue(x, y);
        Player.getMainViewComponent().setView(scaledLander, true);
        System.out.println(Player.getBoundingBoxComponent());
        Player.rotateBy(180);

        Player.setRotation(Math.toRadians(90));
        Player.addComponent(new CollidableComponent(true));
        Player.getBoundingBoxComponent().addHitBox(new HitBox("BODY", BoundingShape.circle(5)));

        Player.addControl(new PlayerControl());
        return Player;
    }

    /**
     *
     * @param x
     * @param y
     * @return
     */
    public static Entity newLand(double x, double y) {
        double currentSpace = 0.0;
        double spaceing = 10.0;
        double lastPoint = -150;
        double flatLandDevider = 1;
        
        //refined chances
        double flatLandChance = .16;
        double hillUpChance = .42;
        double hillDownChance = .84;
        
        //generate a randomiized land points
        generatedLand[0] = 0.0;
        generatedLand[1] = 0.0;
        
        for (int a = 2; a < landWidth - 2; a += 2) {
            double centerChance = (lastPoint+150)/10000;
            
            hillUpChance -= centerChance;
            hillDownChance += centerChance;
                    
            double random = Math.random();

            if (random <= (hillUpChance / flatLandDevider)) {
                generatedLand[a] = currentSpace;
                generatedLand[a + 1] = lastPoint + ((-Math.random() * 25) + 25);
                currentSpace = currentSpace + spaceing;
                lastPoint = generatedLand[a + 1];
                flatLandDevider = 1;
            } else if (random < (hillDownChance / flatLandDevider)) {
                generatedLand[a] = currentSpace;
                generatedLand[a + 1] = lastPoint + ((-Math.random() * 25) + 0);
                currentSpace = currentSpace + spaceing;
                lastPoint = generatedLand[a + 1];
                flatLandDevider = 1;
            } else {
                generatedLand[a] = currentSpace;
                generatedLand[a + 1] = lastPoint;
                currentSpace = currentSpace + spaceing;
                lastPoint = generatedLand[a + 1];
                flatLandDevider = 2;
            }
        }
        generatedLand[landWidth - 2] = currentSpace - spaceing;
        generatedLand[landWidth - 1] = 0.0;

        //put generated land in to polygon
        Polygon polygon = new Polygon();
        polygon.getPoints().addAll(generatedLand);
        polygon.setFill(Color.WHITE);
        
        //create Entity with polygon
        GameEntity Land = new GameEntity();
        Land.getTypeComponent().setValue(EntityType.PLAYER);
        Land.getPositionComponent().setValue(x, y);
        Land.getMainViewComponent().setView(polygon, true);
        Land.addControl(new LandControl());
        Land.addComponent(new CollidableComponent(true));
        
        return Land;
    }

    /**
     *
     * @return
     */
    public static Double[] getLand() {
        return generatedLand;
    }

    /**
     *
     * @return
     */
    public static int getLandWidth() {
        return (landWidth - 4) / 2 * 10;
    }

    /**
     *
     *
     * @param x
     * @param y
     * @param color
     * @return
     */
    public static Entity particalFX(double x, double y, Color color) {
        GameEntity thrustFX = new GameEntity();
        thrustFX.getPositionComponent().setValue(x, y);
        thrustFX.getTypeComponent().setValue(EntityType.PARTICLE);
        thrustFX.getMainViewComponent().setView(new Rectangle(2, 2, color));
        thrustFX.addControl(new ParticleControl());
        return thrustFX;
    }

    /**
     *
     * @param x
     * @param y
     * @param color
     * @return
     */
    public static Entity particalCrashFX(double x, double y, Color color) {
        GameEntity thrustFX = new GameEntity();
        thrustFX.getPositionComponent().setValue(x, y);
        thrustFX.getTypeComponent().setValue(EntityType.PARTICLE);
        thrustFX.getMainViewComponent().setView(new Rectangle((int) (1 + Math.random() * 2), (int) (1 + Math.random() * 2), color));
        thrustFX.addControl(new ParticleCrashControl());
        return thrustFX;
    }

    /**
     * 
     * @param x 
     * @param y 
     * @param width 
     * @param hight 
     * @param opacity 
     * @return 
     */
    public static Entity outOfBounds(double x, double y, int width, int hight, boolean opacity) {
        GameEntity thrustFX = new GameEntity();
        thrustFX.getPositionComponent().setValue(x, y);
        thrustFX.getTypeComponent().setValue(EntityType.WALL);
        if(opacity){
            thrustFX.getMainViewComponent().setView(new Rectangle(width, hight, Color.rgb(255, 0, 0, .5)));
        }
        else{
            thrustFX.getMainViewComponent().setView(new Rectangle(width, hight, Color.rgb(255, 0, 0)));
        }
        thrustFX.addControl(new OutOfBoundsControl());
        return thrustFX;
    }
}
