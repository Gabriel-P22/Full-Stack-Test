import { afterEach, describe, expect, it, vi } from 'vitest'
import { clearStoredFormValues, loadStoredFormValues, persistFormValues } from './storage'

interface Sample {
  fullName: string
}

afterEach(() => {
  window.localStorage.clear()
  vi.restoreAllMocks()
})

describe('persistFormValues / loadStoredFormValues', () => {
  it('round-trips values through localStorage', () => {
    persistFormValues({ fullName: 'Maria Souza' })
    expect(loadStoredFormValues<Sample>()).toEqual({ fullName: 'Maria Souza' })
  })

  it('returns undefined when nothing was stored', () => {
    expect(loadStoredFormValues<Sample>()).toBeUndefined()
  })

  it('returns undefined when stored value is not valid JSON', () => {
    window.localStorage.setItem('verity:cadastro-wizard', '{not-json')
    expect(loadStoredFormValues<Sample>()).toBeUndefined()
  })

  it('silently ignores a localStorage write failure', () => {
    vi.spyOn(window.localStorage.__proto__, 'setItem').mockImplementation(() => {
      throw new Error('quota exceeded')
    })
    expect(() => persistFormValues({ fullName: 'Maria' })).not.toThrow()
  })
})

describe('clearStoredFormValues', () => {
  it('removes the stored value', () => {
    persistFormValues({ fullName: 'Maria Souza' })
    clearStoredFormValues()
    expect(loadStoredFormValues<Sample>()).toBeUndefined()
  })
})
