import { describe, expect, it, vi, beforeEach } from 'vitest'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Route, Routes } from 'react-router-dom'
import { renderWithForm } from '../test/renderWithForm'
import { Step3Professional } from './Step3Professional'
import { fetchProfessions } from '../lib/api'

vi.mock('../lib/api', () => ({
  fetchProfessions: vi.fn(),
}))

function renderStep3() {
  return renderWithForm(
    <Routes>
      <Route path="/etapa-2" element={<p>Informações Residenciais</p>} />
      <Route path="/etapa-3" element={<Step3Professional />} />
      <Route path="/resumo" element={<p>Resumo do Cadastro</p>} />
    </Routes>,
    { initialPath: '/etapa-3', initialValues: { endereco: 'Avenida Paulista' } },
  )
}

beforeEach(() => {
  vi.mocked(fetchProfessions).mockReset()
})

describe('Step3Professional', () => {
  it('disables the profession select while loading, then populates it', async () => {
    vi.mocked(fetchProfessions).mockResolvedValueOnce([
      { id: 1, name: 'Desenvolvedor(a) de Software' },
      { id: 2, name: 'Analista de Sistemas' },
    ])
    renderStep3()

    expect(screen.getByLabelText('Profissão')).toBeDisabled()

    await screen.findByRole('option', { name: 'Desenvolvedor(a) de Software' })
    expect(screen.getByLabelText('Profissão')).not.toBeDisabled()
    expect(screen.getByRole('option', { name: 'Analista de Sistemas' })).toBeInTheDocument()
  })

  it('formats the salary field as currency', async () => {
    vi.mocked(fetchProfessions).mockResolvedValueOnce([])
    const user = userEvent.setup()
    renderStep3()

    await user.type(screen.getByLabelText('Salário'), '500000')
    expect(screen.getByLabelText('Salário')).toHaveValue('R$ 5.000,00')
  })

  it('navigates to the summary once the step is valid', async () => {
    vi.mocked(fetchProfessions).mockResolvedValueOnce([{ id: 1, name: 'Analista de Sistemas' }])
    const user = userEvent.setup()
    renderStep3()

    await screen.findByRole('option', { name: 'Analista de Sistemas' })
    await user.type(screen.getByLabelText('Empresa'), 'Verity Tecnologia')
    await user.selectOptions(screen.getByLabelText('Profissão'), 'Analista de Sistemas')
    await user.type(screen.getByLabelText('Salário'), '500000')
    await user.selectOptions(screen.getByLabelText('Tempo de empresa'), 'De 2 a 5 anos')
    await user.click(screen.getByText('Ver resumo'))

    expect(await screen.findByText('Resumo do Cadastro')).toBeInTheDocument()
  })
})
