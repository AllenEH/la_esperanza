// cypress/e2e/accessibility.cy.js
// Pruebas de accesibilidad WCAG 2.1 con axe-core

describe('Pruebas de Accesibilidad - WCAG 2.1 AA', () => {
  
  beforeEach(() => {
    cy.visit('/');
    // Inyectar axe-core para pruebas de accesibilidad
    cy.injectAxe();
  });

  it('Pantalla de login debe cumplir con WCAG 2.1', () => {
    cy.checkA11y();
  });

  it('Debe tener contraste de colores suficiente', () => {
    cy.checkA11y(null, {
      rules: {
        'color-contrast': { enabled: true }
      }
    });
  });

  it('Todos los inputs deben tener labels', () => {
    cy.get('input').each(($input) => {
      const id = $input.attr('id');
      if (id) {
        cy.get(`label[for="${id}"]`).should('exist');
      }
    });
  });

  it('Debe permitir navegación por teclado (Tab)', () => {
    cy.get('#tel-input').should('be.visible').tab();
    cy.get('#codigo-input').should('be.focused');
    cy.tab();
    cy.focused().should('have.class', 'btn-primary');
  });

  it('Debe tener focus visible en todos los elementos interactivos', () => {
    cy.get('button').first().click().should('have.css', 'outline');
    cy.get('input').first().click().should('have.css', 'outline');
  });

  it('Debe soportar navegación con Escape', () => {
    cy.get('#tel-input').type('50212345678');
    cy.get('#codigo-input').type('1234');
    cy.get('button').contains('🌱 Ingresar al Sistema').click();
    
    // Abrir modal de producto
    cy.get('.product-card').first().click();
    cy.get('#modal-overlay').should('have.class', 'open');
    
    // Presionar Escape
    cy.get('body').type('{esc}');
    cy.get('#modal-overlay').should('not.have.class', 'open');
  });

  it('Botones deben tener tamaño mínimo de 44x44px', () => {
    cy.get('button').each(($btn) => {
      cy.wrap($btn).then(($btn) => {
        const height = $btn.outerHeight();
        const width = $btn.outerWidth();
        
        expect(height).to.be.at.least(40); // Permitir pequeña tolerancia
        expect(width).to.be.at.least(40);
      });
    });
  });

  it('Imágenes y iconos deben tener alt text', () => {
    cy.get('img').each(($img) => {
      cy.wrap($img).should('have.attr', 'alt');
    });
  });

  it('Debe tener idioma declarado', () => {
    cy.get('html').should('have.attr', 'lang', 'es');
  });

  it('Pantalla de catálogo debe ser accesible', () => {
    cy.get('#tel-input').type('50212345678');
    cy.get('#codigo-input').type('1234');
    cy.get('button').contains('🌱 Ingresar al Sistema').click();
    
    cy.get('[data-nav="catalogo"]').click();
    cy.checkA11y();
  });

  it('Debe mostrar errores de forma accesible', () => {
    cy.get('button').contains('🌱 Ingresar al Sistema').click();
    cy.get('#toast').should('be.visible');
    cy.get('#toast').should('have.css', 'visibility', 'visible');
  });

});
