import { describe, expect, it, vi, beforeEach } from 'vitest'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Route, Routes } from 'react-router-dom'
import { renderWithForm } from '../test/renderWithForm'
import { Step2Address } from './Step2Address'
import { fetchAddressByCep } from '../lib/api'

vi.mock('../lib/api', () => ({
  fetchAddressByCep: vi.fn(),
}))

function renderStep2(initialValues = { fullName: 'Maria Souza' }) {
  return renderWithForm(
    <Routes>
      <Route path="/etapa-1" element={<p>Dados Pessoais</p>} />
      <Route path="/etapa-2" element={<Step2Address />} />
      <Route path="/etapa-3" element={<p>Informações Profissionais</p>} />
    </Routes>,
    { initialPath: '/etapa-2', initialValues },
  )
}

beforeEach(() => {
  vi.mocked(fetchAddressByCep).mockReset()
})

describe('Step2Address', () => {
  it('auto-fills the address fields once the CEP lookup resolves', async () => {
    vi.mocked(fetchAddressByCep).mockResolvedValueOnce({
      endereco: 'Avenida Paulista',
      bairro: 'Bela Vista',
      cidade: 'São Paulo',
      estado: 'SP',
    })
    const user = userEvent.setup()
    renderStep2()

    await user.type(screen.getByLabelText('CEP'), '01310100')

    expect(await screen.findByDisplayValue('Avenida Paulista')).toBeInTheDocument()
    expect(screen.getByDisplayValue('Bela Vista')).toBeInTheDocument()
    expect(screen.getByLabelText('Cidade')).toHaveValue('São Paulo')
    expect(screen.getByLabelText('Estado')).toHaveValue('SP')
  })

  it('shows a not-found hint when no service has the CEP', async () => {
    vi.mocked(fetchAddressByCep).mockResolvedValueOnce(null)
    const user = userEvent.setup()
    renderStep2()

    await user.type(screen.getByLabelText('CEP'), '99999999')

    expect(await screen.findByText('CEP não encontrado, preencha manualmente.')).toBeInTheDocument()
  })

  it('navigates back to step 1', async () => {
    const user = userEvent.setup()
    renderStep2()

    await user.click(screen.getByText('Voltar'))

    expect(await screen.findByText('Dados Pessoais')).toBeInTheDocument()
  })

  it('navigates to step 3 once the address is valid', async () => {
    vi.mocked(fetchAddressByCep).mockResolvedValueOnce({
      endereco: 'Avenida Paulista',
      bairro: 'Bela Vista',
      cidade: 'São Paulo',
      estado: 'SP',
    })
    const user = userEvent.setup()
    renderStep2()

    await user.type(screen.getByLabelText('CEP'), '01310100')
    await screen.findByDisplayValue('Avenida Paulista')
    await user.click(screen.getByText('Próximo'))

    expect(await screen.findByText('Informações Profissionais')).toBeInTheDocument()
  })
})
