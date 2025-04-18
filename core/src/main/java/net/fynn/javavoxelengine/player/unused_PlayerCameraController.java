package net.fynn.javavoxelengine.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;

/**
 * Eine nicht verwendete Klasse zur Steuerung der Spieler-Kamera.
 */
public class unused_PlayerCameraController extends Camera {
    private final PerspectiveCamera camera;
    private final Vector3 tmp = new Vector3();

    /** Die Empfindlichkeit der Maussteuerung. */
    public float mouseSensitivity = 0.2f;

    /** Die Bewegungsgeschwindigkeit des Spielers. */
    public float moveSpeed = 10f;


    /**
     * Erstellt einen neuen PlayerCameraController mit der angegebenen Kamera.
     *
     * @param camera Die zu steuernde Kamera.
     */
    public unused_PlayerCameraController(PerspectiveCamera camera) {
        this.camera = camera;
        Gdx.input.setCursorCatched(true); // Lock mouse to window
    }

    /**
     * Aktualisiert die Kamera basierend auf der verstrichenen Zeit.
     *
     * @param deltaTime Die seit dem letzten Aufruf verstrichene Zeit.
     */
    public void update(float deltaTime) {

        // Movement
        tmp.setZero();
        Vector3 forward = new Vector3(camera.direction).nor();
        Vector3 right = new Vector3(camera.direction).crs(Vector3.Y).nor();

        if (Gdx.input.isKeyPressed(Input.Keys.W)) tmp.add(forward);
        if (Gdx.input.isKeyPressed(Input.Keys.S)) tmp.sub(forward);
        if (Gdx.input.isKeyPressed(Input.Keys.D)) tmp.add(right);
        if (Gdx.input.isKeyPressed(Input.Keys.A)) tmp.sub(right);
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) tmp.y += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) tmp.y -= 1;

        tmp.nor().scl(moveSpeed * deltaTime);
        camera.position.add(tmp);

        camera.update();
    }

    @Override
    public void update() {
    }

    @Override
    public void update(boolean updateFrustum) {

    }
}
