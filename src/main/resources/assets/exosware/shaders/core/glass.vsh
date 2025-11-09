#version 150

in vec4 Position;
in vec2 UV0;

out vec2 vertexPos;

void main() {
    gl_Position = Position;
    vertexPos = UV0;
}