#pragma version(1)
#pragma rs_fp_relaxed
#pragma rs java_package_name(fr.ubordeaux.rlasvenes.projettechandroid)

static const float4 weight = {0.299f, 0.587f, 0.114f, 0.0f};

uchar4 RS_KERNEL toGreyAdv ( uchar4 in) {
    const float4 pixelf = rsUnpackColor8888 (in);
    const float grey = dot(pixelf, weight);
    return rsPackColorTo8888 (grey , grey , grey , pixelf .a);
}

