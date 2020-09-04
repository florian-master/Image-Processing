#pragma version(1)
#pragma rs_fp_relaxed
#pragma rs java_package_name(fr.ubordeaux.rlasvenes.projettechandroid)

uchar4 RS_KERNEL invert(uchar4 in) {
  uchar4 out = in;
  out.r = 255 - in.r;
  out.g = 255 - in.g;
  out.b = 255 - in.b;
  return out;
}