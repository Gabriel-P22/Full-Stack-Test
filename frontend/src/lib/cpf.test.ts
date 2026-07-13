import { describe, expect, it } from 'vitest'
import { isValidCPF, onlyDigits } from './cpf'

describe('onlyDigits', () => {
  it('strips every non-digit character', () => {
    expect(onlyDigits('529.982.247-25')).toBe('52998224725')
    expect(onlyDigits('(11) 98765-4321')).toBe('11987654321')
  })
})

describe('isValidCPF', () => {
  it('accepts a valid formatted CPF', () => {
    expect(isValidCPF('529.982.247-25')).toBe(true)
  })

  it('accepts a valid unformatted CPF', () => {
    expect(isValidCPF('52998224725')).toBe(true)
  })

  it('rejects a CPF with a wrong check digit', () => {
    expect(isValidCPF('529.982.247-26')).toBe(false)
  })

  it('rejects a CPF made of the same repeated digit', () => {
    expect(isValidCPF('111.111.111-11')).toBe(false)
    expect(isValidCPF('000.000.000-00')).toBe(false)
  })

  it('rejects a CPF with the wrong length', () => {
    expect(isValidCPF('1234567890')).toBe(false)
    expect(isValidCPF('123456789012')).toBe(false)
  })

  it('rejects an empty value', () => {
    expect(isValidCPF('')).toBe(false)
  })
})
