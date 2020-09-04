#pragma version (1)
#pragma rs java_package_name(fr.ubordeaux.rlasvenes.projettechandroid)

uchar4 RS_KERNEL cartoonize(uchar4 in) {
    //converting to hsv
    float r_float = in.r/255.0f;
    float g_float = in.g/255.0f;
    float b_float = in.b/255.0f;
    float min_rgb = min(r_float, min(g_float, b_float));
    float max_rgb = max(r_float, max(g_float, b_float));
    float delta_rgb = max_rgb - min_rgb;
    float h;
    float s;
    float v;
    if (max_rgb == 0.0f) {
        s = 0.0f;
    }
    else {
        s = delta_rgb / max_rgb;
    }
    v = max_rgb;
    if (delta_rgb == 0.0f) {
        h = 0.0f;
    }
    else {
        if (max_rgb == r_float) {
            h = (g_float - b_float)/delta_rgb;
            h = fmod(h, 6.0f);
            if (h < 0.0f) {
                h = h + 6.0f;
            }
            h = 60.0f*h;
        }
        else {
            if (max_rgb == g_float) {
                h = (b_float - r_float)/delta_rgb;
                h = 60.0f*(h + 2.0f);
            }
            else {
                h = (r_float - g_float)/delta_rgb;
                h = 60.0f*(h + 4.0f);
            }
        }
    }
    //modifying
    s = s + 0.1f;
    if (s > 1.0f) {
        s = 1.0f;
    }
    if (v < 0.25f) {
        v = 0.0f;
    }
    else {
        if (v < 0.5f) {
            v = 0.25f;
        }
        else {
            if (v < 0.75f) {
                v = 0.75f;
            }
            else {
                v = 1.0f;
            }
        }
    }
    //converting to rgb
    float c = v*s;
    float x = h/60.0f;
    x = fmod(x, 2.0f) - 1.0f;
    x = fabs(x);
    x = c*(1.0f - x);
    float m = v - c;
    if (h < 60.0f) {
        r_float = c;
        g_float = x;
        b_float = 0.0f;
    }
    else {
        if (h < 120.0f) {
            r_float = x;
            g_float = c;
            b_float = 0.0f;
        }
        else {
            if (h < 180.0f) {
                r_float = 0.0f;
                g_float = c;
                b_float = x;
            }
            else {
                if (h < 240.0f) {
                    r_float = 0.0f;
                    g_float = x;
                    b_float = c;
                }
                else {
                    if (h < 300.0f) {
                        r_float = x;
                        g_float = 0.0f;
                        b_float = c;
                    }
                    else {
                        r_float = c;
                        g_float = 0.0f;
                        b_float = x;
                    }
                }
            }
        }
    }
    in.r = (uchar) ((r_float + m)*255.0f);
    in.g = (uchar) ((g_float + m)*255.0f);
    in.b = (uchar) ((b_float + m)*255.0f);
    return in;
}
