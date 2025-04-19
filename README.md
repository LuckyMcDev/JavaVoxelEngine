# Java Voxel Engine

Eine Voxel Engine in Java mit custom Chunk und Perlin noise basierter terrain generation.
Ein Imgui fenster als overlay.
Und ein parr challenges zum ausprobieren (man muss äpfel sammeln)

---

## 🚀 Entwicklungsverlauf

Hier siehst du die wichtigsten Schritte der Engine-Entwicklung in der Reihenfolge ihres Entstehens:

### 1. Grundlegende Voxel-Erzeugung
![generation_fucked.png](readme_images%2Fgeneration_fucked.png)

### 2. Perlin-Rausch Terrain einbauen
Perlin Noise:

![Perlin-Rausch](readme_images/perlin_noise.png)

Fertiger Chunk:

![img.png](readme_images/img.png)

### 3. Versuch Grosse mengen zu generieren
![full_chunk_but_laggy.png](readme_images%2Ffull_chunk_but_laggy.png)

### 4. Optimierung durch entfernen von Steinen
![img_1.png](readme_images/img_1.png)

### 5. First-Person-Steuerung
![First-Person-Steuerung](readme_images/JavaVoxelEngine-new-first-person-controller-2025-04-18%2011-30-24.mp4)

### 6. ImGui-Integration
![ImGui-Integration](readme_images/imguiimpl_img.png)

### 7. Challenge-Fenster
![Challenge-Fenster](readme_images/add_new_challenge_window.png)

### 8. Schwierigkeitsauswahl
![Schwierigkeitsauswahl](readme_images/difficuilty_window_added.png)

### 9. Challenge-Info
![Challenge-Info](readme_images/some_challenge_info.png)

### 10. Baum-Generierung
![Bäume](readme_images/smol_trees.png)

### 11. Optimiert:
![new_optimizations_added_doublefps.png](readme_images%2Fnew_optimizations_added_doublefps.png)

### 12. Beim weiterentwickeln des Apfelspiels einen fehler gefunden
Man kann sehen, dass obwohl ich in dem -64 -64 chunk bin,
die chunk local coords -1 beträgt obwohl diese nur 0-63 betragen dürften.

![rounding_error_of_death.png](readme_images%2Frounding_error_of_death.png)

Versucht durch anzeigen des Rays herauszufinden was falsch ist, keine ahnung 

![what_is_even_wrong.png](readme_images%2Fwhat_is_even_wrong.png)

---

## 🎮 Steuerung

- **Bewegung**: W/A/S/D
- **Blicksteuerung**: Rechts click + Maus bewegen
- **Cursor Escapen um Ui zu benutzen**: ESC
- **Herausforderungen starten**: Öffne das „Challenges“-Fenster im ImGui-Overlay

---

## 📺 Zwei Videos an verschidenen stellen des Development prozesses

- **Engine Walkthrough**:  
  [![Engine Demo](readme_images/JavaVoxelEngine%202025-04-17%2013-02-52.mp4)](readme_images/JavaVoxelEngine%202025-04-17%2013-02-52.mp4)

- **First-Person-Controller Demo**:  
  [![FP Controller](readme_images/JavaVoxelEngine-new-first-person-controller-2025-04-18%2011-30-24.mp4)](readme_images/JavaVoxelEngine-new-first-person-controller-2025-04-18%2011-30-24.mp4)

---

## 🛠️ Architektur & Module

- **`core`**: Haupt-Spielschleife, Weltdatenstrukturen, Rendering-Pipeline.
- **`lwjgl3`**: Low-Level-Fenster- und Eingabehandling via LWJGL3-Backend.
- **`assets`**: Texturen

---

*Entwickelt von Fynn © 2025*
