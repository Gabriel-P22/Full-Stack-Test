import { describe, expect, it, vi, beforeEach } from 'vitest'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Route, Routes } from 'react-router-dom'
import { renderWithForm } from '../test/renderWithForm'
import { SummaryPage } from './SummaryPage'
import { generateSummaryPdf } from '../lib/pdf'
import { clearStoredFormValues } from '../lib/storage'
import type { FormValues } from '../schemas/formSchema'

vi.mock('../lib/pdf', () => ({ generateSummaryPdf: vi.fn() }))
vi.mock('../lib/storage', () => ({ clearStoredFormValues: vi.fn(), persistFormValues: vi.fn() }))

const filledValues: Partial<FormValues> = {
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

function renderSummary() {
  return renderWithForm(
    <Routes>
      <Route path="/etapa-1" element={<p>Dados Pessoais</p>} />
      <Route path="/etapa-3" element={<p>Informações Profissionais</p>} />
      <Route path="/resumo" element={<SummaryPage />} />
    </Routes>,
    { initialPath: '/resumo', initialValues: filledValues },
  )
}

beforeEach(() => {
  vi.mocked(generateSummaryPdf).mockReset()
  vi.mocked(clearStoredFormValues).mockReset()
})

describe('SummaryPage', () => {
  it('renders every filled value grouped by section, translating select values to labels', () => {
    renderSummary()

    expect(screen.getByText('Maria da Silva Souza')).toBeInTheDocument()
    expect(screen.getByText('529.982.247-25')).toBeInTheDocument()
    expect(screen.getByText('R$ 8.500,50')).toBeInTheDocument()
    expect(screen.getAllByText('São Paulo')).toHaveLength(2)
    expect(screen.getByText('De 2 a 5 anos')).toBeInTheDocument()
  })

  it('exports the PDF with the current form values', async () => {
    const user = userEvent.setup()
    renderSummary()

    await user.click(screen.getByText('Exportar PDF'))

    expect(generateSummaryPdf).toHaveBeenCalledWith(expect.objectContaining({ fullName: 'Maria da Silva Souza' }))
  })

  it('navigates to the matching step when Editar is clicked', async () => {
    const user = userEvent.setup()
    renderSummary()

    await user.click(screen.getAllByText('Editar')[0])

    expect(await screen.findByText('Dados Pessoais')).toBeInTheDocument()
  })

  it('clears the stored data and restarts on "Novo cadastro"', async () => {
    const user = userEvent.setup()
    renderSummary()

    await user.click(screen.getByText('Novo cadastro'))

    expect(clearStoredFormValues).toHaveBeenCalledOnce()
    expect(await screen.findByText('Dados Pessoais')).toBeInTheDocument()
  })
})
