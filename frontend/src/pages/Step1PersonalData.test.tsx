import { describe, expect, it } from 'vitest'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Route, Routes } from 'react-router-dom'
import { renderWithForm } from '../test/renderWithForm'
import { Step1PersonalData } from './Step1PersonalData'

function renderStep1() {
  return renderWithForm(
    <Routes>
      <Route path="/etapa-1" element={<Step1PersonalData />} />
      <Route path="/etapa-2" element={<p>Informações Residenciais</p>} />
    </Routes>,
    { initialPath: '/etapa-1' },
  )
}

describe('Step1PersonalData', () => {
  it('renders every personal data field', () => {
    renderStep1()
    expect(screen.getByLabelText('Nome completo')).toBeInTheDocument()
    expect(screen.getByLabelText('Data de nascimento')).toBeInTheDocument()
    expect(screen.getByLabelText('CPF')).toBeInTheDocument()
    expect(screen.getByLabelText('Telefone')).toBeInTheDocument()
    expect(screen.getByLabelText('E-mail')).toBeInTheDocument()
  })

  it('blocks navigation and shows errors when the form is invalid', async () => {
    const user = userEvent.setup()
    renderStep1()

    await user.click(screen.getByText('Próximo'))

    expect(await screen.findByText('Informe o nome completo')).toBeInTheDocument()
    expect(screen.queryByText('Informações Residenciais')).not.toBeInTheDocument()
  })

  it('navigates to step 2 once all fields are valid', async () => {
    const user = userEvent.setup()
    renderStep1()

    await user.type(screen.getByLabelText('Nome completo'), 'Maria Souza')
    await user.type(screen.getByLabelText('Data de nascimento'), '15051990')
    await user.type(screen.getByLabelText('CPF'), '52998224725')
    await user.type(screen.getByLabelText('Telefone'), '11987654321')
    await user.type(screen.getByLabelText('E-mail'), 'maria@example.com')
    await user.click(screen.getByText('Próximo'))

    expect(await screen.findByText('Informações Residenciais')).toBeInTheDocument()
  })
})
