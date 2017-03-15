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
package lunar_lander.control;

import com.almasb.ents.AbstractControl;
import com.almasb.ents.Entity;
import com.almasb.fxgl.entity.component.PositionComponent;
import com.almasb.fxgl.entity.component.RotationComponent;
import com.almasb.fxgl.physics.PhysicsComponent;

/**
 *
 * @author Mack
 */
public class ParticleControl extends AbstractControl {

    private double moveSpeed = 2;
    private double velocityX = 0;
    private double velocityY = 0;
    
    protected PositionComponent position;
    protected PhysicsComponent player;
    protected RotationComponent rotation;

    /**
     * 
     * @param entity 
     */
    @Override
    public void onAdded(Entity entity) {
        player = entity.getComponentUnsafe(PhysicsComponent.class);
        position = entity.getComponentUnsafe(PositionComponent.class);
        rotation = entity.getComponentUnsafe(RotationComponent.class);
    }

    /**
     * 
     * @param entity
     * @param d 
     */
    @Override
    public void onUpdate(Entity entity, double d) {
        position.translate(velocityX, velocityY);
    }
    
    /**
     * Add velocity to the Particle
     * @param x Velocity to add to the X-axis
     * @param y Velocity to add to the Y-axis
     */
    public void addVelocity(double x, double y){
        velocityX += x;
        velocityY += x;
    }
    
    /**
     * Change the X,Y velocities to travel in a given direction
     * @param rotation Rotational value to rotate to in degrees
     */
    public void setRotation(double rotation) {
        float tempX = (float) Math.cos(Math.toRadians((rotation + 60)+ Math.random()*60));
        float tempY = (float) Math.sin(Math.toRadians((rotation + 60)+ Math.random()*60));
        moveSpeed = moveSpeed + Math.random() - .5;
        velocityX = moveSpeed * tempX;
        velocityY = moveSpeed * tempY;
    }
}
