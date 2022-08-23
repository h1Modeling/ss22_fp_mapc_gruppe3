package de.feu.massim22.group3.utils;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Point;
import org.junit.jupiter.api.Test;


/**
 * The Class <code>DirectionUtilTest</code> provides methods for testing the class <code>DirectionUtil</code>.
 * Symbols:
 * A: Agent
 * E: Enemy
 * ○: Attached Block
 * ●: Unattached Block
 * ■: Obstacle
 * 
 * @author Phil Heger
 */


public class DirectionUtilTest {

    /**
     * Test of different scenarios where the normalization has to be executed never, once
     * or multiple times (recursively) in one or both directions to test all
     * execution paths.
     */
    @Test
    public void normalizePointOntoMapTest() {
        assertEquals(new Point(3, 4), DirectionUtil.normalizePointOntoMap(
                new Point(3, 4), new Point(4, 6)));
        assertEquals(new Point(3, 0), DirectionUtil.normalizePointOntoMap(
                new Point(-1, 0), new Point(4, 4)));
        assertEquals(new Point(2, 0), DirectionUtil.normalizePointOntoMap(
                new Point(6, 0), new Point(4, 4)));
        assertEquals(new Point(2, 0), DirectionUtil.normalizePointOntoMap(
                new Point(10, 0), new Point(4, 4)));
        assertEquals(new Point(2, 2), DirectionUtil.normalizePointOntoMap(
                new Point(10, 10), new Point(4, 4)));
        assertEquals(new Point(2, 4), DirectionUtil.normalizePointOntoMap(
                new Point(10, 10), new Point(4, 6)));
        assertEquals(new Point(3, 4), DirectionUtil.normalizePointOntoMap(
                new Point(-5, -8), new Point(4, 6)));
    }

    /**
     * Two points are adjacent but one is in the negative y-direction so it is normalized
     * first to have positive coordinates and then checked for vicinity over the edges.
     * 
     *    -4-3-2-1 0 1 2 3 4
     *  -4                  
     *  -3                  
     *  -2                  
     *  -1 x                
     *   0 x                
     *   1                  
     *   2                  
     *   3                  
     *   4                  
     *
     */
    @Test
    public void pointsWithinDistanceTest1(){
        assertTrue(DirectionUtil.pointsWithinDistance(
                new Point(-4, 0), new Point(-4, -1), new Point(5, 5), 1));
        assertFalse(DirectionUtil.pointsWithinDistance(
                new Point(-4, 0), new Point(-4, -1), new Point(5, 5), 0));
    }

    /**
     * Like pointsWithinDistanceTest1 but now with Manhatten distance 2
     * 
     *    -4-3-2-1 0 1 2 3 4
     *  -4                  
     *  -3                  
     *  -2                  
     *  -1   x              
     *   0 x                
     *   1                  
     *   2                  
     *   3                  
     *   4                  
     *
     */

    @Test
    public void pointsWithinDistanceTest2(){
        assertTrue(DirectionUtil.pointsWithinDistance(
                new Point(-4, 0), new Point(-3, -1), new Point(5, 5), 2));
        assertFalse(DirectionUtil.pointsWithinDistance(
                new Point(-4, 0), new Point(-3, -1), new Point(5, 5), 1));
    }

    /**
     * Only one point has to be normalized the other one has already positive coordinates
     * 
     *    -4-3-2-1 0 1 2 3 4
     *  -4                  
     *  -3                  
     *  -2     x            
     *  -1                  
     *   0                  
     *   1                  
     *   2                  
     *   3                  
     *   4                 x
     *
     */

    @Test
    public void pointsWithinDistanceTest3(){
        assertTrue(DirectionUtil.pointsWithinDistance(
                new Point(-2, -2), new Point(4, 4), new Point(5, 5), 2));
        assertFalse(DirectionUtil.pointsWithinDistance(
                new Point(-2, -2), new Point(4, 4), new Point(5, 5), 1));
    }

    /**
     * One point has to be normalized but from too coordinates larger than the map size. T
     *  The other point has already positive coordinates smaller than the map size.
     * 
     *    -4-3-2-1 0 1 2 3 4 5
     *  -4                    
     *  -3                    
     *  -2                    
     *  -1                    
     *   0                    
     *   1                    
     *   2                    
     *   3                    
     *   4                 x  
     *   5                   x
     */

    @Test
    public void pointsWithinDistanceTest4(){
        assertTrue(DirectionUtil.pointsWithinDistance(
                new Point(5, 5), new Point(4, 4), new Point(5, 5), 2));
        assertTrue(DirectionUtil.pointsWithinDistance(
                new Point(5, 5), new Point(4, 4), new Point(5, 5), 2));
        assertFalse(DirectionUtil.pointsWithinDistance(
                new Point(5, 5), new Point(4, 4), new Point(5, 5), 1));
    }
}