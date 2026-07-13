import { Controller, useFormContext } from 'react-hook-form'
import type { FormValues } from '../../schemas/formSchema'
import { onlyDigits } from '../../lib/masks'

const NON_BREAKING_SPACE = ' '

function formatBRLFromDigits(digits: string): string {
  const numeric = Number(digits || '0') / 100
  return numeric
    .toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
    .replaceAll(NON_BREAKING_SPACE, ' ')
}

interface CurrencyFieldProps {
  name: keyof FormValues
  label: string
  fullWidth?: boolean
}

export function CurrencyField({ name, label, fullWidth }: Readonly<CurrencyFieldProps>) {
  const {
    control,
    formState: { errors },
  } = useFormContext<FormValues>()
  const error = errors[name]?.message as string | undefined

  return (
    <div className={`field${fullWidth ? ' field-full' : ''}`}>
      <label htmlFor={name}>{label}</label>
      <Controller
        name={name}
        control={control}
        render={({ field }) => (
          <input
            id={name}
            inputMode="decimal"
            placeholder="R$ 0,00"
            aria-invalid={!!error}
            aria-describedby={error ? `${name}-error` : undefined}
            value={field.value ?? ''}
            onChange={(event) => {
              const digits = onlyDigits(event.target.value).replace(/^0+/, '').slice(0, 12)
              field.onChange(digits ? formatBRLFromDigits(digits) : '')
            }}
            onBlur={field.onBlur}
          />
        )}
      />
      {error && (
        <span className="field-error" id={`${name}-error`} role="alert">
          {error}
        </span>
      )}
    </div>
  )
}
