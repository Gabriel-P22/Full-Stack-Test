import { TextField } from '../components/form/TextField'
import { MaskedTextField } from '../components/form/MaskedTextField'
import { WizardActions } from '../components/wizard/WizardActions'
import { WizardStepShell } from '../components/wizard/WizardStepShell'
import { useStepNavigation } from '../hooks/useStepNavigation'
import { STEP1_FIELDS } from '../schemas/formSchema'
import { CPF_MASK, DATE_MASK, PHONE_MASK } from '../constants/masks'

export function Step1PersonalData() {
  const { goNext } = useStepNavigation(STEP1_FIELDS)

  return (
    <WizardStepShell title="Dados Pessoais" description="Preencha suas informações pessoais para começar.">
      <div className="field-grid">
        <TextField name="fullName" label="Nome completo" autoComplete="name" fullWidth />
        <MaskedTextField
          name="birthDate"
          label="Data de nascimento"
          maskOptions={DATE_MASK}
          placeholder="dd/mm/aaaa"
          inputMode="numeric"
        />
        <MaskedTextField name="cpf" label="CPF" maskOptions={CPF_MASK} placeholder="000.000.000-00" inputMode="numeric" />
        <MaskedTextField
          name="phone"
          label="Telefone"
          maskOptions={PHONE_MASK}
          placeholder="(00) 00000-0000"
          inputMode="numeric"
        />
        <TextField name="email" label="E-mail" type="email" autoComplete="email" />
      </div>
      <WizardActions onNext={() => goNext('/etapa-2')} />
    </WizardStepShell>
  )
}
