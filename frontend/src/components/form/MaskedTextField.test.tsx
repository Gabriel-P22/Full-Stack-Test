import { describe, expect, it } from 'vitest'
import { screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { renderWithForm } from '../../test/renderWithForm'
import { MaskedTextField } from './MaskedTextField'
import { CPF_MASK, PHONE_MASK } from '../../constants/masks'

describe('MaskedTextField', () => {
  it('applies a fixed pattern mask as the user types', async () => {
    const user = userEvent.setup()
    renderWithForm(<MaskedTextField name="cpf" label="CPF" maskOptions={CPF_MASK} />)

    const input = screen.getByLabelText('CPF')
    await user.type(input, '52998224725')

    expect(input).toHaveValue('529.982.247-25')
  })

  it('picks the right mask variant for a dynamic pattern', async () => {
    const user = userEvent.setup()
    renderWithForm(<MaskedTextField name="phone" label="Telefone" maskOptions={PHONE_MASK} />)

    const input = screen.getByLabelText('Telefone')
    await user.type(input, '11987654321')

    expect(input).toHaveValue('(11) 98765-4321')
  })
})
