import { describe, expect, it } from 'vitest'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { renderWithForm } from '../../test/renderWithForm'
import { CurrencyField } from './CurrencyField'

describe('CurrencyField', () => {
  it('formats typed digits as BRL currency', async () => {
    const user = userEvent.setup()
    renderWithForm(<CurrencyField name="salario" label="Salário" />)

    const input = screen.getByLabelText('Salário')
    await user.type(input, '850050')

    expect(input).toHaveValue('R$ 8.500,50')
  })

  it('removes the last typed digit on backspace', async () => {
    const user = userEvent.setup()
    renderWithForm(<CurrencyField name="salario" label="Salário" />)

    const input = screen.getByLabelText('Salário')
    await user.type(input, '1000')
    expect(input).toHaveValue('R$ 10,00')

    await user.type(input, '{backspace}')

    expect(input).toHaveValue('R$ 1,00')
  })

  it('clears back to empty once every digit is removed', async () => {
    const user = userEvent.setup()
    renderWithForm(<CurrencyField name="salario" label="Salário" />)

    const input = screen.getByLabelText('Salário')
    await user.type(input, '5')
    expect(input).toHaveValue('R$ 0,05')

    await user.type(input, '{backspace}')

    expect(input).toHaveValue('')
  })
})
