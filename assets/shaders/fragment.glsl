#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying float v_lightIntensity;
varying vec3 v_worldPosition;
varying float v_distance;

void main() {
    // Increase the ambient light to make the scene brighter
    float ambient = 0.35; // Brighter ambient light
    float lightStrength = ambient + (1.0 - ambient) * v_lightIntensity;

    // Enhance the base color with some warmth
    vec3 baseColor = v_color.rgb * lightStrength * vec3(1.0, 0.9, 0.8); // Slightly warmer tones

    // FOG
    vec3 fogColor = vec3(160.0/255.0, 200.0/255.0, 240.0/255.0); // Lighter and brighter blue fog
    float fogStart = 90.0; // Start fog a bit earlier
    float fogEnd = 120.0; // Keep the fog distance short for a more dynamic effect
    float fogFactor = clamp((v_distance - fogStart) / (fogEnd - fogStart), 0.0, 1.0);

    // Combine the base color with the fog color for a more vibrant look
    vec3 finalColor = mix(baseColor, fogColor, fogFactor);

    gl_FragColor = vec4(finalColor, v_color.a);
}
