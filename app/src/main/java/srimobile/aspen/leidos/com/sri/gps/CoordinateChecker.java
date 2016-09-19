package srimobile.aspen.leidos.com.sri.gps;

import android.location.Location;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CoordinateChecker {
    public static double PI = 3.14159265;
    public static double TWOPI = 2 * PI;


    private Map<String, GeoFenceData> geoFenceDataMap;

    Map<String, Map<String, ArrayList<Double>>> stations = new HashMap<String, Map<String, ArrayList<Double>>>();
    Map<String, ArrayList<Double>> location;

    public String geoFenceId;

    public CoordinateChecker() {

        location = new HashMap<String, ArrayList<Double>>();
        location.put("HB_APPROACH", new ArrayList<Double>(Arrays.asList(
                38.585312, -89.927466,
                38.585310, -89.927072,
                38.585086, -89.927077,
                38.585104, -89.927471)));
        location.put("HB_WIM", new ArrayList<Double>(Arrays.asList(
                38.585594, -89.927200,
                38.585594, -89.927074,
                38.585462, -89.927067,
                38.585468, -89.927196)));
        location.put("HB_EXIT", new ArrayList<Double>(Arrays.asList(
                38.586001, -89.927308,
                38.585999, -89.926995,
                38.585794, -89.926990,
                38.585800, -89.927301)));
        stations.put("HOMEBASE", location);

        location = new HashMap<String, ArrayList<Double>>();
        location.put("NH_APPROACH", new ArrayList<Double>(Arrays.asList(
                38.554664,-89.924700,
                38.554490,-89.924202,
                38.555319,-89.923507,
                38.555443,-89.923927)));
        location.put("NH_WIM", new ArrayList<Double>(Arrays.asList(
                38.556434,-89.925729,
                38.556196,-89.925675,
                38.556207,-89.925135,
                38.556456,-89.925123)));
        location.put("NH_EXIT", new ArrayList<Double>(Arrays.asList(
                38.556403,-89.926689,
                38.555631,-89.926681,
                38.555600,-89.926340,
                38.556445,-89.926321)));
        stations.put("NEIGHBORHOOD", location);

        location = new HashMap<String, ArrayList<Double>>();
        location.put("GLEB_APPROACH", new ArrayList<Double>(Arrays.asList(
                42.294054, -84.171814,
                42.293109, -84.171782,
                42.293149, -84.174174,
                42.294308, -84.174550)));
        location.put("GLEB_WIM", new ArrayList<Double>(Arrays.asList(
                42.294157, -84.176213,
                42.293871, -84.176240,
                42.293871, -84.177001,
                42.294419, -84.177060)));
        location.put("GLEB_EXIT", new ArrayList<Double>(Arrays.asList(
                42.293578, -84.181824,
                42.292340, -84.181813,
                42.292205, -84.183884,
                42.293165, -84.184141)));
        stations.put("GRASSLAKE_EB", location);

        location = new HashMap<String, ArrayList<Double>>();
        location.put("WF_APPROACH", new ArrayList<Double>(Arrays.asList(
                39.311803,-76.963768,
                39.311567,-76.963950,
                39.316003,-76.974637,
                39.316257,-76.974495)));
        location.put("WF_WIM", new ArrayList<Double>(Arrays.asList(
                39.317198,-76.977247,
                39.317377,-76.977177,
                39.317205,-76.976881,
                39.317088,-76.976951)));

        location.put("WF_EXIT", new ArrayList<Double>(Arrays.asList(
                39.318720,-76.981818,
                39.318507,-76.982238,
                39.320866,-76.990030,
                39.321186,-76.989949)));
        stations.put("WEST_FRIENDSHIP", location);


        location = new HashMap<String, ArrayList<Double>>();
        location.put("STOL_WIM", new ArrayList<Double>(Arrays.asList(
                38.95675, -77.15047,
                38.95698, -77.15037,
                38.95686, -77.14989,
                38.95663, -77.15002)));
        location.put("STOL_EXIT", new ArrayList<Double>(Arrays.asList(
                38.95582, -77.15094,
                38.95631, -77.15071,
                38.95615, -77.15014,
                38.95567, -77.15035)));
        stations.put("STOL", location);
    }

    public static void main(String[] args) {
        System.out.println();

        CoordinateChecker coordinateChecker = new CoordinateChecker();

//        System.out.println("" + coordinateChecker.is_valid_gps_coordinate(38.585191, -89.927376));
    }

    public String maxGps(Location location) {

        ArrayList<Double> weighStation = new ArrayList<Double>();

        Double leftMostGpsCoord = 0.0;
        Double rightMostGpsCoord = 0.0;
        Double bottomMostGpsCoord = 0.0;
        Double topMostGpsCoord = 0.0;

        boolean isGpsContained = false;

        Set<String> keys = stations.keySet();


        String weighStationName = "";
        Iterator<String> it = stations.keySet().iterator();
        while(it.hasNext()){
            String stationName = it.next();
            Map<String, ArrayList<Double>> station = stations.get(stationName);

            boolean start = true;
            for (String gate : station.keySet()) {

                ArrayList<Double> gpsValues = station.get(gate);

                for (int x = 0; x < gpsValues.size(); x++) {

                    if (x == 0 && start) {
                        rightMostGpsCoord = gpsValues.get(x);
                        leftMostGpsCoord = gpsValues.get(x);
                    }

                    if (x == 1 && start) {
                        topMostGpsCoord = gpsValues.get(x);
                        bottomMostGpsCoord = gpsValues.get(x);
                        start = false;
                    }

                    if (x % 2 == 0) {

                        Double lat = gpsValues.get(x);
                        if (lat < leftMostGpsCoord) {
                            leftMostGpsCoord = lat;
                        }

                        if (lat > rightMostGpsCoord) {
                            rightMostGpsCoord = lat;
                        }

                    } else {
                        Double lng = gpsValues.get(x);
                        if (lng > topMostGpsCoord) {
                            topMostGpsCoord = lng;
                        }

                        if (lng < bottomMostGpsCoord) {
                            bottomMostGpsCoord = lng;
                        }
                    }


                }
                isGpsContained = true;

            }

            ArrayList<Double> latt_array = new ArrayList<Double>();
            latt_array.add(leftMostGpsCoord);
            latt_array.add(rightMostGpsCoord);

            ArrayList<Double> long_array = new ArrayList<Double>();
            long_array.add(topMostGpsCoord);
            long_array.add(bottomMostGpsCoord);

            int n = latt_array.size();
            double angle = 0;

            angle = 0;

            latt_array.clear();
            latt_array.add(rightMostGpsCoord);
            latt_array.add(rightMostGpsCoord);
            latt_array.add(leftMostGpsCoord);
            latt_array.add(leftMostGpsCoord);


            long_array.clear();
            long_array.add(rightMostGpsCoord);
            long_array.add(rightMostGpsCoord);
            long_array.add(leftMostGpsCoord);
            long_array.add(leftMostGpsCoord);



//                Convert the strings to doubles.
//            for (int count = 0; count < latt_array.size(); count++) {
//                if (count % 2 == 0) {
//                    latt_array.add(latt_array.get(count));
//                } else {
//                    long_array.add(long_array.get(count));
//                }
//            }


            double point1_lat;
            double point1_long;
            double point2_lat;
            double point2_long;

            for (int i = 0; i < n; i++) {
                point1_lat = latt_array.get(i) - location.getLatitude();
                point1_long = long_array.get(i) - location.getLatitude();
                point2_lat = latt_array.get((i + 1) % n) - location.getLatitude();
                // you should have paid more attention in high school geometry.
                point2_long = long_array.get((i + 1) % n) - location.getLongitude();
                angle += Angle2D(point1_lat, point1_long, point2_lat, point2_long);
            }

            if (!isGpsContained && Math.abs(angle) > PI) {
                weighStationName = stationName;
                isGpsContained = true;
            }
        }

        return weighStationName;
    }


    public static double Angle2D(double y1, double x1, double y2, double x2) {
        double dtheta, theta1, theta2;

        theta1 = Math.atan2(y1, x1);
        theta2 = Math.atan2(y2, x2);
        dtheta = theta2 - theta1;
        while (dtheta > PI)
            dtheta -= TWOPI;
        while (dtheta < -PI)
            dtheta += TWOPI;

        return (dtheta);
    }

    public static boolean is_valid_gps_coordinate(double latitude,
                                                  double longitude) {
        // This is a bonus function, it's unused, to reject invalid lat/longs.
        if (latitude > -90 && latitude < 90 && longitude > -180
                && longitude < 180) {
            return true;
        }
        return false;
    }

    public String weighStationName_name_coordinate(double latitude,
                                          double longitude) {

        String gpsLocation = null;
        int i;
        double angle = 0;
        double point1_lat;
        double point1_long;
        double point2_lat;
        double point2_long;

        String stationName = "";

        ArrayList<Double> latt_array = new ArrayList<Double>();
        ArrayList<Double> long_array = new ArrayList<Double>();

                boolean isGpsContained = false;
                for (String weighStationName : stations.keySet()) { // NUMBER OF STATIONS  - 1 KEYS 'OFALLON'

                    Map<String, ArrayList<Double>> station = stations.get(weighStationName);

                    for (String gate : station.keySet()) {

                        ArrayList<Double> gpsValues = station.get(gate);

                        latt_array.clear();
                        long_array.clear();

    //                Convert the strings to doubles.
                    for (int count = 0; count < gpsValues.size(); count++) {
                        if (count % 2 == 0) {
                            latt_array.add(gpsValues.get(count));
                        } else {
                            long_array.add(gpsValues.get(count));
                        }
                    }

                int n = latt_array.size();
                angle = 0;

                for (i = 0; i < n; i++) {
                    point1_lat = latt_array.get(i) - latitude;
                    point1_long = long_array.get(i) - longitude;
                    point2_lat = latt_array.get((i + 1) % n) - latitude;
                    // you should have paid more attention in high school geometry.
                    point2_long = long_array.get((i + 1) % n) - longitude;
                    angle += Angle2D(point1_lat, point1_long, point2_lat, point2_long);
                }

                if (!isGpsContained  && Math.abs(angle) > PI) {
                    stationName = weighStationName;
                    isGpsContained = true;
                }

            }

//            Log.d("KEY", weighStationName);
//                Log.d("GATES", gate);
//                Log.d("KEY", gpsValues.toString());
//            Log.d("LATT", "" + latt_array.toString());
//            Log.d("LONG", "" + long_array.toString());

        }

        return stationName;
    }

    public String gate_name_coordinate(double latitude,
                                          double longitude) {

        String gpsLocation = null;
        int i;
        double angle = 0;
        double point1_lat;
        double point1_long;
        double point2_lat;
        double point2_long;

        String gpsGateName = "";

        ArrayList<Double> latt_array = new ArrayList<Double>();
        ArrayList<Double> long_array = new ArrayList<Double>();

        // NUMBER OF STATIONS  - 1 KEYS 'OFALLON'
        boolean isGpsContained = false;
        for (String weighStationName : stations.keySet()) {

            Map<String, ArrayList<Double>> station = stations.get(weighStationName);

            for (String gate : station.keySet()) {

                ArrayList<Double> gpsValues = station.get(gate);

                latt_array.clear();
                long_array.clear();

                // Convert the strings to doubles.
                for (int count = 0; count < gpsValues.size(); count++) {
                    if (count % 2 == 0) {
                        latt_array.add(gpsValues.get(count));
                    } else {
                        long_array.add(gpsValues.get(count));
                    }
                }

                int n = latt_array.size();
                angle = 0;

                for (i = 0; i < n; i++) {
                    point1_lat = latt_array.get(i) - latitude;
                    point1_long = long_array.get(i) - longitude;
                    point2_lat = latt_array.get((i + 1) % n) - latitude;
                    // you should have paid more attention in high school geometry.
                    point2_long = long_array.get((i + 1) % n) - longitude;
                    angle += Angle2D(point1_lat, point1_long, point2_lat, point2_long);
                }

                if (!isGpsContained  && Math.abs(angle) > PI) {
                    isGpsContained = true;
                    gpsGateName = gate;
                }

            }

//            Log.d("KEY", weighStationName);
//            Log.d("LATT", "" + latt_array.toString());
//            Log.d("LONG", "" + long_array.toString());
        }

        return gpsGateName;
    }


    public boolean isGpsContained(double latitude, double longitude) {

        int i;
        double angle = 0;
        double point1_lat;
        double point1_long;
        double point2_lat;
        double point2_long;

        String gateEntered = "";

        ArrayList<Double> latt_array = new ArrayList<Double>();
        ArrayList<Double> long_array = new ArrayList<Double>();

        boolean isGpsContained = false;
        for (String key : stations.keySet()) { // NUMBER OF STATIONS  - 1 KEYS 'OFALLON'

            Map<String, ArrayList<Double>> station = stations.get(key);

            for (String gate : station.keySet()) {

                ArrayList<Double> gpsValues = station.get(gate);

                latt_array.clear();
                long_array.clear();

//                Convert the strings to doubles.
                for (int count = 0; count < gpsValues.size(); count++) {
                    if (count % 2 == 0) {
                        latt_array.add(gpsValues.get(count));
                    } else {
                        long_array.add(gpsValues.get(count));
                    }
                }

                int n = latt_array.size();
                angle = 0;

                for (i = 0; i < n; i++) {
                    point1_lat = latt_array.get(i) - latitude;
                    point1_long = long_array.get(i) - longitude;
                    point2_lat = latt_array.get((i + 1) % n) - latitude;
                    // you should have paid more attention in high school geometry.
                    point2_long = long_array.get((i + 1) % n) - longitude;
                    angle += Angle2D(point1_lat, point1_long, point2_lat, point2_long);
                }

                if (!isGpsContained  && Math.abs(angle) < PI) {
                    isGpsContained = false;
                } else {
//                    Log.d("GATE", gate);
                    geoFenceId = gate;
                    return true;
                }
            }

//            Log.d("KEY", key);
//            Log.d("LATT", "" + latt_array.toString());
//            Log.d("LONG", "" + long_array.toString());

//            if (geoFenceId == null || "".equals(geoFenceId)) {
//                geoFenceId = "Test Case 1";
//            } else if ("WEIGH_STATION".equals(geoFenceId)) {
//                geoFenceId = "Test Case 3";
//            } else if ("WIM".equals(geoFenceId)) {
//                geoFenceId = "Test Case 5";
//            } else if ("EXIT".equals(geoFenceId)) {
//                geoFenceId = "Test Case 7";
//            }
        }
        return isGpsContained;
    }

    private class GeoFenceData {
        private String name;
        private List<Double> latList;
        private List<Double> longList;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Double> getLatList() {
            return latList;
        }

        public void setLatList(List<Double> latList) {
            this.latList = latList;
        }

        public List<Double> getLongList() {
            return longList;
        }

        public void setLongList(List<Double> longList) {
            this.longList = longList;
        }
    }
}