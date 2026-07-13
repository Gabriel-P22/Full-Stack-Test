import { useEffect, useState } from 'react'
import { useFormContext, useWatch } from 'react-hook-form'
import { TextField } from '../components/form/TextField'
import { SelectField } from '../components/form/SelectField'
import { MaskedTextField } from '../components/form/MaskedTextField'
import { WizardActions } from '../components/wizard/WizardActions'
import { WizardStepShell } from '../components/wizard/WizardStepShell'
import { useStepNavigation } from '../hooks/useStepNavigation'
import { useRequireStepData } from '../hooks/useRequireStepData'
import { STEP2_FIELDS, type FormValues } from '../schemas/formSchema'
import { CEP_MASK } from '../constants/masks'
import { UF_OPTIONS } from '../constants/options'
import { fetchAddressByCep } from '../lib/api'
import { onlyDigits } from '../lib/masks'

type CepStatus = 'idle' | 'loading' | 'success' | 'not-found'

const CEP_STATUS_HINTS: Partial<Record<CepStatus, string>> = {
  loading: 'Buscando endereço...',
  'not-found': 'CEP não encontrado, preencha manualmente.',
}

export function Step2Address() {
  useRequireStepData('fullName', '/etapa-1')
  const { control, setValue } = useFormContext<FormValues>()
  const { goNext, goBack } = useStepNavigation(STEP2_FIELDS)
  const [cepStatus, setCepStatus] = useState<CepStatus>('idle')
  const cep = useWatch({ control, name: 'cep' })

  useEffect(() => {
    const digits = onlyDigits(cep ?? '')
    if (digits.length !== 8) {
      setCepStatus('idle')
      return
    }

    let cancelled = false
    setCepStatus('loading')

    fetchAddressByCep(digits).then((result) => {
      if (cancelled) return
      if (result) {
        setValue('endereco', result.endereco, { shouldValidate: true })
        setValue('bairro', result.bairro, { shouldValidate: true })
        setValue('cidade', result.cidade, { shouldValidate: true })
        setValue('estado', result.estado.toUpperCase(), { shouldValidate: true })
        setCepStatus('success')
      } else {
        setCepStatus('not-found')
      }
    })

    return () => {
      cancelled = true
    }
  }, [cep, setValue])

  return (
    <WizardStepShell
      title="Informações Residenciais"
      description="Informe o CEP para preenchermos o endereço automaticamente."
    >
      <div className="field-grid">
        <MaskedTextField
          name="cep"
          label="CEP"
          maskOptions={CEP_MASK}
          placeholder="00000-000"
          inputMode="numeric"
          fullWidth
          hint={CEP_STATUS_HINTS[cepStatus]}
        />
        <TextField name="endereco" label="Endereço" fullWidth />
        <TextField name="bairro" label="Bairro" />
        <TextField name="cidade" label="Cidade" />
        <SelectField name="estado" label="Estado" options={UF_OPTIONS} placeholder="UF" />
      </div>
      <WizardActions onBack={() => goBack('/etapa-1')} onNext={() => goNext('/etapa-3')} />
    </WizardStepShell>
  )
}
