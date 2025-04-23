package net.fynn.javavoxelengine.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiTabBarFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImBoolean;
import net.fynn.javavoxelengine.challenge.ChallengeManager;
import net.fynn.javavoxelengine.challenge.ChallengeType;
import net.fynn.javavoxelengine.chunk.Chunk;
import net.fynn.javavoxelengine.chunk.ChunkGrid;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import com.badlogic.gdx.graphics.Camera;

public class ThisImGui {

    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    // Visibility flags for windows
    private final ImBoolean showDemoWindow = new ImBoolean(false);
    private final ImBoolean showOptionsWindow = new ImBoolean(false);
    private final ImBoolean showDebugWindow = new ImBoolean(true);
    private final ImBoolean showChallengeWindow = new ImBoolean(true);
    private final ImBoolean showDifficultyWindow = new ImBoolean(false);
    private final ImBoolean showWinWindow = new ImBoolean(false);
    private final ImBoolean showLossWindow = new ImBoolean(false);

    public ThisImGui() {
        create();
    }

    private void create() {
        long windowHandle = ((Lwjgl3Graphics) Gdx.graphics).getWindow().getWindowHandle();
        GLFW.glfwMakeContextCurrent(windowHandle);
        GL.createCapabilities();
        ImGui.createContext();
        ImGuiIO io = ImGui.getIO();
        io.setIniFilename(null);
        io.getFonts().addFontDefault();
        io.getFonts().build();
        imGuiGlfw.init(windowHandle, true);
        imGuiGl3.init("#version 110");
    }

    public void render(Camera camera, int renderedModelCount, ChallengeManager challengeManager, ChunkGrid chunkGrid) {
        imGuiGlfw.newFrame();
        ImGui.newFrame();

        renderMainTabBar();
        renderDemoWindow();
        renderOptionsWindow();
        renderDebugWindow(camera, renderedModelCount, chunkGrid);
        renderChallengeWindow(challengeManager); // New challenge window
        if (challengeManager.hasPlayerWon()) {
            showWinWindow.set(true);
        } else if (challengeManager.hasPlayerLost()) {
            showLossWindow.set(true); // <-- we'll add this loss window next
        }
        renderWinWindow(challengeManager);
        renderLossWindow(challengeManager);
        renderDifficultyWindow(challengeManager); // New difficulty selection window

        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
    }

    private void renderMainTabBar() {
        ImGui.setNextWindowSize(200, 300);
        ImGui.setNextWindowPos(0, 0);
        ImGui.begin("UI Controls", ImGuiWindowFlags.NoCollapse);
        if (ImGui.beginTabBar("##MainTabBar", ImGuiTabBarFlags.None)) {
            if (ImGui.beginTabItem("Windows")) {
                ImGui.checkbox("Demo Window", showDemoWindow);
                ImGui.checkbox("Options", showOptionsWindow);
                ImGui.checkbox("Debug Info", showDebugWindow);
                ImGui.checkbox("Challenges", showChallengeWindow); // Checkbox to toggle challenge window
                ImGui.endTabItem();
            }
            if (ImGui.beginTabItem("Options")) {
                ImGui.text("TODO: RENDER DISTANCE");
                ImGui.endTabItem();
            }
            ImGui.endTabBar();
        }
        ImGui.end();
    }

    private void renderDemoWindow() {
        if (showDemoWindow.get()) {
            ImGui.showDemoWindow(showDemoWindow);
        }
    }

    private void renderOptionsWindow() {
        if (!showOptionsWindow.get()) return;
        ImGui.begin("Options", showOptionsWindow, ImGuiWindowFlags.None);
        ImGui.text("Graphics Options");
        ImGui.text("TODO: RENDER DISTANCE");
        ImGui.end();
    }

    private void renderWinWindow(ChallengeManager challengeManager) {
        if(!showWinWindow.get()) return;
        ImGui.begin("YOU WIN!", showWinWindow, ImGuiWindowFlags.None);
        ImGui.text("You won the " + challengeManager.getModeName() + " Challenge!");

        if (ImGui.button("Play Again")) {
            challengeManager.reset();
            showWinWindow.set(false);
            showDifficultyWindow.set(true); // Reopen difficulty selection
        }

        ImGui.end();
    }

    private void renderLossWindow(ChallengeManager challengeManager) {
        if(!showLossWindow.get()) return;
        ImGui.begin("GAME OVER", showLossWindow, ImGuiWindowFlags.None);
        ImGui.text("You failed the " + challengeManager.getModeName() + " Challenge.");

        if (ImGui.button("Play Again")) {
            challengeManager.reset();
            showLossWindow.set(false);
            showDifficultyWindow.set(true); // Reopen difficulty selection
        }

        ImGui.end();
    }

    private void renderDebugWindow(Camera camera, int renderedModelCount, ChunkGrid chunkGrid) {
        if (!showDebugWindow.get()) return;
        ImGui.setNextWindowPos(0, Gdx.graphics.getHeight() - 250, ImGuiCond.Always);
        ImGui.setNextWindowSize(Gdx.graphics.getWidth(), 250, ImGuiCond.Always);
        ImGui.begin("Debug Info", showDebugWindow,
            ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoTitleBar);

        ImGui.text("FPS: " + Gdx.graphics.getFramesPerSecond());
        ImGui.text("Rendered Model Instances: " + renderedModelCount);
        ImGui.text(String.format(
            "Camera Position: X=%.2f, Y=%.2f, Z=%.2f",
            camera.position.x, camera.position.y, camera.position.z));


        Chunk currChunk = chunkGrid.getChunkAtWorld(camera.position.x, camera.position.z);
        ImGui.text("Current Chunk: "+currChunk.originX+" "+currChunk.originZ);
        ImGui.text("Chunk Local Coords "+chunkGrid.getChunkLocalCoords(camera.position));

        Ray ray = camera.getPickRay(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f);
        ImGui.text("Ray: "+ray);

        Vector3 localCoords = chunkGrid.getChunkLocalCoords(camera.position);

        int localX = (int)Math.floor(localCoords.x);
        int localY = (int)Math.floor(localCoords.y);
        int localZ = (int)Math.floor(localCoords.z);

        ImGui.text("locals: "+localX+" "+localY+" "+localZ);

        Vector3 end = new Vector3();
        ray.getEndPoint(end, 1.0f);
        ImGui.text("endpoint: "+end);


        Chunk currChunkEp = chunkGrid.getChunkAtWorld(end.x, end.z);
        ImGui.text("Current Chunk: "+currChunkEp.originX+" "+currChunkEp.originZ);

        Vector3 localCoordsEp = chunkGrid.getChunkLocalCoords(end);

        int localXep = (int)Math.floor(localCoordsEp.x);
        int localYep = (int)Math.floor(localCoordsEp.y);
        int localZep = (int)Math.floor(localCoordsEp.z);

        ImGui.text("locals: "+localXep+" "+localYep+" "+localZep);

        ImGui.end();
    }

    // New method to render the challenge window
    private void renderChallengeWindow(ChallengeManager challengeManager) {
        if (!showChallengeWindow.get()) return;

        ImGui.setNextWindowSize(200, 300);
        ImGui.setNextWindowPos(0, 300);

        ImGui.begin("Challenge Window", showChallengeWindow, ImGuiWindowFlags.None);

        if (ImGui.button("Start New Challenge")) {
            // When the button is clicked, show the difficulty window
            showDifficultyWindow.set(true); // Show the difficulty window
        }

        // Show information about the currently active challenge
        if (challengeManager.isActive()) {
            ImGui.separator();
            ImGui.text("Active Challenge: " + challengeManager.getModeName());
            ImGui.text("Collected Apples: " + challengeManager.getCollected() + "/" + challengeManager.getTarget());
            ImGui.text("Time Remaining: " + String.format("%.2f", challengeManager.getTimeRemainingSecs()) + " seconds");
        }

        ImGui.end();
    }

    // New method to render the difficulty window
    private void renderDifficultyWindow(ChallengeManager challengeManager) {
        if (!showDifficultyWindow.get()) return;

        ImGui.setNextWindowSize(200, 200);
        ImGui.setNextWindowPos(0, 600);

        ImGui.begin("Select Difficulty", showDifficultyWindow, ImGuiWindowFlags.None);

        if (ImGui.button("Easy")) {
            System.out.println("Easy challenge selected!");
            challengeManager.start(ChallengeType.EASY);
            // Delay hiding the window to the next frame
            Gdx.app.postRunnable(() -> showDifficultyWindow.set(false));
        }

        ImGui.sameLine();

        if (ImGui.button("Medium")) {
            System.out.println("Medium challenge selected!");
            challengeManager.start(ChallengeType.MEDIUM);
            // Delay hiding the window to the next frame
            Gdx.app.postRunnable(() -> showDifficultyWindow.set(false));
        }

        ImGui.sameLine();

        if (ImGui.button("Hard")) {
            System.out.println("Hard challenge selected!");
            challengeManager.start(ChallengeType.HARD);
            // Delay hiding the window to the next frame
            Gdx.app.postRunnable(() -> showDifficultyWindow.set(false));
        }

        if (ImGui.button("DEBUG")) {
            System.out.println("DEBUG challenge selected!");
            challengeManager.start(ChallengeType.DEBUG);
            // Delay hiding the window to the next frame
            Gdx.app.postRunnable(() -> showDifficultyWindow.set(false));
        }

        ImGui.end();
    }


    public void dispose() {
        imGuiGl3.dispose();
        imGuiGlfw.dispose();
        ImGui.destroyContext();
    }
}
