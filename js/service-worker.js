/* ============================================
   SERVICE WORKER - PWA Offline Support
   La Esperanza - Progressive Web App
   ============================================ */

const CACHE_NAME = 'la-esperanza-v1';
const ASSETS_TO_CACHE = [
  '/',
  '/index.html',
  '/css/styles.css',
  '/js/app.js',
  '/js/security.js',
  '/data/db.json',
  '/manifest.json'
];

// Instalar Service Worker y cachear assets
self.addEventListener('install', (event) => {
  console.log('[SW] Instalando Service Worker...');
  
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => {
      console.log('[SW] Cacheando assets...');
      return cache.addAll(ASSETS_TO_CACHE).catch((err) => {
        console.warn('[SW] Algunos assets no se pudieron cachear:', err);
        // No fallar si faltan algunos assets
        return Promise.resolve();
      });
    })
  );
  
  self.skipWaiting();
});

// Activar Service Worker y limpiar caches antiguos
self.addEventListener('activate', (event) => {
  console.log('[SW] Activando Service Worker...');
  
  event.waitUntil(
    caches.keys().then((cacheNames) => {
      return Promise.all(
        cacheNames.map((cacheName) => {
          if (cacheName !== CACHE_NAME) {
            console.log('[SW] Borrando cache antiguo:', cacheName);
            return caches.delete(cacheName);
          }
        })
      );
    })
  );
  
  self.clients.claim();
});

// Estrategia: Cache First, fallback a Network
self.addEventListener('fetch', (event) => {
  const { request } = event;
  
  // Solo cachear GET requests
  if (request.method !== 'GET') {
    return;
  }
  
  // Ignorar requests a Chrome extensions
  if (request.url.includes('chrome-extension://')) {
    return;
  }
  
  // Estrategia Cache First para assets estáticos
  event.respondWith(
    caches.match(request).then((response) => {
      // Devolver del cache si existe
      if (response) {
        console.log('[SW] Sirviendo del cache:', request.url);
        return response;
      }
      
      // Si no está en cache, intentar red
      return fetch(request)
        .then((networkResponse) => {
          // No cachear si no es exitoso
          if (!networkResponse || networkResponse.status !== 200) {
            return networkResponse;
          }
          
          // Cachear la respuesta exitosa
          const responseToCache = networkResponse.clone();
          caches.open(CACHE_NAME).then((cache) => {
            cache.put(request, responseToCache);
          });
          
          console.log('[SW] Cacheando nuevo asset:', request.url);
          return networkResponse;
        })
        .catch(() => {
          // Si no hay red y no está en cache, mostrar offline page
          console.log('[SW] Offline - asset no disponible:', request.url);
          
          // Retornar un response offline para HTML
          if (request.headers.get('accept')?.includes('text/html')) {
            return new Response(
              '<h1>📡 Sin conexión</h1><p>Este contenido no está disponible offline.</p>',
              {
                headers: { 'Content-Type': 'text/html; charset=utf-8' },
                status: 503,
                statusText: 'Service Unavailable'
              }
            );
          }
          
          // Para otros recursos, devolver error 404
          return new Response('Not Found', { status: 404 });
        });
    })
  );
});

// Mensajes desde el cliente
self.addEventListener('message', (event) => {
  if (event.data && event.data.type === 'SKIP_WAITING') {
    self.skipWaiting();
  }
  
  if (event.data && event.data.type === 'CLEAR_CACHE') {
    caches.delete(CACHE_NAME).then(() => {
      console.log('[SW] Cache limpiado');
    });
  }
});

console.log('[SW] Service Worker cargado ✅');
