import { useEffect, useState } from 'react'
import { TextField } from '../components/form/TextField'
import { SelectField } from '../components/form/SelectField'
import { CurrencyField } from '../components/form/CurrencyField'
import { WizardActions } from '../components/wizard/WizardActions'
import { WizardStepShell } from '../components/wizard/WizardStepShell'
import { useStepNavigation } from '../hooks/useStepNavigation'
import { useRequireStepData } from '../hooks/useRequireStepData'
import { STEP3_FIELDS } from '../schemas/formSchema'
import { TEMPO_EMPRESA_OPTIONS, type SelectOption } from '../constants/options'
import { fetchProfessions } from '../lib/api'

export function Step3Professional() {
  useRequireStepData('endereco', '/etapa-2')
  const { goNext, goBack } = useStepNavigation(STEP3_FIELDS)
  const [professionOptions, setProfessionOptions] = useState<SelectOption[]>([])
  const [loadingProfessions, setLoadingProfessions] = useState(true)

  useEffect(() => {
    let cancelled = false

    fetchProfessions()
      .then((professions) => {
        if (cancelled) return
        setProfessionOptions(professions.map((p) => ({ value: p.name, label: p.name })))
      })
      .catch(() => {
        if (!cancelled) setProfessionOptions([])
      })
      .finally(() => {
        if (!cancelled) setLoadingProfessions(false)
      })

    return () => {
      cancelled = true
    }
  }, [])

  return (
    <WizardStepShell title="Informações Profissionais" description="Conte-nos um pouco sobre sua vida profissional.">
      <div className="field-grid">
        <TextField name="empresa" label="Empresa" fullWidth />
        <SelectField
          name="profissao"
          label="Profissão"
          options={professionOptions}
          placeholder={loadingProfessions ? 'Carregando profissões...' : 'Selecione...'}
          disabled={loadingProfessions}
        />
        <CurrencyField name="salario" label="Salário" />
        <SelectField name="tempoDeEmpresa" label="Tempo de empresa" options={TEMPO_EMPRESA_OPTIONS} fullWidth />
      </div>
      <WizardActions onBack={() => goBack('/etapa-2')} onNext={() => goNext('/resumo')} nextLabel="Ver resumo" />
    </WizardStepShell>
  )
}
