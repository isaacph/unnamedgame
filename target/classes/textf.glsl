#version 110
varying vec2 texCoord;
uniform sampler2D sampler;
uniform vec4 color;
void main() {
    float f = texture2D(sampler, texCoord).r;
    gl_FragColor = color * vec4(f);
}