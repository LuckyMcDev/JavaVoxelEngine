#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying float v_lightIntensity;
varying float v_distance; // NEW!!

void main() {
    float ambient = 0.25;
    float lightStrength = ambient + (1.0 - ambient) * v_lightIntensity;
    vec3 finalColor = v_color.rgb * lightStrength * vec3(1.0, 0.95, 0.85);

    // FOG SETTINGS
    vec3 fogColor = vec3(137.0/255.0, 207.0/255.0, 240.0/255.0); // light sky blue
    float fogStart = 50.0;
    float fogEnd = 100.0;
    float fogFactor = clamp((v_distance - fogStart) / (fogEnd - fogStart), 0.0, 1.0);

    // Blend color with fog
    finalColor = mix(finalColor, fogColor, fogFactor);

    gl_FragColor = vec4(finalColor, v_color.a);
}
