#version 110
uniform mat4 matrix;
attribute vec2 position;
void main()
{
    gl_Position = matrix * vec4(position, 0.0, 1.0);
}