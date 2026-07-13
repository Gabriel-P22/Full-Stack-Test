import { describe, expect, it } from 'vitest'
import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { Layout } from './Layout'

describe('Layout', () => {
  it('renders the logo, step indicator and page content', () => {
    render(
      <MemoryRouter initialEntries={['/etapa-1']}>
        <Layout>
          <p>conteúdo da etapa</p>
        </Layout>
      </MemoryRouter>,
    )

    expect(screen.getByAltText('Verity')).toBeInTheDocument()
    expect(screen.getByText('Dados Pessoais')).toBeInTheDocument()
    expect(screen.getByText('conteúdo da etapa')).toBeInTheDocument()
    expect(screen.getByText(/Verity\. Todos os direitos reservados\./)).toBeInTheDocument()
  })
})
