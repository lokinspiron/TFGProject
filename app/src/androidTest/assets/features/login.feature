Feature: Login Screen
  Como usuario
  Quiero poder iniciar sesión
  Para acceder a mi cuenta

  Scenario: Login exitoso
    Given estoy en la pantalla de login
    When ingreso el email "test@example.com"
    And ingreso la contraseña "Test123"
    And presiono el botón de login
    Then debería ver la pantalla principal

  Scenario: Login fallido - campos vacíos
    Given estoy en la pantalla de login
    When presiono el botón de login
    Then debería ver mensaje de error "Correo requerido"
    And debería ver mensaje de error "Contraseña requerida"