#version 330

in vec2 texCoord0;
out vec4 fragColor;

const float HOLE_HALF_SIZE = 0.0025;

void main() {
    vec2 planeUv = fract(texCoord0);
    vec2 distanceFromCenter = abs(planeUv - vec2(0.5));
    if (distanceFromCenter.x < HOLE_HALF_SIZE && distanceFromCenter.y < HOLE_HALF_SIZE) {
        discard;
    }

    fragColor = vec4(0.0, 0.0, 0.0, 1.0);
}
