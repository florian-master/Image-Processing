#pragma version(1)
#pragma rs_fp_relaxed
#pragma rs java_package_name(fr.ubordeaux.rlasvenes.projettechandroid)

float value;

uchar4 RS_KERNEL posterize ( uchar4 in) {
    float4 out = rsUnpackColor8888(in);

    out.r = clamp((out.r) - fmod(out.r, value), 0, 255);
    out.g = clamp((out.g) - fmod(out.g, value), 0, 255);
    out.b = clamp((out.b) - fmod(out.b, value), 0, 255);

    return rsPackColorTo8888(out.r, out.g, out.b, out.a);
}