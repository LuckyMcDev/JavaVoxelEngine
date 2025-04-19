package net.fynn.javavoxelengine.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Crosshair {

    private ShapeRenderer shapeRenderer;

    /**
     * Zeichnet ein kleines Rechteck in der Mitte des Bildschirms mithilfe eines ShapeRenderers
     */
    public void render() {
        shapeRenderer = new ShapeRenderer();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        float centerX = Gdx.graphics.getWidth() / 2f;
        float centerY = Gdx.graphics.getHeight() / 2f;
        float size = 4f; // size of the crosshair

        shapeRenderer.setColor(1, 1, 1, 1); // white color
        shapeRenderer.rect(centerX - size / 2, centerY - size / 2, size, size);

        shapeRenderer.end();
    }

    /**
     * Destructor vom Shape Renderer
     */
    public void dispose() {
        shapeRenderer.dispose();
    }
}
