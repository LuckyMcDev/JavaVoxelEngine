#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoord;
uniform sampler2D u_texture;

void main() {
    // Just sample the texture and modulate by vertex color (SpriteBatch sends white)
    gl_FragColor = v_color * texture2D(u_texture, v_texCoord);
}
