package net.fynn.javavoxelengine;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.*;
import com.badlogic.gdx.utils.Array;
import imgui.ImGui;
import imgui.ImGuiIO;
import net.fynn.javavoxelengine.challenge.AppleCollector;
import net.fynn.javavoxelengine.challenge.ChallengeManager;
import net.fynn.javavoxelengine.chunk.GenerateVoxelInstances;
import net.fynn.javavoxelengine.gui.ThisImGui;
import net.fynn.javavoxelengine.player.Crosshair;
import net.fynn.javavoxelengine.player.Player;
import net.fynn.javavoxelengine.chunk.Chunk;
import net.fynn.javavoxelengine.chunk.ChunkGrid;
import net.fynn.javavoxelengine.voxel.VoxelModelCache;

import java.util.Random;

/**
 * Hauptklasse der Voxel-Engine.
 * Diese Klasse initialisiert und verwaltet die Voxel-Welt und die Benutzeroberfläche.
 *
 * @author Fynn
 * @version 1.2
 */
public class VoxelEngine extends Game {
    // Rendering
    private ModelBatch modelBatch;
    private Environment environment;
    private ThisImGui thisImGui;
    private Crosshair crosshair;

    // World
    private ChunkGrid chunkGrid;
    private Player player;

    // Challenges
    private ChallengeManager challengeManager;
    private AppleCollector appleCollector;

    // Welt-Seed
    private long worldSeed;

    /**
     * Initialisiert die Voxel-Engine und deren Komponenten.
     */
    @Override
    public void create() {
        // 1) Erzeuge einen zufälligen Seed
        int worldSeed = (int) new Random().nextLong();
        Gdx.app.log("VoxelEngine", "World seed: " + worldSeed);

        // 2) Initialisiere Rendering
        modelBatch = new ModelBatch();
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.8f, 0.8f, 0.8f, 1f));
        environment.add(new DirectionalLight().set(1f, 1f, 1f, -1f, -0.8f, -0.2f));

        // 3) Voxel-Cache konfigurieren
        VoxelModelCache.initialize(1f, 1f, 1f);

        // 4) Erstelle ChunkGrid mit zufälligem Seed
        chunkGrid = new ChunkGrid(20, 20, worldSeed);

        // 5) Spieler und Kamera
        player = new Player(chunkGrid);
        Camera cam = player.getCamera();

        // Berechne halbe Weltgröße in Blöcken:
        float halfWorldX = chunkGrid.getGridWidth() * Chunk.WIDTH  / 2f;
        float halfWorldZ = chunkGrid.getGridDepth() * Chunk.DEPTH  / 2f;

        // Positioniere Kamera in die Mitte des Grids, Y-Level bleibt z.B. 10:
        cam.position.set(halfWorldX, 10f, halfWorldZ);
        cam.update();

        // 6) Challenge-System
        challengeManager = new ChallengeManager();
        appleCollector   = new AppleCollector();

        // 7) UI
        crosshair = new Crosshair();
        thisImGui = new ThisImGui();
    }

    /**
     * Render-Methode, die die Voxel-Welt und die Benutzeroberfläche zeichnet.
     */
    @Override
    public void render() {
        Gdx.gl.glViewport(0, 0,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight()
        );
        Gdx.gl.glClearColor(137f/255f, 207f/255f, 240f/255f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        player.update(Gdx.graphics.getDeltaTime());

        // Cursor-Fang und -Freigabe
        ImGuiIO io = ImGui.getIO();
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.input.setCursorCatched(false);
        }
        if (!Gdx.input.isCursorCatched()
            && (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)
            || Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)
            || Gdx.input.isButtonJustPressed(Input.Buttons.MIDDLE))
            && !io.getWantCaptureMouse()) {
            Gdx.input.setCursorCatched(true);
        }

        // Challenge-Timeout prüfen
        challengeManager.update();

        // Welt rendern
        modelBatch.begin(player.getCamera());
        int renderedModelCount = 0;
        for (Chunk chunk : chunkGrid.getChunks()) {
            if (!chunk.shouldRenderChunk(chunk, player)) continue;
            Array<ModelInstance> voxelInstances = new Array<>();
            new GenerateVoxelInstances(chunk, voxelInstances);
            for (ModelInstance instance : voxelInstances) {
                modelBatch.render(instance, environment);
                renderedModelCount++;
            }
        }
        modelBatch.end();

        // Crosshair
        crosshair.render();

        // Apfelsammeln per E
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            appleCollector.tryCollectApple(
                player.getCamera(),
                chunkGrid,
                challengeManager
            );
        }

        // ImGui-Overlay
        thisImGui.render(
            player.getCamera(),
            renderedModelCount,
            challengeManager,
            chunkGrid
        );
    }

    /**
     * Gibt Ressourcen frei, die von der Voxel-Engine verwendet werden.
     */
    @Override
    public void dispose() {
        modelBatch.dispose();
        VoxelModelCache.dispose();
        crosshair.dispose();
        thisImGui.dispose();
    }

    /**
     * Optional: Getter für den aktuell verwendeten Welt-Seed.
     */
    public long getWorldSeed() {
        return worldSeed;
    }
}
