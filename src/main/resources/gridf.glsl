#version 110
varying vec2 texCoord;
uniform sampler2D samplerTile;
uniform sampler2D sampler1;
uniform sampler2D sampler2;

// aspect ratio of tiles: height / width
uniform float vertScale;

// number of tiles per chunk
uniform int numTiles;

// matrix to tranform texture coordinates so that the texture is displayed normally (not skewed)
uniform mat4 texMorph;

// the width of the grid lines, as a percent of the total grid chunk size
uniform float lineWidth;

// the displacement of the texture
uniform vec2 textureOffset;

uniform float textureScale;

uniform vec4 color;

float roundf(float a) {
    if(a - floor(a) > 0.5) {
        return a - floor(a) - 1.0;
    }
    return a - floor(a);
}

void main() {

    // add lines
    float tileScale = 1.0 / float(numTiles);
    vec2 tilePos = vec2(texCoord.x / tileScale, texCoord.y / tileScale);
    float dist = min(abs(roundf(tilePos.x)), abs(roundf(tilePos.y)));
    vec4 lineAdd = vec4(vec3(max(0.0, 1.0 - dist / float(numTiles) / lineWidth)), 0.0);

    // add varying textures
    vec2 tc = (texMorph * vec4(texCoord, 0, 1)).xy + textureOffset;
    int value = int(texture2D(samplerTile, texCoord + vec2(0.5)).r * 128.0);
    vec4 tex = vec4(0);
    if(value == 0) {
        tex = texture2D(sampler1, tc * textureScale);
    } else if(value == 1) {
        tex = texture2D(sampler2, tc * textureScale);
    }

    gl_FragColor = lineAdd + color * tex;
}