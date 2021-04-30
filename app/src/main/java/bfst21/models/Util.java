package bfst21.models;


public class Util {

    /**
     * Calculate the distance between 2 float array coordinates.
     */
    public static double distTo(float[] fromCoords, float[] toCoords) {
        return distTo(fromCoords[0], fromCoords[1], toCoords[0], toCoords[1]);
    }

    /**
     * Calculate the distance between 2 pairs of longitude and latitude values.
     * This is calculated using the Haversine formula.
     */
    public static double distTo(double lon1, double lat1, double lon2, double lat2) {
        lat1 = -lat1 * 0.56f;
        lat2 = -lat2 * 0.56f;

        int radius = 6371; //Radius of Earth

        double rLatDistance = Math.toRadians(lat2 - lat1);
        double rLonDistance = Math.toRadians(lon2 - lon1);

        double a = (Math.sin(rLatDistance / 2) * Math.sin(rLatDistance / 2)) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(rLonDistance / 2) * Math.sin(rLonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return radius * c;
    }

    public static void resizeArray(int[] input, int newSize) {
        int[] copy = new int[newSize];

        for (int i = 0; i < input.length; i++) {
            copy[i] = input[i];
        }
        input = copy;
    }
}
