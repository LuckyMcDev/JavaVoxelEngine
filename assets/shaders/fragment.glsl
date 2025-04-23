#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying float v_lightIntensity;

// This defines a "red" color threshold
const vec3 redColor = vec3(1.0, 0.0, 0.0);  // Pure red color
const float colorThreshold = 0.8; // Threshold to consider a voxel as red

void main() {
    // Determine if the voxel color is red
    float isRed = step(colorThreshold, v_color.r) * (v_color.g < 0.5 && v_color.b < 0.5);

    // Apply lighting to normal voxels
    vec3 finalColor = v_color.rgb * (v_lightIntensity + 0.5);

    // If it's red, apply a much stronger red glow effect
    if (isRed > 0.0) {
        finalColor = mix(finalColor, vec3(1.0, 0.0, 0.0), 1.0);  // Stronger red glow effect
    }

    gl_FragColor = vec4(finalColor, v_color.a);
}
