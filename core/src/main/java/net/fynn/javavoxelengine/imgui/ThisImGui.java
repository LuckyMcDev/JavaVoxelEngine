package net.fynn.javavoxelengine.imgui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiTabBarFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImBoolean;
import net.fynn.javavoxelengine.VoxelEngine;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import com.badlogic.gdx.graphics.Camera;

/**
 * Wraps Dear ImGui integration, with toggleable windows and debug/info panels.
 */
public class ThisImGui {

    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    // Window visibility flags
    private final ImBoolean showDemoWindow = new ImBoolean(false);
    private final ImBoolean showOptionsWindow = new ImBoolean(false);
    private final ImBoolean showDebugWindow = new ImBoolean(true);

    // Application options
    private final float[] renderDistance = new float[]{VoxelEngine.CHUNK_RENDER_DISTANCE}; // in chunks

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

    /**
     * Render all ImGui elements. Should be called each frame.
     *
     * @param camera             the current game camera (for positioning info)
     * @param renderedModelCount number of model instances rendered this frame
     */
    public void render(Camera camera, int renderedModelCount) {
        imGuiGlfw.newFrame();
        ImGui.newFrame();

        renderMainTabBar();
        renderDemoWindow();
        renderOptionsWindow();
        renderDebugWindow(camera, renderedModelCount);

        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
    }

    private void renderMainTabBar() {
        ImGui.setNextWindowSize(200,300);
        ImGui.setNextWindowPos(0,0);
        ImGui.begin("UI Controls", ImGuiWindowFlags.NoCollapse);
        if (ImGui.beginTabBar("##MainTabBar", ImGuiTabBarFlags.None)) {
            if (ImGui.beginTabItem("Windows")) {
                ImGui.checkbox("Demo Window", showDemoWindow);
                ImGui.checkbox("Options", showOptionsWindow);
                ImGui.checkbox("Debug Info", showDebugWindow);
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
        //ImGui.sliderInt("Render Distance", renderDistance, 1, 32);
        ImGui.text("TODO: RENDER DISTANCE");
        // TODO: apply `renderDistance.get()` to your chunk renderer
        ImGui.end();
    }

    private void renderDebugWindow(Camera camera, int renderedModelCount) {
        if (!showDebugWindow.get()) return;
        // pin to bottom
        ImGui.setNextWindowPos(0, Gdx.graphics.getHeight() - 100, ImGuiCond.Always);
        ImGui.setNextWindowSize(Gdx.graphics.getWidth(), 100, ImGuiCond.Always);
        ImGui.begin("Debug Info", showDebugWindow,
            ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoTitleBar);

        ImGui.text("FPS: "+Gdx.graphics.getFramesPerSecond());
        ImGui.text("Rendered Model Instances: " + renderedModelCount);
        ImGui.text(String.format(
            "Camera Position: X=%.2f, Y=%.2f, Z=%.2f",
            camera.position.x, camera.position.y, camera.position.z));
        // add more metrics as needed
        ImGui.end();
    }

    public void dispose() {
        imGuiGl3.dispose();
        imGuiGlfw.dispose();
        ImGui.destroyContext();
    }
}
