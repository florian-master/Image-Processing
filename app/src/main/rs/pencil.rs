#pragma version (1)
#pragma rs java_package_name(fr.ubordeaux.rlasvenes.projettechandroid)

static const float4 weight = {0.299f, 0.587f, 0.114f, 0.0f};

uchar4 RS_KERNEL draw(uchar4 in) {
    const float4 pixelf = rsUnpackColor8888(in);
    float grey = dot(pixelf, weight);
    if (grey < 0.25f) {
        grey = 0.0f;
    }
    else {
        if (grey < 0.5f) {
            grey = 0.25f;
        }
        else {
            if (grey < 0.75f) {
                grey = 0.75f;
            }
            else {
                grey = 1.0f;
            }
        }
    }
    return rsPackColorTo8888(grey, grey, grey, pixelf.a);
}
