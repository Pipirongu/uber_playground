package com.plv.uberplayground.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.plv.uberplayground.UberPlayground;
import com.plv.uberplayground.scoring.HighScoreManager;
import com.plv.uberplayground.scoring.ScoreConstants;

import java.util.List;

/**
 * High Score Screen that displays the top 5 high scores after game over.
 * 
 * This screen implements LibGDX's Screen interface and integrates with the
 * HighScoreManager to load and display scores. It highlights the player's
 * current score if it appears in the top 5.
 * 
 * Key features:
 * - Loads high scores on screen activation
 * - Displays top 5 scores in formatted list with highlighting
 * - Implements LibGDX Screen interface for proper game state management
 * - Handles input for returning to main menu or restarting game
 * 
 * Validates: Requirements 3.1, 3.2, 3.5
 */
public class HighScoreScreen implements Screen {
    
    private static final int VIRTUAL_WIDTH = 1280;
    private static final int VIRTUAL_HEIGHT = 720;
    
    // Screen components
    private final UberPlayground app;
    private OrthographicCamera camera;
    private Stage stage;
    private SpriteBatch batch;
    private BitmapFont font;
    private BitmapFont titleFont;
    private GlyphLayout layout;
    
    // UI Components
    private TextButton playAgainButton;
    private Skin skin;
    
    // High score data
    private HighScoreManager highScoreManager;
    private List<Integer> topScores;
    private int currentPlayerScore;
    private boolean hasCurrentScore;
    
    // Display positioning
    private float titleY;
    private float scoresStartY;
    private float lineHeight;
    
    /**
     * Creates a new HighScoreScreen.
     * 
     * @param app The main application instance
     */
    public HighScoreScreen(final UberPlayground app) {
        this(app, -1, false);
    }
    
    /**
     * Creates a new HighScoreScreen with a current player score to highlight.
     * 
     * @param app The main application instance
     * @param currentPlayerScore The player's current score to highlight if it's in top 5
     * @param hasCurrentScore Whether there is a current score to potentially highlight
     */
    public HighScoreScreen(final UberPlayground app, int currentPlayerScore, boolean hasCurrentScore) {
        this.app = app;
        this.currentPlayerScore = currentPlayerScore;
        this.hasCurrentScore = hasCurrentScore;
        
        try {
            initializeComponents();
            loadHighScores();
            calculateLayout();
            
            Gdx.app.log("HighScoreScreen", "HighScoreScreen initialized successfully");
        } catch (Exception e) {
            Gdx.app.error("HighScoreScreen", "Error initializing HighScoreScreen: " + e.getMessage());
            // Create minimal fallback to prevent crashes
            createFallbackComponents();
        }
    }
    
    /**
     * Initializes the screen components including camera, stage, fonts, and high score manager.
     */
    private void initializeComponents() {
        // Initialize camera and stage
        this.camera = new OrthographicCamera();
        this.stage = new Stage(new FillViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, this.camera));
        
        // Initialize rendering components
        this.batch = new SpriteBatch();
        this.font = new BitmapFont(); // Default LibGDX font
        this.titleFont = new BitmapFont(); // Default LibGDX font for title
        this.layout = new GlyphLayout();
        
        // Scale fonts for better visibility
        this.font.getData().setScale(1.5f);
        this.titleFont.getData().setScale(2.0f);
        
        // Initialize high score manager
        this.highScoreManager = new HighScoreManager();
        
        // Initialize UI components
        initializeUI();
    }
    
    /**
     * Initializes UI components including the Play Again button.
     * Validates: Requirements 4.1 - Display "Play Again" button at bottom
     */
    private void initializeUI() {
        // Create a simple skin for the button
        this.skin = new Skin();
        this.skin.add("default-font", font);
        
        // Create button style
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = font;
        this.skin.add("default", buttonStyle);
        
        // Create Play Again button (Requirements 4.1)
        this.playAgainButton = new TextButton("PLAY AGAIN", skin);
        this.playAgainButton.setSize(200f, 60f);
        this.playAgainButton.setPosition(
            (VIRTUAL_WIDTH - this.playAgainButton.getWidth()) / 2f, 
            50f // Position at bottom of screen
        );
        
        // Add click listener for button functionality (Requirements 4.2, 4.4)
        this.playAgainButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                startNewGame();
            }
        });
        
        // Add button to stage
        this.stage.addActor(this.playAgainButton);
    }
    
    /**
     * Starts a new game session.
     * Validates: Requirements 4.2, 4.4 - Start new session and transition to gameplay
     */
    private void startNewGame() {
        try {
            // Transition directly to gameplay state (Requirements 4.4)
            app.setScreen(new GameScreen(app));
        } catch (Exception e) {
            Gdx.app.error("HighScoreScreen", "Error starting new game: " + e.getMessage());
        }
    }
    
    /**
     * Creates minimal fallback components if initialization fails.
     */
    private void createFallbackComponents() {
        if (this.camera == null) {
            this.camera = new OrthographicCamera();
        }
        if (this.stage == null) {
            this.stage = new Stage(new FillViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, this.camera));
        }
        if (this.batch == null) {
            this.batch = new SpriteBatch();
        }
        if (this.font == null) {
            this.font = new BitmapFont();
        }
        if (this.titleFont == null) {
            this.titleFont = new BitmapFont();
            this.titleFont.getData().setScale(2.0f);
        }
        if (this.layout == null) {
            this.layout = new GlyphLayout();
        }
        if (this.highScoreManager == null) {
            this.highScoreManager = new HighScoreManager();
        }
        
        // Initialize UI components as fallback
        try {
            initializeUI();
        } catch (Exception e) {
            Gdx.app.error("HighScoreScreen", "Failed to initialize UI in fallback: " + e.getMessage());
        }
    }
    
    /**
     * Loads high scores from the HighScoreManager.
     * Validates: Requirements 3.3 - Load high scores when screen loads
     */
    private void loadHighScores() {
        try {
            this.topScores = highScoreManager.getTopScores();
            Gdx.app.log("HighScoreScreen", "Loaded " + topScores.size() + " high scores");
        } catch (Exception e) {
            Gdx.app.error("HighScoreScreen", "Failed to load high scores: " + e.getMessage());
            // Create empty list as fallback (Requirements 3.4, 6.2)
            this.topScores = new java.util.ArrayList<>();
        }
    }
    
    /**
     * Calculates the layout positions for title and score list.
     */
    private void calculateLayout() {
        this.titleY = VIRTUAL_HEIGHT - 100f;
        this.scoresStartY = VIRTUAL_HEIGHT - 200f;
        this.lineHeight = 60f;
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        Gdx.app.log("HighScoreScreen", "High Score Screen shown");
    }

    @Override
    public void render(float delta) {
        // Handle input
        handleInput();
        
        // Clear screen
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // Update and draw stage
        stage.act(delta);
        stage.draw();
        
        // Draw high scores
        batch.begin();
        try {
            renderTitle();
            renderScoreList();
            renderInstructions();
        } catch (Exception e) {
            Gdx.app.error("HighScoreScreen", "Error during rendering: " + e.getMessage());
        } finally {
            batch.end();
        }
    }
    
    /**
     * Handles input for screen navigation.
     * Supports both keyboard input and button clicks for accessibility.
     */
    private void handleInput() {
        // Return to main menu on ESCAPE or SPACE
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE) ||
            Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.SPACE)) {
            try {
                app.setScreen(new MainMenuScreen(app));
            } catch (Exception e) {
                Gdx.app.error("HighScoreScreen", "Error transitioning to MainMenuScreen: " + e.getMessage());
            }
        }
        
        // Start new game on ENTER or R (Requirements 4.2, 4.4)
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ENTER) ||
            Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.R)) {
            startNewGame();
        }
    }
    
    /**
     * Renders the "HIGH SCORES" title at the top of the screen.
     */
    private void renderTitle() {
        String titleText = "HIGH SCORES";
        layout.setText(titleFont, titleText);
        float titleX = (VIRTUAL_WIDTH - layout.width) / 2f;
        titleFont.draw(batch, titleText, titleX, titleY);
    }
    
    /**
     * Renders the list of high scores with highlighting for current player score.
     * Validates: Requirements 3.2, 3.5 - Display top 5 scores and highlight current score
     */
    private void renderScoreList() {
        if (topScores == null || topScores.isEmpty()) {
            // Display "No high scores yet" message (Requirements 3.4, 6.2)
            String noScoresText = "No high scores yet!";
            layout.setText(font, noScoresText);
            float noScoresX = (VIRTUAL_WIDTH - layout.width) / 2f;
            font.draw(batch, noScoresText, noScoresX, scoresStartY);
            return;
        }
        
        // Display up to 5 high scores (Requirements 3.2)
        int maxScoresToShow = Math.min(topScores.size(), ScoreConstants.MAX_HIGH_SCORES);
        
        for (int i = 0; i < maxScoresToShow; i++) {
            int score = topScores.get(i);
            String scoreText = String.format("%d. %d", i + 1, score);
            
            // Check if this is the current player's score to highlight (Requirements 3.5)
            boolean isCurrentPlayerScore = hasCurrentScore && score == currentPlayerScore;
            
            if (isCurrentPlayerScore) {
                // Highlight current player score with different formatting
                scoreText = ">>> " + scoreText + " <<<";
            }
            
            // Calculate centered X position for each score line individually
            layout.setText(font, scoreText);
            float scoreX = (VIRTUAL_WIDTH - layout.width) / 2f;
            float scoreY = scoresStartY - (i * lineHeight);
            
            font.draw(batch, scoreText, scoreX, scoreY);
        }
    }
    
    /**
     * Renders instruction text at the bottom of the screen.
     */
    private void renderInstructions() {
        String instructionText = "Press ENTER or R to play again, SPACE or ESC for main menu";
        layout.setText(font, instructionText);
        float instructionX = (VIRTUAL_WIDTH - layout.width) / 2f;
        float instructionY = 100f;
        
        font.draw(batch, instructionText, instructionX, instructionY);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
        // No specific pause behavior needed for high score screen
    }

    @Override
    public void resume() {
        // No specific resume behavior needed for high score screen
    }

    @Override
    public void hide() {
        // No specific hide behavior needed
    }

    @Override
    public void dispose() {
        try {
            if (batch != null) {
                batch.dispose();
            }
        } catch (Exception e) {
            Gdx.app.error("HighScoreScreen", "Error disposing batch: " + e.getMessage());
        }
        
        try {
            if (font != null) {
                font.dispose();
            }
        } catch (Exception e) {
            Gdx.app.error("HighScoreScreen", "Error disposing font: " + e.getMessage());
        }
        
        try {
            if (titleFont != null) {
                titleFont.dispose();
            }
        } catch (Exception e) {
            Gdx.app.error("HighScoreScreen", "Error disposing title font: " + e.getMessage());
        }
        
        try {
            if (skin != null) {
                skin.dispose();
            }
        } catch (Exception e) {
            Gdx.app.error("HighScoreScreen", "Error disposing skin: " + e.getMessage());
        }
        
        try {
            if (stage != null) {
                stage.dispose();
            }
        } catch (Exception e) {
            Gdx.app.error("HighScoreScreen", "Error disposing stage: " + e.getMessage());
        }
    }
}