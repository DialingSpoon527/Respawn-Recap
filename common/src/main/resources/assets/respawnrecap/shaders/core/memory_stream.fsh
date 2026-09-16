#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:fog.glsl>
#include <minecraft:dynamictransforms.glsl>

uniform sampler2D Sampler0;

layout(location = 0) in float sphericalVertexDistance;
layout(location = 1) in float cylindricalVertexDistance;
layout(location = 2) in vec4 vertexColor;
layout(location = 3) in vec2 texCoord0;

layout(location = 0) out vec4 fragColor;

const float BAND_WIDTH = 0.03;
const float BAND_SPACING = 0.38;
const vec3 GLOW_COLOR = vec3(0.243137, 0.290196, 1.0);

float cyclicDistance(float value, float center) {
    float distance = abs(value - center);
    return min(distance, 1.0 - distance);
}

float revealStrength(float v, float scroll) {
    float strongest = 0.0;
    for (int i = 0; i < 5; ++i) {
        float center = fract(scroll + float(i) * BAND_SPACING);
        float strength = smoothstep(BAND_WIDTH, 0.0, cyclicDistance(v, center));
        strongest = max(strongest, strength);
    }
    return strongest;
}

void main() {
    vec4 source = texture(Sampler0, texCoord0);
    float scroll = (TextureMat * vec4(0.0, 0.0, 0.0, 1.0)).y;
    float alpha = source.a * revealStrength(fract(texCoord0.y), scroll);
    if (alpha <= 0.001) {
        discard;
    }

    vec4 color = vec4(GLOW_COLOR, alpha) * vertexColor * ColorModulator;
    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}
