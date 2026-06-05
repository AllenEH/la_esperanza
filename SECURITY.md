# 🔒 Guía de Seguridad - Código Implementable

Funciones y mejores prácticas de seguridad para La Esperanza.

---

## 1. SANITIZACIÓN & VALIDACIÓN

### 1.1 Sanitizar HTML (XSS Prevention)
```javascript
/**
 * Sanitiza strings para evitar XSS
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

// USO:
const input = '<img src=x onerror="alert(\'XSS\')">';
console.log(sanitizeHTML(input));
// → &lt;img src=x onerror=&quot;alert&#039;XSS&#039;&quot;&gt;
```

### 1.2 Validar Teléfono
```javascript
/**
 * Valida formato de teléfono
 * @param {string} telefono - Número a validar
 * @returns {boolean} Es válido
 */
function validarTelefono(telefono) {
  // Acepta: +502 1234-5678, 50212345678, etc.
  const regex = /^[\+]?[\d\s\-\(\)]{7,20}$/;
  return regex.test(telefono.trim());
}

// USO:
console.log(validarTelefono('+502 1234-5678')); // → true
console.log(validarTelefono('50212345678'));   // → true
console.log(validarTelefono('abc'));           // → false
```

### 1.3 Validar Código SMS
```javascript
/**
 * Valida código SMS (4-6 dígitos)
 * @param {string} codigo - Código a validar
 * @returns {boolean} Es válido
 */
function validarCodigoSMS(codigo) {
  return /^\d{4,6}$/.test(codigo.trim());
}

// USO:
console.log(validarCodigoSMS('1234'));    // → true
console.log(validarCodigoSMS('12'));      // → false
console.log(validarCodigoSMS('1234a'));   // → false
```

### 1.4 Sanitizar Objeto (Todos los campos)
```javascript
/**
 * Sanitiza todos los campos de un objeto
 * @param {object} obj - Objeto a sanitizar
 * @returns {object} Objeto sanitizado
 */
function sanitizarObjeto(obj) {
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

// USO:
const datos = {
  nombre: 'Juan <script>alert("XSS")</script>',
  precio: 100,
  descripcion: 'Producto "especial" & único'
};

console.log(sanitizarObjeto(datos));
// → {
//     nombre: 'Juan &lt;script&gt;alert(&quot;XSS&quot;)&lt;/script&gt;',
//     precio: 100,
//     descripcion: 'Producto &quot;especial&quot; &amp; único'
//   }
```

---

## 2. VALIDACIÓN DE FORMULARIOS

### 2.1 Validar Producto Completo
```javascript
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
  
  // Precio
  if (!producto.precio || isNaN(producto.precio) || producto.precio <= 0) {
    errores.push('Precio debe ser un número mayor a 0');
  }
  
  // Cantidad
  if (producto.cantidad === undefined || producto.cantidad < 0) {
    errores.push('Cantidad no puede ser negativa');
  }
  
  // Categoría
  if (!producto.id_categoria || producto.id_categoria < 1) {
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

// USO:
const producto = {
  nombre: 'Tomates',
  precio: 15.50,
  cantidad: 50,
  id_categoria: 1,
  descripcion: 'Frescos del día'
};

const resultado = validarProducto(producto);
console.log(resultado);
// → { valido: true, errores: [] }

const productoInvalido = { nombre: 'ab', precio: -10, cantidad: 0, id_categoria: 0 };
console.log(validarProducto(productoInvalido));
// → {
//     valido: false,
//     errores: [
//       'Nombre debe tener al menos 3 caracteres',
//       'Precio debe ser un número mayor a 0',
//       'Selecciona una categoría válida'
//     ]
//   }
```

---

## 3. GESTIÓN DE SESIÓN

### 3.1 Gestionar Token (Preparado para JWT)
```javascript
/**
 * Gestor de sesión segura
 */
const SessionManager = {
  
  /**
   * Guardar token de sesión
   * @param {string} token - JWT o token de sesión
   * @param {number} expiresIn - Segundos hasta expiración (default: 1800 = 30 min)
   */
  setToken(token, expiresIn = 1800) {
    const expirationTime = Date.now() + (expiresIn * 1000);
    
    // Usar SessionStorage en vez de LocalStorage (más seguro)
    sessionStorage.setItem('auth_token', token);
    sessionStorage.setItem('auth_expires', expirationTime);
    
    // Para desarrollo
    console.log(`Token guardado. Expira en ${expiresIn}s`);
  },
  
  /**
   * Obtener token válido
   * @returns {string|null} Token si es válido, null si expiró
   */
  getToken() {
    const token = sessionStorage.getItem('auth_token');
    const expiresAt = parseInt(sessionStorage.getItem('auth_expires'));
    
    // Verificar expiración
    if (!token || !expiresAt) return null;
    if (Date.now() > expiresAt) {
      this.clearToken();
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
    console.log('Sesión cerrada');
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

// USO:
SessionManager.setToken('eyJhbGciOiJIUzI1NiIs...', 1800);
console.log(SessionManager.isAuthenticated()); // → true
console.log(SessionManager.getToken());        // → eyJhbGc...
console.log(SessionManager.getTimeRemaining()); // → 1799 (segundos)
SessionManager.clearToken();
```

### 3.2 Validar Permiso por Rol
```javascript
/**
 * Gestor de permisos
 */
const PermisoManager = {
  
  ROLES: {
    PRODUCTOR: 'productor',
    COMPRADOR: 'comprador'
  },
  
  PERMISOS: {
    'productor': ['publicar', 'editar_producto', 'ver_historial'],
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
   */
  hacer(rol, accion, callback) {
    if (!this.tienePermiso(rol, accion)) {
      console.error(`Acceso denegado: ${rol} no puede ${accion}`);
      alert('No tienes permiso para esta acción');
      return false;
    }
    
    callback();
    return true;
  }
};

// USO:
console.log(PermisoManager.tienePermiso('productor', 'publicar'));     // → true
console.log(PermisoManager.tienePermiso('comprador', 'publicar'));     // → false

// Con callback
PermisoManager.hacer('productor', 'publicar', () => {
  console.log('Abriendo formulario de publicación...');
});

// Acceso denegado
PermisoManager.hacer('comprador', 'publicar', () => {
  console.log('Esta línea NO se ejecuta');
});
// → Error: Acceso denegado: comprador no puede publicar
```

---

## 4. AUTENTICACIÓN SEGURA

### 4.1 Simular Login con Validaciones
```javascript
/**
 * Sistema de autenticación (Versión Demo)
 * En producción: Usar backend con OAuth2/JWT
 */
const AuthSystem = {
  
  /**
   * Validar credenciales
   * @param {string} telefono - Número de teléfono
   * @param {string} codigo - Código SMS
   * @returns {object|null} Usuario si credenciales válidas
   */
  async login(telefono, codigo) {
    // Validar entrada
    if (!validarTelefono(telefono)) {
      return { error: 'Teléfono inválido' };
    }
    
    if (!validarCodigoSMS(codigo)) {
      return { error: 'Código SMS debe ser 4-6 dígitos' };
    }
    
    // IMPORTANTE: En producción, esto vendría del backend
    // Aquí es solo demo
    if (codigo !== '1234') {
      return { error: 'Código SMS incorrecto' };
    }
    
    try {
      // Buscar usuario en base de datos
      const usuario = App.db.usuarios.find(u => u.telefono === telefono);
      
      if (!usuario) {
        return { error: 'Usuario no registrado' };
      }
      
      // Generar token simulado (en producción: JWT del backend)
      const token = this.generarTokenDemo(usuario.id_usuario);
      
      // Guardar sesión
      SessionManager.setToken(token, 1800); // 30 minutos
      
      // Guardar usuario actual
      App.usuarioActual = {
        id: usuario.id_usuario,
        nombre: usuario.nombre,
        telefono: usuario.telefono,
        rol: usuario.rol,
        reputacion: usuario.reputacion
      };
      
      return {
        success: true,
        usuario: App.usuarioActual
      };
      
    } catch (error) {
      console.error('Error en autenticación:', error);
      return { error: 'Error del servidor' };
    }
  },
  
  /**
   * Generar token simulado (usar JWT en producción)
   * @param {number} userId - ID del usuario
   * @returns {string} Token
   */
  generarTokenDemo(userId) {
    // Base64 de demostración (NO usar en producción)
    const payload = btoa(JSON.stringify({
      userId: userId,
      iat: Math.floor(Date.now() / 1000)
    }));
    
    return `demo.${payload}.${Date.now()}`;
  },
  
  /**
   * Registrar nuevo usuario
   * @param {object} datos - { nombre, telefono, dpi, rol }
   * @returns {object} Resultado del registro
   */
  async registro(datos) {
    // Validar entrada
    if (!datos.nombre || datos.nombre.trim().length < 3) {
      return { error: 'Nombre muy corto' };
    }
    
    if (!validarTelefono(datos.telefono)) {
      return { error: 'Teléfono inválido' };
    }
    
    if (!datos.dpi || datos.dpi.length < 10) {
      return { error: 'DPI inválido' };
    }
    
    if (!['productor', 'comprador'].includes(datos.rol)) {
      return { error: 'Rol inválido' };
    }
    
    // Verificar si usuario ya existe
    if (App.db.usuarios.find(u => u.telefono === datos.telefono)) {
      return { error: 'Teléfono ya registrado' };
    }
    
    // Crear nuevo usuario
    const nuevoUsuario = {
      id_usuario: Math.max(...App.db.usuarios.map(u => u.id_usuario)) + 1,
      nombre: sanitizeHTML(datos.nombre),
      telefono: datos.telefono,
      dpi: datos.dpi,
      rol: datos.rol,
      reputacion: 5.0,
      foto: datos.rol === 'productor' ? '👩‍🌾' : '👨‍🌾'
    };
    
    App.db.usuarios.push(nuevoUsuario);
    
    // Auto login después de registro
    return this.login(datos.telefono, '1234');
  },
  
  /**
   * Logout
   */
  logout() {
    SessionManager.clearToken();
    App.usuarioActual = null;
    console.log('Sesión cerrada correctamente');
  }
};

// USO:
// Login
const resultado = await AuthSystem.login('50212345678', '1234');
if (resultado.success) {
  console.log('Login exitoso:', resultado.usuario);
} else {
  console.error('Error:', resultado.error);
}

// Logout
AuthSystem.logout();
```

---

## 5. PROTECCIÓN DE DATOS

### 5.1 Encrypt/Decrypt (Para datos sensibles)
```javascript
/**
 * NOTA: Para producción, usar: tweetnacl.js o libsodium.js
 * Esto es solo demostración
 */

/**
 * Encriptación simple (Base64 - NO USAR EN PRODUCCIÓN)
 * @param {string} texto - Texto a encriptar
 * @returns {string} Texto encriptado
 */
function encriptarSimple(texto) {
  return btoa(unescape(encodeURIComponent(texto)));
}

/**
 * Desencriptar
 * @param {string} encriptado - Texto encriptado
 * @returns {string} Texto original
 */
function desencriptarSimple(encriptado) {
  return decodeURIComponent(escape(atob(encriptado)));
}

// ADVERTENCIA: Base64 NO es encriptación segura
// Usar en producción: crypto.subtle (Web Crypto API)

// Ejemplo de encriptación real (Futuro):
async function encriptarAES256(texto, clave) {
  // Requiere: crypto.subtle API
  // Implementación compleja - solo para backend
  console.warn('Usar backend para encriptación real');
}
```

### 5.2 Hash de Contraseña (Para futuro)
```javascript
/**
 * NOTA: Las contraseñas NUNCA deben procesarse en frontend
 * Siempre en backend con HTTPS
 * Esto es solo referencia educativa
 */

/**
 * Hash simple con SHA256 (Frontend)
 * @param {string} password - Contraseña a hashear
 * @returns {Promise<string>} Hash SHA256
 */
async function hashContraseña(password) {
  const encoder = new TextEncoder();
  const data = encoder.encode(password);
  const hashBuffer = await crypto.subtle.digest('SHA-256', data);
  
  // Convertir a hexadecimal
  const hashArray = Array.from(new Uint8Array(hashBuffer));
  return hashArray.map(b => b.toString(16).padStart(2, '0')).join('');
}

// USO:
hashContraseña('mi_contraseña_segura')
  .then(hash => console.log('Hash:', hash));
```

---

## 6. IMPLEMENTACIÓN EN APP.JS

### Agregar a tu app.js existente:

```javascript
// ============================================
// SEGURIDAD - Agregar al inicio de app.js
// ============================================

// 1. Sanitizar todos los inputs antes de procesarlos
function procesarNuevoProducto(datos) {
  const datosLimpios = sanitizarObjeto(datos);
  const validacion = validarProducto(datosLimpios);
  
  if (!validacion.valido) {
    mostrarError(validacion.errores.join(', '));
    return false;
  }
  
  return datosLimpios;
}

// 2. Agregar validación a login
function iniciarSesion() {
  const telefono = document.getElementById('tel-input').value;
  const codigo = document.getElementById('codigo-input').value;
  
  AuthSystem.login(telefono, codigo)
    .then(resultado => {
      if (resultado.error) {
        alert(resultado.error);
      } else {
        mostrarPantalla('menu');
      }
    });
}

// 3. Verificar permisos antes de mostrar opciones
function actualizarMenuSegunRol() {
  if (PermisoManager.tienePermiso(App.usuarioActual.rol, 'publicar')) {
    document.getElementById('btn-publicar').style.display = 'block';
  } else {
    document.getElementById('btn-publicar').style.display = 'none';
  }
}

// 4. Proteger almacenamiento de sesión
window.addEventListener('beforeunload', () => {
  // Opcional: Limpiar token al cerrar tab
  // SessionManager.clearToken();
});

// 5. Detectar inactividad (logout automático)
let inactivityTimer;
function resetInactivityTimer() {
  clearTimeout(inactivityTimer);
  inactivityTimer = setTimeout(() => {
    if (SessionManager.getTimeRemaining() <= 0) {
      AuthSystem.logout();
      alert('Sesión expirada por inactividad');
      mostrarPantalla('login');
    }
  }, 30 * 60 * 1000); // 30 minutos
}

document.addEventListener('click', resetInactivityTimer);
document.addEventListener('keydown', resetInactivityTimer);
```

---

## 7. CHECKLIST DE SEGURIDAD

```
✅ Validación de entrada
   └─ Teléfono, código SMS, productos

✅ Sanitización de datos
   └─ Prevenir XSS

✅ Gestión de sesión segura
   └─ SessionStorage, expiración de token

✅ Control de acceso por rol
   └─ Productor vs Comprador

✅ Protección de datos sensibles
   └─ No almacenar DPI/teléfono real en demo

⚠️ JWT tokens (Implementar en backend)

⚠️ Encriptación end-to-end (Futuro)

⚠️ CORS restrictivo (Backend)

⚠️ Rate limiting (Backend)
```

---

## 8. RECURSOS PARA PRODUCCIÓN

### Librerías recomendadas:
```javascript
// 1. Encriptación segura
// npm install tweetnacl crypto-js
import nacl from 'tweetnacl';
import CryptoJS from 'crypto-js';

// 2. JWT
// npm install jsonwebtoken
const jwt = require('jsonwebtoken');

// 3. Validación
// npm install joi
const schema = Joi.object({ /* ... */ });

// 4. Sanitización
// npm install sanitize-html
const sanitizeHtml = require('sanitize-html');

// 5. Autenticación OAuth
// npm install passport passport-oauth2
```

### Backend (Node.js + Express):
```javascript
// Ejemplo: Backend seguro con Express
const express = require('express');
const helmet = require('helmet'); // Headers de seguridad
const cors = require('cors');
const rateLimit = require('express-rate-limit');
const jwt = require('jsonwebtoken');

const app = express();

// Seguridad
app.use(helmet()); // Headers HTTPS, CSP, etc.
app.use(cors({ origin: 'https://usuario.github.io' })); // CORS

// Rate limiting
const limiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutos
  max: 100 // Máximo 100 requests
});
app.use('/api/', limiter);

// Login con JWT
app.post('/api/login', (req, res) => {
  const { telefono, codigo } = req.body;
  
  // Validar con backend
  // ...
  
  // Generar JWT
  const token = jwt.sign(
    { userId: usuario.id, rol: usuario.rol },
    process.env.JWT_SECRET,
    { expiresIn: '30m' }
  );
  
  res.json({ token });
});

// Middleware para proteger rutas
function verificarToken(req, res, next) {
  const token = req.headers.authorization?.split(' ')[1];
  
  if (!token) {
    return res.status(401).json({ error: 'No token' });
  }
  
  try {
    req.user = jwt.verify(token, process.env.JWT_SECRET);
    next();
  } catch (err) {
    res.status(401).json({ error: 'Token inválido' });
  }
}

// Ruta protegida
app.post('/api/productos', verificarToken, (req, res) => {
  // Solo usuarios autenticados pueden publicar
});
```

---

**Última actualización**: Junio 2026
**Versión**: 1.0
**Responsable**: Equipo de Seguridad
