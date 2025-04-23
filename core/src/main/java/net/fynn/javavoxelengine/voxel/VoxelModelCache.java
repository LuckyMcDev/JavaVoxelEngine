package net.fynn.javavoxelengine.voxel;

import com.badlogic.gdx.graphics.g3d.Model;

import java.util.EnumMap;

/**
 * Cache für Voxel-Modelle.
 * Speichert Modelle für verschiedene Voxeltypen, um die Erstellung neuer Instanzen zu beschleunigen.
 */
public class VoxelModelCache {
    private static final EnumMap<VoxelType, Model> modelCache = new EnumMap<>(VoxelType.class);

    /**
     * Initialisiert den Cache mit Modellen für alle sichtbaren Voxeltypen.
     *
     * @param width  Die Breite der Voxel.
     * @param height Die Höhe der Voxel.
     * @param depth  Die Tiefe der Voxel.
     */
    public static void initialize(float width, float height, float depth) {
        for (VoxelType type : VoxelType.values()) {
            if (!type.isVisible()) continue;

            Model model = Voxel.createCube(width, height, depth, type);
            if (model != null) {
                modelCache.put(type, model);
            }
        }
    }

    /**
     * Gibt das Modell für einen bestimmten Voxeltyp zurück.
     *
     * @param type Der Voxeltyp, dessen Modell abgerufen werden soll.
     * @return Das Modell des Voxeltyps oder null, wenn kein Modell vorhanden ist.
     */
    public static Model getModel(VoxelType type) {
        return modelCache.get(type);
    }

    /**
     * Gibt alle gespeicherten Modelle frei.
     */
    public static void dispose() {
        for (Model model : modelCache.values()) {
            if (model != null) model.dispose();
        }
        modelCache.clear();
    }
}
