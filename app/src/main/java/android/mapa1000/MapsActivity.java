package android.mapa1000;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

//import android.preference.PreferenceManager;


public class MapsActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        OnMapReadyCallback {

    //private static final String PREF_IS_FIRST_RUN="firstRun";
    //private static final String[] PERMS_LOC={ACCESS_FINE_LOCATION};
    //private SharedPreferences prefs;

    AutoCompleteTextView atvPlaces;

    DownloadTask placesDownloadTask;
    DownloadTask placeDetailsDownloadTask;
    ParserTask placesParserTask;
    ParserTask placeDetailsParserTask;
    final int PLACES=0;
    final int PLACES_DETAILS=1;

    private GoogleMap mMap;
    EditText tvLocInfo;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;

    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;

    LatLng latLng;
    Marker mCurrLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Getting a reference to the AutoCompleteTextView
        atvPlaces = (AutoCompleteTextView) findViewById(R.id.atv_places);
        atvPlaces.setThreshold(1);

// Adding textchange listener
        atvPlaces.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
// Creating a DownloadTask to download Google Places matching "s"
                placesDownloadTask = new DownloadTask(PLACES);

// Getting url to the Google Places Autocomplete api
                String url = getAutoCompleteUrl(s.toString());

// Start downloading Google Places
// This causes to execute doInBackground() of DownloadTask class
                placesDownloadTask.execute(url);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
// TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
// TODO Auto-generated method stub
            }
        });

// Setting an item click listener for the AutoCompleteTextView dropdown list
        atvPlaces.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int index,
                                    long id) {

                ListView lv = (ListView) arg0;
                SimpleAdapter adapter = (SimpleAdapter) arg0.getAdapter();

                HashMap<String, String> hm = (HashMap<String, String>) adapter.getItem(index);

// Creating a DownloadTask to download Places details of the selected place
                //placeDetailsDownloadTask = new DownloadTask(PLACES_DETAILS);

// Getting url to the Google Places details api
                //String url = getPlaceDetailsUrl(hm.get("reference"));

// Start downloading Google Place Details
// This causes to execute doInBackground() of DownloadTask class
                //placeDetailsDownloadTask.execute(url);

            }
        });


        mDrawerList = (ListView)findViewById(R.id.navList);
        addDrawerItems();

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(MapsActivity.this, "Time for an upgrade!", Toast.LENGTH_SHORT).show();
                switch(position){
                    case 0 :
                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        break;
                    case 1 :
                        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                        break;
                    case 2 :
                        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                }
            }
        });



        //prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //if (isFirstRun())
        //{
        //    requestPermissions(PERMS_LOC, 1339);
        //}
        tvLocInfo = (EditText) findViewById(R.id.et1);
        //button = (Button) findViewById(R.id.button);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng point) {
                // TODO Auto-generated method stub
                //tvLocInfo.setText(point.toString());
                //mMap.clear();
                mMap.addMarker(new MarkerOptions().position(point).title(point.toString()).icon(BitmapDescriptorFactory.fromResource(R.drawable.point)));
            }
        });

    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        buildGoogleApiClient();
        mGoogleApiClient.connect();
        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    protected synchronized void buildGoogleApiClient() {
        Toast.makeText(this,"buildGoogleApiClient",Toast.LENGTH_SHORT).show();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Toast.makeText(this,"onConnected",Toast.LENGTH_SHORT).show();
        /*Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            //place marker at current position
            mMap.clear();
            latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("Current Position");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
            mCurrLocation = mMap.addMarker(markerOptions);

        }*/

        mLocationRequest = new LocationRequest();
        //mLocationRequest.setInterval(5000); //5 seconds
        //mLocationRequest.setFastestInterval(3000); //3 seconds
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        //mLocationRequest.setSmallestDisplacement(0.1F); //1/10 meter

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        //mMap.addMarker(new MarkerOptions().position(latLng).title("MARKER"));

    }

    public void onSearch(View view)
    {
        //String location = tvLocInfo.getText().toString();
        String location = atvPlaces.getText().toString();
        List<Address> addressList = null;
        if (location != null || !location.equals(" "))
        {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(location, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Address address = addressList.get(0);
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
            mMap.addMarker(new MarkerOptions().position(latLng).title(location.toString()).icon(BitmapDescriptorFactory.fromResource(R.drawable.place)));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this,"onConnectionSuspended",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this,"onConnectionFailed",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {

        //remove previous current location marker and add new one at current position
        if (mCurrLocation != null) {
            mCurrLocation.remove();
        }
        latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("TU JESTEM");
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.myloc));
        mCurrLocation = mMap.addMarker(markerOptions);

        //Toast.makeText(this,"Location Changed",Toast.LENGTH_SHORT).show();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        //If you only need one location, unregister the listener
        //LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }



    /*private boolean isFirstRun() {
        boolean result = prefs.getBoolean(PREF_IS_FIRST_RUN, true);
        if (result) {
            prefs.edit().putBoolean(PREF_IS_FIRST_RUN, false).apply();
        }
        return (result);
    }*/



    private void addDrawerItems() {
        String[] osArray = { "Wikok normalny", "Widok z satelity", "Widok terenu" };
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArray);
        mDrawerList.setAdapter(mAdapter);
    }

    public static String getAutoCompleteUrl(String place){

// Obtain browser key from https://code.google.com/apis/console
        //String key = "key=AIzaSyCfdXATlz7jtM6MEvy9Xh_3_g_Ivc5ysXE";
        String key = "key=AIzaSyAgVyQOZ3shDeWgp4KEDDQaEprqxCnKXyk";

// place to be be searched
        String input = "input="+place;

// place type to be searched
        String types = "types=geocode";

// Sensor enabled
        String sensor = "sensor=false";

// Building the parameters to the web service
        String parameters = input+"&"+types+"&"+sensor+"&"+key;

// Output format
        String output = "json";

// Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/place/autocomplete/"+output+"?"+parameters;

        return url;
    }

   /* private String getPlaceDetailsUrl(String ref){

// Obtain browser key from https://code.google.com/apis/console
        String key = "key=key";

// reference of place
        String reference = "reference="+ref;

// Sensor enabled
        String sensor = "sensor=false";

// Building the parameters to the web service
        String parameters = reference+"&"+sensor+"&"+key;

// Output format
        String output = "json";

// Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/place/details/"+output+"?"+parameters;

        return url;
    }*/

    /** A method to download json data from url */
    public static String downloadUrl(String strUrl) throws IOException{
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

// Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

// Connecting to url
            urlConnection.connect();

// Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while( ( line = br.readLine()) != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("Exception while downloading url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }

        return data;

    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {

        private int downloadType=0;

        // Constructor
        public DownloadTask(int type){
            this.downloadType = type;
        }

        @Override
        protected String doInBackground(String... url) {

// For storing data from web service
            String data = "";

            try{
// Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            //switch(downloadType){
            //case PLACES:
// Creating ParserTask for parsing Google Places
            placesParserTask = new ParserTask(PLACES);

// Start parsing google places json data
// This causes to execute doInBackground() of ParserTask class
            placesParserTask.execute(result);

            //break;

                /*case PLACES_DETAILS :
// Creating ParserTask for parsing Google Places
                    placeDetailsParserTask = new ParserTask(PLACES_DETAILS);

// Starting Parsing the JSON string
// This causes to execute doInBackground() of ParserTask class
                    placeDetailsParserTask.execute(result);
            }*/
        }
    }

    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String,String>>>{

        int parserType = 0;

        public ParserTask(int type){
            this.parserType = type;
        }

        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<HashMap<String, String>> list = null;

            try{
                jObject = new JSONObject(jsonData[0]);

                //switch(parserType){
                //case PLACES :
                PlaceJSONParser placeJsonParser = new PlaceJSONParser();
// Getting the parsed data as a List construct
                list = placeJsonParser.parse(jObject);
                //break;
                //case PLACES_DETAILS :
                //PlaceDetailsJSONParser placeDetailsJsonParser = new PlaceDetailsJSONParser();
// Getting the parsed data as a List construct
                //list = placeDetailsJsonParser.parse(jObject);
                //}

            }catch(Exception e){
                Log.d("Exception",e.toString());
            }
            return list;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> result) {

            //switch(parserType){
            // case PLACES :
            String[] from = new String[] { "description"};
            int[] to = new int[] { android.R.id.text1 };

// Creating a SimpleAdapter for the AutoCompleteTextView
            SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), result, android.R.layout.simple_list_item_1, from, to);

// Setting the adapter
            atvPlaces.setAdapter(adapter);
                    /*break;
                case PLACES_DETAILS :
                    HashMap<String, String> hm = result.get(0);

// Getting latitude from the parsed data
                    double latitude = Double.parseDouble(hm.get("lat"));

// Getting longitude from the parsed data
                    double longitude = Double.parseDouble(hm.get("lng"));

// Getting reference to the SupportMapFragment of the activity_main.xml
                    SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);


// Getting GoogleMap from SupportMapFragment
                    mMap = fm.getMap();

                    LatLng point = new LatLng(latitude, longitude);

                    CameraUpdate cameraPosition = CameraUpdateFactory.newLatLng(point);
                    CameraUpdate cameraZoom = CameraUpdateFactory.zoomBy(5);

// Showing the user input location in the Google Map
                    //mMap.moveCamera(cameraPosition);
                    //mMap.animateCamera(cameraZoom);

                    MarkerOptions options = new MarkerOptions();
                    options.position(point);
                    options.title("Position");
                    options.snippet("Latitude:"+latitude+",Longitude:"+longitude);

// Adding the marker in the Google Map
                    //mMap.addMarker(options);

                    break;
            }*/
        }
    }

}

