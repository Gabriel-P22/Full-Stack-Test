import type { ReactNode } from 'react'
import { Logo } from './Logo'
import { StepIndicator } from '../wizard/StepIndicator'

export function Layout({ children }: { children: ReactNode }) {
  return (
    <div className="app-shell">
      <header className="app-header">
        <Logo />
      </header>
      <StepIndicator />
      <main className="app-main">
        <div className="card">{children}</div>
      </main>
      <footer className="app-footer">
        <p>&copy; {new Date().getFullYear()} Verity. Todos os direitos reservados.</p>
      </footer>
    </div>
  )
}
