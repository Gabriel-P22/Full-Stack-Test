interface SummaryItem {
  label: string
  value: string
}

interface SummarySectionProps {
  title: string
  items: SummaryItem[]
  onEdit: () => void
}

export function SummarySection({ title, items, onEdit }: Readonly<SummarySectionProps>) {
  return (
    <section className="summary-section">
      <div className="summary-section-header">
        <h3>{title}</h3>
        <button type="button" className="btn-link" onClick={onEdit}>
          Editar
        </button>
      </div>
      <dl className="summary-list">
        {items.map((item) => (
          <div className="summary-row" key={item.label}>
            <dt>{item.label}</dt>
            <dd>{item.value || '—'}</dd>
          </div>
        ))}
      </dl>
    </section>
  )
}
