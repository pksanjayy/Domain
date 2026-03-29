/**
 * Polyfills for common Node.js globals required by libraries like SockJS and Stomp.
 */
(window as any).global = window;

(window as any).process = {
  env: { DEBUG: undefined },
  nextTick: function(fn: any) { setTimeout(fn, 0); }
};
