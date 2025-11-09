#version 150

#moj_import <exosware:common.glsl>

in vec2 FragCoord;
in vec2 TexCoord;
in vec4 FragColor;

uniform sampler2D Sampler0;
uniform vec2 Size;
uniform vec4 Radius;
uniform float Smoothness;

out vec4 OutColor;

void main() {
    float alpha = ralpha(Size, FragCoord, Radius, Smoothness);

    vec4 texColor = texture(Sampler0, TexCoord) * FragColor;

    texColor.a *= alpha;

    if (texColor.a < 0.001) discard;

    OutColor = texColor;
}
