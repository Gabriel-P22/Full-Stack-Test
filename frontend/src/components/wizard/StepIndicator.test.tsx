import { describe, expect, it } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { StepIndicator } from './StepIndicator'

describe('StepIndicator', () => {
  it('marks the current step as active and earlier steps as done', () => {
    render(
      <MemoryRouter initialEntries={['/etapa-2']}>
        <StepIndicator />
      </MemoryRouter>,
    )

    expect(screen.getByText('Endereço').closest('li')).toHaveClass('step-active')
    expect(screen.getByText('Dados Pessoais').closest('li')).toHaveClass('step-done')
    expect(screen.getByText('Profissional').closest('li')).toHaveClass('step-pending')
  })

  it('renders every step label', () => {
    render(
      <MemoryRouter initialEntries={['/resumo']}>
        <StepIndicator />
      </MemoryRouter>,
    )

    expect(screen.getByText('Resumo').closest('li')).toHaveClass('step-active')
  })
})
