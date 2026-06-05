/* ============================================
   SISTEMA LA ESPERANZA - JavaScript Principal
   Gestión de estado, datos y navegación
   ============================================ */

// ---- ESTADO GLOBAL ----
const App = {
  usuarioActual: null,
  db: null,
  categoriaFiltro: 'todos',
  busqueda: '',
  carritoModal: null
};

// ---- CARGAR DATOS JSON ----
async function cargarDatos() {
  try {
    const res = await fetch('data/db.json');
    App.db = await res.json();
  } catch (e) {
    // Fallback con datos inline
    App.db = getDatosDemo();
  }
}

function getDatosDemo() {
  return {
    usuarios: [
      { id_usuario: 1, nombre: "María López", telefono: "50212345678", dpi: "1234567890101", rol: "productor", reputacion: 4.8, foto: "👩‍🌾" },
      { id_usuario: 2, nombre: "Juan Pérez", telefono: "50287654321", dpi: "9876543210101", rol: "comprador", reputacion: 4.5, foto: "👨‍🌾" },
      { id_usuario: 3, nombre: "Ana García", telefono: "50211112222", dpi: "1122334455667", rol: "productor", reputacion: 4.9, foto: "👩‍🌾" }
    ],
    categorias: [
      { id_categoria: 1, nombre_categoria: "Verduras", descripcion: "Verduras frescas", icono: "🥦" },
      { id_categoria: 2, nombre_categoria: "Frutas", descripcion: "Frutas frescas", icono: "🍎" },
      { id_categoria: 3, nombre_categoria: "Granos", descripcion: "Granos y cereales", icono: "🌽" },
      { id_categoria: 4, nombre_categoria: "Hierbas", descripcion: "Hierbas y especias", icono: "🌿" },
      { id_categoria: 5, nombre_categoria: "Raíces", descripcion: "Tubérculos", icono: "🥕" }
    ],
    productos: [
      { id_producto: 1, nombre: "Tomates Frescos", imagen: "🍅", precio: 15.00, cantidad: 50, id_categoria: 1, id_usuario: 1, descripcion: "Tomates maduros del día" },
      { id_producto: 2, nombre: "Maíz Amarillo", imagen: "🌽", precio: 8.50, cantidad: 200, id_categoria: 3, id_usuario: 1, descripcion: "Maíz cosechado esta semana" },
      { id_producto: 3, nombre: "Aguacate Hass", imagen: "🥑", precio: 25.00, cantidad: 30, id_categoria: 2, id_usuario: 3, descripcion: "Aguacates cremosos" },
      { id_producto: 4, nombre: "Zanahorias", imagen: "🥕", precio: 10.00, cantidad: 80, id_categoria: 5, id_usuario: 3, descripcion: "Zanahorias frescas" },
      { id_producto: 5, nombre: "Cilantro Fresco", imagen: "🌿", precio: 5.00, cantidad: 100, id_categoria: 4, id_usuario: 1, descripcion: "Cilantro aromático" },
      { id_producto: 6, nombre: "Manzanas Rojas", imagen: "🍎", precio: 20.00, cantidad: 60, id_categoria: 2, id_usuario: 3, descripcion: "Manzanas dulces" },
      { id_producto: 7, nombre: "Frijoles Negros", imagen: "🫘", precio: 12.00, cantidad: 150, id_categoria: 3, id_usuario: 1, descripcion: "Frijoles orgánicos" },
      { id_producto: 8, nombre: "Brócoli Verde", imagen: "🥦", precio: 18.00, cantidad: 40, id_categoria: 1, id_usuario: 3, descripcion: "Brócoli nutritivo" }
    ],
    pedidos: [
      { id_pedido: 1, fecha: "2026-05-10", estado: "Entregado", id_usuario: 2, id_producto: 1, cantidad_pedida: 5 },
      { id_pedido: 2, fecha: "2026-05-15", estado: "Aceptado", id_usuario: 2, id_producto: 3, cantidad_pedida: 3 },
      { id_pedido: 3, fecha: "2026-05-18", estado: "Pendiente", id_usuario: 2, id_producto: 6, cantidad_pedida: 10 }
    ],
    calificaciones: [
      { id_calificacion: 1, puntuacion: 5, comentario: "Excelente productor", id_usuario: 1 },
      { id_calificacion: 2, puntuacion: 4, comentario: "Buenos productos", id_usuario: 1 },
      { id_calificacion: 3, puntuacion: 5, comentario: "Muy buena atención", id_usuario: 3 }
    ]
  };
}

// ---- NAVEGACIÓN ENTRE PANTALLAS ----
function mostrarPantalla(id) {
  document.querySelectorAll('.screen').forEach(s => s.classList.remove('active'));
  const pantalla = document.getElementById('screen-' + id);
  if (pantalla) {
    pantalla.classList.add('active');
    // Scroll al inicio
    pantalla.scrollTop = 0;
  }

  // Actualizar nav activo
  document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
  const navItem = document.querySelector(`[data-nav="${id}"]`);
  if (navItem) navItem.classList.add('active');

  // Mostrar/ocultar bottom nav
  const sinNav = ['login', 'registro'];
  const bottomNav = document.getElementById('bottom-nav');
  if (bottomNav) {
    bottomNav.style.display = sinNav.includes(id) ? 'none' : '';
  }
}

// ---- LOGIN ----
function iniciarSesion() {
  const tel = document.getElementById('tel-input').value.trim();
  const codigo = document.getElementById('codigo-input').value.trim();

  // ✅ VALIDACIONES DE SEGURIDAD
  if (!tel) {
    mostrarToast('📱 Ingresa tu número de teléfono', 'error');
    AuditLog.registrar('LOGIN_FALLIDO', 'N/A', { razon: 'teléfono vacío' }, 'warning');
    return;
  }

  if (!validarTelefono(tel)) {
    mostrarToast('📱 Formato de teléfono inválido', 'error');
    AuditLog.registrar('LOGIN_FALLIDO', 'N/A', { razon: 'teléfono inválido', tel: tel.substring(0, 3) + '***' }, 'warning');
    return;
  }

  if (!codigo) {
    mostrarToast('🔐 Ingresa el código SMS', 'error');
    return;
  }

  if (!validarCodigoSMS(codigo)) {
    mostrarToast('🔐 Código debe ser de 4-6 dígitos', 'error');
    AuditLog.registrar('LOGIN_FALLIDO', 'N/A', { razon: 'código inválido' }, 'warning');
    return;
  }

  if (codigo !== '1234') {
    mostrarToast('🔐 Código incorrecto. Usa: 1234 (demo)', 'error');
    AuditLog.registrar('LOGIN_FALLIDO', 'N/A', { razon: 'código incorrecto' }, 'warning');
    return;
  }

  // ✅ BUSCAR Y AUTENTICAR
  let usuario = App.db.usuarios.find(u => u.telefono.includes(tel.replace(/\D/g,'')));
  if (!usuario) {
    usuario = { id_usuario: Date.now(), nombre: "Usuario Demo", telefono: tel, dpi: "0000000000000", rol: "comprador", reputacion: 4.0, foto: "👤" };
  }

  // ✅ GUARDAR SESIÓN SEGURA
  App.usuarioActual = usuario;
  const tokenDemo = btoa(JSON.stringify({ userId: usuario.id_usuario, iat: Math.floor(Date.now() / 1000) }));
  SessionManager.setToken(tokenDemo, 1800); // 30 minutos
  
  localStorage.setItem('la_esperanza_user', JSON.stringify(usuario));
  
  AuditLog.registrar('LOGIN_EXITOSO', usuario.id_usuario, { usuario: usuario.nombre, rol: usuario.rol }, 'info');
  mostrarToast('✅ ¡Bienvenido, ' + usuario.nombre.split(' ')[0] + '!');
  cargarHome();
  mostrarPantalla('home');
}

function enviarSMS() {
  const tel = document.getElementById('tel-input').value.trim();
  
  if (!tel) {
    mostrarToast('📱 Ingresa tu número primero', 'error');
    return;
  }

  if (!validarTelefono(tel)) {
    mostrarToast('📱 Número de teléfono inválido', 'error');
    return;
  }

  mostrarToast('📨 Código enviado: 1234 (demo)');
  document.getElementById('codigo-input').value = '1234';
  AuditLog.registrar('SMS_ENVIADO', 'N/A', { tel: tel.substring(0, 3) + '***' }, 'info');
}

function cerrarSesion() {
  App.usuarioActual = null;
  SessionManager.clearToken();
  localStorage.removeItem('la_esperanza_user');
  AuditLog.registrar('LOGOUT', 'N/A', {}, 'info');
  mostrarToast('👋 Sesión cerrada');
  mostrarPantalla('login');
}

// ---- REGISTRO ----
function registrarUsuario() {
  const nombre = document.getElementById('reg-nombre').value.trim();
  const tel = document.getElementById('reg-tel').value.trim();
  const dpi = document.getElementById('reg-dpi').value.trim();
  const rol = document.getElementById('reg-rol').value;

  // ✅ VALIDACIONES
  const datosUsuario = {
    nombre: sanitizeHTML(nombre),
    telefono: tel,
    dpi,
    rol
  };

  const validacion = validarUsuario(datosUsuario);
  if (!validacion.valido) {
    mostrarToast('⚠️ ' + validacion.errores[0], 'error');
    AuditLog.registrar('REGISTRO_FALLIDO', 'N/A', { razon: validacion.errores[0] }, 'warning');
    return;
  }

  // Verificar si usuario ya existe
  if (App.db.usuarios.find(u => u.telefono.includes(tel.replace(/\D/g,'')))) {
    mostrarToast('📱 Este teléfono ya está registrado', 'error');
    AuditLog.registrar('REGISTRO_FALLIDO', 'N/A', { razon: 'teléfono duplicado' }, 'warning');
    return;
  }

  const nuevoUsuario = {
    id_usuario: Date.now(),
    nombre: datosUsuario.nombre,
    telefono: tel,
    dpi: dpi,
    rol: rol,
    reputacion: 5.0,
    foto: rol === 'productor' ? '👩‍🌾' : '👤'
  };

  App.db.usuarios.push(nuevoUsuario);
  App.usuarioActual = nuevoUsuario;
  
  const tokenDemo = btoa(JSON.stringify({ userId: nuevoUsuario.id_usuario, iat: Math.floor(Date.now() / 1000) }));
  SessionManager.setToken(tokenDemo, 1800);
  
  localStorage.setItem('la_esperanza_user', JSON.stringify(nuevoUsuario));
  
  AuditLog.registrar('REGISTRO_EXITOSO', nuevoUsuario.id_usuario, { usuario: nuevoUsuario.nombre, rol }, 'info');
  mostrarToast('🎉 ¡Registro exitoso, ' + nombre.split(' ')[0] + '!');
  cargarHome();
  mostrarPantalla('home');
}

// ---- HOME ----
function cargarHome() {
  if (!App.usuarioActual || !App.db) return;

  const u = App.usuarioActual;
  document.getElementById('home-nombre').textContent = u.nombre.split(' ')[0];

  // Estadísticas
  const misProd = App.db.productos.filter(p => p.id_usuario === u.id_usuario).length;
  const misPed = App.db.pedidos.filter(p => p.id_usuario === u.id_usuario).length;

  document.getElementById('stat-productos').textContent = misProd || App.db.productos.length;
  document.getElementById('stat-pedidos').textContent = misPed || App.db.pedidos.length;
  document.getElementById('stat-productores').textContent = App.db.usuarios.filter(u => u.rol === 'productor').length;
  document.getElementById('stat-categorias').textContent = App.db.categorias.length;
}

// ---- CATÁLOGO ----
function cargarCatalogo() {
  renderFiltrosCategorias();
  renderProductos();
}

function renderFiltrosCategorias() {
  const contenedor = document.getElementById('filtros-categorias');
  if (!contenedor || !App.db) return;

  const todos = `<button class="cat-chip active" onclick="filtrarCategoria('todos', this)">
    <span class="chip-icon">🌾</span> Todos
  </button>`;

  const chips = App.db.categorias.map(c => `
    <button class="cat-chip" onclick="filtrarCategoria(${c.id_categoria}, this)">
      <span class="chip-icon">${c.icono}</span> ${c.nombre_categoria}
    </button>
  `).join('');

  contenedor.innerHTML = todos + chips;
}

function filtrarCategoria(id, btn) {
  App.categoriaFiltro = id;
  document.querySelectorAll('.cat-chip').forEach(c => c.classList.remove('active'));
  btn.classList.add('active');
  renderProductos();
}

function buscarProducto(valor) {
  App.busqueda = valor.toLowerCase();
  renderProductos();
}

function renderProductos() {
  const grid = document.getElementById('products-grid');
  if (!grid || !App.db) return;

  let productos = App.db.productos;

  if (App.categoriaFiltro !== 'todos') {
    productos = productos.filter(p => p.id_categoria === App.categoriaFiltro);
  }

  if (App.busqueda) {
    productos = productos.filter(p => p.nombre.toLowerCase().includes(App.busqueda));
  }

  if (productos.length === 0) {
    grid.innerHTML = `<div class="empty-state" style="grid-column:1/-1">
      <div class="empty-icon">🔍</div>
      <div class="empty-title">Sin resultados</div>
      <div class="empty-desc">Intenta con otra búsqueda</div>
    </div>`;
    return;
  }

  grid.innerHTML = productos.map((p, i) => {
    const cat = App.db.categorias.find(c => c.id_categoria === p.id_categoria);
    const catNombre = cat ? cat.nombre_categoria : '';
    return `
      <div class="product-card" style="animation-delay:${i * 0.05}s" onclick="abrirModalProducto(${p.id_producto})">
        <div class="product-image">
          <span style="font-size:64px">${p.imagen}</span>
          <span class="product-cat-badge">${catNombre}</span>
        </div>
        <div class="product-info">
          <div class="product-name">${p.nombre}</div>
          <div class="product-price">Q ${p.precio.toFixed(2)} <span>/ libra</span></div>
          <div class="product-qty">📦 ${p.cantidad} disponibles</div>
          <button class="btn-solicitar" onclick="event.stopPropagation(); abrirModalProducto(${p.id_producto})">
            🛒 Solicitar
          </button>
        </div>
      </div>
    `;
  }).join('');
}

// ---- MODAL PRODUCTO ----
function abrirModalProducto(idProducto) {
  const p = App.db.productos.find(p => p.id_producto === idProducto);
  if (!p) return;

  App.carritoModal = { producto: p, cantidad: 1 };

  document.getElementById('modal-emoji').textContent = p.imagen;
  document.getElementById('modal-title').textContent = p.nombre;
  document.getElementById('modal-price').textContent = `Q ${p.precio.toFixed(2)}`;
  document.getElementById('modal-desc').textContent = p.descripcion;
  document.getElementById('modal-qty-value').textContent = 1;

  const cat = App.db.categorias.find(c => c.id_categoria === p.id_categoria);
  document.getElementById('modal-cat').textContent = cat ? `${cat.icono} ${cat.nombre_categoria}` : '';

  document.getElementById('modal-overlay').classList.add('open');
}

function cerrarModal() {
  document.getElementById('modal-overlay').classList.remove('open');
  App.carritoModal = null;
}

function cambiarCantidad(delta) {
  if (!App.carritoModal) return;
  const max = App.carritoModal.producto.cantidad;
  const nueva = Math.max(1, Math.min(max, App.carritoModal.cantidad + delta));
  App.carritoModal.cantidad = nueva;
  document.getElementById('modal-qty-value').textContent = nueva;
}

function confirmarPedido() {
  if (!App.carritoModal || !App.usuarioActual) return;

  // ✅ VALIDAR PERMISO
  if (!PermisoManager.tienePermiso(App.usuarioActual?.rol, 'comprar')) {
    mostrarToast('❌ Solo compradores pueden hacer pedidos', 'error');
    return;
  }

  const { producto, cantidad } = App.carritoModal;

  const nuevoPedido = {
    id_pedido: Date.now(),
    fecha: new Date().toISOString().split('T')[0],
    estado: "Pendiente",
    id_usuario: App.usuarioActual.id_usuario,
    id_producto: producto.id_producto,
    cantidad_pedida: cantidad
  };

  App.db.pedidos.push(nuevoPedido);
  AuditLog.registrar('PEDIDO_CREADO', App.usuarioActual.id_usuario, { producto: producto.nombre, cantidad }, 'info');
  
  cerrarModal();
  mostrarToast(`✅ Pedido de ${cantidad} ${producto.nombre} enviado`);
  renderHistorial();
}

// ---- PUBLICAR PRODUCTO ----
function publicarProducto() {
  const nombre = document.getElementById('pub-nombre').value.trim();
  const precio = parseFloat(document.getElementById('pub-precio').value);
  const cantidad = parseInt(document.getElementById('pub-cantidad').value);
  const categoria = parseInt(document.getElementById('pub-categoria').value);

  // ✅ VALIDAR PERMISO
  if (!PermisoManager.tienePermiso(App.usuarioActual?.rol, 'publicar')) {
    mostrarToast('❌ Solo productores pueden publicar', 'error');
    AuditLog.registrar('PUBLICAR_FALLIDO', App.usuarioActual?.id_usuario, { razon: 'rol insuficiente' }, 'warning');
    return;
  }

  // ✅ VALIDAR DATOS
  const productoValidar = {
    nombre,
    precio,
    cantidad,
    id_categoria: categoria,
    descripcion: 'Producto fresco de la comunidad'
  };

  const validacion = validarProducto(productoValidar);
  if (!validacion.valido) {
    mostrarToast('⚠️ ' + validacion.errores[0], 'error');
    AuditLog.registrar('PUBLICAR_FALLIDO', App.usuarioActual?.id_usuario, { razon: validacion.errores[0] }, 'warning');
    return;
  }

  const emojis = { 1: '🥦', 2: '🍎', 3: '🌽', 4: '🌿', 5: '🥕' };

  const nuevoProducto = {
    id_producto: Date.now(),
    nombre: sanitizeHTML(nombre),
    imagen: emojis[categoria] || '🌾',
    precio,
    cantidad,
    id_categoria: categoria,
    id_usuario: App.usuarioActual?.id_usuario || 1,
    descripcion: sanitizeHTML('Producto fresco de la comunidad')
  };

  App.db.productos.push(nuevoProducto);

  // Reset form
  document.getElementById('pub-nombre').value = '';
  document.getElementById('pub-precio').value = '';
  document.getElementById('pub-cantidad').value = '';
  document.getElementById('pub-categoria').value = '';
  document.getElementById('preview-icon').textContent = '📸';
  document.getElementById('upload-texts').style.display = '';

  AuditLog.registrar('PRODUCTO_PUBLICADO', App.usuarioActual?.id_usuario, { producto: nuevoProducto.nombre, precio, cantidad }, 'info');
  mostrarToast('🌱 ¡Producto publicado exitosamente!');
  renderProductos();
  mostrarPantalla('catalogo');
  cargarCatalogo();
}

function cargarCategoriaSelect() {
  const select = document.getElementById('pub-categoria');
  if (!select || !App.db) return;

  select.innerHTML = '<option value="">Selecciona una categoría</option>' +
    App.db.categorias.map(c =>
      `<option value="${c.id_categoria}">${c.icono} ${c.nombre_categoria}</option>`
    ).join('');
}

// Simular subida de foto
function simularSubirFoto() {
  const iconos = ['🍅', '🥕', '🌽', '🥦', '🍎', '🌿', '🫘', '🥑', '🧅', '🌶️'];
  const icono = iconos[Math.floor(Math.random() * iconos.length)];
  document.getElementById('preview-icon').textContent = icono;
  document.getElementById('upload-texts').style.display = 'none';
  mostrarToast('📷 Foto cargada correctamente');
}

// ---- HISTORIAL ----
function renderHistorial() {
  const contenedor = document.getElementById('historial-lista');
  if (!contenedor || !App.db || !App.usuarioActual) return;

  const pedidos = App.db.pedidos.filter(p => p.id_usuario === App.usuarioActual.id_usuario);

  if (pedidos.length === 0) {
    contenedor.innerHTML = `
      <div class="empty-state">
        <div class="empty-icon">📋</div>
        <div class="empty-title">Sin pedidos aún</div>
        <div class="empty-desc">¡Solicita productos del catálogo!</div>
      </div>`;
    return;
  }

  const estadoConfig = {
    'Pendiente': { clase: 'pendiente', icono: '⏳' },
    'Aceptado': { clase: 'aceptado', icono: '✅' },
    'Rechazado': { clase: 'rechazado', icono: '❌' },
    'Entregado': { clase: 'entregado', icono: '📦' }
  };

  contenedor.innerHTML = pedidos.reverse().map((ped, i) => {
    const prod = App.db.productos.find(p => p.id_producto === ped.id_producto);
    const prodNombre = prod ? prod.nombre : 'Producto';
    const prodEmoji = prod ? prod.imagen : '🌾';
    const est = estadoConfig[ped.estado] || { clase: 'pendiente', icono: '⏳' };

    return `
      <div class="order-card" style="animation-delay:${i * 0.07}s">
        <div class="order-emoji">${prodEmoji}</div>
        <div class="order-details">
          <div class="order-name">${prodNombre}</div>
          <div class="order-date">📅 ${formatFecha(ped.fecha)} · 📦 Cant: ${ped.cantidad_pedida}</div>
          <span class="status-badge ${est.clase}">${est.icono} ${ped.estado}</span>
        </div>
      </div>`;
  }).join('');
}

function formatFecha(fecha) {
  const d = new Date(fecha + 'T12:00:00');
  return d.toLocaleDateString('es-GT', { day: '2-digit', month: 'short', year: 'numeric' });
}

// ---- PERFIL ----
function cargarPerfil() {
  if (!App.usuarioActual || !App.db) return;
  const u = App.usuarioActual;

  document.getElementById('perfil-avatar').textContent = u.foto || '👤';
  document.getElementById('perfil-nombre').textContent = u.nombre;
  document.getElementById('perfil-rol-badge').textContent = u.rol === 'productor' ? '🌱 Productor' : '🛒 Comprador';
  document.getElementById('perfil-tel').textContent = u.telefono;
  document.getElementById('perfil-dpi').textContent = u.dpi ? '****' + u.dpi.slice(-4) : 'No registrado';

  // Calificaciones
  const cals = App.db.calificaciones.filter(c => c.id_usuario === u.id_usuario);
  const promedio = cals.length > 0
    ? (cals.reduce((sum, c) => sum + c.puntuacion, 0) / cals.length).toFixed(1)
    : u.reputacion.toFixed(1);

  document.getElementById('perfil-rating-value').textContent = promedio;
  renderEstrellas(parseFloat(promedio));

  // Productos publicados
  const misProd = App.db.productos.filter(p => p.id_usuario === u.id_usuario).length;
  document.getElementById('perfil-productos').textContent = misProd + ' publicados';
}

function renderEstrellas(rating) {
  const contenedor = document.getElementById('perfil-estrellas');
  if (!contenedor) return;

  contenedor.innerHTML = [1, 2, 3, 4, 5].map(n =>
    `<span class="star ${n <= Math.round(rating) ? 'filled' : 'empty'}">⭐</span>`
  ).join('');
}

// ---- TOAST ----
let toastTimer = null;
function mostrarToast(mensaje, tipo = 'success') {
  const toast = document.getElementById('toast');
  if (!toast) return;

  toast.textContent = mensaje;
  toast.className = 'toast' + (tipo === 'error' ? ' error' : '');

  clearTimeout(toastTimer);

  // Forzar reflow
  void toast.offsetWidth;
  toast.classList.add('show');

  toastTimer = setTimeout(() => {
    toast.classList.remove('show');
  }, 3000);
}

// ---- INICIALIZACIÓN ----
document.addEventListener('DOMContentLoaded', async () => {
  console.log('[App] Inicializando La Esperanza...');
  
  await cargarDatos();

  // ✅ VERIFICAR SESIÓN SEGURA
  const savedUser = localStorage.getItem('la_esperanza_user');
  const token = SessionManager.getToken();
  
  if (savedUser && token) {
    try {
      const user = JSON.parse(savedUser);
      
      // Validar que el usuario tenga rol válido
      if (!['productor', 'comprador'].includes(user.rol)) {
        throw new Error('Rol inválido');
      }
      
      App.usuarioActual = user;
      cargarHome();
      cargarCatalogo();
      cargarCategoriaSelect();
      renderHistorial();
      cargarPerfil();
      mostrarPantalla('home');
      
      AuditLog.registrar('SESION_RESTAURADA', user.id_usuario, { usuario: user.nombre }, 'info');
      console.log('[App] Sesión restaurada para:', user.nombre);
    } catch (err) {
      console.error('[App] Error restaurando sesión:', err);
      SessionManager.clearToken();
      localStorage.removeItem('la_esperanza_user');
      mostrarPantalla('login');
    }
  } else {
    mostrarPantalla('login');
  }

  // Cerrar modal al hacer click fuera
  document.getElementById('modal-overlay')?.addEventListener('click', (e) => {
    if (e.target === document.getElementById('modal-overlay')) cerrarModal();
  });

  console.log('[App] La Esperanza lista ✅');
});

// ---- NAVEGACIÓN BOTTOM NAV ----
function navegarA(pantalla) {
  // Cargar datos según pantalla
  switch (pantalla) {
    case 'home':    cargarHome(); break;
    case 'catalogo': cargarCatalogo(); break;
    case 'publicar': cargarCategoriaSelect(); break;
    case 'historial': renderHistorial(); break;
    case 'perfil':  cargarPerfil(); break;
  }
  mostrarPantalla(pantalla);
}
