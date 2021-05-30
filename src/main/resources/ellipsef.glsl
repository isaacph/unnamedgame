#version 110
varying vec2 texCoord;
uniform vec2 axes;
uniform vec4 color;
void main() {
    vec2 pos = (texCoord - vec2(0.5)) * 2.0;
    if(pos.x * pos.x / axes.x * axes.x + pos.y * pos.y / axes.y * axes.y <= 1.0) {
        gl_FragColor = color;
    } else {
        discard;
    }
}