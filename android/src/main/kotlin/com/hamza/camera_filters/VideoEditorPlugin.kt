package com.hamza.camera_filters

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import com.daasuu.mp4compose.composer.Mp4Composer
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

class VideoEditorPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    private var activity: Activity? = null
    private lateinit var methodChannel: MethodChannel
    private val myPermissionCode = 34264

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        methodChannel = MethodChannel(flutterPluginBinding.binaryMessenger, "camera_filters")
        methodChannel.setMethodCallHandler(this)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            "getPlatformVersion" -> {
                result.success("Android ${android.os.Build.VERSION.RELEASE}")
            }

            "writeVideofile" -> {
                val currentActivity = activity ?: run {
                    result.error("no_activity", "Activity is null", null)
                    return
                }

                checkPermission(currentActivity)

                val srcFilePath: String = call.argument("srcFilePath") ?: run {
                    result.error("src_file_path_not_found", "the src file path is not found.", null)
                    return
                }

                val destFilePath: String = call.argument("destFilePath") ?: run {
                    result.error("dest_file_path_not_found", "the dest file path is not found.", null)
                    return
                }

                val processing: HashMap<String, HashMap<String, Any>> = call.argument("processing") ?: run {
                    result.error("processing_data_not_found", "the processing is not found.", null)
                    return
                }

                val generator = VideoGeneratorService(Mp4Composer(srcFilePath, destFilePath))
                generator.writeVideofile(processing, result, currentActivity)
            }

            else -> result.notImplemented()
        }
    }

    private fun checkPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            myPermissionCode
        )
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
        binding.addRequestPermissionsResultListener { requestCode, _, _ ->
            requestCode == myPermissionCode
        }
    }

    override fun onDetachedFromActivity() {
        activity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        onAttachedToActivity(binding)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity()
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        methodChannel.setMethodCallHandler(null)
    }
}
