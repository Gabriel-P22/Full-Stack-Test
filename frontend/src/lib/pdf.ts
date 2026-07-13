import { jsPDF } from 'jspdf'
import type { FormValues } from '../schemas/formSchema'
import { getOptionLabel, TEMPO_EMPRESA_OPTIONS, UF_OPTIONS } from '../constants/options'

const VERITY_BLUE: [number, number, number] = [13, 71, 161]
const VERITY_BLUE_DARK: [number, number, number] = [8, 43, 99]
const TEXT_GRAY: [number, number, number] = [60, 68, 82]
const MARGIN_X = 16

interface Section {
  title: string
  rows: [string, string][]
}

function buildSections(data: FormValues): Section[] {
  return [
    {
      title: 'Dados Pessoais',
      rows: [
        ['Nome completo', data.fullName],
        ['Data de nascimento', data.birthDate],
        ['CPF', data.cpf],
        ['Telefone', data.phone],
        ['E-mail', data.email],
      ],
    },
    {
      title: 'Informações Residenciais',
      rows: [
        ['CEP', data.cep],
        ['Endereço', data.endereco],
        ['Bairro', data.bairro],
        ['Cidade', data.cidade],
        ['Estado', getOptionLabel(UF_OPTIONS, data.estado)],
      ],
    },
    {
      title: 'Informações Profissionais',
      rows: [
        ['Empresa', data.empresa],
        ['Profissão', data.profissao],
        ['Salário', data.salario],
        ['Tempo de empresa', getOptionLabel(TEMPO_EMPRESA_OPTIONS, data.tempoDeEmpresa)],
      ],
    },
  ]
}

export function generateSummaryPdf(data: FormValues): void {
  const doc = new jsPDF()
  const pageWidth = doc.internal.pageSize.getWidth()

  doc.setFillColor(...VERITY_BLUE)
  doc.rect(0, 0, pageWidth, 30, 'F')
  doc.setTextColor(255, 255, 255)
  doc.setFont('helvetica', 'bold')
  doc.setFontSize(18)
  doc.text('verity', MARGIN_X, 19)
  doc.setFontSize(11)
  doc.setFont('helvetica', 'normal')
  doc.text('Resumo do Cadastro', pageWidth - MARGIN_X, 19, { align: 'right' })

  let cursorY = 44

  for (const section of buildSections(data)) {
    doc.setTextColor(...VERITY_BLUE_DARK)
    doc.setFont('helvetica', 'bold')
    doc.setFontSize(13)
    doc.text(section.title, MARGIN_X, cursorY)
    cursorY += 3
    doc.setDrawColor(...VERITY_BLUE)
    doc.line(MARGIN_X, cursorY, pageWidth - MARGIN_X, cursorY)
    cursorY += 8

    doc.setFontSize(11)
    for (const [label, value] of section.rows) {
      doc.setFont('helvetica', 'bold')
      doc.setTextColor(...TEXT_GRAY)
      doc.text(`${label}:`, MARGIN_X, cursorY)
      doc.setFont('helvetica', 'normal')
      doc.text(value || '—', MARGIN_X + 55, cursorY)
      cursorY += 7
    }

    cursorY += 6
  }

  const generatedAt = new Date().toLocaleString('pt-BR')
  doc.setFontSize(9)
  doc.setTextColor(...TEXT_GRAY)
  doc.text(`Gerado em ${generatedAt}`, MARGIN_X, doc.internal.pageSize.getHeight() - 10)

  const fileSuffix = data.fullName.trim().toLowerCase().replace(/\s+/g, '-') || 'cadastro'
  doc.save(`verity-cadastro-${fileSuffix}.pdf`)
}
