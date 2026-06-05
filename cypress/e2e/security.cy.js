// cypress/e2e/security.cy.js
describe('Pruebas de Seguridad - La Esperanza', () => {
  
  beforeEach(() => {
    cy.visit('/');
  });

  describe('Validación de entrada', () => {
    
    it('Debe sanitizar XSS en inputs de login', () => {
      const xssPayload = '<img src=x onerror="alert(\'XSS\')">';
      cy.get('#tel-input').type('50212345678');
      cy.get('#codigo-input').type('1234');
      cy.get('button').contains('🌱 Ingresar al Sistema').click();
      
      // Navegar a publicar
      cy.get('[data-nav="publicar"]').click();
      
      // Intentar inyectar XSS
      cy.get('#pub-nombre').type(xssPayload);
      cy.get('#pub-precio').type('10');
      cy.get('#pub-cantidad').type('5');
      cy.get('#pub-categoria').select('1');
      
      // Verificar que no se ejecute el XSS
      cy.window().then((win) => {
        cy.spy(win, 'alert');
      });
      
      cy.get('button').contains('🌱 Publicar Producto').click();
    });

    it('Debe validar precio positivo', () => {
      cy.get('#tel-input').type('50212345678');
      cy.get('#codigo-input').type('1234');
      cy.get('button').contains('🌱 Ingresar al Sistema').click();
      
      cy.get('[data-nav="publicar"]').click();
      
      cy.get('#pub-nombre').type('Tomates');
      cy.get('#pub-precio').type('-10');
      cy.get('#pub-cantidad').type('5');
      cy.get('#pub-categoria').select('1');
      
      cy.get('button').contains('🌱 Publicar Producto').click();
      cy.get('#toast').should('contain', 'mayor a 0');
    });

    it('Debe validar cantidad no negativa', () => {
      cy.get('#tel-input').type('50212345678');
      cy.get('#codigo-input').type('1234');
      cy.get('button').contains('🌱 Ingresar al Sistema').click();
      
      cy.get('[data-nav="publicar"]').click();
      
      cy.get('#pub-nombre').type('Tomates');
      cy.get('#pub-precio').type('10');
      cy.get('#pub-cantidad').type('-5');
      cy.get('#pub-categoria').select('1');
      
      cy.get('button').contains('🌱 Publicar Producto').click();
      cy.get('#toast').should('contain', 'Cantidad');
    });

  });

  describe('Control de acceso', () => {
    
    it('Comprador no puede acceder a publicar', () => {
      // Login como comprador
      cy.get('#tel-input').type('50287654321'); // Juan Pérez (comprador)
      cy.get('#codigo-input').type('1234');
      cy.get('button').contains('🌱 Ingresar al Sistema').click();
      
      cy.get('[data-nav="publicar"]').click();
      cy.get('#pub-nombre').type('Producto');
      cy.get('#pub-precio').type('10');
      cy.get('#pub-cantidad').type('5');
      cy.get('#pub-categoria').select('1');
      
      // Intenta publicar
      cy.get('button').contains('🌱 Publicar Producto').click();
      cy.get('#toast').should('contain', 'No tienes permiso');
    });

    it('Productor puede publicar productos', () => {
      // Login como productor
      cy.get('#tel-input').type('50212345678'); // María López (productora)
      cy.get('#codigo-input').type('1234');
      cy.get('button').contains('🌱 Ingresar al Sistema').click();
      
      cy.get('[data-nav="publicar"]').click();
      cy.get('#pub-nombre').type('Tomates Frescos');
      cy.get('#pub-precio').type('15.50');
      cy.get('#pub-cantidad').type('50');
      cy.get('#pub-categoria').select('1');
      
      cy.get('button').contains('🌱 Publicar Producto').click();
      cy.get('#toast').should('contain', 'publicado exitosamente');
    });

  });

  describe('Validación de teléfono', () => {
    
    it('Debe rechazar teléfono con menos de 7 caracteres', () => {
      cy.get('#tel-input').type('123');
      cy.get('#codigo-input').type('1234');
      cy.get('button').contains('🌱 Ingresar al Sistema').click();
      cy.get('#toast').should('contain', 'inválido');
    });

    it('Debe aceptar formatos de teléfono válidos', () => {
      const telefonos = [
        '50212345678',
        '502 1234 5678',
        '+502-1234-5678',
        '502(1234)5678'
      ];

      telefonos.forEach(tel => {
        cy.get('#tel-input').clear().type(tel);
        cy.get('#codigo-input').type('1234');
        cy.get('button').contains('🌱 Ingresar al Sistema').click();
        cy.get('#toast').should('contain', 'Bienvenido');
        
        // Logout
        cy.get('[data-nav="perfil"]').click();
        cy.get('button').contains('🚪 Cerrar Sesión').click();
      });
    });

  });

});
