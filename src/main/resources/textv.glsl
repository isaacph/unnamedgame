#version 110
attribute vec2 position;
attribute vec2 tex;
varying vec2 texCoord;
uniform mat4 matrix;
void main() {
    gl_Position = matrix * vec4(position, 0.0, 1.0);
    texCoord = tex;
}