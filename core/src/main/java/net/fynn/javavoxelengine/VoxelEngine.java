package net.fynn.javavoxelengine;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.*;
import com.badlogic.gdx.utils.Array;

import net.fynn.javavoxelengine.challenge.AppleCollector;
import net.fynn.javavoxelengine.challenge.ChallengeManager;
import net.fynn.javavoxelengine.chunk.GenerateVoxelInstances;
import net.fynn.javavoxelengine.imgui.ThisImGui;
import net.fynn.javavoxelengine.player.Crosshair;
import net.fynn.javavoxelengine.player.Player;
import net.fynn.javavoxelengine.chunk.Chunk;
import net.fynn.javavoxelengine.chunk.ChunkGrid;
import net.fynn.javavoxelengine.world.VoxelModelCache;

/**
 * Hauptklasse der Voxel-Engine.
 * Diese Klasse initialisiert und verwaltet die Voxel-Welt und die Benutzeroberfläche.
 *
 * @author Fynn
 * @version 1.1
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

    /**
     * Initialisiert die Voxel-Engine und deren Komponenten.
     */
    @Override
    public void create() {
        modelBatch = new ModelBatch();
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.8f, 0.8f, 0.8f, 1f));
        environment.add(new DirectionalLight().set(1f, 1f, 1f, -1f, -0.8f, -0.2f));

        VoxelModelCache.initialize(1f, 1f, 1f);

        chunkGrid = new ChunkGrid(10, 10, 1237161111); // Keine Chunk-Größe mehr nötig

        player = new Player(chunkGrid);

        challengeManager = new ChallengeManager();

        appleCollector = new AppleCollector();

        crosshair = new Crosshair();

        thisImGui = new ThisImGui();
    }

    /**
     * Render-Methode, die die Voxel-Welt und die Benutzeroberfläche zeichnet.
     */
    @Override
    public void render() {

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClearColor(137f / 255f, 207f / 255f, 240f / 255f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        player.update(Gdx.graphics.getDeltaTime());

        Gdx.input.setCursorCatched(true);
        Gdx.input.setCursorPosition(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2);

        challengeManager.update();

        modelBatch.begin(player.getCamera());
        int renderedModelCount = 0;       // Zähler zurücksetzen
        for (Chunk chunk : chunkGrid.getChunks()) {
            if (!chunk.shouldRenderChunk(chunk,player)) continue;

            Array<ModelInstance> voxelInstances = new Array<>();
            new GenerateVoxelInstances(chunk,voxelInstances);

            // Rendern und zählen
            for (ModelInstance instance : voxelInstances) {
                modelBatch.render(instance, environment);
                renderedModelCount++;
            }
        }
        modelBatch.end();

        crosshair.render();

        Chunk playerChunk = chunkGrid.getChunkAtWorld(player.getCamera().position.x, player.getCamera().position.z);
        if(Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            appleCollector.tryCollectApple(player.getCamera(),playerChunk,challengeManager);
        }


        // Model count an ImGui übergeben
        thisImGui.render(player.getCamera(), renderedModelCount, challengeManager);
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
}
