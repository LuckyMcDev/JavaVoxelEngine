package net.fynn.javavoxelengine.imgui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.math.Vector3;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiTabBarFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import net.fynn.javavoxelengine.challenge.ChallengeManager;
import net.fynn.javavoxelengine.challenge.ChallengeType;
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
    private final ImBoolean showChallengeWindow = new ImBoolean(true); // New challenge window flag
    private final ImBoolean showDifficultyWindow = new ImBoolean(false); // New flag for difficulty window

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

    public void render(Camera camera, int renderedModelCount, ChallengeManager challengeManager) {
        imGuiGlfw.newFrame();
        ImGui.newFrame();

        renderMainTabBar();
        renderDemoWindow();
        renderOptionsWindow();
        renderDebugWindow(camera, renderedModelCount);
        renderChallengeWindow(challengeManager); // New challenge window
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

    private void renderDebugWindow(Camera camera, int renderedModelCount) {
        if (!showDebugWindow.get()) return;
        ImGui.setNextWindowPos(0, Gdx.graphics.getHeight() - 100, ImGuiCond.Always);
        ImGui.setNextWindowSize(Gdx.graphics.getWidth(), 100, ImGuiCond.Always);
        ImGui.begin("Debug Info", showDebugWindow,
            ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoTitleBar);

        ImGui.text("FPS: " + Gdx.graphics.getFramesPerSecond());
        ImGui.text("Rendered Model Instances: " + renderedModelCount);
        ImGui.text(String.format(
            "Camera Position: X=%.2f, Y=%.2f, Z=%.2f",
            camera.position.x, camera.position.y, camera.position.z));

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

        ImGui.end();
    }


    public void dispose() {
        imGuiGl3.dispose();
        imGuiGlfw.dispose();
        ImGui.destroyContext();
    }
}
