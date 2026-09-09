#version 150

#moj_import <minecraft:fog.glsl>

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

const float EDGE_FADE_WIDTH = 0.10;

vec4 sampleClamped(vec2 uv) {
    return texture(Sampler0, clamp(uv, vec2(0.0), vec2(1.0)));
}

vec4 blurredImage(vec2 uv) {
    vec2 texel = 1.0 / vec2(textureSize(Sampler0, 0));
    vec4 color = sampleClamped(uv) * 4.0;
    color += sampleClamped(uv + vec2( texel.x, 0.0)) * 2.0;
    color += sampleClamped(uv + vec2(-texel.x, 0.0)) * 2.0;
    color += sampleClamped(uv + vec2(0.0,  texel.y)) * 2.0;
    color += sampleClamped(uv + vec2(0.0, -texel.y)) * 2.0;
    color += sampleClamped(uv + vec2( texel.x,  texel.y));
    color += sampleClamped(uv + vec2(-texel.x,  texel.y));
    color += sampleClamped(uv + vec2( texel.x, -texel.y));
    color += sampleClamped(uv + vec2(-texel.x, -texel.y));
    return color / 16.0;
}

void main() {
    vec2 planeUv = fract(texCoord0);
    vec4 color = blurredImage(planeUv);
    vec2 edgeDistance = min(planeUv, vec2(1.0) - planeUv);
    vec2 edgeFadeByAxis = smoothstep(vec2(0.0), vec2(EDGE_FADE_WIDTH), edgeDistance);
    float edgeFade = edgeFadeByAxis.x * edgeFadeByAxis.y;
    color *= vertexColor * ColorModulator;
    color.a *= edgeFade;
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
