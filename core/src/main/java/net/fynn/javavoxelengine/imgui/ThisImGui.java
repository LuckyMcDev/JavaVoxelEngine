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
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import com.badlogic.gdx.graphics.Camera;

/**
 * Wrappt die Dear ImGui-Integration mit ein- und ausschaltbaren Fenstern und Debug/Info-Panels.
 */
public class ThisImGui {

    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 imGuiGl3 = new ImGuiImplGl3();

    // Sichtbarkeitsflags für Fenster
    private final ImBoolean showDemoWindow = new ImBoolean(false);
    private final ImBoolean showOptionsWindow = new ImBoolean(false);
    private final ImBoolean showDebugWindow = new ImBoolean(true);

    // Anwendungsoptionen
    private final ImInt renderDistance = new ImInt(8); // in Chunks

    /**
     * Erstellt eine neue Instanz von ThisImGui und initialisiert die ImGui-Komponenten.
     */
    public ThisImGui() {
        create();
    }

    /**
     * Initialisiert die ImGui-Komponenten.
     */
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
     * Rendert alle ImGui-Elemente. Sollte in jedem Frame aufgerufen werden.
     *
     * @param camera             Die aktuelle Spielkamera (zur Positionierung von Infos).
     * @param renderedModelCount Anzahl der in diesem Frame gerenderten Modellinstanzen.
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

    /**
     * Rendert die Haupt-Tab-Leiste mit Steuerelementen.
     */
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
                //ImGui.sliderInt("Render Distance (chunks)", renderDistance, 1, 32);
                ImGui.text("TODO: RENDER DISTANCE");
                ImGui.endTabItem();
            }
            ImGui.endTabBar();
        }
        ImGui.end();
    }

    /**
     * Rendert das Demo-Fenster, wenn es sichtbar ist.
     */
    private void renderDemoWindow() {
        if (showDemoWindow.get()) {
            ImGui.showDemoWindow(showDemoWindow);
        }
    }

    /**
     * Rendert das Optionsfenster, wenn es sichtbar ist.
     */
    private void renderOptionsWindow() {
        if (!showOptionsWindow.get()) return;
        ImGui.begin("Options", showOptionsWindow, ImGuiWindowFlags.None);
        ImGui.text("Grafikoptionen");
        //ImGui.sliderInt("Render Distance", renderDistance, 1, 32);
        ImGui.text("TODO: RENDER DISTANCE");
        // TODO: Anwenden von `renderDistance.get()` auf Ihren Chunk-Renderer
        ImGui.end();
    }

    /**
     * Rendert das Debug-Fenster, wenn es sichtbar ist.
     *
     * @param camera             Die aktuelle Spielkamera.
     * @param renderedModelCount Anzahl der gerenderten Modellinstanzen.
     */
    private void renderDebugWindow(Camera camera, int renderedModelCount) {
        if (!showDebugWindow.get()) return;
        // Unten fixieren
        ImGui.setNextWindowPos(0, Gdx.graphics.getHeight() - 100, ImGuiCond.Always);
        ImGui.setNextWindowSize(Gdx.graphics.getWidth(), 100, ImGuiCond.Always);
        ImGui.begin("Debug Info", showDebugWindow,
            ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoTitleBar);

        ImGui.text("FPS: "+Gdx.graphics.getFramesPerSecond());
        ImGui.text("Rendered Model Instances: " + renderedModelCount);
        ImGui.text(String.format(
            "Camera Position: X=%.2f, Y=%.2f, Z=%.2f",
            camera.position.x, camera.position.y, camera.position.z));

        ImGui.text("Cam dir: "+camera.direction);
        ImGui.text("gdx getX: "+Gdx.input.getX());
        ImGui.sameLine();
        ImGui.text("gdx getY: "+Gdx.input.getY());
        ImGui.sameLine();
        ImGui.text("gdx touched: "+Gdx.input.isTouched());
        camera.project(new Vector3(0,0,0));
        ImGui.end();
    }

    /**
     * Gibt die von ImGui verwendeten Ressourcen frei.
     */
    public void dispose() {
        imGuiGl3.dispose();
        imGuiGlfw.dispose();
        ImGui.destroyContext();
    }
}
