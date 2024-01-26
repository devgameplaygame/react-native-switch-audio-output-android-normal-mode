declare namespace SwitchAudioOutput {
    const getAudioDevices: () => string[]
    const setAudioDevice: (deviceName: string) => string[]
    const requestAudioFocus: () => boolean
}

export = SwitchAudioOutput;