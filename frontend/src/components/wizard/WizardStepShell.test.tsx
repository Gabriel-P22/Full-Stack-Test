import { describe, expect, it } from 'vitest'
import { render, screen } from '@testing-library/react'
import { WizardStepShell } from './WizardStepShell'

describe('WizardStepShell', () => {
  it('renders the title, optional description and children', () => {
    render(
      <WizardStepShell title="Dados Pessoais" description="Preencha seus dados">
        <p>conteúdo</p>
      </WizardStepShell>,
    )

    expect(screen.getByRole('heading', { name: 'Dados Pessoais' })).toBeInTheDocument()
    expect(screen.getByText('Preencha seus dados')).toBeInTheDocument()
    expect(screen.getByText('conteúdo')).toBeInTheDocument()
  })

  it('omits the description when not provided', () => {
    render(
      <WizardStepShell title="Resumo">
        <p>conteúdo</p>
      </WizardStepShell>,
    )

    expect(screen.getByRole('heading', { name: 'Resumo' })).toBeInTheDocument()
  })
})
