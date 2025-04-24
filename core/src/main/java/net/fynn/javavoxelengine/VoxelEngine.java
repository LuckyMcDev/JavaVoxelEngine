package net.fynn.javavoxelengine;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.*;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.TimeUtils;
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

    // World & Gameplay
    private ChunkGrid chunkGrid;
    private Player player;
    private long worldSeed;

    // Challenges
    private ChallengeManager challengeManager;
    private AppleCollector appleCollector;

    /**
     * Initialisiert die Voxel-Engine und deren Komponenten.
     */
    @Override
    public void create() {
        // ─────────────────────────────────────────────────────────────
        // 1) Welt-Seed erzeugen
        // ─────────────────────────────────────────────────────────────
        worldSeed = new Random().nextLong();
        Gdx.app.log("VoxelEngine", "World seed: " + worldSeed);

        // ─────────────────────────────────────────────────────────────
        // 2) Shader laden
        // ─────────────────────────────────────────────────────────────
        String vertexCode = loadShaderFile("shaders/vertex.glsl");
        String fragmentCode = loadShaderFile("shaders/fragment.glsl");

        DefaultShader.Config cfg = new DefaultShader.Config(vertexCode, fragmentCode);
        cfg.vertexShader = vertexCode;
        cfg.fragmentShader = fragmentCode;

        modelBatch = new ModelBatch(new DefaultShaderProvider(cfg));

        // ─────────────────────────────────────────────────────────────
        // 3) Licht-Umgebung
        // ─────────────────────────────────────────────────────────────
        environment = new Environment();

        // ─────────────────────────────────────────────────────────────
        // 4) Voxel-Modell-Cache konfigurieren
        // ─────────────────────────────────────────────────────────────
        VoxelModelCache.initialize(1f, 1f, 1f);

        // ─────────────────────────────────────────────────────────────
        // 5) Welt-Chunks und Spieler initialisieren
        // ─────────────────────────────────────────────────────────────
        chunkGrid = new ChunkGrid(20, 20, (int) worldSeed);
        player = new Player(chunkGrid);
        Camera cam = player.getCamera();

        // Kamera zentral über der Welt platzieren
        float halfWorldX = chunkGrid.getGridWidth() * Chunk.WIDTH / 2f;
        float halfWorldZ = chunkGrid.getGridDepth() * Chunk.DEPTH / 2f;
        cam.position.set(halfWorldX, 10f, halfWorldZ);
        cam.update();

        // ─────────────────────────────────────────────────────────────
        // 6) OpenGL-Einstellungen (Backface Culling)
        // ─────────────────────────────────────────────────────────────
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);
        Gdx.gl.glCullFace(GL20.GL_BACK);
        Gdx.gl.glFrontFace(GL20.GL_CCW);

        // ─────────────────────────────────────────────────────────────
        // 7) Gameplay-Logik: Challenges
        // ─────────────────────────────────────────────────────────────
        challengeManager = new ChallengeManager();
        appleCollector = new AppleCollector();

        // ─────────────────────────────────────────────────────────────
        // 8) Benutzeroberfläche (Crosshair + ImGui)
        // ─────────────────────────────────────────────────────────────
        crosshair = new Crosshair();
        thisImGui = new ThisImGui();
    }

    /**
     * Render-Methode, die die Voxel-Welt und die Benutzeroberfläche zeichnet.
     */
    @Override
    public void render() {
        // ─────────────────────────────────────────────────────────────
        // 1) Bildschirm vorbereiten
        // ─────────────────────────────────────────────────────────────
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(137f/255f, 207f/255f, 240f/255f, 1f); // Himmelblau
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // ─────────────────────────────────────────────────────────────
        // 2) Spieler-Logik aktualisieren (Bewegung, Kamera, etc.)
        // ─────────────────────────────────────────────────────────────
        player.update(Gdx.graphics.getDeltaTime());

        // ─────────────────────────────────────────────────────────────
        // 3) Tageszeit simulieren (nur Umgebungslicht)
        // ─────────────────────────────────────────────────────────────
        float t = (TimeUtils.millis() % 2000) / 2000f; // alle 2 Sekunden
        float blend = 0.5f + 0.5f * MathUtils.sin(t * MathUtils.PI2);

        environment.clear();
        environment.set(new ColorAttribute(
            ColorAttribute.AmbientLight,
            0.2f,
            0.2f + 0.6f,
            0.2f,
            1f
        ));
        environment.add(new DirectionalLight().set(1f, 1f, 1f, -1f, -0.8f, -0.2f));

        // ─────────────────────────────────────────────────────────────
        // 4) Culling nochmal sicherstellen
        // ─────────────────────────────────────────────────────────────
        Gdx.gl.glEnable(GL20.GL_CULL_FACE);
        Gdx.gl.glCullFace(GL20.GL_BACK);
        Gdx.gl.glFrontFace(GL20.GL_CCW);

        // ─────────────────────────────────────────────────────────────
        // 5) Welt rendern
        // ─────────────────────────────────────────────────────────────
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

        // ─────────────────────────────────────────────────────────────
        // 6) Crosshair anzeigen
        // ─────────────────────────────────────────────────────────────
        crosshair.render();

        // ─────────────────────────────────────────────────────────────
        // 7) Apple-Challenge
        // ─────────────────────────────────────────────────────────────
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            appleCollector.tryCollectApple(player.getCamera(), chunkGrid, challengeManager);
        }

        // ─────────────────────────────────────────────────────────────
        // 8) Mausfang togglen per TAB
        // ─────────────────────────────────────────────────────────────
        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            boolean current = Gdx.input.isCursorCatched();
            Gdx.input.setCursorCatched(!current); // Toggle
        }

        // ─────────────────────────────────────────────────────────────
        // 9) Challenge-Zustände aktualisieren
        // ─────────────────────────────────────────────────────────────
        challengeManager.update();

        // ─────────────────────────────────────────────────────────────
        // 10) Benutzeroberfläche (ImGui)
        // ─────────────────────────────────────────────────────────────
        thisImGui.render(player.getCamera(), renderedModelCount, challengeManager, chunkGrid);
    }

    /**
     * Gibt Ressourcen frei, die von der Voxel-Engine verwendet werden.
     */
    @Override
    public void dispose() {
        // ─────────────────────────────────────────────────────────────
        // Ressourcen freigeben
        // ─────────────────────────────────────────────────────────────
        modelBatch.dispose();
        VoxelModelCache.dispose();
        crosshair.dispose();
        thisImGui.dispose();
    }

    /**
     * Loads a shader file from disk.
     *
     * @param path The path to the shader file.
     * @return The contents of the shader file as a string.
     */
    private String loadShaderFile(String path) {
        try {
            FileHandle fileHandle = Gdx.files.internal(path);
            if (!fileHandle.exists()) {
                throw new GdxRuntimeException("Shader file not found: " + path);
            }
            return fileHandle.readString();
        } catch (GdxRuntimeException e) {
            Gdx.app.error("VoxelEngine", "Error loading shader file: " + path, e);
            return ""; // fallback
        }
    }

    /**
     * Optional: Getter für den aktuell verwendeten Welt-Seed.
     *
     * @return Der Welt Seed
     */
    public long getWorldSeed() {
        return worldSeed;
    }
}
