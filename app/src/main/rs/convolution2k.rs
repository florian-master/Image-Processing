#pragma version(1)
#pragma rs_fp_full
#pragma rs java_package_name(fr.ubordeaux.rlasvenes.projettechandroid)

rs_allocation input;

int width;
int height;

float* kmatrix1;
float* kmatrix2;
float kdiv;
int ksize;

uchar4 RS_KERNEL apply_convolution2k (uchar4 in,  uint32_t x, uint32_t y) {

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
    float r1=0, g1=0, b1=0, r2=0, g2=0, b2=0;
    for(int kx = 0; kx < ksize; kx++){
       for(int ky = 0; ky < ksize; ky++){
            // get the pixel at the current position
            pixel =  rsUnpackColor8888( rsGetElementAt_uchar4(input, (kx+x), (ky+y)));
            // Compute the sum
            r1 += (pixel.r) * kmatrix1[kx + (ksize * ky)];
            g1 += (pixel.g) * kmatrix1[kx + (ksize * ky)];
            b1 += (pixel.b) * kmatrix1[kx + (ksize * ky)];

            r2 += (pixel.r) * kmatrix2[kx + (ksize * ky)];
            g2 += (pixel.g) * kmatrix2[kx + (ksize * ky)];
            b2 += (pixel.b) * kmatrix2[kx + (ksize * ky)];

        }
    }
    // Check in range and compute the module
    pixel.r = clamp((hypot(r1,r2)/kdiv), 0, 255);
    pixel.g = clamp((hypot(g1,g2)/kdiv), 0, 255);
    pixel.b = clamp((hypot(b1,b2)/kdiv), 0, 255);
    pixel.a = 1.0f;

    return rsPackColorTo8888(pixel);
}