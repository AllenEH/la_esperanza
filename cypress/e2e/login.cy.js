// cypress/e2e/login.cy.js
describe('Pruebas de Login - La Esperanza', () => {
  
  beforeEach(() => {
    cy.visit('/');
  });

  it('Debe mostrar pantalla de login', () => {
    cy.get('.screen#screen-login').should('be.visible');
    cy.get('.logo-text').should('contain', 'La Esperanza');
  });

  it('Debe validar teléfono vacío', () => {
    cy.get('#codigo-input').type('1234');
    cy.get('button').contains('🌱 Ingresar al Sistema').click();
    cy.get('#toast').should('contain', 'Ingresa tu número de teléfono');
  });

  it('Debe validar código vacío', () => {
    cy.get('#tel-input').type('50212345678');
    cy.get('button').contains('🌱 Ingresar al Sistema').click();
    cy.get('#toast').should('contain', 'Ingresa el código SMS');
  });

  it('Debe validar formato de código incorrecto', () => {
    cy.get('#tel-input').type('50212345678');
    cy.get('#codigo-input').type('12');
    cy.get('button').contains('🌱 Ingresar al Sistema').click();
    cy.get('#toast').should('contain', '4-6 dígitos');
  });

  it('Debe rechazar código incorrecto', () => {
    cy.get('#tel-input').type('50212345678');
    cy.get('#codigo-input').type('5678');
    cy.get('button').contains('🌱 Ingresar al Sistema').click();
    cy.get('#toast').should('contain', 'incorrecto');
  });

  it('Debe hacer login exitosamente', () => {
    cy.get('#tel-input').type('50212345678');
    cy.get('#codigo-input').type('1234');
    cy.get('button').contains('🌱 Ingresar al Sistema').click();
    cy.get('#toast').should('contain', '¡Bienvenido');
    cy.get('.screen#screen-home').should('be.visible');
  });

  it('Debe enviar SMS correctamente', () => {
    cy.get('#tel-input').type('50212345678');
    cy.get('button').contains('📨 Enviar').click();
    cy.get('#toast').should('contain', '📨 Código enviado');
    cy.get('#codigo-input').should('have.value', '1234');
  });

  it('Debe validar teléfono antes de enviar SMS', () => {
    cy.get('button').contains('📨 Enviar').click();
    cy.get('#toast').should('contain', 'Ingresa tu número primero');
  });

});
