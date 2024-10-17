package steps;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import pom.home.HomeLogic;

public class HomeSteps {

    HomeLogic homeLogic = new HomeLogic();

    @Given("Abro la aplicacion")
    public void abrirApp() {
        homeLogic.abrirApp();
    }

    @When("Entro a la pokedex")
    public void entroALaPokedex() {
        homeLogic.entrarPokedex();
    }

    @Then("Deberia ver la lista de pokemones")
    public void deberiaVerLaListaDePokemones() {
        homeLogic.verListaPokemones();
    }
}