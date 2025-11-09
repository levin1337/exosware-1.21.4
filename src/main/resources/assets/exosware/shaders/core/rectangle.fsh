#version 150

#moj_import <exosware:common.glsl>

in vec2 FragCoord;
in vec4 FragColor;

uniform vec2 Size;
uniform vec4 Radius;
uniform float Smoothness;

out vec4 OutColor;

void main() {
    float alpha = ralpha(Size, FragCoord, Radius, Smoothness) * FragColor.a;

    if (alpha < 0.001) discard;

    OutColor = vec4(FragColor.rgb, alpha);
}
