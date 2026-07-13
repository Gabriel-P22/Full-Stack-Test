import { describe, expect, it } from 'vitest'
import { getOptionLabel, TEMPO_EMPRESA_OPTIONS, UF_OPTIONS } from './options'

describe('getOptionLabel', () => {
  it('returns the matching label', () => {
    expect(getOptionLabel(UF_OPTIONS, 'SP')).toBe('São Paulo')
    expect(getOptionLabel(TEMPO_EMPRESA_OPTIONS, '2-5-anos')).toBe('De 2 a 5 anos')
  })

  it('falls back to the raw value when there is no match', () => {
    expect(getOptionLabel(UF_OPTIONS, 'ZZ')).toBe('ZZ')
  })
})
