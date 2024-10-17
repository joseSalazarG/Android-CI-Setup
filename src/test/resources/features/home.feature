@Drastic
Feature: Test de la aplicacion

  Background:
    Given Abro la aplicacion

  @test
  Scenario: Pokedex
    When Entro a la pokedex
    Then Deberia ver la lista de pokemones

