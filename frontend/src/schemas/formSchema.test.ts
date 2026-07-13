import { describe, expect, it } from 'vitest'
import { addressSchema, defaultFormValues, formSchema, personalDataSchema, professionalSchema } from './formSchema'

const validValues = {
  fullName: 'Maria da Silva Souza',
  birthDate: '15/05/1990',
  cpf: '529.982.247-25',
  phone: '(11) 98765-4321',
  email: 'maria@example.com',
  cep: '01310-100',
  endereco: 'Avenida Paulista',
  bairro: 'Bela Vista',
  cidade: 'São Paulo',
  estado: 'SP',
  empresa: 'Verity Tecnologia',
  profissao: 'Desenvolvedor(a) de Software',
  salario: 'R$ 8.500,50',
  tempoDeEmpresa: '2-5-anos',
}

describe('formSchema', () => {
  it('accepts a fully valid payload', () => {
    expect(formSchema.safeParse(validValues).success).toBe(true)
  })

  it('rejects the untouched default values', () => {
    expect(formSchema.safeParse(defaultFormValues).success).toBe(false)
  })
})

describe('personalDataSchema', () => {
  it('requires at least two words for the full name', () => {
    const result = personalDataSchema.safeParse({ ...validValues, fullName: 'Maria' })
    expect(result.success).toBe(false)
  })

  it('rejects an invalid CPF', () => {
    const result = personalDataSchema.safeParse({ ...validValues, cpf: '111.111.111-11' })
    expect(result.success).toBe(false)
  })

  it('rejects a malformed phone number', () => {
    const result = personalDataSchema.safeParse({ ...validValues, phone: '11987654321' })
    expect(result.success).toBe(false)
  })

  it('accepts an 8-digit landline phone number', () => {
    const result = personalDataSchema.safeParse({ ...validValues, phone: '(11) 3456-7890' })
    expect(result.success).toBe(true)
  })

  it('rejects an invalid email', () => {
    const result = personalDataSchema.safeParse({ ...validValues, email: 'not-an-email' })
    expect(result.success).toBe(false)
  })

  it('rejects an impossible birth date', () => {
    const result = personalDataSchema.safeParse({ ...validValues, birthDate: '31/02/2020' })
    expect(result.success).toBe(false)
  })
})

describe('addressSchema', () => {
  it('rejects a malformed CEP', () => {
    const result = addressSchema.safeParse({ ...validValues, cep: '01310100' })
    expect(result.success).toBe(false)
  })

  it('rejects an unknown UF', () => {
    const result = addressSchema.safeParse({ ...validValues, estado: 'XX' })
    expect(result.success).toBe(false)
  })

  it('rejects an empty bairro', () => {
    const result = addressSchema.safeParse({ ...validValues, bairro: '' })
    expect(result.success).toBe(false)
  })
})

describe('professionalSchema', () => {
  it('rejects a zero salary', () => {
    const result = professionalSchema.safeParse({ ...validValues, salario: 'R$ 0,00' })
    expect(result.success).toBe(false)
  })

  it('rejects a missing profession', () => {
    const result = professionalSchema.safeParse({ ...validValues, profissao: '' })
    expect(result.success).toBe(false)
  })

  it('rejects a missing tempoDeEmpresa', () => {
    const result = professionalSchema.safeParse({ ...validValues, tempoDeEmpresa: '' })
    expect(result.success).toBe(false)
  })
})
