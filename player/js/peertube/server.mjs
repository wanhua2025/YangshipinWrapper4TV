import http from 'http';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import httpModule from 'http';
import https from 'https';

const __dirname = path.dirname(fileURLToPath(import.meta.url));

const mimeTypes = {
  '.html': 'text/html',
  '.js': 'application/javascript',
  '.css': 'text/css',
  '.json': 'application/json',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.svg': 'image/svg+xml',
  '.mjs': 'application/javascript',
  '.map': 'application/json',
  '.m3u8': 'application/vnd.apple.mpegurl',
  '.ts': 'video/mp2t',
  '.mp4': 'video/mp4',
  '.webm': 'video/webm'
};

function proxyRequest(req, res, targetUrl) {
  const parsedUrl = new URL(targetUrl);
  const isHttps = parsedUrl.protocol === 'https:';
  const client = isHttps ? https : httpModule;
  
  const options = {
    hostname: parsedUrl.hostname,
    port: parsedUrl.port || (isHttps ? 443 : 80),
    path: parsedUrl.pathname + parsedUrl.search,
    method: req.method,
    headers: {
      ...req.headers,
      host: parsedUrl.host,
      origin: parsedUrl.origin,
      referer: parsedUrl.origin + '/'
    }
  };

  const proxyReq = client.request(options, (proxyRes) => {
    const headers = { ...proxyRes.headers };
    headers['Access-Control-Allow-Origin'] = '*';
    headers['Access-Control-Allow-Methods'] = 'GET, POST, OPTIONS';
    headers['Access-Control-Allow-Headers'] = 'Content-Type, Range';
    headers['Access-Control-Expose-Headers'] = 'Content-Length, Content-Range';
    
    delete headers['content-security-policy'];
    delete headers['x-frame-options'];
    
    res.writeHead(proxyRes.statusCode, headers);
    proxyRes.pipe(res);
  });

  proxyReq.on('error', (err) => {
    console.error('Proxy error:', err.message);
    res.writeHead(502);
    res.end('Proxy Error: ' + err.message);
  });

  req.pipe(proxyReq);
}

const server = http.createServer((req, res) => {
  console.log(`${req.method} ${req.url}`);

  if (req.url.startsWith('/proxy/')) {
    const targetUrl = req.url.replace('/proxy/', '');
    if (targetUrl.startsWith('http://') || targetUrl.startsWith('https://')) {
      proxyRequest(req, res, targetUrl);
      return;
    }
  }

  if (req.method === 'OPTIONS') {
    res.writeHead(200, {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type, Range'
    });
    res.end();
    return;
  }

  let filePath = path.join(__dirname, req.url === '/' ? 'demo.html' : req.url.split('?')[0]);
  const ext = path.extname(filePath).toLowerCase();
  const contentType = mimeTypes[ext] || 'application/octet-stream';

  fs.readFile(filePath, (err, data) => {
    if (err) {
      if (err.code === 'ENOENT') {
        res.writeHead(404);
        res.end('Not Found');
      } else {
        res.writeHead(500);
        res.end('Server Error');
      }
      return;
    }

    res.writeHead(200, {
      'Content-Type': contentType,
      'Cache-Control': 'no-cache',
      'Access-Control-Allow-Origin': '*'
    });
    res.end(data);
  });
});

const PORT = 8080;
server.listen(PORT, () => {
  console.log(`✅ 测试服务器运行在 http://localhost:${PORT}`);
  console.log(`🎬 演示页面: http://localhost:${PORT}/demo.html`);
  console.log(`🧪 测试页面: http://localhost:${PORT}/test.html`);
  console.log(`🔀 代理地址: http://localhost:${PORT}/proxy/{URL}`);
});
