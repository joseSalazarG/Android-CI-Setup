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

    }

    public void entrarPokedex() {
        String step = "Entro a la pokedex";
        log.info(step);

    }

    public void verListaPokemones() {
        String step = "Deberia ver la lista de pokemones";
        log.info(step);

    }
}