import { describe, expect, it } from 'vitest'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { renderWithForm } from '../../test/renderWithForm'
import { SelectField } from './SelectField'

const options = [
  { value: 'SP', label: 'São Paulo' },
  { value: 'RJ', label: 'Rio de Janeiro' },
]

describe('SelectField', () => {
  it('renders the placeholder plus every option', () => {
    renderWithForm(<SelectField name="estado" label="Estado" options={options} placeholder="UF" />)

    expect(screen.getByRole('option', { name: 'UF' })).toBeInTheDocument()
    expect(screen.getByRole('option', { name: 'São Paulo' })).toBeInTheDocument()
    expect(screen.getByRole('option', { name: 'Rio de Janeiro' })).toBeInTheDocument()
  })

  it('lets the user pick an option', async () => {
    const user = userEvent.setup()
    renderWithForm(<SelectField name="estado" label="Estado" options={options} />)

    await user.selectOptions(screen.getByLabelText('Estado'), 'RJ')

    expect(screen.getByLabelText('Estado')).toHaveValue('RJ')
  })

  it('can be disabled while options are loading', () => {
    renderWithForm(<SelectField name="profissao" label="Profissão" options={[]} disabled />)
    expect(screen.getByLabelText('Profissão')).toBeDisabled()
  })
})
