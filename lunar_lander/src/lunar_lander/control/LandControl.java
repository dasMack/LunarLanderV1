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
import com.almasb.fxgl.entity.component.BoundingBoxComponent;
import com.almasb.fxgl.entity.component.PositionComponent;
import com.almasb.fxgl.physics.PhysicsComponent;


/**
 *
 * @author Mack
 */
public class LandControl extends AbstractControl {

    protected PositionComponent position;
    protected PhysicsComponent land;
    protected BoundingBoxComponent bbox;

    @Override
    public void onAdded(Entity entity) {
        land = entity.getComponentUnsafe(PhysicsComponent.class);
        position = entity.getComponentUnsafe(PositionComponent.class);
        bbox = entity.getComponentUnsafe(BoundingBoxComponent.class);
    }

    @Override
    public void onUpdate(Entity entity, double tpf) {}

    /**
     * translates the lander by a given amount
     * @param x Value to move on the X-axis by
     * @param y Value to move on the Y-axis by
     */
    public void Move(double x, double y) {
        position.translate(x, y);
    }
    
//    /**
//     * 
//     */
//    public void up() {
//        if (position.getY() >= 5)
//            land.setLinearVelocity(0, -5 * 60);
//        else
//            stop();
//    }
//
//    /**
//     * 
//     */
//    public void down() {
//        if (bbox.getMaxYWorld() <= 600 - 5)
//            land.setLinearVelocity(0, 5 * 60);
//        else
//            stop();
//    }
//
//    /**
//     * 
//     */
//    public void stop() {
//        land.setLinearVelocity(0, 0);
//    }
}
