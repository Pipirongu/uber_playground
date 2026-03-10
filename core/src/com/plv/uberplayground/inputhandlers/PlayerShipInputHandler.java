package com.plv.uberplayground.inputhandlers;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.plv.uberplayground.actors.PlayerShip;
import com.plv.uberplayground.screens.GameScreen;

/**
 * Input handler for PlayerShip keyboard controls.
 * Handles arrow keys for movement and spacebar for shooting.
 */
public class PlayerShipInputHandler extends InputAdapter {
    private PlayerShip playerShip;
    private GameScreen gameScreen;
    
    // Key state tracking for continuous input
    private boolean leftPressed;
    private boolean rightPressed;
    private boolean upPressed;
    private boolean downPressed;
    
    /**
     * Constructor for PlayerShipInputHandler.
     * @param ship The PlayerShip to control
     * @param screen The GameScreen for shooting integration
     */
    public PlayerShipInputHandler(PlayerShip ship, GameScreen screen) {
        this.playerShip = ship;
        this.gameScreen = screen;
        
        // Initialize key states
        this.leftPressed = false;
        this.rightPressed = false;
        this.upPressed = false;
        this.downPressed = false;
    }
    
    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Input.Keys.LEFT:
                leftPressed = true;
                return true;
            case Input.Keys.RIGHT:
                rightPressed = true;
                return true;
            case Input.Keys.UP:
                upPressed = true;
                return true;
            case Input.Keys.DOWN:
                downPressed = true;
                return true;
            case Input.Keys.SPACE:
                // Trigger shoot on keyDown only (not continuous)
                if (playerShip.isAlive()) {
                    playerShip.shoot(gameScreen::spawnProjectile);
                }
                return true;
            default:
                return false;
        }
    }
    
    @Override
    public boolean keyUp(int keycode) {
        switch (keycode) {
            case Input.Keys.LEFT:
                leftPressed = false;
                return true;
            case Input.Keys.RIGHT:
                rightPressed = false;
                return true;
            case Input.Keys.UP:
                upPressed = false;
                return true;
            case Input.Keys.DOWN:
                downPressed = false;
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Update method to be called each frame to apply forces based on current key state.
     * This enables continuous input while keys are held down.
     */
    /**
     * Update method to be called each frame to apply forces based on current key state.
     * This enables continuous input while keys are held down.
     */
    public void update() {
        if (!playerShip.isAlive()) {
            return;
        }

        // Apply rotation based on key state (constant speed rotation)
        if (leftPressed && !rightPressed) {
            playerShip.applyRotation(1);  // Clockwise
        } else if (rightPressed && !leftPressed) {
            playerShip.applyRotation(-1); // Counter-clockwise
        } else {
            // No rotation keys pressed or both pressed - stop rotation
            playerShip.stopRotation();
        }

        // Apply thrust forces based on key state (rapid acceleration)
        if (upPressed) {
            playerShip.applyThrust(1);    // Forward thrust
        }
        if (downPressed) {
            playerShip.applyThrust(-1);   // Reverse thrust
        }
    }
}