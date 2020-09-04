#pragma version (1)
#pragma rs java_package_name(fr.ubordeaux.rlasvenes.projettechandroid)


float saturation;

void root(const uchar4* v_in, uchar4* v_out) {
    float4 current = rsUnpackColor8888(*v_in);

    float Pr = 0.299f;
    float Pg = 0.587f;
    float Pb = 0.114f;

    float comp = (current.r)*(current.r)*Pr+
                  (current.g)*(current.g)*Pg+
                  (current.b)*(current.b)*Pb;

    float  P=sqrt(comp) ;

    current.r = clamp(P+(((current.r)-P)*saturation), 0, 255);
    current.g = clamp(P+(((current.g)-P)*saturation), 0, 255);
    current.b = clamp(P+(((current.b)-P)*saturation), 0, 255);

    *v_out = rsPackColorTo8888(current.r, current.g, current.b, current.a);
}
