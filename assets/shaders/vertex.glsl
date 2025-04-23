attribute vec3 a_position;
attribute vec3 a_normal;
attribute vec2 a_texCoord0;
attribute vec4 a_color;

uniform mat4 u_projViewTrans;
uniform mat4 u_worldTrans;
uniform mat3 u_normalMatrix;

varying vec4 v_color;
varying float v_lightIntensity;
varying float v_distance; // NEW!!

const vec3 lightDirection = normalize(vec3(-1.0, -0.8, -0.2));
const vec3 lightColor = vec3(1.0, 0.95, 0.85); // slightly warm

void main() {
    v_color = a_color;

    vec3 worldNormal = normalize(u_normalMatrix * a_normal);
    float diffuse = max(dot(worldNormal, -lightDirection), 0.0);

    v_lightIntensity = diffuse;

    vec4 worldPosition = u_worldTrans * vec4(a_position, 1.0);
    vec4 viewPosition = u_projViewTrans * worldPosition;
    v_distance = length(viewPosition.xyz); // distance in view space

    gl_Position = viewPosition;
}
