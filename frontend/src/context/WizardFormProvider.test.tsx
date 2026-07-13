import { describe, expect, it } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { useFormContext } from 'react-hook-form'
import { WizardFormProvider } from './WizardFormProvider'
import type { FormValues } from '../schemas/formSchema'
import { persistFormValues } from '../lib/storage'

function FullNameProbe() {
  const { register, watch } = useFormContext<FormValues>()
  return (
    <div>
      <input aria-label="Nome completo" {...register('fullName')} />
      <span>valor: {watch('fullName')}</span>
    </div>
  )
}

describe('WizardFormProvider', () => {
  it('starts from the default empty values when nothing is stored', () => {
    render(
      <WizardFormProvider>
        <FullNameProbe />
      </WizardFormProvider>,
    )

    expect(screen.getByText('valor:')).toBeInTheDocument()
  })

  it('hydrates from previously persisted values', () => {
    persistFormValues({ fullName: 'Maria Souza' })

    render(
      <WizardFormProvider>
        <FullNameProbe />
      </WizardFormProvider>,
    )

    expect(screen.getByLabelText('Nome completo')).toHaveValue('Maria Souza')
  })

  it('persists changes to localStorage as the user types', async () => {
    const user = userEvent.setup()
    render(
      <WizardFormProvider>
        <FullNameProbe />
      </WizardFormProvider>,
    )

    await user.type(screen.getByLabelText('Nome completo'), 'Ana')

    expect(JSON.parse(window.localStorage.getItem('verity:cadastro-wizard') ?? '{}').fullName).toBe('Ana')
  })
})
