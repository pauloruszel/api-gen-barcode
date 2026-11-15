import { useEffect, useRef, useState } from 'react';
import QrScanner from 'qr-scanner';

export function Camera({ onScan, close }: { onScan: (text: string) => void; close: () => void }) {
  const video = useRef<HTMLVideoElement>(null);
  const callback = useRef(onScan);
  const [error, setError] = useState('');

  useEffect(() => { callback.current = onScan; }, [onScan]);

  useEffect(() => {
    if (!video.current) return;
    let active = true;
    const scanner = new QrScanner(video.current, result => {
      if (!active) return;
      active = false;
      scanner.stop();
      callback.current(result.data);
    }, { preferredCamera: 'environment', highlightScanRegion: true });
    void scanner.start().catch(() => {
      if (active) setError('Não foi possível abrir a câmera. Use HTTPS, permita o acesso ou busque pelo nome.');
    });
    return () => {
      active = false;
      scanner.destroy();
    };
  }, []);

  return <div>
    <video ref={video} aria-label="Imagem da câmera" playsInline muted />
    {error && <p role="alert">{error}</p>}
    <button onClick={close}>Fechar câmera</button>
  </div>;
}
