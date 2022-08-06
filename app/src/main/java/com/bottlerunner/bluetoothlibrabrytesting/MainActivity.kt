package com.bottlerunner.bluetoothlibrabrytesting

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.harrysoft.androidbluetoothserial.BluetoothManager
import com.harrysoft.androidbluetoothserial.BluetoothSerialDevice
import com.harrysoft.androidbluetoothserial.SimpleBluetoothDeviceInterface
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import javax.xml.transform.ErrorListener


class MainActivity : AppCompatActivity() {

    val bluetoothManager: BluetoothManager = BluetoothManager.getInstance() //Harry's bluetooth manager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        connectDevice("FC:AA:B6:AA:AC:22")

        val pairedDevices: Collection<BluetoothDevice> = bluetoothManager.pairedDevicesList
        for (device in pairedDevices) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(this,"permission denied",Toast.LENGTH_SHORT).show()

            }

            Log.d("My Bluetooth App", "Device name: " + device.name)
            Log.d("My Bluetooth App", "Device MAC Address: " + device.address)
            Log.d("Debugg",device.bondState.toString())
        }


        val btn = findViewById<Button>(R.id.button)
        btn.setOnClickListener {
            connectDevice("FC:AA:B6:AA:AC:22")          //Tab
            ActivityCompat.requestPermissions(this, arrayOf( Manifest.permission.BLUETOOTH),0  )
            Log.d("Permission","Reached end of btn")
        }

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1) {
            // Request for camera permission.
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted. Start camera preview Activity.
                Log.d("Permission","Reached onRequestPermissionResult")
                ActivityCompat.requestPermissions(this, arrayOf( Manifest.permission.BLUETOOTH),0  )
                Toast.makeText(this,"Permission granted",Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this,grantResults.toString(),Toast.LENGTH_SHORT).show()}
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    var onMessageSentListener = SimpleBluetoothDeviceInterface.OnMessageSentListener{
        Toast.makeText(this, "Sent a message! Message was: " + it, Toast.LENGTH_LONG).show() // Replace context with your context instance.
    }

    var onMessageReveivedListener = SimpleBluetoothDeviceInterface.OnMessageReceivedListener{
        // We received a message! Handle it here.
        Toast.makeText(this, "Received a message! Message was: " + it, Toast.LENGTH_LONG).show(); // Replace context with your context instance.
    }

    var errorListener = SimpleBluetoothDeviceInterface.OnErrorListener{
        Toast.makeText(this,"Durr phite muh, error" + it.message, Toast.LENGTH_SHORT).show()
    }

    lateinit var deviceInterface: SimpleBluetoothDeviceInterface

    var onConnected = Consumer<BluetoothSerialDevice>{
        // You are now connected to this device!
        // Here you may want to retain an instance to your device:
        deviceInterface = it.toSimpleDeviceInterface()

        // Listen to bluetooth events
        deviceInterface.setListeners(onMessageReveivedListener, onMessageSentListener, errorListener)

        // Let's send a message:
        deviceInterface.sendMessage("Hello world!")
    }

    var onError = Consumer<Throwable> {
        Toast.makeText(this, "Error kitna sona he" +it.message,Toast.LENGTH_SHORT).show()
        Log.d("onError" ,it.message + "\n"+ it.cause)

    }

    fun connectDevice(mac:String) {
        bluetoothManager.openSerialDevice(mac)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onConnected, onError)
        Log.d("Debugg","reached 114")
    }

}