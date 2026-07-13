const STORAGE_KEY = 'verity:cadastro-wizard'

export function loadStoredFormValues<T>(): Partial<T> | undefined {
  try {
    const raw = window.localStorage.getItem(STORAGE_KEY)
    if (!raw) return undefined
    return JSON.parse(raw) as Partial<T>
  } catch {
    return undefined
  }
}

export function persistFormValues(values: unknown): void {
  try {
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(values))
  } catch {}
}

export function clearStoredFormValues(): void {
  window.localStorage.removeItem(STORAGE_KEY)
}
