import { useNavigate } from 'react-router-dom'
import { useFormContext } from 'react-hook-form'
import { WizardStepShell } from '../components/wizard/WizardStepShell'
import { SummarySection } from '../components/wizard/SummarySection'
import { useRequireStepData } from '../hooks/useRequireStepData'
import { defaultFormValues, type FormValues } from '../schemas/formSchema'
import { getOptionLabel, TEMPO_EMPRESA_OPTIONS, UF_OPTIONS } from '../constants/options'
import { generateSummaryPdf } from '../lib/pdf'
import { clearStoredFormValues } from '../lib/storage'

export function SummaryPage() {
  useRequireStepData('profissao', '/etapa-3')
  const { getValues, reset } = useFormContext<FormValues>()
  const navigate = useNavigate()
  const values = getValues()

  function handleExport() {
    generateSummaryPdf(values)
  }

  function handleNewRegistration() {
    clearStoredFormValues()
    reset(defaultFormValues)
    navigate('/etapa-1')
  }

  return (
    <WizardStepShell title="Resumo do Cadastro" description="Confira seus dados antes de exportar o PDF.">
      <SummarySection
        title="Dados Pessoais"
        onEdit={() => navigate('/etapa-1')}
        items={[
          { label: 'Nome completo', value: values.fullName },
          { label: 'Data de nascimento', value: values.birthDate },
          { label: 'CPF', value: values.cpf },
          { label: 'Telefone', value: values.phone },
          { label: 'E-mail', value: values.email },
        ]}
      />
      <SummarySection
        title="Informações Residenciais"
        onEdit={() => navigate('/etapa-2')}
        items={[
          { label: 'CEP', value: values.cep },
          { label: 'Endereço', value: values.endereco },
          { label: 'Bairro', value: values.bairro },
          { label: 'Cidade', value: values.cidade },
          { label: 'Estado', value: getOptionLabel(UF_OPTIONS, values.estado) },
        ]}
      />
      <SummarySection
        title="Informações Profissionais"
        onEdit={() => navigate('/etapa-3')}
        items={[
          { label: 'Empresa', value: values.empresa },
          { label: 'Profissão', value: values.profissao },
          { label: 'Salário', value: values.salario },
          { label: 'Tempo de empresa', value: getOptionLabel(TEMPO_EMPRESA_OPTIONS, values.tempoDeEmpresa) },
        ]}
      />
      <div className="wizard-actions">
        <button type="button" className="btn btn-secondary" onClick={handleNewRegistration}>
          Novo cadastro
        </button>
        <button type="button" className="btn btn-primary" onClick={handleExport}>
          Exportar PDF
        </button>
      </div>
    </WizardStepShell>
  )
}
