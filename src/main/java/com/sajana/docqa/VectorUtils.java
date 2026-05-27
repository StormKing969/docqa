package com.sajana.docqa;

public final class VectorUtils {
    private VectorUtils() {}

    /**
     * Converts a float array to the pgvector string format: [0.1,0.2,0.3,...]
     */
    public static String toVectorString(float[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(vector[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}