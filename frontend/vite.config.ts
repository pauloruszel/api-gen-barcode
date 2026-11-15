import { defineConfig, type ProxyOptions } from 'vite';

const target = new URL(process.env.API_ORIGIN ?? 'http://localhost:8080').origin;
const proxy: Record<string, ProxyOptions> = {
  '/api': {
    target,
    changeOrigin: true,
    configure(server) {
      server.on('proxyReq', (upstream, incoming) => {
        const { origin, host } = incoming.headers;
        if (host && (origin === 'http://' + host || origin === 'https://' + host)) {
          upstream.setHeader('Origin', target);
        }
      });
    },
  },
};

export default defineConfig({ server: { proxy }, preview: { proxy } });
