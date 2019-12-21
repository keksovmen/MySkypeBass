package com.Abstraction.Audio.Input;

public interface ChangeableInput {

    void changeInput(int indexOfParticularInputDevice);

    void mute();

    void IncreaseBass(int percentage);
}
