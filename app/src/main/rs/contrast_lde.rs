#pragma version (1)
#pragma rs java_package_name(fr.ubordeaux.rlasvenes.projettechandroid)

int lut_red[256];
int lut_green[256];
int lut_blue[256];

uchar4 RS_KERNEL dynamic_extension(uchar4 in) {
    uchar red = in.r;
    uchar green = in.g;
    uchar blue = in.b;
    in.r = (uchar) lut_red[red];
    in.g = (uchar) lut_green[green];
    in.b = (uchar) lut_blue[blue];
    return in;
}
