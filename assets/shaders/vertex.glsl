attribute vec3 a_position;
attribute vec3 a_normal;
attribute vec2 a_texCoord0;
attribute vec4 a_color;

uniform mat4 u_projViewTrans;
uniform mat4 u_worldTrans;
uniform mat3 u_normalMatrix;

varying vec4 v_color;
varying float v_lightIntensity;

const vec3 lightDirection = normalize(vec3(-1.0, -0.8, -0.2)); // matches your DirectionalLight

void main() {
    v_color = a_color;

    vec3 worldNormal = normalize(u_normalMatrix * a_normal);
    v_lightIntensity = max(dot(worldNormal, -lightDirection), 0.0);

    gl_Position = u_projViewTrans * u_worldTrans * vec4(a_position, 1.0);
}
