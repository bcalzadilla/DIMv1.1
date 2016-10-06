package com.thingworx.sdk.android.steamexample;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceActivity;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.thingworx.communications.client.ConnectedThingClient;
import com.thingworx.communications.client.things.VirtualThing;
import com.thingworx.communications.client.things.VirtualThingPropertyChangeEvent;
import com.thingworx.communications.client.things.VirtualThingPropertyChangeListener;
import com.thingworx.metadata.FieldDefinition;
import com.thingworx.metadata.annotations.ThingworxEventDefinition;
import com.thingworx.metadata.annotations.ThingworxEventDefinitions;
import com.thingworx.metadata.annotations.ThingworxPropertyDefinition;
import com.thingworx.metadata.annotations.ThingworxPropertyDefinitions;
import com.thingworx.metadata.annotations.ThingworxServiceDefinition;
import com.thingworx.metadata.annotations.ThingworxServiceParameter;
import com.thingworx.metadata.annotations.ThingworxServiceResult;
import com.thingworx.metadata.collections.FieldDefinitionCollection;
import com.thingworx.sdk.android.activity.ThingworxActivity;
import com.thingworx.types.BaseTypes;
import com.thingworx.types.collections.ValueCollection;
import com.thingworx.types.constants.CommonPropertyNames;
import com.thingworx.types.primitives.BlobPrimitive;
import com.thingworx.types.primitives.NumberPrimitive;
import com.thingworx.communications.client.ConnectedThingClient;
import com.thingworx.communications.client.things.VirtualThing;
import com.thingworx.metadata.annotations.ThingworxPropertyDefinition;
import com.thingworx.metadata.annotations.ThingworxPropertyDefinitions;
import com.thingworx.metadata.annotations.ThingworxServiceDefinition;
import com.thingworx.metadata.annotations.ThingworxServiceParameter;
import com.thingworx.metadata.annotations.ThingworxServiceResult;
import com.thingworx.types.primitives.StringPrimitive;

import org.joda.time.DateTime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Blob;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 *
 */
public class MainActivity extends ThingworxActivity implements SurfaceHolder.Callback {
    Camera camera;

    @InjectView(R.id.surfaceView)
    SurfaceView surfaceView;
    @InjectView(R.id.btn_take_photo)
    ImageButton btn_take_photo, btn_latitud;
    SurfaceHolder surfaceHolder;
    PictureCallback jpegCallback;
    ShutterCallback shutterCallback;
    private static final int MY_PERMISSION_ACCESS_COARSE_LOCATION = 11;
    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 12;
    private static final int MINIMUM_TIME = 10000;  // 10s
    private static final int MINIMUM_DISTANCE = 50; // 50m
    TextView txtGPS;
    private static final int REQUEST_CAMERA_RESULT = 1;
    double latitud, longitud;
    String Direccion, Text;
    private static final int RECORD_REQUEST_CODE = 101;
    ImageButton btnFire, btnGun, btnMedical;
    private final static String logTag = MainActivity.class.getSimpleName();
    public static final int POLLING_RATE = 1000;
    private final String TAG = MainActivity.class.getName();
    private SteamThing sensor1;
    public String date;
    public double tipoIncidente;
    public File picfile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Create your Virtual Thing and bind it to your android controls
        try {

            sensor1 = new SteamThing("DIMParameters", "Datos de DIM", client);

            /* Adding a property change listener to your VirtualThing is a convenient way to directly
             * bind your android controls to property values. They will get updated
             * as soon as they are changed, either on the server or locally                       */
            sensor1.addPropertyChangeListener(new VirtualThingPropertyChangeListener() {
                @Override
                public void propertyChangeEventReceived(final VirtualThingPropertyChangeEvent evt) {
                    final String propertyName = evt.getProperty().getPropertyDefinition().getName();
                    runOnUiThread(new Runnable() { // Always update your controls on the UI thread
                        @Override
                        public void run() {
                            // change UI elements here
                            DecimalFormat df = new DecimalFormat("#.#######");

                            if (propertyName.equals("proplatitud")) {
                                Double prlatitud = (Double) evt.getPrimitiveValue().getValue();

                            }else if (propertyName.equals("proplongitud")) {
                                Double prlongitud = (Double) evt.getPrimitiveValue().getValue();

                            }else if (propertyName.equals("timestamp")) {
                                DateTime date = (DateTime) evt.getPrimitiveValue().getValue();

                            }else if (propertyName.equals("tipoIncidente")) {
                                Double tipoinc = (Double) evt.getPrimitiveValue().getValue();

                            }else if (propertyName.equals("imagen")) {
                                BlobPrimitive tipoinc = (BlobPrimitive) evt.getPrimitiveValue().getValue();

                            }

                        }
                    });
                }
            });

            // You only need to do this once, no matter how many things your add
            startProcessScanRequestThread(POLLING_RATE, new ConnectionStateObserver() {
                @Override
                public void onConnectionStateChanged(final boolean connected) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        }
                    });
                }
            });

            // If you don't have preferences, display the dialog to get them.
            if (!hasConnectionPreferences()) {
                // Show Preferences Activity
                connectionState = ConnectionState.DISCONNECTED;

                return;
            }


            connect(new VirtualThing[]{sensor1});

        } catch (Exception e) {
            Log.e(TAG, "Failed to initalize with error.", e);
            onConnectionFailed("Failed to initalize with error : " + e.getMessage());
        }

        txtGPS = (TextView) findViewById(R.id.estatusgps);
        btnFire = (ImageButton) findViewById(R.id.btnFire);
        btnGun = (ImageButton) findViewById(R.id.btnGun);
        btnMedical = (ImageButton) findViewById(R.id.btnMedical);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        } else {
            // Simplemente utilizar la función requerida
            // Ya que el usuario ya ha concedido permiso a ellos durante la instalación
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);


        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to record denied");
            makeRequest();
        }


        ButterKnife.inject(this);
        String mProviderName;
        surfaceHolder = surfaceView.getHolder();
        // Instalar un surfaceHolder.Callback notifica cuando el surface se crea o se destruye
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        btn_take_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImage();
                btnFire.setVisibility(View.VISIBLE);
                btnFire.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(MainActivity.this, "Ha seleccionado Fuego", Toast.LENGTH_LONG).show();
                        tipoIncidente =1;
                    }
                });
                btnGun.setVisibility(View.VISIBLE);
                btnGun.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(MainActivity.this, "Ha seleccionado Violencia", Toast.LENGTH_LONG).show();
                        tipoIncidente=2;
                    }
                });
                btnMedical.setVisibility(View.VISIBLE);
                btnMedical.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(MainActivity.this, "Ha seleccionado Salud", Toast.LENGTH_LONG).show();
                        tipoIncidente=3;
                    }
                });
            }
        });


        LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Localizacion Local = new Localizacion();
        Local.setMainActivity(this);
        mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, (LocationListener) Local);

        btn_latitud = (ImageButton) findViewById(R.id.btn_switch);
        btn_latitud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TelephonyManager cel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                String mPhoneNumber = cel.getLine1Number();

                Toast.makeText(MainActivity.this, "Ubicacion" + Text, Toast.LENGTH_SHORT).show();


            }
        });


        jpegCallback = new PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                FileOutputStream outputStream = null;
                File file_image = getDirc();
                if (!file_image.exists() && !file_image.mkdirs()) {
                    Toast.makeText(getApplication(), "No se puede crear directorio", Toast.LENGTH_SHORT).show();
                    return;
                }
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyymmddhhmmss");
                date = simpleDateFormat.format(new Date());
                String photofile = "DIM" + date + ".jpg";
                String file_name = file_image.getPath() + File.separator + photofile;
                picfile = new File(file_name);
                try {
                    outputStream = new FileOutputStream(picfile);
                    outputStream.write(data);
                    outputStream.close();
                } catch (FileNotFoundException e) {
                } catch (IOException ex) {
                } finally {

                }
                Toast.makeText(getApplicationContext(), "Alerta enviada correctamente", Toast.LENGTH_SHORT).show();
                refreshCamera();
                refreshGallery(picfile);
                txtGPS.setText("Alerta enviada correctamente!");
            }
        };
    }

//fin de onCreate


    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume() called.");
        if(getConnectionState() == ConnectionState.DISCONNECTED) {
            try {
                connect(new VirtualThing[]{sensor1});
            } catch (Exception e) {
                Log.e(TAG, "Restart with new settings failed.", e);
            }
        }
    }
    /**
     * This function will be called from the base class to allow you to set
     * values on your virtual thing that are not configured in your aspect defaults or to perform
     * any other UI changes in response to becoming connected to the server.
     */
    @Override
    protected void onConnectionEstablished() {
        super.onConnectionEstablished();
        try {
            this.sensor1.setProperty("proplatitud", latitud);
            this.sensor1.setProperty("proplongitud", longitud);
            this.sensor1.setProperty("timestamp", date);
            this.sensor1.setProperty("tipoIncidente", "5");
            //BlobPrimitive blobNbi = BlobPrimitive.class.cast(picfile);
            //this.sensor1.setProperty("imagen", blobNbi);

        } catch (Exception e) {
            Log.w(TAG, "Error al setear valores por default");
        }
    }

    /*

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_settings was selected
            case R.id.action_settings:
                disconnect();
                Intent i = new Intent(this, PreferenceActivity.class);
                startActivityForResult(i, 1);
                break;
            default:
                break;
        }
        return true;
    }*/

    public void setLocation(Location loc) {

        if (loc.getLatitude() != 0.0 && loc.getLongitude() != 0.0) {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> list = geocoder.getFromLocation(
                        loc.getLatitude(), loc.getLongitude(), 1);
                if (!list.isEmpty()) {
                    Address DirCalle = list.get(0);
                    Direccion = DirCalle.getAddressLine(0);

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //refresh gallery
    public void refreshGallery(File file) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(file));
        sendBroadcast(intent);
    }

    public void refreshCamera() {
        if (surfaceHolder.getSurface() == null) {
            //el surface view no existe
            return;
        }

        try {
            camera.stopPreview();
        } catch (Exception e) {
        }
        //Establece el tamaño de la vista ´previa , rotar etc, ajustes actuales de preview

        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {
        }
    }

    public File getDirc() {
        File dics = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        return new File(dics, "DIM");
    }

    public void captureImage() {
        //tomar la foto
        camera.takePicture(null, null, jpegCallback);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //abrir la camara
        try {
            camera = Camera.open();
        } catch (RuntimeException ex) {
        }
        Camera.Parameters parameters;
        parameters = camera.getParameters();
        //modificar parámetros
        //parameters.setPreviewFrameRate(40);
        parameters.setPreviewSize(352, 288);
        camera.setParameters(parameters);
        camera.setDisplayOrientation(90);
        try {
            //La surface fue creada ahora se pasan parametros para trazar preview
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        refreshCamera();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
//stop preview and release camera
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    protected void makeRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                RECORD_REQUEST_CODE);
    }

    public class Localizacion implements LocationListener {
        MainActivity mainActivity;

        public MainActivity getMainActivity() {
            return mainActivity;
        }

        public void setMainActivity(MainActivity mainActivity) {
            this.mainActivity = mainActivity;
        }

        @Override
        public void onLocationChanged(Location loc) {


            latitud= loc.getLatitude();
            longitud= loc.getLongitude();
            Text = "Mi ubicacion actual es: " + "\n Lat = "
                    + latitud + "\n Long = " + longitud;

            this.mainActivity.setLocation(loc);
        }

        @Override
        public void onProviderDisabled(String provider) {
            txtGPS.setText("GPS Desactivado");
        }

        @Override
        public void onProviderEnabled(String provider) {
            txtGPS.setText("GPS Activado");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

    }

    @SuppressWarnings("serial")
    @ThingworxPropertyDefinitions(properties = {
            @ThingworxPropertyDefinition(name="proplatitud", description="latitud", baseType="NUMBER", category="Status", aspects={"isReadOnly:false"}),
            @ThingworxPropertyDefinition(name="proplongitud", description="longitud", baseType="NUMBER", category="Status", aspects={"isReadOnly:false"}),
            @ThingworxPropertyDefinition(name="timestamp", description="time stamp", baseType="DATETIME", category="Status", aspects={"isReadOnly:false"}),
            @ThingworxPropertyDefinition(name="tipoIncidente", description="tipo incidente", baseType="NUMBER", category="Status", aspects={"isReadOnly:false", "defaultValue:5"}),
            @ThingworxPropertyDefinition(name="imagen", description="imagen incidente", baseType="BLOB", category="Status", aspects={"isReadOnly:false"})
    })


    public class SteamThing extends VirtualThing {


        public SteamThing(String name, String description, ConnectedThingClient client) throws Exception {
            super(name,description,client);


            initializeFromAnnotations();
        }

        /**
         * The application that binds this Thing to its connection is responsible for calling this method
         * periodically to allow this Thing to generate simulated data. If your application generates
         * data instead of simulating it, your would update your properties when new data is available
         * and then call updateSubscribedProperties() to push these values to the server. This method
         * can also be used to poll your hardware if it does not deliver its own data asynchronously.
         * @throws Exception
         */
        @Override
        public void processScanRequest() throws Exception {

            setProperty("proplatitud",latitud );
            setProperty("proplongitud", longitud);
            setProperty("timestamp", date);
            setProperty("tipoIncidente", tipoIncidente);
            //setProperty("imagen", picfile);


            updateSubscribedProperties(15000);
            updateSubscribedEvents(60000);

        }

        /**
         * This sample method will be available to be bound and can be called from the server.
         * When this method is called from the server it will cause this steam client to disconnect.
         * @throws Exception
         */
        @ThingworxServiceDefinition( name="Shutdown", description="Shutdown the client")
        public void Shutdown() throws Exception {
            this.getClient().shutdown();
        }

    }


}


