float roundedBoxSDF(vec2 CenterPosition, vec2 Size, vec4 Radius) {
    vec2 halfSize = Size;
    Radius = min(Radius, vec4(halfSize.x, halfSize.y, halfSize.x, halfSize.y));

    Radius.xy = (CenterPosition.x > 0.0) ? Radius.xy : Radius.zw;
    Radius.x  = (CenterPosition.y > 0.0) ? Radius.x  : Radius.y;

    vec2 q = abs(CenterPosition) - Size + Radius.x;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - Radius.x;
}
float rdist(vec2 pos, vec2 size, vec4 radius) {
    vec2 rxy = mix(radius.wz, radius.xy, step(0.0, pos.x));
    float r = mix(rxy.y, rxy.x, step(0.0, pos.y));

    vec2 v = abs(pos) - size + r;
    return min(max(v.x, v.y), 0.0) + length(max(v, 0.0)) - r;
}
float ralpha(vec2 size, vec2 coord, vec4 radius, float smoothness) {
    vec2 center = 0.5 * size;
    float dist = rdist(center - coord * size, center - 1.0, radius);
    return 1.0 - smoothstep(1.0 - smoothness, 1.0, dist);
}
const vec2 RECT_VERTICES_COORDS[4] = vec2[](
    vec2(0.0, 0.0),
    vec2(0.0, 1.0),
    vec2(1.0, 1.0),
    vec2(1.0, 0.0)
);

vec2 rvertexcoord(int id) {
    return RECT_VERTICES_COORDS[id & 3];
}
