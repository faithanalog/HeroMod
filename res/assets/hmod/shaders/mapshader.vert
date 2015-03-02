#version 120

attribute vec2 a_pos;
attribute vec2 a_tex;

varying vec2 v_tex;

void main() {
    gl_Position = gl_ModelViewProjectionMatrix * vec4(a_pos, 0.0, 1.0);
    v_tex = a_tex;
}