package steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import pom.home.HomeLogic;

public class HomeSteps {

    HomeLogic homeLogic = new HomeLogic();

    @Given("Abro la aplicacion")
    public void abrirApp() {
        homeLogic.abrirApp();
    }

    @And("Voy a opciones")
    public void voyAOpciones() {
        homeLogic.voyAOpciones();
    }

    @Then("Valido ver opciones de audio")
    public void validoVerOpcionesDeAudio() {
        homeLogic.validoVerOpcionesDeAudio();
    }
}