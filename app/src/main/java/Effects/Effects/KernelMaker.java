package Effects;

public class KernelMaker {

    /**
     * Average Blur kernel
     * @param size the kernel [size]*[size] (size must be an odd value)
     * @return the kernel array
     */
    public static float [] AverageBlurKernel(int size) {
        // Check if it's an even number
        if (size%2 == 0 || size < 3)
            return null;

        // Creating the array with the appropriate size
        float [] tab = new float [size*size];

        // Fill the array
        for (int i=0; i<size*size; i++)
            tab[i] = 1;

        return tab;
    }

    /**
     * Gaussian Blur kernel
     * @param size the kernel [size]*[size] (size must be an odd value)
     * @param sigma the sigma value
     * @return the kernel array
     */
    public static float [] GaussianBlurKernel(int size, double sigma) {
        // Check if it's an even number
        if (size%2 == 0 || size < 3)
            return null;

        // Creating the array with the appropriate size
        float [] tab = new float [size*size];

        // Fill the array
        int y=-size/2;
        for (int j=0; j < size; j++) {
            int x=-size/2;
            for (int i=0; i<size; i++) {
                tab[(size*j)+i]= 10* (float) (Math.exp(-(Math.pow(x, 2) + Math.pow(y,2))/(2*Math.pow(sigma, 2))));
                x++;
            }
            y++;
        }

        return tab;
    }

    /**
     * Sobel kernel
     * @return the kernel array
     */
    public static float[][] SobelKernel  () {
        return new float[][]{
                {1.0f, 2.0f, 1.0f,
                0.0f, 0.0f, 0.0f,
                -1.0f, -2.0f, -1.0f
        }, {
                -1.0f, 0.0f, 1.0f,
                -2.0f, 0.0f, 2.0f,
                -1.0f, 0.0f, 1.0f}
        };
    }

    /**
     * Prewitt kernel
     * @return the kernel array
     */
    public static float[][] PrewittKernel () {
        return new float[][] {
                {-1.0f, 0.0f, 1.0f,
                 -1.0f, 0.0f, 1.0f,
                 -1.0f, 0.0f, 1.0f
        },{
                -1.0f, -1.0f, -1.0f,
                 0.0f, 0.0f, 0.0f,
                 1.0f, 1.0f, 1.0f}
        };
    }

    /**
     * Laplace kernel cx4
     * @return the kernel array
     */
    public static float[] LaplaceKernel_cx4 () {
        return new float[]{
                0.0f, 1.0f, 0.0f,
                1.0f, -4.0f, 1.0f,
                0.0f, 1.0f, 0.0f
        };
    }

    /**
     * Laplace kernel cx8
     * @return the kernel array
     */
    public static float[] LaplaceKernel_cx8 () {
        return new float[]{
                1.0f, 1.0f, 1.0f,
                1.0f, -8.0f, 1.0f,
                1.0f, 1.0f, 1.0f
        };
    }

    /**
     * Emboss kernel
     * @return the kernel array
     */
    public static float[] EmbossKernel () {
        return new float[] {
                -2.0f, -1.0f, 0.0f,
                -1.0f, 1.0f, 1.0f,
                 0.0f, 1.0f, 2.0f
        };
    }
}
