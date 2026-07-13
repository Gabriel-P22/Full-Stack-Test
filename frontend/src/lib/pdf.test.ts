import { describe, expect, it, vi } from 'vitest'
import type { FormValues } from '../schemas/formSchema'

const { docMock, MockJsPDF } = vi.hoisted(() => {
  const docMock = {
    internal: {
      pageSize: { getWidth: () => 210, getHeight: () => 297 },
    },
    setFillColor: vi.fn(),
    rect: vi.fn(),
    setTextColor: vi.fn(),
    setFont: vi.fn(),
    setFontSize: vi.fn(),
    text: vi.fn(),
    setDrawColor: vi.fn(),
    line: vi.fn(),
    save: vi.fn(),
  }
  class MockJsPDF {
    constructor() {
      return docMock
    }
  }
  return { docMock, MockJsPDF }
})

vi.mock('jspdf', () => ({ jsPDF: MockJsPDF }))

const { generateSummaryPdf } = await import('./pdf')

const values: FormValues = {
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

describe('generateSummaryPdf', () => {
  it('draws the branded header and every field, then saves the file', () => {
    generateSummaryPdf(values)

    expect(docMock.text).toHaveBeenCalledWith('verity', expect.any(Number), expect.any(Number))
    expect(docMock.text).toHaveBeenCalledWith('Maria da Silva Souza', expect.any(Number), expect.any(Number))
    expect(docMock.text).toHaveBeenCalledWith('São Paulo', expect.any(Number), expect.any(Number))
    expect(docMock.save).toHaveBeenCalledWith('verity-cadastro-maria-da-silva-souza.pdf')
  })

  it('falls back to a generic filename when the name is blank', () => {
    generateSummaryPdf({ ...values, fullName: '' })
    expect(docMock.save).toHaveBeenCalledWith('verity-cadastro-cadastro.pdf')
  })
})
