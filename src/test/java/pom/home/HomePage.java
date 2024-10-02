package pom.home;

import lombok.Getter;

@Getter
public class HomePage {
    private final String btnOpciones = "//android.widget.Button[@resource-id='com.dsemu.drastic:id/button_options']";
    private final String btnOpcionesDeAudio = "//android.widget.TextView[@text='Audio']";
    private final String btnNo = "//android.widget.Button[@text='NO']";
}