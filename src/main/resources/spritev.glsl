#version 330 core
in vec2 position;
in vec2 texture;
uniform mat4 matrix;
out vec2 texCoord;
uniform vec2 spritePos;
uniform vec2 spriteFrame;

void main() {
    gl_Position = matrix * vec4(position, 0, 1);
    texCoord = spritePos + vec2(texture.x * spriteFrame.x, texture.y * spriteFrame.y);
}