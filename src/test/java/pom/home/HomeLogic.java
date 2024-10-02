package pom.home;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import steps.Hooks;

import static org.junit.Assert.*;

public class HomeLogic extends Hooks {

    HomePage homePage = new HomePage();

    private static final Logger log = LogManager.getLogger(HomeLogic.class);

    public void abrirApp() {
        String step = "Abro la aplicacion";
        log.info(step);

        clickElement(homePage.getBtnNo());
    }

    public void voyAOpciones() {
        String step = "Voy a opciones";
        log.info(step);

        clickElement(homePage.getBtnOpciones());
    }

    public void validoVerOpcionesDeAudio() {
        String step = "Valido ver opciones de audio";
        log.info(step);

        assertTrue("No se ve", elementIsDisplayed(homePage.getBtnOpcionesDeAudio()));
    }
}