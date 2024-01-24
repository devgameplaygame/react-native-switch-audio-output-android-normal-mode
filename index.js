/**
 * @providesModule RNSwitchAudioOutput
 */

var { NativeModules } = require("react-native");
var RNSwitchAudioOutput = NativeModules.RNSwitchAudioOutput || {};

var SwitchAudioOutput = {
    getAudioDevices() {
        return RNSwitchAudioOutput.getAudioDevices();
    },
    setAudioDevice(deviceName) {
        return RNSwitchAudioOutput.setAudioDevice(deviceName);
    },
    requestAudioFocus() {
        return RNSwitchAudioOutput.requestAudioFocus();
    }
};

module.exports = SwitchAudioOutput;