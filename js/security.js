/* ============================================
   SEGURIDAD - Funciones de Validación y Sanitización
   La Esperanza - Security Module
   ============================================ */

/**
 * Sanitiza strings para prevenir XSS
 * @param {string} str - String a sanitizar
 * @returns {string} String sanitizado
 */
function sanitizeHTML(str) {
  if (typeof str !== 'string') return '';
  
  const map = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#039;'
  };
  
  return str.replace(/[&<>"']/g, m => map[m]);
}

/**
 * Valida formato de teléfono
 * @param {string} telefono - Número a validar
 * @returns {boolean} Es válido
 */
function validarTelefono(telefono) {
  // Acepta: +502 1234-5678, 50212345678, 502-1234-5678, etc.
  const regex = /^[\+]?[\d\s\-\(\)]{7,20}$/;
  return regex.test(telefono.trim());
}

/**
 * Valida código SMS (4-6 dígitos)
 * @param {string} codigo - Código a validar
 * @returns {boolean} Es válido
 */
function validarCodigoSMS(codigo) {
  return /^\d{4,6}$/.test(codigo.trim());
}

/**
 * Valida email básico
 * @param {string} email - Email a validar
 * @returns {boolean} Es válido
 */
function validarEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim());
}

/**
 * Valida DPI (Guatemala: 13 dígitos)
 * @param {string} dpi - DPI a validar
 * @returns {boolean} Es válido
 */
function validarDPI(dpi) {
  return /^\d{13}$/.test(dpi.trim());
}

/**
 * Valida precio (número positivo)
 * @param {number} precio - Precio a validar
 * @returns {boolean} Es válido
 */
function validarPrecio(precio) {
  return !isNaN(precio) && parseFloat(precio) > 0;
}

/**
 * Valida cantidad (número no negativo)
 * @param {number} cantidad - Cantidad a validar
 * @returns {boolean} Es válido
 */
function validarCantidad(cantidad) {
  return !isNaN(cantidad) && parseInt(cantidad) >= 0;
}

/**
 * Sanitiza objeto completo (todos los campos)
 * @param {object} obj - Objeto a sanitizar
 * @returns {object} Objeto sanitizado
 */
function sanitizarObjeto(obj) {
  if (!obj || typeof obj !== 'object') return {};
  
  return Object.keys(obj).reduce((acc, key) => {
    const valor = obj[key];
    
    if (typeof valor === 'string') {
      acc[key] = sanitizeHTML(valor);
    } else if (typeof valor === 'number' || typeof valor === 'boolean') {
      acc[key] = valor;
    } else if (Array.isArray(valor)) {
      acc[key] = valor.map(item => 
        typeof item === 'string' ? sanitizeHTML(item) : item
      );
    }
    
    return acc;
  }, {});
}

/**
 * Valida todos los campos de un producto
 * @param {object} producto - Producto a validar
 * @returns {object} { valido: boolean, errores: string[] }
 */
function validarProducto(producto) {
  const errores = [];
  
  // Nombre
  if (!producto.nombre || producto.nombre.trim().length < 3) {
    errores.push('Nombre debe tener al menos 3 caracteres');
  }
  
  if (producto.nombre && producto.nombre.length > 100) {
    errores.push('Nombre no puede exceder 100 caracteres');
  }
  
  // Precio
  if (!validarPrecio(producto.precio)) {
    errores.push('Precio debe ser un número mayor a 0');
  }
  
  // Cantidad
  if (!validarCantidad(producto.cantidad)) {
    errores.push('Cantidad no puede ser negativa');
  }
  
  // Categoría
  if (!producto.idCategoria || producto.idCategoria < 1) {
    errores.push('Selecciona una categoría válida');
  }
  
  // Descripción (opcional pero limitada)
  if (producto.descripcion && producto.descripcion.length > 500) {
    errores.push('Descripción no puede exceder 500 caracteres');
  }
  
  return {
    valido: errores.length === 0,
    errores: errores
  };
}

/**
 * Valida todos los campos de un usuario
 * @param {object} usuario - Usuario a validar
 * @returns {object} { valido: boolean, errores: string[] }
 */
function validarUsuario(usuario) {
  const errores = [];
  
  // Nombre
  if (!usuario.nombre || usuario.nombre.trim().length < 3) {
    errores.push('Nombre debe tener al menos 3 caracteres');
  }
  
  // Teléfono
  if (!validarTelefono(usuario.telefono)) {
    errores.push('Teléfono inválido');
  }
  
  // DPI
  if (!usuario.dpi || !validarDPI(usuario.dpi)) {
    errores.push('DPI debe tener 13 dígitos');
  }
  
  // Rol
  if (!['productor', 'comprador'].includes(usuario.rol)) {
    errores.push('Rol inválido. Usa: productor o comprador');
  }
  
  return {
    valido: errores.length === 0,
    errores: errores
  };
}

/**
 * Gestor de Sesión Segura
 */
const SessionManager = {
  
  /**
   * Guardar token de sesión
   * @param {string} token - Token de sesión
   * @param {number} expiresIn - Segundos hasta expiración (default: 1800 = 30 min)
   */
  setToken(token, expiresIn = 1800) {
    const expirationTime = Date.now() + (expiresIn * 1000);
    
    // Guardar tanto en SessionStorage como en LocalStorage para recuperación entre pestañas
    sessionStorage.setItem('auth_token', token);
    sessionStorage.setItem('auth_expires', expirationTime);
    localStorage.setItem('token', token);
    localStorage.setItem('token_expires', expirationTime);
    
    console.log(`[Security] Token guardado. Expira en ${expiresIn}s`);
  },
  
  /**
   * Obtener token válido
   * @returns {string|null} Token si es válido, null si expiró
   */
  getToken() {
    const sessionToken = sessionStorage.getItem('auth_token');
    const sessionExpires = parseInt(sessionStorage.getItem('auth_expires'));
    const localToken = localStorage.getItem('token');
    const localExpires = parseInt(localStorage.getItem('token_expires'));

    const token = sessionToken || localToken;
    const expiresAt = sessionExpires || localExpires;
    
    // Verificar expiración
    if (!token || !expiresAt) return null;
    if (Date.now() > expiresAt) {
      this.clearToken();
      console.warn('[Security] Token expirado');
      return null;
    }
    
    return token;
  },
  
  /**
   * Limpiar sesión
   */
  clearToken() {
    sessionStorage.removeItem('auth_token');
    sessionStorage.removeItem('auth_expires');
    localStorage.removeItem('token');
    localStorage.removeItem('token_expires');
    console.log('[Security] Sesión cerrada');
  },
  
  /**
   * Verificar si hay sesión activa
   * @returns {boolean}
   */
  isAuthenticated() {
    return !!this.getToken();
  },
  
  /**
   * Obtener tiempo restante en segundos
   * @returns {number} Segundos restantes
   */
  getTimeRemaining() {
    const expiresAt = parseInt(sessionStorage.getItem('auth_expires'));
    if (!expiresAt) return 0;
    
    const remaining = Math.ceil((expiresAt - Date.now()) / 1000);
    return Math.max(0, remaining);
  }
};

/**
 * Gestor de Permisos por Rol
 */
const PermisoManager = {
  
  ROLES: {
    PRODUCTOR: 'productor',
    COMPRADOR: 'comprador'
  },
  
  PERMISOS: {
    'productor': ['publicar', 'editar_producto', 'ver_historial', 'calificar'],
    'comprador': ['comprar', 'calificar', 'ver_historial']
  },
  
  /**
   * Verificar si usuario tiene permiso
   * @param {string} rol - Rol del usuario
   * @param {string} accion - Acción a verificar
   * @returns {boolean}
   */
  tienePermiso(rol, accion) {
    if (!this.PERMISOS[rol]) return false;
    return this.PERMISOS[rol].includes(accion);
  },
  
  /**
   * Verificar permiso y ejecutar acción
   * @param {string} rol - Rol del usuario
   * @param {string} accion - Acción a verificar
   * @param {function} callback - Función a ejecutar si autorizado
   * @returns {boolean} Si fue autorizado y ejecutado
   */
  hacer(rol, accion, callback) {
    if (!this.tienePermiso(rol, accion)) {
      console.error(`[Security] Acceso denegado: ${rol} no puede ${accion}`);
      mostrarToast(`❌ No tienes permiso para: ${accion}`, 'error');
      return false;
    }
    
    if (typeof callback === 'function') {
      callback();
    }
    
    console.log(`[Security] Acción autorizada: ${rol} → ${accion}`);
    return true;
  },
  
  /**
   * Obtener permisos del rol
   * @param {string} rol - Rol del usuario
   * @returns {array} Lista de permisos
   */
  obtenerPermisos(rol) {
    return this.PERMISOS[rol] || [];
  }
};

/**
 * Log de Auditoría (para debug y futuros reportes)
 */
const AuditLog = {
  
  logs: [],
  
  /**
   * Registrar evento de seguridad
   * @param {string} accion - Acción realizada
   * @param {string} usuario - ID del usuario
   * @param {object} detalles - Detalles del evento
   * @param {string} nivel - Nivel: 'info', 'warning', 'error'
   */
  registrar(accion, usuario, detalles = {}, nivel = 'info') {
    const evento = {
      timestamp: new Date().toISOString(),
      accion,
      usuario,
      detalles,
      nivel,
      navegador: navigator.userAgent.substring(0, 50),
      ip: 'N/A' // En producción: obtener del backend
    };
    
    this.logs.push(evento);
    
    // Mantener últimos 100 eventos en memoria
    if (this.logs.length > 100) {
      this.logs.shift();
    }
    
    // Log en consola según nivel
    const prefijo = `[${nivel.toUpperCase()}]`;
    if (nivel === 'error') {
      console.error(prefijo, accion, detalles);
    } else if (nivel === 'warning') {
      console.warn(prefijo, accion, detalles);
    } else {
      console.log(prefijo, accion, detalles);
    }
  },
  
  /**
   * Obtener logs (últimos N)
   * @param {number} cantidad - Cantidad de logs (default: 20)
   * @returns {array} Eventos de auditoría
   */
  obtener(cantidad = 20) {
    return this.logs.slice(-cantidad);
  },
  
  /**
   * Filtrar logs por nivel
   * @param {string} nivel - Nivel a filtrar
   * @returns {array} Eventos filtrados
   */
  filtrarPorNivel(nivel) {
    return this.logs.filter(e => e.nivel === nivel);
  },
  
  /**
   * Descargar logs como JSON
   */
  descargar() {
    const data = JSON.stringify(this.logs, null, 2);
    const blob = new Blob([data], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `audit-log-${new Date().toISOString()}.json`;
    a.click();
  }
};

console.log('[Security] Módulo de seguridad cargado ✅');
