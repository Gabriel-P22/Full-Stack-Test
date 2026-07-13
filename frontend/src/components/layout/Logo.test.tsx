import { describe, expect, it } from 'vitest'
import { render, screen } from '@testing-library/react'
import { Logo } from './Logo'

describe('Logo', () => {
  it('renders the Verity logo image', () => {
    render(<Logo />)
    const image = screen.getByAltText('Verity')
    expect(image).toBeInTheDocument()
    expect(image).toHaveAttribute('src', '/logo.avif')
  })
})
