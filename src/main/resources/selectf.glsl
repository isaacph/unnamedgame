#version 110
varying vec2 texCoord;
uniform sampler2D samplerTile;

// aspect ratio of tiles: height / width
uniform float vertScale;

// number of tiles per chunk
uniform int numTiles;

// matrix to tranform texture coordinates so that the texture is displayed normally (not skewed)
uniform mat4 texMorph;

// the width of the grid lines, as a percent of the total grid chunk size
uniform float lineWidth;

uniform vec4 fillColor;
uniform vec4 emptyColor;

float roundf(float a) {
    if(a - floor(a) > 0.5) {
        return a - floor(a) - 1.0;
    }
    return a - floor(a);
}

bool filled(int xOffset, int yOffset) {
    return int(texture2D(samplerTile, texCoord + vec2(0.5) + vec2(xOffset, yOffset) / float(numTiles)).r * 128.0) == 1;
}

float filledF(int xOffset, int yOffset) {
    return float(int(texture2D(samplerTile, texCoord + vec2(0.5) + vec2(xOffset, yOffset) / float(numTiles)).r * 128.0));
}

float remNeg(float x) {
    if(x < 0.0) {
        return 1.0;
    }
    return x;
}

void main() {
    vec4 color = emptyColor;
    float lineAdd = 0.0;
    vec2 tilePos = texCoord * float(numTiles);
    if(filled(0, 0)) {
        color = fillColor;
        lineAdd += filledF(0, 0) * max(0.0, (1.0 - min(abs(roundf(tilePos.x)), abs(roundf(tilePos.y))) / float(numTiles) / lineWidth));
    } else {
        lineAdd += filledF(-1, 0) * max(0.0, 1.0 - remNeg(roundf(tilePos.x)) / float(numTiles) / lineWidth);
        lineAdd += filledF(1, 0) * max(0.0, 1.0 - remNeg(-roundf(tilePos.x)) / float(numTiles) / lineWidth);
        lineAdd += filledF(0, -1) * max(0.0, 1.0 - remNeg(roundf(tilePos.y)) / float(numTiles) / lineWidth);
        lineAdd += filledF(0, 1) * max(0.0, 1.0 - remNeg(-roundf(tilePos.y)) / float(numTiles) / lineWidth);
    }
    gl_FragColor = color + vec4(vec3(1), lineAdd);
}