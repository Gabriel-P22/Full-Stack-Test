import { z } from 'zod'
import { isValidCPF } from '../lib/cpf'
import { isValidBirthDate, parseCurrencyToNumber } from '../lib/masks'
import { UF_OPTIONS } from '../constants/options'

const PHONE_REGEX = /^\(\d{2}\) \d{4,5}-\d{4}$/
const CEP_REGEX = /^\d{5}-\d{3}$/
const UF_CODES = UF_OPTIONS.map((option) => option.value)

export const personalDataSchema = z.object({
  fullName: z
    .string()
    .trim()
    .min(1, 'Informe o nome completo')
    .refine((value) => value.trim().split(/\s+/).length >= 2, {
      message: 'Informe nome e sobrenome',
    }),
  birthDate: z
    .string()
    .min(1, 'Informe a data de nascimento')
    .refine(isValidBirthDate, { message: 'Data de nascimento inválida' }),
  cpf: z
    .string()
    .min(1, 'Informe o CPF')
    .refine(isValidCPF, { message: 'CPF inválido' }),
  phone: z
    .string()
    .min(1, 'Informe o telefone')
    .regex(PHONE_REGEX, 'Telefone inválido'),
  email: z.string().min(1, 'Informe o e-mail').email('E-mail inválido'),
})

export const addressSchema = z.object({
  cep: z.string().min(1, 'Informe o CEP').regex(CEP_REGEX, 'CEP inválido'),
  endereco: z.string().min(1, 'Informe o endereço'),
  bairro: z.string().min(1, 'Informe o bairro'),
  cidade: z.string().min(1, 'Informe a cidade'),
  estado: z
    .string()
    .min(1, 'Informe o estado')
    .refine((value) => UF_CODES.includes(value.toUpperCase()), {
      message: 'Estado inválido',
    }),
})

export const professionalSchema = z.object({
  empresa: z.string().min(1, 'Informe a empresa'),
  profissao: z.string().min(1, 'Selecione uma profissão'),
  salario: z
    .string()
    .min(1, 'Informe o salário')
    .refine((value) => parseCurrencyToNumber(value) > 0, {
      message: 'Informe um salário válido',
    }),
  tempoDeEmpresa: z.string().min(1, 'Selecione o tempo de empresa'),
})

export const formSchema = personalDataSchema.merge(addressSchema).merge(professionalSchema)

export type FormValues = z.infer<typeof formSchema>

export const STEP1_FIELDS = Object.keys(personalDataSchema.shape) as (keyof FormValues)[]
export const STEP2_FIELDS = Object.keys(addressSchema.shape) as (keyof FormValues)[]
export const STEP3_FIELDS = Object.keys(professionalSchema.shape) as (keyof FormValues)[]

export const defaultFormValues: FormValues = {
  fullName: '',
  birthDate: '',
  cpf: '',
  phone: '',
  email: '',
  cep: '',
  endereco: '',
  bairro: '',
  cidade: '',
  estado: '',
  empresa: '',
  profissao: '',
  salario: '',
  tempoDeEmpresa: '',
}
