import { describe, expect, it } from 'vitest'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { renderWithForm } from '../../test/renderWithForm'
import { TextField } from './TextField'

describe('TextField', () => {
  it('renders a label wired to the input', () => {
    renderWithForm(<TextField name="fullName" label="Nome completo" />)
    expect(screen.getByLabelText('Nome completo')).toBeInTheDocument()
  })

  it('lets the user type into the field', async () => {
    const user = userEvent.setup()
    renderWithForm(<TextField name="fullName" label="Nome completo" />)

    const input = screen.getByLabelText('Nome completo')
    await user.type(input, 'Maria Souza')

    expect(input).toHaveValue('Maria Souza')
  })
})
