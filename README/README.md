# Java Voxel Engine

Eine Voxel Engine in Java mit custom Chunk und Perlin noise basierter terrain generation.
Ein Imgui fenster als overlay.
Und ein parr challenges zum ausprobieren (man muss äpfel sammeln)

![Lines of Code](https://img.shields.io/badge/zeilen%20an%20code-2006-brightgreen)

---

## 🎮 Steuerung

- **Bewegung**: W/A/S/D
- **Blicksteuerung**: Rechts click + Maus bewegen
- **Cursor Escapen um Ui zu benutzen**: ESC
- **Herausforderungen starten**: Öffne das „Challenges“-Fenster im ImGui-Overlay

---

## 🛠️ Architektur & Module

- **`core`**: Haupt-Spielschleife, Weltdatenstrukturen, Rendering-Pipeline.
- **`lwjgl3`**: Low-Level-Fenster- und Eingabehandling via LWJGL3-Backend.
- **`assets`**: Texturen und Shader Dateien

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

### 13. Herausgefunden was Falsch ist!

Mir wurde klar das irgendwas nmicht stimmt, also habe ich mir alle daten angeschaut die mit den Ray
und anderem zu tun hat. das sah dann so aus:

![spit_out_all_info.png](readme_images%2Fspit_out_all_info.png)

Als ich mir das genauer angeschaut hab, habe ich gesehen das die Position des rays nicht stimmt!
Wenn man genau hinschaut sieht man in diesem ScreenShot schon was falsch ist ;)

#### Als das herausgefunden war, habe ich den raytracer umgeschrieben:

In diesem Code kann man sehen wie ich den ray mit der position entlang gehe.

![code_for_raytracer.png](readme_images%2Fcode_for_raytracer.png)

### 14. Endlich Fertig?

Ich weiss noch nicht ob ich noch ein paar mehr updates wie zum beispiel einen deffrered renderer
screib oder so etwas.

Richtig cool wie deine Readme aufgebaut ist!  
Wenn du bei **15. Shaders** weitermachen willst und noch Shader-Grundlagen + was du genau gemacht hast erklären möchtest, können wir deinen Stil so weiterziehen: Locker, ein bisschen Storytelling drin, viele Bilder/Erklärungen.

Hier ein Vorschlag, wie du den nächsten Abschnitt machen könntest:

---

## 15. Shaders

Ich hatte etwas Zeit und habe angefangen, mich mit **Shaders** zu beschäftigen.

**Was ist ein Shader überhaupt?**  
Ein Shader ist einfach ein kleines Programm, das auf der Grafikkarte läuft.  
Statt, dass die CPU Pixel oder Dreiecke malt, wird die Arbeit an die GPU ausgelagert — die kann das *viel schneller*.

Es gibt verschiedene Arten von Shadern:

- **Vertex Shader**  
  Bestimmt, wo ein Punkt (Vertex) am Bildschirm angezeigt wird.  
  ➔ Hier kann man z.B. Positionen verändern, um Objekte wackeln zu lassen oder sie größer/kleiner zu machen.

- **Fragment Shader**  
  Bestimmt, welche Farbe ein Pixel bekommt.  
  ➔ Hier passieren Dinge wie Texturen aufmalen, Beleuchtung berechnen oder Spezialeffekte.


**Was ich gemacht habe:**
- Zuerst einen super simplen Shader gebaut, der einfach nur alles schwarz gemacht hat 😅  
  ![shader_shenanigans_work_but_black.png](readme_images/shader_shenanigans_work_but_black.png)

- Dann herausgefunden, dass ich die Lichtberechnung vergessen hatte.  
  ➔ Also Lighting in den Fragment Shader eingebaut!

- Ergebnis:  
  Jetzt werden Blöcke richtig schön beleuchtet 🎉

  ![fixed_the_shaders_not_having_lighting.png](readme_images/fixed_the_shaders_not_having_lighting.png)

- Ich habe dann noch ein bisschen rumgespielt und die Äpfel ganz Rot gemacht also die farbe "eingefangen"
    und dann auf alpha 1 gesetzt.
  ![really_red_apples.png](readme_images/really_red_apples.png)

**Ein mini Shader Beispiel**

Hier ein **ganz einfacher Shader**, der Blöcke einfärbt und ein bisschen Licht draufrechnet.

**Vertex Shader** (`vertex.glsl`):

```glsl
#version 330 core

layout(location = 0) in vec3 aPos;    // Position des Vertex
layout(location = 1) in vec3 aNormal; // Normale (für Lichtberechnung)

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

out vec3 FragPos;   // Weltposition des Fragments
out vec3 Normal;    // Normale zum Fragment

void main()
{
    FragPos = vec3(model * vec4(aPos, 1.0));
    Normal = mat3(transpose(inverse(model))) * aNormal;  

    gl_Position = projection * view * model * vec4(aPos, 1.0);
}
```


**Fragment Shader** (`fragment.glsl`):

```glsl
#version 330 core

in vec3 FragPos;
in vec3 Normal;

out vec4 FragColor;

uniform vec3 lightPos; 
uniform vec3 lightColor;
uniform vec3 objectColor;

void main()
{
    // Richtung vom Fragment zum Licht
    vec3 lightDir = normalize(lightPos - FragPos);
    
    // Lichtintensität (Lambert'sches Gesetz)
    float diff = max(dot(Normal, lightDir), 0.0);
    
    vec3 diffuse = diff * lightColor;
    vec3 result = (diffuse + vec3(0.1)) * objectColor; // + etwas Ambient Light

    FragColor = vec4(result, 1.0);
}
```


**Was dieser Shader macht:**
- Der **Vertex Shader** transformiert die Welt-Koordinaten korrekt auf den Bildschirm.
- Der **Fragment Shader** berechnet die Farbe basierend auf der Lichtposition und der Oberfläche des Blocks.

### 15.2 Shaders Part 2
shader sind ja cool und so deswegen habe ich noch einen hinzugefügt, nämlich einen der etwas "Fog"
hinzufügt.

![fog_using_shaders.png](readme_images%2Ffog_using_shaders.png)

Der fog wurde dann ein bissl geändert zu dem, das heisst weiter weg,
um die chunks die plötzlich eingeblendet werden zu verstecken.

![fog_tweaked.png](readme_images%2Ffog_tweaked.png)

### 15.3 Shaders Part 3

Der shader war gut, aber ein bisschen hart für die Augen.

![soften_shadows_make_more_alive.png](readme_images%2Fsoften_shadows_make_more_alive.png)

Jetzt sieht es ein bisschen besser aus!

---

*Entwickelt von Fynn © 2025*
