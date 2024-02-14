package com.anttivuor.switchaudiooutput;

import android.content.Context;

import android.os.Build;
import android.media.AudioManager;
import android.media.AudioDeviceInfo;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.MediaRouter;
import android.media.MediaRouter.RouteInfo;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.util.ArrayList;

public class RNSwitchAudioOutputModule extends ReactContextBaseJavaModule {
    private final ReactApplicationContext reactContext;

    public RNSwitchAudioOutputModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "RNSwitchAudioOutput";
    }

    private String getAudioRouteType(int type) {
        switch (type){
            case(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP):
            case(AudioDeviceInfo.TYPE_BLUETOOTH_SCO):
                return "Bluetooth";
            case(AudioDeviceInfo.TYPE_WIRED_HEADPHONES):
            case(AudioDeviceInfo.TYPE_WIRED_HEADSET):
            case(AudioDeviceInfo.TYPE_USB_DEVICE):
            case(AudioDeviceInfo.TYPE_USB_ACCESSORY):
            case(AudioDeviceInfo.TYPE_USB_HEADSET):
                return "Headset";
            case(AudioDeviceInfo.TYPE_BUILTIN_MIC):
                return "Phone";
            case(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER):
                return "Speaker";
            default:
                return null;
        }
    }

    @ReactMethod
    public void getAudioDevices(Promise promise) {
        try {
            Context context = this.reactContext.getApplicationContext();

            AudioManager audioManager = (AudioManager) context.getSystemService(context.AUDIO_SERVICE);
            WritableArray devices = Arguments.createArray();
            ArrayList<String> typeChecker = new ArrayList<>();
            AudioDeviceInfo[] audioDeviceInfo = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);

            for (AudioDeviceInfo device : audioDeviceInfo){
                String type = getAudioRouteType(device.getType());
                if (type != null && !typeChecker.contains(type)) {
                    typeChecker.add(type);
                    devices.pushString(type);
                }
            }
            promise.resolve(devices);
        } catch (Exception e) {
            promise.reject("GetAudioRoutes Error", e.getMessage());
        }
    }

    @ReactMethod
    public void setAudioDevice(String deviceName, Promise promise) {
        try {
            Context context = this.reactContext.getApplicationContext();
            AudioManager audioManager = (AudioManager) context.getSystemService(context.AUDIO_SERVICE);
            MediaRouter mediaRouter = (MediaRouter) context.getSystemService(context.MEDIA_ROUTER_SERVICE);

            if (deviceName.equals("Bluetooth")) {
                audioManager.stopBluetoothSco();
                audioManager.setBluetoothScoOn(false);
                audioManager.setBluetoothA2dpOn(true);
                audioManager.setSpeakerphoneOn(false);
            } else if (deviceName.equals("Headset")) {
                audioManager.stopBluetoothSco();
                audioManager.setBluetoothScoOn(false);
                audioManager.setBluetoothA2dpOn(false);
                audioManager.setSpeakerphoneOn(false);
            } else if (deviceName.equals("Speaker")) {
                audioManager.stopBluetoothSco();
                audioManager.setBluetoothScoOn(false);
                audioManager.setBluetoothA2dpOn(false);
                audioManager.setSpeakerphoneOn(true);
            } 

            audioManager.setMode(AudioManager.MODE_NORMAL); 
            int routeCount = mediaRouter.getRouteCount();

            WritableArray output = Arguments.createArray();

            for(int i= 0; i < routeCount; i++){
                
                RouteInfo routeInfo = mediaRouter.getRouteAt(i);

                output.pushString(" deviceType: " + routeInfo.getDeviceType() + ", " + routeInfo.getName());

                if (routeInfo.getDeviceType() == RouteInfo.DEVICE_TYPE_BLUETOOTH && deviceName.equals("Bluetooth")){
                    mediaRouter.selectRoute(mediaRouter.ROUTE_TYPE_LIVE_AUDIO, routeInfo);
                    output.pushString("Bluetooth enabled");
                    promise.resolve(output);
                    return;
                }
                
                if (routeInfo.getDeviceType() == RouteInfo.DEVICE_TYPE_UNKNOWN && deviceName.equals("Speaker")) {
                    mediaRouter.selectRoute(mediaRouter.ROUTE_TYPE_LIVE_AUDIO, routeInfo);
                    output.pushString("Speaker enabled");
                    promise.resolve(output);
                    return;
                }
                if (routeInfo.getDeviceType() == RouteInfo.DEVICE_TYPE_UNKNOWN && deviceName.equals("Headset")) {
                    mediaRouter.selectRoute(mediaRouter.ROUTE_TYPE_LIVE_AUDIO, routeInfo);
                    output.pushString("Headset enabled");
                    promise.resolve(output);
                    return;
                }

                if (routeInfo.getDeviceType() == RouteInfo.DEVICE_TYPE_UNKNOWN && deviceName.equals("Phone")){
                    mediaRouter.selectRoute(mediaRouter.ROUTE_TYPE_LIVE_AUDIO, routeInfo);
                    output.pushString("Phone enabled");
                    promise.resolve(output);
                    return;
                }

            }
            promise.resolve(output);
        } catch (Exception e) {
            promise.reject("SetAudioDevice", e.getMessage());
        }
    }

    @ReactMethod
    public void requestAudioFocus(Promise promise){
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Context context = this.reactContext.getApplicationContext();
                AudioManager audioManager = (AudioManager) context.getSystemService(context.AUDIO_SERVICE);
                AudioAttributes playbackAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();

                AudioFocusRequest audioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(playbackAttributes)
                    .build();

                final Object focusLock = new Object();

                int res = audioManager.requestAudioFocus(audioFocusRequest);
                synchronized(focusLock) {
                    if (res == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
                        promise.resolve(false);
                    } else if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                        promise.resolve(true);
                    } else if (res == AudioManager.AUDIOFOCUS_REQUEST_DELAYED) {
                        promise.resolve(false);
                    }
                }
            }
        }
        catch (Exception e){
            promise.reject("requestAudioFocus", e.getMessage());
        }

    }

}
