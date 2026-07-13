import type { ReactNode } from 'react'

interface WizardStepShellProps {
  title: string
  description?: string
  children: ReactNode
}

export function WizardStepShell({ title, description, children }: Readonly<WizardStepShellProps>) {
  return (
    <section>
      <h1 className="step-title">{title}</h1>
      {description && <p className="step-description">{description}</p>}
      {children}
    </section>
  )
}
