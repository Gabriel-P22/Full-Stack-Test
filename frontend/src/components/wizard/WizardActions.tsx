interface WizardActionsProps {
  onBack?: () => void
  onNext: () => void
  nextLabel?: string
  backLabel?: string
}

export function WizardActions({ onBack, onNext, nextLabel = 'Próximo', backLabel = 'Voltar' }: Readonly<WizardActionsProps>) {
  return (
    <div className="wizard-actions">
      {onBack && (
        <button type="button" className="btn btn-secondary" onClick={onBack}>
          {backLabel}
        </button>
      )}
      <button type="button" className="btn btn-primary" onClick={onNext}>
        {nextLabel}
      </button>
    </div>
  )
}
