const STORAGE_PREFIX = 'sv_';
const KEY_SUFFIX = '_k';

function generateSessionKey(): string {
  const raw = [
    navigator.userAgent,
    screen.width,
    screen.height,
    new Date().getTimezoneOffset()
  ].join('|');
  let hash = 0;
  for (let i = 0; i < raw.length; i++) {
    const char = raw.charCodeAt(i);
    hash = ((hash << 5) - hash) + char;
    hash |= 0;
  }
  return Math.abs(hash).toString(36);
}

function xorEncode(value: string, key: string): string {
  let result = '';
  for (let i = 0; i < value.length; i++) {
    result += String.fromCharCode(
      value.charCodeAt(i) ^ key.charCodeAt(i % key.length)
    );
  }
  return btoa(result);
}

function xorDecode(encoded: string, key: string): string {
  try {
    const decoded = atob(encoded);
    let result = '';
    for (let i = 0; i < decoded.length; i++) {
      result += String.fromCharCode(
        decoded.charCodeAt(i) ^ key.charCodeAt(i % key.length)
      );
    }
    return result;
  } catch {
    return '';
  }
}

function getKeyForItem(itemKey: string): string {
  const storedKey = sessionStorage.getItem(STORAGE_PREFIX + itemKey + KEY_SUFFIX);
  if (storedKey) return storedKey;
  const newKey = generateSessionKey();
  sessionStorage.setItem(STORAGE_PREFIX + itemKey + KEY_SUFFIX, newKey);
  return newKey;
}

export const secureStorage = {
  setItem(key: string, value: string): void {
    const k = getKeyForItem(key);
    const encoded = xorEncode(value, k);
    sessionStorage.setItem(STORAGE_PREFIX + key, encoded);
  },

  getItem(key: string): string | null {
    const encoded = sessionStorage.getItem(STORAGE_PREFIX + key);
    if (!encoded) return null;
    const k = getKeyForItem(key);
    const decoded = xorDecode(encoded, k);
    return decoded || null;
  },

  removeItem(key: string): void {
    sessionStorage.removeItem(STORAGE_PREFIX + key);
    sessionStorage.removeItem(STORAGE_PREFIX + key + KEY_SUFFIX);
  },

  clear(): void {
    const keysToRemove: string[] = [];
    for (let i = 0; i < sessionStorage.length; i++) {
      const k = sessionStorage.key(i);
      if (k?.startsWith(STORAGE_PREFIX)) {
        keysToRemove.push(k);
      }
    }
    keysToRemove.forEach(k => sessionStorage.removeItem(k));
  }
};
