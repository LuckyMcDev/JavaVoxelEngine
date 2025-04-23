#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying float v_lightIntensity;
varying vec3 v_worldPosition;
varying float v_distance;

void main() {
    float ambient = 0.25;
    float lightStrength = ambient + (1.0 - ambient) * v_lightIntensity;
    vec3 baseColor = v_color.rgb * lightStrength * vec3(1.0, 0.95, 0.85);

    // FOG
    vec3 fogColor = vec3(137.0/255.0, 207.0/255.0, 240.0/255.0);
    float fogStart = 90.0;
    float fogEnd = 120.0;
    float fogFactor = clamp((v_distance - fogStart) / (fogEnd - fogStart), 0.0, 1.0);

    vec3 finalColor = mix(baseColor, fogColor, fogFactor);

    // SUN
    vec3 sunDirection = normalize(vec3(0.3, 0.7, 0.2));
    vec3 cameraToPixel = normalize(v_worldPosition);

    float sunAmount = pow(max(dot(cameraToPixel, sunDirection), 0.0), 500.0);
    vec3 sunColor = vec3(1.0, 0.95, 0.85);

    finalColor += sunColor * sunAmount * 2.0;

    gl_FragColor = vec4(finalColor, v_color.a);
}
