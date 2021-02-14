#version 110
varying vec2 texCoord;
uniform sampler2D samplerTile;
uniform sampler2D sampler1;
uniform sampler2D sampler2;
uniform vec4 color;
uniform float vertScale;
uniform int numTiles;
uniform mat4 texMorph;
uniform float lineWidth;
uniform float yOffset;

float roundf(float a) {
    if(a - floor(a) > 0.5) {
        return a - floor(a) - 1.0;
    }
    return a - floor(a);
}

void main() {
    vec4 c = vec4(0);
    float tileScale = 1.0 / float(numTiles);
    vec2 tilePos = vec2(texCoord.x / tileScale, texCoord.y / tileScale);
    float dist = min(abs(roundf(tilePos.x)), abs(roundf(tilePos.y)));
    c = vec4(vec3(max(0.0, 1.0 - dist / float(numTiles) / lineWidth)), 0.0);
    vec2 tc = (texMorph * vec4(texCoord, 0, 1)).xy + vec2(0, yOffset);
    int value = int(texture2D(samplerTile, texCoord + vec2(0.5)).r * 128.0);
    vec4 tex = vec4(0);
    if(value == 0) {
        tex = texture2D(sampler1, tc);
    } else if(value == 1) {
        tex = texture2D(sampler2, tc);
    }
    gl_FragColor = c + color * tex;
}