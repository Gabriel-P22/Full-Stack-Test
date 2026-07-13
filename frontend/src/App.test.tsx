import { describe, expect, it, vi, beforeEach } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import App from './App'
import { fetchAddressByCep, fetchProfessions } from './lib/api'
import { generateSummaryPdf } from './lib/pdf'

vi.mock('./lib/api', () => ({
  fetchAddressByCep: vi.fn(),
  fetchProfessions: vi.fn(),
}))
vi.mock('./lib/pdf', () => ({ generateSummaryPdf: vi.fn() }))

beforeEach(() => {
  vi.mocked(fetchAddressByCep).mockReset()
  vi.mocked(fetchProfessions).mockReset()
  vi.mocked(generateSummaryPdf).mockReset()
  window.localStorage.clear()
  window.history.pushState({}, '', '/')
})

describe('App', () => {
  it('completes the full wizard flow end-to-end and exports the PDF', async () => {
    vi.mocked(fetchAddressByCep).mockResolvedValue({
      endereco: 'Avenida Paulista',
      bairro: 'Bela Vista',
      cidade: 'São Paulo',
      estado: 'SP',
    })
    vi.mocked(fetchProfessions).mockResolvedValue([{ id: 1, name: 'Desenvolvedor(a) de Software' }])

    const user = userEvent.setup()
    render(<App />)

    expect(await screen.findByRole('heading', { name: 'Dados Pessoais' })).toBeInTheDocument()

    await user.type(screen.getByLabelText('Nome completo'), 'Maria Souza')
    await user.type(screen.getByLabelText('Data de nascimento'), '15051990')
    await user.type(screen.getByLabelText('CPF'), '52998224725')
    await user.type(screen.getByLabelText('Telefone'), '11987654321')
    await user.type(screen.getByLabelText('E-mail'), 'maria@example.com')
    await user.click(screen.getByText('Próximo'))

    expect(await screen.findByText('Informações Residenciais')).toBeInTheDocument()
    await user.type(screen.getByLabelText('CEP'), '01310100')
    await screen.findByDisplayValue('Avenida Paulista')
    await user.click(screen.getByText('Próximo'))

    expect(await screen.findByText('Informações Profissionais')).toBeInTheDocument()
    await screen.findByRole('option', { name: 'Desenvolvedor(a) de Software' })
    await user.type(screen.getByLabelText('Empresa'), 'Verity Tecnologia')
    await user.selectOptions(screen.getByLabelText('Profissão'), 'Desenvolvedor(a) de Software')
    await user.type(screen.getByLabelText('Salário'), '850000')
    await user.selectOptions(screen.getByLabelText('Tempo de empresa'), 'De 2 a 5 anos')
    await user.click(screen.getByText('Ver resumo'))

    expect(await screen.findByText('Resumo do Cadastro')).toBeInTheDocument()
    expect(screen.getByText('Maria Souza')).toBeInTheDocument()

    await user.click(screen.getByText('Exportar PDF'))
    expect(generateSummaryPdf).toHaveBeenCalledOnce()
  })

  it('redirects unknown routes to step 1', async () => {
    window.history.pushState({}, '', '/rota-invalida')
    render(<App />)
    expect(await screen.findByRole('heading', { name: 'Dados Pessoais' })).toBeInTheDocument()
  })
})
