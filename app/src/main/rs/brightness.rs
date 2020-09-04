#pragma version(1)
#pragma rs java_package_name(fr.ubordeaux.rlasvenes.projettechandroid)

#include "rs_debug.rsh"

float brightness_offset;

uchar4 RS_KERNEL brightness(uchar4 in) {
    uchar4 out = in;

    out.r = clamp(out.r + brightness_offset, 0, 255);
    out.g = clamp(out.g + brightness_offset, 0, 255);
    out.b = clamp(out.b + brightness_offset, 0, 255);

    return out;
}

