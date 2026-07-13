import { useLocation } from 'react-router-dom'

const STEPS = [
  { path: '/etapa-1', label: 'Dados Pessoais' },
  { path: '/etapa-2', label: 'Endereço' },
  { path: '/etapa-3', label: 'Profissional' },
  { path: '/resumo', label: 'Resumo' },
]

export function StepIndicator() {
  const { pathname } = useLocation()
  const currentIndex = STEPS.findIndex((step) => step.path === pathname)

  return (
    <ol className="step-indicator">
      {STEPS.map((step, index) => {
        const status = index < currentIndex ? 'done' : index === currentIndex ? 'active' : 'pending'
        return (
          <li key={step.path} className={`step step-${status}`}>
            <span className="step-index">{status === 'done' ? '✓' : index + 1}</span>
            <span className="step-label">{step.label}</span>
          </li>
        )
      })}
    </ol>
  )
}
