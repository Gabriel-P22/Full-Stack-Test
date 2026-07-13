import { describe, expect, it } from 'vitest'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Route, Routes } from 'react-router-dom'
import { renderWithForm } from '../test/renderWithForm'
import { useStepNavigation } from './useStepNavigation'
import { TextField } from '../components/form/TextField'
import type { FormValues } from '../schemas/formSchema'

function TestStep({ fields, nextPath }: { fields: (keyof FormValues)[]; nextPath: string }) {
  const { goNext, goBack } = useStepNavigation(fields)
  return (
    <div>
      <TextField name="fullName" label="Nome completo" />
      <button type="button" onClick={() => goNext(nextPath)}>
        avançar
      </button>
      <button type="button" onClick={() => goBack('/etapa-1')}>
        voltar
      </button>
    </div>
  )
}

describe('useStepNavigation', () => {
  it('does not navigate when the required fields are invalid', async () => {
    const user = userEvent.setup()
    renderWithForm(
      <Routes>
        <Route path="/etapa-1" element={<TestStep fields={['fullName']} nextPath="/etapa-2" />} />
        <Route path="/etapa-2" element={<p>chegou na etapa 2</p>} />
      </Routes>,
      { initialPath: '/etapa-1' },
    )

    await user.click(screen.getByText('avançar'))

    expect(screen.queryByText('chegou na etapa 2')).not.toBeInTheDocument()
  })

  it('navigates once the required fields are valid', async () => {
    const user = userEvent.setup()
    renderWithForm(
      <Routes>
        <Route path="/etapa-1" element={<TestStep fields={['fullName']} nextPath="/etapa-2" />} />
        <Route path="/etapa-2" element={<p>chegou na etapa 2</p>} />
      </Routes>,
      { initialPath: '/etapa-1' },
    )

    await user.type(screen.getByLabelText('Nome completo'), 'Maria Souza')
    await user.click(screen.getByText('avançar'))

    expect(await screen.findByText('chegou na etapa 2')).toBeInTheDocument()
  })

  it('goBack always navigates regardless of validity', async () => {
    const user = userEvent.setup()
    renderWithForm(
      <Routes>
        <Route path="/etapa-1" element={<p>etapa 1</p>} />
        <Route path="/etapa-2" element={<TestStep fields={['fullName']} nextPath="/etapa-3" />} />
      </Routes>,
      { initialPath: '/etapa-2' },
    )

    await user.click(screen.getByText('voltar'))

    expect(await screen.findByText('etapa 1')).toBeInTheDocument()
  })
})
