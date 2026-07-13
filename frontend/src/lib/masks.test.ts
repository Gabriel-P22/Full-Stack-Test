import { describe, expect, it } from 'vitest'
import { isValidBirthDate, onlyDigits, parseCurrencyToNumber } from './masks'

describe('onlyDigits', () => {
  it('removes non-numeric characters', () => {
    expect(onlyDigits('01310-100')).toBe('01310100')
  })
})

describe('isValidBirthDate', () => {
  it('accepts a real past date in dd/mm/yyyy format', () => {
    expect(isValidBirthDate('15/05/1990')).toBe(true)
  })

  it('rejects an incomplete date', () => {
    expect(isValidBirthDate('15/05/19')).toBe(false)
    expect(isValidBirthDate('')).toBe(false)
  })

  it('rejects a calendar-impossible date', () => {
    expect(isValidBirthDate('31/02/2020')).toBe(false)
    expect(isValidBirthDate('32/01/2020')).toBe(false)
    expect(isValidBirthDate('01/13/2020')).toBe(false)
  })

  it('rejects a date in the future', () => {
    const future = new Date()
    future.setFullYear(future.getFullYear() + 1)
    const day = String(future.getDate()).padStart(2, '0')
    const month = String(future.getMonth() + 1).padStart(2, '0')
    expect(isValidBirthDate(`${day}/${month}/${future.getFullYear()}`)).toBe(false)
  })

  it('rejects unreasonable years', () => {
    expect(isValidBirthDate('01/01/1800')).toBe(false)
  })
})

describe('parseCurrencyToNumber', () => {
  it('parses a BRL formatted string into a number', () => {
    expect(parseCurrencyToNumber('R$ 8.500,50')).toBe(8500.5)
    expect(parseCurrencyToNumber('R$ 0,00')).toBe(0)
    expect(parseCurrencyToNumber('R$ 1.234.567,89')).toBe(1234567.89)
  })

  it('returns 0 for an unparsable value', () => {
    expect(parseCurrencyToNumber('')).toBe(0)
    expect(parseCurrencyToNumber('abc')).toBe(0)
  })
})
