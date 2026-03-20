const storagePrefix = 'insightflow.platform'

function getStorage() {
  if (typeof window === 'undefined') {
    return null
  }

  return window.localStorage
}

export function readStorage<T>(key: string, fallback: T): T {
  const storage = getStorage()

  if (!storage) {
    return fallback
  }

  const raw = storage.getItem(`${storagePrefix}.${key}`)

  if (!raw) {
    return fallback
  }

  try {
    return JSON.parse(raw) as T
  } catch {
    return fallback
  }
}

export function writeStorage<T>(key: string, value: T) {
  const storage = getStorage()

  if (!storage) {
    return
  }

  storage.setItem(`${storagePrefix}.${key}`, JSON.stringify(value))
}
