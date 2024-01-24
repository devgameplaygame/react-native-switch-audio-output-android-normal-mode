declare namespace SwitchAudioOutput {
    const getAudioDevices: () => string[]
    const setAudioDevice: (deviceName: string) => boolean
    const requestAudioFocus: () => boolean
}

export = SwitchAudioOutput;