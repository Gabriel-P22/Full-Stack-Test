import { describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { SummarySection } from './SummarySection'

describe('SummarySection', () => {
  it('renders every item label and value', () => {
    render(
      <SummarySection
        title="Dados Pessoais"
        onEdit={() => {}}
        items={[
          { label: 'Nome completo', value: 'Maria Souza' },
          { label: 'E-mail', value: '' },
        ]}
      />,
    )

    expect(screen.getByText('Dados Pessoais')).toBeInTheDocument()
    expect(screen.getByText('Nome completo')).toBeInTheDocument()
    expect(screen.getByText('Maria Souza')).toBeInTheDocument()
    expect(screen.getByText('—')).toBeInTheDocument()
  })

  it('calls onEdit when the edit button is clicked', async () => {
    const user = userEvent.setup()
    const onEdit = vi.fn()
    render(<SummarySection title="Dados Pessoais" onEdit={onEdit} items={[]} />)

    await user.click(screen.getByText('Editar'))

    expect(onEdit).toHaveBeenCalledOnce()
  })
})
