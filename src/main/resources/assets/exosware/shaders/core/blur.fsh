#version 150

#moj_import <exosware:common.glsl>

in vec2 FragCoord;
in vec2 TexCoord;
in vec4 FragColor;

uniform sampler2D Sampler0;
uniform vec2 Size;
uniform vec4 Radius;
uniform float Smoothness;
uniform float BlurRadius;

out vec4 OutColor;

const float DPI = 6.28318530718;
const int STEPS = 16;
const int RADIAL_SAMPLES = 5;

void main() {
    vec2 texSize = vec2(textureSize(Sampler0, 0));
    vec2 multiplier = BlurRadius / texSize;

    vec3 average = texture(Sampler0, TexCoord).rgb;

    for (int j = 0; j < STEPS; j++) {
        float angle = float(j) * DPI / float(STEPS);
        vec2 dir = vec2(cos(angle), sin(angle));
        for (int i = 1; i <= RADIAL_SAMPLES; i++) {
            float f = float(i) / float(RADIAL_SAMPLES);
            average += texture(Sampler0, TexCoord + dir * multiplier * f).rgb;
        }
    }


    average /= float(STEPS * RADIAL_SAMPLES + 1);

    vec4 color = vec4(average, 1.0) * FragColor;
    color.a *= ralpha(Size, FragCoord, Radius, Smoothness);

    if (color.a < 0.001) discard;

    OutColor = color;
}
