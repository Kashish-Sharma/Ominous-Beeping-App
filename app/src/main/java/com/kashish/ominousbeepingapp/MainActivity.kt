package com.kashish.ominousbeepingapp

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import com.kashish.ominousbeepingapp.databinding.ActivityMainBinding
import com.kashish.ominousbeepingapp.listener.ShakeDetector
import com.kashish.ominousbeepingapp.listener.ShakeDetector.OnShakeListener

class MainActivity : AppCompatActivity() {

    private val MAX_TIME = 50.toLong()
    private val SPEED = 1.5f

    private lateinit var binding: ActivityMainBinding

    //For Shake Detection
    private lateinit var mSensorManager: SensorManager
    private lateinit var mAccelerometer: Sensor
    private lateinit var mShakeDetector: ShakeDetector

    //For Audio
    private var mMediaPlayer: MediaPlayer? = null

    //ImageView array
    private var visibleRingNum = 0
    private val mRingsArray = arrayListOf<ImageView>()

    //Handler
    private var mHandler: Handler? = null
    private var mRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this@MainActivity, R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        initSensorManager()
    }

    private fun initSensorManager() {
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mShakeDetector = ShakeDetector()
        mShakeDetector.setOnShakeListener(object : OnShakeListener {
            override fun onShake(count: Int) {
                handleShakeEvent(count)
            }
        })
    }

    private fun initRings() {
        visibleRingNum = 0
        mHandler = Handler()
        mRunnable = Runnable {
            run {
                updateRings()
                mHandler?.postDelayed(mRunnable, MAX_TIME)
            }
        }
        binding.activityMainRingsFrameLayout.visibility = View.INVISIBLE
        mRingsArray.forEach { it.visibility = View.GONE }
        mRingsArray.add(binding.ringFirst)
        mRingsArray.add(binding.ringThird)
        mRingsArray.add(binding.ringFifth)
        mRingsArray.add(binding.ringSeventh)
    }

    private fun updateRings() {
        when {
            visibleRingNum < 0 -> {
                mRingsArray[ mRingsArray.size + visibleRingNum ].visibility = View.GONE
                visibleRingNum++
            }
            else -> {

                if (visibleRingNum == 4) visibleRingNum = -4
                else {
                    mRingsArray[visibleRingNum].visibility = View.VISIBLE
                    visibleRingNum++
                }
            }
        }

    }

    private fun handleShakeEvent(shakeCount: Int) {
        if (mMediaPlayer == null && shakeCount > 1) {
            mMediaPlayer = MediaPlayer.create(this@MainActivity, R.raw.beep)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mMediaPlayer?.playbackParams = mMediaPlayer?.playbackParams?.setSpeed(SPEED)
            }
            mMediaPlayer?.start()
            mMediaPlayer?.isLooping = true
            binding.activityMainRingsFrameLayout.visibility = View.VISIBLE
            mHandler?.post(mRunnable)
        }
    }

    override fun onPause() {
        mSensorManager.unregisterListener(mShakeDetector)
        mMediaPlayer?.release()
        mMediaPlayer = null

        mHandler?.removeCallbacks(mRunnable)

        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        mSensorManager.registerListener(mShakeDetector, mAccelerometer,	SensorManager.SENSOR_DELAY_UI)
        initRings()
    }

}
