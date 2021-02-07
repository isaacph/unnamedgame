#version 110
varying vec2 texCoord;
uniform sampler2D sampler;
uniform vec4 color;
void main() {
    gl_FragColor = color * texture2D(sampler, texCoord);
}