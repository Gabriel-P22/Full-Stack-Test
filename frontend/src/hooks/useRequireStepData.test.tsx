import { describe, expect, it } from 'vitest'
import { screen } from '@testing-library/react'
import { Route, Routes } from 'react-router-dom'
import { renderWithForm } from '../test/renderWithForm'
import { useRequireStepData } from './useRequireStepData'

function GuardedStep() {
  useRequireStepData('fullName', '/etapa-1')
  return <p>conteúdo protegido</p>
}

describe('useRequireStepData', () => {
  it('redirects when the required field is empty', () => {
    renderWithForm(
      <Routes>
        <Route path="/etapa-2" element={<GuardedStep />} />
        <Route path="/etapa-1" element={<p>etapa 1</p>} />
      </Routes>,
      { initialPath: '/etapa-2' },
    )

    expect(screen.getByText('etapa 1')).toBeInTheDocument()
    expect(screen.queryByText('conteúdo protegido')).not.toBeInTheDocument()
  })

  it('renders normally when the required field is already filled', () => {
    renderWithForm(
      <Routes>
        <Route path="/etapa-2" element={<GuardedStep />} />
        <Route path="/etapa-1" element={<p>etapa 1</p>} />
      </Routes>,
      { initialPath: '/etapa-2', initialValues: { fullName: 'Maria Souza' } },
    )

    expect(screen.getByText('conteúdo protegido')).toBeInTheDocument()
  })
})
