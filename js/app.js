/* ============================================
   SISTEMA LA ESPERANZA - JavaScript Principal
   Gestión de estado, datos y navegación
   ============================================ */

const API_BASE_URL = 'http://localhost:8080/api';

// ---- ESTADO GLOBAL ----
const App = {
  usuarioActual: null,
  db: {
    categorias: [],
    productos: [],
    pedidos: [],
    calificaciones: []
  },
  categoriaFiltro: 'todos',
  busqueda: '',
  carritoModal: null
};

async function apiFetch(path, options = {}) {
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {})
  };

  const token = SessionManager.getToken();
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(API_BASE_URL + path, {
    ...options,
    headers
  });

  if (!response.ok) {
    let errorMessage = response.statusText;
    try {
      const errorBody = await response.json();
      if (errorBody.message) {
        errorMessage = errorBody.message;
      }
    } catch (ignored) {}
    throw new Error(`${response.status} - ${errorMessage}`);
  }

  if (response.status === 204) return null;
  return await response.json();
}

// ---- CARGAR DATOS DEL BACKEND ----
async function cargarDatos() {
  try {
    const [categorias, productosPage] = await Promise.all([
      apiFetch('/categorias'),
      apiFetch('/productos?page=0&size=50')
    ]);

    App.db.categorias = categorias;
    App.db.productos = productosPage.content.map(p => ({
      idProducto: p.idProducto,
      nombre: p.nombre,
      precio: Number(p.precio),
      cantidad: p.cantidad,
      imagen: p.imagen || '🌾',
      descripcion: p.descripcion,
      idCategoria: p.idCategoria,
      nombreCategoria: p.nombreCategoria,
      idUsuario: p.idUsuario,
      nombreUsuario: p.nombreUsuario,
      fechaPublicacion: p.fechaPublicacion,
      disponible: p.disponible
    }));
    App.db.pedidos = [];
    App.db.calificaciones = [];
  } catch (e) {
    console.warn('[App] No es posible conectar con el backend, cargando demo local', e);
    App.db = getDatosDemo();
  }
}

function getDatosDemo() {
  return {
    categorias: [
      { idCategoria: 1, nombreCategoria: 'Verduras', descripcion: 'Verduras frescas', icono: '🥦' },
      { idCategoria: 2, nombreCategoria: 'Frutas', descripcion: 'Frutas frescas', icono: '🍎' },
      { idCategoria: 3, nombreCategoria: 'Granos', descripcion: 'Granos y cereales', icono: '🌽' },
      { idCategoria: 4, nombreCategoria: 'Hierbas', descripcion: 'Hierbas y especias', icono: '🌿' },
      { idCategoria: 5, nombreCategoria: 'Raíces', descripcion: 'Tubérculos', icono: '🥕' }
    ],
    productos: [
      { idProducto: 1, nombre: 'Tomates Frescos', imagen: '🍅', precio: 15.00, cantidad: 50, idCategoria: 1, idUsuario: 1, nombreCategoria: 'Verduras', descripcion: 'Tomates maduros del día' },
      { idProducto: 2, nombre: 'Maíz Amarillo', imagen: '🌽', precio: 8.50, cantidad: 200, idCategoria: 3, idUsuario: 1, nombreCategoria: 'Granos', descripcion: 'Maíz cosechado esta semana' },
      { idProducto: 3, nombre: 'Aguacate Hass', imagen: '🥑', precio: 25.00, cantidad: 30, idCategoria: 2, idUsuario: 3, nombreCategoria: 'Frutas', descripcion: 'Aguacates cremosos' },
      { idProducto: 4, nombre: 'Zanahorias', imagen: '🥕', precio: 10.00, cantidad: 80, idCategoria: 5, idUsuario: 3, nombreCategoria: 'Raíces', descripcion: 'Zanahorias frescas' },
      { idProducto: 5, nombre: 'Cilantro Fresco', imagen: '🌿', precio: 5.00, cantidad: 100, idCategoria: 4, idUsuario: 1, nombreCategoria: 'Hierbas', descripcion: 'Cilantro aromático' },
      { idProducto: 6, nombre: 'Manzanas Rojas', imagen: '🍎', precio: 20.00, cantidad: 60, idCategoria: 2, idUsuario: 3, nombreCategoria: 'Frutas', descripcion: 'Manzanas dulces' },
      { idProducto: 7, nombre: 'Frijoles Negros', imagen: '🫘', precio: 12.00, cantidad: 150, idCategoria: 3, idUsuario: 1, nombreCategoria: 'Granos', descripcion: 'Frijoles orgánicos' },
      { idProducto: 8, nombre: 'Brócoli Verde', imagen: '🥦', precio: 18.00, cantidad: 40, idCategoria: 1, idUsuario: 3, nombreCategoria: 'Verduras', descripcion: 'Brócoli nutritivo' }
    ],
    pedidos: [],
    calificaciones: []
  };
}

async function actualizarUsuarioActual() {
  const perfil = await apiFetch('/usuarios/me');
  App.usuarioActual = perfil;
  localStorage.setItem('la_esperanza_user', JSON.stringify(perfil));
}

async function cargarHistorialBackend() {
  if (!App.usuarioActual) return;
  try {
    const pedidos = await apiFetch('/pedidos/mis-pedidos');
    App.db.pedidos = pedidos;
  } catch (e) {
    console.warn('[App] No se pudo cargar historial de pedidos', e);
  }
}

// ---- NAVEGACIÓN ENTRE PANTALLAS ----
function mostrarPantalla(id) {
  document.querySelectorAll('.screen').forEach(s => s.classList.remove('active'));
  const pantalla = document.getElementById('screen-' + id);
  if (pantalla) {
    pantalla.classList.add('active');
    pantalla.scrollTop = 0;
  }

  document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
  const navItem = document.querySelector(`[data-nav="${id}"]`);
  if (navItem) navItem.classList.add('active');

  const sinNav = ['login', 'registro'];
  const bottomNav = document.getElementById('bottom-nav');
  if (bottomNav) {
    bottomNav.style.display = sinNav.includes(id) ? 'none' : '';
  }
}

// ---- LOGIN ----
async function iniciarSesion() {
  const tel = document.getElementById('tel-input').value.trim();
  const codigo = document.getElementById('codigo-input').value.trim();

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

  try {
    const authResponse = await apiFetch('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ telefono: tel, codigo })
    });

    SessionManager.setToken(authResponse.token, authResponse.expiresIn / 1000);
    await actualizarUsuarioActual();
    await cargarDatos();
    await cargarHistorialBackend();

    AuditLog.registrar('LOGIN_EXITOSO', App.usuarioActual.idUsuario, { usuario: App.usuarioActual.nombre, rol: App.usuarioActual.rol }, 'info');
    mostrarToast('✅ ¡Bienvenido, ' + App.usuarioActual.nombre.split(' ')[0] + '!');
    cargarHome();
    cargarCatalogo();
    cargarCategoriaSelect();
    renderHistorial();
    cargarPerfil();
    mostrarPantalla('home');
  } catch (err) {
    mostrarToast('❌ Error de autenticación: ' + err.message, 'error');
    AuditLog.registrar('LOGIN_FALLIDO', 'N/A', { razon: err.message }, 'warning');
  }
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
async function registrarUsuario() {
  const nombre = document.getElementById('reg-nombre').value.trim();
  const tel = document.getElementById('reg-tel').value.trim();
  const dpi = document.getElementById('reg-dpi').value.trim();
  const rol = document.getElementById('reg-rol').value;

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

  try {
    const authResponse = await apiFetch('/auth/registrar', {
      method: 'POST',
      body: JSON.stringify(datosUsuario)
    });

    SessionManager.setToken(authResponse.token, authResponse.expiresIn / 1000);
    await actualizarUsuarioActual();
    await cargarDatos();
    await cargarHistorialBackend();

    AuditLog.registrar('REGISTRO_EXITOSO', App.usuarioActual.idUsuario, { usuario: App.usuarioActual.nombre, rol }, 'info');
    mostrarToast('🎉 ¡Registro exitoso, ' + App.usuarioActual.nombre.split(' ')[0] + '!');
    cargarHome();
    cargarCatalogo();
    cargarCategoriaSelect();
    renderHistorial();
    cargarPerfil();
    mostrarPantalla('home');
  } catch (err) {
    mostrarToast('❌ Error en el registro: ' + err.message, 'error');
    AuditLog.registrar('REGISTRO_FALLIDO', 'N/A', { razon: err.message }, 'warning');
  }
}

// ---- HOME ----
function cargarHome() {
  if (!App.usuarioActual || !App.db) return;

  const u = App.usuarioActual;
  document.getElementById('home-nombre').textContent = u.nombre.split(' ')[0];

  const misProd = App.db.productos.filter(p => p.idUsuario === u.idUsuario).length;
  const misPed = App.db.pedidos.filter(p => p.idUsuario === u.idUsuario).length;

  document.getElementById('stat-productos').textContent = misProd || App.db.productos.length;
  document.getElementById('stat-pedidos').textContent = misPed || App.db.pedidos.length;
  document.getElementById('stat-productores').textContent = App.db.categorias.length;
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
    <button class="cat-chip" onclick="filtrarCategoria(${c.idCategoria}, this)">
      <span class="chip-icon">${c.icono}</span> ${c.nombreCategoria}
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
    productos = productos.filter(p => p.idCategoria === App.categoriaFiltro);
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
    const cat = App.db.categorias.find(c => c.idCategoria === p.idCategoria);
    const catNombre = cat ? cat.nombreCategoria : '';
    return `
      <div class="product-card" style="animation-delay:${i * 0.05}s" onclick="abrirModalProducto(${p.idProducto})">
        <div class="product-image">
          <span style="font-size:64px">${p.imagen}</span>
          <span class="product-cat-badge">${catNombre}</span>
        </div>
        <div class="product-info">
          <div class="product-name">${p.nombre}</div>
          <div class="product-price">Q ${p.precio.toFixed(2)} <span>/ libra</span></div>
          <div class="product-qty">📦 ${p.cantidad} disponibles</div>
          <button class="btn-solicitar" onclick="event.stopPropagation(); abrirModalProducto(${p.idProducto})">
            🛒 Solicitar
          </button>
        </div>
      </div>
    `;
  }).join('');
}

// ---- MODAL PRODUCTO ----
function abrirModalProducto(idProducto) {
  const p = App.db.productos.find(p => p.idProducto === idProducto);
  if (!p) return;

  App.carritoModal = { producto: p, cantidad: 1 };

  document.getElementById('modal-emoji').textContent = p.imagen;
  document.getElementById('modal-title').textContent = p.nombre;
  document.getElementById('modal-price').textContent = `Q ${p.precio.toFixed(2)}`;
  document.getElementById('modal-desc').textContent = p.descripcion;
  document.getElementById('modal-qty-value').textContent = 1;

  const cat = App.db.categorias.find(c => c.idCategoria === p.idCategoria);
  document.getElementById('modal-cat').textContent = cat ? `${cat.icono} ${cat.nombreCategoria}` : '';

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

async function confirmarPedido() {
  if (!App.carritoModal || !App.usuarioActual) return;

  if (!PermisoManager.tienePermiso(App.usuarioActual?.rol, 'comprar')) {
    mostrarToast('❌ Solo compradores pueden hacer pedidos', 'error');
    return;
  }

  const { producto, cantidad } = App.carritoModal;

  try {
    const pedido = await apiFetch('/pedidos', {
      method: 'POST',
      body: JSON.stringify({ idProducto: producto.idProducto, cantidad, comentario: '' })
    });

    App.db.pedidos.unshift(pedido);
    AuditLog.registrar('PEDIDO_CREADO', App.usuarioActual.idUsuario, { producto: producto.nombre, cantidad }, 'info');
    cerrarModal();
    mostrarToast(`✅ Pedido de ${cantidad} ${producto.nombre} enviado`);
    renderHistorial();
  } catch (err) {
    mostrarToast('❌ Error creando pedido: ' + err.message, 'error');
  }
}

// ---- PUBLICAR PRODUCTO ----
async function publicarProducto() {
  const nombre = document.getElementById('pub-nombre').value.trim();
  const precio = parseFloat(document.getElementById('pub-precio').value);
  const cantidad = parseInt(document.getElementById('pub-cantidad').value);
  const categoria = parseInt(document.getElementById('pub-categoria').value);

  if (!PermisoManager.tienePermiso(App.usuarioActual?.rol, 'publicar')) {
    mostrarToast('❌ Solo productores pueden publicar', 'error');
    AuditLog.registrar('PUBLICAR_FALLIDO', App.usuarioActual?.idUsuario, { razon: 'rol insuficiente' }, 'warning');
    return;
  }

  const productoValidar = {
    nombre,
    precio,
    cantidad,
    idCategoria: categoria,
    descripcion: 'Producto fresco de la comunidad'
  };

  const validacion = validarProducto(productoValidar);
  if (!validacion.valido) {
    mostrarToast('⚠️ ' + validacion.errores[0], 'error');
    AuditLog.registrar('PUBLICAR_FALLIDO', App.usuarioActual?.idUsuario, { razon: validacion.errores[0] }, 'warning');
    return;
  }

  const preview = document.getElementById('preview-icon')?.textContent || '🌾';

  try {
    const producto = await apiFetch('/productos', {
      method: 'POST',
      body: JSON.stringify({
        nombre: sanitizeHTML(nombre),
        precio,
        cantidad,
        idCategoria: categoria,
        descripcion: sanitizeHTML('Producto fresco de la comunidad'),
        imagen: preview
      })
    });

    App.db.productos.unshift(producto);

    document.getElementById('pub-nombre').value = '';
    document.getElementById('pub-precio').value = '';
    document.getElementById('pub-cantidad').value = '';
    document.getElementById('pub-categoria').value = '';
    document.getElementById('preview-icon').textContent = '📸';
    document.getElementById('upload-texts').style.display = '';

    AuditLog.registrar('PRODUCTO_PUBLICADO', App.usuarioActual?.idUsuario, { producto: producto.nombre, precio, cantidad }, 'info');
    mostrarToast('🌱 ¡Producto publicado exitosamente!');
    renderProductos();
    mostrarPantalla('catalogo');
    cargarCatalogo();
  } catch (err) {
    mostrarToast('❌ Error publicando producto: ' + err.message, 'error');
  }
}

function cargarCategoriaSelect() {
  const select = document.getElementById('pub-categoria');
  if (!select || !App.db) return;

  select.innerHTML = '<option value="">Selecciona una categoría</option>' +
    App.db.categorias.map(c =>
      `<option value="${c.idCategoria}">${c.icono} ${c.nombreCategoria}</option>`
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

  const pedidos = App.db.pedidos.filter(p => p.idUsuario === App.usuarioActual.idUsuario);

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
    'Entregado': { clase: 'entregado', icono: '📦' },
    'Cancelado': { clase: 'rechazado', icono: '🚫' }
  };

  contenedor.innerHTML = pedidos.slice().reverse().map((ped, i) => {
    const prod = App.db.productos.find(p => p.idProducto === ped.idProducto);
    const prodNombre = prod ? prod.nombre : ped.nombreProducto || 'Producto';
    const prodEmoji = prod ? prod.imagen : '🌾';
    const est = estadoConfig[ped.estado] || { clase: 'pendiente', icono: '⏳' };

    return `
      <div class="order-card" style="animation-delay:${i * 0.07}s">
        <div class="order-emoji">${prodEmoji}</div>
        <div class="order-details">
          <div class="order-name">${prodNombre}</div>
          <div class="order-date">📅 ${formatFecha(ped.fechaPedido || ped.fecha)} · 📦 Cant: ${ped.cantidadPedida || ped.cantidad_pedida}</div>
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
  const cals = App.db.calificaciones.filter(c => c.idUsuario === u.idUsuario);
  const promedio = cals.length > 0
    ? (cals.reduce((sum, c) => sum + c.puntuacion, 0) / cals.length).toFixed(1)
    : Number(u.reputacion || 0).toFixed(1);

  document.getElementById('perfil-rating-value').textContent = promedio;
  renderEstrellas(parseFloat(promedio));

  // Productos publicados
  const misProd = App.db.productos.filter(p => p.idUsuario === u.idUsuario).length;
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

  const savedUser = localStorage.getItem('la_esperanza_user');
  const token = SessionManager.getToken();
  
  if (savedUser && token) {
    try {
      await actualizarUsuarioActual();
      await cargarHistorialBackend();
      cargarHome();
      cargarCatalogo();
      cargarCategoriaSelect();
      renderHistorial();
      cargarPerfil();
      mostrarPantalla('home');

      const user = App.usuarioActual;
      AuditLog.registrar('SESION_RESTAURADA', user.idUsuario, { usuario: user.nombre }, 'info');
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
