package com.example.andrew.weatherapplicationandrew

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TimePicker
import android.widget.Toast
import com.example.andrew.weatherapplicationandrew.Common.Common
import com.example.andrew.weatherapplicationandrew.Model.OpenWeatherMap
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.zipcodedialog.*
import java.util.*


class MainActivity : AppCompatActivity(),  GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,LocationListener {

    val PERMISSION_REQUEST_CODE = 1001
    val PLAY_SERVICE_RESOLUTION_REQUEST = 1000


    var mGoogleApiClient: GoogleApiClient? = null
    var mLocationRequest: LocationRequest? = null
    internal var openWeatherMap = OpenWeatherMap()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        requestPermission();
        if (checkPlayService())
            buildGoogleApiClient()
    }



    private fun requestPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Verifying Permissions are granted", Toast.LENGTH_SHORT).show()
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {


        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Finding location", Toast.LENGTH_LONG).show()
                    if (checkPlayService()) {
                        buildGoogleApiClient()
                        mGoogleApiClient!!.connect()
                    }

                } else {

                    val zipcodeDialog: AlertDialog.Builder = AlertDialog.Builder(this)
                    val zipcodeDialogView = layoutInflater.inflate(R.layout.zipcodedialog, null)
                    val txtZipcode = zipcodeDialogView.findViewById<EditText>(R.id.txtZipcode)

                    zipcodeDialog.setTitle("Enter A ZipCode")
                    zipcodeDialog.setView(zipcodeDialogView)
                    zipcodeDialog.setCancelable(false)
                    zipcodeDialog.setPositiveButton("Submit", { dialogInterface: DialogInterface, i: Int -> })

                    val customDialog = zipcodeDialog.create()
                    customDialog.show()
                    customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener({
                        if (txtZipcode.text.length == 5) {

                            customDialog.dismiss()
                            mGoogleApiClient!!.connect()


                            txtCity.text = txtZipcode.editableText

                            GetZipWeather().execute(Common.apiZipRequest(txtCity!!.text.toString()))

                        } else
                            Toast.makeText(baseContext, "Invalid Zipcode Entered", Toast.LENGTH_SHORT).show()
                    })
                }

            }
        }
    }

    private fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build()
    }

    private fun checkPlayService(): Boolean {
        var resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)

        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICE_RESOLUTION_REQUEST).show()
            } else {
                Toast.makeText(applicationContext, "This device is not support", Toast.LENGTH_SHORT).show()
                finish()
            }
            return false
        }
        return true
    }

    override fun onConnected(p0: Bundle?) {
        createLocationRequest();
    }

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest!!.interval = 1000 // 10 seconds
        mLocationRequest!!.fastestInterval = 5000 // 5 seconds
        mLocationRequest!!.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)


    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.i("Error", "Connetion failed: " + p0.errorCode)
        txtCity.text = "No location found, Please Check gps signal \n" +
                "If it is OFF please clear this applications memory (settings>Applications>this application's name) \n" +
                "Press force stop and clear the storage's cache and data \n" +
                "Wait a minute or two for it to load up correctly"
    }

    override fun onLocationChanged(location: Location  ) {
        mGoogleApiClient!!.connect()

        if(txtZipcode==txtCity)
            GetZipWeather().execute(Common.apiZipRequest(txtCity!!.text.toString()))
        else
        GetWeather().execute(Common.apiRequest(location!!.latitude.toString(), location!!.longitude.toString()))

    }

    override fun onConnectionSuspended(p0: Int) {
        mGoogleApiClient!!.connect()
    }

    override fun onStart() {
        super.onStart()

        if (mGoogleApiClient != null)
            mGoogleApiClient!!.connect()

    }

    override fun onDestroy() {
        mGoogleApiClient!!.disconnect()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        checkPlayService()
    }

    inner class GetZipWeather : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg params: String?): String {
            var stream: String? = null
            var urlString = params[0]

            val http = Helper()

            stream = http.getHTTPData(urlString)

            return stream
        }


        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)


            val gson = Gson()
            val mType = object : TypeToken<OpenWeatherMap>() {}.type


            openWeatherMap = Gson().fromJson<OpenWeatherMap>(result, mType)


            //setinformation into the UI
            txtCity.text = "City: ${openWeatherMap.name}, ${openWeatherMap.sys!!.country}"
            txtFahrenheit.text = "${openWeatherMap.main!!.temp} °F"
            txtLastUpdate.text = "Last Updated: ${Common.dateNow}"
            txtDescription.text = "Description: ${openWeatherMap.weather!![0].description}"
            txtTime.text = "Sunrise: ${Common.unixTimeStampToDateTime(openWeatherMap.sys!!.sunrise)} / Sunset: ${Common.unixTimeStampToDateTime(openWeatherMap.sys!!.sunset)}"

            Picasso.with(this@MainActivity)
                    .load(Common.getImage(openWeatherMap.weather!![0].icon!!))
                    .into(imageView)


            imageSunRise.visibility = View.INVISIBLE


            var c = Calendar.getInstance()
            var localhour: Int = c.get(Calendar.HOUR_OF_DAY)
            //morning sunrise colors
            if (localhour == 6) {

                txtCity.setTextColor(Color.parseColor("#000000"));
                txtFahrenheit.setTextColor(Color.parseColor("#000000"));
                txtLastUpdate.setTextColor(Color.parseColor("#000000"));
                txtDescription.setTextColor(Color.parseColor("#000000"));
                txtTime.setTextColor(Color.parseColor("#000000"));
                mainActivityID.setBackgroundColor(Color.parseColor("#DED2D8"));

                imageSunRise.visibility = View.VISIBLE
                imageSunSet.visibility = View.INVISIBLE
                imageNoonSun.visibility = View.INVISIBLE

                //evening sunset colors
            } else if (localhour == 18){
                txtCity.setTextColor(Color.parseColor("#000000"));
                txtFahrenheit.setTextColor(Color.parseColor("#000000"));
                txtLastUpdate.setTextColor(Color.parseColor("#000000"));
                txtDescription.setTextColor(Color.parseColor("#000000"));
                txtTime.setTextColor(Color.parseColor("#000000"));
                mainActivityID.setBackgroundColor(Color.parseColor("#DED2D8"));
                imageSunRise.visibility = View.INVISIBLE
                imageSunSet.visibility = View.VISIBLE
                imageNoonSun.visibility = View.INVISIBLE
            } //noon time
            else if (localhour > 6 && localhour < 18) {
                txtCity.setTextColor(Color.parseColor("#000000"));
                txtFahrenheit.setTextColor(Color.parseColor("#000000"));
                txtLastUpdate.setTextColor(Color.parseColor("#000000"));
                txtDescription.setTextColor(Color.parseColor("#000000"));
                txtTime.setTextColor(Color.parseColor("#000000"));
                mainActivityID.setBackgroundColor(Color.parseColor("#FDF6E6"));
                imageSunRise.visibility = View.INVISIBLE
                imageSunSet.visibility = View.INVISIBLE
                imageNoonSun.visibility = View.VISIBLE
            } // night time
            else {
                txtCity.text = localhour.toString()
                mainActivityID.setBackgroundColor(Color.parseColor("#000000"));
                txtCity.setTextColor(Color.parseColor("#ffffff"));
                txtFahrenheit.setTextColor(Color.parseColor("#ffffff"));
                txtLastUpdate.setTextColor(Color.parseColor("#ffffff"));
                txtDescription.setTextColor(Color.parseColor("#ffffff"));
                txtTime.setTextColor(Color.parseColor("#ffffff"));
                imageSunRise.visibility = View.INVISIBLE
                imageSunSet.visibility = View.INVISIBLE
                imageNoonSun.visibility = View.INVISIBLE
            }
        }
    }
    inner class GetWeather : AsyncTask<String, Void, String>() {


        override fun doInBackground(vararg params: String?): String {
            var stream: String? = null
            var urlString = params[0]

            val http = Helper()

            stream = http.getHTTPData(urlString)

            return stream
        }


        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            val gson = Gson()
            val mType = object : TypeToken<OpenWeatherMap>() {}.type

            openWeatherMap = Gson().fromJson<OpenWeatherMap>(result, mType)


            //setinformation into the UI
            txtCity.text = "City: ${openWeatherMap.name}, ${openWeatherMap.sys!!.country}"
            txtFahrenheit.text = "${openWeatherMap.main!!.temp} °F"
            txtLastUpdate.text = "Last Updated: ${Common.dateNow}"
            txtDescription.text = "Description: ${openWeatherMap.weather!![0].description}"
            txtTime.text = "Sunrise: ${Common.unixTimeStampToDateTime(openWeatherMap.sys!!.sunrise)} / Sunset: ${Common.unixTimeStampToDateTime(openWeatherMap.sys!!.sunset)}"

            Picasso.with(this@MainActivity)
                    .load(Common.getImage(openWeatherMap.weather!![0].icon!!))
                    .into(imageView)


            imageSunRise.visibility = View.INVISIBLE

            var c = Calendar.getInstance()
            var localhour: Int = c.get(Calendar.HOUR_OF_DAY)
            //morning sunrise colors
            if (localhour == 6) {

                txtCity.setTextColor(Color.parseColor("#000000"));
                txtFahrenheit.setTextColor(Color.parseColor("#000000"));
                txtLastUpdate.setTextColor(Color.parseColor("#000000"));
                txtDescription.setTextColor(Color.parseColor("#000000"));
                txtTime.setTextColor(Color.parseColor("#000000"));
                mainActivityID.setBackgroundColor(Color.parseColor("#DED2D8"));

                imageSunRise.visibility = View.VISIBLE
                imageSunSet.visibility = View.INVISIBLE
                imageNoonSun.visibility = View.INVISIBLE

                //evening sunset colors
            } else if (localhour == 18){
                txtCity.setTextColor(Color.parseColor("#000000"));
                txtFahrenheit.setTextColor(Color.parseColor("#000000"));
                txtLastUpdate.setTextColor(Color.parseColor("#000000"));
                txtDescription.setTextColor(Color.parseColor("#000000"));
                txtTime.setTextColor(Color.parseColor("#000000"));
                mainActivityID.setBackgroundColor(Color.parseColor("#DED2D8"));
                imageSunRise.visibility = View.INVISIBLE
                imageSunSet.visibility = View.VISIBLE
                imageNoonSun.visibility = View.INVISIBLE
            } //noon time
            else if (localhour > 6 && localhour < 18) {
                txtCity.setTextColor(Color.parseColor("#000000"));
                txtFahrenheit.setTextColor(Color.parseColor("#000000"));
                txtLastUpdate.setTextColor(Color.parseColor("#000000"));
                txtDescription.setTextColor(Color.parseColor("#000000"));
                txtTime.setTextColor(Color.parseColor("#000000"));
                mainActivityID.setBackgroundColor(Color.parseColor("#FDF6E6"));
                imageSunRise.visibility = View.INVISIBLE
                imageSunSet.visibility = View.INVISIBLE
                imageNoonSun.visibility = View.VISIBLE
            } // night time
            else {
                txtCity.text = localhour.toString()
                mainActivityID.setBackgroundColor(Color.parseColor("#000000"));
                txtCity.setTextColor(Color.parseColor("#ffffff"));
                txtFahrenheit.setTextColor(Color.parseColor("#ffffff"));
                txtLastUpdate.setTextColor(Color.parseColor("#ffffff"));
                txtDescription.setTextColor(Color.parseColor("#ffffff"));
                txtTime.setTextColor(Color.parseColor("#ffffff"));
                imageSunRise.visibility = View.INVISIBLE
                imageSunSet.visibility = View.INVISIBLE
                imageNoonSun.visibility = View.INVISIBLE
            }
        }

    }
}










