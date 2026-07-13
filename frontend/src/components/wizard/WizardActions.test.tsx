import { describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { WizardActions } from './WizardActions'

describe('WizardActions', () => {
  it('omits the back button when onBack is not provided', () => {
    render(<WizardActions onNext={() => {}} />)
    expect(screen.queryByText('Voltar')).not.toBeInTheDocument()
    expect(screen.getByText('Próximo')).toBeInTheDocument()
  })

  it('calls the provided callbacks', async () => {
    const user = userEvent.setup()
    const onNext = vi.fn()
    const onBack = vi.fn()
    render(<WizardActions onNext={onNext} onBack={onBack} nextLabel="Ver resumo" />)

    await user.click(screen.getByText('Voltar'))
    await user.click(screen.getByText('Ver resumo'))

    expect(onBack).toHaveBeenCalledOnce()
    expect(onNext).toHaveBeenCalledOnce()
  })
})
