#pragma version(1)
#pragma rs_fp_full
#pragma rs java_package_name(fr.ubordeaux.rlasvenes.projettechandroid)

rs_allocation input;

int width;
int height;

float* kmatrix;
float kdiv;
int ksize;


uchar4 RS_KERNEL apply_convolution (uchar4 in,  uint32_t x, uint32_t y) {

    int kmid = (ksize-1)/2;
    // Check if we are near the border
    if(x < kmid || y < kmid)
        return in;

    if((x > width - ksize) || (y > height - ksize))
        return in;

    // Check for division by zero
    if (kdiv <= 0)
        kdiv = 1;

    float4 pixel;
    float r=0, g=0, b=0;
    int kx,ky;

    for(kx = 0; kx < ksize; kx++){
        for(ky = 0; ky < ksize; ky++){
            // get the pixel at the current position
            pixel = rsUnpackColor8888(rsGetElementAt_uchar4(input,(kx+x) - (ksize/2), (ky+y) - (ksize/2)));

            // Compute the sum
            r += (pixel.r) * kmatrix[kx + (ksize * ky)];
            g += (pixel.g) * kmatrix[kx + (ksize * ky)];
            b += (pixel.b) * kmatrix[kx + (ksize * ky)];
        }
    }
    // Check in range
    pixel.r = clamp((r/kdiv), 0, 255);
    pixel.g = clamp((g/kdiv), 0, 255);
    pixel.b = clamp((b/kdiv), 0, 255);
    pixel.a = 1;

    return rsPackColorTo8888(pixel);
}