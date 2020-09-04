#pragma version (1)
#pragma rs java_package_name(fr.ubordeaux.rlasvenes.projettechandroid)

float hue; //hue is set by the java code
static const float4 weight = {0.299f, 0.587f, 0.114f, 0.0f};

uchar4 RS_KERNEL keep_color(uchar4 in) {
    float r = in.r/255.0f; //converting in float
    float g = in.g/255.0f;
    float b = in.b/255.0f;
    float max_rgb = max(r, max(g, b)); //maximum intensity of the rgb colors
    float min_rgb = min(r, min(g, b)); //minimum
    float delta_rgb = max_rgb - min_rgb;
    float h; //kernel hue
    if (delta_rgb == 0.0f) {
        h = 0.0f;
    }
    else {
        if (max_rgb == r) {
            h = (g - b)/delta_rgb;
            h = fmod(h, 6.0f);
            if (h < 0.0f) {
                h = h + 6.0f;
            }
            h = 60.0f*h;
        }
        else {
            if (max_rgb == g) {
                h = (b - r)/delta_rgb;
                h = 60.0f*(h + 2.0f);
            }
            else {
                if (max_rgb == b) {
                    h = (r - g)/delta_rgb;
                    h = 60.0f*(h + 4.0f);
                }
            }
        }
    }
    float min_hue = hue - 25.0f; //minimum hue to keep the kernel
    float max_hue = hue + 25.0f; //maximum
    bool keep = false;  //true if keeping it
    if (min_hue < 0.0f) {
        min_hue = min_hue + 360.0f;
        if (h < max_hue || h > min_hue) {
            keep = true;
        }
    }
    else {
        if (max_hue >= 360.0f) {
            max_hue = max_hue - 360.0f;
            if (h < max_hue || h > min_hue) {
                keep = true;
            }
        }
        else {
            if (h < max_hue && h > min_hue) {
                keep = true;
            }
        }
    }
    if (keep == false) {
        const float4 pixelf = rsUnpackColor8888(in); //grey method
        const float grey = dot(pixelf, weight);
        return rsPackColorTo8888(grey, grey, grey, pixelf.a);
    }
    else { //no change for the kernel
        return in;
    }
}
